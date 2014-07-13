package com.joey.server;

import com.joey.common.JobQueue;
import com.joey.common.Pair;
import com.joey.handler.ServerExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.SelectionKey;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author joey@joey.com
 * @datetime 2014-7-2.
 */
public final class JdbServer extends AbstractServer {
    private static final Logger logger = LoggerFactory.getLogger(JdbServer.class);

    private static final int DEFAULT_PORT = 4321;

    private int port;

    private MessageConsumer consumer;

    private AtomicReference<SelectionKey> connectKey = new AtomicReference<SelectionKey>();
    private JobQueue<Pair> jobQueue;
    private ServerExceptionHandler serverExceptionHandler;

    public JdbServer() {
        this(DEFAULT_PORT);
    }

    public JdbServer(int port) {
        super(port);
        this.port = port;
        init_members();

        // this.setDaemon(true);
    }

    private void init_members() {
        this.serverExceptionHandler = new ServerExceptionHandler();
        this.setUncaughtExceptionHandler(serverExceptionHandler);

        /** start JobQueue monitor thread */
        this.jobQueue = JobQueue.getInstance();
        this.consumer = new MessageConsumer(jobQueue);
        this.consumer.start();
    }

    protected void messageReceived(Pair pair) {
        System.out.println("message received ... ");
        if (jobQueue == null) {
            this.jobQueue = JobQueue.getInstance();
            this.consumer.setJobQueue(this.jobQueue);
        }
        this.jobQueue.add(pair);
    }

    public void setJobQueue(JobQueue<Pair> jobQueue) {
        this.jobQueue = jobQueue;
    }

    @Override
    protected void connection(SelectionKey key) {
        // TODO process after accept client connected
        connectKey.set(key);
    }

    @Override
    protected void disconnected(SelectionKey key) {
        key.cancel();
    }
}
