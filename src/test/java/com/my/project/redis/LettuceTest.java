package com.my.project.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.junit.*;
import redis.embedded.RedisServer;

import java.io.IOException;
import java.net.ServerSocket;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class LettuceTest {
    private RedisClient client;
    private StatefulRedisConnection<String, String> connection;
    private RedisCommands<String, String> commands;

    private static RedisServer server;
    private static int PORT;

    private static final String HOSTNAME = "127.0.0.1";
    private static final String PASSWORD = "123456";

    @BeforeClass
    public static void beforeClass() throws IOException {
        PORT = randomPort();
        server = RedisServer.builder().port(PORT)
                .setting("bind " + HOSTNAME)
                .setting("requirepass " + PASSWORD)
                .build();
        server.start();
    }

    @AfterClass
    public static void afterClass() {
        server.stop();
    }

    @Before
    public void before() {
        this.client = RedisClient.create(RedisURI.builder()
                .withPassword(PASSWORD)
                .withHost(HOSTNAME)
                .withPort(PORT)
                .withDatabase(0).build());
        this.connection = this.client.connect();
        this.commands = this.connection.sync();
    }

    @After
    public void after() {
        this.connection.close();
        this.client.shutdown();
    }

    @Test
    public void testSetGetDel() {
        String key = "name";
        String value = "yang";
        this.commands.set(key, value);
        assertEquals(value, this.commands.get(key));
        this.commands.del(key);
        assertNull(this.commands.get(key));
    }

    private static Integer randomPort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}
