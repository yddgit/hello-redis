package com.my.project.redis;

import redis.clients.jedis.Jedis;

/**
 * Redis in Java
 */
public class HelloRedis {

	/** Redis Server */
	private static final String REDIS_HOST = "127.0.0.1";
	/** Redis Server Port */
	private static final int REDIS_PORT = 6379;
	/** Redis Auth Password */
	private static final String REDIS_PASS = "123456";

	public static void main(String[] args) {

		Jedis jedis = null;

		try {
			jedis = new Jedis(REDIS_HOST, REDIS_PORT);
			jedis.auth(REDIS_PASS);
			jedis.set("foo", "bar");
			String value = jedis.get("foo");
			System.out.println("foo=" + value);
			System.out.println("hackers=" + jedis.zrange("hackers", 0, -1));
			System.out.println("superpowers=" + jedis.smembers("superpowers"));
			System.out.println("user:1000=" + jedis.hgetAll("user:1000"));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis != null) {
				jedis.close();
			}
		}

	}

}
