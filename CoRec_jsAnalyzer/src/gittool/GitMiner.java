package gittool;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import com.google.gson.Gson;

public class GitMiner {
	private final Repository repository;
	private Git git;
	private Iterable<RevCommit> revCommits;
	static String projectPath;
	static String projectName;
	static Pattern bugfix = Pattern.compile("bug|fix|error|patch|adjust|failure");
	static String[] projects = new String[]{
		"https://github.com/serverless/serverless",
		"https://github.com/nodejs/node",
		"https://github.com/meteor/meteor",
		"https://github.com/TryGhost/Ghost",
		"https://github.com/freeCodeCamp/freeCodeCamp",
		"https://github.com/electron/electron",
		"https://github.com/storybooks/storybook",
		"https://github.com/mrdoob/three.js",
		"https://github.com/webpack/webpack",
		"https://github.com/mozilla/pdf.js"
	};
	private static HashMap<String, List<String>> jsonMapNew = new HashMap<>();
	private static HashMap<String, List<String>> jsonMapOld = new HashMap<>();
	
	public GitMiner(Repository repository) {
		this.repository = repository;
		git = new Git(repository);
		refreshRevCommits();
	}
	
	public void refreshRevCommits(){
		try {
			revCommits = git.log().call();
		} catch (NoHeadException e) {
			e.printStackTrace();
		} catch (GitAPIException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean createDir(String destDirName) {
		File dir = new File(destDirName);
		if (dir.exists()) {
			return false;
		}
		if (!destDirName.endsWith(File.separator)) {
			destDirName = destDirName + File.separator;
		}
		if (dir.mkdirs()) {
			return true;
		} else {
			return false;
		}
	}
	
	public int getSize(Iterable<RevCommit> commitsRev){
		int count = 0;
		for(RevCommit commit : commitsRev){
			count++;
		}
		return count;
	}
	
	public List<RevCommit> getCommitList(Iterable<RevCommit> commitsRev) {
		List<RevCommit> commitList = new ArrayList<RevCommit>();
		for(RevCommit commit : commitsRev){
			commitList.add(commit);
		}
		return commitList;
	}
	
	public void call2(String web) throws IOException {
		jsonMapOld.put(web, new ArrayList<String>());
		jsonMapNew.put(web, new ArrayList<String>());
		String commitHash;
		List<RevCommit> commitList = getCommitList(revCommits);
		String message;
		
		for (int i = 0; i < commitList.size() - 1; i++) {
			RevCommit commit = commitList.get(i);

			System.out.println(i);
			commitHash = commit.getName();
			message = commit.getFullMessage();
//			if(!bugfix.matcher(message.toLowerCase()).find()) continue;
			if(message.toLowerCase().contains("typo")) continue;
			String gitDir = projectPath + "/" + ".git";
			String showCommand = "git --git-dir=" + gitDir  + " show " + commitHash;
			System.out.println(commitHash);
			Process proc = Runtime.getRuntime().exec(showCommand);
			
			BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			String output = null;
			
			while ((output = br.readLine()) != null) {
				if(output.startsWith("diff --git a"))
					System.out.println(output);
				if(output.startsWith("diff --git a") && output.endsWith(".js")){
					jsonMapNew.get(web).add(commit.getName());
					jsonMapOld.get(web).add(commitList.get(i + 1).getName());
					break;
				}
			}
			br.close();
		}
	}
	
	
	
	public void call() throws Exception {
		String commitHash;
		int counter = getSize(revCommits);
		refreshRevCommits();
		String message;
		
		for (RevCommit commit : revCommits) {
			counter--;
			if(counter == 0) continue;
			System.out.println(counter);
			commitHash = commit.getName();
			message = commit.getFullMessage();
//			if(!bugfix.matcher(message.toLowerCase()).find()) continue;
//			int issueId = getIssueId(message);
//			it's not a bugfix
//			if(issueId == -1 && !bugfix.matcher(message.toLowerCase()).find()) {
//				continue;
//			}
//			delete typo
//			if(message.toLowerCase().contains("typo")) continue;
////			it's a duplicate bugfix
//			if(issueId != -1 && !issueSet.add(issueId)) continue;
//			
//			int issueOrPrId = getIssueOrPrId(message);
//			
//			if(issueId == -1 && issueOrPrId != -1 && !issuePrSet.add(issueOrPrId)) continue;
//			
			
			System.out.println(commitHash);
//			for r2c
//			String commitDir = projectPath + "/" + projectName + "-commits-r2c" + "/" + commitHash.substring(0, 7);
//					+ "_" + projectName + "_" + counter;
			String commitDir = projectPath + "/" + projectName + "-commits" + "/" + commitHash.substring(0, 7);
//					+ "_" + projectName + "_" + counter;
			String gitDir = projectPath + "/" + ".git";
			String showCommand = "git --git-dir=" + gitDir  + " show " + commitHash;
			System.out.println(commitHash);
			Process proc = Runtime.getRuntime().exec(showCommand);
			
			BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			String output = null;
			String fromFolder = commitDir + "/" + "from";
			String toFolder = commitDir + "/" + "to";
			
			while ((output = br.readLine()) != null) {
				if(output.startsWith("diff --git a"))
					System.out.println(output);
				if(output.startsWith("diff --git a") && output.endsWith(".js")){
					System.out.println(commitHash + " " + output);
					String[] temp = output.split(" ");
					String fileName = temp[temp.length - 1].substring(2).replace("/", "++");
					System.out.println(fileName);
					output = br.readLine();
					if(!output.startsWith("index")) output = br.readLine();
					System.out.println(output);
					String[] indexes = output.split(" ");
					String[] oldNewFiles = indexes[1].split("\\.\\.");
					if(oldNewFiles.length < 2) continue;
					String oldFileHash = oldNewFiles[0];
					String newFileHash = oldNewFiles[1];
					System.out.println(oldFileHash);
					createDir(fromFolder);
					createDir(toFolder);
					String fromCommand, toCommand;
					if (!oldFileHash.startsWith("0000000")) {
						fromCommand = "git --git-dir=" + gitDir  + " show " + oldFileHash;
						Process saveOldFile = Runtime.getRuntime().exec(fromCommand);
						File oldFileOut = new File(fromFolder + "/" + fileName);
						FileOutputStream oldFos = new FileOutputStream(oldFileOut);
						BufferedWriter bwOld = new BufferedWriter(new OutputStreamWriter(oldFos));
						String fileContents;
						BufferedReader brOld = new BufferedReader(new InputStreamReader(saveOldFile.getInputStream()));
						while((fileContents = brOld.readLine()) != null) {
							bwOld.write(fileContents + "\n");
						}
						bwOld.close();
						brOld.close();
					}
					if (!newFileHash.startsWith("0000000")) {
						toCommand = "git --git-dir=" + gitDir  + " show " + newFileHash;
						Process saveNewFile = Runtime.getRuntime().exec(toCommand);
						File newFileOut = new File(toFolder + "/" + fileName);
						FileOutputStream newFos = new FileOutputStream(newFileOut);
						BufferedWriter bwNew = new BufferedWriter(new OutputStreamWriter(newFos));
						String fileContents;
						BufferedReader brNew = new BufferedReader(new InputStreamReader(saveNewFile.getInputStream()));
						while((fileContents = brNew.readLine()) != null) {
							bwNew.write(fileContents + "\n");
						}
						bwNew.close();
						brNew.close();
					}
				}
			}
			br.close();
		}
	}
	
	
	public static void main(String[] args) throws IOException, InterruptedException {
		String repoPath = "/Users/zijianjiang/test-repos";
//		String repoPath = "/Volumes/TEYADI";
		File file = new File(repoPath);
		
		String[] directories = file.list(new FilenameFilter() {
		  @Override
		  public boolean accept(File current, String name) {
		    return new File(current, name).isDirectory();
		  }
		});
		
		for(String folder : directories) {
			if(!"node".contains(folder)) continue;
//			String web = null;
//			for(String repo : projects) {
//				String[] split = repo.split("/");
//				if(split[split.length - 1].equals(folder)) {
//					web = repo;
//					break;
//				}
// 			}
			projectPath = repoPath + "/" + folder;
			projectName = folder;
//			for r2c
//			createDir(projectPath + "/" + projectName + "-commits-r2c");
			createDir(projectPath + "/" + projectName + "-commits");
			FileRepositoryBuilder builder = new FileRepositoryBuilder();
			try {
				Repository repository = builder.setGitDir(new File(projectPath + "/" + ".git"))
					.readEnvironment().findGitDir().build();
				GitMiner miner = new GitMiner(repository);
//				miner.call2(web);
				miner.call();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
//		Gson gson = new Gson();
//		String jsonOld = gson.toJson(jsonMapOld);
//		String jsonNew = gson.toJson(jsonMapNew);
//		File saveFileOld = new File("/Users/zijianjiang/Documents/ZijianJiang_repo/old.json");
//		File saveFileNew = new File("/Users/zijianjiang/Documents/ZijianJiang_repo/new.json");
//		try (PrintStream out = new PrintStream(new FileOutputStream(saveFileOld))) {
//		    out.print(jsonOld);
//		}
//		try (PrintStream out = new PrintStream(new FileOutputStream(saveFileNew))) {
//		    out.print(jsonNew);
//		}
	}
	
	
//	public int getIssueId(String message) {
//		Pattern pattern1 = Pattern.compile("issue *\\d+");
//		Pattern pattern2 = Pattern.compile("issue *#\\d+");
//		Matcher matcher1 = pattern1.matcher(message.toLowerCase());
//		Matcher matcher2 = pattern2.matcher(message.toLowerCase());
//		if(matcher1.find()) {
//			int start = matcher1.start();
//			int end = matcher1.end();
//			return Integer.valueOf(message.substring(start + 5, end).trim());
//		}
//		else if(matcher2.find()) {
//			int start = matcher2.start();
//			int end = matcher2.end();
//			return Integer.valueOf(message.substring(start + 5, end).trim().substring(1));
//		}
//		else return -1;
//	}
//	
//	public int getIssueOrPrId(String message){
//		Pattern pattern = Pattern.compile("#\\d+");
//		Matcher matcher = pattern.matcher(message.toLowerCase());
//		if(matcher.find()) {
//			int start = matcher.start();
//			int end = matcher.end();
//			return Integer.valueOf(message.substring(start + 1, end).trim());
//		}
//		return -1;
//	}
}
