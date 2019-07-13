package com.org.project.configserver.config.controller;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonObject;
import com.org.project.configserver.config.manager.RedisConfigManager;

/**
 * Controller for the configuration service
 * @author Sriram Tanikella <sriram18981@gmail.com>
 *
 */

@RestController
@RequestMapping("config/api/v1")
public class ConfigController {
	
	private static final Logger logger = LoggerFactory.getLogger(ConfigController.class);
	
	
	@Autowired
	RedisConfigManager redisManager;
	
	/**
	 * Ping URL to see if the service is up and running
	 * @return
	 */
	@GetMapping(path="about")
	public String about() {
		return "Config service is up and running";
	}
	
	
	/**
	 * Fetch the Configuration from the store
	 * @return
	 */
	@GetMapping(path="config")
	public JsonObject getConfig() {
		logger.debug("got a reqeust for the global configuration");
		
		JsonObject config = redisManager.getConfig();
		logger.info("got the configuration {}", config);
		
		return config;
	}
	
	
	/**
	 * Register a client as a subscriber for the config push
	 * @return
	 * @throws IOException 
	 */
	@PutMapping(path="subscribe")
	public JsonObject subscribe(@RequestBody Map<String, String> params, HttpServletResponse response) throws IOException {
		logger.info("got a request for subscription {}", params);
		JsonObject responseBody = new JsonObject();
		
		
		if(params.containsKey("endPoint")) {
			String message = redisManager.addSubscription(params.get("endPoint"));
			responseBody.addProperty("status", message);
		} else {
			response.sendError(HttpStatus.BAD_REQUEST.value(), "\"endPoint\" is a required member in the request body");
		}

		return responseBody;
	}

	
	/**
	 * utility method to test the config push to subscribers
	 * @param config
	 */
	@PostMapping(path="config")
	public void testSubscriptions(@RequestBody JsonObject config) {
		logger.info("configuration recieved thru subscription {}", config);
	}

	

}
