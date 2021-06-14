package vt.edu.graph;

import jstest.TraverseJson;
import jstest.TraverseJsonTyped;

import com.github.gumtreediff.tree.ITree;

public class ClientClass extends ClientMember{
//	public TraverseJson tJroot;
//	public ITree node;
//	public String className;
//	public String path;
//	public String sig;
//	public int start;
	
//	public ClientClass(String className, ITree node, String path){
//		this.className = className;
//		this.node = node;
//		this.path = path;
//		this.sig = path + "++" + className;
//	}
	
//	public ClientClass(String name, ITree node, String path, TraverseJson tJroot){
//		super(name, node, path, tJroot);
//	}
	
	public ClientClass(String name, ITree node, ITree typedNode, String path, TraverseJsonTyped tJroot) {
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
        ClientClass that = (ClientClass) o;
        return this.sig.equals(that.sig);
    }
	
	@Override
	public int hashCode() {
		return sig.hashCode();
	}
	
//	public String getSuperClass() {
//		String result = null;
//		
//		
//		
//		return result;
//	}
}
