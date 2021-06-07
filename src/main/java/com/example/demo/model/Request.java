package com.example.demo.model;

import lombok.Data;

@Data
public class Request {
    private PayloadType payloadType;
    private Payload payload;
}
