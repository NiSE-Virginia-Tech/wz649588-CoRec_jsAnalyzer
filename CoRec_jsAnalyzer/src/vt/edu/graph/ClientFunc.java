package vt.edu.graph;

import jstest.TraverseJson;
import jstest.TraverseJsonTyped;

import com.github.gumtreediff.tree.ITree;

public class ClientFunc extends ClientMember{
//	public ITree node;
//	public String funcName;
//	public String path;
//	public String sig;
//	public TraverseJson tJroot;
//	public int start;
//	
//	public ClientFunc(String funcName, ITree node, String path){
//		this.funcName = funcName;
//		this.node = node;
//		this.path = path;
//		this.sig = path + "++" + funcName;
//	}
	
	
	public ClientFunc(String name, ITree node, ITree typedNode, String path, TraverseJsonTyped tJroot){
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
        ClientFunc that = (ClientFunc) o;
        return this.sig.equals(that.sig);
    }
	
	@Override
	public int hashCode() {
		return sig.hashCode();
	}
}
