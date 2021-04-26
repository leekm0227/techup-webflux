package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EnemyManager {

    private static int MAX = 20;
    private static String chars[] = "a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z".split(",");
    private ConcurrentHashMap<String, Long> enemies;

    @Autowired
    ScoreManager scoreManager;

    @Autowired
    EnemyPublisher enemyPublisher;

    @PostConstruct
    private void init() {
        this.enemies = new ConcurrentHashMap<>();
        while (enemies.size() < MAX) {
            addEnemy(enemies);
        }

        enemyPublisher.next(new HashMap<>(enemies));
    }

    public void next(String sid, String key) {
        enemies.computeIfPresent(key, (s, l) -> {
            if (System.currentTimeMillis() < l) {
                enemies.remove(key);
                scoreManager.next(sid);
                enemyPublisher.next(new HashMap<>(enemies));
            }

            return l;
        });
    }

    public void update() {
        long cur = System.currentTimeMillis();
        enemies.entrySet().removeIf(entry -> entry.getValue() < cur);

        if (enemies.size() < MAX) {
            addEnemy(enemies);
            enemyPublisher.next(new HashMap<>(enemies));
        }
    }

    private Long addEnemy(ConcurrentHashMap<String, Long> enemies) {
        StringBuilder buffer = new StringBuilder();
        Random random = new Random();

        int size = random.nextInt(10) + 5;
        long expire = System.currentTimeMillis() + ((random.nextInt(10) + 5) * 1000);

        for (int i = 0; i < size; i++) {
            buffer.append(chars[random.nextInt(chars.length)]);
        }

        String key = buffer.toString();
        return enemies.putIfAbsent(key, expire);
    }
}
