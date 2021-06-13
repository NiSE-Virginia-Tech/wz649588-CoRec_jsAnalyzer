package vt.edu.graph;

import jstest.TraverseJson;
import jstest.TraverseJsonTyped;

import com.github.gumtreediff.tree.ITree;

public class ClientMember {
	public TraverseJsonTyped tJroot;
	public ITree node;
	public String name;
	public String path;
	public String sig;
	public int start;
	public ITree typedNode;
	public String exportedSig;
	public String exportedName;
	
//	public ClientMember(String name, ITree node, String path){
//		this.name = name;
//		this.node = node;
//		this.path = path;
//		this.sig = path + "++" + name;
//	}
	
//	public ClientMember(String name, ITree node, String path, TraverseJson tJroot){
//		this.name = name;
//		this.node = node;
//		this.path = path;
//		this.sig = path + "++" + name;
//		this.tJroot = tJroot;
//		this.start = tJroot.getObjStartPos(node);
//	}
	
	public ClientMember(String name, ITree node, ITree typedNode, String path, TraverseJsonTyped tJroot){
		this.name = name;
		this.node = node;
		this.path = path;
		this.typedNode = typedNode;
		
		this.tJroot = tJroot;
		this.start = tJroot.getObjStartPos(node);
		this.sig = path + "++" + name;
		this.exportedSig = sig;
		this.exportedName = name;
		if (tJroot.alterName.size() > 0 && tJroot.alterName.containsKey(name)) {
			exportedSig = path + "++" + tJroot.alterName.get(name);
			exportedName = tJroot.alterName.get(name);
		}
	}
	
	
	
	public String getSignature() {
		return sig;
	}
	@Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ClientMember that = (ClientMember) o;
        return this.sig.equals(that.sig);
    }
	
	@Override
	public int hashCode() {
		return sig.hashCode();
	}
}
