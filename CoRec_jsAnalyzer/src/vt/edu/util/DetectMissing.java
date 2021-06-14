package vt.edu.util;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;

public class DetectMissing {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String commitFolder = "/Users/zijianjiang/test-repos/Ghost/Ghost-commits";
		String tgzFolder = "/Users/zijianjiang/Documents/R2C/Ghost";
		File commitFile = new File(commitFolder);
		File tgzFile = new File(tgzFolder);
		HashSet<String> tgzSet = new HashSet<String>(Arrays.asList(tgzFile.list()));
		HashSet<String> commitSet = new HashSet<String>(Arrays.asList(commitFile.list()));
		for (String version : commitSet) {
			
			if (!tgzSet.contains(version + ".tgz")) System.out.println(version);
			
		}
		
	}

}
