package jstest;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.gumtreediff.tree.ITree;

public class TraverseJsonTyped {
	Set<String> prototypeSet = new HashSet<>();
//	jsFilePath should be in the form of package1++package2++name.js
	boolean isAMD = false;
	boolean isNodeJs = true;
	
	String jsFilePath;
	ITree jsonTree;
	ITree typedJsonTree;
	Map<String, ITree> nameToITree = new HashMap<String, ITree>();
	Map<String, List<String>> expNameToToken = new HashMap<>();
	HashSet<ITree> entities = new HashSet<>();
	HashSet<ITree> typedEntities = new HashSet<>();
	HashSet<String> varSet = new HashSet<>();
	HashSet<String> funcSet = new HashSet<>();
	HashSet<String> classSet = new HashSet<>();
	HashSet<String> expSet = new HashSet<>();
	HashSet<String> importSet = new HashSet<>();
	HashSet<String> exportSet = new HashSet<>();
	HashSet<String> toBeInit = new HashSet<>();
//	static fields or functions
	HashSet<String> staticVF = new HashSet<>();
//	deal with import things, for require + import * as ...(folder or js files)
	Map<String, String> importVarToJs = new HashMap<String, String>();
	
//	deal with import things, for import statement (function, var, class), key: local, value: js ++ name 
	Map<String, String> importEntityToJs = new HashMap<String, String>();
	
	Map<String, String> importIndexCheck = new HashMap<String, String>();
	
//	deal with exports.a = b; then b is an entity already defined, a is its alternative name.
	public Map<String, String> alterName = new HashMap<>();
	
	ITree exportFunc = null;
	String exportVarName = "";
	boolean hasModule = false;
	String moduleName = "";
	HashSet<String> exportVarNames = new HashSet<>();
	HashMap<String, String> superClassFinder = new HashMap<>();
	HashMap<String, String> varToClass = new HashMap<>();
	
	
	List<Integer> tokenStartPos = new ArrayList<Integer>();
	List<String> tokens = new ArrayList<String>();
	
	/*
	 * Added on Oct. 11th for typedast
	 * typedJsonTree, which almost has the same thing as jsonTree
	 * plus the inferred type and loc
	 */
	HashMap<String, ITree> typedNameToITree = new HashMap<>();
	List<ITree> jsonEntities = new ArrayList<ITree>();
	List<ITree> typedJsonEntities = new ArrayList<ITree>();
	Set<String> isExportEntity = new HashSet<String>();
	Set<String> isPureExportEntity = new HashSet<>();
	
	
	public TraverseJsonTyped(ITree typedJsonTree, String jsFilePath, ITree jsonTree){
		this.jsonTree = jsonTree;
		this.jsFilePath = jsFilePath.replace(".js", "");
		this.typedJsonTree = typedJsonTree;
		
		buildTokenLists();
		if(!isNodeJs) typedJsonEntities = typedJsonTree.getChild(1).getChild(1).getChildren();
		jsonEntities = jsonTree.getChild(1).getChild(1).getChildren();
		
		for(int i = 0; i < jsonEntities.size(); i++){
			ITree entity = jsonEntities.get(i);
			ITree typedEntity = null;
			if (!isNodeJs)  typedEntity = typedJsonEntities.get(i);
			entities.add(entity);
			if (!isNodeJs) typedEntities.add(typedEntity);
			buildEntity(entity, typedEntity, false);
		}
		checkAMD();
	}
	
	
	public void dealWithExportEntity(ITree entity, ITree typedEntity) {
//		entity.getChild(1) is declaration field
		ITree exportEntity = entity.getChild(1).getChild(1);
		ITree exportTypedEntity = null;
		if(!isNodeJs) exportTypedEntity = typedEntity.getChild(1).getChild(1);
		String export =exportEntity.toShortString();
		if(!export.startsWith("NULL")) {
			String exportType = getEntityKind(exportEntity);
			if(exportType.contains("Declaration")){
				buildEntity(exportEntity, exportTypedEntity, true);
			}
			
			
			else if(exportType.contains("ObjectExpression")) {
				dealWithObjectExport(exportEntity, exportTypedEntity);
			}
		}
		
		else {
			 ITree specifiers = fieldNameToITree(entity, "specifiers");
			 List<ITree> speArray = specifiers.getChild(1).getChildren();
			 for (int i = 0; i < speArray.size(); i++) {
				 ITree specifier = speArray.get(i);
				 ITree exported = fieldNameToITree(specifier, "exported");
				 ITree local = fieldNameToITree(specifier, "local");
				 String exportedName = removeQuote(exported.getChild(1).getChild(1).getChild(1).getChild(0).toShortString().substring(8));
				 String localName = removeQuote(local.getChild(1).getChild(1).getChild(1).getChild(0).toShortString().substring(8));
				 if (!exportedName.equals(localName)) alterName.put(localName, exportedName);
				 isExportEntity.add(localName);
			 }
		}
	}
	
	public boolean dealWithObjectExport(ITree export, ITree typedExport) {
		ITree propertyArray = export.getChild(1).getChild(1);
		ITree typedPropertyArray = null;
		if(!isNodeJs) typedPropertyArray = typedExport.getChild(1).getChild(1);
		boolean hasExport = false;
		List<ITree> properties = propertyArray.getChildren();
		List<ITree> typedProperties = null;
		if(!isNodeJs) typedProperties = typedPropertyArray.getChildren();
		for(int i = 0; i < properties.size(); i++) {
			ITree property = properties.get(i);
			ITree typedProperty = null;
			if(!isNodeJs) typedProperty = typedProperties.get(i);
			System.out.println(property.toShortString());
			ITree key = fieldNameToITree(property, "key");
			ITree value = fieldNameToITree(property, "value");
			ITree typedValue = null;
			if (!isNodeJs) typedValue = fieldNameToITree(typedProperty, "value");
			String keyName = removeQuote(key.getChild(1).getChild(1).getChild(1).getChild(0).toShortString().substring(8));
			System.out.println(keyName);
			String valueType = removeQuote(value.getChild(1).getChild(0).getChild(1).getChild(0).toShortString().substring(8));
			if(valueType.contains("Function")) {
				funcSet.add(keyName);
				isPureExportEntity.add(keyName);
				nameToITree.put(keyName, property);
				if(!isNodeJs) typedNameToITree.put(keyName, typedProperty);
//				Added to check 
				expNameToToken.put(keyName,getFullTokenList(property));
				hasExport = true;
			}
			else if(valueType.contains("Class")) {
				ITree unTypedClass = value.getChild(1);
				ITree typedClass = null;
				if(!isNodeJs) typedClass = typedValue.getChild(1);
				dealWithClass(keyName, unTypedClass, typedClass);
				classSet.add(keyName + " class");
				isPureExportEntity.add(keyName + " class");
				nameToITree.put(keyName + " class", property);
				if(!isNodeJs) typedNameToITree.put(keyName + " class", typedProperty);
				expNameToToken.put(keyName + " class", getFullTokenList(property));
				String superClass = getSuperClass(unTypedClass);
				if (superClass != null) superClass += " class";
				superClassFinder.put(keyName + " class", superClass);
				hasExport = true;
			}
			else if(!valueType.equals("Identifier")){
				varSet.add(keyName);
				nameToITree.put(keyName, property);
				isPureExportEntity.add(keyName);
				if(!isNodeJs) typedNameToITree.put(keyName, typedProperty);
				expNameToToken.put(keyName, getFullTokenList(property));
				hasExport = true;
			}
			else if(valueType.equals("Identifier")){
				isExportEntity.add(keyName);
			}
			
		}
		return hasExport;
	}
	
	/*
	 * result.getChild(1) is the object of that field.
	 */
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
			if (tmp.getChildren().size() == 0) continue;
			if (tmp.getChild(0).toShortString().length() <= 8) continue;
			if(tmp.getChild(0).toShortString().substring(8).equals("\"" + name + "\"")) return true;
		}
		return false;
	}
	
	
	public void dealWithImportEntity (ITree entity, ITree typedEntity) {
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
				importIndexCheck.put(localName, sourcePath + "++" + "index" + "++" +
									importedName);
			}
			
		}
	}
	
	public String getFieldName(ITree p, String... s) {
		
		for (String field : s) {
			p = fieldNameToITree(p, field).getChild(1);
		}
		return removeQuote(p.getChild(0).toShortString().substring(8));
	}
	
	public ITree multiLevel(ITree p, String... s){
		for (String field : s) {
			p = fieldNameToITree(p, field).getChild(1);
		}
		return p;
	}
	/*
	 * How to define a function:
	 * 1 varDeclare
	 * 2 functionDeclare
	 * 3 assignmentExpression (can be prototyped)
	 */
	/*
	 * Need to deal with self.x = ...., here self means this (done)
	 */
	public void dealWithProtoFunc(String funcName) {
		ITree entity = nameToITree.get(funcName);
		ITree typedEntity = null;
		if(!isNodeJs) typedEntity = typedNameToITree.get(funcName);
		ITree body = null;
		ITree typedBody = null;
		String type = getFieldName(entity, "type");
		if (type.equals("VariableDeclarator")) {
			body = multiLevel(entity, "init", "body");
			if (!isNodeJs) typedBody = multiLevel(typedEntity, "init", "body");
		}
		else if(type.equals("FunctionDeclaration")) {
			body = fieldNameToITree(entity, "body").getChild(1);
			if (!isNodeJs) typedBody = fieldNameToITree(typedEntity, "body").getChild(1);
		}
		else if(type.equals("AssignmentExpression")) {
			body = multiLevel(entity, "right", "body");
			if (!isNodeJs) typedBody = multiLevel(typedEntity, "right", "body");
		}
		
		else return;
		
		List<ITree> bodyArray = fieldNameToITree(body, "body").getChild(1).getChildren();
		List<ITree> typedBodyArray = null;
		if (!isNodeJs) typedBodyArray = fieldNameToITree(typedBody, "body").getChild(1).getChildren();
		for (int j = 0; j < bodyArray.size(); j++) {
			ITree ele = bodyArray.get(j);
			List<String> tokensOfEle = getFullTokenList(ele);
			ITree typedEle = null;
			if (!isNodeJs) typedEle = typedBodyArray.get(j);
			String typeOfEle = getFieldName(ele, "type");
			if (!typeOfEle.equals("ExpressionStatement")) continue;
			ITree expression = fieldNameToITree(ele, "expression").getChild(1);
			String typeOfExpression = getFieldName(expression, "type");
			if (!typeOfExpression.equals("AssignmentExpression") && !typeOfExpression.equals("MemberExpression")) continue;
			if (typeOfExpression.equals("MemberExpression")) {
				String typeOfObject = getFieldName(expression, "object", "type");
		
				if (!typeOfObject.equals("ThisExpression") && !tokensOfEle.get(0).equals("self")) continue;
				String varName = funcName + "++" + getFieldName(expression, "property", "name");
				varSet.add(varName);
				nameToITree.put(varName, ele);
				if (!isNodeJs) typedNameToITree.put(varName, typedEle);
				expNameToToken.put(varName, getFullTokenList(ele));
				continue;
			}
			String typeOfLeft = getFieldName(expression, "left", "type");
			if (!typeOfLeft.equals("MemberExpression")) continue;
			String typeOfObject = getFieldName(expression, "left", "object", "type");
			if (!typeOfObject.equals("ThisExpression") && !tokensOfEle.get(0).equals("self") || typeOfObject.equals("MemberExpression")) continue;
			String typeOfRight = getFieldName(expression, "right", "type");
			ITree right = multiLevel(ele, "expression", "right");
			ITree typedRight = null;
			if (!isNodeJs) typedRight = multiLevel(typedEle, "expression", "right");
			if (typeOfRight.contains("FunctionExpression")) {
				String innerFuncName = funcName + "++" + getFieldName(expression,"left", "property", "name");
				funcSet.add(innerFuncName);
				nameToITree.put(innerFuncName, right);
				if (!isNodeJs) typedNameToITree.put(innerFuncName, typedRight);
				expNameToToken.put(innerFuncName, getFullTokenList(right));
			}
			else {
				String varName = funcName + "++" + getFieldName(expression,"left", "property", "name");
				varSet.add(varName);
				nameToITree.put(varName, ele);
				if (!isNodeJs) typedNameToITree.put(varName, typedEle);
				expNameToToken.put(varName, getFullTokenList(ele));
				if (typeOfRight.equals("NewExpression")) {
					String toClass = "";
					ITree callee = multiLevel(expression, "right", "callee");
					String typeOfCallee = getFieldName(expression, "right", "callee", "type");
					if (typeOfCallee.equals("MemberExpression")) {
						dealWithMember(callee, varName, funcName);
					}
					else if(typeOfCallee.equals("Identifier")){
						toClass = getFieldName(expression, "right", "callee", "name");
						if (funcSet.contains(toClass) || classSet.contains(toClass)) varToClass.put(varName, toClass);
						else if (importVarToJs.containsKey(toClass)) varToClass.put(varName, importVarToJs.get(toClass));
						else if (importEntityToJs.containsKey(toClass)) varToClass.put(varName, importEntityToJs.get(toClass));
					}
				}
			}
		}
	}
	
	public void dealWithMember(ITree member, String varName, String funcName) {
		List<String> tokenOfMember = getFullTokenList(member);
		StringBuilder sb = new StringBuilder();
		boolean isOuterEntity = false;
		for (int i = 0; i < tokenOfMember.size(); i++) {
			String s = tokenOfMember.get(i);
			if (i == 0) {
				if (importVarToJs.containsKey(s)) s = importVarToJs.get(s);
				else if (importEntityToJs.containsKey(s)) s = importEntityToJs.get(s);
				else if (varToClass.containsKey(s)) s = varToClass.get(s);
				if (!s.equals(tokenOfMember.get(i))) isOuterEntity = true;
				sb.append(s);
			}
			else if (s.equals(".")) sb.append("++");
			else sb.append(s);
		}
		if(isOuterEntity) varToClass.put(varName, sb.toString());
	}
	
	public void dealWithClass(String className, ITree entity, ITree typedEntity) {
		ITree body = fieldNameToITree(entity, "body").getChild(1);
		ITree typedBody = null;
		if (!isNodeJs) typedBody = fieldNameToITree(typedEntity, "body").getChild(1);
		List<ITree> bodyArray = fieldNameToITree(body, "body").getChild(1).getChildren();
		List<ITree> typedBodyArray = null;
		if (!isNodeJs) typedBodyArray = fieldNameToITree(typedBody, "body").getChild(1).getChildren();
		for (int i = 0; i < bodyArray.size(); i++) {
			ITree method = bodyArray.get(i);
			ITree typedMethod = null;
			if (!isNodeJs) typedMethod = typedBodyArray.get(i);
			List<String> tokenList = getFullTokenList(method);
			
			ITree typeOfMethod = fieldNameToITree(method, "type");
			if (typeOfMethod == null) continue;
			String type = removeQuote(typeOfMethod.getChild(1).getChild(0).toShortString().substring(8));
			if (!type.equals("MethodDefinition")) continue;
			String nameOfMethod = getFieldName(method, "key", "name");
			System.out.println(nameOfMethod + " " + "is a method in class " + className);
			
			
//			String nameOfMethod = tokenList.get(0);
			if (nameOfMethod.equals("constructor")) {
				nameOfMethod = className;
				List<ITree> consBodyArray = multiLevel(method, "value", "body", "body").getChildren();
				List<ITree> typedConsBodyArray = null;
				if(!isNodeJs) typedConsBodyArray = multiLevel(typedMethod, "value", "body", "body").getChildren();
				System.out.println(consBodyArray.size() + " " + "inside fields or methods");
				for (int j = 0; j < consBodyArray.size(); j++) {
					ITree consEle = consBodyArray.get(j);
					ITree typedConsEle = null;
					List<String> tokenOfEle = getFullTokenList(consEle);
					if (!isNodeJs) typedConsEle = typedConsBodyArray.get(j);
					String typeOfEle = getFieldName(consEle, "type");
					if (!typeOfEle.equals("ExpressionStatement")) continue;
					ITree expression = fieldNameToITree(consEle, "expression").getChild(1);
					String typeOfExpression = getFieldName(expression, "type");
					if (!typeOfExpression.equals("AssignmentExpression") && !typeOfExpression.equals("MemberExpression")) continue;
					if (typeOfExpression.equals("MemberExpression")) {
						String typeOfObject = getFieldName(expression, "object", "type");
						if (!typeOfObject.equals("ThisExpression") && !tokenOfEle.get(0).equals("self")) continue;
						String varName = className + "++" + getFieldName(expression, "property", "name");
						varSet.add(varName);
						nameToITree.put(varName, consEle);
						if (!isNodeJs) typedNameToITree.put(varName, typedConsEle);
						expNameToToken.put(varName, getFullTokenList(consEle));
						continue;
					}
					String typeOfLeft = getFieldName(expression, "left", "type");
					if (!typeOfLeft.equals("MemberExpression")) continue;
					String typeOfObject = getFieldName(expression, "left", "object", "type");
					if (!typeOfObject.equals("ThisExpression") && !tokenOfEle.get(0).equals("self") || typeOfObject.equals("MemberExpression")) continue;
					
					String typeOfRight = getFieldName(expression, "right", "type");
					if (typeOfRight.contains("FunctionExpression")) {
						String funcName = className + "++" + getFieldName(expression,"left", "property", "name");
						funcSet.add(funcName);
						nameToITree.put(funcName, consEle);
						if (!isNodeJs) typedNameToITree.put(funcName, typedConsEle);
						expNameToToken.put(funcName, getFullTokenList(consEle));
					}
					else {
						String varName = className + "++" + getFieldName(expression,"left", "property", "name");
						varSet.add(varName);
						nameToITree.put(varName, consEle);
						if (!isNodeJs) typedNameToITree.put(varName, typedConsEle);
						expNameToToken.put(varName, getFullTokenList(consEle));
						if (typeOfRight.equals("NewExpression")) {
							String toClass = "";
							ITree callee = multiLevel(expression, "right", "callee");
							String typeOfCallee = getFieldName(expression, "right", "callee", "type");
							if (typeOfCallee.equals("MemberExpression")) {
								dealWithMember(callee, varName, className);
							}
							else if(typeOfCallee.equals("Identifier")){
								toClass = getFieldName(expression, "right", "callee", "name");
								if (funcSet.contains(toClass) || classSet.contains(toClass)) varToClass.put(varName, toClass);
								else if (importVarToJs.containsKey(toClass)) varToClass.put(varName, importVarToJs.get(toClass));
								else if (importEntityToJs.containsKey(toClass)) varToClass.put(varName, importEntityToJs.get(toClass));
							}
						}
					}
				}
			}
			else nameOfMethod = className + "++" + nameOfMethod;
			System.out.println(nameOfMethod);
			funcSet.add(nameOfMethod);
			nameToITree.put(nameOfMethod, method);
			if (!isNodeJs) typedNameToITree.put(nameOfMethod, typedMethod);
			expNameToToken.put(nameOfMethod, tokenList);
			
		}
	}
	
	public void buildEntity(ITree entity, ITree typedEntity, boolean exported) {
		switch (getEntityKind(entity)) {
		case "\"ImportDeclaration\"":
//			Things to de about import
//			put into the varToJsFile map
			dealWithImportEntity (entity, typedEntity);
			break;
		case "\"ExportNamedDeclaration\"":
//			Things to do with export
			dealWithExportEntity(entity, typedEntity);
			break;
		case "\"ExportAllDeclaration\"":
//			Things to do with export
			break;
		case "\"ExportDefaultDeclaration\"":
//			Things to do with export
			dealWithExportEntity(entity, typedEntity);
			break;
		case "\"VariableDeclaration\"":
			for(String var : getVariableName(entity, typedEntity, exported)){
				varSet.add(var);
			}
			break;
		case "\"FunctionDeclaration\"":
			System.out.println(jsFilePath);
			
			String nameOfFunction = getFunctionName(entity);
			if(exported == true) isExportEntity.add(nameOfFunction);
			funcSet.add(nameOfFunction);
			nameToITree.put(nameOfFunction, entity);
			if(!isNodeJs) typedNameToITree.put(nameOfFunction, typedEntity);
			expNameToToken.put(nameOfFunction,getFullTokenList(entity));
			break;
		case "\"ClassDeclaration\"":
			
			String nameOfClass = getClassName(entity);
			dealWithClass(nameOfClass, entity, typedEntity);
			classSet.add(nameOfClass + " class");
			if(exported == true) isExportEntity.add(nameOfClass + " class");
			nameToITree.put(nameOfClass + " class", entity);
			if(!isNodeJs) typedNameToITree.put(nameOfClass + " class", typedEntity);
			expNameToToken.put(nameOfClass + " class",getFullTokenList(entity));
			String superClass = getSuperClass(entity);
			if (superClass != null) superClass += " class";
			superClassFinder.put(nameOfClass + " class", superClass);
			break;
		default:
			String expName = getExpName(entity);
			
			if(expName.startsWith("\"ExpressionStatement\"")) {
				expName = getInnerExpName(entity);
				entity = entity.getChild(1).getChild(1);
				if(!isNodeJs) typedEntity = typedEntity.getChild(1).getChild(1);
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
			List<String> tokenList = getFullTokenList(entity);
			
			if(!(expName.startsWith("\"AssignmentExpression\"") && isExport(entity, typedEntity)) && !expName.startsWith("\"Literal\"")){
				if(expName.startsWith("\"AssignmentExpression\"")) {
//					Assignment expression case 1:
					ITree left = fieldNameToITree(entity, "left");
					ITree typedLeft = null;
					if(!isNodeJs) typedLeft = fieldNameToITree(typedEntity, "left");
					
					ITree typeOfLeft = fieldNameToITree(left.getChild(1), "type");
					String entityType = removeQuote(typeOfLeft.getChild(1).getChild(0).toShortString().substring(8));
					if(entityType.equals("Identifier")) {
						ITree nameOfLeft = fieldNameToITree(left.getChild(1), "name");
						String entityName = removeQuote(nameOfLeft.getChild(1).getChild(0).toShortString().substring(8));
						if(!nameToITree.containsKey(entityName) || toBeInit.contains(entityName)) {
							if (toBeInit.contains(entityName)) {
								varSet.remove(entityName);
							}
							ITree right = fieldNameToITree(entity, "right");
							ITree typedRight = null;
							if(!isNodeJs) typedRight = fieldNameToITree(typedEntity, "right");
							ITree typeOfRight = fieldNameToITree(right.getChild(1), "type");
							String rightType = removeQuote(typeOfRight.getChild(1).getChild(0).toShortString().substring(8));
							if(rightType.contains("FunctionExpression")) {
								funcSet.add(entityName);
								nameToITree.put(entityName, entity);
								if(!isNodeJs) typedNameToITree.put(entityName, typedEntity);
								expNameToToken.put(entityName, tokenList);
							}
							else if(rightType.equals("ClassExpression")) {
								ITree typedClass = null;
								if(!isNodeJs) typedClass = typedRight.getChild(1);
								dealWithClass(entityName, right.getChild(1), typedClass);
								classSet.add(entityName + " class");
								nameToITree.put(entityName + " class", entity);
								if(!isNodeJs) typedNameToITree.put(entityName + " class", typedEntity);
								expNameToToken.put(entityName + " class", tokenList);
								String superKlass = getSuperClass(right.getChild(1));
								if (superKlass != null) superKlass += " class";
								superClassFinder.put(entityName + " class", superKlass);
							}
							
							else if(!isNodeJs && rightType.contains("CallExpression")) {
								boolean rightCallSig = havingCallSigs(typedRight), leftCallSig = havingCallSigs(typedLeft);
								String rightReturnKind = getInferredKind(typedRight), leftReturnKind = getInferredKind(typedLeft);
								if (rightCallSig == true && rightReturnKind.equals("object") || leftCallSig == true && leftReturnKind.equals("object")) {
									funcSet.add(entityName);
									nameToITree.put(entityName, entity);
									typedNameToITree.put(entityName, typedEntity);
									expNameToToken.put(entityName, tokenList);
								}
							}
							
							else {
								if (rightType.equals("NewExpression")) {
									String toClass = "";
									ITree callee = multiLevel(right.getChild(1), "callee");
									String typeOfCallee = getFieldName(callee , "type");
									if (typeOfCallee.equals("MemberExpression")) {
										dealWithMember(callee, entityName, "");
									}
									else if(typeOfCallee.equals("Identifier")){
										toClass = getFieldName(right.getChild(1), "callee", "name");
										if (funcSet.contains(toClass) || classSet.contains(toClass)) varToClass.put(entityName, toClass);
										else if (importVarToJs.containsKey(toClass)) varToClass.put(entityName, importVarToJs.get(toClass));
										else if (importEntityToJs.containsKey(toClass)) varToClass.put(entityName, importEntityToJs.get(toClass));
									}
									
								}
								varSet.add(entityName);
								nameToITree.put(entityName, entity);
								if(!isNodeJs) typedNameToITree.put(entityName, typedEntity);
								expNameToToken.put(entityName,tokenList);
							}
							
						}
						else {
							expSet.add(expName);
							nameToITree.put(expName, entity);
							if(!isNodeJs) typedNameToITree.put(expName, typedEntity);
							expNameToToken.put(expName,tokenList);
						}
						
					}
					
//					Assignment expression case 2: f1.prototype = {} --> ObjectExpression 
//												or f1.prototype.xx = whatever 
					
					else if (entityType.equals("MemberExpression")) {
						ITree memberParts = left.getChild(1);
						ITree object = fieldNameToITree(memberParts, "object").getChild(1);
						ITree nameO = fieldNameToITree(object, "name");
						ITree object0 = fieldNameToITree(object, "object");
						ITree property = fieldNameToITree(memberParts, "property").getChild(1);
						property = fieldNameToITree(property, "name").getChild(1);
						String nameOfProperty = removeQuote(property.getChild(0).toShortString().substring(8));
						if (nameO != null) { 
							object = fieldNameToITree(object, "name").getChild(1);
							String nameOfObject = removeQuote(object.getChild(0).toShortString().substring(8));
							
							/*
							 * Add a new logic when programmers use prototype
							 */
							
							System.out.println(nameOfProperty);
							/*
							 * Here is going to deal with the f1.f2 = {} this f2 is a field in f1 or a method in
							 * f1. As static
							 */
							
							
							if (nameOfProperty.equals("prototype") && funcSet.contains(nameOfObject)) {
								if (prototypeSet.add(nameOfObject)) {
									dealWithProtoFunc(nameOfObject);
								}
								ITree right = fieldNameToITree(entity, "right");
								ITree typedRight = null;
								if(!isNodeJs) typedRight = fieldNameToITree(typedEntity, "right");
								ITree typeOfRight = fieldNameToITree(right.getChild(1), "type");
								String rightType = removeQuote(typeOfRight.getChild(1).getChild(0).toShortString().substring(8));
								if (rightType.equals("ObjectExpression")) {
									ITree propertyArray = right.getChild(1).getChild(1).getChild(1);
									ITree typedPropertyArray = null;
									if(!isNodeJs) typedPropertyArray = typedRight.getChild(1).getChild(1).getChild(1);
									List<ITree> properties = propertyArray.getChildren();
									List<ITree> typedProperties = null;
									if(!isNodeJs) typedProperties = typedPropertyArray.getChildren();
									System.out.println(properties.size());
									for(int i = 0; i < properties.size(); i++) {
										ITree protoProperty = properties.get(i);
										ITree typedProtoProperty = null;
										if(!isNodeJs) typedProtoProperty = typedProperties.get(i);
										ITree key = fieldNameToITree(protoProperty, "key");
//										System.out.println(key);
										ITree value = fieldNameToITree(protoProperty, "value");
										String keyName = removeQuote(key.getChild(1).getChild(1).getChild(1).getChild(0).toShortString().substring(8));
										System.out.println(keyName);
										String valueType = removeQuote(value.getChild(1).getChild(0).getChild(1).getChild(0).toShortString().substring(8));
										String entityName = nameOfObject + "++" + keyName;
										if(valueType.contains("Function")) {
											funcSet.add(entityName);		
										}
										else {
											varSet.add(entityName);
										}
										nameToITree.put(entityName, protoProperty);
										if(!isNodeJs) typedNameToITree.put(entityName, typedProtoProperty);
//										Added to check 
										expNameToToken.put(entityName,getFullTokenList(protoProperty));
									}
								}
								else {
									expSet.add(expName);
									nameToITree.put(expName, entity);
									if(!isNodeJs) typedNameToITree.put(expName, typedEntity);
									expNameToToken.put(expName,tokenList);
								}
							}
							/*
							 * end here
							 */
							
							
							else if (hasModule && moduleName.equals(nameOfObject)) {
	//							System.out.println("Yes, can detect a module function");
								ITree right = fieldNameToITree(entity, "right");
								ITree typedRight = null;
								if(!isNodeJs) typedRight = fieldNameToITree(typedEntity, "right");
								ITree typeOfRight = fieldNameToITree(right.getChild(1), "type");
								String rightType = removeQuote(typeOfRight.getChild(1).getChild(0).toShortString().substring(8));
								if(rightType.contains("FunctionExpression")) {
									funcSet.add(nameOfProperty);
									isExportEntity.add(nameOfProperty);
									nameToITree.put(nameOfProperty, entity);
									if(!isNodeJs) typedNameToITree.put(nameOfProperty, typedEntity);
									expNameToToken.put(nameOfProperty, tokenList);
								}
								else if(rightType.equals("ClassExpression")) {
									ITree typedClass = null;
									if(!isNodeJs) typedClass = typedRight.getChild(1);
									dealWithClass(nameOfProperty, right.getChild(1), typedClass);
									classSet.add(nameOfProperty + " class");
									isExportEntity.add(nameOfProperty + " class");
									nameToITree.put(nameOfProperty + " class", entity);
									if(!isNodeJs) typedNameToITree.put(nameOfProperty + " class", typedEntity);
									expNameToToken.put(nameOfProperty + " class", tokenList);
									String superKlass = getSuperClass(right.getChild(1));
									if (superKlass != null) superKlass += " class";
									superClassFinder.put(nameOfProperty + " class", superKlass);
								}
								
								else if(!isNodeJs && rightType.contains("CallExpression")) {
									boolean rightCallSig = havingCallSigs(typedRight), leftCallSig = havingCallSigs(typedLeft);
									String rightReturnKind = getInferredKind(typedRight), leftReturnKind = getInferredKind(typedLeft);
									if (rightCallSig == true && rightReturnKind.equals("object") || leftCallSig == true && leftReturnKind.equals("object")) {
										funcSet.add(nameOfProperty);
										isExportEntity.add(nameOfProperty);
										nameToITree.put(nameOfProperty, entity);
										typedNameToITree.put(nameOfProperty, typedEntity);
										expNameToToken.put(nameOfProperty, tokenList);
									}
								}
								
								else {
									varSet.add(nameOfProperty);
									isExportEntity.add(nameOfProperty);
									nameToITree.put(nameOfProperty, entity);
									if(!isNodeJs) typedNameToITree.put(nameOfProperty, typedEntity);
									expNameToToken.put(nameOfProperty,tokenList);
								}
							}
							
							/*
							 * deal with f1.f2 = {}, here f1 should be prototyped, then f2 is a static f/m of f1
							 */
							else if (!moduleName.equals(nameOfObject) && (funcSet.contains(nameOfObject) && prototypeSet.contains(nameOfObject) || classSet.contains(nameOfObject + " class"))) {
								String entityName = nameOfObject + "++" + nameOfProperty;
								staticVF.add(entityName);
//								String entityName = nameOfProperty;
								ITree right = fieldNameToITree(entity, "right");
								ITree typedRight = null;
								if(!isNodeJs) typedRight = fieldNameToITree(typedEntity, "right");
								ITree typeOfRight = fieldNameToITree(right.getChild(1), "type");
								String rightType = removeQuote(typeOfRight.getChild(1).getChild(0).toShortString().substring(8));
								if(rightType.contains("FunctionExpression")) {
									funcSet.add(entityName);
									nameToITree.put(entityName, entity);
									if(!isNodeJs) typedNameToITree.put(entityName, typedEntity);
									expNameToToken.put(entityName, tokenList);
								}
								
								else if(!isNodeJs && rightType.contains("CallExpression")) {
									boolean rightCallSig = havingCallSigs(typedRight), leftCallSig = havingCallSigs(typedLeft);
									String rightReturnKind = getInferredKind(typedRight), leftReturnKind = getInferredKind(typedLeft);
									if (rightCallSig == true && rightReturnKind.equals("object") || leftCallSig == true && leftReturnKind.equals("object")) {
										funcSet.add(entityName);
										nameToITree.put(entityName, entity);
										typedNameToITree.put(entityName, typedEntity);
										expNameToToken.put(entityName, tokenList);
									}
								}
								
								else {
									varSet.add(entityName);
									nameToITree.put(entityName, entity);
									if(!isNodeJs) typedNameToITree.put(entityName, typedEntity);
									expNameToToken.put(entityName,tokenList);
								}
							}
							
							else {
								expSet.add(expName);
								nameToITree.put(expName, entity);
								if(!isNodeJs) typedNameToITree.put(expName, typedEntity);
								expNameToToken.put(expName,tokenList);
							}
						}
						
						else if (object0 != null) {
							object0 = fieldNameToITree(object, "object").getChild(1);
							ITree name1 = fieldNameToITree(object0, "name");
							ITree property0 = fieldNameToITree(object, "property").getChild(1);
							String n1 = "";
							String p1 = removeQuote(fieldNameToITree(property0, "name").getChild(1).getChild(0).toShortString().substring(8));
							if (name1 != null && p1.equals("prototype")) {
								
								n1 = removeQuote(name1.getChild(1).getChild(0).toShortString().substring(8));
								
								if (funcSet.contains(n1)) {
									if (prototypeSet.add(n1)) {
										System.out.println(n1);
										dealWithProtoFunc(n1);
									}
									String entityName = n1 + "++" + nameOfProperty;
//									String entityName = nameOfProperty;
									ITree right = fieldNameToITree(entity, "right");
									ITree typedRight = null;
									if(!isNodeJs) typedRight = fieldNameToITree(typedEntity, "right");
									ITree typeOfRight = fieldNameToITree(right.getChild(1), "type");
									String rightType = removeQuote(typeOfRight.getChild(1).getChild(0).toShortString().substring(8));
									if(rightType.contains("FunctionExpression")) {
										funcSet.add(entityName);
										nameToITree.put(entityName, entity);
										if(!isNodeJs) typedNameToITree.put(entityName, typedEntity);
										expNameToToken.put(entityName, tokenList);
									}
									
									else if(!isNodeJs && rightType.contains("CallExpression")) {
										boolean rightCallSig = havingCallSigs(typedRight), leftCallSig = havingCallSigs(typedLeft);
										String rightReturnKind = getInferredKind(typedRight), leftReturnKind = getInferredKind(typedLeft);
										if (rightCallSig == true && rightReturnKind.equals("object") || leftCallSig == true && leftReturnKind.equals("object")) {
											funcSet.add(entityName);
											nameToITree.put(entityName, entity);
											typedNameToITree.put(entityName, typedEntity);
											expNameToToken.put(entityName, tokenList);
										}
									}
									
									else {
										varSet.add(entityName);
										nameToITree.put(entityName, entity);
										if(!isNodeJs) typedNameToITree.put(entityName, typedEntity);
										expNameToToken.put(entityName,tokenList);
									}
								}
								else {
									expSet.add(expName);
									nameToITree.put(expName, entity);
									if(!isNodeJs) typedNameToITree.put(expName, typedEntity);
									expNameToToken.put(expName,tokenList);
								}
							}
							else {
								expSet.add(expName);
								nameToITree.put(expName, entity);
								if(!isNodeJs) typedNameToITree.put(expName, typedEntity);
								expNameToToken.put(expName,tokenList);
							}
						}
						
						else {
							expSet.add(expName);
							nameToITree.put(expName, entity);
							if(!isNodeJs) typedNameToITree.put(expName, typedEntity);
							expNameToToken.put(expName,tokenList);
						}
					
					}
					
//					Assignment expression default case:
					
					else {
						expSet.add(expName);
						nameToITree.put(expName, entity);
						if(!isNodeJs) typedNameToITree.put(expName, typedEntity);
						expNameToToken.put(expName,tokenList);
					}
					
				}
				
				/*
				 * deal with _.extend
				 */
				
				else if (expName.startsWith("\"CallExpression\"") && tokenList.size() > 1 && tokenList.get(0).equals("_") && tokenList.get(2).equals("extend")) {
					List<ITree> argumentsArray = fieldNameToITree(entity, "arguments").getChild(1).getChildren();
					System.out.println(argumentsArray.size() + "size");
					List<ITree> typedArgumentsArray = null;
					if (!isNodeJs) typedArgumentsArray = fieldNameToITree(typedEntity, "arguments").getChild(1).getChildren();
					ITree arg0 = argumentsArray.get(0);
					ITree arg1 = argumentsArray.get(1);
					ITree typedArg1 = null;
					if (!isNodeJs) typedArg1 = typedArgumentsArray.get(1);
					String typeOfArg0 = getType(arg0), typeOfArg1 = getType(arg1);
					boolean isExtend = false;
					String nameOfObject = "", nameOfProperty = "";
					if (typeOfArg0.equals("MemberExpression") && typeOfArg1.equals("ObjectExpression")) {
						
						ITree object = fieldNameToITree(arg0, "object").getChild(1);
						ITree nameO = fieldNameToITree(object, "name");
						if (nameO != null) { 
							object = fieldNameToITree(object, "name").getChild(1);
							ITree property = fieldNameToITree(arg0, "property").getChild(1);
							property = fieldNameToITree(property, "name").getChild(1);
							nameOfObject = removeQuote(object.getChild(0).toShortString().substring(8));
							nameOfProperty = removeQuote(property.getChild(0).toShortString().substring(8));
							if (nameOfProperty.equals("prototype") && funcSet.contains(nameOfObject)) isExtend = true;
						}
					}
					if (isExtend) {
						if (prototypeSet.add(nameOfObject)) {
							System.out.println(nameOfObject);
							dealWithProtoFunc(nameOfObject);
							
						}
						ITree propertyArray = arg1.getChild(1).getChild(1);
						ITree typedPropertyArray = null;
						if(!isNodeJs) typedPropertyArray = typedArg1.getChild(1).getChild(1);
						List<ITree> properties = propertyArray.getChildren();
						List<ITree> typedProperties = null;
						if(!isNodeJs) typedProperties = typedPropertyArray.getChildren();
						System.out.println(properties.size());
						for(int i = 0; i < properties.size(); i++) {
							ITree protoProperty = properties.get(i);
							ITree typedProtoProperty = null;
							if(!isNodeJs) typedProtoProperty = typedProperties.get(i);
							ITree key = fieldNameToITree(protoProperty, "key");
//								System.out.println(key);
							ITree value = fieldNameToITree(protoProperty, "value");
							String keyName = removeQuote(key.getChild(1).getChild(1).getChild(1).getChild(0).toShortString().substring(8));
							System.out.println(keyName);
							String valueType = removeQuote(value.getChild(1).getChild(0).getChild(1).getChild(0).toShortString().substring(8));
							String entityName = nameOfObject + "++" + keyName;
//							String entityName = keyName;
							if(valueType.contains("Function")) {
								funcSet.add(entityName);		
							}
							else {
								varSet.add(entityName);
							}
							nameToITree.put(entityName, protoProperty);
							if(!isNodeJs) typedNameToITree.put(entityName, typedProtoProperty);
//								Added to check 
							expNameToToken.put(entityName,getFullTokenList(protoProperty));
							}
						}
					else {
						expSet.add(expName);
						nameToITree.put(expName, entity);
						if(!isNodeJs) typedNameToITree.put(expName, typedEntity);
						expNameToToken.put(expName,tokenList);
						
					}
					
				}
				
				else {
				
					expSet.add(expName);
					nameToITree.put(expName, entity);
					if(!isNodeJs) typedNameToITree.put(expName, typedEntity);
					expNameToToken.put(expName, tokenList);
				}
			}
		}
	}
	
	String getType(ITree entity) {
		ITree type = fieldNameToITree(entity, "type");
		if (type == null) return "";
		return removeQuote(type.getChild(1).getChild(0).toShortString().substring(8));
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
	
	private String tokenToCode (List<String> tokens) {
		StringBuilder sb = new StringBuilder();
		for (String token : tokens) sb.append(token);
		return sb.toString();
	}
	
	private boolean isVarExport(ITree var, String varName, ITree typedVar){
		List<String> checkToken = getFullTokenList(var);
//		for(String s : checkToken) {
//			System.out.println(s);
//		}
		/*
		 * deal with 1. var test = module.exports;
		 * 				test.add = function(){};(or whatever)
		 *  	     2. var ad = module.exports = function(){};
		 */
		int i = checkToken.indexOf("exports");
		String code = tokenToCode(checkToken);
		if(i > 0 && i < 7 && (code.contains("=exports") || code.contains("=module.exports"))) {
			hasModule = true;
			moduleName = varName;
			
			ITree init = var.getChild(2);
			ITree typedInit = null;
			if(!isNodeJs) typedInit = typedVar.getChild(2);
			if(!init.getChild(0).toShortString().substring(8).equals("\"init\"")) {
				for(ITree tmp : var.getChildren()) {
					if(tmp.getChild(0).toShortString().substring(8).equals("\"init\"")) {
						init = tmp;
						break;
					}
				}
			}
			
			if(!isNodeJs && !typedInit.getChild(0).toShortString().substring(8).equals("\"init\"")) {
				for(ITree tmp : typedVar.getChildren()) {
					if(tmp.getChild(0).toShortString().substring(8).equals("\"init\"")) {
						typedInit = tmp;
						break;
					}
				}
			}
			
			ITree object = init.getChild(1);
			ITree typedObject = null;
			if(!isNodeJs) typedObject = typedInit.getChild(1);
			if(object.toShortString().startsWith("NULL")) return false;
			String typeOfVar = object.getChild(0).getChild(1).getChild(0).toShortString();
			if(typeOfVar.contains("AssignmentExpression")){
				isExport(object, typedObject);
			}
					
			return true;
		}
		return false;
	}
	
	public List<String> getVariableName(ITree entity, ITree typedEntity, boolean exported){
		List<String> varList = new ArrayList<>();
		ITree varArray = entity.getChild(1).getChild(1);
		ITree typedVarArray = null;
		if(!isNodeJs) typedVarArray = typedEntity.getChild(1).getChild(1);
		List<ITree> vars = varArray.getChildren();
		List<ITree> typedVars = null;
		if(!isNodeJs) typedVars = typedVarArray.getChildren();
		for(int i = 0; i < vars.size(); i++){
			ITree var = vars.get(i);
			ITree typedVar = null;
			if(!isNodeJs) typedVar = typedVars.get(i);
			String varName = var.getChild(1).getChild(1).getChild(1).getChild(1).getChild(0).toShortString().substring(8).replace("\"", "");
			ITree idTree = var.getChild(1);
			if (!hasTheField(idTree.getChild(1), "name") || varName.equals("")) {
				isVarImport(var, varName);
				continue;
			}
			
			
			if(isVarImport(var, varName)){
				continue;
			}
			
			if(isVarExport(var, varName, typedVar)) {
				continue;
			}
			
			
			if(isVarFunction(var, typedVar, varName)) {
				funcSet.add(varName);
				if(exported == true) isExportEntity.add(varName);
				nameToITree.put(varName, var);
				if(!isNodeJs) typedNameToITree.put(varName, typedVar);
				expNameToToken.put(varName,getFullTokenList(var));
				continue;
			}
			if(isVarClass(var, typedVar, varName)) {
				ITree unTypedClass = fieldNameToITree(var, "init").getChild(1);
				
				ITree typedClass = null;
				if(!isNodeJs) typedClass = fieldNameToITree(typedVar, "init").getChild(1);
				dealWithClass(varName, unTypedClass, typedClass);
				classSet.add(varName + " class");
				if(exported == true) isExportEntity.add(varName + " class");
				nameToITree.put(varName + " class", var);
				if(!isNodeJs) typedNameToITree.put(varName + " class", typedVar);
				expNameToToken.put(varName + " class", getFullTokenList(var));
				String superClass = getSuperClass(unTypedClass);
				if (superClass != null) superClass += " class";
				superClassFinder.put(varName + " class", superClass);
				
				continue;
			}
			
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
			if(object.toShortString().startsWith("NULL")) toBeInit.add(varName);
			/*
			 * var a = new Class()
			 */
			else{
				String typeOfInit = getFieldName(object, "type");
				if (typeOfInit.equals("NewExpression")) {
					String toClass = "";
					ITree callee = multiLevel(object, "callee");
					String typeOfCallee = getFieldName(callee, "type");
					if (typeOfCallee.equals("MemberExpression")) {
						dealWithMember(callee, varName, "");
					}
					else if(typeOfCallee.equals("Identifier")){
						toClass = getFieldName(object, "callee", "name");
						if (funcSet.contains(toClass) || classSet.contains(toClass)) varToClass.put(varName, toClass);
						else if (importVarToJs.containsKey(toClass)) varToClass.put(varName, importVarToJs.get(toClass));
						else if (importEntityToJs.containsKey(toClass)) varToClass.put(varName, importEntityToJs.get(toClass));
					}
				}
			}
			varList.add(varName);
			nameToITree.put(varName, var);
			if(!isNodeJs) typedNameToITree.put(varName, typedVar);
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
		if(i < 0 && checkToken.size() > 1) {
			ITree init = fieldNameToITree(entity, "init");
			ITree type = fieldNameToITree(init.getChild(1), "type");
			String typeName = removeQuote(type.getChild(1).getChild(0).toShortString().substring(8));
			if(typeName.equals("MemberExpression")) {
				int indexOfEqual = checkToken.indexOf("=");
				String object = checkToken.get(indexOfEqual + 1);
				
				if (importVarToJs.containsKey(object)) {
					String nObject = importVarToJs.get(object);
					for (int j = indexOfEqual + 3; j < checkToken.size(); j++) {
						if (!checkToken.get(j).equals(".")) nObject += "++" + checkToken.get(j); 
					}
					importVarToJs.put(varName, nObject);
					importIndexCheck.put(varName, nObject + "++" + "index");
					return true;
				}
				else if (importEntityToJs.containsKey(object)) {
					String nObject = importEntityToJs.get(object);
					for (int j = indexOfEqual + 3; j < checkToken.size(); j++) {
						if (!checkToken.get(j).equals(".")) nObject += "++" + checkToken.get(j); 
					}
					importEntityToJs.put(varName, nObject);
					importIndexCheck.put(varName, nObject + "++" + "index");
					return true;
				}
			}
			return false;
		}

		if(i > 0 && i < checkToken.size() - 1 && checkToken.get(i - 1).equals("=") && checkToken.get(i + 1).equals("(")) {
			
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
				
				else if(jsFile.charAt(0) == '.'|| isNodeJs == true){
					System.out.println(varName);
					System.out.println(getPath(jsFilePath, jsFile) + "++" + nameOfEntity);
					String getPathSource = null;
					if(isNodeJs == true && jsFile.charAt(0) != '.') {
						getPathSource = "lib" + "++" + jsFile.replace("/", "++");
					}
					
					else getPathSource = getPath(jsFilePath, jsFile);
					importEntityToJs.put(varName, getPathSource + "++" + nameOfEntity);
					importIndexCheck.put(varName, getPathSource + "++" + "index++" + nameOfEntity);
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
	
	public boolean isVarFunction(ITree var, ITree typedVar, String varName) {
		ITree typedId = null;
		if(!isNodeJs) typedId = typedVar.getChild(1);
		ITree init = var.getChild(2);
		ITree typedInit = null;
		if(!isNodeJs) typedInit = typedVar.getChild(2);
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
		
//		the return type of this callExpression is "function"
//		Another case like var a = (function f1{})();
		if(!isNodeJs && typeOfVar.contains("CallExpression")) {
			boolean callSig = havingCallSigs(typedInit);
			String returnKind = getInferredKind(typedInit);
			if (callSig == true && returnKind.equals("object")) return true;
			callSig = havingCallSigs(typedId);
			returnKind = getInferredKind(typedId);
			if (callSig == true && returnKind.equals("object")) return true;
			
			if (returnKind.equals("typeof")) {
				System.out.println("true");
				String expression = getInferredExpression(typedId);
				System.out.println(expression);
				if (expression.equals(varName)) return true;
			}
		}
		

		
		return false;
	}
	
	/*
	 * shape like
	 * field
	 * 		a
	 *      b
	 *      inferredType
	 *      			kind
	 *      			name
	 *Here this entity is the field
	 */
	private String getInferredExpression(ITree entity) {
		ITree inferred = fieldNameToITree(entity.getChild(1), "inferredType");
		ITree obj = inferred.getChild(1);
		ITree expression = fieldNameToITree(obj, "expression");
		
		if (expression == null) return "";
		String kindStr = expression.getChild(1).getChild(0).toShortString().substring(8).replace("\"", "");
		
		return kindStr;
		
	}
	
	private String getInferredKind(ITree entity) {
		ITree inferred = fieldNameToITree(entity.getChild(1), "inferredType");
		ITree obj = inferred.getChild(1);
		ITree kind = fieldNameToITree(obj, "kind");
		
		
		String kindStr = kind.getChild(1).getChild(0).toShortString().substring(8).replace("\"", "");
		
		return kindStr.toLowerCase();
		
	}
	
	private boolean havingCallSigs(ITree entity) {
		ITree inferred = fieldNameToITree(entity.getChild(1), "inferredType");
		ITree obj = inferred.getChild(1);
		return hasTheField(obj, "callSignatures");
	}
	
	public boolean isVarClass(ITree var, ITree typedVar, String varName) {
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
			return true;
		}
		return false;
	}
//	Here need to deal with module.exports.a = .......; (done)
//	Need to deal with module.exports.a = a;(a is not a variable) (done)
	public boolean isExport(ITree exp, ITree typedExp) {
		List<String> checkToken = getFullTokenList(exp);
//		for(String s : checkToken) {
//			System.out.println(s);
//		}
		
		if(checkToken.size() > 5 && checkToken.get(0).equals("module") && checkToken.get(2).equals("exports")) {
			if(checkToken.size() == 7 && checkToken.get(4).equals(checkToken.get(6))) return true;
			exportVarName = "exportEntity";
			if (checkToken.get(3).equals(".")) exportVarName = checkToken.get(4);
			ITree right = fieldNameToITree(exp, "right");
			ITree typedRight = null;
			if(!isNodeJs) typedRight = fieldNameToITree(typedExp, "right");
			ITree typeOfRight = fieldNameToITree(right.getChild(1), "type");
			String entityType = removeQuote(typeOfRight.getChild(1).getChild(0).toShortString().substring(8));
			ITree typedObject = null;
			if(!isNodeJs) typedObject = typedRight.getChild(1);
			
			if(checkToken.get(4).equals("function") || entityType.contains("FunctionExpression")){
				funcSet.add(exportVarName);
				isPureExportEntity.add(exportVarName);
				nameToITree.put(exportVarName, exp);
				if(!isNodeJs) typedNameToITree.put(exportVarName, typedExp);
				expNameToToken.put(exportVarName, getFullTokenList(exp));
			}
			else if (checkToken.get(4).equals("class") || entityType.equals("ClassExpression")) {
				ITree typedClass = null;
				if(!isNodeJs) typedClass = typedRight.getChild(1);
				dealWithClass(exportVarName, right.getChild(1), typedClass);
				classSet.add(exportVarName + " class");
				isPureExportEntity.add(exportVarName + " class");
				nameToITree.put(exportVarName + " class", exp);
				if(!isNodeJs) typedNameToITree.put(exportVarName + " class", typedExp);
				expNameToToken.put(exportVarName + " class", getFullTokenList(exp));
				String superClass = getSuperClass(right.getChild(1));
				if (superClass != null) superClass += " class";
				superClassFinder.put(exportVarName + " class", superClass);
			}
			
			else if (entityType.equalsIgnoreCase("ObjectExpression") &&
					dealWithObjectExport(right.getChild(1), typedObject) == true) {
				System.out.println("This is an object export");
				
				return true;	
			}
			else {
				varSet.add(exportVarName);
				isPureExportEntity.add(exportVarName);
				nameToITree.put(exportVarName, exp);
				if(!isNodeJs) typedNameToITree.put(exportVarName, typedExp);
				expNameToToken.put(exportVarName, getFullTokenList(exp));
			}
			return true;
		}
		
		else if(checkToken.size() >= 5 && checkToken.get(0).equals("exports") && checkToken.get(1).equals(".")) {
			
			String entityName = checkToken.get(2);
			ITree right = fieldNameToITree(exp, "right");
			ITree typedRight = null;
			if(!isNodeJs) typedRight = fieldNameToITree(typedExp, "right");
			ITree typeOfRight = fieldNameToITree(right.getChild(1), "type");
			String entityType = removeQuote(typeOfRight.getChild(1).getChild(0).toShortString().substring(8));
			System.out.println("can it come here?" + entityName);
			
			if(checkToken.get(4).equals("function") || entityType.contains("FunctionExpression")) {
//				System.out.println("This is a function export" + entityName);
				funcSet.add(entityName);
				isPureExportEntity.add(entityName);
				nameToITree.put(entityName, right.getChild(1));
				if(!isNodeJs) typedNameToITree.put(entityName, typedRight.getChild(1));
				expNameToToken.put(entityName, getFullTokenList(exp));
			}
			else if(checkToken.get(4).equals("class") || entityType.equals("ClassExpression")) {
				ITree typedClass = null;
				if(!isNodeJs) typedClass = typedRight.getChild(1);
				dealWithClass(entityName, right.getChild(1), typedClass);
				classSet.add(entityName + " class");
				isPureExportEntity.add(entityName + " class");
				nameToITree.put(entityName + " class", right.getChild(1));
				if(!isNodeJs) typedNameToITree.put(entityName + " class", typedRight.getChild(1));
				expNameToToken.put(entityName + " class", getFullTokenList(exp));
				String superClass = getSuperClass(right.getChild(1));
				if (superClass != null) superClass += " class";
				superClassFinder.put(entityName + " class", superClass);
			}
//			Here needs some more work if the exported name is different from the original name
			else if(checkToken.size() == 5 || entityType.equals("Identifier")) {
				System.out.println("It's an Identifier");
				alterName.put(checkToken.get(4), checkToken.get(2));
				isExportEntity.add(checkToken.get(4));
				return true;
			}
			
			else if (!funcSet.contains(entityName) && !classSet.contains(entityName)){
				varSet.add(entityName);
				isPureExportEntity.add(entityName);
				nameToITree.put(entityName, right.getChild(1));
				if(!isNodeJs) typedNameToITree.put(exportVarName, typedRight.getChild(1));
				expNameToToken.put(entityName, getFullTokenList(exp));
			}
			return true;
		}
		
//		Assume one export
		else if(checkToken.size() > 2 && checkToken.size() < 6 && checkToken.get(0).equals("module") && checkToken.get(2).equals("exports")){
			exportVarName = checkToken.get(4);
			exportVarNames.add(exportVarName);
			isExportEntity.add(exportVarName);
			System.out.println(exportVarName + "is an export");
			return true;
		}
		
		/*
		 * deal with a = module.exports; or a = module.exports = function(){};(or whatever)
		 * or a = exports;
		 */
		else if (checkToken.size() == 3 && checkToken.get(2).equals("exports")) {
			hasModule = true;
			moduleName = checkToken.get(0);
			return true;
		}
		
		else if(checkToken.size() > 4 && checkToken.get(2).equals("module") && checkToken.get(4).equals("exports")) {
			hasModule = true;
			moduleName = checkToken.get(0);
			if (checkToken.size() == 5) return true;
			ITree right = fieldNameToITree(exp, "right").getChild(1);
			ITree typedRight = null;
			if(!isNodeJs) typedRight = fieldNameToITree(typedExp, "right").getChild(1);
			ITree typeOfRight = fieldNameToITree(right, "type");
			String entityType = removeQuote(typeOfRight.getChild(1).getChild(0).toShortString().substring(8));
			if (entityType.equals("AssignmentExpression")) {
				isExport(right, typedRight);
			}
			
			return true;
		}
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
			TestChange2.amdFiles++;
		}	
	}
	
	public String getSuperClass(ITree entity) {
		String result = null;
		
		ITree superClass = fieldNameToITree(entity, "superClass");
		String isNull = superClass.getChild(1).toShortString();
		System.out.println("isNull is real " + isNull);
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

