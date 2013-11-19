package org.hive2hive.core.process.common.put;

import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.sql.Timestamp;
import java.util.Date;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.data.NetworkContentWrapper;
import org.hive2hive.core.network.messages.BaseMessage;
import org.hive2hive.core.network.messages.usermessages.UserMessageContainer;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.security.EncryptionUtil;

public class PutUserMessageStep extends PutProcessStep {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(PutUserMessageStep.class);
	
	// TODO add domain key to putted user message
	// TODO refactor the whole PutProcessStep
	
	private final PrivateKey privateKey;
	
	public PutUserMessageStep(String userId, BaseMessage userMessage, ProcessStep nextStep, PrivateKey privateKey) {
		super(userId, createContentKey(), new NetworkContentWrapper<BaseMessage>(userMessage), nextStep);
		
		this.privateKey = privateKey;
	}
	
	@Override
	public void start() {

		// sign the user message
		byte[] userMessageBytes = EncryptionUtil.serializeObject(content);
		try {
			byte[] signature = EncryptionUtil.sign(userMessageBytes, privateKey);
			UserMessageContainer container = new UserMessageContainer(userMessageBytes, signature);
			
			put(locationKey, contentKey, container);
			
		} catch (InvalidKeyException | SignatureException e) {
			logger.error("Exception while signing user message: ", e);
		}
	}
	
	private static String createContentKey() {
		return String.format("%s-%s", H2HConstants.UM_CONTENT_KEY_PREFIX, new Timestamp(new Date().getTime()).toString());
	}
}