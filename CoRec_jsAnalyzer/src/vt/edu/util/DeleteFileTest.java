package vt.edu.util;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

public class DeleteFileTest {
	public static void main(String[] args) throws IOException, InterruptedException {
		String repoPath = "/Volumes/repos/three/three-commit/c00575e_three_27103";
		File file = new File(repoPath);
		
		file.delete();
	}
}
