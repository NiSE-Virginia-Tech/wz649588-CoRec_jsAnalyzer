package wekaPre;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class ARFFGenerator {
	static final private String folder = "/Users/zijianjiang/Documents/NaM/pattern_set/V3/V4/";
	static final private String[] projects = new String[]{"pdf","meteor","habitica","node","serverless","webpack1","react","Ghost"};
	static final private String[] patterns = new String[]{"afcf", "avcf", "cfcf"};
	public static void mainSingle(String[] args) {
		// TODO Auto-generated method stub
		for (String project : projects) {
			if(!project.equals("meteor")) continue;
			for (String pattern : patterns) {
				
				if (!pattern.equals("avcf")) continue;
				String projectFolder = folder + pattern + "/" + project + "/";
				
				for (int i = 0; i < 5; i++){
					
					String csvNameTrain = projectFolder + "main_" + pattern + "DataTotal9_" + project + "_training" + i + ".csv";
					String csvNameTest = projectFolder + "main_" + pattern + "DataTotal9_" + project + "_testing" + i + ".csv";
				
					String arffTrain = projectFolder + "Train" + project + i + ".arff";
					String arffTest = projectFolder + "Test" + project + i + ".arff";
					createFile(arffTrain);
					createFile(arffTest);
					File toWrite = new File(arffTrain);
					File from = new File(csvNameTrain);
					File preFile = new File(projectFolder + project + "Pre");
					writeFile(toWrite, from, preFile);
					toWrite = new File(arffTest);
					from = new File(csvNameTest);
					preFile = new File(projectFolder + project + "Pre");
					writeFile(toWrite, from, preFile);
				}
			}
		}	
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		for (String project : projects) {
			if(!project.equals("node")) continue;
			String projectFolder = folder + "all" + "/" + project + "/";
			
			for (int i = 0; i < 5; i++){
				
				String csvNameTrain = projectFolder + "main_" + "wholeTotal_" + project + "_training" + i + ".csv";
				String csvNameTest = projectFolder + "main_" + "wholeTotal_" + project + "_testing" + i + ".csv";
			
				String arffTrain = projectFolder + "Train" + project + i + ".arff";
				String arffTest = projectFolder + "Test" + project + i + ".arff";
				createFile(arffTrain);
				createFile(arffTest);
				File toWrite = new File(arffTrain);
				File from = new File(csvNameTrain);
				File preFile = new File(projectFolder + project + "Pre");
				writeFile(toWrite, from, preFile);
				toWrite = new File(arffTest);
				from = new File(csvNameTest);
				preFile = new File(projectFolder + project + "Pre");
				writeFile(toWrite, from, preFile);
			}
		}
			
	}
	
	public static void createFile(String fileName) {
		try {
		      File myObj = new File("fileName");
		      if (myObj.createNewFile()) {
		        System.out.println("File created: " + myObj.getName());
		      } else {
		        System.out.println("File already exists.");
		      }
		    } catch (IOException e) {
		      System.out.println("An error occurred.");
		      e.printStackTrace();
		    }
	}
	
	private static String getFileContent(File f) throws IOException {
		String content = null;
		Scanner s = new Scanner(f);
        if (s.useDelimiter("\\Z").hasNext()) {
            content = s.useDelimiter("\\Z").next();
        }
        s.close();
        return content;
	}
	
	public static void writeFile(File toFile, File fromFile, File preFile) {
		 try {
			  String pre = getFileContent(preFile);
			  System.out.println(pre);
			  String data = getFileContent(fromFile);
		      FileWriter myWriter = new FileWriter(toFile);
		      
		      myWriter.write(pre);
		      myWriter.write("\n");
		      myWriter.write(data);
		      myWriter.close();
		      System.out.println("Successfully wrote to the file.");
		    } catch (IOException e) {
		      System.out.println("An error occurred.");
		      e.printStackTrace();
		    }
	}

}
