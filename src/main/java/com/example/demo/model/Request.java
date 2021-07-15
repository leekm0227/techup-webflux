package com.example.demo.model;

import com.example.demo.model.type.PayloadType;
import com.example.demo.model.type.ReceiveType;
import lombok.Data;

@Data
public class Request {
    private PayloadType payloadType;
    private String sessionId;
    private long regTime;

    // channel
    private String channelId;

    // broadcast
    private ReceiveType receiveType;
    private String receiver;
    private String body;

    // move
    private int[] dir;
    private int[] pos;

    // attack
    private String targetId;
}
