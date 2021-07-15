package com.example.demo.manager;

import com.example.demo.model.Player;
import com.example.demo.model.type.PlayerType;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PlayerManager {
    private final ConcurrentHashMap<String, Player> playerMap;
    private final int MAX_X = 1000;
    private final int MAX_Y = 1000;
    private final int MIN = 0;

    public PlayerManager() {
        this.playerMap = new ConcurrentHashMap<>();
    }

    public Player init(String sessionId) {
        Player player = new Player();

        // set status

        // set random pos
        Random rand = new Random();
        int[] pos = new int[]{rand.nextInt(MAX_X), rand.nextInt(MAX_Y)};
        player.setPos(pos);

        playerMap.put(sessionId, player);
        return player;
    }

    public Player getPlayer(String sessionId) {
        return playerMap.getOrDefault(sessionId, init(sessionId));
    }

    public void leave(String sessionId) {
        playerMap.remove(sessionId);
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

    public void Attack(String sessionId, String targetId){


    }
}
