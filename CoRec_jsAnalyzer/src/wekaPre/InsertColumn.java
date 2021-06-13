package wekaPre;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import vt.edu.sql.SqliteManager;

public class InsertColumn {
	private static String avData;
	private static String avData4;
	private static boolean executeFromFirstBug = true;

	public static void main(String[] args) throws SQLException {
		// TODO Auto-generated method stub
		String[] projects = new String[]{"meteor", "Ghost", "pdf", "habitica", "react", "serverless", "webpack1"}; 
		for (String project : projects) {
			insertColumn (project);
		}
	}
	
	public static void insertColumn (String project) throws SQLException {
		avData = "avcfData3_" + project;
		avData4 = "avcfData4_" + project;
		Connection conn = SqliteManager.getConnection();
		Statement stmt = conn.createStatement();
		if (executeFromFirstBug){
			stmt.executeUpdate("DROP TABLE IF EXISTS " + avData4);
		}
		stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + avData4 + "(cfSig TEXT, cf1Sig TEXT, candiSig TEXT, changed TEXT, f1 INTEGER,f2 INTEGER,f3 TEXT,f4 INTEGER,f5 TEXT,f6 INTEGER,f7 TEXT,f8 INTEGER,f9 INTEGER,f10 INTEGER)");
		ResultSet rs = stmt.executeQuery("SELECT count(*) FROM " + avData);
		rs.next();
		int totalNum = rs.getInt(1);
		System.out.println(totalNum);
		rs.close();
		stmt.close();
		conn.close();
		for (int offset = 0; offset < totalNum; offset++) {
			conn = SqliteManager.getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT * FROM " + avData + " LIMIT 1 OFFSET " + offset);
			int f1 = rs.getInt(5), f2 = rs.getInt(6), f4 = rs.getInt(8),
					 f7 = rs.getInt(1), f8 = rs.getInt(12), f9 = rs.getInt(13);
			String f3 = rs.getString(7), classify = rs.getString(4), f5 = rs.getString(9), f6 = rs.getString(10);
			String afSig = rs.getString(1), cf1Sig = rs.getString(2), candiSig = rs.getString(3);
			rs.close();
			stmt.close();
			conn.close();
			
			System.out.println(offset + 1 + "/" + totalNum);
			
			conn = SqliteManager.getConnection();
			try {
				PreparedStatement ps = conn.prepareStatement("INSERT INTO " + avData4 + " (cfSig,cf1Sig,candiSig,changed,f1,f2,f3,f4,f5,f6,f7,f8,f9,f10) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
				ps.setString(1, afSig);
				ps.setString(2, cf1Sig);
				ps.setString(3, candiSig);
				ps.setString(4, classify);
				ps.setInt(5, f1);
				ps.setInt(6, f2);
				ps.setString(7, f3);
				ps.setInt(8, f4);
				ps.setString(9, f5);
				ps.setInt(10, 0);
				ps.setString(11, f6);
				ps.setInt(12, f7);
				ps.setInt(13, f8);
				ps.setInt(14, f9);
				ps.executeUpdate();
				ps.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}

}
