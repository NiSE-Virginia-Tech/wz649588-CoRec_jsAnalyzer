package vt.edu.graph;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import vt.edu.sql.SqliteManager;

public class PatternGroupGenerator {
	
	public static void main(String[] args) throws SQLException {
		String[] projects = {"meteor", "react","habitica","pdf","serverless","electron"
				,"webpack1","storybook","Ghost","node"};
		for (String project : projects) {
			if(!project.equals("node")){
				continue;
			}
			Set<String> set = new HashSet<>();
			
			String pattern_id_table = "em_largest_match_with_pattern_final_revision_" + project;
			Connection conn = SqliteManager.getConnection();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT count(*) FROM " + pattern_id_table);
			rs.next();
			int totalNum = rs.getInt(1);
			stmt.close();
			conn.close();
			for (int offset = 0; offset < totalNum; offset++) {
				conn = SqliteManager.getConnection();
				stmt = conn.createStatement();
				
				rs = stmt.executeQuery("SELECT * FROM " + pattern_id_table + " LIMIT 1 OFFSET " + offset);
				String bugname = rs.getString(2);
				set.add(bugname);
				rs.close();
				
				stmt.close();
				conn.close();
			}
			System.out.println(project + " " + set.size());
		}
		
	}
}
