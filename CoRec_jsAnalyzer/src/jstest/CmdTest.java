package jstest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CmdTest {
	String gitDir;
	
	public CmdTest(String gitDir){
		this.gitDir = gitDir;
	}
	public List<String> getCommits() throws IOException{
//		/usr/local/bin
		String command = "git --git-dir=" + gitDir + " rev-list --all";
//		String command = "git rev-list -all";
		Process proc = null;
		List<String> commits = new ArrayList<String>();
		try{
			proc = Runtime.getRuntime().exec(command);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			proc.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(
                proc.getInputStream()));
        String output = null;
        try {
			while((output = stdInput.readLine()) != null) {
				System.out.println(output);
				commits.add(output);
			};
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return commits;
       
	}
	
	
	
	
	
	public void exe(String command){
		Process proc = null;
		try{
			proc = Runtime.getRuntime().exec(command);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			proc.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
//		BufferedReader stdInput = new BufferedReader(new InputStreamReader(
//                proc.getInputStream()));
//        String output = null;
//        try {
//			output = stdInput.readLine();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//        return output;
	}
	
	public void cmdForRemoveLocs(String fileName) throws IOException{
		String command = "/usr/local/bin/node /Users/zijianjiang/Documents/esprima/removeKey.js " + fileName + " " + "loc";
        exe(command);
	}
	
	public void cmdForR2c() throws IOException{
	}
}
