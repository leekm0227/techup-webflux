package com.example.demo.model;

import lombok.Data;

@Data
public class Request {
    private PayloadType payloadType;
    private ReceiveType receiveType;
    private String sessionId;
    private String channelId;
    private String receiver;
    private String body;
    private int[] dir;
    private int[] pos;
    private long regTime;
}
