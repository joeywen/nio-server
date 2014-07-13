package com.joey;

import com.joey.server.JdbServer;

/**
 * Created by joey on 14-7-4.
 */
public class Main {

    public static void main(String[] args) {
        JdbServer server = new JdbServer();
        server.setDaemon(true);
        server.listening();
    }

}
