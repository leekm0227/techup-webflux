package com.example.demo.manager;

import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PosManager {
    private final ConcurrentHashMap<String, int[]> posMap;

    public PosManager() {
        this.posMap = new ConcurrentHashMap<>();
    }

    public int[] init(String sessionId) {
        Random rand = new Random();
        int[] pos = new int[]{rand.nextInt(1000), rand.nextInt(1000)};
        posMap.put(sessionId, pos);
        return pos;
    }

    public void leave(String sessionId) {
        posMap.remove(sessionId);
    }

    public int[] move(String sessionId, int[] dir) {
        posMap.computeIfPresent(sessionId, (key, ints) -> {
            ints[0] += dir[0];
            ints[1] += dir[1];
            return ints;
        });

        return posMap.get(sessionId);
    }
}
