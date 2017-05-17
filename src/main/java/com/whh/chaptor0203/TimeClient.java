package com.whh.chaptor0203;

/**
 * NIO cline
 * Created by xuzhuo on 2017/5/16.
 */
public class TimeClient {
    public static void main(String[] args) {
        new Thread(new TimeClientHandle("127.0.0.1", 7777)).start();
    }
}
