package com.whh.chaptor0204;

/**
 * AIO
 * Created by xuzhuo on 2017/5/17.
 */
public class TimeServer {
    public static void main(String[] args) {
        int port = 7777;
        AsyncTimeServerHandler timeServerHandler = new AsyncTimeServerHandler(port);
        new Thread(timeServerHandler, "AIO-AsyncTimeServerHandler-001").start();
    }
}
