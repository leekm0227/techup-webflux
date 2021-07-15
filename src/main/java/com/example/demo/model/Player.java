package com.example.demo.model;

import com.example.demo.model.type.ElementType;
import com.example.demo.model.type.PlayerType;
import lombok.Data;

@Data
public class Player {
    private int[] pos;
    private int hp;
    private int power;
    private PlayerType playerType;
    private ElementType elementType;
}
