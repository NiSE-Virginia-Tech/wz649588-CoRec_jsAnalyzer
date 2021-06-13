package wekaPre;

import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import vt.edu.sql.SqliteManager;
import weka.core.Instance;
import weka.core.UnassignedClassException;

public class Randomization {
	private static String avcfData;
	private static String afcfData;
	private static String cfcfData;
	
	private static String avcfDataT;
	private static String afcfDataT;
	private static String afcfDataTotal;
	private static String cfcfDataT;
	private static Map<Integer, String> map = new HashMap<>();
	private static String wholeData;
	private static String wholeNew, wholeTotal;
	private static double fraction = 0.5;
	
	private static boolean executeFromFirstBug = true;
	
	public static void main(String[] args) throws SQLException {
		// TODO Auto-generated method stub
				String repoPath = "/Users/zijianjiang/test-repos";
				File repos = new File(repoPath);
				String[] directories = repos.list(new FilenameFilter() {
				  @Override
				  public boolean accept(File current, String name) {
				    return new File(current, name).isDirectory();
				  }
				});
				for(String folder : directories) {
					System.out.println(folder);
//					if(!folder.matches("webpack1|serverless|react|Ghost|habitica")) continue;
					if(!folder.matches("node")) continue;
//					if (!folder.equals("meteor")) continue;
					wholeData = "wholeCommit_" + folder;
//					avcfData = "avcfData3_" + folder;
//					cfcfData = "cfcfData2_" + folder;
					wholeNew = "wholeNew_" + folder;
					wholeTotal = "wholeTotal_" + folder;
//					avcfDataT = "avcfDataTV2_" + folder;
//					cfcfDataT = "cfcfDataTV2_" + folder;
					Connection conn = SqliteManager.getConnection();
					Statement stmt = conn.createStatement();
//					if (executeFromFirstBug){
//						stmt.executeUpdate("DROP TABLE IF EXISTS " + afcfDataT);
//					}
//					stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + afcfDataT + "(f1 INTEGER,f2 INTEGER,f3 TEXT,f4 INTEGER,f5 TEXT,f6 INTEGER,f7 TEXT,f8 INTEGER,class TEXT,f9 INTEGER,f10 INTEGER)");
					
//					if (executeFromFirstBug){
//						stmt.executeUpdate("DROP TABLE IF EXISTS " + cfcfDataT);
//					}
//					stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + cfcfDataT + "(f1 INTEGER,f2 INTEGER,f3 TEXT,f4 INTEGER,f5 TEXT,f6 INTEGER,f7 TEXT,f8 INTEGER,class TEXT,f9 INTEGER,f10 INTEGER)");
////					
					if (executeFromFirstBug){
						stmt.executeUpdate("DROP TABLE IF EXISTS " + wholeNew);
					}
					stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + wholeNew + "(cfSig TEXT, cf1Sig TEXT, candiSig TEXT, changed TEXT, f1 INTEGER,f2 INTEGER,f3 TEXT,f4 INTEGER,f5 INTEGER,f6 INTEGER)");
//					
					if (executeFromFirstBug){
						stmt.executeUpdate("DROP TABLE IF EXISTS " + wholeTotal);
					}
					stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + wholeTotal + "(cfSig TEXT, cf1Sig TEXT, candiSig TEXT, changed TEXT, f1 INTEGER,f2 INTEGER,f3 TEXT,f4 INTEGER,f5 INTEGER,f6 INTEGER)");
					ResultSet rs = stmt.executeQuery("SELECT count(*) FROM " + wholeData);
					rs.next();
					int totalNum = rs.getInt(1);
					System.out.println(totalNum);
					
					int[] index = new int[totalNum];
					for (int i = 0; i < totalNum; i++) {
						index[i] = i;
					}
					
					shuffle(index, (int)(totalNum * fraction));
					
					stmt.close();
					conn.close();
					boolean av = false;
					if(av){
						for (int offset = 0; offset < totalNum; offset++) {
							conn = SqliteManager.getConnection();
							stmt = conn.createStatement();
							
							rs = stmt.executeQuery("SELECT * FROM " + avcfData + " LIMIT 1 OFFSET " + offset);
							int f1 = rs.getInt(5), f2 = rs.getInt(6), f4 = rs.getInt(8),
									 f7 = rs.getInt(11), f8 = rs.getInt(12),f9 = rs.getInt(13);
							String f3 = rs.getString(7), classify = rs.getString(4), f5 = rs.getString(9), f6 = rs.getString(10);
							rs.close();
							
							stmt.close();
							conn.close();
							System.out.println(offset + 1 + "/" + totalNum);
							
							conn = SqliteManager.getConnection();
							try {
								
								
								PreparedStatement ps = conn.prepareStatement("INSERT INTO " + avcfDataT + " (f1,f2,f3,f4,f5,f6,f7,f8,class,f9) VALUES (?,?,?,?,?,?,?,?,?,?)");
								ps.setInt(1, f1);
								ps.setInt(2, f2);
								ps.setString(3, f3);
								ps.setInt(4, f4);
								ps.setString(5, f5);
								ps.setString(6, f6);
								ps.setInt(7, f7);
								ps.setInt(8, f8);
								ps.setString(9, classify);
								ps.setInt(10, f9);
								ps.executeUpdate();
								ps.close();
								conn.close();
							} catch (SQLException e) {
								e.printStackTrace();
								System.exit(-1);
							}
						}
					}
					
					else {						
						for (int offset = 0; offset < totalNum; offset++) {
							conn = SqliteManager.getConnection();
							stmt = conn.createStatement();
							
							rs = stmt.executeQuery("SELECT * FROM " + wholeData + " LIMIT 1 OFFSET " + index[offset]);
							int f1 = rs.getInt(5), f2 = rs.getInt(6), f4 = rs.getInt(8),
									 f5 = rs.getInt(9), f6 = rs.getInt(10);
							String f3 = rs.getString(7), classify = rs.getString(4);
							String afSigReal = rs.getString(1), cf1SigReal = rs.getString(2), candiSigReal = rs.getString(3);
							rs.close();
							
							rs = stmt.executeQuery("SELECT * FROM " + wholeData + " LIMIT 1 OFFSET " + offset);
							String afSig = rs.getString(1), cf1Sig = rs.getString(2), candiSig = rs.getString(3);
							rs.close();
						
							
							stmt.close();
							conn.close();
							System.out.println(offset + 1 + "/" + totalNum);
							
							conn = SqliteManager.getConnection();
							try {
								PreparedStatement ps = conn.prepareStatement("INSERT INTO " + wholeNew + " (cfSig,cf1Sig,candiSig,changed,f1,f2,f3,f4,f5,f6) VALUES (?,?,?,?,?,?,?,?,?,?)");
								ps.setString(1, afSig);
								ps.setString(2, cf1Sig);
								ps.setString(3, candiSig);
								ps.setString(4, classify);
								ps.setInt(5, f1);
								ps.setInt(6, f2);
								ps.setString(7, f3);
								ps.setInt(8, f4);
								ps.setInt(9, f5);
								ps.setInt(10, f6);
								ps.executeUpdate();
								ps.close();
								conn.close();
							} catch (SQLException e) {
								e.printStackTrace();
								System.exit(-1);
							}
							
							conn = SqliteManager.getConnection();
							try {
								PreparedStatement ps = conn.prepareStatement("INSERT INTO " + wholeTotal + " (cfSig,cf1Sig,candiSig,changed,f1,f2,f3,f4,f5,f6) VALUES (?,?,?,?,?,?,?,?,?,?)");
								ps.setString(1, afSigReal);
								ps.setString(2, cf1SigReal);
								ps.setString(3, candiSigReal);
								ps.setString(4, classify);
								ps.setInt(5, f1);
								ps.setInt(6, f2);
								ps.setString(7, f3);
								ps.setInt(8, f4);
								ps.setInt(9, f5);
								ps.setInt(10, f6);
								ps.executeUpdate();
								ps.close();
								conn.close();
							} catch (SQLException e) {
								e.printStackTrace();
								System.exit(-1);
							}
							
//							conn = SqliteManager.getConnection();
//							stmt = conn.createStatement();
//							
//							System.out.println(offset + 1 + "/" + totalNum);
//							System.out.println(index[offset]);
//							
//							conn = SqliteManager.getConnection();
//							try {
//								PreparedStatement ps = conn.prepareStatement("INSERT INTO " + afcfDataTotal + " (afSig,cf1Sig,candiSig,changed,f1,f2,f3,f4,f5,f6) VALUES (?,?,?,?,?,?,?,?,?,?)");
//								ps.setString(1, afSigReal);
//								ps.setString(2, cf1SigReal);
//								ps.setString(3, candiSigReal);
//								ps.setString(4, classify);
//								ps.setInt(5, f1);
//								ps.setInt(6, f2);
//								ps.setString(7, f3);
//								ps.setInt(8, f4);
//								ps.setInt(9, f5);
//								ps.setInt(10, f6);
//								ps.executeUpdate();
//								ps.close();
//								conn.close();
//							} catch (SQLException e) {
//								e.printStackTrace();
//								System.exit(-1);
//							}
						}
					}
							
				}
	}
	
	public static void mainNode(String[] args) throws SQLException {
		// TODO Auto-generated method stub
				String repoPath = "/Users/zijianjiang/test-repos";
				File repos = new File(repoPath);
				String[] directories = repos.list(new FilenameFilter() {
				  @Override
				  public boolean accept(File current, String name) {
				    return new File(current, name).isDirectory();
				  }
				});
				for(String folder : directories) {
					System.out.println(folder);
//					if(!folder.matches("webpack1|serverless|react|Ghost|habitica")) continue;
					if(!folder.matches("node")) continue;
//					if (!folder.equals("meteor")) continue;
					afcfData = "avcfData3_" + folder;
//					avcfData = "avcfData3_" + folder;
//					cfcfData = "cfcfData2_" + folder;
					afcfDataT = "avcfDataNew9_" + folder;
					afcfDataTotal = "avcfDataTotal9_" + folder;
//					avcfDataT = "avcfDataTV2_" + folder;
//					cfcfDataT = "cfcfDataTV2_" + folder;
					Connection conn = SqliteManager.getConnection();
					Statement stmt = conn.createStatement();
//					if (executeFromFirstBug){
//						stmt.executeUpdate("DROP TABLE IF EXISTS " + afcfDataT);
//					}
//					stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + afcfDataT + "(f1 INTEGER,f2 INTEGER,f3 TEXT,f4 INTEGER,f5 TEXT,f6 INTEGER,f7 TEXT,f8 INTEGER,class TEXT,f9 INTEGER,f10 INTEGER)");
					
//					if (executeFromFirstBug){
//						stmt.executeUpdate("DROP TABLE IF EXISTS " + cfcfDataT);
//					}
//					stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + cfcfDataT + "(f1 INTEGER,f2 INTEGER,f3 TEXT,f4 INTEGER,f5 TEXT,f6 INTEGER,f7 TEXT,f8 INTEGER,class TEXT,f9 INTEGER,f10 INTEGER)");
////					
					if (executeFromFirstBug){
						stmt.executeUpdate("DROP TABLE IF EXISTS " + afcfDataT);
					}
					stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + afcfDataT + "(cfSig TEXT, cf1Sig TEXT, candiSig TEXT, changed TEXT, f1 INTEGER,f2 INTEGER,f3 TEXT,f4 INTEGER,f5 INTEGER,f6 INTEGER)");
//					
					if (executeFromFirstBug){
						stmt.executeUpdate("DROP TABLE IF EXISTS " + afcfDataTotal);
					}
					stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + afcfDataTotal + "(cfSig TEXT, cf1Sig TEXT, candiSig TEXT, changed TEXT, f1 INTEGER,f2 INTEGER,f3 TEXT,f4 INTEGER,f5 INTEGER,f6 INTEGER)");
					ResultSet rs = stmt.executeQuery("SELECT count(*) FROM " + afcfData);
					rs.next();
					int totalNum = rs.getInt(1);
					System.out.println(totalNum);
					
					int[] index = new int[totalNum];
					for (int i = 0; i < totalNum; i++) {
						index[i] = i;
					}
					
					shuffle(index, (int)(totalNum * fraction));
					
					stmt.close();
					conn.close();
					boolean av = false;
					if(av){
						for (int offset = 0; offset < totalNum; offset++) {
							conn = SqliteManager.getConnection();
							stmt = conn.createStatement();
							
							rs = stmt.executeQuery("SELECT * FROM " + avcfData + " LIMIT 1 OFFSET " + offset);
							int f1 = rs.getInt(5), f2 = rs.getInt(6), f4 = rs.getInt(8),
									 f7 = rs.getInt(11), f8 = rs.getInt(12),f9 = rs.getInt(13);
							String f3 = rs.getString(7), classify = rs.getString(4), f5 = rs.getString(9), f6 = rs.getString(10);
							rs.close();
							
							stmt.close();
							conn.close();
							System.out.println(offset + 1 + "/" + totalNum);
							
							conn = SqliteManager.getConnection();
							try {
								
								
								PreparedStatement ps = conn.prepareStatement("INSERT INTO " + avcfDataT + " (f1,f2,f3,f4,f5,f6,f7,f8,class,f9) VALUES (?,?,?,?,?,?,?,?,?,?)");
								ps.setInt(1, f1);
								ps.setInt(2, f2);
								ps.setString(3, f3);
								ps.setInt(4, f4);
								ps.setString(5, f5);
								ps.setString(6, f6);
								ps.setInt(7, f7);
								ps.setInt(8, f8);
								ps.setString(9, classify);
								ps.setInt(10, f9);
								ps.executeUpdate();
								ps.close();
								conn.close();
							} catch (SQLException e) {
								e.printStackTrace();
								System.exit(-1);
							}
						}
					}
					
					else {						
						for (int offset = 0; offset < totalNum; offset++) {
							conn = SqliteManager.getConnection();
							stmt = conn.createStatement();
							
							rs = stmt.executeQuery("SELECT * FROM " + afcfData + " LIMIT 1 OFFSET " + index[offset]);
							int f1 = rs.getInt(5), f2 = rs.getInt(6), f4 = rs.getInt(8),
									 f5 = rs.getInt(9), f6 = rs.getInt(10);
							String f3 = rs.getString(7), classify = rs.getString(4);
							String afSigReal = rs.getString(1), cf1SigReal = rs.getString(2), candiSigReal = rs.getString(3);
							rs.close();
							
							rs = stmt.executeQuery("SELECT * FROM " + afcfData + " LIMIT 1 OFFSET " + offset);
							String afSig = rs.getString(1), cf1Sig = rs.getString(2), candiSig = rs.getString(3);
							rs.close();
						
							
							stmt.close();
							conn.close();
							System.out.println(offset + 1 + "/" + totalNum);
							
							conn = SqliteManager.getConnection();
							try {
								PreparedStatement ps = conn.prepareStatement("INSERT INTO " + afcfDataT + " (cfSig,cf1Sig,candiSig,changed,f1,f2,f3,f4,f5,f6) VALUES (?,?,?,?,?,?,?,?,?,?)");
								ps.setString(1, afSig);
								ps.setString(2, cf1Sig);
								ps.setString(3, candiSig);
								ps.setString(4, classify);
								ps.setInt(5, f1);
								ps.setInt(6, f2);
								ps.setString(7, f3);
								ps.setInt(8, f4);
								ps.setInt(9, f5);
								ps.setInt(10, f6);
								ps.executeUpdate();
								ps.close();
								conn.close();
							} catch (SQLException e) {
								e.printStackTrace();
								System.exit(-1);
							}
							
							conn = SqliteManager.getConnection();
							try {
								PreparedStatement ps = conn.prepareStatement("INSERT INTO " + afcfDataTotal + " (cfSig,cf1Sig,candiSig,changed,f1,f2,f3,f4,f5,f6) VALUES (?,?,?,?,?,?,?,?,?,?)");
								ps.setString(1, afSigReal);
								ps.setString(2, cf1SigReal);
								ps.setString(3, candiSigReal);
								ps.setString(4, classify);
								ps.setInt(5, f1);
								ps.setInt(6, f2);
								ps.setString(7, f3);
								ps.setInt(8, f4);
								ps.setInt(9, f5);
								ps.setInt(10, f6);
								ps.executeUpdate();
								ps.close();
								conn.close();
							} catch (SQLException e) {
								e.printStackTrace();
								System.exit(-1);
							}
							
//							conn = SqliteManager.getConnection();
//							stmt = conn.createStatement();
//							
//							System.out.println(offset + 1 + "/" + totalNum);
//							System.out.println(index[offset]);
//							
//							conn = SqliteManager.getConnection();
//							try {
//								PreparedStatement ps = conn.prepareStatement("INSERT INTO " + afcfDataTotal + " (afSig,cf1Sig,candiSig,changed,f1,f2,f3,f4,f5,f6) VALUES (?,?,?,?,?,?,?,?,?,?)");
//								ps.setString(1, afSigReal);
//								ps.setString(2, cf1SigReal);
//								ps.setString(3, candiSigReal);
//								ps.setString(4, classify);
//								ps.setInt(5, f1);
//								ps.setInt(6, f2);
//								ps.setString(7, f3);
//								ps.setInt(8, f4);
//								ps.setInt(9, f5);
//								ps.setInt(10, f6);
//								ps.executeUpdate();
//								ps.close();
//								conn.close();
//							} catch (SQLException e) {
//								e.printStackTrace();
//								System.exit(-1);
//							}
						}
					}
							
				}
	}
	
	public static void mainOthers(String[] args) throws SQLException {
		// TODO Auto-generated method stub
		String repoPath = "/Users/zijianjiang/test-repos";
		File repos = new File(repoPath);
		String[] directories = repos.list(new FilenameFilter() {
		  @Override
		  public boolean accept(File current, String name) {
		    return new File(current, name).isDirectory();
		  }
		});
		for(String folder : directories) {
			System.out.println(folder);
//			if(!folder.matches("webpack1|serverless|react|Ghost|habitica")) continue;
			if(!folder.matches("serverless|meteor|Ghost|habitica|pdf|webpack1|react")) continue;
			if (!folder.equals("meteor")) continue;
			afcfData = "avcfData3_" + folder;
//			avcfData = "avcfData3_" + folder;
//			cfcfData = "cfcfData2_" + folder;
			afcfDataT = "avcfDataNew9_" + folder;
			afcfDataTotal = "avcfDataTotal9_" + folder;
//			avcfDataT = "avcfDataTV2_" + folder;
//			cfcfDataT = "cfcfDataTV2_" + folder;
			Connection conn = SqliteManager.getConnection();
			Statement stmt = conn.createStatement();
//			if (executeFromFirstBug){
//				stmt.executeUpdate("DROP TABLE IF EXISTS " + afcfDataT);
//			}
//			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + afcfDataT + "(f1 INTEGER,f2 INTEGER,f3 TEXT,f4 INTEGER,f5 TEXT,f6 INTEGER,f7 TEXT,f8 INTEGER,class TEXT,f9 INTEGER,f10 INTEGER)");
			
//			if (executeFromFirstBug){
//				stmt.executeUpdate("DROP TABLE IF EXISTS " + cfcfDataT);
//			}
//			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + cfcfDataT + "(f1 INTEGER,f2 INTEGER,f3 TEXT,f4 INTEGER,f5 TEXT,f6 INTEGER,f7 TEXT,f8 INTEGER,class TEXT,f9 INTEGER,f10 INTEGER)");
////			
			if (executeFromFirstBug){
				stmt.executeUpdate("DROP TABLE IF EXISTS " + afcfDataT);
			}
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + afcfDataT + "(cfSig TEXT, cf1Sig TEXT, candiSig TEXT, changed TEXT, f1 INTEGER,f2 INTEGER,f3 TEXT,f4 INTEGER,f5 TEXT,f6 TEXT,f7 INTEGER,f8 INTEGER,f9 INTEGER)");
////			
			if (executeFromFirstBug){
				stmt.executeUpdate("DROP TABLE IF EXISTS " + afcfDataTotal);
			}
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + afcfDataTotal + "(cfSig TEXT, cf1Sig TEXT, candiSig TEXT, changed TEXT, f1 INTEGER,f2 INTEGER,f3 TEXT,f4 INTEGER,f5 TEXT,f6 TEXT,f7 INTEGER,f8 INTEGER,f9 INTEGER)");
			ResultSet rs = stmt.executeQuery("SELECT count(*) FROM " + afcfData);
			rs.next();
			int totalNum = rs.getInt(1);
			System.out.println(totalNum);
			
			int[] index = new int[totalNum];
			for (int i = 0; i < totalNum; i++) {
				index[i] = i;
			}
			
//			shuffle(index, (int)(totalNum*fraction));
//			shuffle(index, (int)(totalNum*fraction));
//			shuffle(index, (int)(totalNum*fraction));
			
			
			stmt.close();
			conn.close();
			shuffle(index, totalNum);
			
			for (int offset = 0; offset < totalNum; offset++) {
				System.out.println(offset);
				conn = SqliteManager.getConnection();
				stmt = conn.createStatement();
				rs = stmt.executeQuery("SELECT * FROM " + afcfData + " LIMIT 1 OFFSET " + offset);
				String result = rs.getString(4);
				map.put(offset, result);
				rs.close();
				stmt.close();
				conn.close();
			}
			stratify(totalNum, index);
			index = stratStep(index);
			
			boolean av = true;
			if(av){
				for (int offset = 0; offset < totalNum; offset++) {
					conn = SqliteManager.getConnection();
					stmt = conn.createStatement();
					
					rs = stmt.executeQuery("SELECT * FROM " + afcfData + " LIMIT 1 OFFSET " + index[offset]);
					int f1 = rs.getInt(5), f2 = rs.getInt(6), f4 = rs.getInt(8),
							 f7 = rs.getInt(11), f8 = rs.getInt(12), f9 = rs.getInt(13);
					String f3 = rs.getString(7), classify = rs.getString(4), f5 = rs.getString(9), f6 = rs.getString(10);
					String afSigReal = rs.getString(1), cf1SigReal = rs.getString(2), candiSigReal = rs.getString(3);
					rs.close();
					
					rs = stmt.executeQuery("SELECT * FROM " + afcfData + " LIMIT 1 OFFSET " + offset);
					String afSig = rs.getString(1), cf1Sig = rs.getString(2), candiSig = rs.getString(3);
					rs.close();
				
					
					stmt.close();
					conn.close();
					System.out.println(offset + 1 + "/" + totalNum);
					
					conn = SqliteManager.getConnection();
					try {
						PreparedStatement ps = conn.prepareStatement("INSERT INTO " + afcfDataT + " (cfSig,cf1Sig,candiSig,changed,f1,f2,f3,f4,f5,f6,f7,f8,f9) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)");
						ps.setString(1, afSig);
						ps.setString(2, cf1Sig);
						ps.setString(3, candiSig);
						ps.setString(4, classify);
						ps.setInt(5, f1);
						ps.setInt(6, f2);
						ps.setString(7, f3);
						ps.setInt(8, f4);
						ps.setString(9, f5);
						ps.setString(10, f6);
						ps.setInt(11, f7);
						ps.setInt(12, f8);
						ps.setInt(13, f9);
						ps.executeUpdate();
						ps.close();
						conn.close();
					} catch (SQLException e) {
						e.printStackTrace();
						System.exit(-1);
					}
					
//					conn = SqliteManager.getConnection();
//					stmt = conn.createStatement();
					
					System.out.println(offset + 1 + "/" + totalNum);
					
					conn = SqliteManager.getConnection();
					try {
						PreparedStatement ps = conn.prepareStatement("INSERT INTO " + afcfDataTotal + " (cfSig,cf1Sig,candiSig,changed,f1,f2,f3,f4,f5,f6,f7,f8,f9) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)");
						ps.setString(1, afSigReal);
						ps.setString(2, cf1SigReal);
						ps.setString(3, candiSigReal);
						ps.setString(4, classify);
						ps.setInt(5, f1);
						ps.setInt(6, f2);
						ps.setString(7, f3);
						ps.setInt(8, f4);
						ps.setString(9, f5);
						ps.setString(10, f6);
						ps.setInt(11, f7);
						ps.setInt(12, f8);
						ps.setInt(13, f9);
						ps.executeUpdate();
						ps.close();
						conn.close();
					} catch (SQLException e) {
						e.printStackTrace();
						System.exit(-1);
					}
				}
			}
			
			else {
				for (int offset = 0; offset < totalNum; offset++) {
					conn = SqliteManager.getConnection();
					stmt = conn.createStatement();
					
					rs = stmt.executeQuery("SELECT * FROM " + afcfData + " LIMIT 1 OFFSET " + index[offset]);
					int f1 = rs.getInt(5), f2 = rs.getInt(6), f4 = rs.getInt(8),
							 f6 = rs.getInt(10), f8 = rs.getInt(12), f9 = rs.getInt(13),f10 = rs.getInt(14);
					String f3 = rs.getString(7), classify = rs.getString(4), f5 = rs.getString(9), f7 = rs.getString(11);
					String afSigReal = rs.getString(1), cf1SigReal = rs.getString(2), candiSigReal = rs.getString(3);
					rs.close();
					
					rs = stmt.executeQuery("SELECT * FROM " + afcfData + " LIMIT 1 OFFSET " + offset);
					String afSig = rs.getString(1), cf1Sig = rs.getString(2), candiSig = rs.getString(3);
					rs.close();
				
					
					stmt.close();
					conn.close();
					System.out.println(offset + 1 + "/" + totalNum);
					
					conn = SqliteManager.getConnection();
					try {
						PreparedStatement ps = conn.prepareStatement("INSERT INTO " + afcfDataT + " (cfSig,cf1Sig,candiSig,changed,f1,f2,f3,f4,f5,f6,f7,f8,f9,f10) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
						ps.setString(1, afSig);
						ps.setString(2, cf1Sig);
						ps.setString(3, candiSig);
						ps.setString(4, classify);
						ps.setInt(5, f1);
						ps.setInt(6, f2);
						ps.setString(7, f3);
						ps.setInt(8, f4);
						ps.setString(9, f5);
						ps.setInt(10, f6);
						ps.setString(11, f7);
						ps.setInt(12, f8);
						ps.setInt(13, f9);
						ps.setInt(14,f10);
						ps.executeUpdate();
						ps.close();
						conn.close();
					} catch (SQLException e) {
						e.printStackTrace();
						System.exit(-1);
					}
					
//					conn = SqliteManager.getConnection();
//					stmt = conn.createStatement();
					
					System.out.println(offset + 1 + "/" + totalNum);
					
					conn = SqliteManager.getConnection();
					try {
						PreparedStatement ps = conn.prepareStatement("INSERT INTO " + afcfDataTotal + " (cfSig,cf1Sig,candiSig,changed,f1,f2,f3,f4,f5,f6,f7,f8,f9,f10) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
						ps.setString(1, afSigReal);
						ps.setString(2, cf1SigReal);
						ps.setString(3, candiSigReal);
						ps.setString(4, classify);
						ps.setInt(5, f1);
						ps.setInt(6, f2);
						ps.setString(7, f3);
						ps.setInt(8, f4);
						ps.setString(9, f5);
						ps.setInt(10, f6);
						ps.setString(11, f7);
						ps.setInt(12, f8);
						ps.setInt(13, f9);
						ps.setInt(14,f10);
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
	
	public static void mainOthersAll(String[] args) throws SQLException {
		// TODO Auto-generated method stub
		String repoPath = "/Users/zijianjiang/test-repos";
		File repos = new File(repoPath);
		String[] directories = repos.list(new FilenameFilter() {
		  @Override
		  public boolean accept(File current, String name) {
		    return new File(current, name).isDirectory();
		  }
		});
		for(String folder : directories) {
			System.out.println(folder);
//			if(!folder.matches("webpack1|serverless|react|Ghost|habitica")) continue;
			if(!folder.matches("serverless|meteor|Ghost|habitica|pdf|webpack1|react")) continue;
			if (!folder.equals("webpack1")) continue;
			wholeData = "wholeCommit_" + folder;
//			avcfData = "avcfData3_" + folder;
//			cfcfData = "cfcfData2_" + folder;
			wholeNew = "wholeNew_" + folder;
			wholeTotal = "wholeTotal_" + folder;
//			avcfDataT = "avcfDataTV2_" + folder;
//			cfcfDataT = "cfcfDataTV2_" + folder;
			Connection conn = SqliteManager.getConnection();
			Statement stmt = conn.createStatement();
//			if (executeFromFirstBug){
//				stmt.executeUpdate("DROP TABLE IF EXISTS " + afcfDataT);
//			}
//			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + afcfDataT + "(f1 INTEGER,f2 INTEGER,f3 TEXT,f4 INTEGER,f5 TEXT,f6 INTEGER,f7 TEXT,f8 INTEGER,class TEXT,f9 INTEGER,f10 INTEGER)");
			
//			if (executeFromFirstBug){
//				stmt.executeUpdate("DROP TABLE IF EXISTS " + cfcfDataT);
//			}
//			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + cfcfDataT + "(f1 INTEGER,f2 INTEGER,f3 TEXT,f4 INTEGER,f5 TEXT,f6 INTEGER,f7 TEXT,f8 INTEGER,class TEXT,f9 INTEGER,f10 INTEGER)");
////			
			if (executeFromFirstBug){
				stmt.executeUpdate("DROP TABLE IF EXISTS " + wholeNew);
			}
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + wholeNew + "(cfSig TEXT, cf1Sig TEXT, candiSig TEXT, changed TEXT, f1 INTEGER,f2 INTEGER,f3 TEXT,f4 INTEGER,f5 TEXT,f6 INTEGER, f7 TEXT,f8 INTEGER,f9 INTEGER,f10 INTEGER)");
////			
			if (executeFromFirstBug){
				stmt.executeUpdate("DROP TABLE IF EXISTS " + wholeTotal);
			}
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + wholeTotal + "(cfSig TEXT, cf1Sig TEXT, candiSig TEXT, changed TEXT, f1 INTEGER,f2 INTEGER,f3 TEXT,f4 INTEGER,f5 TEXT,f6 INTEGER, f7 TEXT,f8 INTEGER,f9 INTEGER,f10 INTEGER)");
			ResultSet rs = stmt.executeQuery("SELECT count(*) FROM " + wholeData);
			rs.next();
			int totalNum = rs.getInt(1);
			System.out.println(totalNum);
			
			int[] index = new int[totalNum];
			for (int i = 0; i < totalNum; i++) {
				index[i] = i;
			}
			
//			shuffle(index, (int)(totalNum*fraction));
//			shuffle(index, (int)(totalNum*fraction));
//			shuffle(index, (int)(totalNum*fraction));
			
			
			stmt.close();
			conn.close();
			shuffle(index, (int)(totalNum*fraction));
//			
//			for (int offset = 0; offset < totalNum; offset++) {
//				System.out.println(offset);
//				conn = SqliteManager.getConnection();
//				stmt = conn.createStatement();
//				rs = stmt.executeQuery("SELECT * FROM " + wholeData + " LIMIT 1 OFFSET " + offset);
//				String result = rs.getString(4);
//				map.put(offset, result);
//				rs.close();
//				stmt.close();
//				conn.close();
//			}
//			
			boolean av = false;
			if(av){
				for (int offset = 0; offset < totalNum; offset++) {
					conn = SqliteManager.getConnection();
					stmt = conn.createStatement();
					
					rs = stmt.executeQuery("SELECT * FROM " + afcfData + " LIMIT 1 OFFSET " + index[offset]);
					int f1 = rs.getInt(5), f2 = rs.getInt(6), f4 = rs.getInt(8),
							 f7 = rs.getInt(11), f8 = rs.getInt(12), f9 = rs.getInt(13);
					String f3 = rs.getString(7), classify = rs.getString(4), f5 = rs.getString(9), f6 = rs.getString(10);
					String afSigReal = rs.getString(1), cf1SigReal = rs.getString(2), candiSigReal = rs.getString(3);
					rs.close();
					
					rs = stmt.executeQuery("SELECT * FROM " + afcfData + " LIMIT 1 OFFSET " + offset);
					String afSig = rs.getString(1), cf1Sig = rs.getString(2), candiSig = rs.getString(3);
					rs.close();
				
					
					stmt.close();
					conn.close();
					System.out.println(offset + 1 + "/" + totalNum);
					
					conn = SqliteManager.getConnection();
					try {
						PreparedStatement ps = conn.prepareStatement("INSERT INTO " + afcfDataT + " (cfSig,cf1Sig,candiSig,changed,f1,f2,f3,f4,f5,f6,f7,f8,f9) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)");
						ps.setString(1, afSig);
						ps.setString(2, cf1Sig);
						ps.setString(3, candiSig);
						ps.setString(4, classify);
						ps.setInt(5, f1);
						ps.setInt(6, f2);
						ps.setString(7, f3);
						ps.setInt(8, f4);
						ps.setString(9, f5);
						ps.setString(10, f6);
						ps.setInt(11, f7);
						ps.setInt(12, f8);
						ps.setInt(13, f9);
						ps.executeUpdate();
						ps.close();
						conn.close();
					} catch (SQLException e) {
						e.printStackTrace();
						System.exit(-1);
					}
					
//					conn = SqliteManager.getConnection();
//					stmt = conn.createStatement();
					
					System.out.println(offset + 1 + "/" + totalNum);
					
					conn = SqliteManager.getConnection();
					try {
						PreparedStatement ps = conn.prepareStatement("INSERT INTO " + afcfDataTotal + " (cfSig,cf1Sig,candiSig,changed,f1,f2,f3,f4,f5,f6,f7,f8,f9) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)");
						ps.setString(1, afSigReal);
						ps.setString(2, cf1SigReal);
						ps.setString(3, candiSigReal);
						ps.setString(4, classify);
						ps.setInt(5, f1);
						ps.setInt(6, f2);
						ps.setString(7, f3);
						ps.setInt(8, f4);
						ps.setString(9, f5);
						ps.setString(10, f6);
						ps.setInt(11, f7);
						ps.setInt(12, f8);
						ps.setInt(13, f9);
						ps.executeUpdate();
						ps.close();
						conn.close();
					} catch (SQLException e) {
						e.printStackTrace();
						System.exit(-1);
					}
				}
			}
			
			else {
				for (int offset = 0; offset < totalNum; offset++) {
					conn = SqliteManager.getConnection();
					stmt = conn.createStatement();
					
					rs = stmt.executeQuery("SELECT * FROM " + wholeData + " LIMIT 1 OFFSET " + index[offset]);
					int f1 = rs.getInt(5), f2 = rs.getInt(6), f4 = rs.getInt(8),
							 f6 = rs.getInt(10), f8 = rs.getInt(12), f9 = rs.getInt(13),f10 = rs.getInt(14);
					String f3 = rs.getString(7), classify = rs.getString(4), f5 = rs.getString(9), f7 = rs.getString(11);
					String afSigReal = rs.getString(1), cf1SigReal = rs.getString(2), candiSigReal = rs.getString(3);
					rs.close();
					
					rs = stmt.executeQuery("SELECT * FROM " + wholeData + " LIMIT 1 OFFSET " + offset);
					String afSig = rs.getString(1), cf1Sig = rs.getString(2), candiSig = rs.getString(3);
					rs.close();
				
					
					stmt.close();
					conn.close();
					System.out.println(offset + 1 + "/" + totalNum);
					
					conn = SqliteManager.getConnection();
					try {
						PreparedStatement ps = conn.prepareStatement("INSERT INTO " + wholeNew + " (cfSig,cf1Sig,candiSig,changed,f1,f2,f3,f4,f5,f6,f7,f8,f9,f10) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
						ps.setString(1, afSig);
						ps.setString(2, cf1Sig);
						ps.setString(3, candiSig);
						ps.setString(4, classify);
						ps.setInt(5, f1);
						ps.setInt(6, f2);
						ps.setString(7, f3);
						ps.setInt(8, f4);
						ps.setString(9, f5);
						ps.setInt(10, f6);
						ps.setString(11, f7);
						ps.setInt(12, f8);
						ps.setInt(13, f9);
						ps.setInt(14,f10);
						ps.executeUpdate();
						ps.close();
						conn.close();
					} catch (SQLException e) {
						e.printStackTrace();
						System.exit(-1);
					}
					
					
					System.out.println(offset + 1 + "/" + totalNum);
					
					conn = SqliteManager.getConnection();
					try {
						PreparedStatement ps = conn.prepareStatement("INSERT INTO " + wholeTotal + " (cfSig,cf1Sig,candiSig,changed,f1,f2,f3,f4,f5,f6,f7,f8,f9,f10) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
						ps.setString(1, afSigReal);
						ps.setString(2, cf1SigReal);
						ps.setString(3, candiSigReal);
						ps.setString(4, classify);
						ps.setInt(5, f1);
						ps.setInt(6, f2);
						ps.setString(7, f3);
						ps.setInt(8, f4);
						ps.setString(9, f5);
						ps.setInt(10, f6);
						ps.setString(11, f7);
						ps.setInt(12, f8);
						ps.setInt(13, f9);
						ps.setInt(14,f10);
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
	
	public static void shuffle(int[] arr, int n) {
		Random r = new Random(); 
        
        // Start from the last element and swap one by one. We don't 
        // need to run for the first element that's why i > 0 
        for (int i = n-1; i > 0; i--) { 
              
            // Pick a random index from 0 to i 
            int j = r.nextInt(i+1); 
              
            // Swap arr[i] with the element at random index 
            int temp = arr[i]; 
            arr[i] = arr[j]; 
            arr[j] = temp; 
        } 
	}
	
	public static void shuffle2(int[] arr, int n) {
		int m = n / 2;
		for (int i = 0; i < m; i += 2) {
			int tmp = arr[i];
			arr[i] = arr[m + i - 1];
			arr[m + i - 1] = tmp;
		}
	}
	
	public static void stratify(int totalNum, int[] index) {
      int i = 0;
      for (int j = 0; j < totalNum; j++) {
//    	  System.out.println(map.get(index[j]));
    	  if (map.get(index[j]).equals("true")) {
    		  System.out.println("true");
    		  int temp = index[i];
        	  index[i] = index[j];
        	  index[j] = temp;
        	  i++;
    	  }
      }
	  }
	
	public static int[] stratStep(int[] index) {
		int[] res = new int[index.length];
		int i = 0;
		for(int fold = 0; fold < 10; fold++) {
			int start = 0;
			while (start < index.length) {
				if (i < index.length && (start + fold < index.length)) {
					res[i++] = index[start + fold];
				}
				start += 10;
			}
		}
		return res;
	  }


}
