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
public class DataTransformerNode {
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
			if(!folder.matches("node")) continue;
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
//			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + afcfDataT + "(f1 INTEGER,f2 INTEGER,f3 TEXT,f4 INTEGER,f5 INTEGER,f6 INTEGER,class TEXT)");
//			
//			if (executeFromFirstBug){
//				stmt.executeUpdate("DROP TABLE IF EXISTS " + cfcfDataT);
//			}
//			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + cfcfDataT + "(f1 INTEGER,f2 INTEGER,f3 TEXT,f4 INTEGER,f5 INTEGER,f6 INTEGER,class TEXT)");
//			
			if (executeFromFirstBug){
				stmt.executeUpdate("DROP TABLE IF EXISTS " + avcfDataT);
			}
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + avcfDataT + "(f1 INTEGER,f2 INTEGER,f3 TEXT,f4 INTEGER,f5 INTEGER,f6 INTEGER,class TEXT)");
//			
			ResultSet rs = stmt.executeQuery("SELECT count(*) FROM " + avcfData);
			rs.next();
			int totalNum = rs.getInt(1);
			System.out.println(totalNum);
			
			stmt.close();
			conn.close();
			for (int offset = 0; offset < totalNum; offset++) {
				conn = SqliteManager.getConnection();
				stmt = conn.createStatement();
				
				rs = stmt.executeQuery("SELECT * FROM " + avcfData + " LIMIT 1 OFFSET " + offset);
				int f1 = rs.getInt(5), f2 = rs.getInt(6), f4 = rs.getInt(8),
						 f5 = rs.getInt(9), f6 = rs.getInt(10);
				String classify = rs.getString(4), f3 = rs.getString(7);
				rs.close();
				
				stmt.close();
				conn.close();
				System.out.println(offset + 1 + "/" + totalNum);
				
				conn = SqliteManager.getConnection();
				try {
					
					
					PreparedStatement ps = conn.prepareStatement("INSERT INTO " + avcfDataT + " (f1,f2,f3,f4,f5,f6,class) VALUES (?,?,?,?,?,?,?)");
					ps.setInt(1, f1);
					ps.setInt(2, f2);
					ps.setString(3, f3);
					ps.setInt(4, f4);
					ps.setInt(5, f5);
					ps.setInt(6, f6);
					ps.setString(7, classify);
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
