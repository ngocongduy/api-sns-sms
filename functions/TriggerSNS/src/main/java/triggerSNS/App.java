package TriggerSNS.src.main.java.triggerSNS;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import TriggerSNS.src.main.java.triggerSNS.AmazonSNSSMS;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;

/**
 * Handler for requests to Lambda function.
 */
public class App implements RequestHandler<Map<String, String>, Map<String, String>> {
	@Override
	public Map<String, String> handleRequest(final Map<String, String> input, final Context context) {

		System.out.println("Input - object to string:...");
		System.out.println(input);
		LambdaLogger logger = context.getLogger();

		try {
			logger.log(input.toString());
			String defaultContent = "get message failed";
			String content = input.getOrDefault("message", defaultContent);
			String defaultUrl = "https://www.google.com.vn/";
			
			String url = input.getOrDefault("url", defaultUrl);
			String message = String.format("Message: %s , url: %s ", content, url);
			logger.log(message);
			boolean isSuccess = ! (content.compareTo(defaultContent) == 0);
			if ( isSuccess ) {
				// Do trigger sns
				AmazonSNSClient snsClient = (AmazonSNSClient) AmazonSNSSMS.createSNS();
				String topicName = "MySMSTopic";
//				String phoneNumber = "+84937450882";
//				boolean isSubcribed = AmazonSNSSMS.subcribeTopicWithSMSAndPhone(snsClient, topicName, phoneNumber);
//				if(isSubcribed) {
//					AmazonSNSSMS.publishAndSendWithSubcription(snsClient, topicName, message, isSubcribed);
//				}
				
				
				String email = "duy.ngo.3@fecredit.com.vn";
				String topicArn = AmazonSNSSMS.createTopicAndGetSNSTopicArn(snsClient, topicName);
				AmazonSNSSMS.subscribeToTopicProtocalAndEndpoint(snsClient, topicArn, "email", email);
				PublishResult result = snsClient.publish(
						new PublishRequest().withTopicArn(topicArn).withMessage(message));
				System.out.println(result);
			}
			
			Map<String, String> output = new HashMap<>();
			output.put("success", Boolean.toString(isSuccess));
			return output;

		} catch (Exception e) {
			logger.log(e.getMessage());
		}
		System.out.println("Lambda called but failed!...");
		System.out.println(input);
		return null;

	}
	public Map<String, String> handleRequestDelete(Map<String, String> input, final Context context) {
		System.out.println("Input - object to string:...");
		System.out.println(input);
		LambdaLogger logger = context.getLogger();
		
		String defaultTopicName = "abcxyz";
		String topicName = input.getOrDefault("topicName", defaultTopicName);
		
		boolean isSuccess = ! (topicName.compareTo(defaultTopicName) == 0);
		if(isSuccess) {
			AmazonSNSClient snsClient = (AmazonSNSClient) AmazonSNSSMS.createSNS();
			String topicArn = AmazonSNSSMS.createTopicAndGetSNSTopicArn(snsClient, topicName);
			AmazonSNSSMS.deleteTopicAndGetSNSTopicArn(snsClient, topicArn);
		}
		
		Map<String, String> output = new HashMap<>();
		String info = String.format("Deleted, topic name: %s, Execute: %s ",topicName, Boolean.toString(isSuccess));
		output.put("success", "Deleted");
		return output;

	}

}
