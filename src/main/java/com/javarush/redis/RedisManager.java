package com.javarush.redis;

import com.javarush.config.Config;
import com.sun.istack.NotNull;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisException;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisStringCommands;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@FieldDefaults(level = AccessLevel.PRIVATE)
public final class RedisManager {
    @Getter
    RedisClient client;

    StatefulRedisConnection<String, String> connection;
    RedisStringCommands<String, String> sync;

    final ExecutorService redisExecutor = Executors.newFixedThreadPool(Config.redisConfig.threadPoolSize());

    public void load() {
        if (client != null)
            throw new RedisException("Redis already loaded");

        final RedisURI uri = RedisURI.Builder
                .redis(Config.redisConfig.host(), Config.redisConfig.port())
                .withPassword(Config.redisConfig.password().toCharArray())
                .withTimeout(Duration.ofMillis(Config.redisConfig.timeout()))
                .build();
        this.client = RedisClient.create(uri);
        this.client.setOptions(ClientOptions.builder()
                .autoReconnect(true)
                .build());

        this.connection = client.connect();
        this.sync = connection.sync();
        System.out.println("\n" + "=".repeat(20) + "\nУспешное соединение с Redis\n" + "=".repeat(20) + "\n");
    }

    @NotNull
    public RedisStringCommands<String, String> sync() {
        return sync;
    }

    public void shutdown() {
        redisExecutor.shutdown();
        try {
            if (!redisExecutor.awaitTermination(Config.redisConfig.terminationAwaitSec(), TimeUnit.SECONDS))
                redisExecutor.shutdownNow();
        } catch (InterruptedException e) {
            redisExecutor.shutdownNow();
        }

        connection.close();
        client.shutdown();
    }
}
