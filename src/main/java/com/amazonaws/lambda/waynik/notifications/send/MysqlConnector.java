package com.amazonaws.lambda.waynik.notifications.send;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MysqlConnector {
	private static final String url = "jdbc:mysql://mysql.com";
	private static final String userName = "web";
	private static final String password = "pass";
	
	public static Connection getConnection() throws SQLException
	{
		return DriverManager.getConnection(url, userName, password);
	}
}
