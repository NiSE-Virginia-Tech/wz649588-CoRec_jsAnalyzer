package vt.edu.empirical;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import vt.edu.sql.SqliteManager;


public class SqlQuery {
	public static void main(String[] args) throws SQLException, IOException {
		changeThreshold(5);
//		getRCPNodes();
//		getMostPattern();
	}
	
	public void getMostNodes() throws SQLException, IOException{
		String resultTable = "empirical_graph_stat_revision_node";
		Connection conn = SqliteManager.getConnection();
		Statement stmt = conn.createStatement();
		String subquery = "SELECT bug_name, sum(total_num) AS node_num FROM "
				+ resultTable + " GROUP BY bug_name";
		ResultSet rs = stmt.executeQuery("SELECT bug_name from (" + subquery +") WHERE node_num=1019");
		rs.next();
		String bugname = rs.getString(1);
//		int totalRow = rs.getInt(1);
		System.out.println(bugname);
		rs.close();
		conn.close();
	}
	
	public static void changeThreshold(int threshold) throws SQLException, IOException{
		String[] projects = {"webpack1", "node", "meteor","habitica","react","electron","Ghost","pdf","serverless","storybook"};
		for(String project : projects) {
			Connection conn = SqliteManager.getConnection();
			Statement stmt = conn.createStatement();
			String table = "em_largest_match_with_pattern_idnotest_" + project;
//			if (project.equals("node")) table = "em_largest_match_with_pattern_final_revision_node";
			String subquery = "SELECT DISTINCT pattern_id, bug_name FROM " + table;
			String query = "SELECT pattern_id, COUNT(bug_name) as num_bug FROM(" + subquery + ") GROUP BY pattern_id";
			ResultSet rs = stmt.executeQuery("SELECT pattern_id, COUNT(pattern_id) FROM (" + query + ") WHERE num_bug > " + threshold);
			rs.next();
			int num = rs.getInt(2);
//			int totalRow = rs.getInt(1);
			System.out.println(project + ": " + num);
			rs.close();
			conn.close();
		}
	}
	
	public static void getMostPattern() throws SQLException, IOException{
		String[] projects = {"webpack1", "node", "meteor","habitica","react","electron","Ghost","pdf","serverless","storybook"};
		for(String project : projects) {
			Connection conn = SqliteManager.getConnection();
			Statement stmt = conn.createStatement();
			String table = "em_largest_match_with_pattern_idnotest_" + project;
			if (project.equals("node")) table = "em_largest_match_with_pattern_final_revision_node";
			String subquery = "SELECT DISTINCT pattern_id, bug_name FROM " + table;
			String query = "SELECT pattern_id, COUNT(bug_name) as num_bug FROM(" + subquery + ") GROUP BY pattern_id";
			ResultSet rs = stmt.executeQuery("SELECT pattern_id, MAX(num_bug) FROM (" + query + " )");
			rs.next();
			int num = rs.getInt(2);
//			int totalRow = rs.getInt(1);
			System.out.println(project + ": " + num);
			rs.close();
			conn.close();
		}
	}
	
	public static void getRCPNodes() throws SQLException, IOException{
		String[] projects = {"webpack1", "node", "meteor","habitica","react","electron","Ghost","pdf","serverless","storybook"};
		for(String project : projects) {
			Connection conn = SqliteManager.getConnection();
			Statement stmt = conn.createStatement();
			String table = "em_largest_match_patternnotest_" + project;
//			if (project.equals("node")) table = "em_largest_match_final_revision_node";
			String subquery = "SELECT COUNT(pattern_id) FROM " + table + " WHERE node_num>4";
			ResultSet rs = stmt.executeQuery(subquery);
			rs.next();
			int num = rs.getInt(1);
//			int totalRow = rs.getInt(1);
			System.out.println(project + ": " + num);
			rs.close();
			conn.close();
		}
	}
}
