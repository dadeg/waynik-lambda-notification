package com.amazonaws.lambda.waynik.notifications.send;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.commons.dbutils.DbUtils;
import org.json.simple.JSONObject;


public class FirebaseTokenModel {
	
	private String table = "firebase_tokens";
	
	private String ID = "id";
	private String USER_ID = "user_id";
	private String TOKEN = "device_registration_token";
	private String CREATED_AT = "created_at";
    
	private String[] fields = {
		ID,
        USER_ID,
        TOKEN,
        CREATED_AT
    };
	
	public String getRegistrationToken(int userId) throws Exception
	{
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			
			connection = MysqlConnector.getConnection();
			
			statement = connection.prepareStatement("select " + TOKEN + " from " + table + " WHERE " + USER_ID + " = ? ORDER BY " + CREATED_AT + " desc LIMIT 1;");
			statement.setInt(1, userId);
			resultSet = statement.executeQuery();
			
	        while (resultSet.next()) {
				return resultSet.getString(TOKEN);
	        }
	        
	        throw new Exception("no token found.");
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(statement);
			DbUtils.closeQuietly(connection);
		}
	}
}
