package wekaPre;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import vt.edu.sql.SqliteManager;

public class MergeData {
	private static final String[] projects = new String[]{"pdf","meteor","habitica","serverless","webpack1","react","Ghost","node","electron","storybook"};
	private static final String[] patterns = new String[]{"afcf", "avcf", "cfcf"};
	private static final String dataFilePath = "/Users/zijianjiang/Documents/NaM/pattern_set/V1/";
	private static  boolean executeFromFirstBug = true;
	public static void main(String[] args) throws SQLException {
		for (String project : projects) {
//			orderByCommit(project);
			countCommit(project);
		}
		System.out.println(totalCommit);
//		orderByCommit("node");
//		merge("meteor", "avcf");
	}
	
	
	
	public static void mergePatterns(String project) throws SQLException {
		String table = "whole_" + project;
		String afTable = "afcfData1_" + project, cfTable = "cfcfData2_" + project, avTable = "avcfData3_" + project;
		Connection conn = SqliteManager.getConnection();
		Statement stmt = conn.createStatement();
		if (!project.equals("node")) {
			if (executeFromFirstBug){
				stmt.executeUpdate("DROP TABLE IF EXISTS " + table);
			}
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + table + "(cfSig TEXT, cf1Sig TEXT, candiSig TEXT, changed TEXT, f1 INTEGER,f2 INTEGER,f3 TEXT,f4 INTEGER,f5 TEXT,f6 INTEGER,f7 TEXT,f8 INTEGER,f9 INTEGER,f10 INTEGER)");
		}
		else {
			if (executeFromFirstBug){
				stmt.executeUpdate("DROP TABLE IF EXISTS " + table);
			}
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + table + "(cfSig TEXT, cf1Sig TEXT, candiSig TEXT, changed TEXT, f1 INTEGER,f2 INTEGER,f3 TEXT,f4 INTEGER,f5 INTEGER,f6 INTEGER)");
		}
		stmt.close();
		conn.close();
		conn = SqliteManager.getConnection();
		try {
			PreparedStatement ps = conn.prepareStatement("INSERT INTO " + table + " SELECT * FROM " + afTable);
			ps.executeUpdate();
			ps.close();
			ps = conn.prepareStatement("INSERT INTO " + table + " SELECT * FROM " + cfTable);
			ps.executeUpdate();
			ps.close();
			ps = conn.prepareStatement("INSERT INTO " + table + " SELECT * FROM " + avTable);
			ps.executeUpdate();
			ps.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(-1);
		}	
	}
	private static Set<String> set;
	private static int totalCommit = 0;
	public static void countCommit(String project) throws SQLException {
		set = new HashSet<>();
		String afTable = "afCommit_" + project;
		String avTable = "avCommit_" + project;
		String cfTable = "cfCommit_" + project;
		count(afTable);
		count(avTable);
		count(cfTable);
		System.out.println(project + " " + set.size());
		totalCommit += set.size();
	}
	
	public static void count(String table) throws SQLException {
		Connection conn = SqliteManager.getConnection();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT count(*) FROM " + table);
		rs.next();
		int totalNum = rs.getInt(1);
//		Set<String> set = new HashSet<>();
		rs.close();
		stmt.close();
		conn.close();
		for (int offset = 0; offset < totalNum; offset++) {
			conn = SqliteManager.getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT * FROM " + table + " LIMIT 1 OFFSET " + offset);
			String s = rs.getString(1);
//			System.out.println(s);
			String commit = "";
			if (!s.contains("++")) {
//				System.out.println("true");
				commit = s;
			}
			else {
				commit = s.split("\\+\\+")[1];
			}
//				System.out.println(commit);
			rs.close();
			set.add(commit);
//			System.out.println(set.size());
			stmt.close();
			conn.close();
		}
	}
	
	public static void orderByCommit(String project) throws SQLException {
		String table = "whole_" + project;
		String orderedTable = "wholeCommit_" + project;
		Connection conn = SqliteManager.getConnection();
		Statement stmt = conn.createStatement();
		if (!project.equals("node")) {
			if (executeFromFirstBug){
				stmt.executeUpdate("DROP TABLE IF EXISTS " + orderedTable);
			}
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + orderedTable + "(cfSig TEXT, cf1Sig TEXT, candiSig TEXT, changed TEXT, f1 INTEGER,f2 INTEGER,f3 TEXT,f4 INTEGER,f5 TEXT,f6 INTEGER,f7 TEXT,f8 INTEGER,f9 INTEGER,f10 INTEGER)");
		}
		else {
			if (executeFromFirstBug){
				stmt.executeUpdate("DROP TABLE IF EXISTS " + orderedTable);
			}
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + orderedTable + "(cfSig TEXT, cf1Sig TEXT, candiSig TEXT, changed TEXT, f1 INTEGER,f2 INTEGER,f3 TEXT,f4 INTEGER,f5 INTEGER,f6 INTEGER)");
		}
		stmt.close();
		conn.close();
		conn = SqliteManager.getConnection();
		try {
			PreparedStatement ps = conn.prepareStatement("INSERT INTO " + orderedTable + " SELECT * FROM " + table + " ORDER BY cfSig");
			ps.executeUpdate();
			ps.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(-1);
		}	
	}
	
	public static void merge(String project, String pattern) throws SQLException {
		String tableName = pattern + "DataTrainingV1For_" + project;
		Connection conn = SqliteManager.getConnection();
		Statement stmt = conn.createStatement();
		if (executeFromFirstBug){
			stmt.executeUpdate("DROP TABLE IF EXISTS " + tableName);
		}
		switch(pattern) {
		case "afcf":{
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + tableName + "(f1 INTEGER,f2 INTEGER,f3 TEXT,f4 INTEGER,f5 TEXT,f6 INTEGER,f7 TEXT,f8 INTEGER,class TEXT,f9 INTEGER)");
			break;
		}
		case "cfcf":{
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + tableName + "(f1 INTEGER,f2 INTEGER,f3 TEXT,f4 INTEGER,f5 TEXT,f6 INTEGER,f7 TEXT,f8 INTEGER,class TEXT,f9 INTEGER)");
			break;
		}
		default:{
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + tableName + "(f1 INTEGER,f2 INTEGER,f3 TEXT,f4 INTEGER,f5 TEXT,f6 TEXT,f7 INTEGER,f8 INTEGER,class TEXT)");
			break;
		}
		}
		stmt.close();
		conn.close();
		for (String trainingProject : projects) {
			conn = SqliteManager.getConnection();
			if (trainingProject.equals(project) || trainingProject.equals("node")) continue;
			String trainingData = pattern + "DataTV1_" + trainingProject;
			try {
				PreparedStatement ps = conn.prepareStatement("INSERT INTO " + tableName + " SELECT * FROM " + trainingData);
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
