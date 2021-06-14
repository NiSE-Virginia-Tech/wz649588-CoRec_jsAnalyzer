package jstest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.gumtreediff.tree.ITree;

public class AllTypeGetter {
	private Set<String> typeSet;
	private List<String> typeList;
	private ITree root;
	public AllTypeGetter (ITree root) {
		this.typeSet = new HashSet<>();
		this.typeList = new ArrayList<>();
		this.root = root;
		traverseNode(root);
		System.out.println(typeList.size());
		for(String type : typeList) System.out.println(type);
	}
	
	public boolean hasTheField (ITree node, String name) {
		for (ITree tmp : node.getChildren()) {
			if(tmp.getChildren().size() < 1) continue;
			if(tmp.getChild(0).toShortString().substring(8).equals("\"" + name + "\"")) return true;
		}
		return false;
	}
	public String getFieldName(ITree p, String... s) {
		
		for (String field : s) {
			p = fieldNameToITree(p, field).getChild(1);
		}
		return removeQuote(p.getChild(0).toShortString().substring(8));
	}
	
	String getType(ITree entity) {
		ITree type = fieldNameToITree(entity, "type");
		if (type == null) return "";
		return removeQuote(type.getChild(1).getChild(0).toShortString().substring(8));
	}
	
	public ITree multiLevel(ITree p, String... s){
		for (String field : s) {
			p = fieldNameToITree(p, field).getChild(1);
		}
		return p;
	}
	
	public String removeQuote(String s){
		int len = s.length();
		return s.substring(1, len - 1);
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
	
	public void traverseNode(ITree node) {
//		if(node.getHeight() < 6) return;
//		System.out.println(node.getChild(1).getChild(1).getChild(5).getChild(2).getHeight());
		for (ITree child : node.getChildren()) {
			System.out.println(child.getHeight());
			if (child.getChildren().size() < 1) continue;
			if (child.getChild(0).toShortString().contains("\"" + "inferredType" + "\"")) {
				ITree infer = child.getChild(1);
				if (!hasTheField(infer, "callSignatures")) {
					String kind = getFieldName(infer, "kind");
					if (!kind.equals("nominative")) {
						if (!kind.equals("any")){
							typeSet.add(kind);
							typeList.add(kind);
						}
					}
					else {
						ITree fully = multiLevel(infer, "fullyQualifiedName");
						
						ITree builtinNode = fieldNameToITree(fully, "builtin");
						String builtin = builtinNode.getChild(1).toShortString().substring(0,5);
						String fullyKind = getFieldName(fully, "name");
						System.out.println("what is " + builtin);
						if (builtin.equals("FALSE")) {
							String fileName = getFieldName(fully, "fileName");
							fullyKind = fileName + " " + fullyKind;
						}
						
						
						
						if (!fullyKind.contains("NodeRequire") && !fullyKind.equals("any")) {
							typeSet.add(fullyKind);
							typeList.add(fullyKind);
						}
						System.out.println(fullyKind);
					}
				}
				else {
					List<ITree> callSigsList = multiLevel(infer, "callSignatures").getChildren();
					if(callSigsList.size() < 1) continue;
					ITree callSig = callSigsList.get(0);
					String funcType = getFieldName(callSig, "returnType", "kind");
					System.out.println(funcType);
					if (!funcType.equals("void") && !funcType.equals("any")) {
						typeSet.add(funcType);
						typeList.add(funcType);
					}
				}
			}
			traverseNode(child);
		}
	}
	
	public Set<String> getTypeSet() {
		return typeSet;
	}
	
	public List<String> getTypeList() {
		return typeList;
	}
	
}
