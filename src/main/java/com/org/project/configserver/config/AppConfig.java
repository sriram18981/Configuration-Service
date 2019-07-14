package com.org.project.configserver.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.org.project.configserver.config.message.impl.MessageSubscriberImpl;

@Configuration
@Import(Slf4jMDCFilterConfiguration.class)
public class AppConfig {

	@Value("${spring.redis.host}")
	private String redisServer;

	@Value("${spring.redis.port}")
	private int redisPort;

	@Bean
	public LettuceConnectionFactory redisConnectionFactory() {
		return new LettuceConnectionFactory(new RedisStandaloneConfiguration(redisServer, redisPort));
	}

	@Bean
	public RedisTemplate<String, Object> redisTemplate() {

		RedisTemplate<String, Object> template = new RedisTemplate<>();

		template.setConnectionFactory(redisConnectionFactory());
		template.setKeySerializer(new StringRedisSerializer());
		template.setHashValueSerializer(new GenericToStringSerializer<Object>(Object.class));
		template.setValueSerializer(new GenericToStringSerializer<Object>(Object.class));
		template.setDefaultSerializer(new StringRedisSerializer());

		return template;

	}

	@Bean
	public MessageListenerAdapter messageListener() {
		return new MessageListenerAdapter(messageSubscriberImpl());
	}

	@Bean
	public MessageSubscriberImpl messageSubscriberImpl() {
		return new MessageSubscriberImpl();
	}

	@Bean
	public RedisMessageListenerContainer redisContainer() {
		final RedisMessageListenerContainer container = new RedisMessageListenerContainer();

		container.setConnectionFactory(redisConnectionFactory());
		container.addMessageListener(messageListener(), new ChannelTopic("config"));
		container.addMessageListener(messageListener(), new ChannelTopic("client"));

		return container;
	}

	@Bean
	public RestTemplate restTemplate() {
		RestTemplate rt = new RestTemplate();
		rt.getMessageConverters().add(new StringHttpMessageConverter());
		rt.getMessageConverters().add(new GsonHttpMessageConverter());

		return rt;
	}

}
