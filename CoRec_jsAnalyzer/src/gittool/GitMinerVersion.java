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

public class GitMinerVersion {
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
	
	public GitMinerVersion(Repository repository) {
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
	
	private List<RevCommit> getCommitList(Iterable<RevCommit> commitsRev) {
		List<RevCommit> commitList = new ArrayList<RevCommit>();
		for(RevCommit commit : commitsRev){
			commitList.add(commit);
		}
		return commitList;
	}
	
	private String getVersion(String gitDir, String commitHash, int count) throws IOException {
		String showCommand = "git --git-dir=" + gitDir  + " describe --tag " + commitHash;
//		For meteor, no tag
//		String showCommand = "git --git-dir=" + gitDir  + " describe --tag " + commitHash; 
		System.out.println(commitHash);
		Process proc = Runtime.getRuntime().exec(showCommand);
		
		BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		String output = br.readLine();
		if (output == null) return "No version";
		String[] versions = output.split("-");
		br.close();
		return versions[0].replace("/","-");
//		return count/500 + "";
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
//			if(message.toLowerCase().contains("typo")) continue;
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
	
	
	
	private void call() throws Exception {
		String commitHash;
		int counter = getSize(revCommits);
		refreshRevCommits();
		String message;
		List<String> versionList = new ArrayList<>();
		for (RevCommit commit : revCommits) {
			counter--;
			if(counter == 0) continue;
			System.out.println(counter);
//			if (counter >= 1242) continue;
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
			String gitDir = projectPath + "/" + ".git";
			String version = getVersion(gitDir, commitHash, counter);
			if (!version.equals("No version")) versionList.add(version);
			if (version.equals("No version")) {
				if (versionList.size() > 0) version = versionList.get(versionList.size() - 1);
				else continue;
			}
//			for r2c
//			String commitDir = projectPath + "/" + projectName + "-commits-r2c" + "/" + commitHash.substring(0, 7);
//					+ "_" + projectName + "_" + counter;
			String commitDir = projectPath + "/" + projectName + "-commits" + "/" + version + "/" + commitHash.substring(0, 7);
//					+ "_" + projectName + "_" + counter;
			
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
					
//					download the package.json file
					String versionDir = projectPath + "/" + projectName + "-commits" + "/" + version;
					File versionFile = new File(versionDir);
					if (!versionFile.exists()) {
						createDir(versionDir);
						File packageFile = new File(versionDir + "/" + "package.json");
						String getPackageCommand = "git --git-dir=" + gitDir + " show " + commitHash + ":package.json";
						Process getPackage = Runtime.getRuntime().exec(getPackageCommand);
						FileOutputStream packageOps = new FileOutputStream(packageFile);
						BufferedWriter packageBw = new BufferedWriter(new OutputStreamWriter(packageOps));
						String fileContents;
						BufferedReader packageBr = new BufferedReader(new InputStreamReader(getPackage.getInputStream()));
						while((fileContents = packageBr.readLine()) != null) {
							packageBw.write(fileContents + "\n");
						}
						packageBw.close();
						packageBr.close();
					}
					
//					For meteor download the other two js files instead of json file
//					String versionDir = projectPath + "/" + projectName + "-commits" + "/" + version;
//					File versionFile = new File(versionDir);
//					if (!versionFile.exists()) {
//						createDir(versionDir);
//						File packageFile = new File(versionDir + "/" + "dev-bundle-tool-package.js");
//						File packageFile2 = new File(versionDir + "/" + "dev-bundle-server-package.js");
//						File packageFile3 = new File(versionDir + "/" + "generate-dev-bundle.sh");
//						String getPackageCommand = "git --git-dir=" + gitDir + " show " + commitHash + ":scripts/dev-bundle-tool-package.js";
//						String getPackageCommand2 = "git --git-dir=" + gitDir + " show " + commitHash + ":scripts/dev-bundle-tool-package.js";
//						String getPackageCommand3 = "git --git-dir=" + gitDir + " show " + commitHash + ":scripts/generate-dev-bundle.sh";
//						Process getPackage = Runtime.getRuntime().exec(getPackageCommand);
//						FileOutputStream packageOps = new FileOutputStream(packageFile);
//						BufferedWriter packageBw = new BufferedWriter(new OutputStreamWriter(packageOps));
//						String fileContents;
//						BufferedReader packageBr = new BufferedReader(new InputStreamReader(getPackage.getInputStream()));
//						while((fileContents = packageBr.readLine()) != null) {
//							packageBw.write(fileContents + "\n");
//						}
//						packageBw.close();
//						packageBr.close();
//						
//						Process getPackage2 = Runtime.getRuntime().exec(getPackageCommand2);
//						FileOutputStream packageOps2 = new FileOutputStream(packageFile2);
//						BufferedWriter packageBw2 = new BufferedWriter(new OutputStreamWriter(packageOps2));
//						String fileContents2;
//						BufferedReader packageBr2 = new BufferedReader(new InputStreamReader(getPackage2.getInputStream()));
//						while((fileContents2 = packageBr2.readLine()) != null) {
//							packageBw2.write(fileContents2 + "\n");
//						}
//						packageBw2.close();
//						packageBr2.close();
//						
//						Process getPackage3 = Runtime.getRuntime().exec(getPackageCommand3);
//						FileOutputStream packageOps3 = new FileOutputStream(packageFile3);
//						BufferedWriter packageBw3 = new BufferedWriter(new OutputStreamWriter(packageOps3));
//						String fileContents3;
//						BufferedReader packageBr3 = new BufferedReader(new InputStreamReader(getPackage3.getInputStream()));
//						while((fileContents3 = packageBr3.readLine()) != null) {
//							if (fileContents3.startsWith("npm install") && fileContents3.contains("@")) {
//								String[] strs = fileContents3.split(" ");
//								String dep = strs[2];
//								String[] deps = dep.split("@");
//								if(deps.length == 2) packageBw3.write("\"" + deps[0] + "\""  + 
//								":" + " \"" + deps[1] + "\"," + "\n");
//							}
//						}
//						packageBw3.close();
//						packageBr3.close();
//						
//					}
//					
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
			if(!folder.equals("react")) continue;
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
				GitMinerVersion miner = new GitMinerVersion(repository);
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
