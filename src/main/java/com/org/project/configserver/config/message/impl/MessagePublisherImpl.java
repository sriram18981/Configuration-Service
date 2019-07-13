package com.org.project.configserver.config.message.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

import com.org.project.configserver.config.message.MessagePublisher;


/**
 * Utility class to publish messages to the chosen Redis channel
 * @author Sriram Tanikella <sriram18981@gmail.com>
 *
 */
@Component("MessagePublisher")
@Scope(scopeName=ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MessagePublisherImpl implements MessagePublisher {

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	public void publish(ChannelTopic topic, String message) {
		redisTemplate.convertAndSend(topic.getTopic(), message);
	}

}
