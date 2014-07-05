package com.doogga.server;

import com.doogga.common.Pair;
import com.doogga.common.ProtoHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author joey@doogga.com
 * @version 1.0.0
 * @time 2014-07-02
 */
public abstract class AbstractServer extends Thread {

    private final static Logger logger = LoggerFactory.getLogger(AbstractServer.class);

	private enum State {STOPPED, STOPPING, RUNNING}

	private static int DEFAULT_MESSAGE_SIZE = 65535;
    private final int port;
    private String host;
    private Selector selector = null;
    private ServerSocketChannel server = null;
	private final AtomicReference<State> state = new AtomicReference<State>(State.STOPPED);

    protected final int defaultBufferSize;
    protected final ConcurrentHashMap<SelectionKey, ByteBuffer> readBuffersMap = new ConcurrentHashMap<SelectionKey, ByteBuffer>();
    protected AbstractServer(int port) {
    	this(port, DEFAULT_MESSAGE_SIZE);
    }

    protected AbstractServer(int port, int defaultBufferSize) {
        this(null, port, defaultBufferSize);

    }

    protected AbstractServer(String host, int port, int defaultBufferSize) {
        this.port = port;
        this.defaultBufferSize = defaultBufferSize;

        this.host = host;
    }

    public int getPort() {
    	return port;
    }

    public String getHostName() {
        return this.host;
    }

    public InetSocketAddress getAddress() {
        try {
            if (this.host == null) {
                return new InetSocketAddress(this.port);
            } else {
                return new InetSocketAddress(host, this.port);
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isRunning() {
    	return state.get() == State.RUNNING;
    }

    public boolean isStopped() {
    	return state.get() == State.STOPPED;
    }

    public void connect() {
        if (!state.compareAndSet(State.STOPPED, State.RUNNING)) {
            System.out.println("server start twice...");
            return;
        }
        System.out.println("server starting...");

        try {
            selector = Selector.open();
            server = ServerSocketChannel.open();
            server.socket().setReuseAddress(true);
            server.socket().bind(this.getAddress());
            server.configureBlocking(false);
            server.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("server started...");
            //Thread.currentThread().sleep(20);
        } catch (Exception e) {
            System.out.println("catch server start error ...");
            logger.error("Server start error. stop");
            throw new RuntimeException("Server failure: " + e);
        }
    }

    /**
     * starting listening client connect
     */
    public void listening() {
        if (!this.isRunning()) {
            System.out.println("server is not started yet .");
            logger.info("Server is not bind yet. Start it using #connect()# method first !");
            connect();
            // return;
        }

        this.start();
    }

    /**
     * Start the server running - accepting connections, receiving messages. If the server is
     * already running, it will not be started again. This method is designed to be called in
     * its own thread and will not return until the server is stopped.
     *
     * @throws RuntimeException if the server fails
     */
    public void run() {
        // ensure that the server is not started twice
        try {
            while (state.get() == State.RUNNING) {
                selector.select(10); // check every 10ms whether the server has been requested to stop
                for (Iterator<SelectionKey> i = selector.selectedKeys().iterator(); i.hasNext(); ) {
                    SelectionKey key = i.next();
                    try {
                        i.remove();

                        if (key.isConnectable()) {
                            ((SocketChannel) key.channel()).finishConnect();
                        }

                        if (key.isAcceptable()) {
                            // accept connection
                            SocketChannel client = server.accept();
                            client.configureBlocking(false);
                            client.socket().setTcpNoDelay(true);
                            connection(client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE));

                            /** just for info log, test
                             try {
                             InetSocketAddress addr = (InetSocketAddress) client.getRemoteAddress();
                             logger.info(String.format("Accept message from host:[%s:%d]", addr.getHostName(), addr.getPort()));
                             } catch (ClassCastException e) {
                             // do nothing
                             }*/
                        }

                        if (key.isReadable()) {
                            for (Pair pair : readIncomingMessage(key)) {
                                messageReceived(pair);
                            }
                        }

                    } catch (IOException ioe) {
                        System.out.println("selection key error...");
                        key.cancel();
                        readBuffersMap.remove(key);
                        disconnected(key);
                    }
                }

                Thread.currentThread().sleep(10);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Server failure: " + e.getMessage());
        } finally {
            System.out.println("finally...");
            this.stop_server();
        }
    }

    private List<Pair> readIncomingMessage(SelectionKey key) throws IOException {
        ByteBuffer readBuffer = readBuffersMap.get(key);
        if (readBuffer==null) {
            readBuffer = ByteBuffer.allocate(defaultBufferSize);
            readBuffersMap.put(key, readBuffer);
        }
        if (((ReadableByteChannel)key.channel()).read(readBuffer)==-1) {
            throw new IOException("Read on closed key");
        }

        readBuffer.flip();
        List<Pair> result = new ArrayList<Pair>();

        Pair pair = readMessage(key, readBuffer);
        while (pair!=null) {
            result.add(pair);
            pair = readMessage(key, readBuffer);
        }

        return result;
    }

    protected Pair readMessage(SelectionKey key, ByteBuffer readBuffer) {
        int bytesToRead;
        ProtoHeader header;
        if (readBuffer.remaining() > ProtoHeader.HEADER_BYTE_LENGTH) { // must have at least header bytes to read the size of the message
            byte[] lengthBytes = new byte[ProtoHeader.HEADER_BYTE_LENGTH];
            readBuffer.get(lengthBytes);
            header = ProtoHeader.parseHeader(ByteBuffer.wrap(lengthBytes));
            bytesToRead = header.len -  header.hlen; // get the message body length
            if ((readBuffer.limit() - readBuffer.position()) < bytesToRead) {
                // Not enough data - prepare for writing again
                if (readBuffer.limit() == readBuffer.capacity()) {
                    // message may be longer than buffer => resize buffer to message size
                    int oldCapacity = readBuffer.capacity();
                    ByteBuffer tmp = ByteBuffer.allocate(bytesToRead + ProtoHeader.HEADER_BYTE_LENGTH);
                    readBuffer.position(0);
                    tmp.put(readBuffer);
                    readBuffer = tmp;
                    readBuffer.position(oldCapacity);
                    readBuffer.limit(readBuffer.capacity());
                    readBuffersMap.put(key, readBuffer);
                    return null;
                } else {
                    // rest for writing
                    readBuffer.position(readBuffer.limit());
                    readBuffer.limit(readBuffer.capacity());
                    return null;
                }
            }
        } else {
            // Not enough data - prepare for writing again
            logger.info("Not enough data - prepare for writing again");
            System.out.println("cc Not enough data - prepare for writing again");
            readBuffer.position(readBuffer.limit());
            readBuffer.limit(readBuffer.capacity());
            return null;
        }

        byte[] resultMessage = new byte[bytesToRead];
        readBuffer.get(resultMessage, 0, bytesToRead);
        // remove read message from buffer
        int remaining = readBuffer.remaining();
        readBuffer.limit(readBuffer.capacity());
        readBuffer.compact();
        readBuffer.position(0);
        readBuffer.limit(remaining);

        /**
         * add concurrent hash map to selection key attachment
         * so that consumer worker can get the Map and remove
         * the selection key if the key is closed.
         */
        key.attach(readBuffersMap);

        return new Pair(key, new Pair(header, ByteBuffer.wrap(resultMessage)));
    }

    public boolean stop_server() {
        try {
            selector.close();
            server.socket().close();
            server.close();
            state.set(State.STOPPED);
        } catch (Exception e) {
            // do nothing - server failed
        }

    	return state.compareAndSet(State.RUNNING, State.STOPPING);
    }

    // =================== abstract method ============================== //
    protected abstract void messageReceived(Pair pair);

    protected abstract void connection(SelectionKey key);

    protected abstract void disconnected(SelectionKey key);

}
