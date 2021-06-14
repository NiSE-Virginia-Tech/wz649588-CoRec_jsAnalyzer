/*
 * random forest 
 */
package wekaPre;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import vt.edu.sql.SqliteManager;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;


/**
 * @author Zijian Jiang
 *
 */
/**
 * @author Zijian Jiang
 *
 */
public class WekaTest {

	/** file names are defined*/
	public static String TRAINING_DATA_SET_FILENAME="/Users/zijianjiang/Documents/NaM/pattern_set/V3/V4/afcf/meteorTrain1.arff";
	public static String TESTING_DATA_SET_FILENAME="/Users/zijianjiang/Documents/NaM/pattern_set/V3/V4/afcf/meteorTest1.arff";
	public static int count = 0, tp = 0, pre = 0;
	public static Map<String, HashSet<String>> predicted;
	public static Map<String, HashSet<String>> truePositive;
	public static Map<String, HashSet<String>> ground;

	/**
	 * This method is to load the data set.
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public static Instances getDataSet(String fileName, boolean isNode) throws IOException {
		
		/**
		 * we can set the file i.e., loader.setFile("finename") to load the data
		 */
		
		int classIdx = 3; //or 8
		if (isNode) classIdx = 3; //for Node
		/** the arffloader to load the arff file */
		ArffLoader loader = new ArffLoader();
		/** load the traing data */
//		loader.setSource(RandomForestDemo.class.getResourceAsStream(fileName));
		/**
		 * we can also set the file like loader3.setFile(new
		 * File("test-confused.arff"));
		 */
		loader.setFile(new File(fileName));
		Instances dataSet = loader.getDataSet();
		/** set the index based on the data given in the arff files */
		dataSet.setClassIndex(classIdx);
//		System.out.println(dataSet);
		return dataSet;
	}

	/**
	 * This method is used to process the input and return the statistics.
	 * 
	 * @throws Exception
	 */
	public void processJ48() throws Exception {

		Instances trainingDataSet = getDataSet(TRAINING_DATA_SET_FILENAME, false);
		Instances testingDataSet = getDataSet(TESTING_DATA_SET_FILENAME, false);
		
		
		
		J48 j48 = new J48();
//		forest.setNumTrees(100);
		FilteredClassifier fc = new FilteredClassifier();
		fc.setClassifier(j48);
		Remove rm = new Remove();
//		rm.setAttributeIndices("6");
		rm.setAttributeIndices("1-3");
		fc.setFilter(rm);
		
		/** */
		fc.buildClassifier(trainingDataSet);
		/**
		 * train the alogorithm with the training data and evaluate the
		 * algorithm with testing data
		 */
		Evaluation eval = new Evaluation(trainingDataSet);
		eval.evaluateModel(fc, testingDataSet);
		
		
		/** Print the algorithm summary */
		System.out.println("** Decision Tress Evaluation with Datasets **");
		System.out.println(eval.toSummaryString());
		System.out.print(" the expression for the input data as per alogorithm is ");
		System.out.println(j48);
		System.out.println(eval.toMatrixString());
		 System.out.println(eval.toClassDetailsString());
		
	}
	
	public void processRF() throws Exception {

		Instances trainingDataSet = getDataSet(TRAINING_DATA_SET_FILENAME, false);
		Instances testingDataSet = getDataSet(TESTING_DATA_SET_FILENAME, false);
		
		
		
		RandomForest forest=new RandomForest();
//		forest.setNumTrees(100);
		FilteredClassifier fc = new FilteredClassifier();
		fc.setClassifier(forest);
		Remove rm = new Remove();
//		rm.setAttributeIndices("6");
		rm.setAttributeIndices("1-3");
		fc.setFilter(rm);
		
		/** */
		fc.buildClassifier(trainingDataSet);
		/**
		 * train the alogorithm with the training data and evaluate the
		 * algorithm with testing data
		 */
		Evaluation eval = new Evaluation(trainingDataSet);
		eval.evaluateModel(fc, testingDataSet);
		
		
		/** Print the algorithm summary */
		System.out.println("** Decision Tress Evaluation with Datasets **");
		System.out.println(eval.toSummaryString());
		System.out.print(" the expression for the input data as per alogorithm is ");
		System.out.println(forest);
		System.out.println(eval.toMatrixString());
		 System.out.println(eval.toClassDetailsString());
		
	}
	
	public void processAB() throws Exception {

		Instances trainingDataSet = getDataSet(TRAINING_DATA_SET_FILENAME, false);
		Instances testingDataSet = getDataSet(TESTING_DATA_SET_FILENAME, false);
		
		
		
		AdaBoostM1 adaBoost = new AdaBoostM1();
		RandomForest rf = new RandomForest();
		adaBoost.setClassifier(rf);
//		forest.setNumTrees(100);
		FilteredClassifier fc = new FilteredClassifier();
		fc.setClassifier(adaBoost);
		Remove rm = new Remove();
//		rm.setAttributeIndices("6");
		rm.setAttributeIndices("1-3,10");
		fc.setFilter(rm);
		
		/** */
		fc.buildClassifier(trainingDataSet);
		/**
		 * train the alogorithm with the training data and evaluate the
		 * algorithm with testing data
		 */
		Evaluation eval = new Evaluation(trainingDataSet);
		eval.evaluateModel(fc, testingDataSet);
		
		
		/** Print the algorithm summary */
		System.out.println("** Decision Tress Evaluation with Datasets **");
		System.out.println(eval.toSummaryString());
		System.out.print(" the expression for the input data as per alogorithm is ");
		System.out.println(adaBoost);
		System.out.println(eval.toMatrixString());
		 System.out.println(eval.toClassDetailsString());
		
	}
	
	public void processNB() throws Exception {

		Instances trainingDataSet = getDataSet(TRAINING_DATA_SET_FILENAME, false);
		Instances testingDataSet = getDataSet(TESTING_DATA_SET_FILENAME, false);
		
		NaiveBayes naiveBayes = new NaiveBayes();
//		forest.setNumTrees(100);
		FilteredClassifier fc = new FilteredClassifier();
		fc.setClassifier(naiveBayes);
		Remove rm = new Remove();
//		rm.setAttributeIndices("6");
		rm.setAttributeIndices("1-3");
		fc.setFilter(rm);
		
		/** */
		fc.buildClassifier(trainingDataSet);
		/**
		 * train the alogorithm with the training data and evaluate the
		 * algorithm with testing data
		 */
		Evaluation eval = new Evaluation(trainingDataSet);
		eval.evaluateModel(fc, testingDataSet);
		
		
		/** Print the algorithm summary */
		System.out.println("** Decision Tress Evaluation with Datasets **");
		System.out.println(eval.toSummaryString());
		System.out.print(" the expression for the input data as per alogorithm is ");
		System.out.println(naiveBayes);
		System.out.println(eval.toMatrixString());
		 System.out.println(eval.toClassDetailsString());
		
	}
	
	public void processRFFilter(boolean isNode) throws Exception {
		Instances trainingDataSet = getDataSet(TRAINING_DATA_SET_FILENAME, isNode);
		int numFolds = 2;
		RandomForest forest=new RandomForest();
//		forest.setNumTrees(100);
		FilteredClassifier fc = new FilteredClassifier();
		fc.setClassifier(forest);
		Remove rm = new Remove();
//		rm.setAttributeIndices("6");
		rm.setAttributeIndices("1-3, 5-6");
		fc.setFilter(rm);
		Evaluation eval = new Evaluation(trainingDataSet);
		eval.crossValidateModel(fc, trainingDataSet, numFolds, new Random(1));
		System.out.println("** Decision Tress Evaluation with Datasets **");
		System.out.println(eval.toSummaryString());
		System.out.print(" the expression for the input data as per alogorithm is ");
		System.out.println(forest);
		System.out.println(eval.toMatrixString());
		 System.out.println(eval.toClassDetailsString());
	}
	
	static String CPRF = "whole_CPRF_abrfnewnew";
	static int covered_total = 0;
	static double C = 0 , P = 0, R = 0, F = 0;
	
	public static void mainsingle(String[] args) throws Exception {
		boolean recreate = false;
//		String CPRF = "j48_CPRF_cfcf";
		Connection conn = SqliteManager.getConnection();
		Statement stmt = conn.createStatement();
		if (recreate){
			stmt.executeUpdate("DROP TABLE IF EXISTS " + CPRF);
		}
		stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + CPRF + "(project TEXT,C DOUBLE,P DOUBLE,R DOUBLE,F DOUBLE,covered INTEGER,total INTEGER)");
		stmt.close();
		conn.close();
		
		WekaTest weTest = new WekaTest();
		String[] projects = new String[]{"pdf","meteor","habitica","serverless","webpack1","react","Ghost","node"};
		String[] patterns = new String[]{"afcf", "avcf", "cfcf"};
//			if (!project.equals("ndoe")) continue;
		for (String pattern : patterns) {
			if (!pattern.equals("afcf")) continue;
			for (String project : projects) {
				System.out.println(project);
				if (!project.equals("react")) continue;
				predicted = new HashMap<>();
				truePositive = new HashMap<>();
				ground = TPMap.getMap(pattern + "DataTotal9_" + project);
				
				
				for (int i = 0; i < 5; i++) {
					TRAINING_DATA_SET_FILENAME="/Users/zijianjiang/Documents/NaM/pattern_set/V3/V4/" + pattern + "/" + project + "/" + "Train" + project + i + ".arff";
					TESTING_DATA_SET_FILENAME="/Users/zijianjiang/Documents/NaM/pattern_set/V3/V4/" + pattern + "/" + project + "/" + "Test" + project + i + ".arff";
					weTest.processAB();
					System.out.println("Finish" + i);
				}
				CPRF(project);
			}
		}
		
//		System.out.println(C/covered_total + " " + P/covered_total + " " + R/covered_total + " " + F/covered_total);
		
	}
	
	public static void main(String[] args) throws Exception {
		boolean recreate = true;
//		String CPRF = "j48_CPRF_cfcf";
		Connection conn = SqliteManager.getConnection();
		Statement stmt = conn.createStatement();
		if (recreate){
			stmt.executeUpdate("DROP TABLE IF EXISTS " + CPRF);
		}
		stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + CPRF + "(project TEXT,C DOUBLE,P DOUBLE,R DOUBLE,F DOUBLE,covered INTEGER,total INTEGER)");
		stmt.close();
		conn.close();
		
		WekaTest weTest = new WekaTest();
		String[] projects = new String[]{"pdf","meteor","habitica","serverless","webpack1","react","Ghost","node"};
		for (String project : projects) {
			System.out.println(project);
			if (project.equals("node")) continue;
			predicted = new HashMap<>();
			truePositive = new HashMap<>();
			ground = TPMap.getMap("wholeCommit_" + project);
			
			
			for (int i = 0; i < 5; i++) {
				TRAINING_DATA_SET_FILENAME="/Users/zijianjiang/Documents/NaM/pattern_set/V3/V4/all/" + project + "/" + "Train" + project + i + ".arff";
				TESTING_DATA_SET_FILENAME="/Users/zijianjiang/Documents/NaM/pattern_set/V3/V4/all/" + project + "/" + "Test" + project + i + ".arff";
				weTest.processAB();
				System.out.println("Finish" + i);
			}
			CPRF(project);
		}
		
		
//		System.out.println(C/covered_total + " " + P/covered_total + " " + R/covered_total + " " + F/covered_total);
		
	}
	
	public void crossValidationProcessIBK(boolean isNode) throws Exception {
		Instances trainingDataSet = getDataSet(TRAINING_DATA_SET_FILENAME, isNode);
		int numFolds = 2;
		IBk ibk = new IBk();
		Evaluation eval = new Evaluation(trainingDataSet);
		eval.crossValidateModel(ibk, trainingDataSet, numFolds, new Random(0));
		System.out.println("** Decision Tress Evaluation with Datasets **");
		System.out.println(eval.toSummaryString());
		System.out.print(" the expression for the input data as per alogorithm is ");
		System.out.println(ibk);
		System.out.println(eval.toMatrixString());
		 System.out.println(eval.toClassDetailsString());
	}
	
	public void crossValidationProcessNB(boolean isNode) throws Exception {
		Instances trainingDataSet = getDataSet(TRAINING_DATA_SET_FILENAME, isNode);
		int numFolds = 10;
		NaiveBayes naiveBayes = new NaiveBayes();
		Evaluation eval = new Evaluation(trainingDataSet);
		eval.crossValidateModel(naiveBayes, trainingDataSet, numFolds, new Random(0));
		System.out.println("** Decision Tress Evaluation with Datasets **");
		System.out.println(eval.toSummaryString());
		System.out.print(" the expression for the input data as per alogorithm is ");
		System.out.println(naiveBayes);
		System.out.println(eval.toMatrixString());
		 System.out.println(eval.toClassDetailsString());
	}
	
	public void crossValidationProcessJ48(boolean isNode) throws Exception {
		Instances trainingDataSet = getDataSet(TRAINING_DATA_SET_FILENAME, isNode);
		int numFolds = 10;
		J48 j48 = new J48();
		Evaluation eval = new Evaluation(trainingDataSet);
		eval.crossValidateModel(j48, trainingDataSet, numFolds, new Random(1));
		System.out.println("** Decision Tress Evaluation with Datasets **");
		System.out.println(eval.toSummaryString());
		System.out.print(" the expression for the input data as per alogorithm is ");
		System.out.println(j48);
		System.out.println(eval.toMatrixString());
		 System.out.println(eval.toClassDetailsString());
	}
	
	public void crossValidationProcessAdaboost(boolean isNode) throws Exception {
		Instances trainingDataSet = getDataSet(TRAINING_DATA_SET_FILENAME, isNode);
		int numFolds = 10;
		AdaBoostM1 adaBoost = new AdaBoostM1();
		Evaluation eval = new Evaluation(trainingDataSet);
		eval.crossValidateModel(adaBoost, trainingDataSet, numFolds, new Random(1));
		System.out.println("** Decision Tress Evaluation with Datasets **");
		System.out.println(eval.toSummaryString());
		System.out.print(" the expression for the input data as per alogorithm is ");
		System.out.println(adaBoost);
		System.out.println(eval.toMatrixString());
		 System.out.println(eval.toClassDetailsString());
	}
	
	
	
	public static void mainTest(String[] args) throws Exception {
		WekaTest weTest = new WekaTest();
		weTest.processRF();
//		String[] projects = new String[]{"pdf","meteor","habitica","electron","serverless","webpack1","react","Ghost","storybook"};
//		String[] patterns = new String[]{"afcf", "avcf", "cfcf"};
//		for (String project : projects) {
//			for (String pattern : patterns) {
////				if (!pattern.equals("avcf")) continue;
////				TRAINING_DATA_SET_FILENAME = "/Users/zijianjiang/Documents/NaM/pattern_set/V1/main_" + pattern + "DataTrainingV1For_" + project + ".arff";
////				TESTING_DATA_SET_FILENAME = "/Users/zijianjiang/Documents/NaM/pattern_set/V1/main_" + pattern + "DataTV1_" + project + ".arff";
//				System.out.println(TRAINING_DATA_SET_FILENAME);
//				
//				weTest.processRF();
//			}
//		}
	}
	
	
	public static void CPRF(String project) {
		int coveredTask = 0;
		double precision = 0, recall = 0;
		for (String key : ground.keySet()) {
			if (predicted.containsKey(key)) {
				coveredTask++;
				if (truePositive.containsKey(key)) {
					precision += truePositive.get(key).size() * 1.0 / predicted.get(key).size();
					recall += truePositive.get(key).size() * 1.0 / ground.get(key).size();
				}
			}
		}
		recall /= coveredTask;
		precision /= coveredTask;
		System.out.println(coveredTask + "/" + ground.size());
		System.out.println("coverage = " + coveredTask *  1.0 / ground.size());
		System.out.println("recall = " + recall);
		System.out.println("precision = " + precision);
		System.out.println("fscore = " + 2 * recall * precision / (recall + precision));
		C += coveredTask * coveredTask *  1.0 / ground.size();
		P += coveredTask * precision;
		R += coveredTask * recall;
		F += coveredTask * 2 * recall * precision / (recall + precision);
		covered_total += coveredTask;
		Connection conn = SqliteManager.getConnection();
		try {
			PreparedStatement ps = conn.prepareStatement("INSERT INTO " + CPRF + " (project,C,P,R,F,covered,total) VALUES (?,?,?,?,?,?,?)");
			ps.setString(1, project);
			ps.setDouble(2, coveredTask * 1.0 / ground.size());
			ps.setDouble(3, recall);
			ps.setDouble(4, precision);
			ps.setDouble(5, 2 * recall * precision / (precision + recall));
			ps.setInt(6,coveredTask);
			ps.setInt(7,ground.size());
			ps.executeUpdate();
			ps.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	
	public static void main0(String[] args) throws Exception {
		WekaTest weTest = new WekaTest();
		weTest.processRF();
	}
	public void crossValidationProcessRF(boolean isNode) throws Exception {
		Instances trainingDataSet = getDataSet(TRAINING_DATA_SET_FILENAME, isNode);
		int numFolds = 2;
		RandomForest forest=new RandomForest();
//		forest.setNumTrees(100);
		Evaluation eval = new Evaluation(trainingDataSet);
		eval.crossValidateModel(forest, trainingDataSet, numFolds, new Random(1));
		System.out.println("** Decision Tress Evaluation with Datasets **");
		System.out.println(eval.toSummaryString());
		System.out.print(" the expression for the input data as per alogorithm is ");
		System.out.println(forest);
		System.out.println(eval.toMatrixString());
		 System.out.println(eval.toClassDetailsString());
	}
	
	public void crossValidationProcessRFFilter(boolean isNode) throws Exception {
		Instances trainingDataSet = getDataSet(TRAINING_DATA_SET_FILENAME, isNode);
		int numFolds = 2;
		RandomForest forest=new RandomForest();
//		forest.setNumTrees(100);
		FilteredClassifier fc = new FilteredClassifier();
		fc.setClassifier(forest);
		Remove rm = new Remove();
//		rm.setAttributeIndices("6");
		rm.setAttributeIndices("1-3, 5-6");
		fc.setFilter(rm);
		Evaluation eval = new Evaluation(trainingDataSet);
		eval.crossValidateModel(fc, trainingDataSet, numFolds, new Random(1));
		System.out.println("** Decision Tress Evaluation with Datasets **");
		System.out.println(eval.toSummaryString());
		System.out.print(" the expression for the input data as per alogorithm is ");
		System.out.println(forest);
		System.out.println(eval.toMatrixString());
		 System.out.println(eval.toClassDetailsString());
	}
	
	public void crossValidationProcessJ48Filter(boolean isNode) throws Exception {
		Instances trainingDataSet = getDataSet(TRAINING_DATA_SET_FILENAME, isNode);
		int numFolds = 2;
//		RandomForest forest=new RandomForest();
		J48 j48 = new J48();
//		forest.setNumTrees(100);
		FilteredClassifier fc = new FilteredClassifier();
		fc.setClassifier(j48);
		Remove rm = new Remove();
//		rm.setAttributeIndices("6");
		rm.setAttributeIndices("1-3");
		fc.setFilter(rm);
		Evaluation eval = new Evaluation(trainingDataSet);
		eval.crossValidateModel(fc, trainingDataSet, numFolds, new Random(1));
		System.out.println("** Decision Tress Evaluation with Datasets **");
		System.out.println(eval.toSummaryString());
		System.out.print(" the expression for the input data as per alogorithm is ");
		System.out.println(j48);
		System.out.println(eval.toMatrixString());
		 System.out.println(eval.toClassDetailsString());
	}
	
	public void crossValidationProcessABFilter(boolean isNode) throws Exception {
		Instances trainingDataSet = getDataSet(TRAINING_DATA_SET_FILENAME, isNode);
		int numFolds = 2;
//		RandomForest forest=new RandomForest();
		AdaBoostM1 adaBoost = new AdaBoostM1();
		
//		forest.seNumTrees(100);
		FilteredClassifier fc = new FilteredClassifier();
		fc.setClassifier(adaBoost);
		Remove rm = new Remove();
//		rm.setAttributeIndices("6");
		rm.setAttributeIndices("1-3");
		fc.setFilter(rm);
		Evaluation eval = new Evaluation(trainingDataSet);
		eval.crossValidateModel(fc, trainingDataSet, numFolds, new Random(1));
		System.out.println("** Decision Tress Evaluation with Datasets **");
		System.out.println(eval.toSummaryString());
		System.out.print(" the expression for the input data as per alogorithm is ");
		System.out.println(adaBoost);
		System.out.println(eval.toMatrixString());
		 System.out.println(eval.toClassDetailsString());
	}
	
	public void crossValidationProcessNBFilter(boolean isNode) throws Exception {
		Instances trainingDataSet = getDataSet(TRAINING_DATA_SET_FILENAME, isNode);
		int numFolds = 2;
		NaiveBayes naiveBayes = new NaiveBayes();
//		RandomForest forest=new RandomForest();
//		forest.setNumTrees(100);
		FilteredClassifier fc = new FilteredClassifier();
		fc.setClassifier(naiveBayes);
		Remove rm = new Remove();
//		rm.setAttributeIndices("6");
		rm.setAttributeIndices("1-3");
		fc.setFilter(rm);
		Evaluation eval = new Evaluation(trainingDataSet);
		eval.crossValidateModel(fc, trainingDataSet, numFolds, new Random(1));
		System.out.println("** Decision Tress Evaluation with Datasets **");
		System.out.println(eval.toSummaryString());
		System.out.print(" the expression for the input data as per alogorithm is ");
		System.out.println(naiveBayes);
		System.out.println(eval.toMatrixString());
		 System.out.println(eval.toClassDetailsString());
	}
	

}