package vt.edu.extraction;

import java.util.Comparator;

import vt.edu.graph.ReferenceNode;

public class VertexComparator implements Comparator<ReferenceNode>{

	/**
	 * Check whether two ReferenceNodes are of the same type
	 * @param v1 first ReferenceNode
	 * @param v2 second ReferenceNode
	 * @return 0 if two nodes are of the same type, otherwise a non-zero value
	 */
	@Override
	public int compare(ReferenceNode v1, ReferenceNode v2) {
		return v1.type - v2.type;
	}
}
