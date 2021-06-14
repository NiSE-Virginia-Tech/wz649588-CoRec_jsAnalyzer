package vt.edu.util;


import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CodeLineCounter {
	
	private Path root;
	
	private List<Path> javaScriptFiles = new ArrayList<>();
	
	private int line = 0;
	
	public CodeLineCounter(String folder) {
		root = FileSystems.getDefault().getPath(folder);
	}

	private void listJavaScriptFiles(Path p) {
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(p)) {
		    for (Path file: stream) {
		    	if (Files.isDirectory(file)) {
		    		listJavaScriptFiles(file);
		    	} else if (file.getFileName().toString().endsWith(".js")) {
		    		javaScriptFiles.add(file);
//		    		System.out.println("Add File: " + file.toString());
		    	}
		    }
		} catch (IOException | DirectoryIteratorException x) {
		    // IOException can never be thrown by the iteration.
		    // In this snippet, it can only be thrown by newDirectoryStream.
		    System.err.println(x);
		}
	}
	
	public int getCodeLine() {
		listJavaScriptFiles(root);
		
		for (Path javaScriptFile: javaScriptFiles) {
//			System.out.println("Processing File: " + javaFile.toString());
			Charset charset = Charset.forName("UTF-8");
			int currentLine = 0;
			try (BufferedReader reader = Files.newBufferedReader(javaScriptFile, charset)) {
			    while (reader.readLine() != null) {
			    	currentLine++;
			    }
			} catch (IOException x) {
			    System.err.format("IOException: %s%n", x);
			}
//			System.out.println(currentLine);
			line += currentLine;
//			System.out.println(line);
			
		}
		
		return line;
	}
	
	
	
	public static void main(String[] args) {
		String root = "/Users/Zijianjiang/test-repos/check/";
		String[] projects = {"node", "meteor", "react","habitica","pdf","serverless","electron"
				,"webpack","storybook","ghost"};
		for (String project: projects) {
			String folder = root + project;
			System.out.println(project);
			CodeLineCounter counter = new CodeLineCounter(folder);
			int codeLine = counter.getCodeLine();
			System.out.println(codeLine);
		}
	}
}
