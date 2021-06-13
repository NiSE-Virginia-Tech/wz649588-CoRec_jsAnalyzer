package jstest;
/*
 * To run the mainFromDataSet, one can only get on set of training data once. (Need to be modified)
 */
import java.io.File;

import vt.edu.util.Pair;
import vt.edu.sql.SqliteManager;
import vt.edu.graph.*;

import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import wekaPre.CommitOrder;
import wekaPre.RosePrediction;
import wekaPre.TARMAQPrediction;
import wekaPre.TransARPrediction;


public class TestChange2 {
	static String repoPath = "/Users/zijianjiang/test-repos";
	static boolean executeFromFirstBug = false;
	public static String editScriptTable;
	static boolean goon = false;
	static int amdFiles = 0;
	
	public static int[] inOrOut = new int[3];
	static String afcfTable = null;
	static String avcfTable = null;
	static String cfcfTable = null;
	static String afCommitTable = null;
	static String avCommitTable = null;
	static String cfCommitTable = null;
	static Set<String> afSet = new HashSet<>();
	static Set<String> cfSet = new HashSet<>();
	static Set<String> avSet = new HashSet<>();
	public static String thisVersion;
	static String roseTable = null;
	static String afPredictTable = null;
	
	
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
			if(getFileSizeMegaBytes(file) > 50) return true;
		}
		
		for(File file : to.listFiles()) {
			if(getFileSizeMegaBytes(file) > 50) return true;
		}	
		return false;
	}
	
	private static int getFileSizeMegaBytes(File file) {
		return (int) (file.length() / (1024 * 1024));
	}
	
	
	
	
	public static void main(String[] args) throws Exception {	
		int[] count = new int[1000];
		int[] typeChangeCount = new int[12];
		File repos = new File(repoPath);
		String[] directories = repos.list(new FilenameFilter() {
		  @Override
		  public boolean accept(File current, String name) {
		    return new File(current, name).isDirectory();
		  }
		});
		
		
		for(String folder : directories) {
//			afSet = new HashSet<>();
//			avSet = new HashSet<>();
//			cfSet = new HashSet<>();
//			afCommitTable = "afCommit_" + folder;
//			cfCommitTable = "cfCommit_" + folder;
//			avCommitTable = "avCommit_" + folder;
//			
			editScriptTable = "classify_graphmerge_final_revision_" + folder;
			if(!folder.equals("node")) continue;
//			afcfTable = "afcfData2_" + folder;
//			cfcfTable = "cfcfData2_" + folder;
//			avcfTable = "avcfData2_" + folder;
			Connection conn = SqliteManager.getConnection();
			Statement stmt = conn.createStatement();
			if (executeFromFirstBug)
				stmt.executeUpdate("DROP TABLE IF EXISTS " + editScriptTable);
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + editScriptTable
					+ " (bug_name TEXT, graph_num INTEGER,"
					+ "graph_data TEXT)");
			stmt.close();
			conn.close();
			
//			conn = SqliteManager.getConnection();
//			stmt = conn.createStatement();
//			if (executeFromFirstBug){
//				stmt.executeUpdate("DROP TABLE IF EXISTS " + afCommitTable);
//			}
//			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + afCommitTable + "(bugName TEXT)");
//			stmt.close();
//			conn.close();
//			
//			conn = SqliteManager.getConnection();
//			stmt = conn.createStatement();
//			if (executeFromFirstBug){
//				stmt.executeUpdate("DROP TABLE IF EXISTS " + cfCommitTable);
//			}
//			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + cfCommitTable + "(bugName TEXT)");
//			stmt.close();
//			conn.close();
//			
//			conn = SqliteManager.getConnection();
//			stmt = conn.createStatement();
//			if (executeFromFirstBug){
//				stmt.executeUpdate("DROP TABLE IF EXISTS " + avCommitTable);
//			}
//			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + avCommitTable + "(bugName TEXT)");
//			stmt.close();
//			conn.close();
//			
//			conn = SqliteManager.getConnection();
//			stmt = conn.createStatement();
//			if (executeFromFirstBug){
//				stmt.executeUpdate("DROP TABLE IF EXISTS " + afcfTable);
//			}
//			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + afcfTable + "(afSig TEXT,cf1Sig TEXT,candiSig TEXT,changed INTEGER,f1 INTEGER,f2 INTEGER,f3 INTEGER,f4 INTEGER,f5 INTEGER)");
//			stmt.close();
//			conn.close();
//			
//			conn = SqliteManager.getConnection();
//			stmt = conn.createStatement();
//			if (executeFromFirstBug){
//				stmt.executeUpdate("DROP TABLE IF EXISTS " + cfcfTable);
//			}
//			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + cfcfTable + "(cfSig TEXT,cf1Sig TEXT,candiSig TEXT,changed INTEGER,f1 INTEGER,f2 INTEGER,f3 INTEGER,f4 INTEGER,f5 INTEGER)");
//			stmt.close();
//			conn.close();
//			
//		
//			conn = SqliteManager.getConnection();
//			stmt = conn.createStatement();
//			if (executeFromFirstBug){
//				stmt.executeUpdate("DROP TABLE IF EXISTS " + avcfTable);
//			}
//			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + avcfTable + "(avSig TEXT,cf1Sig TEXT,candiSig TEXT,changed INTEGER,f1 INTEGER,f2 INTEGER,f3 INTEGER,f4 INTEGER,f5 INTEGER)");
//			stmt.close();
//			conn.close();
//			
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
			for(i = 1664; i < commitDirectory.length; i++) {
				System.out.println(i + "/" + commitDirectory.length);
				String commit = commitDirectory[i];
//				if (!commit.equals("8444bba") && !goon) continue;
//				goon = true;
//				if (commit.equals("ef0701d")) continue;
				System.out.println(commit);
				try{
				ChangeExtracterClient2 changeClient = new ChangeExtracterClient2();
				String commitPath = commitsFolder + "/" + commit;
				
//				if(checkFileSizeFail(commitPath)) continue;
//				Need to check if it's correct here
//				if(checkFileMissing(commitPath)) continue;
				
				List<ChangeFactNode> cfList = changeClient.parseChanges(commitPath);
				
				ConnectChanges cC = new ConnectChanges();
				List<TraverseJsonTyped> oldTJ = changeClient.oldTJ;
				if (cfList == null) continue;
				
				cC.groupChanges(cfList, commit, oldTJ);
				int changed = 0;
				for(ChangeFactNode cf : cfList){
					changed += cf.getNumOfChanged();
					typeChangeCount[0] += cf.insertedVars.size();
					typeChangeCount[1] += cf.deletedVars.size();
					typeChangeCount[2] += cf.changedVars.size();
					typeChangeCount[3] += cf.insertedFuncs.size();
					typeChangeCount[4] += cf.deletedFuncs.size();
					typeChangeCount[5] += cf.changedFuncs.size();
					typeChangeCount[6] += cf.insertedClasses.size();
					typeChangeCount[7] += cf.deletedClasses.size();
					typeChangeCount[8] += cf.changedClasses.size();
					typeChangeCount[9] += cf.insertedExps.size();
					typeChangeCount[10] += cf.deletedExps.size();
					typeChangeCount[11] += cf.changedExps.size();
				}
				
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
				}catch (OutOfMemoryError e) {
					e.printStackTrace();
					}
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
//			writeCommitData(afCommitTable, afSet);
//			writeCommitData(cfCommitTable, cfSet);
//			writeCommitData(avCommitTable, avSet);
		}
			
	}
	
	public static void writeCommitData(String table, Set<String> set) {
		for (String bugName : set) {
			try {
				Connection conn = SqliteManager.getConnection();
				PreparedStatement ps = conn.prepareStatement("INSERT INTO " + table + " (bugName) VALUES (?)");
				ps.setString(1, bugName);
				ps.executeUpdate();
				ps.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}
	
	
	public static void mainRoseOthers(String[] args) throws Exception {
		File repos = new File(repoPath);
		String[] directories = repos.list(new FilenameFilter() {
		  @Override
		  public boolean accept(File current, String name) {
		    return new File(current, name).isDirectory();
		  }
		});
		for(String folder : directories) {
			System.out.println(folder);
			
			if(!folder.matches("pdf|serverless|Ghost|habitica|webpack1|react|meteor")) continue;
			//if(!folder.equals("pdf")) continue;
			//afPredictTable = "avPredictRose_" + folder;
			afPredictTable = "avPredictTransAR_" + folder;
			afCommitTable = "avCommit_" + folder;
			TransARPrediction.commitOrderTable = "commit_order_" + folder;
			roseTable = "af_rose_" + folder;
			Connection conn = SqliteManager.getConnection();
			Statement stmt = conn.createStatement();
			if (executeFromFirstBug){
				stmt.executeUpdate("DROP TABLE IF EXISTS " + afPredictTable);
			}
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + afPredictTable
					+ " (bug_name TEXT,af_sig TEXT,used_cf TEXT,real_other_cf TEXT,"
					+ "predicted_cf TEXT,precision INTEGER,recall TEXT, ground_truth_size INTEGER, predicted_size INTEGER, true_positive_size INTEGER)");
			ResultSet rs = stmt.executeQuery("SELECT count(*) FROM " + afCommitTable);
			rs.next();
			int totalNum = rs.getInt(1);
			System.out.println(totalNum);
			
			stmt.close();
			conn.close();
			for (int offset = 0; offset < totalNum; offset++) {
				conn = SqliteManager.getConnection();
				stmt = conn.createStatement();
				rs = stmt.executeQuery("SELECT bugName FROM " + afCommitTable + " LIMIT 1 OFFSET " + offset);
				String bugName = rs.getString(1);
				rs.close();
				
				stmt.close();
				conn.close();
				
				
				System.out.println(offset + 1 + "/" + totalNum);
				System.out.println(bugName);
				
				String[] verBug = bugName.split("\\+\\+");
				String version = verBug[0], commit = verBug[1];
				try {
					thisVersion = version;
					ChangeExtracterClient3 changeClient = new ChangeExtracterClient3(folder, version, commit);
					String commitsFolder = repoPath + "/" + folder + "/" + folder + "-commits";
					String versionFolder = commitsFolder + "/" + version;
					String commitPath = versionFolder + "/" + commit;
	//				if(checkFileSizeFail(commitPath)) continue;
	//					Need to check if it's correct here
	//					if(checkFileMissing(commitPath)) continue;
					
					List<ChangeFact> cfList = changeClient.parseChanges(commitPath);
					List<TraverseJsonTyped> oldTJ = changeClient.oldTJ;
					ConnectChanges cC = new ConnectChanges();
					if (cfList == null) continue;
					System.out.println("The size of cfList is " + cfList.size());
					
					cC.groupChanges(cfList, commit, oldTJ);
				}catch (OutOfMemoryError e) {
					e.printStackTrace();
					}
			}
		}
		
	}
	
	
	public static void mainFromDataOthers(String[] args) throws Exception {
		File repos = new File(repoPath);
		String[] directories = repos.list(new FilenameFilter() {
		  @Override
		  public boolean accept(File current, String name) {
		    return new File(current, name).isDirectory();
		  }
		});
		for(String folder : directories) {
			RosePrediction.commitOrderTable = "commit_order_" + folder;
			roseTable = "af_rose_" + folder;
			System.out.println(folder);
			if(!folder.matches("electron|serverless|Ghost|habitica|pdf|webpack1|react|storybook|meteor")) continue;
//			if (!folder.equals("meteor")) continue;
			afCommitTable = "afCommit_" + folder;
			cfCommitTable = "cfCommit_" + folder;
			avCommitTable = "avCommit_" + folder;
			afcfTable = "afcfData3_" + folder;
			avcfTable = "avcfData3_" + folder;
			cfcfTable = "cfcfData3_" + folder;
			Connection conn = SqliteManager.getConnection();
			Statement stmt = conn.createStatement();
			if (executeFromFirstBug){
				stmt.executeUpdate("DROP TABLE IF EXISTS " + afcfTable);
			}
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + afcfTable + "(afSig TEXT,cf1Sig TEXT,candiSig TEXT,changed TEXT,f1 INTEGER,f2 INTEGER,f3 TEXT,f4 INTEGER,f5 TEXT,f6 INTEGER,f7 TEXT,f8 INTEGER,f9 INTEGER,f10 INTEGER)");
			
			if (executeFromFirstBug){
				stmt.executeUpdate("DROP TABLE IF EXISTS " + cfcfTable);
			}
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + cfcfTable + "(cfSig TEXT,cf1Sig TEXT,candiSig TEXT,changed TEXT,f1 INTEGER,f2 INTEGER,f3 TEXT,f4 INTEGER,f5 TEXT,f6 INTEGER,f7 TEXT,f8 INTEGER,f9 INTEGER,f10 INTEGER)");
			
			if (executeFromFirstBug){
				stmt.executeUpdate("DROP TABLE IF EXISTS " + avcfTable);
			}
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + avcfTable + "(avSig TEXT,cf1Sig TEXT,candiSig TEXT,changed TEXT,f1 INTEGER,f2 INTEGER,f3 TEXT,f4 INTEGER,f5 TEXT,f6 TEXT,f7 INTEGER,f8 INTEGER,f9 INTEGER)");
			
			ResultSet rs = stmt.executeQuery("SELECT count(*) FROM " + avCommitTable);
			rs.next();
			int totalNum = rs.getInt(1);
			System.out.println(totalNum);
			
			stmt.close();
			conn.close();
			for (int offset = 0; offset < totalNum; offset++) {
				conn = SqliteManager.getConnection();
				stmt = conn.createStatement();
				
				rs = stmt.executeQuery("SELECT bugName FROM " + avCommitTable + " LIMIT 1 OFFSET " + offset);
				String bugName = rs.getString(1);
				rs.close();
				
				stmt.close();
				conn.close();
				
				
				System.out.println(offset + 1 + "/" + totalNum);
				System.out.println(bugName);
				
				String[] verBug = bugName.split("\\+\\+");
				String version = verBug[0], commit = verBug[1];
				try {
					thisVersion = version;
					ChangeExtracterClient3 changeClient = new ChangeExtracterClient3(folder, version, commit);
					String commitsFolder = repoPath + "/" + folder + "/" + folder + "-commits";
					String versionFolder = commitsFolder + "/" + version;
					String commitPath = versionFolder + "/" + commit;
	//				if(checkFileSizeFail(commitPath)) continue;
	//					Need to check if it's correct here
	//					if(checkFileMissing(commitPath)) continue;
					
					List<ChangeFact> cfList = changeClient.parseChanges(commitPath);
					List<TraverseJsonTyped> oldTJ = changeClient.oldTJ;
					ConnectChanges cC = new ConnectChanges();
					if (cfList == null) continue;
					System.out.println("The size of cfList is " + cfList.size());
					
					cC.groupChanges(cfList, commit, oldTJ);
				}catch (OutOfMemoryError e) {
					e.printStackTrace();
					}
			}
		}
			
		
	}
	
	public static void mainOthers(String[] args) throws Exception {
		String[] projects = new String[]{"pdf","meteor","habitica","node","serverless","webpack1","react","Ghost"};
		String[] patterns = new String[]{"av", "af", "cf"};
		for (String pattern : patterns) {
			if (!pattern.equals("av")) continue;
			double[] wa = new double[4];
			int totalTask = 0;
			for (String project : projects) {
				String table = pattern + "PredictTransAR_" + project;
				System.out.println(table);
				Connection conn = SqliteManager.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT count(*) FROM " + table);
				rs.next();
				int totalNum = rs.getInt(1);
				stmt.close();
				conn.close();
				
				int task = 0, precision = 0, recall = 0;
				for (int offset = 0; offset < totalNum; offset++) {
					conn = SqliteManager.getConnection();
					stmt = conn.createStatement();
					
					rs = stmt.executeQuery("SELECT precision, recall FROM " + table + " LIMIT 1 OFFSET " + offset);
					String pre = rs.getString(1);
//					System.out.println(pre);
					String rec = rs.getString(2);
					if (pre != null) {
						task++;
						precision += Integer.valueOf(pre);
						recall += Integer.valueOf(rec);
					}
					
					stmt.close();
					conn.close();
				}
				double prec = (double) precision / task;
				double reca = (double) recall / task;
				
				double fsco = 2 * prec * reca / (prec + reca);
				if(prec == 0&& reca == 0) fsco = 0;
				System.out.println(task + "/" + totalNum);
				System.out.println("coverage: " + (double) task / totalNum);
				System.out.println("precision: " + prec);
				System.out.println("recall: " + reca);
				System.out.println("f-score: " + fsco);
				totalTask += task;
				wa[0] += (double) task / totalNum * task;
				wa[1] += project.equals("storybook")? 0 : prec * task;
				wa[2] += project.equals("storybook")? 0 : reca * task;
				wa[3] += project.equals("storybook")? 0 : fsco * task;
			}
			for (int i = 0; i < 4; i++) {
				System.out.println(wa[i] / totalTask);
			}
			
			
		}
		
	}
	
	public static void mainRoseNode(String[] args) throws Exception {
		File repos = new File(repoPath);
		String[] directories = repos.list(new FilenameFilter() {
		  @Override
		  public boolean accept(File current, String name) {
		    return new File(current, name).isDirectory();
		  }
		});
		for(String folder : directories) {
			System.out.println(folder);
			if(!folder.matches("node")) continue;
			String commitsFolder = repoPath + "/" + folder + "/" + folder + "-commits";
			afPredictTable = "avPredictTransAR_" + folder;
			afCommitTable = "avCommit_" + folder;
			TransARPrediction.commitOrderTable = "commit_order_" + folder;
			roseTable = "af_rose_" + folder;
			Connection conn = SqliteManager.getConnection();
			Statement stmt = conn.createStatement();
			if (executeFromFirstBug){
				stmt.executeUpdate("DROP TABLE IF EXISTS " + afPredictTable);
			}
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + afPredictTable
					+ " (bug_name TEXT,af_sig TEXT,used_cf TEXT,real_other_cf TEXT,"
					+ "predicted_cf TEXT,precision INTEGER,recall TEXT, ground_truth_size INTEGER, predicted_size INTEGER, true_positive_size INTEGER)");
			ResultSet rs = stmt.executeQuery("SELECT count(*) FROM " + afCommitTable);
			rs.next();
			int totalNum = rs.getInt(1);
			System.out.println(totalNum);
			
			stmt.close();
			conn.close();
			for (int offset = 0; offset < totalNum; offset++) {
				conn = SqliteManager.getConnection();
				stmt = conn.createStatement();
				rs = stmt.executeQuery("SELECT bugName FROM " + afCommitTable + " LIMIT 1 OFFSET " + offset);
				String bugName = rs.getString(1);
				rs.close();
				
				stmt.close();
				conn.close();
				
				
				System.out.println(offset + 1 + "/" + totalNum);
				System.out.println(bugName);
				
				String commit = bugName;
				
				try {
					ChangeExtracterClient2 changeClient = new ChangeExtracterClient2();
					String commitPath = commitsFolder + "/" + commit;
					
//					if(checkFileSizeFail(commitPath)) continue;
//					Need to check if it's correct here
//					if(checkFileMissing(commitPath)) continue;
					
					List<ChangeFactNode> cfList = changeClient.parseChanges(commitPath);
					
					ConnectChanges cC = new ConnectChanges();
					List<TraverseJsonTyped> oldTJ = changeClient.oldTJ;
					if (cfList == null) continue;
					
					cC.groupChanges(cfList, commit, oldTJ);
				}catch (OutOfMemoryError e) {
					e.printStackTrace();
					}
			}
		}
		
	}
	
	public static void mainNodeData(String[] args) throws Exception {
		RosePrediction.commitOrderTable = "commit_order_node";
		roseTable = "af_rose_node";
		File repos = new File(repoPath);
		String[] directories = repos.list(new FilenameFilter() {
		  @Override
		  public boolean accept(File current, String name) {
		    return new File(current, name).isDirectory();
		  }
		});
		for(String folder : directories) {
			if (!folder.equals("node")) continue;
			String commitsFolder = repoPath + "/" + folder + "/" + folder + "-commits";
			afCommitTable = "afCommit_" + folder;
			cfCommitTable = "cfCommit_" + folder;
			avCommitTable = "avCommit_" + folder;
			afcfTable = "afcfData3_" + folder;
			avcfTable = "avcfData3_" + folder;
			cfcfTable = "cfcfData3_" + folder;
			Connection conn = SqliteManager.getConnection();
			Statement stmt = conn.createStatement();
			if (executeFromFirstBug){
				stmt.executeUpdate("DROP TABLE IF EXISTS " + afcfTable);
			}
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + afcfTable + "(afSig TEXT,cf1Sig TEXT,candiSig TEXT,changed TEXT,f1 INTEGER,f2 INTEGER,f3 TEXT,f4 INTEGER,f5 INTEGER,f6 INTEGER)");
			
			if (executeFromFirstBug){
				stmt.executeUpdate("DROP TABLE IF EXISTS " + cfcfTable);
			}
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + cfcfTable + "(cfSig TEXT,cf1Sig TEXT,candiSig TEXT,changed TEXT,f1 INTEGER,f2 INTEGER,f3 TEXT,f4 INTEGER,f5 INTEGER,f6 INTEGER)");
			
			if (executeFromFirstBug){
				stmt.executeUpdate("DROP TABLE IF EXISTS " + avcfTable);
			}
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + avcfTable + "(avSig TEXT,cf1Sig TEXT,candiSig TEXT,changed TEXT,f1 INTEGER,f2 INTEGER,f3 TEXT,f4 INTEGER,f5 INTEGER,f6 INTEGER)");
			
			ResultSet rs = stmt.executeQuery("SELECT count(*) FROM " + avCommitTable);
			rs.next();
			int totalNum = rs.getInt(1);
			stmt.close();
			conn.close();
			
			for (int offset = 0; offset < totalNum; offset++) {
				conn = SqliteManager.getConnection();
				stmt = conn.createStatement();
				
				rs = stmt.executeQuery("SELECT bugName FROM " + avCommitTable + " LIMIT 1 OFFSET " + offset);
				String bugName = rs.getString(1);
				rs.close();
				
				stmt.close();
				conn.close();
				
				
				System.out.println(offset + 1 + "/" + totalNum);
				System.out.println(bugName);
				
				String commit = bugName;
				
				try {
					ChangeExtracterClient2 changeClient = new ChangeExtracterClient2();
					String commitPath = commitsFolder + "/" + commit;
					
//					if(checkFileSizeFail(commitPath)) continue;
//					Need to check if it's correct here
//					if(checkFileMissing(commitPath)) continue;
					
					List<ChangeFactNode> cfList = changeClient.parseChanges(commitPath);
					
					ConnectChanges cC = new ConnectChanges();
					List<TraverseJsonTyped> oldTJ = changeClient.oldTJ;
					if (cfList == null) continue;
					
					cC.groupChanges(cfList, commit, oldTJ);
				}catch (OutOfMemoryError e) {
					e.printStackTrace();
					}
			}
		}
			
		
	}
	
	public static void mainToExtractRoseTable(String[] args) throws Exception {
		String[] projects = new String[]{"storybook"};
		for (String project: projects) {
			Connection conn = SqliteManager.getConnection();
			Statement stmt = conn.createStatement();
			
			
			roseTable = "af_rose_" + project;
			
			RosePrediction.commitOrderTable = "commit_order_" + project;
			
			if (executeFromFirstBug) {
				stmt.executeUpdate("DROP TABLE IF EXISTS " + roseTable);
			}
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + roseTable
					+ " (bug_name TEXT, name TEXT)");
			
			stmt.close();
			conn.close();
			
			String commitsFolder = repoPath + "/" + project + "/" + project + "-commits";
			File commitsFile = new File(commitsFolder);
			String[] commitDirectory = commitsFile.list(new FilenameFilter() {
			  @Override
			  public boolean accept(File current, String name) {
			    return new File(current, name).isDirectory();
			  }
			});
			int i = 0;
			for(i = 0; i < commitDirectory.length; i++) {
//				if(i < 51) continue;
				System.out.println(i + "/" + commitDirectory.length);
				String commit = commitDirectory[i];
				if (project.equals("node")) {
					
					try{
						ChangeExtracterClient2 changeClient = new ChangeExtracterClient2();
						String commitPath = commitsFolder + "/" + commit;
						List<ChangeFactNode> cfList = changeClient.parseChanges(commitPath);
						if (cfList == null) continue;	
						ChangeFactNode.extractChangesForRose(commit, cfList);
					}catch (OutOfMemoryError e) {
						e.printStackTrace();
						}
				}
				else {
					File versionFile = new File(commitsFolder + "/" + commit);
					String[] commitDirectoryIn = versionFile.list(new FilenameFilter() {
						  @Override
						  public boolean accept(File current, String name) {
						    return new File(current, name).isDirectory();
						  }
						});
					
					int j = 0;
					for(String commitIn : commitDirectoryIn) {
//						if (i == 51 && j < 14) continue; 
						System.out.println((j++) + "/" + commitDirectoryIn.length);
						try{
							ChangeExtracterClient3 changeClient = new ChangeExtracterClient3(project, commit, commitIn);
							String commitPath = commitsFolder + "/" + commit + "/" + commitIn;
							List<ChangeFact> cfList = changeClient.parseChanges(commitPath);
							if (cfList == null) continue;	
							ChangeFact.extractChangesForRose(commitIn, cfList);
						}catch (OutOfMemoryError e) {
							e.printStackTrace();
							}
							
					}
				}
			}		
		}	
	}
	
	
	public static void mainOthers(String[] args) throws Exception {
				
		int[] count = new int[1000];
		int[] typeChangeCount = new int[12];
		File repos = new File(repoPath);
		String[] directories = repos.list(new FilenameFilter() {
		  @Override
		  public boolean accept(File current, String name) {
		    return new File(current, name).isDirectory();
		  }
		});
		
		
		for(String folder : directories) {
//			afSet = new HashSet<>();
//			avSet = new HashSet<>();
//			cfSet = new HashSet<>();
//			afCommitTable = "afCommit_" + folder;
//			cfCommitTable = "cfCommit_" + folder;
//			avCommitTable = "avCommit_" + folder;
			
			editScriptTable = "classify_graphmerge_final_" + folder;
			if(!folder.matches("serverless")) continue;
//			if(!"storybook".contains(folder)) continue;
//			afcfTable = "afcfData2_" + folder;
//			avcfTable = "avcfData2_" + folder;
//			cfcfTable = "cfcfData2_" + folder;
			Connection conn = SqliteManager.getConnection();
			Statement stmt = conn.createStatement();
			if (executeFromFirstBug)
				stmt.executeUpdate("DROP TABLE IF EXISTS " + editScriptTable);
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + editScriptTable
					+ " (bug_name TEXT, graph_num INTEGER,"
					+ "graph_data TEXT)");
			stmt.close();
			conn.close();
			
//			conn = SqliteManager.getConnection();
//			stmt = conn.createStatement();
//			if (executeFromFirstBug){
//				stmt.executeUpdate("DROP TABLE IF EXISTS " + afCommitTable);
//			}
//			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + afCommitTable + "(bugName TEXT)");
//			stmt.close();
//			conn.close();
//			
//			conn = SqliteManager.getConnection();
//			stmt = conn.createStatement();
//			if (executeFromFirstBug){
//				stmt.executeUpdate("DROP TABLE IF EXISTS " + cfCommitTable);
//			}
//			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + cfCommitTable + "(bugName TEXT)");
//			stmt.close();
//			conn.close();
//			
//			conn = SqliteManager.getConnection();
//			stmt = conn.createStatement();
//			if (executeFromFirstBug){
//				stmt.executeUpdate("DROP TABLE IF EXISTS " + avCommitTable);
//			}
//			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + avCommitTable + "(bugName TEXT)");
//			stmt.close();
//			conn.close();
//			
//			conn = SqliteManager.getConnection();
//			stmt = conn.createStatement();
//			if (executeFromFirstBug){
//				stmt.executeUpdate("DROP TABLE IF EXISTS " + afcfTable);
//			}
//			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + afcfTable + "(afSig TEXT,cf1Sig TEXT,candiSig TEXT,changed TEXT,f1 INTEGER,f2 INTEGER,f3 TEXT,f4 INTEGER,f5 TEXT,f6 INTEGER,f7 TEXT,f8 INTEGER,f9 INTEGER)");
//			stmt.close();
//			conn.close();
//			
//			conn = SqliteManager.getConnection();
//			stmt = conn.createStatement();
//			if (executeFromFirstBug){
//				stmt.executeUpdate("DROP TABLE IF EXISTS " + cfcfTable);
//			}
//			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + cfcfTable + "(cfSig TEXT,cf1Sig TEXT,candiSig TEXT,changed TEXT,f1 INTEGER,f2 INTEGER,f3 TEXT,f4 INTEGER,f5 TEXT,f6 INTEGER,f7 TEXT,f8 INTEGER,f9 INTEGER)");
//			stmt.close();
//			conn.close();
//			
//			conn = SqliteManager.getConnection();
//			stmt = conn.createStatement();
//			if (executeFromFirstBug){
//				stmt.executeUpdate("DROP TABLE IF EXISTS " + avcfTable);
//			}
//			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + avcfTable + "(avSig TEXT,cf1Sig TEXT,candiSig TEXT,changed TEXT,f1 INTEGER,f2 INTEGER,f3 TEXT,f4 INTEGER,f5 TEXT,f6 TEXT,f7 INTEGER,f8 INTEGER)");
//			stmt.close();
//			conn.close();
			
			int multi = 0;
			int total = 0;
			System.out.println(folder);
			String commitsFolder = repoPath + "/" + folder + "/" + folder + "-commits";
			File commitsFile = new File(commitsFolder);
			String[] versionDirectory = commitsFile.list(new FilenameFilter() {
			  @Override
			  public boolean accept(File current, String name) {
			    return new File(current, name).isDirectory();
			  }
			});
			
			
			for(String version : versionDirectory) {
				thisVersion = version;
				System.out.println(version);
				String versionFolder = commitsFolder + "/" + version;
				File versionFile = new File(versionFolder);
				String[] commitDirectory = versionFile.list(new FilenameFilter() {
					  @Override
					  public boolean accept(File current, String name) {
					    return new File(current, name).isDirectory();
					  }
					});
				
				int i = 0;
				for(i = 0; i < commitDirectory.length; i++) {
					System.out.println(i + "/" + commitDirectory.length);
					String commit = commitDirectory[i];
					System.out.println(commit);
//					if (!commit.equals("733d638")) continue;
////				
					
//					if (!commit.equals("df91acf") && !goon) continue;
//////////					
//					goon = true;
//////					
//					if (commit.equals("df91acf")) continue;
					try{
						ChangeExtracterClient3 changeClient = new ChangeExtracterClient3(folder, version, commit);
						String commitPath = versionFolder + "/" + commit;
//						if(checkFileSizeFail(commitPath)) continue;
	//					Need to check if it's correct here
	//					if(checkFileMissing(commitPath)) continue;
						
						List<ChangeFact> cfList = changeClient.parseChanges(commitPath);
						List<TraverseJsonTyped> oldTJ = changeClient.oldTJ;
						ConnectChanges cC = new ConnectChanges();
						if (cfList == null) continue;
//						System.out.println("The size of cfList is " + cfList.size());
						
						cC.groupChanges(cfList, commit, oldTJ);
						if(cfList == null) continue;
						int changed = 0;
						for(ChangeFact cf : cfList){
							changed += cf.getNumOfChanged();
							typeChangeCount[0] += cf.insertedVars.size();
							typeChangeCount[1] += cf.deletedVars.size();
							typeChangeCount[2] += cf.changedVars.size();
							typeChangeCount[3] += cf.insertedFuncs.size();
							typeChangeCount[4] += cf.deletedFuncs.size();
							typeChangeCount[5] += cf.changedFuncs.size();
	//						if(cf.changedFuncs.size() > 0){
	//							for(Pair<ClientFunc, ClientFunc> p : cf.changedFuncs){
	//								System.out.println("changed func name is " + p.fst.funcName);
	//							}
	//						}
							typeChangeCount[6] += cf.insertedClasses.size();
							typeChangeCount[7] += cf.deletedClasses.size();
							typeChangeCount[8] += cf.changedClasses.size();
							typeChangeCount[9] += cf.insertedExps.size();
							typeChangeCount[10] += cf.deletedExps.size();
							typeChangeCount[11] += cf.changedExps.size();
						}
						
	//					if(changed > 5) {
	//						System.out.println(commit + " num " + changed);
	//						break;
	//					}
						if(changed < 1000) count[changed]++;
						if(changed > 1) multi++;
						if(changed >= 1) total++;
						System.out.println(multi + "/" + total);
						for(int k = 0; k < 12; k++){
							System.out.println("changes are" + typeChangeCount[k]);
							System.out.println("number of AMD files is " + amdFiles);
						}
						System.out.println(inOrOut[0]);
						System.out.println(inOrOut[1]);
						System.out.println(commit);
	//					if(commit.equals("2c59bf8")) break;
						} catch (OutOfMemoryError e) {
							e.printStackTrace();
							}
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
//			writeCommitData(afCommitTable, afSet);
//			writeCommitData(cfCommitTable, cfSet);
//			writeCommitData(avCommitTable, avSet);
		}	
	}
}