package vt.edu.extraction;

import java.util.Comparator;

import vt.edu.graph.ReferenceEdge;

public class EdgeComparator implements Comparator<ReferenceEdge>{
	/**
	 * Check whether two ReferenceEdges are of the same type
	 * @param e1 first ReferenceEdge
	 * @param e2 second ReferenceEdge
	 * @return 0 if two edges are of the same type, otherwise a non-zero value 
	 */
	@Override
	public int compare(ReferenceEdge e1, ReferenceEdge e2) {
		return ((e1.type == e2.type) && (e1.dep == e2.dep)) ? 0 : 1;
	}
}
