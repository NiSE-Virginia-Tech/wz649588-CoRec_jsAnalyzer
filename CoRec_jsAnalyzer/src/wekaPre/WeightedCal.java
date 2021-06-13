package wekaPre;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import vt.edu.sql.SqliteManager;

public class WeightedCal {
	private static String table;
	
	public static void main(String[] args) throws SQLException{
		table = "whole_CPRF_abrfnewnew";
		double WC = 0, WP = 0, WR = 0, WF = 0;
		int totalCovered = 0;
		for (int offset = 0; offset < 8; offset++) {
			Connection conn = SqliteManager.getConnection();
			Statement stmt = conn.createStatement();
			
			ResultSet rs = stmt.executeQuery("SELECT * FROM " + table + " LIMIT 1 OFFSET " + offset);
			double C = rs.getDouble(2), P = rs.getDouble(3), R = rs.getDouble(4), F = rs.getDouble(5);
			int covered = rs.getInt(6);
			rs.close();
			
			stmt.close();
			conn.close();
			if (covered != 0){
			WC += C * covered;
			WP += P * covered;
			WR += R * covered;
			WF += F * covered;
			}
			totalCovered += covered;
		}
		System.out.println("WC = " + WC/totalCovered);
		System.out.println("WP = " + WP/totalCovered);
		System.out.println("WR = " + WR/totalCovered);
		System.out.println("WF = " + WF/totalCovered);
		
	}
}
