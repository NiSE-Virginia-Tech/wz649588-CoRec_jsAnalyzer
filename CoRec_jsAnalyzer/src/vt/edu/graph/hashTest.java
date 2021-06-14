package vt.edu.graph;

import java.util.HashSet;
import java.util.Set;

public class hashTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Set<ClientVar> varSet = new HashSet<ClientVar>();
		ClientVar cv0 = new ClientVar("deft", null, null, "dsafdsf", null);
		ClientVar cv1 = new ClientVar("deft", null, null, "dsafdsd", null);
		varSet.add(cv0);
		System.out.println(varSet.add(cv1));
	}

}
