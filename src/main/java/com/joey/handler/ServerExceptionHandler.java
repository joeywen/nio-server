package com.joey.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by joey on 2014-7-2.
 */
public class ServerExceptionHandler implements Thread.UncaughtExceptionHandler {
    private final static Logger logger = LoggerFactory.getLogger(ServerExceptionHandler.class);

	public void uncaughtException(Thread t, Throwable e) {
		// TODO Auto-generated method stub
		System.out.println("uncaught exception : " + e.getMessage());
	}
}
