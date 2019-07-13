package com.org.project.configserver.config.message;

import org.springframework.data.redis.listener.ChannelTopic;

/**
 * The publisher interface to work with Pub/Sub mechanism with Redis
 * @author Sriram Tanikella <sriram18981@gmail.com>
 *
 */
public interface MessagePublisher {

	/**
	 * Publish the message to the Redis channel
	 * @param message
	 */
	void publish(ChannelTopic topic, String message);
}
