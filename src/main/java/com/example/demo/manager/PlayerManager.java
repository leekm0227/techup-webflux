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
    private final ConcurrentHashMap<String, Player> playerMap;
    private final ConcurrentHashMap<String, Integer[]> posMap;
    private final ConcurrentHashMap<String, Integer[]> hpMap;
    private final int MAX_X = 100;
    private final int MAX_Y = 100;
    private final int MIN = 0;

    public PlayerManager(BroadcastPublisher broadcastPublisher) {
        this.broadcastPublisher = broadcastPublisher;
        this.playerMap = new ConcurrentHashMap<>();
        this.posMap = new ConcurrentHashMap<>();
        this.hpMap = new ConcurrentHashMap<>();
    }

    public ConcurrentHashMap<String, Player> list() {
        return playerMap;
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
        playerMap.put(sessionId, player);
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
        playerMap.remove(sessionId);
        posMap.remove(sessionId);
        hpMap.remove(sessionId);

        Request request = new Request();
        request.setPayloadType(PayloadType.DEAD);
        request.setReceiveType(ReceiveType.CHANNEL);
        request.setRegTime(System.currentTimeMillis());
        request.setSessionId(sessionId);
        broadcastPublisher.next(request);
    }

    public Player move(String sessionId, int[] dir) {
        Player player = playerMap.get(sessionId);
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

        return playerMap.get(sessionId);
    }

    public Player attack(String sessionId, String targetId) {
        Player player = playerMap.get(sessionId);
        Player target = playerMap.get(targetId);
        if (player == null || target == null) return null;

        int x = player.getPos()[0] - target.getPos()[0];
        int y = player.getPos()[1] - target.getPos()[1];
        double dis = Math.sqrt(Math.abs(x * x) + Math.abs(y * y));

        // range: 1
        if (dis < 2) {
            hpMap.computeIfPresent(targetId, (key, hp) -> {
                hp[0] = hp[0] - player.getPower();
                return hp;
            });
        }

        return playerMap.get(targetId);
    }
}
