package jstest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.github.gumtreediff.tree.ITree;

public class SigGetter {
	HashSet<String> varSigs = new HashSet<>();
	HashSet<String> classSigs = new HashSet<>();
	HashSet<String> funcSigs = new HashSet<>();
	Map<String, String> varToClass = new HashMap<>();
	Map<String, String> outerVarToClass;
	ITree astNode;
	TraverseJsonTyped tJ;
	String name;
	
	public SigGetter(ITree astNode, TraverseJsonTyped tJ, String name) {
		this.name = name;
		this.astNode = astNode;
		this.tJ = tJ;
		this.outerVarToClass = tJ.varToClass;
		buildSigs();
	}
	
	
	public void buildSigs() {
		buildSigsSelf();
//		buildSigsOut();
	}
//	
//	 to see the connections in itself
	
	public void buildSigsSelf() {

		List<String> nodeTokens = tJ.expNameToToken.get(name);
		String upper = "";
		if (name.contains("++")) {
			String[] names = name.split("\\+\\+");
			upper = names[0];
		}
		for (int i = 0; i < nodeTokens.size(); i++) {
			/*
			 * deal with an entity directly accesses another entity
			 * 1. directly accesses by its name
			 * 2. using this.(self.)entity, then the signature of this entity is going to be presented as upperClass++entity
			 */
			String token = nodeTokens.get(i);
			if (i == 0 || !nodeTokens.get(i - 1).equals(".") || (i > 1 && nodeTokens.get(i - 2).equals(tJ.moduleName))) {
				if (tJ.varSet.contains(token)) varSigs.add(tJ.jsFilePath + "++" + token);
				else if (tJ.funcSet.contains(token) && i < nodeTokens.size() - 1 &&  nodeTokens.get(i + 1).equals("(")) funcSigs.add(tJ.jsFilePath + "++" + token);
				else if (tJ.classSet.contains(token)) classSigs.add(tJ.jsFilePath + "++" + token);
				
			}
			else if (i >= 2 && (nodeTokens.get(i - 2).equals("self") || nodeTokens.get(i - 2).equals("this") && nodeTokens.get(i - 1).equals("."))) {
				token = upper + "++" + token;
				if (tJ.varSet.contains(token)) varSigs.add(tJ.jsFilePath + "++" + token);
				else if (tJ.funcSet.contains(token) && i < nodeTokens.size() - 1 &&  nodeTokens.get(i + 1).equals("(")) funcSigs.add(tJ.jsFilePath + "++" + token);
				else if (tJ.classSet.contains(token)) classSigs.add(tJ.jsFilePath + "++" + token);
			}
			
			else if (i >= 3 && i < nodeTokens.size() - 2 && token.equals("new") && nodeTokens.get(i - 1).equals("=") && nodeTokens.get(i - 3).equals("var")) {
				String var = nodeTokens.get(i - 2);
				String className = nodeTokens.get(i + 1);
				varToClass.put(var, className);
			}
			
			else if (i >= 2 && varToClass.containsKey(nodeTokens.get(i - 2)) && nodeTokens.get(i - 1).equals(".")) {
				token = varToClass.get(nodeTokens.get(i - 2)) + "++" + token;
				if (tJ.varSet.contains(token)) varSigs.add(tJ.jsFilePath + "++" + token);
				else if (tJ.funcSet.contains(token) && i < nodeTokens.size() &&  nodeTokens.get(i + 1).equals("(")) funcSigs.add(tJ.jsFilePath + "++" + token);
				else if (tJ.classSet.contains(token)) classSigs.add(tJ.jsFilePath + "++" + token);
			}
			
			else if (i >= 2 && outerVarToClass.containsKey(nodeTokens.get(i - 2)) && nodeTokens.get(i - 1).equals(".")) {
				token = outerVarToClass.get(nodeTokens.get(i - 2)) + "++" + token;
				if (tJ.varSet.contains(token)) varSigs.add(tJ.jsFilePath + "++" + token);
				else if (tJ.funcSet.contains(token) && i < nodeTokens.size() - 1 &&  nodeTokens.get(i + 1).equals("(")) funcSigs.add(tJ.jsFilePath + "++" + token);
				else if (tJ.classSet.contains(token)) classSigs.add(tJ.jsFilePath + "++" + token);
			}
			
			else if (i >= 2 && (tJ.classSet.contains(nodeTokens.get(i - 2)) || tJ.funcSet.contains(nodeTokens.get(i - 2))) && nodeTokens.get(i - 1).equals(".")) {
				token = nodeTokens.get(i - 2) + "++" + token;
				if (tJ.varSet.contains(token)) varSigs.add(tJ.jsFilePath + "++" + token);
				else if (tJ.funcSet.contains(token) && i < nodeTokens.size() - 1 &&  nodeTokens.get(i + 1).equals("(")) funcSigs.add(tJ.jsFilePath + "++" + token);
				else if (tJ.classSet.contains(token)) classSigs.add(tJ.jsFilePath + "++" + token);
			}
			/*
			 * need to deal with a second layer connection, var(or no var) x = new upper();, then using 
			 * x.entity to invoke(access) the entity.(done when the var was declared in the same entity, let's check the outsider)
			 * done when the var is declared globally.
			 */
			
		}
	}
}

