package com.doogga.server;

import com.doogga.common.ProtoHeader;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AbstractServerTest {

	private static final int PORT = 11000;
	private static JdbServer server;
	
	@BeforeClass
    public static void setup() throws Exception {
        //server = new JdbServer(PORT);
        //server.start();
        //System.out.println("port is : " + PORT + ", current thread sleep 3 seconds...");
        //Thread.currentThread().sleep(3000);
	}
	
	@AfterClass
    public static void tearDown() throws Exception {
		server.stop_server();
	}

    public ProtoHeader genHeader() {
        ProtoHeader header = new ProtoHeader();
        int msgLen = "Hello!".getBytes().length;
        header.len = ProtoHeader.HEADER_BYTE_LENGTH + msgLen;
        header.cmd = 0;
        header.seq = 0;
        header.hash = 0xE001;
        header.ret = 0;
        header.hlen = ProtoHeader.HEADER_BYTE_LENGTH;

        return header;
    }


    @Test
    public void testSendMessage() throws Exception {
        server = new JdbServer(PORT);
        server.connect();
        server.listening();
        System.out.println("port is : " + PORT + ", current thread sleep 3 seconds...");
        Thread.currentThread().sleep(3000);

        assertTrue(server.isRunning());

        ProtoHeader header = genHeader();
        Socket sc = new Socket("localhost", PORT);
        OutputStream os = sc.getOutputStream();
        os.write(ProtoHeader.packHeader(header));
        //os.write("Hello!".length());
        System.out.println("Hello!".getBytes().length);
        os.write("Hello!".getBytes());
        os.flush();
        System.out.println("write done.");
        InputStream is = sc.getInputStream();
        byte[] buffer = new byte[1024];

        BufferedInputStream reader = new BufferedInputStream(is);
        while (reader.read(buffer, 0, 24) != 1) {
            ByteBuffer bb = ByteBuffer.wrap(buffer);
            byte[] b = new byte[18];
            bb.get(b);
            int cnt = bb.limit() - bb.position();


            System.out.println("limit : " + bb.limit());
            System.out.println("pos : " + bb.position());
            System.out.println("ss : " + new String(b));

            byte[] nn = new byte[cnt];
            bb.get(nn);
            System.out.println("back to client :" + new String(nn));
        }
        assertTrue(server.isRunning());
        sc.close();
    }

    @Test
    public void testSendMultipleMessage() throws Exception {
        server = new JdbServer(PORT + 1);
        server.listening();
        System.out.println("port is : " + (PORT + 1) + ", current thread sleep 3 seconds...");
        Thread.currentThread().sleep(3000);

        Socket sc = new Socket("localhost", PORT + 1);
        OutputStream os = sc.getOutputStream();
        os.write(0);
        os.write("Hello!".length());
        os.write("Hello!".getBytes());
        os.flush();
        //Thread.sleep(20);
        os.write(0);
        os.write("Again".length());
        os.write("Again".getBytes());
        os.flush();
        Thread.sleep(20);

        InputStream is = sc.getInputStream();
        byte[] buffer = new byte[128];

        BufferedInputStream reader = new BufferedInputStream(is);
        while (reader.read(buffer, 0, 20) != 1) {
            System.out.println("send multi-message, back to client :" + new String(buffer));
        }

        sc.close();
    }

    @Test
    public void testStop() throws Exception {
        server = new JdbServer(PORT + 2);
        server.listening();
        System.out.println("port is : " + (PORT + 2) + ", current thread sleep 3 seconds...");
        Thread.currentThread().sleep(3000);
        assertFalse(server.isStopped());
        assertTrue(server.stop_server());
        Thread.currentThread().sleep(3000);
        assertTrue(server.isStopped());
    }

    /** test error
	@Test
    public void testStartTwice() throws Exception {
		server.start();
        Thread.currentThread().sleep(1000);
		server.start();
        Thread.currentThread().sleep(1000);
		server.stop_server();
        Thread.currentThread().sleep(3000);
		assertTrue(server.isStopped());
	}*/

	@Test
    public void testStopTwice() throws Exception {
        server = new JdbServer(PORT + 3);
        server.listening();
        System.out.println("port is : " + (PORT + 3) + ", current thread sleep 3 seconds...");

		assertTrue(server.stop_server());
		assertFalse(server.stop_server());
		Thread.currentThread().sleep(1000);
		assertTrue(server.isStopped());
	}

	@Test
    public void testConnection() throws Exception {
        server = new JdbServer(PORT + 4);
        server.listening();
        System.out.println("port is : " + (PORT + 4) + ", current thread sleep 3 seconds...");

		Socket sc = new Socket("localhost", PORT);
		assertTrue(sc.isConnected());
		sc.close();
	}

	@Test
    public void testSendSplitMessage() throws Exception {
        server = new JdbServer(PORT + 5);
        server.listening();
        System.out.println("port is : " + (PORT + 5) + ", current thread sleep 3 seconds...");


		Socket sc = new Socket("localhost", PORT);
		OutputStream os = sc.getOutputStream();
		os.write(0);
		os.write("Hello!".length());
		os.write("Hel".getBytes());
		os.flush();
		os.write("lo!".getBytes());
		os.flush();

		Thread.currentThread().sleep(10);
        InputStream is = sc.getInputStream();
        byte[] buffer = new byte[128];

        BufferedInputStream reader = new BufferedInputStream(is);
        while (reader.read(buffer, 0, 20) != 1) {
            System.out.println("send split-message, back to client :" + new String(buffer));
        }
		assertTrue(server.isRunning());
		sc.close();
	}


    @Test
    public void testMultiConnection() throws Exception {
        server = new JdbServer(PORT);
        server.listening();
        System.out.println("port is : " + (PORT) + ", current thread sleep 3 seconds...");

        //Socket sc = new Socket("localhost", PORT);

        for (int i = 0; i < 100000; i++) {
            Socket sc = new Socket("localhost", PORT);
            OutputStream os = sc.getOutputStream();
            os.write(0);
            os.write(String.format("Hello! %d", i).length());
            os.write(String.format("Hello! %d", i).getBytes());
            os.flush();
        }

//        InputStream is = sc.getInputStream();
//        byte[] buffer = new byte[128];
//
//        BufferedInputStream reader = new BufferedInputStream(is);
//        while (reader.read(buffer, 0, 20) != 1) {
//            System.out.println("send split-message, back to client :" + new String(buffer));
//        }
    }

}
