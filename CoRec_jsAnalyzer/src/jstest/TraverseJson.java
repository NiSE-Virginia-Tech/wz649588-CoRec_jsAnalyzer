package jstest;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.github.gumtreediff.tree.ITree;

public class TraverseJson {
//	jsFilePath should be in the form of package1++package2++name.js
	boolean isAMD = false;
	boolean isNodeJs = false;
	String jsFilePath;
	String jsonFile;
	ITree jsonTree;
	
//	typedAST
	
	
	HashMap<String, ITree> nameToITree = new HashMap<String, ITree>();
	HashMap<String, List<String>> expNameToToken = new HashMap<>();
//	HashMap<String, List<String>> nameToToken = new HashMap<>();
	HashSet<ITree> entities = new HashSet<>();
	HashSet<String> varSet = new HashSet<>();
	HashSet<String> funcSet = new HashSet<>();
	HashSet<String> classSet = new HashSet<>();
	HashSet<String> expSet = new HashSet<>();
	HashSet<String> importSet = new HashSet<>();
	HashSet<String> exportSet = new HashSet<>();
//	HashSet<String> importVar = new HashSet<>();
//	deal with import things, for require + import * as ...(folder or js files)
	HashMap<String, String> importVarToJs = new HashMap<String, String>();
//	deal with import things, for import statement (function, var, class), key: local, value: js ++ name 
	HashMap<String, String> importEntityToJs = new HashMap<String, String>();
//	HashMap<String, List<Integer>> varToDefPos = new HashMap<>();
//	HashMap<String, List<ITree>> varToDefTree = new HashMap<>();
	HashMap<String, String> importIndexCheck = new HashMap<String, String>();
	
	
	ITree exportFunc = null;
	String exportVarName = "";
	HashSet<String> exportVarNames = new HashSet<>();
	HashMap<String, String> superClassFinder = new HashMap<>();
	
	
	List<Integer> tokenStartPos = new ArrayList<Integer>();
	List<String> tokens = new ArrayList<String>();
	
	/*
	 * Added on June 10 for typedast
	 * astJsonTree, which almost has the same thing as jsonTree
	 * plus the inferred type and loc
	 */
//	HashMap<String, ITree> astNameToITree = new HashMap<>();
//	List<ITree> astJsonEntities = new ArrayList<ITree>();
	List<ITree> jsonEntities = new ArrayList<ITree>();
	
	
//	public TraverseJson(ITree jsonTree, String jsonFile, String jsFilePath){
	public TraverseJson(ITree jsonTree, String jsonFile, String jsFilePath){
		this.jsonTree = jsonTree;
		this.jsonFile = jsonFile;
		this.jsFilePath = jsFilePath.replace(".js", "");
//		this.astJsonTree = astJsonTree;
//		this.jsContent = jsContent;
		buildTokenLists();
//		astJsonEntities = astJsonTree.getChild(1).getChild(1).getChildren();
		jsonEntities = jsonTree.getChild(1).getChild(1).getChildren();
		for(int i = 0; i < jsonEntities.size(); i++){
			ITree entity = jsonEntities.get(i);
//			ITree astEntity = astJsonEntities.get(i);
			entities.add(entity);
			buildEntity(entity);
		}
//		dealWithExportFunc(exportFunc);
		checkAMD();
	}
	
//	public void dealWithExportFunc(ITree entity) {
//		if(entity != null) {
//			
//		}
//	}
	
//	public String getFuncDetail(ITree entity, ITree astEntity) {
//		StringBuilder sb = new StringBuilder()
//	}
	
	public void dealWithExportEntity(ITree entity) {
		ITree exportEntity = entity.getChild(1).getChild(1);
//		ITree exportAstEntity = astEntity.getChild(1).getChild(1);
		String export =exportEntity.toShortString();
		if(!export.startsWith("NULL")) {
			String exportType = getEntityKind(exportEntity);
			if(exportType.contains("Declaration")){
				buildEntity(exportEntity);
			}
			else if(exportType.contains("ObjectExpression")) {
				dealWithObjectExport(exportEntity);
			}
		}
	}
	
	public boolean dealWithObjectExport(ITree export) {
		ITree propertyArray = export.getChild(1).getChild(1);
		boolean hasExport = false;
		for(ITree property : propertyArray.getChildren()) {
			System.out.println(property.toShortString());
			ITree key = fieldNameToITree(property, "key");
			ITree value = fieldNameToITree(property, "value");
			String keyName = removeQuote(key.getChild(1).getChild(1).getChild(1).getChild(0).toShortString().substring(8));
			System.out.println(keyName);
			String valueType = removeQuote(value.getChild(1).getChild(0).getChild(1).getChild(0).toShortString().substring(8));
			if(valueType.contains("Function")) {
				funcSet.add(keyName);
				nameToITree.put(keyName, property);
//				Added to check 
				expNameToToken.put(keyName,getFullTokenList(property));
				hasExport = true;
			}
			else if(valueType.contains("Class")) {
				classSet.add(keyName);
				nameToITree.put(keyName, property);
				expNameToToken.put(keyName, getFullTokenList(property));
				hasExport = true;
			}
			else if(!valueType.equals("Identifier")){
				varSet.add(keyName);
				nameToITree.put(keyName, property);
				expNameToToken.put(keyName, getFullTokenList(property));
				hasExport = true;
			}
			
		}
		return hasExport;
	}
	
	public ITree fieldNameToITree (ITree node, String name) {
		ITree result = null;
		for(ITree tmp : node.getChildren()) {
			if(tmp.getChild(0).toShortString().substring(8).equals("\"" + name + "\"")) {
				result = tmp;
				return result;
			}
		}
		return result;
	}
	
	public boolean hasTheField (ITree node, String name) {
		for (ITree tmp : node.getChildren()) {
			if(tmp.getChild(0).toShortString().substring(8).equals("\"" + name + "\"")) return true;
		}
		return false;
	}
	
	
	public void dealWithImportEntity (ITree entity) {
		List<String> importList = new ArrayList<>();
		ITree importArray = entity.getChild(1).getChild(1);
		List<ITree> imports = importArray.getChildren();
		String sourceName = entity.getChild(2).getChild(1).getChild(1).getChild(1)
				.getChild(0).toShortString().substring(8).replace("\"", "");
		if(sourceName.endsWith(".js")) sourceName = sourceName.replace(".js", "");
		else if (sourceName.endsWith(".json")) sourceName = sourceName.replace(".json", "");
		String sourcePath = getPath(jsFilePath, sourceName);
		
		for(int i = 0; i < imports.size(); i++){
			ITree importEntity = imports.get(i);
			String localName = importEntity.getChild(1).getChild(1).getChild(1).getChild(1).
					getChild(0).toShortString().substring(8).replace("\"", "");
			if(!hasTheField(importEntity, "imported")) {
				importVarToJs.put(localName, sourcePath);
				importIndexCheck.put(localName,  sourcePath + "++" + "index");
			}
			else {
				String importedName = importEntity.getChild(2).getChild(1).getChild(1).getChild(1).
						getChild(0).toShortString().substring(8).replace("\"", "");
				importEntityToJs.put(localName, sourcePath + "++" + importedName);
				importEntityToJs.put(localName, sourcePath + "++" + "index" + "++" +
									importedName);
			}
			
		}
	}
	
	
	public void buildEntity(ITree entity) {
		switch (getEntityKind(entity)) {
		case "\"ImportDeclaration\"":
//			Things to de about import
//			put into the varToJsFile map
			dealWithImportEntity (entity);
			break;
		case "\"ExportNamedDeclaration\"":
//			Things to do with export
			dealWithExportEntity(entity);
			break;
		case "\"ExportAllDeclaration\"":
//			Things to do with export
			break;
		case "\"ExportDefaultDeclaration\"":
//			Things to do with export
			dealWithExportEntity(entity);
			break;
		case "\"VariableDeclaration\"":
			for(String var : getVariableName(entity)){
				varSet.add(var);
			}
			break;
		case "\"FunctionDeclaration\"":
			String nameOfFunction = getFunctionName(entity);
			funcSet.add(nameOfFunction);
			nameToITree.put(nameOfFunction, entity);
//			astNameToITree.put(nameOfFunction, astEntity);
//			Added to check 
			expNameToToken.put(nameOfFunction,getFullTokenList(entity));
			break;
		case "\"ClassDeclaration\"":
			String nameOfClass = getClassName(entity);
			classSet.add(nameOfClass);
			nameToITree.put(nameOfClass, entity);
//			astNameToITree.put(nameOfClass, astEntity);
//			Added to check 
			expNameToToken.put(nameOfClass,getFullTokenList(entity));
			superClassFinder.put(nameOfClass, getSuperClass(entity));
			break;
		default:
			String expName = getExpName(entity);
			
			if(expName.startsWith("\"ExpressionStatement\"")) {
				expName = getInnerExpName(entity);
				entity = entity.getChild(1).getChild(1);
			}
//			if(expName.startsWith("\"AssignmentExpression\"") && !isExport(entity)) {
//				String left = getNameOfAssignLeft(entity);
//				if(left != null) {
//					System.out.println(left);
//					if(varSet.contains(left)) {
//						if(!varToDefPos.containsKey(left)) {
//							varToDefPos.put(left, new ArrayList<Integer>());
//							varToDefTree.put(left, new ArrayList<ITree>());
//						}
//						varToDefPos.get(left).add(getObjStartPos(entity));
//						varToDefTree.get(left).add(entity);
//					}
//				}
//			}
			/*
			 * for test
			 */
//			System.out.println(expName);
//			System.out.println(isExport(entity));
			
			if(!(expName.startsWith("\"AssignmentExpression\"") && isExport(entity)) && !expName.startsWith("\"Literal\"")){
				if(expName.startsWith("\"AssignmentExpression\"")) {
					ITree left = fieldNameToITree(entity, "left");
					ITree typeOfLeft = fieldNameToITree(left.getChild(1), "type");
					String entityType = removeQuote(typeOfLeft.getChild(1).getChild(0).toShortString().substring(8));
					if(entityType.equals("Identifier")) {
						ITree nameOfLeft = fieldNameToITree(left.getChild(1), "name");
						String entityName = removeQuote(nameOfLeft.getChild(1).getChild(0).toShortString().substring(8));
						if(!nameToITree.containsKey(entityName)) {
							ITree right = fieldNameToITree(entity, "right");
							ITree typeOfRight = fieldNameToITree(right.getChild(1), "type");
							String rightType = removeQuote(typeOfRight.getChild(1).getChild(0).toShortString().substring(8));
							if(rightType.equals("FunctionExpression")) {
								funcSet.add(entityName);
								nameToITree.put(entityName, entity);
								expNameToToken.put(entityName, getFullTokenList(entity));
							}
							else if(rightType.equals("ClassExpression")) {
								classSet.add(entityName);
								nameToITree.put(entityName, entity);
								expNameToToken.put(entityName, getFullTokenList(entity));
							}
							
							else {
								varSet.add(entityName);
								nameToITree.put(entityName, entity);
								expNameToToken.put(entityName,getFullTokenList(entity));
							}
							
						}
						else {
							expSet.add(expName);
							nameToITree.put(entityName, entity);
							expNameToToken.put(entityName,getFullTokenList(entity));
						}
					}
				}
				else {
				
					expSet.add(expName);
					nameToITree.put(expName, entity);
	//				astNameToITree.put(expName, astEntity);
					expNameToToken.put(expName, getExpTokenList(entity));
				}
			}
		}
	}
	
	public String getNameOfAssignLeft(ITree entity) {
		ITree leftNode = fieldNameToITree(entity, "left");
		ITree nameNode = fieldNameToITree(leftNode.getChild(1), "name");
		if(nameNode == null) return null;
		return leftNode.getChild(1).getChild(1).getChild(1).getChild(0).toShortString().substring(8).replace("\"", "");
	}
	
	public String removeQuote(String s){
		int len = s.length();
		return s.substring(1, len - 1);
	}
	
	public void buildTokenLists(){
		int pos = jsonTree.getChildren().size();
		ITree tokenTree = jsonTree.getChild(pos - 1).getChild(1);
		for(ITree token : tokenTree.getChildren()){
			String value = token.getChild(1).getChild(1).getChild(0).toShortString().substring(8);
			value = removeQuote(value);//value.replace("\"", "");
			value = value.replace("\\\"","\"");
			tokens.add(value);
			int tokenPos = getObjStartPos(token);
			int end = getObjEndPos(token);
			tokenStartPos.add(tokenPos);
//			System.out.println(value + " " + tokenPos + " " + end);
		}
	}
	
	public int getObjStartPos(ITree Obj){
		ITree range = Obj.getChild(Obj.getChildren().size() - 1);
//		System.out.println(range.getChild(0).toShortString().substring(8).equals("\"range\""));
		if(!range.getChild(0).toShortString().substring(8).equals("\"range\"")) {
			for(ITree tmp : Obj.getChildren()) {
				if(tmp.getChild(0).toShortString().substring(8).equals("\"range\"")) {
					range = tmp;
					break;
				}
			}
		}
		String pos = range.getChild(1).getChild(0).getChild(0).toShortString().substring(8);
		return Integer.valueOf(pos);
	}
	
	public int getObjEndPos(ITree Obj){
		ITree range = Obj.getChild(Obj.getChildren().size() - 1);
		if(!range.getChild(0).toShortString().substring(8).equals("\"range\"")) {
			for(ITree tmp : Obj.getChildren()) {
				if(tmp.getChild(0).toShortString().substring(8).equals("\"range\"")) {
					range = tmp;
					break;
				}
			}
		}
		String pos = range.getChild(1).getChild(1).getChild(0).toShortString().substring(8);
		return Integer.valueOf(pos);
	}
	
	public String getEntityKind(ITree entity){
		return entity.getChild(0).getChild(1).getChild(0).toShortString().substring(8);
	}
	
	public HashSet<ITree> getEntities() {
		return entities;
	}
	
	public HashSet<String> getEntityString() {
		HashSet<String> entityString = new HashSet<>();
		for(ITree entity : entities){
			 entityString.add(jsonFile.substring(entity.getPos(), entity.getEndPos()));
		}
		return entityString;
	}
	
	public String getFunctionName(ITree entity){
		StringBuilder sb = new StringBuilder();
//		System.out.println(entity.getChild(1).getChild(1).getChild(1).getChild(1).getChild(0).toShortString().substring(8));
		sb.append(entity.getChild(1).getChild(1).getChild(1).getChild(1).getChild(0).toShortString().substring(8).replace("\"", ""));
//		sb.append("(");
//		List<String> param = new ArrayList<String>();
//		for(ITree paramTree : entity.getChild(2).getChild(1).getChildren()){
////			System.out.println(jsFilePath + paramTree.getChild(1).getChild(1).getChild(0).toShortString());
//			String paraName = paramTree.getChild(1).getChild(1).getChild(0).toShortString().substring(8);
//			param.add(paraName);
//			sb.append(paraName + ",");
//		}
//		if(param.size() > 0) {
//			sb.delete(sb.length() - 1, sb.length());
//		}
//		sb.append(")");
		return sb.toString();
	}
	
	public List<String> getVariableName(ITree entity){
		List<String> varList = new ArrayList<>();
		ITree varArray = entity.getChild(1).getChild(1);
//		ITree astVarArray = astEntity.getChild(1).getChild(1);
		List<ITree> vars = varArray.getChildren();
//		List<ITree> astVars = astVarArray.getChildren();
		for(int i = 0; i < vars.size(); i++){
			ITree var = vars.get(i);
//			ITree astVar = astVars.get(i);
			String varName = var.getChild(1).getChild(1).getChild(1).getChild(1).getChild(0).toShortString().substring(8).replace("\"", "");
			ITree idTree = var.getChild(1);
			if (!hasTheField(idTree.getChild(1), "name") || varName.equals("")) {
				isVarImport(var, varName);
				continue;
			}
			
			
			if(isVarImport(var, varName)){
//				System.out.println(varName + " is an import");
//				importVar.add(varName);
				continue;
			}
			if(isVarFunction(var)) {
				funcSet.add(varName);
				nameToITree.put(varName, var);
//				astNameToITree.put(varName, astVar);
//				Added to check 
				expNameToToken.put(varName,getFullTokenList(var));
				continue;
			}
			if(isVarClass(var, varName)) {
				classSet.add(varName);
				nameToITree.put(varName, var);
//				astNameToITree.put(varName, astVar);
//				Added to check 
				expNameToToken.put(varName,getFullTokenList(var));
				
				continue;
			}	
			varList.add(varName);
			nameToITree.put(varName, var);
//			astNameToITree.put(varName, astVar);
//			Added to check 
			expNameToToken.put(varName,getFullTokenList(var));
		}
		return varList;
	}
	
	public String getClassName(ITree entity){
		return entity.getChild(1).getChild(1).getChild(1).getChild(1).getChild(0).toShortString().substring(8).replace("\"", "");
	}
	
	public String getExpName(ITree entity){
		if (getExpTokenList(entity).size() > 0)
			return getEntityKind(entity) + " " + getObjStartPos(entity) + " " + getExpTokenList(entity).get(0);
		else return getEntityKind(entity) + " " + getObjStartPos(entity);
	}
	
	public String getInnerExpName(ITree entity){
		if (getExpTokenList(entity).size() > 0)
			return entity.getChild(1).getChild(1).getChild(0).getChild(1).getChild(0).toShortString().substring(8) + " " + getObjStartPos(entity) + 
				" " + getExpTokenList(entity).get(0);
		else return getEntityKind(entity) + " " + getObjStartPos(entity);
	}
	
	
	
	List<String> getExpTokenList(ITree expTree){
		List<String> expTokenList = new ArrayList<String>();
		int start = getObjStartPos(expTree), end = getObjEndPos(expTree);
		int startIndex = Collections.binarySearch(tokenStartPos, start);
//		System.out.println("start at " + startIndex);
		for(int i = startIndex; i < tokenStartPos.size() && tokenStartPos.get(i) < end; i++){
			if("(){},;[]<>\"'".contains(tokens.get(i))) continue;
			expTokenList.add(tokens.get(i));
//			System.out.println(tokens.get(i));
		}
		return expTokenList;
	}
	
	List<String> getFullTokenList(ITree tree) {
		List<String> fullTokenList = new ArrayList<String>();
		int start = getObjStartPos(tree), end = getObjEndPos(tree);
		int startIndex = Collections.binarySearch(tokenStartPos, start);
		for(int i = startIndex; i < tokenStartPos.size() && tokenStartPos.get(i) < end; i++){
			if("{},;".contains(tokens.get(i))) continue;
			fullTokenList.add(tokens.get(i));
//			System.out.println(tokens.get(i));
		}
		return fullTokenList;
	}
	
	public boolean isVarImport(ITree entity, String varName) {
		List<String> checkToken = getFullTokenList(entity);
//		for(String s : checkToken) {
//			System.out.println(s);
//		}
		int i = checkToken.indexOf("require");
		if(i < 0) return false;

		if(i > 0 && i < checkToken.size() && checkToken.get(i - 1).equals("=") && checkToken.get(i + 1).equals("(")) {
			
			ITree init = fieldNameToITree(entity, "init");
//			if(init == null) return false;
			ITree type = fieldNameToITree(init.getChild(1), "type");
//			if(type == null) return false;
			String typeName = removeQuote(type.getChild(1).getChild(0).toShortString().substring(8));
			if(typeName.equals("CallExpression")) {
				String jsFile = null;
				if(checkToken.contains("path") && checkToken.contains("join") && checkToken.contains("__dirname")) {
					int dirIndex = checkToken.indexOf("__dirname");
					StringBuilder sb = new StringBuilder();
				
					for(int j = dirIndex + 1; j < checkToken.size(); j++) {
						String dir = checkToken.get(j).replace("\\\"","").replace("\"", "").replace("'", "");
						if(dir.equals(")")) break;
						if(!dir.equals(",")) {
							sb.append(dir);
							sb.append("/");
						}
					}
					sb.delete(sb.length() - 1, sb.length());
					jsFile = "./" + sb.toString();
					if(jsFile.endsWith(".js")) jsFile = jsFile.replace(".js", "");
					else if(jsFile.endsWith(".json")) jsFile = jsFile.replace(".json", "");
				}
				
				
				else {
					jsFile = checkToken.get(i + 2).replace("'", "");
				
	//				jsFile = jsFile.replace("\"", "");
					jsFile = jsFile.replace("\\\"","");
					jsFile = jsFile.replace("\"", "");
					System.out.println(jsFile + "This is important");
					if(jsFile.endsWith(".js")) jsFile = jsFile.replace(".js", "");
					else if(jsFile.endsWith(".json")) jsFile = jsFile.replace(".json", "");
				}
//				System.out.println(jsFile.charAt(0));
				/*
				 * deal with @electron or storybook
				 */
				
//				jsFile = jsFile.replace("/", "++");
				
				if(jsFile.charAt(0) == '@') {
					jsFile = jsFile.replace("@electron/internal", "lib");
					jsFile = jsFile.replace("/", "++");
					System.out.println(jsFile);
					importVarToJs.put(varName, jsFile);
					importIndexCheck.put(varName, jsFile + "++" + "index");
				}
				
				else if(jsFile.charAt(0) == '.' || isNodeJs == true){
					System.out.println(varName);
					System.out.println(getPath(jsFilePath, jsFile));
					String getPathSource = null;
					if(isNodeJs == true && jsFile.charAt(0) != '.') {
						getPathSource = "lib" + "++" + jsFile.replace("/", "++");
					}
					
					else getPathSource = getPath(jsFilePath, jsFile);
					
					if(hasTheField(entity.getChild(1).getChild(1), "properties")) {
						ITree propertyArray = entity.getChild(1).getChild(1).getChild(1).getChild(1);
						for(ITree property : propertyArray.getChildren()) {
							System.out.println(property.toShortString());
							ITree key = fieldNameToITree(property, "key");
							ITree value = fieldNameToITree(property, "value");
							String keyName = removeQuote(key.getChild(1).getChild(1).getChild(1).getChild(0).toShortString().substring(8));
							System.out.println(keyName);
							String valueName = removeQuote(value.getChild(1).getChild(1).getChild(1).getChild(0).toShortString().substring(8));
							importEntityToJs.put(keyName, getPathSource + "++" + valueName);
							importIndexCheck.put(keyName, getPathSource + "++" + "index" + "++" + valueName);
						}
					}
					
					else if(!varName.equals("")) {
						importVarToJs.put(varName, getPathSource);
						importIndexCheck.put(varName, getPathSource +  "++"  + "index");
					}
					
				}
				
				return true;
			}
			
			else if(typeName.equals("MemberExpression")) {
				int indexOfDot = checkToken.indexOf(".");
				String nameOfEntity = checkToken.get(indexOfDot + 1);
				String jsFile = null;
				if(checkToken.contains("path") && checkToken.contains("join") && checkToken.contains("__dirname")) {
					int dirIndex = checkToken.indexOf("__dirname");
					StringBuilder sb = new StringBuilder();
				
					for(int j = dirIndex + 1; j < indexOfDot; j++) {
						String dir = checkToken.get(j).replace("\\\"","").replace("\"", "").replace("'", "");
						if(dir.equals(")")) break;
						if(!dir.equals(",")) {
							sb.append(dir);
							sb.append("/");
						}
					}
					sb.delete(sb.length() - 1, sb.length());
					jsFile = "./" + sb.toString();
					if(jsFile.endsWith(".js")) jsFile = jsFile.replace(".js", "");
					else if(jsFile.endsWith(".json")) jsFile = jsFile.replace(".json", "");
				}
				
				else {
					jsFile = checkToken.get(i + 2).replace("'", "");
				
	//				jsFile = jsFile.replace("\"", "");
					jsFile = jsFile.replace("\\\"","");
					jsFile = jsFile.replace("\"", "");
					System.out.println(jsFile + "This is important");
					if(jsFile.endsWith(".js")) jsFile = jsFile.replace(".js", "");
					else if(jsFile.endsWith(".json")) jsFile = jsFile.replace(".json", "");
				}
				
//				jsFile = jsFile.replace("/", "++");
				
				if(jsFile.charAt(0) == '@') {
					jsFile = jsFile.replace("@electron/internal", "lib");
					jsFile = jsFile.replace("/", "++");
					System.out.println(jsFile + "++" + nameOfEntity);
					importEntityToJs.put(varName, jsFile + "++" + nameOfEntity);
					importIndexCheck.put(varName, jsFile + "++" + "index++" + nameOfEntity);
				}
				
				else if(jsFile.charAt(0) == '.'){
					System.out.println(varName);
					System.out.println(getPath(jsFilePath, jsFile) + "++" + nameOfEntity);
					importEntityToJs.put(varName, getPath(jsFilePath, jsFile) + "++" + nameOfEntity);
					importIndexCheck.put(varName, getPath(jsFilePath, jsFile) + "++" + "index++" + nameOfEntity);
				}
				
				return true;
				
			}
			
			return false;
			
//			else if(typeName.equals("Member"))
		}
		
		
//		if(i > 0 && i < checkToken.size() && checkToken.get(i - 1).equals("=") && checkToken.get(i + 1).equals("(")
//				&& checkToken.size() <= i + 5){
//			String jsFile = checkToken.get(i + 2).replace("'", "");
////			jsFile = jsFile.replace("\"", "");
//			jsFile = jsFile.replace("\\\"","");
//			jsFile = jsFile.replace("\"", "");
//			System.out.println(jsFile + "This is important");
//			if(jsFile.endsWith(".js")) jsFile = jsFile.replace(".js", "");
//			else if(jsFile.endsWith(".json")) jsFile = jsFile.replace(".json", "");
////			System.out.println(jsFile.charAt(0));
//			/*
//			 * deal with @electron or storybook
//			 */
//			if(jsFile.charAt(0) == '@') {
//				jsFile = jsFile.replace("@electron/internal", "lib");
//				jsFile = jsFile.replace("/", "++");
//				System.out.println(jsFile);
//				importVarToJs.put(varName, jsFile);
//			}
//			
//			else if(jsFile.charAt(0) == '.'){
//				System.out.println(varName);
//				System.out.println(getPath(jsFilePath, jsFile));
//				importVarToJs.put(varName, getPath(jsFilePath, jsFile));
//			}
//			return true;
//		}
		return false;
	}
	
	static String getPath(String currentFile, String jsFile){
		StringBuilder sb = new StringBuilder();
		String[] currentFilePath = currentFile.split("\\+\\+");
		int len = currentFilePath.length - 1;
		String[] relativePath = jsFile.split("/");
		int start = 0;
		if(relativePath[start].equals(".")) start++;
		for(;start < relativePath.length; start++) {
			if(!relativePath[start].equals("..")) break;
			len--;
		}
		for(int i = 0; i < len; i++){
			sb.append(currentFilePath[i]);
			sb.append("++");
		}
		for(int i = start; i < relativePath.length; i++) {
			sb.append(relativePath[i]);
			if(i < relativePath.length - 1) sb.append("++");
		}
		
		return sb.toString();
		
	}
	
	public boolean isVarFunction(ITree var) {
		ITree init = var.getChild(2);
		if(!init.getChild(0).toShortString().substring(8).equals("\"init\"")) {
			for(ITree tmp : var.getChildren()) {
				if(tmp.getChild(0).toShortString().substring(8).equals("\"init\"")) {
					init = tmp;
					break;
				}
			}
		}
		ITree object = init.getChild(1);
		if(object.toShortString().startsWith("NULL")) return false;
		String typeOfVar = object.getChild(0).getChild(1).getChild(0).toShortString();
		if(typeOfVar.contains("FunctionExpression")) return true;
		return false;
	}
	
	public boolean isVarClass(ITree var, String varName) {
		ITree init = var.getChild(2);
		if(!init.getChild(0).toShortString().substring(8).equals("\"init\"")) {
			for(ITree tmp : var.getChildren()) {
				if(tmp.getChild(0).toShortString().substring(8).equals("\"init\"")) {
					init = tmp;
					break;
				}
			}
		}
		ITree object = init.getChild(1);
		if(object.toShortString().startsWith("NULL")) return false;
		String typeOfVar = object.getChild(0).getChild(1).getChild(0).toShortString();
		if(typeOfVar.contains("ClassExpression")){
			String superClassName = getSuperClass(init.getChild(1));
			superClassFinder.put(varName, superClassName);
			return true;
		}
		return false;
	}
	
	public boolean isExport(ITree exp) {
		List<String> checkToken = getFullTokenList(exp);
//		for(String s : checkToken) {
//			System.out.println(s);
//		}
		if(checkToken.size() > 5 && checkToken.get(0).equals("module") && checkToken.get(2).equals("exports")) {
			exportVarName = "exportEntity";
			ITree right = fieldNameToITree(exp, "right");
			ITree typeOfRight = fieldNameToITree(right.getChild(1), "type");
			String entityType = removeQuote(typeOfRight.getChild(1).getChild(0).toShortString().substring(8));
			
			if(checkToken.get(4).equals("function") || entityType.equals("FunctionExpression")){
				funcSet.add(exportVarName);
				nameToITree.put(exportVarName, exp);
				expNameToToken.put(exportVarName, getFullTokenList(exp));
			}
			else if (checkToken.get(4).equals("class") || entityType.equals("ClassExpression")) {
				String superClassName = getSuperClass(right.getChild(1));
				superClassFinder.put(exportVarName, superClassName);
				classSet.add(exportVarName);
				nameToITree.put(exportVarName, exp);
				expNameToToken.put(exportVarName, getFullTokenList(exp));
			}
//			Here needs some more work if there are lots of staffs in the object
			else if (entityType.equalsIgnoreCase("ObjectExpression") &&
					dealWithObjectExport(right.getChild(1)) == true) {
				System.out.println("This is an object export");
				
				return true;	
			}
			else {
				varSet.add(exportVarName);
				nameToITree.put(exportVarName, exp);
				expNameToToken.put(exportVarName, getFullTokenList(exp));
			}
			return true;
		}
		
		else if(checkToken.size() >= 5 && checkToken.get(0).equals("exports") && checkToken.get(1).equals(".")) {
			String entityName = checkToken.get(2);
			ITree right = fieldNameToITree(exp, "right");
			ITree typeOfRight = fieldNameToITree(right.getChild(1), "type");
			String entityType = removeQuote(typeOfRight.getChild(1).getChild(0).toShortString().substring(8));
			
			
			if(checkToken.get(4).equals("function") || entityType.equals("FunctionExpression")) {
//				System.out.println("This is a function export" + entityName);
				funcSet.add(entityName);
				nameToITree.put(entityName, exp);
				expNameToToken.put(entityName, getFullTokenList(exp));
			}
			else if(checkToken.get(4).equals("class") || entityType.equals("ClassExpression")) {
//				System.out.println("This is a class export" + entityName);
				String superClassName = getSuperClass(right.getChild(1));
				superClassFinder.put(entityName, superClassName);
				classSet.add(entityName);
				nameToITree.put(entityName, exp);
				expNameToToken.put(entityName, getFullTokenList(exp));
			}
//			Here needs some more work if the exported name is different from the original name
			else if(checkToken.size() == 5 || entityType.equals("Identifier")) {
				System.out.println("It's an Identifier");
				return true;
			}
			
			else {
				varSet.add(entityName);
				nameToITree.put(entityName, exp);
				expNameToToken.put(entityName, getFullTokenList(exp));
			}
			return true;
		}
		
//		Assume one export
		else if(checkToken.size() > 2 && checkToken.size() < 6 && checkToken.get(0).equals("module") && checkToken.get(2).equals("exports")){
			exportVarName = checkToken.get(4);
			exportVarNames.add(exportVarName);
			System.out.println(exportVarName + "is an export");
			return true;
		}
		
//		else if(checkToken.get(0).equals("exports") && checkToken.get)
		
		
		/*
		 * Deal with module.exports = function ........
		 */
		
		
		
		return false;
	}
	
	public void checkAMD() {
		int index = 0;
		if(tokens.size() > 0 && tokens.get(0).equals("\\\"use strict\\\"")) {
			index = 1;
			if(tokens.size() > 1 && tokens.get(1).equals(";")) {
				index = 2;
			}
		}
		if(tokens.size() > index && (tokens.get(index).equals("define") || 
				tokens.get(index).equals("require") || tokens.get(index).equals("("))) {
			isAMD = true;
		}	
	}
	
	public String getSuperClass(ITree entity) {
		String result = null;
		
		ITree superClass = fieldNameToITree(entity, "superClass");
		String isNull = superClass.getChild(1).toShortString();
		if(isNull.startsWith("NULL")) return result;
		result = removeQuote(superClass.getChild(1).getChild(1).getChild(1).getChild(0).toShortString().substring(8));
//		System.out.println("superClass name is" +removeQuote(superClass.getChild(1).getChild(1).getChild(1).getChild(0).toShortString().substring(8)));
		return result;
	}
	
//	public static void main(String[] args) {
//		String cP = "pub++adg++erf.js";
//		String rP = "'../a/x'";
//		System.out.println(getPath(cP,rP));
//	}
	
}
