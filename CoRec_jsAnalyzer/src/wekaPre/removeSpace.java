package wekaPre;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import vt.edu.sql.SqliteManager;

public class removeSpace {

	public static void main(String[] args) throws SQLException {
		String[] projects = new String[]{"pdf","meteor","habitica","node","electron","serverless","webpack1","react","Ghost","storybook"};
		String[] patterns = new String[]{"afcf", "avcf", "cfcf"};
		for (String project : projects) {
			for (String pattern : patterns) {
				String dataTable = pattern + "Data3_" + project;
				if (!pattern.equals("avcf")) continue;
				remove(dataTable);
			}
		}
	}
	
	public static void remove(String dataTable) throws SQLException {
		Connection conn = SqliteManager.getConnection();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT count(*) FROM " + dataTable);
		rs.next();
		int totalNum = rs.getInt(1);
		System.out.println(totalNum);
		
		stmt.close();
		conn.close();
		for (int offset = 0; offset < totalNum; offset++) {
			conn = SqliteManager.getConnection();
			stmt = conn.createStatement();
			
			rs = stmt.executeQuery("SELECT avSig FROM " + dataTable + " LIMIT 1 OFFSET " + offset);
			String bugA = rs.getString(1);
			if (!bugA.contains(" ")) {
				stmt.close();
				conn.close();
				continue;
			}
			String bugB = bugA.replace(" ", "");
			System.out.println(bugB);
			rs.close();
			PreparedStatement ps = conn.prepareStatement("UPDATE " + dataTable + " SET avSig = '" + bugB + "' WHERE avSig = '" + bugA + "'");
			ps.executeUpdate();
			ps.close();
			stmt.close();
			conn.close();
	    }
	}
		
		

}
