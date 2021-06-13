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

public class ChangeExtracterClient {
	
	private String getFileContent(File f) throws IOException {
		String content = null;
		Scanner s = new Scanner(f);
        if (s.useDelimiter("\\Z").hasNext()) {
            content = s.useDelimiter("\\Z").next();
        }
        s.close();
        return content;
	}
	
	/*
	 * Here if a js file is in AMD style, then we return a null list, which is OK!
	 */
	
	public List<ChangeFact> parseChanges(String filePath) throws Exception {
		
		File commit = new File(filePath);
		File from = new File(commit.getAbsolutePath() + "/from");
		File to = new File(commit.getAbsolutePath() + "/to");
		Map<String, File> oldJsonFiles = new HashMap<String, File>();
		Map<String, File> newJsonFiles = new HashMap<String, File>();
		Map<String, File> oldJsFiles = new HashMap<String, File>();
		Map<String, File> newJsFiles = new HashMap<String, File>();
		
		
//		Map<String, File> oldAstJsonFiles = new HashMap<String, File>();
//		Map<String, File> newAstJsonFiles = new HashMap<String, File>();
		
		String fileName = null;
		
		Set<String> visited = new HashSet<String>();
		
		for(File f : from.listFiles()) {
			fileName = f.getName();
			System.out.println(fileName);
			
//			if(fileName.contains("test")) continue;
			
			if(fileName.endsWith(".js")) {
				int len = fileName.length();
				fileName = fileName.substring(0, len - 3);
				oldJsFiles.put(fileName, f);
			}
//			else if(fileName.endsWith(".json") && fileName.indexOf("test") < 0) {
			/*
			 * typed-ast json files
			 */
//			else if(fileName.endsWith(".js.ast.json")) {
//				int len = fileName.length();
//				fileName = fileName.substring(0, len - 12);
//				oldAstJsonFiles.put(fileName, f);
//			}
			
			else if(fileName.endsWith(".json")) {
				int len = fileName.length();
				fileName = fileName.substring(0, len - 5);
				oldJsonFiles.put(fileName, f);
			}
		}
		
		for(File f : to.listFiles()) {
			fileName = f.getName();
			
//			if(fileName.contains("test")) continue;
			if(fileName.endsWith(".js")) {
				int len = fileName.length();
				fileName = fileName.substring(0, len - 3);
				newJsFiles.put(fileName, f);
			}
			
			/*
			 * typed-ast json files
			 */
//			else if(fileName.endsWith(".js.ast.json")) {
//				int len = fileName.length();
//				fileName = fileName.substring(0, len - 12);
//				newAstJsonFiles.put(fileName, f);
//			}
		
			else if(fileName.endsWith(".json")) {
				int len = fileName.length();
				fileName = fileName.substring(0, len - 5);
				newJsonFiles.put(fileName, f);
			}
		}
		
		ChangeFact cf = null;
		List<ChangeFact> cfList = new ArrayList<ChangeFact>();
		
		File oJsonFile = null, nJsonFile = null, oJsFile = null, nJsFile = null;
		/*
		 * typed-ast
		 */
//		File oAstJsonFile = null, nAstJsonFile = null;
//		TraverseJson tJoAst = null, tjnAst = null;
		
		TraverseJson tJo = null, tJn = null;
		ITree nTree = null, oTree = null;/*nAstTree = null, oAstTree = null;*/
		try{
			for(Entry<String, File> entry : oldJsonFiles.entrySet()) {
				fileName = entry.getKey();
				visited.add(fileName);
				oJsonFile = entry.getValue();
				oJsFile = oldJsFiles.get(fileName);
//				typed-ast
//				oAstJsonFile = oldAstJsonFiles.get(fileName);
//				String oAstJsonContent = getFileContent(oAstJsonFile);
//				oAstTree = new AntlrJsonTreeGenerator().generateFrom().string(oAstJsonContent).getRoot();
				
				String oJsonContent = getFileContent(oJsonFile);
				oTree = new AntlrJsonTreeGenerator().generateFrom().string(oJsonContent).getRoot();
				tJo = new TraverseJson(oTree, oJsonContent, oJsFile.getName());
//				check AMD
				if(tJo.isAMD == true) return null;
				nJsonFile = newJsonFiles.get(fileName);
				if(nJsonFile != null) {
					nJsFile = newJsFiles.get(fileName);
//					typed-ast
//					nAstJsonFile = newAstJsonFiles.get(fileName);
//					String nAstJsonContent = getFileContent(nAstJsonFile);
//					nAstTree = new AntlrJsonTreeGenerator().generateFrom().string(nAstJsonContent).getRoot();
					
					String nJsonContent = getFileContent(nJsonFile);
					nTree = new AntlrJsonTreeGenerator().generateFrom().string(nJsonContent).getRoot();
					tJn = new TraverseJson(nTree, nJsonContent, nJsFile.getName());
//					check AMD
					if(tJn.isAMD == true) return null;
				}
//				cf = new ChangeFact(oJsFile, nJsFile, tJo, tJn);
//				testOutPut(cf);
				cfList.add(cf);
			}
			
			oJsonFile = null; 
			oJsFile = null;
			tJo = null;
			
			for(Entry<String, File> entry : newJsonFiles.entrySet()) {
				fileName = entry.getKey();
				if(!visited.add(fileName)){
					continue;
				}
				nJsonFile = entry.getValue();
				nJsFile = newJsFiles.get(fileName);
//				typed-ast
//				nAstJsonFile = newAstJsonFiles.get(fileName);
//				String nAstJsonContent = getFileContent(nJsonFile);
//				nAstTree = new AntlrJsonTreeGenerator().generateFrom().string(nAstJsonContent).getRoot();
				
				String nJsonContent = getFileContent(nJsonFile);
				nTree = new AntlrJsonTreeGenerator().generateFrom().string(nJsonContent).getRoot();
				tJn = new TraverseJson(nTree, nJsonContent, nJsFile.getName());
//				check AMD
				if(tJn.isAMD == true) return null;
//				cf = new ChangeFact(oJsFile, nJsFile, tJo, tJn);
				cfList.add(cf);
			}
		} catch (Exception e) {
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

