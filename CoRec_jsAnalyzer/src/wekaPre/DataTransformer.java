package wekaPre;

import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import jstest.ChangeExtracterClient3;
import jstest.ChangeFact;
import jstest.ConnectChanges;
import jstest.TestChange2;
import jstest.TraverseJsonTyped;
import vt.edu.sql.SqliteManager;

/*
 * This class is used to deal with the af(av,cf)_cf data and convert them to arff files
 */
public class DataTransformer {
	private static String avcfData;
	private static String afcfData;
	private static String cfcfData;
	
	private static String avcfDataT;
	private static String afcfDataT;
	private static String cfcfDataT;
	
	private static boolean executeFromFirstBug = true;
	
	public static void main(String[] args) throws Exception {
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
			if(!folder.matches("electron|meteor|serverless|Ghost|habitica|pdf|webpack1|react|storybook")) continue;
//			if (!folder.equals("meteor")) continue;
			afcfData = "afcfData1_" + folder;
			avcfData = "avcfData3_" + folder;
			cfcfData = "cfcfData2_" + folder;
			afcfDataT = "afcfDataTV2_" + folder;
			avcfDataT = "avcfDataTV2_" + folder;
			cfcfDataT = "cfcfDataTV2_" + folder;
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
				stmt.executeUpdate("DROP TABLE IF EXISTS " + avcfDataT);
			}
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + avcfDataT + "(f1 INTEGER,f2 INTEGER,f3 TEXT,f4 INTEGER,f5 TEXT,f6 TEXT,f7 INTEGER,f8 INTEGER,class TEXT,f9 INTEGER)");
////			
			ResultSet rs = stmt.executeQuery("SELECT count(*) FROM " + avcfData);
			rs.next();
			int totalNum = rs.getInt(1);
			System.out.println(totalNum);
			
			stmt.close();
			conn.close();
			boolean av = true;
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
					
					rs = stmt.executeQuery("SELECT * FROM " + afcfData + " LIMIT 1 OFFSET " + offset);
					int f1 = rs.getInt(5), f2 = rs.getInt(6), f4 = rs.getInt(8),
							 f6 = rs.getInt(10), f8 = rs.getInt(12), f9 = rs.getInt(13),f10 = rs.getInt(14);
					String f3 = rs.getString(7), classify = rs.getString(4), f5 = rs.getString(9), f7 = rs.getString(11);
					rs.close();
					
					stmt.close();
					conn.close();
					System.out.println(offset + 1 + "/" + totalNum);
					
					conn = SqliteManager.getConnection();
					try {
						
						
						PreparedStatement ps = conn.prepareStatement("INSERT INTO " + afcfDataT + " (f1,f2,f3,f4,f5,f6,f7,f8,class,f9,f10) VALUES (?,?,?,?,?,?,?,?,?,?,?)");
						ps.setInt(1, f1);
						ps.setInt(2, f2);
						ps.setString(3, f3);
						ps.setInt(4, f4);
						ps.setString(5, f5);
						ps.setString(6, f7);
						ps.setInt(7, f6);
						ps.setInt(8, f8);
						ps.setString(9, classify);
						ps.setInt(10, f9);
						ps.setInt(11,f10);
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
