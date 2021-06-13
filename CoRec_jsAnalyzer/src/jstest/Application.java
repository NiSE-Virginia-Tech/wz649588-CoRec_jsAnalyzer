package jstest;

import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

import vt.edu.sql.SqliteManager;

public class Application implements IApplication{
	public String repoPath = "/Users/zijianjiang/test-repos";
	public static String editScriptTable;
	boolean executeFromFirstBug = true;

	public Object start(IApplicationContext arg0) throws Exception {
		
		File repos = new File(repoPath);
		String[] directories = repos.list(new FilenameFilter() {
		  @Override
		  public boolean accept(File current, String name) {
		    return new File(current, name).isDirectory();
		  }
		});
		for(String folder : directories) {
			System.out.println(folder);
			String commitsFolder = repoPath + "/" + folder + "/" + folder + "-commits";
			
			String commitTable = "classify_commits_" + folder;
			editScriptTable = "classify_graph_" + folder;
			String checkedBugTable = "classify_commits_" + folder + "_checked";
			Connection conn = SqliteManager.getConnection();
			Statement stmt = conn.createStatement();
			
			if (this.executeFromFirstBug) {
				stmt.executeUpdate("DROP TABLE IF EXISTS " + editScriptTable);
				stmt.executeUpdate("DROP TABLE IF EXISTS " + checkedBugTable);
				stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + editScriptTable
						+ " (bug_name TEXT, graph_num INTEGER,"
						+ "graph_data TEXT)");
				stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + checkedBugTable + " (bug_name TEXT)");
				ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + commitTable);
				rs.next();
				int totalNum = rs.getInt(1);
				rs.close();
				
				stmt.close();
				conn.close();
				
				for (int offset = 0; offset < totalNum; offset++) {
					conn = SqliteManager.getConnection();
					stmt = conn.createStatement();
					
					rs = stmt.executeQuery("SELECT bug_name FROM " + commitTable + " LIMIT 1 OFFSET " + offset);
					rs.next();
					String bugName = rs.getString(1);
					rs.close();
					
					rs = stmt.executeQuery("SELECT COUNT(*) FROM " + checkedBugTable + " WHERE bug_name='" + bugName + "'");
					rs.next();
					boolean bugIsChecked = false;
					if (rs.getInt(1) > 0)
						bugIsChecked = true;
					rs.close();
					
					stmt.close();
					conn.close();
					
					if (bugIsChecked)
						continue;
					
					System.out.println(offset + 1 + "/" + totalNum);
					System.out.println(bugName);
					
					processBug(folder, commitsFolder, bugName);
					conn = SqliteManager.getConnection();
					stmt = conn.createStatement();
					stmt.executeUpdate(String.format("INSERT INTO %s (bug_name) VALUES (\"%s\")", checkedBugTable, bugName));
					stmt.close();
					conn.close();
				}
			}	
		}
		return null;
	}
	
	public void processBug(String folder, String commitsFolder, String bugName) throws Exception {
		ChangeExtracterClient changeClient = new ChangeExtracterClient();
		String commitPath = commitsFolder + "/" + bugName;
//		if(checkFileSizeFail(commitPath)) continue;
		
		List<ChangeFact> cfList = changeClient.parseChanges(commitPath);
		ConnectChanges connector = new ConnectChanges();
		connector.groupChanges(cfList, bugName, new ArrayList<TraverseJsonTyped>());
	}
	
	
	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}
}
