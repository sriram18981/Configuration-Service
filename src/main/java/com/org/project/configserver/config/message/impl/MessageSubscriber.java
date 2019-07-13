package com.org.project.configserver.config.message.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

import com.org.project.configserver.config.manager.RedisConfigManager;

/**
 * Listener to listen from certain interesting channels from Redis
 * 
 * @author Sriram Tanikella <sriram18981@gmail.com>
 *
 */
public class MessageSubscriber implements MessageListener {
	private static final Logger logger = LoggerFactory.getLogger(MessageSubscriber.class);
	
	@Autowired
	RedisConfigManager redisManager;
	
	/**
	 * to store all the messages received, if any time, want to replay those
	 */
	private static List<String> messageList = new ArrayList<>();

	/**
	 * handler method to receive messages from Redis and take actions
	 */
	public void onMessage(final Message message, final byte[] pattern) {

        messageList.add(message.toString());
        String channelPattern = new String(pattern);
        logger.debug("Got a message '{}' on the channel '{}'", message, channelPattern);
        
        if(StringUtils.equalsIgnoreCase("config", channelPattern) 
        		&& StringUtils.equalsIgnoreCase("update", message.toString())) {
        	
        	logger.info("Recieved message from Redis to refresh the configuration.");
        	redisManager.refreshConfig();
        }
    }
	
	/**
	 * Accessor method for the messages received
	 * @return
	 */
	public List<String> getMessageList(){
		return messageList;
	}
}
