package com.example.mq.mqApp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.mq.mqApp.service.MQService;
import com.ibm.mq.MQQueueManager;


@RestController
@RequestMapping("/MQApp")
public class MQController {

	private static final Logger LOGGER = LoggerFactory.getLogger(MQController.class);

	@Autowired
	private MQService mqService;
	
	private static MQQueueManager queueMgr;

	@GetMapping("/healthCheck")
	public String healthCheck() {
		LOGGER.info("MQService up and running");
		return "MQService up and running";
	}
	
	
	@GetMapping("/mqCheck")
	public String mqCheck() {
		LOGGER.info("MQService up and running");
		queueMgr = mqService.connectToQueue();
		return "MQService up and running";
	}
	
	@GetMapping("/mqSend")
	public String mqSend() {
		LOGGER.info("MQService up and running");
		mqService.sendMessage(queueMgr,"");
		return "MQService up and running";
	}
}

