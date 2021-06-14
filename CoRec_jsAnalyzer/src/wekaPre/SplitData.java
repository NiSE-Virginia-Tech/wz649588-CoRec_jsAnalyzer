package wekaPre;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import vt.edu.sql.SqliteManager;

public class SplitData {
	private static String DataNew;
	private static String DataTotal;
	private static int commitNum;
	private static String pattern;
	private static String[] foldNew = new String[5];
	private static String[] foldTotal = new String[5];
	private static String[] foldNew_train = new String[5];
	private static String[] foldTotal_train = new String[5];
	private static boolean testingCreated = false;
	private static boolean trainingCreated = false;
	private static int[] commitInFold = new int[] {27, 27, 27, 27, 26};
	private static int[] seperator = new int[]{0, 11643, 18805, 32800,45008,52480};
	private static int indicator = 0;
	
	public static void main(String[] args) throws SQLException {
		// TODO Auto-generated method stub
		String[] projects = new String[]{"node"};
		Set<String> commitSet = new HashSet<>();
		for (String project : projects) {
			if (!project.equals("node")) continue;
			DataNew = "wholeTotal_" + project;
			
//			DataTotal = pattern + "DataTotal9_" + project;
			
			for (int i = 0; i < 5; i++) {
				foldNew[i] = DataNew + "_testing" + i;
//				foldTotal[i] = DataTotal + "_testing" + i;
				foldNew_train[i] = DataNew + "_training" + i;
//				foldTotal_train[i] = DataTotal + "_training" + i;
				Connection conn = SqliteManager.getConnection();
				Statement stmt = conn.createStatement();
				if (!testingCreated){
					stmt.executeUpdate("DROP TABLE IF EXISTS " + foldNew[i]);
				}
				stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + foldNew[i] + 
						"(afSig TEXT, cf1Sig TEXT, candiSig TEXT, changed TEXT, f1 INTEGER,f2 INTEGER,f3 TEXT,f4 INTEGER,f5 INTEGER,f6 INTEGER)");
				
//				if (!testingCreated){
//					stmt.executeUpdate("DROP TABLE IF EXISTS " + foldTotal[i]);
//				}
//				stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + foldTotal[i] + 
//						"(afSig TEXT, cf1Sig TEXT, candiSig TEXT, changed TEXT, f1 INTEGER,f2 INTEGER,f3 TEXT,f4 INTEGER,f5 INTEGER,f6 INTEGER)");
				
				if (!trainingCreated){
					stmt.executeUpdate("DROP TABLE IF EXISTS " + foldNew_train[i]);
				}
				stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + foldNew_train[i] + 
						"(afSig TEXT, cf1Sig TEXT, candiSig TEXT, changed TEXT, f1 INTEGER,f2 INTEGER,f3 TEXT,f4 INTEGER,f5 INTEGER,f6 INTEGER)");
				
//				if (!trainingCreated){
//					stmt.executeUpdate("DROP TABLE IF EXISTS " + foldTotal_train[i]);
//				}
//				stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + foldTotal_train[i] + 
//						"(afSig TEXT, cf1Sig TEXT, candiSig TEXT, changed TEXT, f1 INTEGER,f2 INTEGER,f3 TEXT,f4 INTEGER,f5 INTEGER,f6 INTEGERR)");
				
				
				stmt.close();
				conn.close();
			}
			
			Connection conn = SqliteManager.getConnection();
//			Statement stmt = conn.createStatement();
//			ResultSet rs = stmt.executeQuery("SELECT count(*) FROM " + DataNew);
//			rs.next();
//			int totalNum = rs.getInt(1);
//			rs.close();
//			stmt.close();
//			conn.close();
//			seperator[5] = totalNum;
//			
//			if (!testingCreated) {
//				for (int offset = 0; offset < totalNum; offset++) {
//					conn = SqliteManager.getConnection();
//					stmt = conn.createStatement();
//					rs = stmt.executeQuery("SELECT * FROM " + DataNew + " LIMIT 1 OFFSET " + offset);
//					String commit = rs.getString(1).substring(0, 7);
//	//				System.out.println(commit);
//					rs.close();
//					commitSet.add(commit);
//					if (commitSet.size() > commitInFold[indicator]) {
//						indicator++;
//						commitSet.clear();
//						commitSet.add(commit);
//						seperator[indicator] = offset;
//						System.out.println(offset);
//					}
//					
//					stmt.close();
					conn.close();
//				}
//				
				/*
				 * split data below
				 */
				
				for (int i = 0; i < 5; i++) {
					conn = SqliteManager.getConnection();
					try {
						PreparedStatement ps = conn.prepareStatement("INSERT INTO " + foldNew[i] + " SELECT * FROM " + DataNew + " LIMIT " + 
								(seperator[i + 1] - seperator[i]) + " OFFSET " + seperator[i]);
						ps.executeUpdate();
						ps.close();
						conn.close();
					} catch (SQLException e) {
						e.printStackTrace();
						System.exit(-1);
					}	
					
//					conn = SqliteManager.getConnection();
//					try {
//						PreparedStatement ps = conn.prepareStatement("INSERT INTO " + foldTotal[i] + " SELECT * FROM " + DataTotal + " LIMIT " + 
//								(seperator[i + 1] - seperator[i]) + " OFFSET " + seperator[i]);
//						ps.executeUpdate();
//						ps.close();
//						conn.close();
//					} catch (SQLException e) {
//						e.printStackTrace();
//						System.exit(-1);
//					}	
				}
			
			/*
			 * merge data below
			 */
			
			for (int i = 0; i < 5; i++) {
				for (int j = 0; j < 5; j++) {
					if (i == j) continue;
//					conn = SqliteManager.getConnection();
//					try {
//						PreparedStatement ps = conn.prepareStatement("INSERT INTO " + foldTotal_train[i] + " SELECT * FROM " + foldTotal[j]);
//						ps.executeUpdate();
//						ps.close();
//						conn.close();
//					} catch (SQLException e) {
//						e.printStackTrace();
//						System.exit(-1);
//					}	
					conn = SqliteManager.getConnection();
					try {
						PreparedStatement ps = conn.prepareStatement("INSERT INTO " + foldNew_train[i] + " SELECT * FROM " + foldNew[j]);
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
	}
	
	public static void mainNode(String[] args) throws SQLException {
		// TODO Auto-generated method stub
		String[] projects = new String[]{"node"};
//		pattern = "avcf";
		Set<String> commitSet = new HashSet<>();
		for (String project : projects) {
			if (!project.equals("node")) continue;
			DataNew = "wholeNew_" + project;
			
			DataTotal = "whole_" + project;
			
			for (int i = 0; i < 5; i++) {
				foldNew[i] = DataNew + "_testing" + i;
				foldTotal[i] = DataTotal + "_testing" + i;
				foldNew_train[i] = DataNew + "_training" + i;
				foldTotal_train[i] = DataTotal + "_training" + i;
				Connection conn = SqliteManager.getConnection();
				Statement stmt = conn.createStatement();
				if (!testingCreated){
					stmt.executeUpdate("DROP TABLE IF EXISTS " + foldNew[i]);
				}
				stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + foldNew[i] + 
						"(afSig TEXT, cf1Sig TEXT, candiSig TEXT, changed TEXT, f1 INTEGER,f2 INTEGER,f3 TEXT,f4 INTEGER,f5 INTEGER,f6 INTEGER)");
				
				if (!testingCreated){
					stmt.executeUpdate("DROP TABLE IF EXISTS " + foldTotal[i]);
				}
				stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + foldTotal[i] + 
						"(afSig TEXT, cf1Sig TEXT, candiSig TEXT, changed TEXT, f1 INTEGER,f2 INTEGER,f3 TEXT,f4 INTEGER,f5 INTEGER,f6 INTEGER)");
				
				if (!trainingCreated){
					stmt.executeUpdate("DROP TABLE IF EXISTS " + foldNew_train[i]);
				}
				stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + foldNew_train[i] + 
						"(afSig TEXT, cf1Sig TEXT, candiSig TEXT, changed TEXT, f1 INTEGER,f2 INTEGER,f3 TEXT,f4 INTEGER,f5 INTEGER,f6 INTEGER)");
				
				if (!trainingCreated){
					stmt.executeUpdate("DROP TABLE IF EXISTS " + foldTotal_train[i]);
				}
				stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + foldTotal_train[i] + 
						"(afSig TEXT, cf1Sig TEXT, candiSig TEXT, changed TEXT, f1 INTEGER,f2 INTEGER,f3 TEXT,f4 INTEGER,f5 INTEGER,f6 INTEGERR)");
				
				
				stmt.close();
				conn.close();
			}
			
			Connection conn = SqliteManager.getConnection();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT count(*) FROM " + DataNew);
			rs.next();
			int totalNum = rs.getInt(1);
			rs.close();
			stmt.close();
			conn.close();
			seperator[5] = totalNum;
			
			if (!testingCreated) {
				for (int offset = 0; offset < totalNum; offset++) {
					conn = SqliteManager.getConnection();
					stmt = conn.createStatement();
					rs = stmt.executeQuery("SELECT * FROM " + DataNew + " LIMIT 1 OFFSET " + offset);
					String commit = rs.getString(1).substring(0, 7);
	//				System.out.println(commit);
					rs.close();
					commitSet.add(commit);
					if (commitSet.size() > commitInFold[indicator]) {
						indicator++;
						commitSet.clear();
						commitSet.add(commit);
						seperator[indicator] = offset;
						System.out.println(offset);
					}
					
					stmt.close();
					conn.close();
				}
				
				/*
				 * split data below
				 */
				
				for (int i = 0; i < 5; i++) {
					conn = SqliteManager.getConnection();
					try {
						PreparedStatement ps = conn.prepareStatement("INSERT INTO " + foldNew[i] + " SELECT * FROM " + DataNew + " LIMIT " + 
								(seperator[i + 1] - seperator[i]) + " OFFSET " + seperator[i]);
						ps.executeUpdate();
						ps.close();
						conn.close();
					} catch (SQLException e) {
						e.printStackTrace();
						System.exit(-1);
					}	
					
					conn = SqliteManager.getConnection();
					try {
						PreparedStatement ps = conn.prepareStatement("INSERT INTO " + foldTotal[i] + " SELECT * FROM " + DataTotal + " LIMIT " + 
								(seperator[i + 1] - seperator[i]) + " OFFSET " + seperator[i]);
						ps.executeUpdate();
						ps.close();
						conn.close();
					} catch (SQLException e) {
						e.printStackTrace();
						System.exit(-1);
					}	
				}
			}
			
			/*
			 * merge data below
			 */
			
			for (int i = 0; i < 5; i++) {
				for (int j = 0; j < 5; j++) {
					if (i == j) continue;
					conn = SqliteManager.getConnection();
					try {
						PreparedStatement ps = conn.prepareStatement("INSERT INTO " + foldTotal_train[i] + " SELECT * FROM " + foldTotal[j]);
						ps.executeUpdate();
						ps.close();
						conn.close();
					} catch (SQLException e) {
						e.printStackTrace();
						System.exit(-1);
					}	
					conn = SqliteManager.getConnection();
					try {
						PreparedStatement ps = conn.prepareStatement("INSERT INTO " + foldNew_train[i] + " SELECT * FROM " + foldNew[j]);
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
		
	}
	
	public static void mainOhtersAll(String[] args) throws SQLException {
		String[] projects = new String[]{"meteor", "Ghost", "pdf", "habitica", "react", "serverless", "webpack1"};
		Set<String> commitSet = new HashSet<>();
		for (String project : projects) {
			if (!project.equals("webpack1")) continue;
			DataNew = "wholeTotal_" + project;
			
//			DataTotal = "DataTotal9_" + project;
			
			for (int i = 0; i < 5; i++) {
				foldNew[i] = DataNew + "_testing" + i;
//				foldTotal[i] = DataTotal + "_testing" + i;
				foldNew_train[i] = DataNew + "_training" + i;
//				foldTotal_train[i] = DataTotal + "_training" + i;
				Connection conn = SqliteManager.getConnection();
				Statement stmt = conn.createStatement();
				if (!testingCreated){
					stmt.executeUpdate("DROP TABLE IF EXISTS " + foldNew[i]);
				}
				stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + foldNew[i] + 
						"(afSig TEXT, cf1Sig TEXT, candiSig TEXT, changed TEXT, f1 INTEGER,f2 INTEGER,f3 TEXT,f4 INTEGER,f5 TEXT,f6 INTEGER,f7 TEXT,f8 INTEGER,f9 INTEGER,f10 INTEGER)");
				
//				if (!testingCreated){
//					stmt.executeUpdate("DROP TABLE IF EXISTS " + foldTotal[i]);
//				}
//				stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + foldTotal[i] + 
//						"(afSig TEXT, cf1Sig TEXT, candiSig TEXT, changed TEXT, f1 INTEGER,f2 INTEGER,f3 TEXT,f4 INTEGER,f5 TEXT,f6 TEXT,f7 INTEGER,f8 INTEGER,f9 INTEGER)");
//				
				if (!trainingCreated){
					stmt.executeUpdate("DROP TABLE IF EXISTS " + foldNew_train[i]);
				}
				stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + foldNew_train[i] + 
						"(afSig TEXT, cf1Sig TEXT, candiSig TEXT, changed TEXT, f1 INTEGER,f2 INTEGER,f3 TEXT,f4 INTEGER,f5 TEXT,f6 INTEGER,f7 TEXT,f8 INTEGER,f9 INTEGER,f10 INTEGER)");
				
//				if (!trainingCreated){
//					stmt.executeUpdate("DROP TABLE IF EXISTS " + foldTotal_train[i]);
//				}
//				stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + foldTotal_train[i] + 
//						"(afSig TEXT, cf1Sig TEXT, candiSig TEXT, changed TEXT, f1 INTEGER,f2 INTEGER,f3 TEXT,f4 INTEGER,f5 TEXT,f6 TEXT,f7 INTEGER,f8 INTEGER,f9 INTEGER)");
//				
				
				stmt.close();
				conn.close();
			}
			
			Connection conn = SqliteManager.getConnection();
//			Statement stmt = conn.createStatement();
//			ResultSet rs = stmt.executeQuery("SELECT count(*) FROM " + DataNew);
//			rs.next();
//			int totalNum = rs.getInt(1);
//			rs.close();
//			stmt.close();
//			conn.close();
//			seperator[5] = totalNum;
//			
//			if (!testingCreated) {
//				for (int offset = 0; offset < totalNum; offset++) {
//					conn = SqliteManager.getConnection();
//					stmt = conn.createStatement();
//					rs = stmt.executeQuery("SELECT * FROM " + DataNew + " LIMIT 1 OFFSET " + offset);
//					String commit = rs.getString(1).substring(0, 7);
//	//				System.out.println(commit);
//					rs.close();
//					commitSet.add(commit);
//					if (commitSet.size() > commitInFold[indicator]) {
//						indicator++;
//						commitSet.clear();
//						commitSet.add(commit);
//						seperator[indicator] = offset;
//						System.out.println(offset);
//					}
//					
//					stmt.close();
					conn.close();
//				}
//				
				/*
				 * split data below
				 */
				
				for (int i = 0; i < 5; i++) {
					conn = SqliteManager.getConnection();
					try {
						PreparedStatement ps = conn.prepareStatement("INSERT INTO " + foldNew[i] + " SELECT * FROM " + DataNew + " LIMIT " + 
								(seperator[i + 1] - seperator[i]) + " OFFSET " + seperator[i]);
						ps.executeUpdate();
						ps.close();
						conn.close();
					} catch (SQLException e) {
						e.printStackTrace();
						System.exit(-1);
					}	
					
//					conn = SqliteManager.getConnection();
//					try {
//						PreparedStatement ps = conn.prepareStatement("INSERT INTO " + foldTotal[i] + " SELECT * FROM " + DataTotal + " LIMIT " + 
//								(seperator[i + 1] - seperator[i]) + " OFFSET " + seperator[i]);
//						ps.executeUpdate();
//						ps.close();
//						conn.close();
//					} catch (SQLException e) {
//						e.printStackTrace();
//						System.exit(-1);
//					}	
				}
			
			
			/*
			 * merge data below
			 */
			
			for (int i = 0; i < 5; i++) {
				for (int j = 0; j < 5; j++) {
					if (i == j) continue;
//					conn = SqliteManager.getConnection();
//					try {
//						PreparedStatement ps = conn.prepareStatement("INSERT INTO " + foldTotal_train[i] + " SELECT * FROM " + foldTotal[j]);
//						ps.executeUpdate();
//						ps.close();
//						conn.close();
//					} catch (SQLException e) {
//						e.printStackTrace();
//						System.exit(-1);
//					}	
					conn = SqliteManager.getConnection();
					try {
						PreparedStatement ps = conn.prepareStatement("INSERT INTO " + foldNew_train[i] + " SELECT * FROM " + foldNew[j]);
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
	}
	
	public static void mainOthers(String[] args) throws SQLException {
		// TODO Auto-generated method stub
		String[] projects = new String[]{"meteor", "Ghost", "pdf", "habitica", "react", "serverless", "webpack1"};
		pattern = "avcf";
		Set<String> commitSet = new HashSet<>();
		for (String project : projects) {
			if (!project.equals("meteor")) continue;
			DataNew = pattern + "DataNew9_" + project;
			
			DataTotal = pattern + "DataTotal9_" + project;
			commitNum = 59;
			
			for (int i = 0; i < 5; i++) {
				foldNew[i] = DataNew + "_testing" + i;
				foldTotal[i] = DataTotal + "_testing" + i;
				foldNew_train[i] = DataNew + "_training" + i;
				foldTotal_train[i] = DataTotal + "_training" + i;
				Connection conn = SqliteManager.getConnection();
				Statement stmt = conn.createStatement();
				if (!testingCreated){
					stmt.executeUpdate("DROP TABLE IF EXISTS " + foldNew[i]);
				}
				stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + foldNew[i] + 
						"(afSig TEXT, cf1Sig TEXT, candiSig TEXT, changed TEXT, f1 INTEGER,f2 INTEGER,f3 TEXT,f4 INTEGER,f5 TEXT,f6 TEXT,f7 INTEGER,f8 INTEGER,f9 INTEGER)");
				
				if (!testingCreated){
					stmt.executeUpdate("DROP TABLE IF EXISTS " + foldTotal[i]);
				}
				stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + foldTotal[i] + 
						"(afSig TEXT, cf1Sig TEXT, candiSig TEXT, changed TEXT, f1 INTEGER,f2 INTEGER,f3 TEXT,f4 INTEGER,f5 TEXT,f6 TEXT,f7 INTEGER,f8 INTEGER,f9 INTEGER)");
				
				if (!trainingCreated){
					stmt.executeUpdate("DROP TABLE IF EXISTS " + foldNew_train[i]);
				}
				stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + foldNew_train[i] + 
						"(afSig TEXT, cf1Sig TEXT, candiSig TEXT, changed TEXT, f1 INTEGER,f2 INTEGER,f3 TEXT,f4 INTEGER,f5 TEXT,f6 TEXT,f7 INTEGER,f8 INTEGER,f9 INTEGER)");
				
				if (!trainingCreated){
					stmt.executeUpdate("DROP TABLE IF EXISTS " + foldTotal_train[i]);
				}
				stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + foldTotal_train[i] + 
						"(afSig TEXT, cf1Sig TEXT, candiSig TEXT, changed TEXT, f1 INTEGER,f2 INTEGER,f3 TEXT,f4 INTEGER,f5 TEXT,f6 TEXT,f7 INTEGER,f8 INTEGER,f9 INTEGER)");
				
				
				stmt.close();
				conn.close();
			}
			
			Connection conn = SqliteManager.getConnection();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT count(*) FROM " + DataNew);
			rs.next();
			int totalNum = rs.getInt(1);
			rs.close();
			stmt.close();
			conn.close();
			seperator[5] = totalNum;
			
			if (!testingCreated) {
				for (int offset = 0; offset < totalNum; offset++) {
					conn = SqliteManager.getConnection();
					stmt = conn.createStatement();
					rs = stmt.executeQuery("SELECT * FROM " + DataNew + " LIMIT 1 OFFSET " + offset);
					String commit = rs.getString(1).substring(0, 7);
	//				System.out.println(commit);
					rs.close();
					commitSet.add(commit);
					if (commitSet.size() > commitInFold[indicator]) {
						indicator++;
						commitSet.clear();
						commitSet.add(commit);
						seperator[indicator] = offset;
						System.out.println(offset);
					}
					
					stmt.close();
					conn.close();
				}
				
				/*
				 * split data below
				 */
				
				for (int i = 0; i < 5; i++) {
					conn = SqliteManager.getConnection();
					try {
						PreparedStatement ps = conn.prepareStatement("INSERT INTO " + foldNew[i] + " SELECT * FROM " + DataNew + " LIMIT " + 
								(seperator[i + 1] - seperator[i]) + " OFFSET " + seperator[i]);
						ps.executeUpdate();
						ps.close();
						conn.close();
					} catch (SQLException e) {
						e.printStackTrace();
						System.exit(-1);
					}	
					
					conn = SqliteManager.getConnection();
					try {
						PreparedStatement ps = conn.prepareStatement("INSERT INTO " + foldTotal[i] + " SELECT * FROM " + DataTotal + " LIMIT " + 
								(seperator[i + 1] - seperator[i]) + " OFFSET " + seperator[i]);
						ps.executeUpdate();
						ps.close();
						conn.close();
					} catch (SQLException e) {
						e.printStackTrace();
						System.exit(-1);
					}	
				}
			}
			
			/*
			 * merge data below
			 */
			
			for (int i = 0; i < 5; i++) {
				for (int j = 0; j < 5; j++) {
					if (i == j) continue;
					conn = SqliteManager.getConnection();
					try {
						PreparedStatement ps = conn.prepareStatement("INSERT INTO " + foldTotal_train[i] + " SELECT * FROM " + foldTotal[j]);
						ps.executeUpdate();
						ps.close();
						conn.close();
					} catch (SQLException e) {
						e.printStackTrace();
						System.exit(-1);
					}	
					conn = SqliteManager.getConnection();
					try {
						PreparedStatement ps = conn.prepareStatement("INSERT INTO " + foldNew_train[i] + " SELECT * FROM " + foldNew[j]);
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
		
	}

}
