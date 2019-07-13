package com.org.project.configserver.config.manager;

import java.net.URI;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.org.project.configserver.config.message.MessagePublisher;

/**
 * Manager to handle all the things related to Redis configuration.
 * 
 * @author Sriram Tanikella <sriram18981@gmail.com>
 *
 */

@Component
public class RedisConfigManager {
	private static final String SUBSCRIPTIONS = "subscriptions";
	private static final Logger logger = LoggerFactory.getLogger(RedisConfigManager.class);
	private JsonObject config = null;

	@Autowired
	ApplicationContext context;

	@Autowired
	RedisTemplate<String, Object> redisTemplate;

	@Autowired
	RestTemplate restTemplate;

	/**
	 * Get all the configuration stored in the Redis server
	 * 
	 * @return
	 */
	public JsonObject getConfig() {

		if (config != null) {
			return config;
		}

		Set<String> keys = redisTemplate.keys("*");
		config = new JsonObject();
		Gson gsonObj = new Gson();
		JsonParser parser = new JsonParser();

		logger.info("got keys from Redis : {}", keys);

		for (String key : keys) {
			DataType type = redisTemplate.type(key);
			logger.info("type of key {}  is {}", key, type);

			switch (type.code()) {
			case "list":
				config.add(key, parser.parse(gsonObj.toJson(redisTemplate.opsForList().range(key, 0, -1))));
				break;
			case "hash":
				config.add(key, parser.parse(gsonObj.toJson(redisTemplate.opsForHash().entries(key))));
				break;
			case "string":
				config.addProperty(key, redisTemplate.opsForValue().get(key).toString());
				break;
			case "set":
				config.add(key, parser.parse(gsonObj.toJson(redisTemplate.opsForSet().members(key))));
				break;
			case "zset":
				config.add(key, parser.parse(gsonObj.toJson(redisTemplate.opsForZSet().range(key, 0, -1))));
				config.add(key + "-with-scores",
						parser.parse(gsonObj.toJson(redisTemplate.opsForZSet().rangeWithScores(key, 0, -1))));
				break;
			default:
				logger.warn("Data type not being handled: {}", type);
				break;

			}

		}

		return config;
	}

	/**
	 * Set a hash key property for the given hash and key
	 * 
	 * @return
	 */
	public void setHashKey(String hash, String key, String value) {

		redisTemplate.opsForHash().put(hash, key, value);

	}

	/**
	 * Register for subscription for the configuration push
	 * 
	 * @param endPoint
	 */
	public String addSubscription(String endPoint) {
		long response = redisTemplate.opsForSet().add(SUBSCRIPTIONS, endPoint);
		
		String message = (response == 0) ? String.format("Subscription already registered for %s", endPoint) 
										   : String.format("Added subscription for %s", endPoint);
		
		logger.info(message);
		return message;
	}

	/**
	 * Makes the config cache invalid and forces to fetch the config from Redis
	 * again
	 */
	public void refreshConfig() {
		config = null;
		getConfig();

		MessagePublisher publisher = context.getBean("MessagePublisher", MessagePublisher.class);
		publisher.publish(new ChannelTopic("config"), "config refreshed");

		logger.debug("Pushing the configuration to the subscribed clients");
		pushConfigToSubscribers();
	}

	/**
	 * Push the configuration to the subscribed clients
	 */
	private void pushConfigToSubscribers() {

		if (config.get(SUBSCRIPTIONS) == null) {
			logger.info("No subscribers found.");
			return;
		}

		JsonArray subsciberUris = config.get(SUBSCRIPTIONS).getAsJsonArray();
		logger.info("Got the subscribers list {}", subsciberUris);

		subsciberUris.forEach(uri -> {
			try {

				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);

				HttpEntity<String> entity = new HttpEntity<>(config.toString(), headers);
				Object response = restTemplate.postForObject(new URI(uri.getAsString()), entity, Object.class);
				logger.info("successfully posted the config to {} and the response is  {}", uri, response);

			} catch (Exception e) {
				// TODO need to identify recoverable errors and take action accordingly.
				logger.error("something went wrong while pushing configuration to {}", uri, e);
			}

		});

	}

}
