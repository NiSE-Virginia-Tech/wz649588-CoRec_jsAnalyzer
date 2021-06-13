package vt.edu.util;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

public class jsonGetter {
	public static void main(String[] args) throws IOException {
//		String repoPath = "/Volumes/TEYADI";
		String repoPath = "/Users/zijianjiang/test-repos";
		File repos = new File(repoPath);
		String[] directories = repos.list(new FilenameFilter() {
		  @Override
		  public boolean accept(File current, String name) {
		    return new File(current, name).isDirectory();
		  }
		});
		
		for(String folder : directories) {
			if(!folder.equals("habitica")) continue;
			System.out.println(folder);
			String commitsFolder = repoPath + "/" + folder + "/" + folder + "-commits";
			File commitsFile = new File(commitsFolder);
			String[] commitDirectory = commitsFile.list(new FilenameFilter() {
			  @Override
			  public boolean accept(File current, String name) {
			    return new File(current, name).isDirectory();
			  }
			});
			int i = 0;
			for(String commit : commitDirectory) {
				System.out.println(i++ + "/" + commitDirectory.length);
//				if(i < 2100) continue;
				System.out.println(commit);
				String fromFolder = commitsFolder + "/" + commit + "/from";
				String toFolder = commitsFolder + "/" + commit + "/to";
				File from = new File(fromFolder);
				File to = new File(toFolder);
				for(String fjs : from.list()){
					if(!fjs.endsWith(".js")) continue;
					System.out.println(fjs);
					int len = fjs.length();
					String fjson = fjs.substring(0, len - 2) + "json";
					String f1 = fromFolder + "/" + fjs;
					String f2 = fromFolder + "/" + fjson;
					String command = "/usr/local/bin/node /Users/zijianjiang/Documents/esprima/jsToJson.js " + 
							f1 + " " + f2;
					Process proc = Runtime.getRuntime().exec(command);
					try {
						proc.waitFor();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				for(String tjs : to.list()){
					if(!tjs.endsWith(".js")) continue;
					System.out.println(tjs);
					int len = tjs.length();
					String tjson = tjs.substring(0, len - 2) + "json";
					String f1 = toFolder + "/" + tjs;
					String f2 = toFolder + "/" + tjson;
					String command = "/usr/local/bin/node /Users/zijianjiang/Documents/esprima/jsToJson.js " + 
							f1 + " " + f2;
					Process proc = Runtime.getRuntime().exec(command);
					try {
						proc.waitFor();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
			}
		}
	}
}
