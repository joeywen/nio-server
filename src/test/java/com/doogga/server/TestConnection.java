package com.doogga.server;

import com.doogga.v2.common.ProtoHeader;
import com.doogga.v2.util.LOG;
import com.doogga.v2.util.Utils;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by joey on 14-7-4.
 */
public class TestConnection {

    public static ProtoHeader genHeader() {
        ProtoHeader header = new ProtoHeader();
        int msgLen = "Hello".getBytes().length;
        header.len = ProtoHeader.HEADER_BYTE_LENGTH + msgLen;
        header.cmd = 0;
        header.seq = 0;
        header.hash = 0xE001;
        header.ret = 0;
        header.hlen = ProtoHeader.HEADER_BYTE_LENGTH;

        return header;
    }

    public static void main(String[] argv) throws Exception{
        ExecutorService service = Executors.newCachedThreadPool();
        List<Future<String>> result = new ArrayList<Future<String>>();
        for (int i = 0; i < 5; i++) {
            Demo demo = new Demo("Hello", "Hello" + i);
            result.add(service.submit(demo));
        }
        LOG.info("get result .... len : " + result.size());

        System.out.println("done");
    }

    static class Demo implements Callable<String> {

        private String name;
        private String data;
        public Demo(String data, String name) {
            this.name = name;
            this.data = data;
        }

        @Override
        public String call() throws Exception {
            String result = "";
            Socket sc = new Socket("localhost", 11000);
            ProtoHeader header = genHeader();
            OutputStream os = sc.getOutputStream();
            os.write(Utils.packHeader(header));
            os.write(String.format(data).getBytes());
            //os.flush();

            os.write(Utils.packHeader(header));
            os.write(String.format(data).getBytes());
            //os.flush();

            os.write(Utils.packHeader(header));
            os.write(String.format(data).getBytes());
            //os.flush();

            os.write(Utils.packHeader(header));
            os.write(String.format(data).getBytes());
            os.flush();

            InputStream is = sc.getInputStream();
            byte[] buffer = new byte[128];


            BufferedInputStream reader = new BufferedInputStream(is);
            int offset = 0;
            while (reader.read(buffer, offset, 24) != -1) {
                ByteBuffer bb = ByteBuffer.wrap(buffer);
                byte[] b = new byte[18];
                bb.get(b);
                int cnt = bb.limit() - bb.position();
                byte[] nn = new byte[cnt];
                bb.get(nn);
                System.out.println("back to client :" + new String(nn) + ", " + name);
                result = result + ", " + new String(nn);
                offset += 24;
            }
            //LOG.info("back to client : " + result);
            sc.close();

            return result;
        }
    }
}
