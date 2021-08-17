package com.example.demo.manager;

import com.example.demo.model.Player;
import com.example.demo.model.Request;
import com.example.demo.model.type.PayloadType;
import com.example.demo.model.type.ReceiveType;
import com.example.demo.publisher.BroadcastPublisher;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PlayerManager {
    private final BroadcastPublisher broadcastPublisher;
    private final ConcurrentHashMap<String, Integer[]> posMap;
    private final ConcurrentHashMap<String, Integer[]> hpMap;
    private final int MAX_X = 100;
    private final int MAX_Y = 100;
    private final int MIN = 0;

    public PlayerManager(BroadcastPublisher broadcastPublisher) {
        this.broadcastPublisher = broadcastPublisher;
        this.posMap = new ConcurrentHashMap<>();
        this.hpMap = new ConcurrentHashMap<>();
    }

    public HashMap<String, Player> list() {
        HashMap<String, Player> result = new HashMap<>();

        for (String key : posMap.keySet()) {
            result.put(key, new Player() {{
                setId(key);
                setPos(posMap.get(key));
                setHp(hpMap.get(key));
            }});
        }

        return result;
    }

    public void spawn(String sessionId) {
        Player player = new Player();

        // set status
        player.setId(sessionId);
        player.setPower(1);
        player.setHp(new Integer[]{10, 0});

        // set random pos
        Random rand = new Random();
        player.setPos(new Integer[]{rand.nextInt(MAX_X), rand.nextInt(MAX_Y)});
        posMap.put(sessionId, player.getPos());
        hpMap.put(sessionId, player.getHp());

        // send request
        Request request = new Request();
        request.setPayloadType(PayloadType.SPAWN);
        request.setReceiveType(ReceiveType.CHANNEL);
        request.setRegTime(System.currentTimeMillis());
        request.setSessionId(sessionId);
        request.setPlayer(player);
        broadcastPublisher.next(request);
    }

    public void dead(String sessionId) {
        posMap.remove(sessionId);
        hpMap.remove(sessionId);

        Request request = new Request();
        request.setPayloadType(PayloadType.DEAD);
        request.setReceiveType(ReceiveType.CHANNEL);
        request.setRegTime(System.currentTimeMillis());
        request.setSessionId(sessionId);
        broadcastPublisher.next(request);
    }

    public Integer[] move(String sessionId, int[] dir) {
        Integer[] player = posMap.get(sessionId);
        if (player == null) return null;

        posMap.computeIfPresent(sessionId, (key, pos) -> {
            int x = pos[0];
            int y = pos[1];

            x += dir[0];
            y += dir[1];

            if (x < MIN) x = MIN;
            if (x > MAX_X) x = MAX_X;
            if (y < MIN) y = MIN;
            if (y > MAX_Y) y = MAX_Y;

            pos[0] = x;
            pos[1] = y;

            return pos;
        });

        return posMap.get(sessionId);
    }

    public Integer[] attack(String sessionId, String targetId) {
        Integer[] playerPos = posMap.get(sessionId);
        Integer[] targetPos = posMap.get(targetId);
        if (playerPos == null || targetPos == null) return null;

        int x = playerPos[0] - targetPos[0];
        int y = playerPos[1] - targetPos[1];
        double dis = Math.sqrt(Math.abs(x * x) + Math.abs(y * y));

        // range: 1
        if (dis < 2) {
            hpMap.computeIfPresent(targetId, (key, hp) -> {
                hp[0] = hp[0] - 1;
                return hp;
            });
        }

        return hpMap.get(targetId);
    }
}
