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

public class ChangeExtracterClient2 {
	
	/*
	 * Here if a js file is in AMD style, then we return a null list, which is OK!
	 */
	List<TraverseJsonTyped> oldTJ = new ArrayList<TraverseJsonTyped>();
	public List<ChangeFactNode> parseChanges(String filePath) throws Exception {
		
		File commit = new File(filePath);
		File from = new File(commit.getAbsolutePath() + "/from");
		File to = new File(commit.getAbsolutePath() + "/to");
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
		
		ChangeFactNode cf = null;
		List<ChangeFactNode> cfList = new ArrayList<ChangeFactNode>();
		
		File oJsFile = null, nJsFile = null;

		
		TraverseJsonTyped tJo = null, tJn = null;
		ITree nTree = null, oTree = null;
		try{
			for(Entry<String, File> entry : oldJsFiles.entrySet()) {
				fileName = entry.getKey();
				visited.add(fileName);
				oJsFile = oldJsFiles.get(fileName);
				jsToJson jJ = new jsToJson(oJsFile.getAbsolutePath());
				String oJsonContent = jJ.cmdForEs();
				if (oJsonContent.length() == 0) continue;
				oTree = new AntlrJsonTreeGenerator().generateFrom().string(oJsonContent).getRoot();
				tJo = new TraverseJsonTyped(null, oJsFile.getName(), oTree);
//				check AMD
				if(tJo.isAMD == true) return null;
				oldTJ.add(tJo);
				nJsFile = newJsFiles.get(fileName);
				if(nJsFile != null) {
					jJ = new jsToJson(nJsFile.getAbsolutePath());				
					String nJsonContent = jJ.cmdForEs();
					if(nJsonContent.length() == 0) continue;
					nTree = new AntlrJsonTreeGenerator().generateFrom().string(nJsonContent).getRoot();
					tJn = new TraverseJsonTyped(null, nJsFile.getName(), nTree);
//					check AMD
					if(tJn.isAMD == true) return null;
				}
				cf = new ChangeFactNode(oJsFile, nJsFile, tJo, tJn);
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
				tJn = new TraverseJsonTyped(null, nJsFile.getName(), nTree);
//				check AMD
				if(tJn.isAMD == true) return null;
				cf = new ChangeFactNode(oJsFile, nJsFile, tJo, tJn);
				cfList.add(cf);
			}
		} catch (Exception e) {
			System.out.println("This is too big");
			e.printStackTrace();
		}
		return cfList;
	}
	
	private void testOutPut(ChangeFactNode cf){
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

