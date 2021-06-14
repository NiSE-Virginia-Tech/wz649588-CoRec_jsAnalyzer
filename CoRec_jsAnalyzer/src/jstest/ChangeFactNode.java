package jstest;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.tree.ITree;

import vt.edu.sql.SqliteManager;
import vt.edu.util.Pair;
import vt.edu.graph.ClientClass;
import vt.edu.graph.ClientExp;
import vt.edu.graph.ClientFunc;
import vt.edu.graph.ClientVar;

public class ChangeFactNode {
	File leftJs = null, rightJs = null;
	TraverseJsonTyped tJl;
	TraverseJsonTyped tJr;
	List<ClientVar> insertedVars = new ArrayList<ClientVar>();
	List<ClientVar> deletedVars = new ArrayList<ClientVar>();
	List<ClientFunc> insertedFuncs = new ArrayList<ClientFunc>();
	List<ClientFunc> deletedFuncs = new ArrayList<ClientFunc>();
	List<ClientClass> insertedClasses = new ArrayList<ClientClass>();
	List<ClientClass> deletedClasses = new ArrayList<ClientClass>();
	List<ClientExp> insertedExps = new ArrayList<ClientExp>();
	List<ClientExp> deletedExps = new ArrayList<ClientExp>();
	List<Pair<ClientFunc, ClientFunc>> changedFuncs = new ArrayList<Pair<ClientFunc, ClientFunc>>();
	List<Pair<ClientClass, ClientClass>> changedClasses = new ArrayList<Pair<ClientClass, ClientClass>>();
	List<Pair<ClientVar, ClientVar>> changedVars = new ArrayList<Pair<ClientVar, ClientVar>>();
	List<Pair<ClientExp, ClientExp>> changedExps = new ArrayList<Pair<ClientExp, ClientExp>>();
	HashSet<String> visitedLeftExp = new HashSet<String>();
	HashSet<String> visitedRightExp = new HashSet<String>();
	
	public int getNumOfChanged() {
		return insertedVars.size() + deletedVars.size() + changedVars.size()
				+ insertedFuncs.size() + deletedFuncs.size() + changedFuncs.size()
				+ insertedClasses.size() + deletedClasses.size() + changedClasses.size()
				+ insertedExps.size() + deletedExps.size() + changedExps.size();
	}
	
	public ChangeFactNode (File leftJs, File rightJs, TraverseJsonTyped tJl,TraverseJsonTyped tJr){
		this.leftJs = leftJs;
		this.rightJs = rightJs;
		
		this.tJl = tJl;
		this.tJr = tJr;
		if(rightJs == null) {
			buildDeletedFact();
		}
		else if(leftJs == null) {
			buildAddedFact();
		}
		
		else buildChangeFact();
	}
	
	public void buildDeletedFact() {
		for(String leftVar : tJl.varSet) {
			deletedVars.add(new ClientVar(leftVar, tJl.nameToITree.get(leftVar), null, tJl.jsFilePath, tJl));
		}
		for(String leftFunc : tJl.funcSet) {
			deletedFuncs.add(new ClientFunc(leftFunc, tJl.nameToITree.get(leftFunc), null, tJl.jsFilePath, tJl));
		}
		for(String leftClass : tJl.classSet) {
			deletedClasses.add(new ClientClass(leftClass, tJl.nameToITree.get(leftClass), null,  tJl.jsFilePath, tJl));
		}
		for(String leftExp : tJl.expSet) {
			deletedExps.add(new ClientExp(leftExp, tJl.nameToITree.get(leftExp), null, tJl.jsFilePath, tJl));
		}
	}
	
	public void buildAddedFact() {
		for(String rightVar : tJr.varSet) {
			insertedVars.add(new ClientVar(rightVar, tJr.nameToITree.get(rightVar), null, tJr.jsFilePath, tJr));
		}
		for(String rightFunc : tJr.funcSet) {
			insertedFuncs.add(new ClientFunc(rightFunc, tJr.nameToITree.get(rightFunc), null, tJr.jsFilePath, tJr));
		}
		for(String rightClass : tJr.classSet) {
			insertedClasses.add(new ClientClass(rightClass, tJr.nameToITree.get(rightClass), null, tJr.jsFilePath, tJr));
		}
		for(String rightExp : tJr.expSet) {
			insertedExps.add(new ClientExp(rightExp, tJr.nameToITree.get(rightExp), null, tJr.jsFilePath, tJr));
		}
	}
	
//	get all the changed lists(just for one js file change)
	public void buildChangeFact(){	
//		variables
		for(String leftVar : tJl.varSet){
			if(!tJr.varSet.contains(leftVar)) deletedVars.add(new ClientVar(leftVar, tJl.nameToITree.get(leftVar), null, tJl.jsFilePath, tJl));
			else{
//				System.out.println("Does it take long time?");
				List<String> lVarToken = tJl.expNameToToken.get(leftVar);
				List<String> rVarToken = tJr.expNameToToken.get(leftVar);
				ITree lVarTree = tJl.nameToITree.get(leftVar), rVarTree = tJr.nameToITree.get(leftVar);
				if(!lVarToken.equals(rVarToken)) {
					changedVars.add(new Pair(new ClientVar(leftVar, lVarTree, null, tJl.jsFilePath, tJl), new ClientVar
							(leftVar, rVarTree, null, tJr.jsFilePath, tJr)));
				}
//				System.out.println("Does it take long time?");
//				ITree lVarTree = tJl.nameToITree.get(leftVar), rVarTree = tJr.nameToITree.get(leftVar);
//				if(!actionTool.getActions(lVarTree, rVarTree).isEmpty()){
//					changedVars.add(new Pair(new ClientVar(leftVar, lVarTree, tJl.jsFilePath), new ClientVar
//							(leftVar, rVarTree, tJr.jsFilePath)));
//				}
			}
		}
		for(String rightVar : tJr.varSet){
			if(!tJl.varSet.contains(rightVar)) insertedVars.add(new ClientVar(rightVar, tJr.nameToITree.get(rightVar), null, tJr.jsFilePath, tJr));
		}
		
//		functions
		for(String leftFunc : tJl.funcSet){
			if(!tJr.funcSet.contains(leftFunc)) deletedFuncs.add(new ClientFunc(leftFunc, tJl.nameToITree.get(leftFunc), null, tJl.jsFilePath, tJl));
			else{
//				System.out.println("Does it take long time?");
				List<String> lFuncToken = tJl.expNameToToken.get(leftFunc);
				List<String> rFuncToken = tJr.expNameToToken.get(leftFunc);
				ITree lFuncTree = tJl.nameToITree.get(leftFunc), rFuncTree = tJr.nameToITree.get(leftFunc);
				if(!lFuncToken.equals(rFuncToken)) {
					changedFuncs.add(new Pair(new ClientFunc(leftFunc, lFuncTree, null, tJl.jsFilePath, tJl), new ClientFunc
							(leftFunc, rFuncTree, null, tJr.jsFilePath, tJr)));
				}
//				ITree lFuncTree = tJl.nameToITree.get(leftFunc), rFuncTree = tJr.nameToITree.get(leftFunc);
//				if(!actionTool.getActions(lFuncTree, rFuncTree).isEmpty()){
//					changedFuncs.add(new Pair(new ClientFunc(leftFunc, lFuncTree, tJl.jsFilePath), new ClientFunc
//							(leftFunc, rFuncTree, tJr.jsFilePath)));
//				}
			}
		}
		for(String rightFunc : tJr.funcSet){
			if(!tJl.funcSet.contains(rightFunc)) insertedFuncs.add(new ClientFunc(rightFunc, tJr.nameToITree.get(rightFunc), null, tJr.jsFilePath, tJr));
		}
		
//		classes
		for(String leftClass : tJl.classSet){
			if(!tJr.classSet.contains(leftClass)) deletedClasses.add(new ClientClass(leftClass, tJl.nameToITree.get(leftClass), null, tJl.jsFilePath, tJl));
			else{
//				System.out.println("Does it take long time?");
//				List<String> lClassToken = tJl.expNameToToken.get(leftClass);
//				List<String> rClassToken = tJr.expNameToToken.get(leftClass);
				ITree lClassTree = tJl.nameToITree.get(leftClass), rClassTree = tJr.nameToITree.get(leftClass);
				ITree typedLClassTree = tJl.typedNameToITree.get(leftClass), typedRClassTree = tJr.typedNameToITree.get(leftClass);
//				if(!lClassToken.equals(rClassToken)) {
				String leftSuper = tJl.superClassFinder.get(leftClass), rightSuper = tJr.superClassFinder.get(leftClass);
				if(leftSuper == null && rightSuper == null) continue;
				else if (leftSuper != null && rightSuper != null && leftSuper.equals(rightSuper)) continue;
				else {
					changedClasses.add(new Pair(new ClientClass(leftClass, lClassTree, typedLClassTree, tJl.jsFilePath, tJl), new ClientClass
							(leftClass, rClassTree, typedRClassTree, tJr.jsFilePath, tJr)));
				}
//				if(!actionTool.getActions(lClassTree, rClassTree).isEmpty()){
//					changedClasses.add(new Pair(new ClientClass(leftClass, lClassTree, tJl.jsFilePath), new ClientClass
//							(leftClass, rClassTree, tJr.jsFilePath)));
//				}
			}
		}
		for(String rightClass : tJr.classSet){
			if(!tJl.classSet.contains(rightClass)) insertedClasses.add(new ClientClass(rightClass, tJr.nameToITree.get(rightClass), null, tJr.jsFilePath, tJr));
		}
		
////		Same expressions
//		for(String leftExp : tJl.expSet){
//			ITree lExpTree = tJl.nameToITree.get(leftExp);
//			String expKind = leftExp.split(" ")[0];
//			for(String rightExp : tJr.expSet){
//				if(visitedRightExp.contains(rightExp)) continue;
//				ITree rExpTree = tJr.nameToITree.get(rightExp);
//				if(rightExp.startsWith(expKind) && actionTool.getActions(lExpTree, rExpTree).isEmpty()){
//					visitedLeftExp.add(leftExp);
//					visitedRightExp.add(rightExp);
//				}
//			}
//		}
		
		PriorityQueue<MatchTupleNode> expMatchings = new PriorityQueue<>();
			
		
//		Build the expmatching priorityqueue
		for(String leftExp : tJl.expSet){
			if(visitedLeftExp.contains(leftExp)) continue;
			ITree lExpTree = tJl.nameToITree.get(leftExp);
			List<String> lExpTokens = tJl.expNameToToken.get(leftExp);
			String expKind = leftExp.split(" ")[0];
			String tempChange = null;
			for(String rightExp : tJr.expSet){
				if(visitedRightExp.contains(rightExp)) continue;
				if(rightExp.startsWith(expKind)){
					ITree rExpTree = tJr.nameToITree.get(rightExp);
					List<String> rExpTokens = tJr.expNameToToken.get(rightExp);
					double match = matchExpTokens(lExpTokens, rExpTokens);
//					System.out.println(match);
					if(match == 1.0) {
						visitedLeftExp.add(leftExp);
						visitedRightExp.add(rightExp);
					}
					if(match >= 0.5){
						expMatchings.offer(new MatchTupleNode(match, leftExp, rightExp, lExpTree, rExpTree));
						tempChange = rightExp;
					}
				}
			}
		}
		
//		changed expressions
		while(!expMatchings.isEmpty()){
			MatchTupleNode expMatch = expMatchings.poll();
			if(visitedLeftExp.contains(expMatch.leftExp) || visitedRightExp.contains(expMatch.rightExp)) continue;
			changedExps.add(new Pair(new ClientExp(expMatch.leftExp, expMatch.lExpTree, null, tJl.jsFilePath, tJl), new ClientExp
					(expMatch.rightExp, expMatch.rExpTree, null, tJr.jsFilePath, tJr)));
			visitedLeftExp.add(expMatch.leftExp);
			visitedRightExp.add(expMatch.rightExp);
		}
		
//		deleted expressions
		for(String leftExp : tJl.expSet){
			if(!visitedLeftExp.contains(leftExp)){
				ITree lExpTree = tJl.nameToITree.get(leftExp);
				deletedExps.add(new ClientExp(leftExp, lExpTree, null, tJl.jsFilePath, tJl));
				visitedLeftExp.add(leftExp);
			}
		}
		
		
//		added expressions
		for(String rightExp : tJr.expSet){
			if(!visitedRightExp.contains(rightExp)){
				ITree rExpTree = tJr.nameToITree.get(rightExp);
				insertedExps.add(new ClientExp(rightExp, rExpTree, null, tJr.jsFilePath, tJr));
			}
		}
		
	}
	
	
//	get the effective nodes defined by having a parent called "STRING"
	public HashSet<String> getEffeNodes(ITree tree){
		HashSet<String> effeNodesSet = new HashSet<>();
		for(ITree child : tree.getTrees()){
			if(child.isLeaf() && child.getParent().toShortString().equals("STRING@@")){
				effeNodesSet.add(child.toShortString() + "in" + child.getDepth());
			}
		}
		return effeNodesSet;
	}
	
//	get the matching percentage
	public double matchExpTokens(List<String> lExpTokens, List<String> rExpTokens){
		int lsize = lExpTokens.size(), rsize = rExpTokens.size();
		int len = lcs(lExpTokens, rExpTokens, lsize, rsize);
//		System.out.println(len);
		return (double) len * 2 / (double) (lsize + rsize);
	}
	
//	longest common subsequence
//	public int lcs (List<String> X, List<String> Y, int m, int n){
//		int L[][] = new int[m + 1][n + 1];
//		for(int i = 0; i <= m; i++){
//			for(int j = 0; j <= n; j++){
//				if(i == 0 || j == 0) L[i][j] = 0;
//				else if(X.get(i - 1).equals(Y.get(j - 1))) L[i][j] = L[i - 1][j - 1] + 1;
//				else L[i][j] = Math.max(L[i - 1][j], L[i][j - 1]);
//			}
//		}
//		return L[m][n];
//	}
	
    public static int lcs(List<String> X, List<String> Y, int m, int n)
    {

        // allocate storage for one-dimensional arrays curr and prev
        int[] curr = new int[n + 1];
        int[] prev = new int[n + 1];
 
        // fill the lookup table in bottom-up manner
        for (int i = 0; i <= m; i++)
        {
            for (int j = 0; j <= n; j++)
            {
                if (i > 0 && j > 0)
                {
                    // if current character of X and Y matches
                    if (X.get(i - 1).equals(Y.get(j - 1))) {
                        curr[j] = prev[j - 1] + 1;
                    }
                    // else if current character of X and Y don't match
                    else {
                        curr[j] = Integer.max(prev[j], curr[j - 1]);
                    }
                }
            }
 
            // replace contents of previous array with current array
            System.arraycopy(curr, 0, prev, 0, n);
        }
 
        // LCS will be last entry in the lookup table
        return curr[n];
    }
    
    public static void extractChangesForRose(String commitName, List<ChangeFactNode> cfList) throws SQLException {
		for (ChangeFactNode cf: cfList) {
			File oFile = cf.leftJs;
			File nFile = cf.rightJs;
			
			if (oFile == null && nFile == null)
				continue;
			
			for (Pair<ClientFunc, ClientFunc> entity: cf.changedFuncs) {
//				SourceCodeChange sc = cf.entityChanges.get(entity);
				ClientFunc func = entity.fst;
				Connection conn = SqliteManager.getConnection();
				PreparedStatement ps = conn.prepareStatement("INSERT INTO "+ TestChange2.roseTable
						+ " (bug_name,name) VALUES (?,?)");
				ps.setString(1, commitName);
				ps.setString(2, func.sig);
				ps.executeUpdate();
				ps.close();
				conn.close();
			}
		}
	}
}

//	A class for building the expMatching PriorityQueue
class MatchTupleNode implements Comparable<MatchTupleNode>{
	double match;
	String leftExp, rightExp;
	ITree lExpTree, rExpTree;
	public MatchTupleNode(double match, String leftExp, String rightExp, ITree lExpTree, ITree rExpTree){
		this.match = match;
		this.leftExp = leftExp;
		this.rightExp = rightExp;
		this.lExpTree = lExpTree;
		this.rExpTree = rExpTree;
	}
	@Override
	public int compareTo(MatchTupleNode mT){
		if(this.match > mT.match) return -1;
		else if(this.match < mT.match) return 1;
		else return 0;
	}
}