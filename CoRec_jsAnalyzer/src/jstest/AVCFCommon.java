package jstest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.gumtreediff.tree.ITree;

import vt.edu.graph.ClientFunc;
import vt.edu.graph.ClientVar;

public class AVCFCommon {
//	private ClientFunc cf1, cf2, cf3;
//	
//	public CFCFCommon(ClientFunc cf1, ClientFunc cf2, ClientFunc cf3) {
//		this.cf1 = cf1;
//		this.cf2 = cf2;
//		this.cf3 = cf3;
//	}
	TraverseJsonTyped avTJ;
	
	public AVCFCommon(TraverseJsonTyped avTJ) {
		this.avTJ = avTJ;
	}
	
	public Set<String> getAccessedVarsDecInKnown (ClientFunc cf, ClientVar av) {
		Set<String> invokedVarSet = new HashSet<>();
		String pathOfAV = av.path;
		String pathOfCF = cf.path;
		if (pathOfAV.equals(pathOfCF)) {
			SigGetter sgCF = new SigGetter(cf.typedNode, cf.tJroot, cf.name);
			Set<String> varSigs = sgCF.varSigs;
			for (String var : varSigs) {
				if (var.startsWith(pathOfAV) && !var.equals(av.sig)) 
					invokedVarSet.add(var);
			}
		}
		else {
			String tokens = tokenSig(cf.tJroot, cf.tJroot.expNameToToken.get(cf.name));
			Set<String> varSet = av.tJroot.varSet;
			if (avTJ != null) varSet = avTJ.varSet;
			for(String var : varSet) {
				if (!var.equals(av.name) && tokens.contains(av.path + "++" + var)) {
					invokedVarSet.add(av.path + "++" + var);
				}
			}
		}
		return invokedVarSet;
	}
	
	public static int lcs(List<String> X, List<String> Y, int m, int n)
    {

        // allocate storage for one-dimensional arrays curr and prev
        int[] curr = new int[n + 1];
        int[] prev = new int[n + 1];
 
        // fill the lookup table in bottom-up manner
        for (int i = 0; i <= m; i++)
        {
            for (int j = 0; j <= n; j++)
            {
                if (i > 0 && j > 0)
                {
                    // if current character of X and Y matches
                    if (X.get(i - 1).equals(Y.get(j - 1))) {
                        curr[j] = prev[j - 1] + 1;
                    }
                    // else if current character of X and Y don't match
                    else {
                        curr[j] = Integer.max(prev[j], curr[j - 1]);
                    }
                }
            }
 
            // replace contents of previous array with current array
            System.arraycopy(curr, 0, prev, 0, n);
        }
 
        // LCS will be last entry in the lookup table
        return curr[n];
    }

	public double matchExpTokens(List<String> lExpTokens, List<String> rExpTokens){
		int lsize = lExpTokens.size(), rsize = rExpTokens.size();
		int len = lcs(lExpTokens, rExpTokens, lsize, rsize);
//		System.out.println(len);
		return (double) len * 2 / (double) (lsize + rsize);
	}
	
	
	
//	token similarity!! 
	public int tokenSimilarity(ClientFunc f1, ClientFunc f2) {
		
		return (int) (100 * matchExpTokens(f1.tJroot.expNameToToken.get(f1.name), f2.tJroot.expNameToToken.get(f2.name)));
	}
	
	public boolean sameType(ClientFunc f1, ClientFunc f2) {
		String t1 = typeOfFunc(f1), t2 = typeOfFunc(f2);
		return t1 != null && t2 != null && t1.equals(t2);
	}
	
	public Set<String> getInvokedFuncsAll(ClientFunc cf) {
		Set<String> invokedFuncSet = new HashSet<>();
		ITree root = cf.typedNode;
		
		
		return invokedFuncSet;
	}
	
	
	
	public Set<String> getInvokedFuncsDecInKnown (ClientFunc cf, ClientVar av) {
		Set<String> invokedFuncSet = new HashSet<>();
		String pathOfAV = av.path;
		String pathOfCF = cf.path;
		if (pathOfAV.equals(pathOfCF)) {
			SigGetter sgCF = new SigGetter(cf.typedNode, cf.tJroot, cf.name);
			Set<String> funcSigs = sgCF.funcSigs;
			for (String func : funcSigs) {
				if (func.startsWith(pathOfAV) && !func.equals(av.sig)) 
					invokedFuncSet.add(func);
			}
		}
		else {
			String tokens = tokenSig(cf.tJroot, cf.tJroot.expNameToToken.get(cf.name));
			Set<String> funcSet = av.tJroot.funcSet;
			if (avTJ != null) funcSet = avTJ.funcSet;
			for(String func : funcSet) {
				if (!func.equals(av.name) && tokens.contains(av.path + "++" + func)) {
					invokedFuncSet.add(av.path + "++" + func);
				}
			}
		}
		return invokedFuncSet;
	}
	
//	public boolean hasReturnType(ClientFunc cf, ClientFunc af) {
//		String returnType = getReturnType(af);
//		if (returnType == "null") return true;
//		
//	}
	private String tokenSig(TraverseJsonTyped tJ, List<String> tokens) {
		
		Map<String, String> varToJs = new HashMap<String, String>();
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < tokens.size(); i++) {
			String s = tokens.get(i);
			if(s != null && s.equals(".")) s = "++";
			else if(s.equals("require") && i <= tokens.size() - 4 && tokens.get(i + 1).equals("(") && (i == 0 || !tokens.get(i - 1).equals("."))) {
				String jsFile = tokens.get(i + 2).replace("'", "");
				jsFile = jsFile.replace("\\\"","");
				jsFile = jsFile.replace("\"", "");
				if(jsFile.endsWith(".js")) jsFile = jsFile.replace(".js", "");
				else if(jsFile.endsWith(".json")) jsFile = jsFile.replace(".json", "");
				String getPathSource = null;
				if(jsFile.length() > 0 && jsFile.charAt(0) == '.' || tJ.isNodeJs == true) {
					if (tJ.isNodeJs == true && jsFile.charAt(0) != '.') {
						getPathSource = "lib" + "++" + jsFile.replace("/", "++");
					}
					else getPathSource = TraverseJsonTyped.getPath(tJ.jsFilePath, jsFile);
				}
				
				else getPathSource = jsFile.replace("/", "++");
				
				
				s = getPathSource;
				i += 3;
			}
			
			else if (i >= 2 && i < tokens.size() - 1 && s.equals("new") && tokens.get(i - 1).equals("=") && tJ.importVarToJs.containsKey
					(tokens.get(i + 1))) {
				varToJs.put(tokens.get(i - 2), tJ.importVarToJs.get(tokens.get(i + 1)));
			}
			
					
			else if((i == 0 || (i > 0 && !tokens.get(i - 1).equals("."))) && tJ.importEntityToJs.keySet().contains(s)) s = tJ.importEntityToJs.get(s);
			else if((i == 0 || (i > 0 && !tokens.get(i - 1).equals("."))) && tJ.importVarToJs.keySet().contains(s)) s = tJ.importVarToJs.get(s);
			else if((i == 0 || (i > 0 && !tokens.get(i - 1).equals(".")) || (i > 1 && (tokens.get(i - 2).equals("this") || tokens.get(i - 2).equals("self")) && tokens.get(i - 1).equals(".")))){
				if (tJ.varToClass.containsKey(s)) s = tJ.varToClass.get(s);
				else if (varToJs.containsKey(s)) s= varToJs.get(s);
			}
			sb.append(s);
		}
		
		return sb.toString();
	}
	
	public int commonFuncInvo(ClientFunc f1, ClientFunc f2, ClientVar av) {
		Set<String> setF1 = getInvokedFuncsDecInKnown(f1, av), setF2 = getInvokedFuncsDecInKnown(f2, av);
		setF1.retainAll(setF2);
		return setF1.size();
	}
	
	public boolean sameReturnType(ClientFunc f1, ClientFunc f2) {
		System.out.println(f1.sig + " " + f2.sig + "two functions");
//		System.out.println(f2.tJroot.expNameToToken.get(f2.name));
		System.out.println(getReturnType(f1));
		System.out.println(getReturnType(f2));
		if (getReturnType(f1) != null && getReturnType(f2) != null && getReturnType(f1).equals(getReturnType(f2)))
			return true;
		return false;
	}
	
	
//	type 0 name 1, type same, if any then compare if name same
//	mapSet1 when value is 0, means it's not any type
	public int commonParameters(ClientFunc f1, ClientFunc f2) {
		List<String[]> l1 = getParameters(f1);
		System.out.println(f1.sig + " check " + ConnectChanges.bugName);
		System.out.println(f2.sig + " check " + ConnectChanges.bugName);
		List<String[]> l2 = getParameters(f2);
		Map<String, Integer> map1 = new HashMap<>();
		Map<String, Integer> mapSet1 = new HashMap<>();
		int common = 0;
		for (String[] para : l1) {
			String name = para[1], type = para[0];
			if (!type.equals("any")) {
				map1.put(type, map1.getOrDefault(type, 0) + 1);
				mapSet1.put(name, 0);
			}
			else {
				mapSet1.put(name, 1);
			}
		}
		for (String[] para : l2) {
			String name = para[1], type = para[0];
			if (type.equals("any")) {
				if (mapSet1.containsKey(name) && mapSet1.get(name) == 1) common++;
			}
			else {
				if (map1.containsKey(type) && map1.get(type) > 0){
					common++;
					map1.put(type, map1.get(type) - 1);
				}
			}
		}
		return common;
	}
	
	public int containHowManyParameters(List<String> typesInCF, List<String[]> paras) {
		int result = 0;
//		Map<String, Integer> mapPara = new HashMap<>();
		Map<String, Integer> mapTypes = new HashMap<>();
		for (String type : typesInCF) {
			mapTypes.put(type, mapTypes.getOrDefault(type, 0) + 1);
		}
		for (String[] para : paras) {
			String kind = para[0];
			if (kind.equals("any")) result++;
			else if (mapTypes.containsKey(kind) && mapTypes.get(kind) > 0) {
				result++;
				mapTypes.put(kind, mapTypes.get(kind) - 1);
			}
		}
		
		return result;
	}
	
	public int commonVarAccess(ClientFunc f1, ClientFunc f2, ClientVar av) {
		Set<String> setF1 = getAccessedVarsDecInKnown(f1, av), setF2 = getAccessedVarsDecInKnown(f2, av);
		setF1.retainAll(setF2);
		return setF1.size();
	}
	
	public int commonStatements(ClientFunc f1, ClientFunc f2) {
		
		return 0;
	}
	
	public String typeOfFunc(ClientFunc f) {
//		ITree nodeF = f.typedNode;
		ITree nodeF = f.node;
		TraverseJsonTyped tJF = f.tJroot;
		String type = null;
//		System.out.println(f.sig);
		if (tJF.hasTheField(nodeF, "type")) {
			type = tJF.getFieldName(nodeF, "type");
		}
		return type;
	}
	
	public int similarStatement(ClientFunc f1, ClientFunc f2) {
		int sim = 0;
		ITree treeF1 = f1.node, treeF2 = f2.node;
		TraverseJsonTyped tJ = f1.tJroot;
		Map<String, Integer> stateMap1 = getState(treeF1, tJ), stateMap2 = getState(treeF2, tJ);
		for (String key : stateMap1.keySet()) {
			if (stateMap2.containsKey(key)) {
				int value1 = stateMap1.get(key), value2 = stateMap2.get(key);
				sim += Math.min(value1, value2);
			}
		}
		return sim;
	}
	
	public Map<String, Integer> getState(ITree root, TraverseJsonTyped tJ) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		dfsITree(root, map, tJ);
		return map;
	}
	

	public String hasTheField (ITree node, String name) {
		String type = null;
		for (ITree tmp : node.getChildren()) {
			if (tmp.getChildren().size() < 2) continue;
			if (tmp.getChild(0).toShortString().length() <= 8) continue;
			if(tmp.getChild(0).toShortString().substring(8).equals("\"" + name + "\"")) {
				if (tmp.getChild(1).getChildren().size() < 1) continue;
				return removeQuote(tmp.getChild(1).getChild(0).toShortString().substring(8));
			}
		}
		return type;
	}
	
	public String removeQuote(String s){
		int len = s.length();
		return s.substring(1, len - 1);
	}
	
	
	
	public void dfsITree(ITree root, Map<String, Integer> map, TraverseJsonTyped tJ) {
		if (root == null) return;
		if (root.getChildren().size() < 2) return;
		if (hasTheField(root, "type") != null) {
			String kind = hasTheField(root, "type");
			if (kind.toLowerCase().endsWith("statement") || kind.toLowerCase().endsWith("expression")) {
				map.put(kind, map.getOrDefault(kind, 0) + 1);
			}
		}
		for (ITree child : root.getChildren()) {
			dfsITree(child, map, tJ);
		}
	}
	
	public String typeOfVar(ClientVar v) {
		ITree nodeF = v.typedNode;
		TraverseJsonTyped tJF = v.tJroot;
		String type = null;
//		System.out.println(v.sig);
		if (tJF.hasTheField(nodeF, "type")) {
			type = tJF.getFieldName(nodeF, "type");
		}
		return type;
	}
	
	public String getAVType(ClientVar v) {
		String avType = null;
		TraverseJsonTyped tJV = v.tJroot;
		ITree nodeV = v.typedNode;
		String type = typeOfVar(v);
		switch (type) {
		case "VariableDeclarator" :{
			ITree infer = tJV.multiLevel(nodeV, "id", "inferredType");
			avType = tJV.getFieldName(infer, "kind");
			if (avType.equals("nominative")) {
				ITree fully = tJV.multiLevel(infer, "fullyQualifiedName");
				
				ITree builtinNode = tJV.fieldNameToITree(fully, "builtin");
				String builtin = builtinNode.getChild(1).toShortString().substring(0,5);
				String fullyKind = tJV.getFieldName(fully, "name");
				if (builtin.equals("FALSE")) {
					String fileName = tJV.getFieldName(fully, "fileName");
					fullyKind = fileName + " " + fullyKind;
				}
				avType = fullyKind;
			}
			break;
		}
		
		case "ExpressionStatement" :{
			ITree infer = null;
			if (tJV.hasTheField(nodeV, "expression")){
				ITree expression = tJV.multiLevel(nodeV, "expression");
				if (tJV.hasTheField(expression, "left")) {
					ITree property = tJV.multiLevel(nodeV, "expression", "left", "property");
				
					if (tJV.hasTheField(property, "inferredType")) {
						infer = tJV.multiLevel(nodeV, "expression", "left", "property", "inferredType");
					}
					else infer = tJV.multiLevel(nodeV, "expression", "left", "inferredType");
				}
				else infer = tJV.multiLevel(nodeV, "expression", "property", "inferredType");
			}
			else infer = tJV.multiLevel(nodeV, "inferredType");
			avType = tJV.getFieldName(infer, "kind");
			if (avType.equals("nominative")) {
				ITree fully = tJV.multiLevel(infer, "fullyQualifiedName");
				
				ITree builtinNode = tJV.fieldNameToITree(fully, "builtin");
				String builtin = builtinNode.getChild(1).toShortString().substring(0,5);
				String fullyKind = tJV.getFieldName(fully, "name");
				if (builtin.equals("FALSE")) {
					String fileName = tJV.getFieldName(fully, "fileName");
					fullyKind = fileName + " " + fullyKind;
				}
				avType = fullyKind;
			}
			break;
		}
		
		default : {
			ITree infer = tJV.multiLevel(nodeV, "inferredType");
			avType = tJV.getFieldName(infer, "kind");
			if (avType.equals("nominative")) {
				ITree fully = tJV.multiLevel(infer, "fullyQualifiedName");
				
				ITree builtinNode = tJV.fieldNameToITree(fully, "builtin");
				String builtin = builtinNode.getChild(1).toShortString().substring(0,5);
				String fullyKind = tJV.getFieldName(fully, "name");
				if (builtin.equals("FALSE")) {
					String fileName = tJV.getFieldName(fully, "fileName");
					fullyKind = fileName + " " + fullyKind;
				}
				avType = fullyKind;
			}
			break;
		}
		}
		
		return avType;
	}
	
	public List<String[]> getParameters(ClientFunc f) {
		List<String[]> para = new ArrayList<>();
		String type = typeOfFunc(f);
		
		ITree nodeF = f.typedNode;
		TraverseJsonTyped tJF = f.tJroot;
		switch (type) {
		case "VariableDeclarator": {
			ITree init = tJF.multiLevel(nodeF, "init"), inferred = null;
			
			if (tJF.hasTheField(init, "params")){
				inferred = tJF.multiLevel(init, "params");
			
			
//			List<ITree> callSigsList = tJF.multiLevel(nodeF, "id", "inferredType", "callSignatures").getChildren();
//			ITree callSig = callSigsList.get(0);
				List<ITree> parameterList = inferred.getChildren();
				for (ITree parameter : parameterList) {
					String typeOfPara = tJF.getFieldName(parameter, "type");
					if (typeOfPara.equals("ObjectPattern")) {
						List<ITree> objectParaList = tJF.multiLevel(parameter, "inferredType", "properties").getChildren();
						for (ITree objectPara : objectParaList) {
							para.add(new String[]{tJF.getFieldName(objectPara, "type", "kind"), tJF.getFieldName(objectPara, "name")});
						}
					}
					else if (typeOfPara.equals("RestElement")) {
						para.add(new String[]{"RestElement " + tJF.getFieldName(parameter, "inferredType", "name"), tJF.getFieldName(parameter, "argument", "name")});
					}
					else if (typeOfPara.equals("AssignmentPattern")) {
						String paraType = tJF.getFieldName(parameter, "left", "inferredType", "kind");
						if (paraType.equals("object")) {
							for (ITree proper : tJF.multiLevel(parameter, "left", "inferredType", "properties").getChildren()) {
								para.add(new String[]{tJF.getFieldName(proper, "type", "kind"), tJF.getFieldName(proper, "name")});
							}
							
						}
						else para.add(new String[]{tJF.getFieldName(parameter, "left", "inferredType", "kind"), tJF.getFieldName(parameter, "left", "name")});
					}
					else para.add(new String[]{tJF.getFieldName(parameter, "inferredType", "kind"), tJF.getFieldName(parameter, "name")});
				}
				break; 
				} else {
					ITree infer = tJF.multiLevel(nodeF, "id", "inferredType");
					if (tJF.hasTheField(infer, "callSignatures")) {
						List<ITree> callSigsList = tJF.multiLevel(nodeF, "id", "inferredType", "callSignatures").getChildren();
						if (callSigsList == null || callSigsList.size() == 0) break;
						ITree callSig = callSigsList.get(0);
						List<ITree> parameterList = tJF.multiLevel(callSig, "parameters").getChildren();
						for (ITree parameter : parameterList) {
							para.add(new String[]{tJF.getFieldName(parameter, "type", "kind"), tJF.getFieldName(parameter, "name")});
						}
					}
					break;
				}
			}
			
		case "FunctionDeclaration": {
			
			List<ITree> parameterList = tJF.multiLevel(nodeF, "params").getChildren();
			for (ITree parameter : parameterList) {
				String typeOfPara = tJF.getFieldName(parameter, "type");
				if (typeOfPara.equals("ObjectPattern")) {
					List<ITree> objectParaList = null;
					ITree paraInfer = tJF.multiLevel(parameter, "inferredType");
					if (tJF.hasTheField(paraInfer, "properties")) {
						objectParaList = tJF.multiLevel(parameter, "inferredType", "properties").getChildren();
						for (ITree objectPara : objectParaList) {
							para.add(new String[]{tJF.getFieldName(objectPara, "type", "kind"), tJF.getFieldName(objectPara, "name")});
						}
					}
					else {
						objectParaList = tJF.multiLevel(parameter, "properties").getChildren();
						for (ITree objectPara : objectParaList) {
							para.add(new String[]{tJF.getFieldName(objectPara, "inferredType", "kind"), tJF.getFieldName(objectPara, "key", "name")});
						}
					}
					
				}
				else if (typeOfPara.equals("RestElement")) {
					para.add(new String[]{"RestElement " + tJF.getFieldName(parameter, "inferredType", "name"), tJF.getFieldName(parameter, "argument", "name")});
				}
				else if (typeOfPara.equals("AssignmentPattern")) {
					String leftType = tJF.getFieldName(parameter, "left", "type");
					if (leftType.equals("ObjectPattern")) {
						for (ITree proper : tJF.multiLevel(parameter, "left", "properties").getChildren()) {
							para.add(new String[]{tJF.getFieldName(proper, "inferredType", "kind"), tJF.getFieldName(proper, "key", "name")});
						}
					}
					else {
						String paraType = tJF.getFieldName(parameter, "left", "inferredType", "kind");
						if (paraType.equals("object")) {
							for (ITree proper : tJF.multiLevel(parameter, "left", "inferredType", "properties").getChildren()) {
								para.add(new String[]{tJF.getFieldName(proper, "type", "kind"), tJF.getFieldName(proper, "name")});
							}
							
						}
						else para.add(new String[]{tJF.getFieldName(parameter, "left", "inferredType", "kind"), tJF.getFieldName(parameter, "left", "name")});
					}
				}
				else para.add(new String[]{tJF.getFieldName(parameter, "inferredType", "kind"), tJF.getFieldName(parameter, "name")});
			}
			break;}
		
		case "FunctionExpression" :{
			List<ITree> parameterList = tJF.multiLevel(nodeF, "params").getChildren();
			for (ITree parameter : parameterList) {
				String typeOfPara = tJF.getFieldName(parameter, "type");
				if (typeOfPara.equals("ObjectPattern")) {
					List<ITree> objectParaList = tJF.multiLevel(parameter, "inferredType", "properties").getChildren();
					for (ITree objectPara : objectParaList) {
						para.add(new String[]{tJF.getFieldName(objectPara, "type", "kind"), tJF.getFieldName(objectPara, "name")});
					}
				}
				else if (typeOfPara.equals("RestElement")) {
					para.add(new String[]{"RestElement " + tJF.getFieldName(parameter, "inferredType", "name"), tJF.getFieldName(parameter, "argument", "name")});
				}
				else if (typeOfPara.equals("AssignmentPattern")) {
					String leftType = tJF.getFieldName(parameter, "left", "type");
					if (leftType.equals("ObjectPattern")) {
						for (ITree proper : tJF.multiLevel(parameter, "left", "properties").getChildren()) {
							para.add(new String[]{tJF.getFieldName(proper, "inferredType", "kind"), tJF.getFieldName(proper, "key", "name")});
						}
					}
					else {
						String paraType = tJF.getFieldName(parameter, "left", "inferredType", "kind");
						if (paraType.equals("object")) {
							for (ITree proper : tJF.multiLevel(parameter, "left", "inferredType", "properties").getChildren()) {
								para.add(new String[]{tJF.getFieldName(proper, "type", "kind"), tJF.getFieldName(proper, "name")});
							}
							
						}
						else para.add(new String[]{tJF.getFieldName(parameter, "left", "inferredType", "kind"), tJF.getFieldName(parameter, "left", "name")});
					}
				}
				else para.add(new String[]{tJF.getFieldName(parameter, "inferredType", "kind"), tJF.getFieldName(parameter, "name")});
			}
			break;
		}
		
		case "MethodDefinition": {
			
//			String keyName = tJF.getFieldName(nodeF, "key", "name");
//			if (keyName.equals("constructor")) {
//				
//			}
//			List<ITree> callSigsList = tJF.multiLevel(nodeF, "inferredType", "callSignatures").getChildren();
//			ITree callSig = callSigsList.get(0);
			List<ITree> parameterList = tJF.multiLevel(nodeF, "value", "params").getChildren();
			for (ITree parameter : parameterList) {
				String typeOfPara = tJF.getFieldName(parameter, "type");
				if (typeOfPara.equals("ObjectPattern")) {
					ITree infer = tJF.multiLevel(parameter, "inferredType");
					if (tJF.hasTheField(infer, "properties")) {
						List<ITree> objectParaList = tJF.multiLevel(parameter, "inferredType", "properties").getChildren();
						for (ITree objectPara : objectParaList) {
							para.add(new String[]{tJF.getFieldName(objectPara, "type", "kind"), tJF.getFieldName(objectPara, "name")});
						}
					}
					else {
						List<ITree> objectParaList = tJF.multiLevel(parameter, "properties").getChildren();
						for (ITree objectPara : objectParaList) {
							String name = tJF.getFieldName(objectPara, "key", "name");
							String kind = tJF.getFieldName(objectPara, "inferredType", "kind");
							if (kind.equals("nominative")) {
								ITree fully = tJF.multiLevel(objectPara, "inferredType", "fullyQualifiedName");
								
								ITree builtinNode = tJF.fieldNameToITree(fully, "builtin");
								String builtin = builtinNode.getChild(1).toShortString().substring(0,5);
								String fullyKind = tJF.getFieldName(fully, "name");
								if (builtin.equals("FALSE")) {
									String fileName = tJF.getFieldName(fully, "fileName");
									fullyKind = fileName + " " + fullyKind;
								}
								kind = fullyKind;
							}
							para.add(new String[]{kind, name});
						}
					}
				}
				else if (typeOfPara.equals("RestElement")) {
					para.add(new String[]{"RestElement " + tJF.getFieldName(parameter, "inferredType", "name"), tJF.getFieldName(parameter, "argument", "name")});
				}
				
				else if (typeOfPara.equals("AssignmentPattern")) {
					String leftType = tJF.getFieldName(parameter, "left", "type");
					if (leftType.equals("ObjectPattern")) {
						for (ITree proper : tJF.multiLevel(parameter, "left", "properties").getChildren()) {
							para.add(new String[]{tJF.getFieldName(proper, "inferredType", "kind"), tJF.getFieldName(proper, "key", "name")});
						}
					}
					else {
						String paraType = tJF.getFieldName(parameter, "left", "inferredType", "kind");
						if (paraType.equals("object")) {
							for (ITree proper : tJF.multiLevel(parameter, "left", "inferredType", "properties").getChildren()) {
								para.add(new String[]{tJF.getFieldName(proper, "type", "kind"), tJF.getFieldName(proper, "name")});
							}
							
						}
						else para.add(new String[]{tJF.getFieldName(parameter, "left", "inferredType", "kind"), tJF.getFieldName(parameter, "left", "name")});
					}
				}
				else para.add(new String[]{tJF.getFieldName(parameter, "inferredType", "kind"), tJF.getFieldName(parameter, "name")});
			}
			break;}
		case "Property": {
//			
			List<ITree> parameterList = tJF.multiLevel(nodeF, "value", "params").getChildren();
			for (ITree parameter : parameterList) {
				String typeOfPara = tJF.getFieldName(parameter, "type");
				if (typeOfPara.equals("ObjectPattern")) {
					ITree infer = tJF.multiLevel(parameter, "inferredType");
					if (tJF.hasTheField(infer, "properties")) {
						List<ITree> objectParaList = tJF.multiLevel(parameter, "inferredType", "properties").getChildren();
						for (ITree objectPara : objectParaList) {
							para.add(new String[]{tJF.getFieldName(objectPara, "type", "kind"), tJF.getFieldName(objectPara, "name")});
						}
					}
					else {
						List<ITree> objectParaList = tJF.multiLevel(parameter, "properties").getChildren();
						for (ITree objectPara : objectParaList) {
							String name = tJF.getFieldName(objectPara, "key", "name");
							String kind = tJF.getFieldName(objectPara, "inferredType", "kind");
							if (kind.equals("nominative")) {
								ITree fully = tJF.multiLevel(objectPara, "inferredType", "fullyQualifiedName");
								
								ITree builtinNode = tJF.fieldNameToITree(fully, "builtin");
								String builtin = builtinNode.getChild(1).toShortString().substring(0,5);
								String fullyKind = tJF.getFieldName(fully, "name");
								if (builtin.equals("FALSE")) {
									String fileName = tJF.getFieldName(fully, "fileName");
									fullyKind = fileName + " " + fullyKind;
								}
								kind = fullyKind;
							}
							para.add(new String[]{kind, name});
						}
					}
				}
				else if (typeOfPara.equals("RestElement")) {
					para.add(new String[]{"RestElement " + tJF.getFieldName(parameter, "inferredType", "name"), tJF.getFieldName(parameter, "argument", "name")});
				}
				
				else if (typeOfPara.equals("AssignmentPattern")) {
					String leftType = tJF.getFieldName(parameter, "left", "type");
					if (leftType.equals("ObjectPattern")) {
						for (ITree proper : tJF.multiLevel(parameter, "left", "properties").getChildren()) {
							para.add(new String[]{tJF.getFieldName(proper, "inferredType", "kind"), tJF.getFieldName(proper, "key", "name")});
						}
					}
					else {
						String paraType = tJF.getFieldName(parameter, "left", "inferredType", "kind");
						if (paraType.equals("object")) {
							for (ITree proper : tJF.multiLevel(parameter, "left", "inferredType", "properties").getChildren()) {
								para.add(new String[]{tJF.getFieldName(proper, "type", "kind"), tJF.getFieldName(proper, "name")});
							}
							
						}
						else para.add(new String[]{tJF.getFieldName(parameter, "left", "inferredType", "kind"), tJF.getFieldName(parameter, "left", "name")});
					}
				}
				else para.add(new String[]{tJF.getFieldName(parameter, "inferredType", "kind"), tJF.getFieldName(parameter, "name")});
			}
			break;}
//		default:{
//			List<ITree> parameterList = tJF.multiLevel(nodeF, "value", "params").getChildren();
//			for (ITree parameter : parameterList) {
//				String typeOfPara = tJF.getFieldName(parameter, "type");
//				if (typeOfPara.equals("ObjectPattern")) {
//					ITree infer = tJF.multiLevel(parameter, "inferredType");
//					if (tJF.hasTheField(infer, "properties")) {
//						List<ITree> objectParaList = tJF.multiLevel(parameter, "inferredType", "properties").getChildren();
//						for (ITree objectPara : objectParaList) {
//							para.add(new String[]{tJF.getFieldName(objectPara, "type", "kind"), tJF.getFieldName(objectPara, "name")});
//						}
//					}
//					else {
//						List<ITree> objectParaList = tJF.multiLevel(parameter, "properties").getChildren();
//						for (ITree objectPara : objectParaList) {
//							String name = tJF.getFieldName(objectPara, "key", "name");
//							String kind = tJF.getFieldName(objectPara, "inferredType", "kind");
//							if (kind.equals("nominative")) {
//								ITree fully = tJF.multiLevel(objectPara, "inferredType", "fullyQualifiedName");
//								
//								ITree builtinNode = tJF.fieldNameToITree(fully, "builtin");
//								String builtin = builtinNode.getChild(1).toShortString().substring(0,5);
//								String fullyKind = tJF.getFieldName(fully, "name");
//								if (builtin.equals("FALSE")) {
//									String fileName = tJF.getFieldName(fully, "fileName");
//									fullyKind = fileName + " " + fullyKind;
//								}
//								kind = fullyKind;
//							}
//							para.add(new String[]{kind, name});
//						}
//					}
//				}
//				else if (typeOfPara.equals("RestElement")) {
//					para.add(new String[]{"RestElement " + tJF.getFieldName(parameter, "inferredType", "name"), tJF.getFieldName(parameter, "argument", "name")});
//				}
//				
//				else if (typeOfPara.equals("AssignmentPattern")) {
//					String leftType = tJF.getFieldName(parameter, "left", "type");
//					if (leftType.equals("ObjectPattern")) {
//						for (ITree proper : tJF.multiLevel(parameter, "left", "properties").getChildren()) {
//							para.add(new String[]{tJF.getFieldName(proper, "inferredType", "kind"), tJF.getFieldName(proper, "key", "name")});
//						}
//					}
//					else {
//						String paraType = tJF.getFieldName(parameter, "left", "inferredType", "kind");
//						if (paraType.equals("object")) {
//							for (ITree proper : tJF.multiLevel(parameter, "left", "inferredType", "properties").getChildren()) {
//								para.add(new String[]{tJF.getFieldName(proper, "type", "kind"), tJF.getFieldName(proper, "name")});
//							}
//							
//						}
//						else para.add(new String[]{tJF.getFieldName(parameter, "left", "inferredType", "kind"), tJF.getFieldName(parameter, "left", "name")});
//					}
//				}
//				else para.add(new String[]{tJF.getFieldName(parameter, "inferredType", "kind"), tJF.getFieldName(parameter, "name")});
//			}
//			break;}
		}
		return para;
	}
	
	public void dfsITreeList(ITree root, List<String> list, TraverseJsonTyped tJ) {
		if (root == null) return;
		if (root.getChildren().size() < 2) return;
		if (hasTheField(root, "type") != null) {
			String kind = hasTheField(root, "type");
			if (kind.toLowerCase().endsWith("statement") || kind.toLowerCase().endsWith("expression")) {
				list.add(kind);
			}
		}
		for (ITree child : root.getChildren()) {
			dfsITreeList(child, list, tJ);
		}
	}
	
	public List<String> getStatementList(ITree root, TraverseJsonTyped tJ){
		List<String> stateList = new ArrayList<String>();
		dfsITreeList(root, stateList, tJ);
		return stateList;
	}
	
	public int similarStatementV1(ClientFunc f1, ClientFunc f2) {
		ITree treeF1 = f1.node, treeF2 = f2.node;
		TraverseJsonTyped tJ = f1.tJroot;
		List<String> stateList1 = getStatementList(treeF1, tJ), stateList2 = getStatementList(treeF2, tJ);
		return (int) (100 * matchExpTokens(stateList1, stateList2));
	}
	
//	V2 similarStatement means common similarity, without ordering, percentage
	public int similarStatementV2(ClientFunc f1, ClientFunc f2) {
		ITree treeF1 = f1.node, treeF2 = f2.node;
		TraverseJsonTyped tJ = f1.tJroot;
		List<String> stateList1 = getStatementList(treeF1, tJ), stateList2 = getStatementList(treeF2, tJ);
		int sim = similarStatement(f1, f2);
		return (int) (100 * (double) sim / (stateList1.size() + stateList2.size()));
	}
	
	public String getReturnType(ClientFunc f) {
		String type = typeOfFunc(f);
		System.out.println("What is the type " + type);
		String returnType = null;
		ITree nodeF = f.typedNode;
		TraverseJsonTyped tJF = f.tJroot;
		switch (type) {
		case "VariableDeclarator": {
			ITree inferred = tJF.multiLevel(nodeF, "init", "inferredType");
			boolean hasCallSignatures = tJF.hasTheField(inferred, "callSignatures");
			if (hasCallSignatures) {
			ITree idInferred = tJF.multiLevel(nodeF, "id", "inferredType");
			List<ITree> callSigsList = null;
			if (!tJF.hasTheField(idInferred, "callSignatures")) {
				callSigsList = tJF.multiLevel(nodeF, "init", "inferredType", "callSignatures").getChildren();
			}
			
			else callSigsList = tJF.multiLevel(nodeF, "id", "inferredType", "callSignatures").getChildren();
			
			if (callSigsList == null || callSigsList.size() == 0) {
				returnType = "any";
				break;
			}
			ITree callSig = callSigsList.get(0);
			returnType = tJF.getFieldName(callSig, "returnType", "kind");
			if (returnType.equals("nominative")) {
				ITree fully = tJF.multiLevel(callSig, "returnType", "fullyQualifiedName");
				
				ITree builtinNode = tJF.fieldNameToITree(fully, "builtin");
				String builtin = builtinNode.getChild(1).toShortString().substring(0,5);
				String fullyKind = tJF.getFieldName(fully, "name");
				if (builtin.equals("FALSE")) {
					String fileName = tJF.getFieldName(fully, "fileName");
					fullyKind = fileName + " " + fullyKind;
				}
				returnType = fullyKind;
			}
			break; }
			else {
				break;}
			}
			
		case "FunctionDeclaration": {
			ITree inferred = tJF.multiLevel(nodeF, "inferredType");
			boolean hasCallSignatures = tJF.hasTheField(inferred, "callSignatures");
			if (hasCallSignatures) {
			List<ITree> callSigsList = tJF.multiLevel(nodeF, "inferredType", "callSignatures").getChildren();
			ITree callSig = callSigsList.get(0);
			returnType = tJF.getFieldName(callSig, "returnType", "kind");
			if (returnType.equals("nominative")) {
				ITree fully = tJF.multiLevel(callSig, "returnType", "fullyQualifiedName");
				
				ITree builtinNode = tJF.fieldNameToITree(fully, "builtin");
				String builtin = builtinNode.getChild(1).toShortString().substring(0,5);
				String fullyKind = tJF.getFieldName(fully, "name");
				if (builtin.equals("FALSE")) {
					String fileName = tJF.getFieldName(fully, "fileName");
					fullyKind = fileName + " " + fullyKind;
				}
				returnType = fullyKind;
			}
			break; }
			else {
				break;}
			}
		
		case "MethodDefinition": {
			String inferKind = tJF.getFieldName(nodeF, "inferredType", "kind");
			if (!inferKind.equals("any")) {
				ITree infer = tJF.multiLevel(nodeF, "inferredType");
				if (!tJF.hasTheField(infer, "callSignatures")) {
					returnType = inferKind;
				} 
				else {
					List<ITree> callSigsList = tJF.multiLevel(nodeF, "inferredType", "callSignatures").getChildren();
					if (callSigsList == null || callSigsList.size() == 0) {
						returnType = inferKind;
						List<ITree> propList = tJF.multiLevel(nodeF, "inferredType", "properties").getChildren();
						for (ITree prop : propList) {
							returnType += " " + tJF.getFieldName(prop, "type", "kind");
						}
						break;
					}
					ITree callSig = callSigsList.get(0);
					returnType = tJF.getFieldName(callSig, "returnType", "kind");
					if (returnType.equals("nominative")) {
						ITree fully = tJF.multiLevel(callSig, "returnType", "fullyQualifiedName");
						
						ITree builtinNode = tJF.fieldNameToITree(fully, "builtin");
						String builtin = builtinNode.getChild(1).toShortString().substring(0,5);
						String fullyKind = tJF.getFieldName(fully, "name");
						if (builtin.equals("FALSE")) {
							String fileName = tJF.getFieldName(fully, "fileName");
							fullyKind = fileName + " " + fullyKind;
						}
						returnType = fullyKind;
					}
				}
				break; }
				else {
					break;}
				}
		case "Property": {
//			ITree value = tJF.multiLevel(nodeF, "value");
			ITree inferred = tJF.multiLevel(nodeF, "inferredType");
			boolean hasCallSignatures = tJF.hasTheField(inferred, "callSignatures");
			if (hasCallSignatures) {
			List<ITree> callSigsList = tJF.multiLevel(nodeF, "inferredType", "callSignatures").getChildren();
			ITree callSig = callSigsList.get(0);
			returnType = tJF.getFieldName(callSig, "returnType", "kind");
			if (returnType.equals("nominative")) {
				ITree fully = tJF.multiLevel(callSig, "returnType", "fullyQualifiedName");
				
				ITree builtinNode = tJF.fieldNameToITree(fully, "builtin");
				String builtin = builtinNode.getChild(1).toShortString().substring(0,5);
				String fullyKind = tJF.getFieldName(fully, "name");
				if (builtin.equals("FALSE")) {
					String fileName = tJF.getFieldName(fully, "fileName");
					fullyKind = fileName + " " + fullyKind;
				}
				returnType = fullyKind;
			}
			break; }
			else {
				break;}
			}
		
		default : {
			ITree inferred = tJF.multiLevel(nodeF, "inferredType");
			boolean hasCallSignatures = tJF.hasTheField(inferred, "callSignatures");
			if (hasCallSignatures) {
			List<ITree> callSigsList = tJF.multiLevel(nodeF, "inferredType", "callSignatures").getChildren();
			ITree callSig = callSigsList.get(0);
			returnType = tJF.getFieldName(callSig, "returnType", "kind");
			if (returnType.equals("nominative")) {
				ITree fully = tJF.multiLevel(callSig, "returnType", "fullyQualifiedName");
				
				ITree builtinNode = tJF.fieldNameToITree(fully, "builtin");
				String builtin = builtinNode.getChild(1).toShortString().substring(0,5);
				String fullyKind = tJF.getFieldName(fully, "name");
				if (builtin.equals("FALSE")) {
					String fileName = tJF.getFieldName(fully, "fileName");
					fullyKind = fileName + " " + fullyKind;
				}
				returnType = fullyKind;
			}
			break; 
			} else {
				break;
			}
		}
		}
		return returnType;
	}
	
}

