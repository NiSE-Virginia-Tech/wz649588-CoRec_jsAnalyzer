package vt.edu.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;

public class UnZipTgz {
	private static final int BUFFER_SIZE = 1000;
	public static void main(String[] args) {
		String dir = "/Users/zijianjiang/Documents/R2C/atom/";
		File dirFile = new File(dir);
		for(String file: dirFile.list()){
			if(!file.endsWith(".tgz")) continue;
			System.out.println(dir + file);
		    unzipFile(dir + file);
		}
	}
	private static void unzipFile(String filename) {
		int mark = filename.lastIndexOf("/");
		String dirname = filename.substring(0, mark);
		String shortname = filename.substring(mark+1);
		String version = shortname.substring(0, shortname.length() - 4);
		dirname = dirname + "/" + version;
		
		File dir = new File(dirname);
		if(!dir.exists()) {
			unTgzFile(filename, dirname);
		}
	}
	
	public static void createDirectory(String outputDir,String subDir){  
	       File file = new File(outputDir);  
	       if(!(subDir == null || subDir.trim().equals(""))){
	           file = new File(outputDir + File.separator + subDir);  
	       }  
	       if(!file.exists()){  
	           file.mkdirs();  
	       }  
	   }  
	
	private static void unTgzFile(String filename, String dirname) {
	       try {
	    	  File tarFile = new File(filename);
	   		  GzipCompressorInputStream inputStream = new GzipCompressorInputStream(new FileInputStream(tarFile));
	   		  TarArchiveInputStream tarIn = new TarArchiveInputStream(inputStream, BUFFER_SIZE);
	          TarArchiveEntry entry = null;
	           while ((entry = tarIn.getNextTarEntry()) != null) {

	               if (entry.isDirectory()) {
	                   createDirectory(dirname, entry.getName());  
	               } else {
	                   File tmpFile = new File(dirname + File.separator + entry.getName());
	                   createDirectory(tmpFile.getParent() + File.separator, null);
	                   OutputStream out = null;                   
	                   out = new FileOutputStream(tmpFile);
	                   int length = 0;
	                   byte[] b = new byte[2048];
	                   while ((length = tarIn.read(b)) != -1) {
	                       out.write(b, 0, length);
	                   }
	                   IOUtils.closeQuietly(out);
	               }
	           }
	           IOUtils.closeQuietly(tarIn);
	       } catch (Exception e) {
	           e.printStackTrace();
	       } 
		}

}
