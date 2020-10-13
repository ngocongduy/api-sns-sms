package TriggerSNS.src.main.java.triggerSNS;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.DeleteTopicRequest;
import com.amazonaws.services.sns.model.DeleteTopicResult;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.SubscribeResult;

public class AmazonSNSSMS {

	private static ProfileCredentialsProvider credentialsProvider;
	{
		credentialsProvider = new ProfileCredentialsProvider();
		try {
			credentialsProvider.getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException("Cannot load the credentials from the credential profiles file. ", e);
		}
	}

	public static AmazonSNS createSNS() {
		AmazonSNS sns = AmazonSNSClientBuilder.standard()
				// .withCredentials(credentialsProvider)
				.withRegion(Regions.AP_SOUTHEAST_1).build();
		return sns;
	}

	public static String createTopicAndGetSNSTopicArn(AmazonSNSClient snsClient, String topicName) {
		CreateTopicRequest tr = new CreateTopicRequest(topicName);
		CreateTopicResult ts = snsClient.createTopic(tr);
		System.out.println(ts);
		System.out.println("Create topic request " + snsClient.getCachedResponseMetadata(tr));
		String topicArn = ts.getTopicArn();
		return topicArn;
	}
	
	public static void deleteTopicAndGetSNSTopicArn(AmazonSNSClient snsClient, String topicArn) {
		DeleteTopicRequest dr = new DeleteTopicRequest(topicArn);
		DeleteTopicResult ds = snsClient.deleteTopic(dr);
		System.out.println(ds);
		System.out.println("Delete topic request " + snsClient.getCachedResponseMetadata(dr));
	}

	public static void subscribeToTopicProtocalAndEndpoint(AmazonSNSClient snsClient, String topicArn, String protocol,
			String endpoint) {
		SubscribeRequest subscribe = new SubscribeRequest(topicArn, protocol, endpoint);
		SubscribeResult subscribeResult = snsClient.subscribe(subscribe);
		System.out.println("Subscribe request: " + snsClient.getCachedResponseMetadata(subscribe));
		System.out.println("Subscribe result: " + subscribeResult);
	}

	public static boolean subcribeTopicWithSMSAndPhone(AmazonSNSClient sns, String topicName, String phoneNumber) {
		try {
			String topicArn = createTopicAndGetSNSTopicArn(sns, topicName);
			subscribeToTopicProtocalAndEndpoint(sns, topicArn, "sms", phoneNumber);
			System.out.println("Subcribe successully.");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private static Map<String, MessageAttributeValue> setSMSAttributes() {
		Map<String, MessageAttributeValue> smsAttributes = new HashMap<String, MessageAttributeValue>();
		// Promotional is cost effective, transactional is time prefered
		smsAttributes.put("AWS.SNS.SMS.SenderID", new MessageAttributeValue().withStringValue("mySenderID")
				.withDataType("String"));
		smsAttributes.put("AWS.SNS.SMS.MaxPrice", new MessageAttributeValue().withStringValue("0.01")
				.withDataType("Number"));
		smsAttributes.put("AWS.SNS.SMS.SMSType", new MessageAttributeValue().withStringValue("Promotional")
				.withDataType("String"));
		return smsAttributes;
	}

	public static void publishAndSendNoTopicSubcription(AmazonSNSClient snsClient) {

		Map<String, MessageAttributeValue> smsAttributes = setSMSAttributes();

		String message = "My SMS message";
		String phoneNumber = "+";
		PublishResult result = snsClient.publish(new PublishRequest().withMessage(message).withPhoneNumber(phoneNumber)
				.withMessageAttributes(smsAttributes));
		System.out.println(result); // Prints the message ID.

	}

	public static boolean publishAndSendWithSubcription(AmazonSNSClient snsClient, String topicName, String message,
			boolean isSubcribed) {
		if (isSubcribed) {
			try {
				Map<String, MessageAttributeValue> smsAttributes = setSMSAttributes();
				String topicArn = createTopicAndGetSNSTopicArn(snsClient, topicName);
				sendSMSMessageToTopic(snsClient, topicArn, message, smsAttributes);
				System.out.println("Publish successfully.");
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	private static void sendSMSMessageToTopic(AmazonSNSClient snsClient, String topicArn, String message,
			Map<String, MessageAttributeValue> smsAttributes) {
		PublishResult result = snsClient.publish(
				new PublishRequest().withTopicArn(topicArn).withMessage(message).withMessageAttributes(smsAttributes));
		System.out.println(result);
	}

}
