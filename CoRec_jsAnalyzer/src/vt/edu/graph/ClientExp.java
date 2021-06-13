package vt.edu.graph;

import jstest.TraverseJson;
import jstest.TraverseJsonTyped;

import com.github.gumtreediff.tree.ITree;

public class ClientExp extends ClientMember{
//	public ITree node;
//	public String expName;
//	public String path;
//	public String sig;
//	public TraverseJson tJroot;
//	public int start;
	
//	public ClientExp(String expName, ITree node, String path){
//		this.expName = expName;
//		this.node = node;
//		this.path = path;
//		this.sig = path + "++" + expName;
//	}
	
	public ClientExp(String name, ITree node, ITree typedNode, String path, TraverseJsonTyped tJroot){
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
        ClientExp that = (ClientExp) o;
        return this.sig.equals(that.sig);
    }
	
	@Override
	public int hashCode() {
		return sig.hashCode();
	}
}
