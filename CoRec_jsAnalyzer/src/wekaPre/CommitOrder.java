package wekaPre;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import vt.edu.sql.SqliteManager;

/**
 * To consider the temporal order of the 
 * @author Ye Wang
 * @since 07/08/2018
 *
 */
public class CommitOrder {
	
//	private static String command = "git --git-dir=/Users/Vito/git/aries/.git show -s --format=%ct 13e8e0f";
	
	private String project;
	
	private String gitRepo;
	
	public CommitOrder(String project) {
		this.project = project;
		this.gitRepo = getGitRepoPath(project);
	}
	
	private final static String getGitRepoPath(String project) {
		return "/Users/zijianjiang/test-repos/" + project + "/.git";
	}
	
	private int getTimestamp(String commitHash) {
		String commandTemplate = "git --git-dir=%s show -s --format=%%ct %s";
		String command = String.format(commandTemplate, gitRepo, commitHash);
		Process p = null;
		try {
			p = Runtime.getRuntime().exec(command);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			p.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(
                p.getInputStream()));
        String output = null;
		try {
			output = stdInput.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int timestamp = -1;
		try {
			timestamp = Integer.parseInt(output);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
        return timestamp;
	}
	
	public void exec() throws SQLException {
		
		Connection conn = SqliteManager.getConnection();
		Statement stmt = conn.createStatement();
		String outputTable = "commit_order_" + project;
		
		stmt.executeUpdate("DROP TABLE IF EXISTS " + outputTable);
		stmt.executeUpdate("CREATE TABLE " + outputTable + " (bug_name TEXT, ordering INTEGER)");
		
		List<String> commitNames = new ArrayList<>();
		String repoPath = "/Users/zijianjiang/test-repos";
		String commitsFolder = repoPath + "/" + project + "/" + project + "-commits";
		File commitsFile = new File(commitsFolder);
		String[] commitDirectory = commitsFile.list(new FilenameFilter() {
		  @Override
		  public boolean accept(File current, String name) {
		    return new File(current, name).isDirectory();
		  }
		});
		
		for(int i = 0; i < commitDirectory.length; i++) {
			String commit = commitDirectory[i];
			if (project.equals("node")) {
				commitNames.add(commit);
			}
			else {
				File versionFile = new File(commitsFolder + "/" + commit);
				String[] commitDirectoryIn = versionFile.list(new FilenameFilter() {
					  @Override
					  public boolean accept(File current, String name) {
					    return new File(current, name).isDirectory();
					  }
					});
				
				
				for(String commitIn : commitDirectoryIn) {
					commitNames.add(commitIn);
				}
			}
		}
		stmt.close();
		
		PreparedStatement ps = conn.prepareStatement("INSERT INTO "
				+ outputTable + " (bug_name,ordering) VALUES (?,?)");
		for (String commitName: commitNames) {
			int end = 0;
			while (end < commitName.length() && commitName.charAt(end) != '_')
				end++;
			String hash = commitName.substring(0, end);
			int order = getTimestamp(hash);
			System.out.println(commitName + ", " + order);
			if (order != -1) {
				ps.setString(1, commitName);
				ps.setInt(2, order);
				ps.executeUpdate();
			}
		}
		ps.close();
		
		
	}
	

	public static void main(String[] args) throws SQLException {
//		String[] projects = new String[]{"pdf","meteor","habitica","node","electron","serverless","webpack1","react","Ghost"};
		String[] projects = new String[]{"storybook"};
		for (String project: projects) {
			CommitOrder order = new CommitOrder(project);
			order.exec();
		}
	}
	
	public static void main1(String[] args) throws IOException {
		// This is a test
		String command = "git --git-dir=/Users/Vito/git/aries/.git show -s --format=%ct 0084a90";
		Process p = Runtime.getRuntime().exec(command);
		try {
			p.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(
                p.getInputStream()));
        String output = stdInput.readLine();
        int timestamp = Integer.parseInt(output);
        System.out.println(timestamp);
        
	}

}
