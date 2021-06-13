package vt.edu.graph;

import jstest.TraverseJson;
import jstest.TraverseJsonTyped;

import com.github.gumtreediff.tree.ITree;

public class ClientVar extends ClientMember{
//	public ITree node;
//	public String varName;
//	public String path;
//	public String sig;
//	public TraverseJson tJroot;
//	public int start = 0;
//	
//	public ClientVar(String varName, ITree node, String path){
//		this.varName = varName;
//		this.node = node;
//		this.path = path;
//		this.sig = path + "++" + varName;
//	}
	
	
	public ClientVar(String name, ITree node, ITree typedNode, String path, TraverseJsonTyped tJroot){
		super(name, node, typedNode, path, tJroot);
	}
	
	
//	public String getSignature() {
//		return sig;
//	}
	
	@Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ClientVar that = (ClientVar) o;
        return this.sig.equals(that.sig) && (this.start == that.start);
    }
	
	@Override
	public int hashCode() {
		return (sig + start).hashCode();
	}
}
