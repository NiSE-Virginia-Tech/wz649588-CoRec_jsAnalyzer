package jstest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import vt.edu.graph.ClientClass;
import vt.edu.graph.ClientFunc;
import vt.edu.graph.ClientVar;

import com.github.gumtreediff.gen.antlr3.json.AntlrJsonTreeGenerator;
import com.github.gumtreediff.tree.ITree;

public class typedTraverseTest {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String folder = "/Users/zijianjiang/Documents/R2C/test/v2/ast-consumer/";
		String commit = "12345679/to/";
		String fileName = "test.js.ast.json";
		String jsFolder = "/Users/zijianjiang/test-repos/test/test-commits/v2/";
		String jsFileName = "test.js";
		jsToJson jJ = new jsToJson(jsFolder + commit + jsFileName);
//		jsToJson jJ = new jsToJson("/Users/zijianjiang/Documents/js/test.js");
		File rFile = new File("/Users/zijianjiang/Documents/js/test.js");
		String contentOld = jJ.cmdForEs();
//		System.out.println(contentOld);
		File typedTraversed = new File(folder + commit + fileName);
//		File typedTraversed = new File("/Users/zijianjiang/Documents/esprima/another2/test.js.ast.json");
//		System.out.println(new File("/Users/zijianjiang/Documents/ast-consumer-1").exists());
		String contentOfTyped = getFileContent(rFile);
//		ITree typedTree = new AntlrJsonTreeGenerator().generateFrom().string(contentOfTyped).getRoot();
//		ITree typedTree = null;
		ITree oTree = new AntlrJsonTreeGenerator().generateFrom().string(contentOfTyped).getRoot();
		
		TraverseJsonTyped tJ = new TraverseJsonTyped(null, "test.js", oTree);
		for (String s : tJ.funcSet) System.out.println(s + " OK");
		
//		AllTypeGetter getter = new AllTypeGetter(typedTree);
		
//		System.out.println(tJ.hasModule);
//		System.out.println("moduleName is " + tJ.moduleName);
//		System.out.println(tJ.funcSet.size() + " functions");
//		System.out.println(tJ.varSet.size() + " variables");
//		for (String func : tJ.varSet) System.out.println(func);
//		
//		for (String key : tJ.varToClass.keySet()) {
//			System.out.println(key + " to " + tJ.varToClass.get(key));
//		}
//		
//		for (String key : tJ.importEntityToJs.keySet()) {
//			System.out.println(key + " to " + tJ.importEntityToJs.get(key));
//		}
//		
//		
//		ClientVar v1 = new ClientVar("xx", oTree, typedTree, "path", tJ);
//		System.out.println(v1.exportedName);
//		
//		ChangeFact cf = new ChangeFact(null, rFile, null, tJ);
//		for (ClientClass var : cf.insertedClasses) {
//			System.out.println(var.sig);
//		}
//		List<ITree> entities = oTree.getChild(1).getChild(1).getChildren();
//		List<ITree> typedEntities = typedTree.getChild(1).getChild(1).getChildren();
//		
//		
//		System.out.println(typedEntities.size());
//		System.out.println(entities.size());
//		System.out.println(typedTraversed.exists());
//		
//		ITree test = entities.get(4);
//		ITree typedTest = typedEntities.get(4);
//		List<String> varList = new ArrayList<>();
//		ITree varArray = test.getChild(1).getChild(1);
//		ITree typedVarArray = typedTest.getChild(1).getChild(1);
//		List<ITree> vars = varArray.getChildren();
//		List<ITree> typedVars = typedVarArray.getChildren();
//		for(int i = 0; i < vars.size(); i++){
//			ITree var = vars.get(i);
//			ITree typedVar = typedVars.get(i);
//			String varName = var.getChild(1).getChild(1).getChild(1).getChild(1).getChild(0).toShortString().substring(8).replace("\"", "");
//			String typedVarName = typedVar.getChild(1).getChild(1).getChild(1).getChild(1).getChild(0).toShortString().substring(8).replace("\"", "");
//			System.out.println(varName);
//			System.out.println(typedVarName);
//			
//			ITree init = var.getChild(2);
//			ITree typedInit = typedVar.getChild(1);
//			
//			System.out.println(getInferredKind(typedInit));
//			
//			if(!init.getChild(0).toShortString().substring(8).equals("\"init\"")) {
//				for(ITree tmp : var.getChildren()) {
//					if(tmp.getChild(0).toShortString().substring(8).equals("\"init\"")) {
//						init = tmp;
//						break;
//					}
//				}
//			}
//			if(!typedInit.getChild(0).toShortString().substring(8).equals("\"init\"")) {
//				for(ITree tmp : typedVar.getChildren()) {
//					if(tmp.getChild(0).toShortString().substring(8).equals("\"init\"")) {
//						typedInit = tmp;
//						break;
//					}
//				}
//			}
//			ITree object = init.getChild(1);
//			ITree typedObject = typedInit.getChild(1);
//			String typeOfVar = object.getChild(0).getChild(1).getChild(0).toShortString();
//			String typedTypeOfVar = typedObject.getChild(0).getChild(1).getChild(0).toShortString();
//			System.out.println(typeOfVar);
//			System.out.println(typedTypeOfVar);
//		}
		
		List<String> a = new ArrayList<>();
		a.add("any");
		a.add("tr");
		a.add("tr");
		List<String[]> b = new ArrayList<>();
		b.add(new String[]{"any", ""});
		b.add(new String[]{"any", ""});
		b.add(new String[]{"tr", ""});
		b.add(new String[]{"asdf", ""});
		CFCFCommon common = new CFCFCommon(null);
		System.out.println(common.containHowManyParameters(a,b));
		
		
	}
	
	private static String getInferredKind(ITree entity) {
		ITree inferred = fieldNameToITree(entity.getChild(1), "inferredType");
		ITree obj = inferred.getChild(1);
		ITree kind = fieldNameToITree(obj, "kind");
		
		
		String kindStr = kind.getChild(1).getChild(0).toShortString().substring(8).replace("\"", "");
		
		return kindStr.toLowerCase();
		
	}
	
	private static String getInferredName(ITree entity) {
		ITree inferred = fieldNameToITree(entity.getChild(1), "inferredType");
		ITree obj = inferred.getChild(1);
		ITree name = fieldNameToITree(obj, "name");
		String nameStr = null;
		if (name != null) {
			nameStr = name.getChild(1).getChild(0).toShortString().substring(8).replace("\"", "");
			return nameStr.toLowerCase();
		}
		return null;
	}
	
	public static ITree fieldNameToITree (ITree node, String name) {
		ITree result = null;
		for(ITree tmp : node.getChildren()) {
			if(tmp.getChild(0).toShortString().substring(8).equals("\"" + name + "\"")) {
				result = tmp;
				return result;
			}
		}
		return result;
	}
	
	private static String getFileContent(File f) throws IOException {
		String content = null;
		Scanner s = new Scanner(f);
        if (s.useDelimiter("\\Z").hasNext()) {
            content = s.useDelimiter("\\Z").next();
        }
        s.close();
        return content;
	}

}
