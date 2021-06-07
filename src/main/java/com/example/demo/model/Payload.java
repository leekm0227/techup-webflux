package com.example.demo.model;

import lombok.Data;

@Data
public class Payload {
    private ReceiveType receiveType;
    private String sessionId;
    private String channelId;
    private String receiver;
    private String body;
}
