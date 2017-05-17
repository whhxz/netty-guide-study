package com.whh.chaptor0203;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NIO server
 * Created by xuzhuo on 2017/5/16.
 */
public class TimeServer {
    private static final Logger logger = LoggerFactory.getLogger(TimeServer.class);

    public static void main(String[] args) {
        new Thread(new MultiplexerTimeServer(7777), "NIO-MultiplexerTimeServer-001").start();
    }
}
