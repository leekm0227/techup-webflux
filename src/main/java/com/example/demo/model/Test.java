package com.example.demo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "MST_FLEA_MARKET_ENTRY_ITEM")
public class Test {

    @Id
    private String id;
    private PayloadType payloadType;
    private int rare;
    private long regTime;
}
