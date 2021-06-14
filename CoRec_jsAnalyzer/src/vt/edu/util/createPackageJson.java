package vt.edu.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class createPackageJson {
	public static void main(String[] args) throws IOException {
		String repoPath = "/Users/zijianjiang/test-repos/meteor/meteor-commits";
		File repos = new File(repoPath);
		String[] directories = repos.list(new FilenameFilter() {
		  @Override
		  public boolean accept(File current, String name) {
		    return new File(current, name).isDirectory();
		  }
		});
		for(String folder : directories) {
			File deleted = new File(repoPath + "/" + folder + "/" + "package.json");
			deleted.delete();
			
			copyFileUsingStream(new File("/Users/zijianjiang/Documents/R2C/package.json"), new File(
					repoPath + "/" + folder + "/" + "package.json"));
//			String command1 = "/usr/local/bin/node " + repoPath + "/" + folder + "/" + "dev-bundle-tool-package.js";
//			String command2 = "/usr/local/bin/node " + repoPath + "/" + folder + "/" + "dev-bundle-server-package.js";
			String packageFile = repoPath + "/" + folder + "/" + "dev-bundle-tool-package.js";
			String packageFile2 = repoPath + "/" + folder + "/" + "dev-bundle-server-package.js";
			File shFile = new File(repoPath + "/" + folder + "/" + "generate-dev-bundle.sh");
//			StringBuilder sb = new StringBuilder();
//
//			Process proc = null;
//			proc = Runtime.getRuntime().exec(command1);
//			
//			
			File packageJson = new File(packageFile);
//			FileOutputStream fOS = new FileOutputStream(packageJson);
//			BufferedWriter bwOld = new BufferedWriter(new OutputStreamWriter(fOS));
//			String fileContents;
//			BufferedReader brOld = new BufferedReader(new InputStreamReader(proc.getInputStream()));
//			while((fileContents = brOld.readLine()) != null) {
//				bwOld.write(fileContents + "\n");
//			}
//			bwOld.close();
//			brOld.close();
//			
//			Process proc2 = null;
//			proc2 = Runtime.getRuntime().exec(command2);
//			
//			
			File packageJson2 = new File(packageFile2);
			shFile.delete();
			packageJson.delete();
			packageJson2.delete();
//			FileOutputStream fOS2 = new FileOutputStream(packageJson2);
//			BufferedWriter bwOld2 = new BufferedWriter(new OutputStreamWriter(fOS2));
//			String fileContents2;
//			BufferedReader brOld2 = new BufferedReader(new InputStreamReader(proc2.getInputStream()));
//			while((fileContents2 = brOld2.readLine()) != null) {
//				bwOld2.write(fileContents2 + "\n");
//			}
//			bwOld2.close();
//			brOld2.close();
		}
		
		
	}
	
	private static void copyFileUsingStream(File source, File dest) throws IOException {
	    InputStream is = null;
	    OutputStream os = null;
	    try {
	        is = new FileInputStream(source);
	        os = new FileOutputStream(dest);
	        byte[] buffer = new byte[1024];
	        int length;
	        while ((length = is.read(buffer)) > 0) {
	            os.write(buffer, 0, length);
	        }
	    } finally {
	        is.close();
	        os.close();
	    }
	}
}
