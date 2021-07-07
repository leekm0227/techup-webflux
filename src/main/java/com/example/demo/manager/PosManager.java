package com.example.demo.manager;

import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PosManager {
    private final ConcurrentHashMap<String, int[]> posMap;
    private final int MAX_X = 150;
    private final int MAX_Y = 60;
    private final int MIN = 0;

    public PosManager() {
        this.posMap = new ConcurrentHashMap<>();
    }

    public int[] init(String sessionId) {
        Random rand = new Random();
        int[] pos = new int[]{rand.nextInt(MAX_X), rand.nextInt(MAX_Y)};
        posMap.put(sessionId, pos);
        return pos;
    }

    public int[] getPos(String sessionId) {
        return posMap.getOrDefault(sessionId, init(sessionId));
    }

    public void leave(String sessionId) {
        posMap.remove(sessionId);
    }

    public int[] move(String sessionId, int[] dir) {
        return posMap.computeIfPresent(sessionId, (key, pos) -> {
            pos[0] += dir[0];
            pos[1] += dir[1];
            if (pos[0] < MIN) pos[0] = MIN;
            if (pos[0] > MAX_X) pos[0] = MAX_X;
            if (pos[1] < MIN) pos[1] = MIN;
            if (pos[1] > MAX_Y) pos[1] = MAX_Y;
            return pos;
        });
    }
}
