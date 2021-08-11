package com.example.demo.manager;

import com.example.demo.model.Player;
import com.example.demo.model.Request;
import com.example.demo.model.type.PayloadType;
import com.example.demo.model.type.ReceiveType;
import com.example.demo.publisher.BroadcastPublisher;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PlayerManager {
    private final BroadcastPublisher broadcastPublisher;
    private final ConcurrentHashMap<String, Player> playerMap;
    private final int MAX_X = 100;
    private final int MAX_Y = 100;
    private final int MIN = 0;

    public PlayerManager(BroadcastPublisher broadcastPublisher) {
        this.broadcastPublisher = broadcastPublisher;
        this.playerMap = new ConcurrentHashMap<>();
    }

    public ConcurrentHashMap<String, Player> list() {
        return playerMap;
    }

    public void spawn(String sessionId) {
        Player player = new Player();

        // set status
        player.setId(sessionId);
        player.setPower(1);
        player.setHp(10);

        // set random pos
        Random rand = new Random();
        int[] pos = new int[]{rand.nextInt(MAX_X), rand.nextInt(MAX_Y)};
        player.setPos(pos);
        playerMap.put(sessionId, player);

        // send request
        Request request = new Request();
        request.setPayloadType(PayloadType.SPAWN);
        request.setReceiveType(ReceiveType.CHANNEL);
        request.setSessionId(sessionId);
        request.setPlayer(player);
        broadcastPublisher.next(request);
    }

    public void dead(String sessionId) {
        playerMap.remove(sessionId);

        Request request = new Request();
        request.setPayloadType(PayloadType.DEAD);
        request.setReceiveType(ReceiveType.CHANNEL);
        request.setSessionId(sessionId);
        broadcastPublisher.next(request);
    }

    public Player move(String sessionId, int[] dir) {
        return playerMap.computeIfPresent(sessionId, (key, player) -> {
            int x = player.getPos()[0];
            int y = player.getPos()[1];

            x += dir[0];
            y += dir[1];

            if (x < MIN) x = MIN;
            if (x > MAX_X) x = MAX_X;
            if (y < MIN) y = MIN;
            if (y > MAX_Y) y = MAX_Y;

            player.getPos()[0] = x;
            player.getPos()[1] = y;

            return player;
        });
    }

    public Player attack(String sessionId, String targetId) {
        Player player = playerMap.get(sessionId);
        if (player == null) return null;

        playerMap.computeIfPresent(targetId, (key, target) -> {
            int x = player.getPos()[0] - target.getPos()[0];
            int y = player.getPos()[1] - target.getPos()[1];

            // range: 1
            if (Math.sqrt(Math.abs(x * x) + Math.abs(y * y)) < 2) {
                int hp = target.getHp() - player.getPower();
                target.setHp(hp);
            }

            return target;
        });

        return playerMap.get(targetId);
    }
}
