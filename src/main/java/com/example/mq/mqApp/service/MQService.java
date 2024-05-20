package com.example.mq.mqApp.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.Hashtable;
//import java.util.concurrent.ExecutorService;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.security.cert.CertificateException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import com.google.common.base.Charsets;
import com.ibm.mq.*;
import com.ibm.mq.constants.CMQC;
import com.ibm.mq.constants.MQConstants;

@Service
public class MQService {
	@Autowired
	private ResourceLoader resourceLoader;

//	@Autowired
//	@Qualifier("executors")
//	private ExecutorService executors;

	private int id = 1;

	private MQQueueManager queueManager;
	private static final Logger LOGGER = LoggerFactory.getLogger(MQService.class);
	private static final String HOSTNAME = "localhost";
	private static final int PORT = 1414;
	private static final String CHANNEL = "DEV.APP.SVRCONN";
	private static final String QUEUE_MANAGER = "QM1";
	private static final String HOST = "localhost"; // Host name or IP address
	private static final String APP_USER = "mqApp"; // User name that application uses to connect to MQ
	private static final String APP_PASSWORD = "4321"; // Password that the application uses to connect to MQ
	private static final String QUEUE_NAME = "DEV.QUEUE.1";// Queue that the application uses to put and get messages to and from
//	private static final String cipherSuite = "TLS_RSA_WITH_AES_256_CBC_SHA256";

	public MQQueueManager connectToQueueManager(String queueMgrName, Hashtable<String, Object> properties) {
		try {
			queueManager = new MQQueueManager(queueMgrName, properties);
			LOGGER.info("Connected to queue manager: {}, {}, {}", queueMgrName, queueManager.isConnected(),
					queueManager.isOpen());
			sendMessage(queueManager,QUEUE_NAME);
			//executors.execute(() -> 
			receiveMessage(queueManager, QUEUE_NAME);
			receiveMessage(queueManager, "DEV.QUEUE.2");
//			executors.execute(() -> receiveMessage(queueManager, "RGSH.REPLY.QUEUE.0001"));

		} catch (Exception e) {
			LOGGER.error("Error {}", e);
		}
		return queueManager;
	}

	private void receiveMessage(MQQueueManager queueManager, String queuename) {

		LOGGER.info("Stsrt receive message from {} queue", queuename);
		int openOptions = MQConstants.MQOO_INPUT_AS_Q_DEF | MQConstants.MQOO_OUTPUT;

		MQQueue inboundMessageQueue = null;
		try {
			inboundMessageQueue = queueManager.accessQueue(queuename, openOptions);
		} catch (MQException e) {
			LOGGER.error("Error ..{}", e);
		}

		// read message from input queue
		boolean getMore = true;
		int count = 0;
		while (getMore) {

			count += 1;
			try {
				MQMessage getMessage = new MQMessage();
				getMessage.characterSet = 1208; // UTF-8
				getMessage.format = CMQC.MQFMT_STRING;
				inboundMessageQueue.get(getMessage);

				int messageLength = getMessage.getMessageLength();

				String messageData = getMessage.readStringOfCharLength(messageLength);
				

				LOGGER.info("Recieved Reqeust - Message ID: {},{}", new String(getMessage.messageId, Charsets.UTF_8),
						queuename);
				LOGGER.info("Correlation ID: {},{}", new String(getMessage.correlationId, Charsets.UTF_8), queuename);
				LOGGER.info("Data: {},{}", messageData, queuename);
				if(getMessage.replyToQueueManagerName!=null) {
					sendMessage(queueManager, getMessage.replyToQueueName);
					LOGGER.info("getMessage.replyToQueueName): {}",  getMessage.replyToQueueName);
				}
			} catch (MQException e) {

				if ((e.completionCode == CMQC.MQCC_FAILED) && (e.reasonCode == CMQC.MQRC_NO_MSG_AVAILABLE)) {
					LOGGER.info("No message");

				} else {
					LOGGER.info("MQException: " + e.getLocalizedMessage());
					LOGGER.info("CC=" + e.completionCode + " : RC=" + e.reasonCode);
					LOGGER.error("Error ..{}", e);
					getMore = false;
				}

			} catch (IOException e) {

				LOGGER.error("Error ..{}", e);

			}
			getMore=false;
		}
	}

	public void sendMessage(MQQueueManager queueManager,String queueName) {
		// TODO Auto-generated method stub
		int openOptions = MQConstants.MQOO_INPUT_AS_Q_DEF | MQConstants.MQOO_OUTPUT;
		try {
			LOGGER.info("Start sendMessage");
			MQQueue queue = queueManager.accessQueue(queueName, openOptions);
			MQMessage putMessage = new MQMessage();
			String messageId = "E2ECLR" + (id + 1);
			id = id + 1;
			String correlationId = "E2ECLR";
			String payload;// = "<message>Testing of message sending to LQ.RESOURCE.MANAGER.DATA queue
							// </message>";
			payload = "<?xml version=\"1.0\"?><CLR_REQUEST><CIRCUIT_ID>W2B60748</CIRCUIT_ID><VIEW>P</VIEW><SHORT_LONG>S</SHORT_LONG><EQUIPMENT>Y</EQUIPMENT><SO_NOTES>Y</SO_NOTES><CI_NOTES>Y</CI_NOTES><CPE_NOTES>Y</CPE_NOTES><USER_ID>10.118.226.70</USER_ID><APPROVAL_LEVEL>A168</APPROVAL_LEVEL></CLR_REQUEST>";
			putMessage.messageId = messageId.getBytes();
			putMessage.correlationId = correlationId.getBytes();
//			putMessage.characterSet = 1208; // UTF-8
//			putMessage.format = CMQC.MQFMT_STRING;
			byte [] xmlData = payload.getBytes();
			putMessage.persistence = CMQC.MQPER_PERSISTENCE_AS_Q_DEF; // durable message
			putMessage.expiry = 28800000; // in 1/10th of a second. 48 hours.

			putMessage.replyToQueueName = "DEV.QUEUE.2";
			putMessage.replyToQueueManagerName = "QM1";
//			putMessage.writeString(payload);
			putMessage.write(xmlData);

			queue.put(putMessage);
			LOGGER.info("Successfully sent msg");
		} catch (MQException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error ..{}", e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error ..{}", e);
		}

	}

	public void receiveMessage1(MQQueueManager queueManager) {
		try {
			LOGGER.info("Start receiveMessage... with {},{}", queueManager.getName(), queueManager.isOpen());
//			MQQueue queue = queueManager.accessQueue("RGSH.REPLY.QUEUE.0001", MQC.MQOO_OUTPUT);
//			MQQueue queue2 = queueManager.accessQueue("LQ.RESOURCE.MANAGER.DATA", MQC.MQOO_OUTPUT);
			int opts = CMQC.MQOO_FAIL_IF_QUIESCING + CMQC.MQOO_INPUT_AS_Q_DEF + CMQC.MQOO_INQUIRE;
			int opts2 = CMQC.MQOO_FAIL_IF_QUIESCING + CMQC.MQOO_OUTPUT + CMQC.MQOO_INQUIRE;

			MQQueue queue = queueManager.accessQueue("RGSH.REPLY.QUEUE.0001", opts);

			MQQueue sendQueue = queueManager.accessQueue("LQ.RESOURCE.MANAGER.DATA", opts2);

			MQMessage sentmessage = new MQMessage();

			sentmessage.writeString("First message sent to LQ.RESOURCE.MANAGER.DATA queue ");
			sendQueue.put(sentmessage);
			LOGGER.info("Message sent successfully  {}", sendQueue.getCurrentDepth());
			sendQueue.close();

			MQGetMessageOptions options = new MQGetMessageOptions();

			options.options = CMQC.MQOO_INPUT_AS_Q_DEF | CMQC.MQOO_OUTPUT | CMQC.MQOO_INQUIRE;
			int a = CMQC.MQRC_NO_MSG_AVAILABLE;
			options.waitInterval = 25000;
			boolean getMore = true;
			int noMsg = queue.getCurrentDepth();
//			int noMsg =0;
			LOGGER.info("No of messages in queue {} is {}", queue.getName(), noMsg);

			while (getMore) {
				MQMessage message = new MQMessage();
				try {
					queue.get(message, options);
					LOGGER.info("REceived messsage 1 from RGSH.REPLY.QUEUE.0001 queue is {}", message.readLine());
					LOGGER.info("REceived messsage 1 from RGSH.REPLY.QUEUE.0001 queue is {}",
							message.readString(CMQC.MQOO_INPUT_AS_Q_DEF));
				} catch (MQException e) {
					if ((e.completionCode == CMQC.MQCC_FAILED) && (e.reasonCode == CMQC.MQRC_NO_MSG_AVAILABLE)) {
						LOGGER.info("No message");
						break;
					} else {
						LOGGER.info("MQException: " + e.getLocalizedMessage());
						LOGGER.info("CC=" + e.completionCode + " : RC=" + e.reasonCode);
						LOGGER.error("Error ..{}", e);
						getMore = false;
					}
				}
			}

			getMore = true;
			MQQueue queue2 = queueManager.accessQueue("LQ.RESOURCE.MANAGER.DATA", opts);
			noMsg = queue2.getCurrentDepth();
//			noMsg=1;
			LOGGER.info("No of messages in queue {} is {}", queue2.getName(), noMsg);

			while (getMore) {
				MQMessage message2 = new MQMessage();
				try {
					queue2.get(message2, options);
					LOGGER.info("REceived messsage 1 from LQ.RESOURCE.MANAGER.DATA queue is {}", message2.readLine());

					LOGGER.info("REceived messsage 1 from LQ.RESOURCE.MANAGER.DATA queue is {}",
							message2.readString(CMQC.MQOO_INPUT_AS_Q_DEF));
				} catch (MQException e) {
					if ((e.completionCode == CMQC.MQCC_FAILED) && (e.reasonCode == CMQC.MQRC_NO_MSG_AVAILABLE)) {
						LOGGER.info("No message");
						break;
					} else {
						LOGGER.info("MQException: " + "{}", e.getLocalizedMessage());
						LOGGER.info("CC=  {}, : RC= {}", e.completionCode, e.reasonCode);
						getMore = false;
						LOGGER.error("Error ..{}", e);
					}
				}
			}

			queue.close();
			queue2.close();

		} catch (Exception e) {
			LOGGER.error("Error in receiveMessage {}", e);

		}
	}

	public MQQueueManager connectToQueue() {
		Hashtable<String, Object> properties = new Hashtable<>();

		properties.put(CMQC.HOST_NAME_PROPERTY, HOSTNAME);
		properties.put(CMQC.PORT_PROPERTY, PORT);
		properties.put(CMQC.CHANNEL_PROPERTY, CHANNEL);
		properties.put(CMQC.USER_ID_PROPERTY, APP_USER);
		properties.put(CMQC.PASSWORD_PROPERTY, APP_PASSWORD);
//		properties.put(CMQC.SSL_CIPHER_SUITE_PROPERTY, cipherSuite);
		properties.put(CMQC.TRANSPORT_PROPERTY, CMQC.TRANSPORT_MQSERIES_CLIENT);
//		SSLSocketFactory sslSocketFactory = getSocketFactory();
//		if (sslSocketFactory != null) {
//			properties.put(CMQC.SSL_SOCKET_FACTORY_PROPERTY, sslSocketFactory);
//		}
//		System.setProperty("com.ibm.mq.cfg.useIBMCipherMappings", "false");
		return connectToQueueManager(QUEUE_MANAGER, properties);
	}

	public SSLSocketFactory getSocketFactory() {
		KeyStore ks;
		SSLSocketFactory sslSocketFactory = null;
		String keyp = "bgwmqm";
//		String keyp = "Its8tmsK@fka";
		char[] PWD = keyp.toCharArray();
		try {
			org.springframework.core.io.Resource jmsks = resourceLoader.getResource("classpath:UAT3key.jks");
			ks = KeyStore.getInstance("JKS");
//		ks = KeyStore.getInstance("PKCS12");
			ks.load(new FileInputStream(jmsks.getFile().getAbsolutePath()), PWD);
			KeyStore trustStore = KeyStore.getInstance("JKS");
//		KeyStore trustStore = KeyStore.getInstance("PKCS12");
			trustStore.load(new FileInputStream(jmsks.getFile().getAbsolutePath()), null);
			TrustManagerFactory trustManagerFactory = TrustManagerFactory
					.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			KeyManagerFactory keyManagerFactory = KeyManagerFactory
					.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			trustManagerFactory.init(trustStore);
			keyManagerFactory.init(ks, PWD);
			SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
			sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
			sslSocketFactory = sslContext.getSocketFactory();
			LOGGER.info("Intialized ssl socket factory");
		} catch (KeyStoreException e) {
			LOGGER.error("KeyStoreException on getSocketFactory due to {}", e);
		} catch (NoSuchAlgorithmException e) {
			LOGGER.error("NoSuchAlgorithmException on getSocketFactory due to {}", e);
		} catch (java.security.cert.CertificateException e) {
			LOGGER.error("CertificateException on getSocketFactory due to {}", e);
		} catch (FileNotFoundException e) {
			LOGGER.error("FileNotFoundException on getSocketFactory due to {}", e);
		} catch (IOException e) {
			LOGGER.error("IOException on getSocketFactory due to {}", e);
		} catch (UnrecoverableKeyException e) {
			LOGGER.error("UnrecoverableKeyException on getSocketFactory due to {}", e);
		} catch (KeyManagementException e) {
			LOGGER.error("KeyManagementException on getSocketFactory due to {}", e);
		}
		LOGGER.info("sslContextFactory " + sslSocketFactory);
		return sslSocketFactory;
	}

}


