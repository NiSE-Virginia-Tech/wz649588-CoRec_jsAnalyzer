package vt.edu.graph;

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import vt.edu.sql.SqliteManager;

public class LMatchTopPattern {
private String project;
	
	private String picFolder;
	
	private static String outputFolder; 
	
	public LMatchTopPattern(String project) {
		this.project = project;
		picFolder = "/Users/zijianjiang/Documents/NaM/characterization/patterns3/largestPatternPic_revision_" + project;
		outputFolder = "/Users/zijianjiang/Documents/patterns/toppattern3/revision" + project;
	}

	public void execute() {
		initFolder(outputFolder);
		String matchWithId = "em_largest_match_with_pattern_final_revision_" + project;
		String matchCollapsed = "em_largest_match_collapsednotest_revision_" + project;
		String sql = String.format(
				"SELECT collapsed_id, count(*) FROM %s LEFT JOIN %s ON %s.pattern_id=%s.pattern_id"
				+ " GROUP BY collapsed_id ORDER BY count(*) DESC",
				matchWithId, matchCollapsed, matchWithId, matchCollapsed);
		Connection conn = SqliteManager.getConnection();
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			int index = 1;
			while (rs.next() && index <= 10) {
				String id = rs.getString(1);
				int count = rs.getInt(2);
				System.out.println(id + "," + count);
				String extractedFileName = id + ".png";
				Path sourcePath = FileSystems.getDefault().getPath(picFolder, extractedFileName);
				String outputFileName = project + "-" + index + ".png";
				Path targetPath = FileSystems.getDefault().getPath(outputFolder, outputFileName);
				try {
					Files.copy(sourcePath, targetPath);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				index++;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void initFolder(String folder) {
		Path path = Paths.get(folder);
		if (Files.exists(path)) {
			// empty the folder
			try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
			    for (Path file: stream) {
			    	Files.delete(file);
			    }
			} catch (IOException | DirectoryIteratorException x) {
			    x.printStackTrace();
			}
		} else {
			// create the folder
			try {
				Files.createDirectory(path);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		
//		initFolder(outputFolder);
		
//		String[] projects = {"webpack", "electron", "habitica", "meteor", "Ghost", "serverless", "nodejs", "phaser", "storybook", "three"};
		String[] projects = {"webpack1", "electron", "habitica", "meteor", "Ghost", "serverless", "node", "react", "storybook", "pdf"};
//		String[] projects = {"atom", "webpack1", "electron", "node", "Ghost", "storybook", "pdf", "meteor", "react", "habitica", "serverless"};
		for (String project: projects) {
			System.out.println(project);
			if (!project.equals("node")) continue;
			LMatchTopPattern summary = new LMatchTopPattern(project);
			summary.execute();
		}
	}
}
