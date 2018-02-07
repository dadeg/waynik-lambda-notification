package com.amazonaws.lambda.waynik.notifications.send;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import com.amazonaws.services.lambda.runtime.events.SNSEvent;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class LambdaFunctionHandler implements RequestHandler<SNSEvent, String> {
	private String SECRET_KEY_TO_STOP_STRANGERS = "secret";
	private String FCM_AUTH_KEY = "AA...WH";
	private String FCM_URL = "https://fcm.googleapis.com/fcm/send";
	
	private LambdaLogger logger = null;
	
    @Override
    public String handleRequest(SNSEvent event, Context context) {
        try {
        		logger = context.getLogger();
	    		context.getLogger().log("Received event: " + event);
	        
	        String snsPayload = event.getRecords().get(0).getSNS().getMessage();
	        context.getLogger().log("From SNS: " + snsPayload);
	        
	        JSONObject payload = (JSONObject) new JSONParser().parse(snsPayload);
	        context.getLogger().log(payload.toString());
	        
	        if (!payload.containsKey("apiKey") || !payload.get("apiKey").equals(SECRET_KEY_TO_STOP_STRANGERS)) {
	        		throw new Exception("please provide an access key.");
	        }
	        
	        if (!payload.containsKey("userId")) {
	        		context.getLogger().log("no userid");
	        		throw new Exception("userId is a required parameter.");
	        }
	        if (!payload.containsKey("message") && !payload.containsKey("data")) {
	        		context.getLogger().log("no message or data");
	        		throw new Exception("message or data are required parameters.");
	        }
	        
	        int userId = Integer.parseUnsignedInt(payload.get("userId").toString());
	        
	        Object messageObject = payload.get("message");
	        String message = null;
	        if (messageObject != null) {
	        		message = messageObject.toString();
	        }
	        
	        JSONObject data = (JSONObject) payload.get("data");
	        context.getLogger().log("UserID: " + userId);
	        context.getLogger().log("message: " + message);
	        context.getLogger().log("data: " + data);
	        
	        String token = new FirebaseTokenModel().getRegistrationToken(userId);

	        context.getLogger().log("token: " + token);
	        
	        if (message != null) {
	        		return sendNormalPushNotification(token, message, data);
	        } else {
		        	// since message or data is required, this should never fail.
		        return sendSilentPushNotification(token, data);
	        } 
        } catch (Exception e) {
        		context.getLogger().log("Exception: " + e.toString());
        		return "Exception: " + e.toString();
        }
    }
    
    private String sendNormalPushNotification(String token, String message, JSONObject data) throws Exception
    {
    		JSONObject payload = new JSONObject();
    		payload.put("to", token);
    		payload.put("priority", "high");
    		
    		JSONObject messageBody = new JSONObject();
    		messageBody.put("body", message);
    		payload.put("notification", messageBody);
    		
    		if (data != null) {
    			payload.put("data", data);
    		}
   
    		return sendNotification(payload);
    }
    
    private String sendSilentPushNotification(String token, JSONObject data) throws Exception
    {
		JSONObject payload = new JSONObject();
		payload.put("to", token);
		payload.put("priority", "high");
		payload.put("data", data);

		// content_available is for iOs and it makes the app wake up.
		payload.put("content_available", true);
		
		return sendNotification(payload);
    }
    
    /**
	 * From Google's website:
     * HTTP POST request

		https://fcm.googleapis.com/fcm/send
		Content-Type:application/json
		Authorization:key=AIzaSyZ-1u...0GBYzPu7Udno5aA
		
		{ "data": {
		    "score": "5x1",
		    "time": "15:10"
		  },
		  "to" : "bk3RNwTe3H0:CI2k_HHwgIpoDKCIZvvDMExUdFQ3P1..."
		}
	 */
	private String sendNotification(JSONObject payload) throws Exception
	{
				
		   URL url = new URL(FCM_URL);
		   HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		   conn.setUseCaches(false);
		   conn.setDoInput(true);
		   conn.setDoOutput(true);

		   conn.setRequestMethod("POST");
		   conn.setRequestProperty("Authorization","key=" + FCM_AUTH_KEY);
		   conn.setRequestProperty("Content-Type","application/json");


		   OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		   wr.write(payload.toString());
		   wr.flush();
		   
		   java.util.Scanner s = new java.util.Scanner(conn.getInputStream()).useDelimiter("\\A");
		   return s.hasNext() ? s.next() : "";
	}
}
