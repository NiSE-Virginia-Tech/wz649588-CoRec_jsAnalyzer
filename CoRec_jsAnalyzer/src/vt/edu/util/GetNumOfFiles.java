package vt.edu.util;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

public class GetNumOfFiles {
	public static void main(String[] args) throws IOException, InterruptedException {
		String repoPath = "/Users/zijianjiang/test-repos/";
//		String repoPath = "/Volumes/TEYADI/meteor/meteor-commits";
		String[] projects = {"meteor", "react","habitica","pdf","serverless","electron"
				,"webpack1","storybook","Ghost","node"};
		for (String project : projects) {
			if (!project.equals("node")) continue;
			String commitPath = repoPath + project + "/" + project + "-commits";
			System.out.println(project);
			int n = 0;
			File folder = new File(commitPath);
			String[] directories = folder.list(new FilenameFilter() {
			  @Override
			  public boolean accept(File current, String name) {
			    return new File(current, name).isDirectory();
			  }
			});
			System.out.println(directories.length);
			
//			
//			for (String version : directories) {
////				System.out.println(version);
//				File versionFolder = new File(commitPath + "/" + version);
//				String[] commits = versionFolder.list(new FilenameFilter() {
//					  @Override
//					  public boolean accept(File current, String name) {
//					    return new File(current, name).isDirectory();
//					  }
//					});
//				n += commits.length;
//			}
//			
//			System.out.println(n);
		}
	}
}
