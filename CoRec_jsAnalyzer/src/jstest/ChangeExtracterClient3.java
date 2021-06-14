package jstest;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.Set;

import vt.edu.graph.ClientVar;
import vt.edu.util.Pair;

import com.github.gumtreediff.gen.antlr3.json.AntlrJsonTreeGenerator;
import com.github.gumtreediff.tree.ITree;

public class ChangeExtracterClient3 {
	
	/*
	 * Here if a js file is in AMD style, then we return a null list, which is OK!
	 */
	String typedFilePath = "/Users/zijianjiang/Documents/R2C";
	String folder, commit, version;
	List<TraverseJsonTyped> oldTJ = new ArrayList<TraverseJsonTyped>();
	
	public ChangeExtracterClient3(String folder, String version, String commit) {
		this.folder = folder;
		this.commit = commit;
		this.version = version;
		this.typedFilePath = "/Users/zijianjiang/Documents/R2C/" + folder + "/" + version + "/ast-consumer/" + commit;
	}
	
	private String getFileContent(File f) throws IOException {
		String content = null;
		Scanner s = new Scanner(f);
        if (s.useDelimiter("\\Z").hasNext()) {
            content = s.useDelimiter("\\Z").next();
        }
        s.close();
        return content;
	}
	
	
	public List<ChangeFact> parseChanges(String filePath) throws Exception {
		
		File commit = new File(filePath);
		File typedCommit = new File(typedFilePath);
		if (!typedCommit.exists()) return null;
		
		File from = new File(commit.getAbsolutePath() + "/from");
		File typedFrom = new File(typedCommit.getAbsolutePath() + "/from");
		File to = new File(commit.getAbsolutePath() + "/to");
		File typedTo = new File(typedCommit.getAbsolutePath() + "/to");
		
		Map<String, File> oldJsFiles = new HashMap<String, File>();
		Map<String, File> newJsFiles = new HashMap<String, File>();
		
		String fileName = null;
		
		Set<String> visited = new HashSet<String>();
		
		for(File f : from.listFiles()) {
			fileName = f.getName();
			System.out.println(fileName);
			
			if(fileName.contains("test")) continue;
			
			if(fileName.endsWith(".js")) {
				int len = fileName.length();
				fileName = fileName.substring(0, len - 3);
				oldJsFiles.put(fileName, f);
			}

		}
		
		for(File f : to.listFiles()) {
			fileName = f.getName();
			
			if(fileName.contains("test")) continue;
			if(fileName.endsWith(".js")) {
				int len = fileName.length();
				fileName = fileName.substring(0, len - 3);
				newJsFiles.put(fileName, f);
			}
		}
		
		ChangeFact cf = null;
		List<ChangeFact> cfList = new ArrayList<ChangeFact>();
		
		File oJsFile = null, nJsFile = null, oTypedJson = null, nTypedJson = null;
		String jsonFileName = null, typedOJsonContent = null, typedNJsonContent = null;
		TraverseJsonTyped tJo = null, tJn = null;
		ITree nTree = null, oTree = null, typedNTree = null, typedOTree = null;
		try{
			for(Entry<String, File> entry : oldJsFiles.entrySet()) {
				fileName = entry.getKey();
				visited.add(fileName);
				oJsFile = oldJsFiles.get(fileName);
				jsToJson jJ = new jsToJson(oJsFile.getAbsolutePath());
				String oJsonContent = jJ.cmdForEs();
				if (oJsonContent.length() == 0) continue;
				oTree = new AntlrJsonTreeGenerator().generateFrom().string(oJsonContent).getRoot();
				jsonFileName =typedFilePath + "/from/" + fileName + ".js.ast.json";
				oTypedJson = new File(jsonFileName);
				if (!oTypedJson.exists()) continue;
				typedOJsonContent = getFileContent(oTypedJson);
				
				typedOTree = new AntlrJsonTreeGenerator().generateFrom().string(typedOJsonContent).getRoot();
				
				
				tJo = new TraverseJsonTyped(typedOTree, oJsFile.getName(), oTree);
	//				check AMD, need to decide if we are going to continue analyzing this commit folder
				if(tJo.isAMD == true) return null;
				oldTJ.add(tJo);
	//				if(tJo.isAMD == true) continue;
				nJsFile = newJsFiles.get(fileName);
				if(nJsFile != null) {
					
					jJ = new jsToJson(nJsFile.getAbsolutePath());				
					String nJsonContent = jJ.cmdForEs();
					if(nJsonContent.length() == 0) continue;
					nTree = new AntlrJsonTreeGenerator().generateFrom().string(nJsonContent).getRoot();
					
					jsonFileName =typedFilePath + "/to/" + fileName + ".js.ast.json";
					nTypedJson = new File(jsonFileName);
					if (!nTypedJson.exists()) continue;
					typedNJsonContent = getFileContent(nTypedJson);
					typedNTree = new AntlrJsonTreeGenerator().generateFrom().string(typedNJsonContent).getRoot();
					
					
					tJn = new TraverseJsonTyped(typedNTree, nJsFile.getName(), nTree);
	//					check AMD
					if(tJn.isAMD == true) return null;
	
				}
				cf = new ChangeFact(oJsFile, nJsFile, tJo, tJn);
	//				testOutPut(cf);
				cfList.add(cf);
			}
			
			oJsFile = null;
			tJo = null;
			
			for(Entry<String, File> entry : newJsFiles.entrySet()) {
				fileName = entry.getKey();
				if(!visited.add(fileName)){
					continue;
				}
				nJsFile = newJsFiles.get(fileName);
				jsToJson jJ = new jsToJson(nJsFile.getAbsolutePath());
				String nJsonContent = jJ.cmdForEs();
				if(nJsonContent.length() == 0) continue;
				nTree = new AntlrJsonTreeGenerator().generateFrom().string(nJsonContent).getRoot();
				
				jsonFileName =typedFilePath + "/to/" + fileName + ".js.ast.json";
				nTypedJson = new File(jsonFileName);
				if (!nTypedJson.exists()) continue;
				typedNJsonContent = getFileContent(nTypedJson);
				typedNTree = new AntlrJsonTreeGenerator().generateFrom().string(typedNJsonContent).getRoot();
				
				
				tJn = new TraverseJsonTyped(typedNTree, nJsFile.getName(), nTree);
	//				check AMD
				if(tJn.isAMD == true) return null;
				
				cf = new ChangeFact(oJsFile, nJsFile, tJo, tJn);
				cfList.add(cf);
			}
		}catch(Exception e) {
			e.printStackTrace();
			System.out.println("This is too big");
		}
		
		return cfList;
	}
	
	private void testOutPut(ChangeFact cf){
		System.out.println(cf.leftJs.getName());
		System.out.println(cf.changedClasses.size());
        System.out.println(cf.deletedClasses.size());
        System.out.println(cf.insertedClasses.size());
        System.out.println(cf.changedVars.size());
        for(Pair<ClientVar, ClientVar> varPair : cf.changedVars){
        		System.out.println(varPair.fst.sig);
        }
        System.out.println(cf.deletedVars.size());
        System.out.println(cf.insertedVars.size());
        System.out.println(cf.changedFuncs.size());
        System.out.println(cf.deletedFuncs.size());
        System.out.println(cf.insertedFuncs.size());
        System.out.println(cf.changedExps.size());
        System.out.println(cf.deletedExps.size());
        System.out.println(cf.insertedExps.size());
		
	}
}

