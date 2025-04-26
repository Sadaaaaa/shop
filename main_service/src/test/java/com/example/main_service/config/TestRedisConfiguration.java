//package com.example.main_service.config;
//
//import jakarta.annotation.PostConstruct;
//import jakarta.annotation.PreDestroy;
//import org.springframework.boot.test.context.TestConfiguration;
//import redis.embedded.RedisServer;
//
//@TestConfiguration
//public class TestRedisConfiguration {
//
//    private RedisServer redisServer;
//
//    @PostConstruct
//    public void postConstruct() {
//        redisServer = new RedisServer(6379);
//        redisServer.start();
//    }
//
//    @PreDestroy
//    public void preDestroy() {
//        if (redisServer != null) {
//            redisServer.stop();
//        }
//    }
//}