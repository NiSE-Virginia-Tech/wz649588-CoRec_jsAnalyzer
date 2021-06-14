package vt.edu.graph;

import org.jgrapht.Graph;

import vt.edu.graph.GraphDataJson.GraphEdge;
import vt.edu.graph.GraphDataJson.GraphNode;

public class GraphPair {

	public String bn1;
	public String bn2;
	
	public int gn1;
	public int gn2;
	
	public Graph<GraphNode, GraphEdge> g1;
	public Graph<GraphNode, GraphEdge> g2;
	
	public Graph<GraphNode, GraphEdge> sg1;
	public Graph<GraphNode, GraphEdge> sg2;
}
