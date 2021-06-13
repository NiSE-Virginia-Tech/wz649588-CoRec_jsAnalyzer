package vt.edu.extraction;

import java.nio.file.Path;
import java.util.List;

public class BugGraphPaths {
	// Name of bug
		String name;
		
		// Paths of XML files storing JUNG DirectSparseGraph
		List<Path> xmlPaths;
		
		BugGraphPaths(String name, List<Path> xmlPaths) {
			this.name = name;
			this.xmlPaths = xmlPaths;
		}
}
