package vt.edu.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class SqliteManager {
	public static Connection getConnection() {
		Connection connection = null;
		try {
			connection = DriverManager.getConnection("jdbc:sqlite:/Users/zijianjiang/Documents/esprima/newmanager/entityChangeNew.sqlite");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return connection;
	}
}

