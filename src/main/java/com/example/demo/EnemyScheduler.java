package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EnemyScheduler {

    @Autowired
    EnemyManager enemyManager;

    @Scheduled(fixedDelay = 100)
    public void update(){
        enemyManager.update();
    }
}
