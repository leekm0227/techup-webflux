package com.example.demo.handler;

import com.example.demo.model.PayloadType;
import com.example.demo.model.Request;
import com.example.demo.model.Test;
import com.example.demo.repository.TestRepository1;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class EventHandler1 {

    private final ObjectMapper mapper;
    private final TestRepository1 repository;

    public EventHandler1(TestRepository1 repository) {
        this.mapper = new ObjectMapper();
        this.repository = repository;
    }

    public String handle(String payloadText) {
        try {
            Request request = mapper.readValue(payloadText, Request.class);

            Test test = new Test() {{
                setRegTime(request.getRegTime());
            }};

            if(request.getPayloadType() == PayloadType.TEST){
                test.setPayloadType(PayloadType.TEST);
                return mapper.writeValueAsString(test);
            }else{
                test.setPayloadType(PayloadType.FIND_TEST);
                return mapper.writeValueAsString(repository.findByRare(request.getCode()).orElse(test));
            }
        } catch (JsonProcessingException e) {
            return "err";
        }
    }
}
