package jstest;

import java.io.File;

import vt.edu.util.Pair;
import vt.edu.sql.SqliteManager;
import vt.edu.graph.*;

import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;

public class Testchange {
	static boolean executeFromFirstBug = true;
	public static String editScriptTable;
	
	public static int[] inOrOut = new int[3];
	
	/*
	 * if there is a json file missing, this commit should also be deleted
	 */
	
	private static boolean checkFileMissing(String filePath) {
		File from = new File(filePath + "/from");
		File to = new File(filePath + "/to");
		
		HashSet<String> jsFiles= new HashSet<>();
		HashSet<String> jsonFiles = new HashSet<>();
		
		for(File file : from.listFiles()) {
			String fileName = file.getName();
			System.out.println(fileName);
			String[] combNames = fileName.split("\\.");
			if(combNames[1].equals("js")) jsFiles.add(combNames[0]);
			
			else if(combNames[1].equals("json")) jsonFiles.add(combNames[0]);
			if(jsFiles.size() > jsonFiles.size()) return true;
		}
		
		jsFiles= new HashSet<>();
		jsonFiles = new HashSet<>();
		
		for(File file : to.listFiles()) {
			String fileName = file.getName();
			String[] combNames = fileName.split("\\.");
			if(combNames[1].equals("js")) jsFiles.add(combNames[0]);
			else if(combNames[1].equals("json")) jsonFiles.add(combNames[0]);
			if(jsFiles.size() > jsonFiles.size()) return true;
		}
		return false;
	}
	
	/*
	 * Here when checking out a file whose size is larger than 50 Mb, I think
	 * I need to pass this commit instead of just that file.
	 */
	private static boolean checkFileSizeFail(String filePath) {
		File from = new File(filePath + "/from");
		File to = new File(filePath + "/to");
		
		for(File file : from.listFiles()) {
//			System.out.println(file.length());
			if(getFileSizeMegaBytes(file) > 50) return true;
		}
		
		for(File file : to.listFiles()) {
//			System.out.println(file.length());
			if(getFileSizeMegaBytes(file) > 50) return true;
		}	
		return false;
	}
	
	private static int getFileSizeMegaBytes(File file) {
		return (int) (file.length() / (1024 * 1024));
	}
	
	public static void main(String[] args) throws Exception {
		
//		String commit = "/Users/zijianjiang/test-repos/jquery/jquery-commits/fea7a2a_jquery_6380";
//		ChangeExtracterClient changeClient = new ChangeExtracterClient();
//		changeClient.parseChanges(commit);
//		Connection conn = SqliteManager.getConnection();
//		Statement stmt = conn.createStatement();
//		
		int[] count = new int[1000];
		int[] typeChangeCount = new int[12];
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
			System.out.println(directories.length);
//			if(inOrOut[1] > 0) break;
			System.out.println(folder);
			editScriptTable = "classify_graphmerge_renew2_" + folder;
			if(!folder.equals("pdf")) continue;
			Connection conn = SqliteManager.getConnection();
			Statement stmt = conn.createStatement();
			if (executeFromFirstBug)
				stmt.executeUpdate("DROP TABLE IF EXISTS " + editScriptTable);
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + editScriptTable
					+ " (bug_name TEXT, graph_num INTEGER,"
					+ "graph_data TEXT)");
			stmt.close();
			conn.close();
			
			int multi = 0;
			int total = 0;
			System.out.println(folder);
			String commitsFolder = repoPath + "/" + folder + "/" + folder + "-commits";
			File commitsFile = new File(commitsFolder);
			String[] commitDirectory = commitsFile.list(new FilenameFilter() {
			  @Override
			  public boolean accept(File current, String name) {
			    return new File(current, name).isDirectory();
			  }
			});
			System.out.println(commitDirectory.length);
			int i = 0;
			for(i = 0; i < commitDirectory.length; i++) {
//				if (i % 3 != 0) continue; //just to reduce the size of the data, commnet when needed
				System.out.println(i + "/" + commitDirectory.length);
				String commit = commitDirectory[i];
//				if(!commit.equals("2c59bf8")) continue;
				System.out.println(commit);
				ChangeExtracterClient2 changeClient = new ChangeExtracterClient2();
				String commitPath = commitsFolder + "/" + commit;
				
//				if(checkFileSizeFail(commitPath)) continue;
//				Need to check if it's correct here
//				if(checkFileMissing(commitPath)) continue;
				
				List<ChangeFactNode> cfList = changeClient.parseChanges(commitPath);
				
				ConnectChanges cC = new ConnectChanges();
				if (cfList == null) continue;
				
				cC.groupChanges(cfList, commit);
				if(cfList == null) continue;
				int changed = 0;
				for(ChangeFactNode cf : cfList){
					changed += cf.getNumOfChanged();
					typeChangeCount[0] += cf.insertedVars.size();
					typeChangeCount[1] += cf.deletedVars.size();
					typeChangeCount[2] += cf.changedVars.size();
					typeChangeCount[3] += cf.insertedFuncs.size();
					typeChangeCount[4] += cf.deletedFuncs.size();
					typeChangeCount[5] += cf.changedFuncs.size();
//					if(cf.changedFuncs.size() > 0){
//						for(Pair<ClientFunc, ClientFunc> p : cf.changedFuncs){
//							System.out.println("changed func name is " + p.fst.funcName);
//						}
//					}
					typeChangeCount[6] += cf.insertedClasses.size();
					typeChangeCount[7] += cf.deletedClasses.size();
					typeChangeCount[8] += cf.changedClasses.size();
					typeChangeCount[9] += cf.insertedExps.size();
					typeChangeCount[10] += cf.deletedExps.size();
					typeChangeCount[11] += cf.changedExps.size();
				}
				
//				if(changed > 5) {
//					System.out.println(commit + " num " + changed);
//					break;
//				}
				if(changed < 1000) count[changed]++;
				if(changed > 1) multi++;
				if(changed >= 1) total++;
				System.out.println(multi + "/" + total);
				for(int k = 0; k < 12; k++){
					System.out.println("changes are" + typeChangeCount[k]);
				}
				System.out.println(inOrOut[0]);
				System.out.println(inOrOut[1]);
				System.out.println(commit);
//				if(commit.equals("2c59bf8")) break;
			}
			for(int j = 0; j < 1000; j++){
				if(count[j] != 0) System.out.println(j + " " + count[j]);
			}
			
			for(int j = 0; j < 12; j++){
				System.out.println(typeChangeCount[j]);
			}
			System.out.println(inOrOut[0]);
			System.out.println(inOrOut[1]);
			System.out.println(inOrOut[2]);
		}
		
	}
}