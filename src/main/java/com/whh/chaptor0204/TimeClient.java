package com.whh.chaptor0204;

/**
 * timeClient
 * Created by xuzhuo on 2017/5/17.
 */
public class TimeClient {
    public static void main(String[] args) {
        int port = 7777;
        new Thread(new AsyncTimeClientHandler("127.0.0.1", port), "AIO-AsyncTimeClientHandler-001").start();
    }
}
