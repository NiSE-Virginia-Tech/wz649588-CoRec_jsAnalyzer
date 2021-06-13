package vt.edu.util;

/******
 * This is to get the typed-ast-json file and put them into the same folder as the esprima ast
 */

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import com.google.common.io.Files;

public class ExtractTypedAstFiles {
	
	public static void main(String[] args) throws IOException {
//		testMoveFile("/Users/zijianjiang/Documents/Results/testMove.js", "/Users/zijianjiang/Documents/testMove1.js");
		String projectName = "webpack";
		String projectCommits = "/Users/zijianjiang/Documents/R2C/" + projectName + "_commits";
		File commitsFolder = new File(projectCommits);
		String[] commitDirectory = commitsFolder.list(new FilenameFilter() {
			  @Override
			  public boolean accept(File current, String name) {
			    return new File(current, name).isDirectory();
			  }
			});
		for (String commit : commitDirectory) {
			System.out.println(commit);
			if(!commit.equals("test")) continue;
			String sourceFromFolder = projectCommits + "/" + commit + "/from";
			String sourceToFolder = projectCommits + "/" + commit + "/to";
			String targetFromFolder = "/Users/zijianjiang/test-repos/" + projectName + "/" + projectName
					+ "-commits/" + commit + "/from";
			String targetToFolder = "/Users/zijianjiang/test-repos/" + projectName + "/" + projectName
					+ "-commits/" + commit + "/to";
			moveFiles(sourceFromFolder, targetFromFolder);
			moveFiles(sourceToFolder, targetToFolder);
		}
	}
	
	public static void moveFiles(String sourceDir, String targetDir) throws IOException {
//		remove to dir (targetDir) from its sourceDir
		File dir = new File(targetDir);
		for (String file : dir.list()){
			if (!file.endsWith(".js")) continue;
			String path = file.substring(0, file.length() - 3).replace("++", "/") + ".js.ast.json";
			String sourceFilePath = sourceDir + "/" + path;
			File sourceFile = new File(sourceFilePath);
			String targetFilePath = targetDir + "/" + file + ".ast.json";
			
			String command = "/usr/local/bin/node /Users/zijianjiang/Documents/esprima/jsonStr.js " + 
					sourceFilePath + " " + targetFilePath;
			Process proc = Runtime.getRuntime().exec(command);
			try {
				proc.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
//			if (sourceFile.renameTo(new File(targetFilePath))) {
//				sourceFile.delete();
//				System.out.println("File moved successfully");
//			}
//			else {
//				System.out.println("Failed to move the file");
//			}
			
		}
	}
	
	
//	for a test
	public static void testMoveFile(String sourceDir, String targetDir) {
		File sourceFile = new File(sourceDir);
		if (sourceFile.renameTo(new File(targetDir))) {
			sourceFile.delete();
			System.out.println("Yes");
		}
		else {
			System.out.println("no");
		}
	}
}

