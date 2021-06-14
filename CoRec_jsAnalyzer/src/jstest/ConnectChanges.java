package jstest;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.gumtreediff.tree.ITree;
import com.google.gson.Gson;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

import edu.uci.ics.jung.graph.DirectedSparseGraph;
import vt.edu.script.json.EditScriptJson;
import vt.edu.script.json.GraphDataJson;
import vt.edu.script.json.GraphDataWithNodesJson;
import vt.edu.sql.SqliteManager;
import vt.edu.util.Pair;
import vt.edu.extraction.PatternDotUtil;
import vt.edu.graph.ClientClass;
import vt.edu.graph.ClientExp;
import vt.edu.graph.ClientFunc;
import vt.edu.graph.ClientMember;
import vt.edu.graph.ClientVar;
import vt.edu.graph.ReferenceEdge;
import vt.edu.graph.ReferenceNode;
import wekaPre.RosePrediction;
import wekaPre.TARMAQPrediction;
import wekaPre.TransARPrediction;

public class ConnectChanges {
	List<ChangeFactNode> cfList;
	static String bugName;
	List<TraverseJsonTyped> oldTJ;
	Map<ClientFunc, TraverseJsonTyped> afToOldTJ = new HashMap<>();
	Map<ClientVar, TraverseJsonTyped> avToOldTJ = new HashMap<>();
	public void groupChanges(List<ChangeFactNode> cfList, String bugName, List<TraverseJsonTyped> oldTJ) {
		this.cfList = cfList;
		this.bugName = bugName;
		this.oldTJ = oldTJ;
		Set<ClientFunc> oldChangedFuncs = new HashSet<ClientFunc>();
		Set<ClientFunc> newChangedFuncs = new HashSet<ClientFunc>();
		Set<ClientFunc> deletedFuncs = new HashSet<ClientFunc>();
		Set<ClientFunc> insertedFuncs = new HashSet<ClientFunc>();

		Set<ClientVar> oldChangedVars = new HashSet<ClientVar>();
		Set<ClientVar> newChangedVars = new HashSet<ClientVar>();
		Set<ClientVar> insertedVars = new HashSet<ClientVar>();
		Set<ClientVar> deletedVars = new HashSet<ClientVar>();
		
		Set<ClientClass> oldChangedClasses = new HashSet<ClientClass>();
		Set<ClientClass> newChangedClasses = new HashSet<ClientClass>();
		Set<ClientClass> insertedClasses = new HashSet<ClientClass>();
		Set<ClientClass> deletedClasses = new HashSet<ClientClass>();
		
		Set<ClientExp> oldChangedExps = new HashSet<ClientExp>();
		Set<ClientExp> newChangedExps = new HashSet<ClientExp>();
		Set<ClientExp> insertedExps = new HashSet<ClientExp>();
		Set<ClientExp> deletedExps = new HashSet<ClientExp>();
		
		Map<ClientFunc, ClientFunc> newToOldFuncMap = new HashMap<>();
		Map<ClientFunc, ClientFunc> oldToNewFuncMap = new HashMap<>();
		
		Map<ClientClass, ClientClass> newToOldClassMap = new HashMap<>();
		Map<ClientClass, ClientClass> oldToNewClassMap = new HashMap<>();
		
		Map<ClientExp, ClientExp> newToOldExpMap = new HashMap<>();
		Map<ClientExp, ClientExp> oldToNewExpMap = new HashMap<>();
		Map<ClientVar, ClientVar> newToOldVarMap = new HashMap<>();
		Map<ClientVar, ClientVar> oldToNewVarMap = new HashMap<>();
		
		ClientFunc f1 = null, f2 = null;
		ClientExp e1 = null, e2 = null;
		ClientVar v1 = null, v2 = null;
		ClientClass c1 = null, c2 = null;
//		Map<ClientFunc, TraverseJsonTyped> afToOldTJ = new HashMap<>();
//		Map<ClientVar, TraverseJsonTyped> avToOldTJ = new HashMap<>();
		
		for (ChangeFactNode cf : cfList) {
			
			for (Pair<ClientFunc, ClientFunc> p : cf.changedFuncs) {
				f1 = p.fst;
				f2 = p.snd;
				oldChangedFuncs.add(f1);
				newChangedFuncs.add(f2);
				oldToNewFuncMap.put(f1, f2);
				newToOldFuncMap.put(f2, f1);
			}
			
			for (Pair<ClientVar, ClientVar> p : cf.changedVars) {
				v1 = p.fst;
				v2 = p.snd;
				oldChangedVars.add(v1);
				newChangedVars.add(v2);
				newToOldVarMap.put(v2, v1);
				oldToNewVarMap.put(v1, v2);
			}
			
			for (Pair<ClientExp, ClientExp> p : cf.changedExps) {
				e1 = p.fst;
				e2 = p.snd;
				oldChangedExps.add(e1);
				newChangedExps.add(e2);
				newToOldExpMap.put(e2, e1);
				oldToNewExpMap.put(e1, e2);
			}
			
			for (Pair<ClientClass, ClientClass> p : cf.changedClasses) {
				c1 = p.fst;
				c2 = p.snd;
				oldChangedClasses.add(c1);
				newChangedClasses.add(c2);
				newToOldClassMap.put(c2, c1);
				oldToNewClassMap.put(c1, c2);
			}
			
			for (ClientVar cv : cf.insertedVars) {
				insertedVars.add(cv);
				avToOldTJ.put(cv, cf.tJl);
			}
			
			for (ClientFunc cfunc : cf.insertedFuncs) {
				insertedFuncs.add(cfunc);
				afToOldTJ.put(cfunc, cf.tJl);
			}
			
			for (ClientClass cc : cf.insertedClasses) {
				insertedClasses.add(cc);
			}
			
			for (ClientExp ce : cf.insertedExps) {
				insertedExps.add(ce);
			}
			
			for (ClientVar cv : cf.deletedVars) {
				deletedVars.add(cv);
			}
			
			for (ClientFunc cfunc : cf.deletedFuncs) {
				deletedFuncs.add(cfunc);
			}
			
			for (ClientClass cc : cf.deletedClasses) {
				deletedClasses.add(cc);
			}
			
			for (ClientExp ce : cf.deletedExps) {
				deletedExps.add(ce);
			}	
		}
		
		List<DirectedSparseGraph<ReferenceNode, ReferenceEdge>> impactGraphs = new 
				ArrayList<DirectedSparseGraph<ReferenceNode, ReferenceEdge>>();
		
		analyzeVarAccessFunc(impactGraphs, oldChangedFuncs, ReferenceNode.CF,
				deletedVars, ReferenceNode.DV);
		analyzeVarAccessFunc(impactGraphs, oldChangedFuncs, ReferenceNode.CF,
				oldChangedVars, ReferenceNode.CV);
		analyzeVarAccessFunc(impactGraphs, deletedFuncs, ReferenceNode.DF,
				deletedVars, ReferenceNode.DV);
		analyzeVarAccessFunc(impactGraphs, deletedFuncs, ReferenceNode.DF,
				oldChangedVars, ReferenceNode.CV);	
		analyzeVarAccessFunc(impactGraphs, newChangedFuncs, ReferenceNode.CF,
				insertedVars, ReferenceNode.AV);
		analyzeVarAccessFunc(impactGraphs, newChangedFuncs, ReferenceNode.CF,
				newChangedVars, ReferenceNode.CV);
		analyzeVarAccessFunc(impactGraphs, insertedFuncs, ReferenceNode.AF,
				insertedVars, ReferenceNode.AV);
		analyzeVarAccessFunc(impactGraphs, insertedFuncs, ReferenceNode.AF,
				newChangedVars, ReferenceNode.CV);
		
		analyzeVarAccessVar(impactGraphs, oldChangedVars, ReferenceNode.CV,
				deletedVars, ReferenceNode.DV);
		analyzeVarAccessVar(impactGraphs, oldChangedVars, ReferenceNode.CV,
				oldChangedVars, ReferenceNode.CV);
		analyzeVarAccessVar(impactGraphs, deletedVars, ReferenceNode.DV,
				deletedVars, ReferenceNode.DV);
		analyzeVarAccessVar(impactGraphs, deletedVars, ReferenceNode.DV,
				oldChangedVars, ReferenceNode.CV);	
		analyzeVarAccessVar(impactGraphs, newChangedVars, ReferenceNode.CV,
				insertedVars, ReferenceNode.AV);
		analyzeVarAccessVar(impactGraphs, newChangedVars, ReferenceNode.CV,
				newChangedVars, ReferenceNode.CV);
		analyzeVarAccessVar(impactGraphs, insertedVars, ReferenceNode.AV,
				insertedVars, ReferenceNode.AV);
		analyzeVarAccessVar(impactGraphs, insertedVars, ReferenceNode.AV,
				newChangedVars, ReferenceNode.CV);
		
		analyzeVarAccessExp(impactGraphs, oldChangedExps, ReferenceNode.CE,
				deletedVars, ReferenceNode.DV);
		analyzeVarAccessExp(impactGraphs, oldChangedExps, ReferenceNode.CE,
				oldChangedVars, ReferenceNode.CV);
		analyzeVarAccessExp(impactGraphs, deletedExps, ReferenceNode.DE,
				deletedVars, ReferenceNode.DV);
		analyzeVarAccessExp(impactGraphs, deletedExps, ReferenceNode.DE,
				oldChangedVars, ReferenceNode.CV);	
		analyzeVarAccessExp(impactGraphs, newChangedExps, ReferenceNode.CE,
				insertedVars, ReferenceNode.AV);
		analyzeVarAccessExp(impactGraphs, newChangedExps, ReferenceNode.CE,
				newChangedVars, ReferenceNode.CV);
		analyzeVarAccessExp(impactGraphs, insertedExps, ReferenceNode.AE,
				insertedVars, ReferenceNode.AV);
		analyzeVarAccessExp(impactGraphs, insertedExps, ReferenceNode.AE,
				newChangedVars, ReferenceNode.CV);
//		
//		analyzeVarContainClass(impactGraphs, oldChangedClasses, ReferenceNode.CC,
//				deletedVars, ReferenceNode.DV);
//		analyzeVarContainClass(impactGraphs, oldChangedClasses, ReferenceNode.CC,
//				oldChangedVars, ReferenceNode.CV);
//		analyzeVarContainClass(impactGraphs, deletedClasses, ReferenceNode.DC,
//				deletedVars, ReferenceNode.DV);
//		analyzeVarContainClass(impactGraphs, deletedClasses, ReferenceNode.DC,
//				oldChangedVars, ReferenceNode.CV);	
//		analyzeVarContainClass(impactGraphs, newChangedClasses, ReferenceNode.CC,
//				insertedVars, ReferenceNode.AV);
//		analyzeVarContainClass(impactGraphs, newChangedClasses, ReferenceNode.CC,
//				newChangedVars, ReferenceNode.CV);
//		analyzeVarContainClass(impactGraphs, insertedClasses, ReferenceNode.AC,
//				insertedVars, ReferenceNode.AV);
//		analyzeVarContainClass(impactGraphs, insertedClasses, ReferenceNode.AC,
//				newChangedVars, ReferenceNode.CV);
		
		
		analyzeFuncAccessFunc(impactGraphs, oldChangedFuncs, ReferenceNode.CF,
				deletedFuncs, ReferenceNode.DF);
		analyzeFuncAccessFunc(impactGraphs, oldChangedFuncs, ReferenceNode.CF,
				oldChangedFuncs, ReferenceNode.CF);
		analyzeFuncAccessFunc(impactGraphs, deletedFuncs, ReferenceNode.DF,
				deletedFuncs, ReferenceNode.DF);
		analyzeFuncAccessFunc(impactGraphs, deletedFuncs, ReferenceNode.DF,
				oldChangedFuncs, ReferenceNode.CF);	
		analyzeFuncAccessFunc(impactGraphs, newChangedFuncs, ReferenceNode.CF,
				insertedFuncs, ReferenceNode.AF);
		analyzeFuncAccessFunc(impactGraphs, newChangedFuncs, ReferenceNode.CF,
				newChangedFuncs, ReferenceNode.CF);
		analyzeFuncAccessFunc(impactGraphs, insertedFuncs, ReferenceNode.AF,
				insertedFuncs, ReferenceNode.AF);
		analyzeFuncAccessFunc(impactGraphs, insertedFuncs, ReferenceNode.AF,
				newChangedFuncs, ReferenceNode.CF);
		
		analyzeFuncAccessVar(impactGraphs, oldChangedVars, ReferenceNode.CV,
				deletedFuncs, ReferenceNode.DF);
		analyzeFuncAccessVar(impactGraphs, oldChangedVars, ReferenceNode.CV,
				oldChangedFuncs, ReferenceNode.CF);
		analyzeFuncAccessVar(impactGraphs, deletedVars, ReferenceNode.DV,
				deletedFuncs, ReferenceNode.DF);
		analyzeFuncAccessVar(impactGraphs, deletedVars, ReferenceNode.DV,
				oldChangedFuncs, ReferenceNode.CF);	
		analyzeFuncAccessVar(impactGraphs, newChangedVars, ReferenceNode.CV,
				insertedFuncs, ReferenceNode.AF);
		analyzeFuncAccessVar(impactGraphs, newChangedVars, ReferenceNode.CV,
				newChangedFuncs, ReferenceNode.CF);
		analyzeFuncAccessVar(impactGraphs, insertedVars, ReferenceNode.AV,
				insertedFuncs, ReferenceNode.AF);
		analyzeFuncAccessVar(impactGraphs, insertedVars, ReferenceNode.AV,
				newChangedFuncs, ReferenceNode.CF);
		
		analyzeFuncAccessExp(impactGraphs, oldChangedExps, ReferenceNode.CE,
				deletedFuncs, ReferenceNode.DF);
		analyzeFuncAccessExp(impactGraphs, oldChangedExps, ReferenceNode.CE,
				oldChangedFuncs, ReferenceNode.CF);
		analyzeFuncAccessExp(impactGraphs, deletedExps, ReferenceNode.DE,
				deletedFuncs, ReferenceNode.DF);
		analyzeFuncAccessExp(impactGraphs, deletedExps, ReferenceNode.DE,
				oldChangedFuncs, ReferenceNode.CF);	
		analyzeFuncAccessExp(impactGraphs, newChangedExps, ReferenceNode.CE,
				insertedFuncs, ReferenceNode.AF);
		analyzeFuncAccessExp(impactGraphs, newChangedExps, ReferenceNode.CE,
				newChangedFuncs, ReferenceNode.CF);
		analyzeFuncAccessExp(impactGraphs, insertedExps, ReferenceNode.AE,
				insertedFuncs, ReferenceNode.AF);
		analyzeFuncAccessExp(impactGraphs, insertedExps, ReferenceNode.AE,
				newChangedFuncs, ReferenceNode.CF);
		
		
// analyzeClassContain
		analyzeFuncAccessClass(impactGraphs, insertedClasses, ReferenceNode.AC, insertedFuncs, ReferenceNode.AF);
		analyzeVarAccessClass(impactGraphs, insertedClasses, ReferenceNode.AC, insertedVars, ReferenceNode.AV);
		analyzeFuncAccessClass(impactGraphs, deletedClasses, ReferenceNode.DC, deletedFuncs, ReferenceNode.DF);
		analyzeVarAccessClass(impactGraphs, deletedClasses, ReferenceNode.DC, deletedVars, ReferenceNode.DV);
		

		analyzeClassAccessClass(impactGraphs, oldChangedClasses, ReferenceNode.CC,
				deletedClasses, ReferenceNode.DC);
		analyzeClassAccessClass(impactGraphs, oldChangedClasses, ReferenceNode.CC,
				oldChangedClasses, ReferenceNode.CC);
		analyzeClassAccessClass(impactGraphs, deletedClasses, ReferenceNode.DC,
				deletedClasses, ReferenceNode.DC);
		analyzeClassAccessClass(impactGraphs, deletedClasses, ReferenceNode.DC,
				oldChangedClasses, ReferenceNode.CC);	
		analyzeClassAccessClass(impactGraphs, newChangedClasses, ReferenceNode.CC,
				insertedClasses, ReferenceNode.AC);
		analyzeClassAccessClass(impactGraphs, newChangedClasses, ReferenceNode.CC,
				newChangedClasses, ReferenceNode.CC);
		analyzeClassAccessClass(impactGraphs, insertedClasses, ReferenceNode.AC,
				insertedClasses, ReferenceNode.AC);
		analyzeClassAccessClass(impactGraphs, insertedClasses, ReferenceNode.AC,
				newChangedClasses, ReferenceNode.CC);
		
		impactGraphs = convertNewVersionToOldVersion(impactGraphs, newToOldFuncMap);
		impactGraphs = convertNewVersionToOldVersion(impactGraphs, newToOldVarMap);
		impactGraphs = convertNewVersionToOldVersion(impactGraphs, newToOldClassMap);
		impactGraphs = convertNewVersionToOldVersion(impactGraphs, newToOldExpMap);

		
//		
//		
		addIsolatedNodes(impactGraphs, insertedVars, ReferenceNode.AV);
		addIsolatedNodes(impactGraphs, deletedVars, ReferenceNode.DV);
		addIsolatedNodes(impactGraphs, oldChangedVars, ReferenceNode.CV);
		addIsolatedNodes(impactGraphs, insertedFuncs, ReferenceNode.AF);
		addIsolatedNodes(impactGraphs, deletedFuncs, ReferenceNode.DF);
		addIsolatedNodes(impactGraphs, oldChangedFuncs, ReferenceNode.CF);
		addIsolatedNodes(impactGraphs, insertedClasses, ReferenceNode.AC);
		addIsolatedNodes(impactGraphs, deletedClasses, ReferenceNode.DC);
		addIsolatedNodes(impactGraphs, oldChangedClasses, ReferenceNode.CC);
		addIsolatedNodes(impactGraphs, insertedExps, ReferenceNode.AE);
		addIsolatedNodes(impactGraphs, deletedExps, ReferenceNode.DE);
		addIsolatedNodes(impactGraphs, oldChangedExps, ReferenceNode.CE);
		
		
		
		impactGraphs = mergeChanges(impactGraphs);
		
		
//		extract data for random forest(features + true or false, need all the functions)

		
		
		List<String> graphDataValues = new ArrayList<>();
		
		Gson gson = new Gson();
		/*
		 * Rose prediction 
		 */
		for (int i = 0; i < impactGraphs.size(); i++) {
			DirectedSparseGraph<ReferenceNode, ReferenceEdge> jung = impactGraphs.get(i);
			Graph<ReferenceNode, ReferenceEdge> jgrapht = convertJungToJGraphT(jung);
//			for (ReferenceNode node : jgrapht.vertexSet()) {
//				if (node.type != ReferenceNode.AV)
//					continue;
//				ClientVar af = (ClientVar) node.ref;
//				Set<ClientFunc> cfs = buildAvDataset(node, jgrapht);
//				if (cfs == null) continue;
//				Set<ClientFunc> candidateSet = new HashSet<>();
//				
//				TraverseJsonTyped afTJ = af.tJroot;
//				TraverseJsonTyped afOldTJ = afToOldTJ.get(af);
//				
//				if (afTJ.isExportEntity.contains(af.name)) {
//					if (oldTJ != null) {
//						for (TraverseJsonTyped tJ : oldTJ) {
//							Map<String, ITree> nameToITree = tJ.nameToITree;
//							Map<String, ITree> typedNameToITree = tJ.typedNameToITree;
//							for (String func : tJ.funcSet) {
//								ClientFunc candidate = new ClientFunc(func, nameToITree.get(func), typedNameToITree.get(func), tJ.jsFilePath, tJ);
//								candidateSet.add(candidate);
//							}
//						}
//					}
//				}
//				else if (afTJ.isPureExportEntity.contains(af.name)) {
//					if (oldTJ != null) {
//						for (TraverseJsonTyped tJ : oldTJ) {
//							if (tJ.jsFilePath.equals(afTJ.jsFilePath)) continue;
//							Map<String, ITree> nameToITree = tJ.nameToITree;
//							Map<String, ITree> typedNameToITree = tJ.typedNameToITree;
//							for (String func : tJ.funcSet) {
//								ClientFunc candidate = new ClientFunc(func, nameToITree.get(func), typedNameToITree.get(func), tJ.jsFilePath, tJ);
//								candidateSet.add(candidate);
//							}
//						}
//					}
//				}
////				if it's af or av then afOldTJ(change 5 places)
//				else {
//					if (afOldTJ != null){
//					//if (afTJ != null) {
//						Map<String, ITree> nameToITree = afTJ.nameToITree;
//						Map<String, ITree> typedNameToITree = afTJ.typedNameToITree;
//						for (String func : afTJ.funcSet) {
//							ClientFunc candidate = new ClientFunc(func, nameToITree.get(func), typedNameToITree.get(func), afTJ.jsFilePath, afTJ);
//							candidateSet.add(candidate);
//						}
//					}
//				}
//				boolean useRose = false;
//				if (cfs.size() >= 2) {
//					for (ClientFunc usedFunc : cfs) {
//						List<String> evidenceMethods = new ArrayList<>();
//						evidenceMethods.add(usedFunc.sig);
//						List<String> roseResult = TransARPrediction.execute(evidenceMethods, TestChange2.roseTable, bugName);
//						Set<String> truePositives = new HashSet<>();
//						Set<String> falsePositives = new HashSet<>();
//						Set<String> falseNegatives = new HashSet<>();
//						Map<String, Set<String>> tpVars = new HashMap<>();
//						Map<String, Set<String>> fpVars = new HashMap<>();
//						Map<String, Set<String>> fnVars = new HashMap<>();
//						Set<String> realOtherCfs = new HashSet<>();
//						Set<String> cfsSigs = new HashSet<>();
//						for (ClientFunc cf : cfs) cfsSigs.add(cf.sig);
// 						for (String predicted : roseResult) {
//							if (!cfsSigs.contains(predicted)) realOtherCfs.add(predicted);
//						}
// 						for (ClientFunc mf : cfs) {
// 							if (mf.equals(usedFunc)) continue;
// 							if (roseResult.contains(mf.sig)) {
// 								truePositives.add(mf.sig);
// 							}
// 							else falseNegatives.add(mf.sig);
// 						}
// 						int recall = 100 * truePositives.size() / (cfs.size() - 1);
// 						int precision = -1;
// 						if (!roseResult.isEmpty()) {
// 							precision = 100 * truePositives.size() / roseResult.size();
// 						}
// 						Gson gsonHere = new Gson();
// 						Connection conn = SqliteManager.getConnection();
// 						try {
// 							PreparedStatement ps = conn.prepareStatement("INSERT INTO " + TestChange2.afPredictTable
// 									+ " (bug_name,af_sig,used_cf,real_other_cf,predicted_cf,precision,recall, ground_truth_size, predicted_size, true_positive_size)"
// 			//						+ "access_precision,access_detail, access_fields)"
// 									+ " VALUES (?,?,?,?,?,?,?,?,?,?)");
// 							ps.setString(1, bugName);
// 							ps.setString(2, af.sig);
// 							ps.setString(3, usedFunc.sig);
//// 							Set<String> realOtherCms = new HashSet<>(cmSigSet);
//// 							realOtherCms.remove(usedCmRef.getSignature());
// 							ps.setString(4, gsonHere.toJson(realOtherCfs));
// 							ps.setString(5, gsonHere.toJson(roseResult));
// 							if (precision == -1) {
// 								ps.setNull(6, java.sql.Types.INTEGER);
// 								ps.setNull(7, java.sql.Types.INTEGER);
// 							}
// 							else {
// 								ps.setInt(6, precision);
// 								ps.setInt(7, recall);
// 							}
// 							ps.setInt(8, cfs.size() - 1);
// 							ps.setInt(9, roseResult.size());
// 							ps.setInt(10, truePositives.size());
// 							ps.executeUpdate();
// 							ps.close();
// 							conn.close();
// 						} catch (SQLException e) {
// 							e.printStackTrace();
// 						}
//					}
//				}
//				
//				
//			}
		/*
		 * Rose prediction end here
		 */

			/*
			 * Important for AF_CF data extracting
			 */
//			/* Important for ML data extracting
//			
//			for (ReferenceNode node: jgrapht.vertexSet()) {
//				if (node.type != ReferenceNode.AF)
//					continue;
//				Set<ClientFunc> cfs = buildAfDataset(node, jgrapht);
//				if (cfs == null)
//					continue;
//				TestChange2.afSet.add(TestChange2.thisVersion + "++" + bugName);
//				ClientFunc af = (ClientFunc) node.ref;
//				String afSig = af.getSignature();
////				AFPredictionDataset.put(afSig, cfs);
//				for (ClientFunc cf1 : cfs) {
//					for (ClientFunc cf2 : cfs) {
//						if (cf1.equals(cf2)) continue;
//						if (af.tJroot.isNodeJs) writeDataAFCFNode(af, cf1, cf2, true);
//						else writeDataAFCF(af, cf1, cf2, true);
//					}
//					TraverseJsonTyped afTJ = af.tJroot;
//					TraverseJsonTyped afOldTJ = afToOldTJ.get(af);
//					
//					if (afTJ.isExportEntity.contains(af.name)) {
//						if (oldTJ != null) {
//							for (TraverseJsonTyped tJ : oldTJ) {
//								Map<String, ITree> nameToITree = tJ.nameToITree;
//								Map<String, ITree> typedNameToITree = tJ.typedNameToITree;
//								for (String func : tJ.funcSet) {
//									ClientFunc candidate = new ClientFunc(func, nameToITree.get(func), typedNameToITree.get(func), tJ.jsFilePath, tJ);
//									if (!cfs.contains(candidate)) {
//										if (!af.tJroot.isNodeJs) writeDataAFCF(af, cf1, candidate, false);
//										else writeDataAFCFNode(af, cf1, candidate, false);
//									}
//								}
//							}
//						}
//						
//					}
//					else if (afTJ.isPureExportEntity.contains(af.name)) {
//						if (oldTJ != null) {
//							for (TraverseJsonTyped tJ : oldTJ) {
//								if (tJ.jsFilePath.equals(afTJ.jsFilePath)) continue;
//								Map<String, ITree> nameToITree = tJ.nameToITree;
//								Map<String, ITree> typedNameToITree = tJ.typedNameToITree;
//								for (String func : tJ.funcSet) {
//									ClientFunc candidate = new ClientFunc(func, nameToITree.get(func), typedNameToITree.get(func), tJ.jsFilePath, tJ);
//									if (!cfs.contains(candidate)) {
//										if (!af.tJroot.isNodeJs) writeDataAFCF(af, cf1, candidate, false);
//										else writeDataAFCFNode(af, cf1, candidate, false);
//									}
//								}
//							}
//						}
//					}
//					else {
//						if (afOldTJ != null){
//							Map<String, ITree> nameToITree = afOldTJ.nameToITree;
//							Map<String, ITree> typedNameToITree = afOldTJ.typedNameToITree;
//							for (String func : afOldTJ.funcSet) {
//								ClientFunc candidate = new ClientFunc(func, nameToITree.get(func), typedNameToITree.get(func), afOldTJ.jsFilePath, afOldTJ);
//								if (!cfs.contains(candidate)) {
//									if (!af.tJroot.isNodeJs) writeDataAFCF(af, cf1, candidate, false);
//									else writeDataAFCFNode(af, cf1, candidate, false);
//								}
//							}
//						}
//					}
//				}	
//			}
//			
////			*/
////			cf_cf data extracting
//			
//			
//			for (ReferenceNode node: jgrapht.vertexSet()) {
//				if (node.type != ReferenceNode.CF)
//					continue;
//				Set<ClientFunc> cfs = buildAfDataset(node, jgrapht);
//				if (cfs == null)
//					continue;
//				List<ClientFunc> cfsList = new ArrayList<ClientFunc>(cfs);
//				
//				TestChange2.cfSet.add(TestChange2.thisVersion + "++" + bugName);
//				ClientFunc cf = (ClientFunc) node.ref;
//				String cfSig = cf.getSignature();
////				AFPredictionDataset.put(afSig, cfs);
////				for (int j = 0; j < cfsList.size(); j++){
////					ClientFunc cf1 = cfsList.get(j);
////					for (int k = j + 1; k < cfsList.size(); k++) {
////						ClientFunc  cf2 = cfsList.get(k);
////						if (!cf.tJroot.isNodeJs) writeDataCFCF(cf, cf1, cf2, true);
////						else writeDataCFCFNode(cf, cf1, cf2, true);
////					}
//				
//				for (ClientFunc cf1 : cfs) {
//					for (ClientFunc cf2 : cfs) {
//						if (cf1.equals(cf2)) continue;
//						if (!cf.tJroot.isNodeJs) writeDataCFCF(cf, cf1, cf2, true);
//						else writeDataCFCFNode(cf, cf1, cf2, true);
//					}
//					TraverseJsonTyped cfTJ = cf.tJroot;
//					
//					if (cfTJ.isExportEntity.contains(cf.name)) {
//						if (oldTJ != null) {
//							for (TraverseJsonTyped tJ : oldTJ) {
//								Map<String, ITree> nameToITree = tJ.nameToITree;
//								Map<String, ITree> typedNameToITree = tJ.typedNameToITree;
//								for (String func : tJ.funcSet) {
//									ClientFunc candidate = new ClientFunc(func, nameToITree.get(func), typedNameToITree.get(func), tJ.jsFilePath, tJ);
//									if (!cfs.contains(candidate)) {
//										if (!cf.tJroot.isNodeJs) writeDataCFCF(cf, cf1, candidate, false);
//										else writeDataCFCFNode(cf, cf1, candidate, false);
//									}
//								}
//							}
//						}
//						
//					}
//					else if (cfTJ.isPureExportEntity.contains(cf.name)) {
//						if (oldTJ != null) {
//							for (TraverseJsonTyped tJ : oldTJ) {
//								if (tJ.jsFilePath.equals(cfTJ.jsFilePath)) continue;
//								Map<String, ITree> nameToITree = tJ.nameToITree;
//								Map<String, ITree> typedNameToITree = tJ.typedNameToITree;
//								for (String func : tJ.funcSet) {
//									ClientFunc candidate = new ClientFunc(func, nameToITree.get(func), typedNameToITree.get(func), tJ.jsFilePath, tJ);
//									if (!cfs.contains(candidate)) {
//										if (!cf.tJroot.isNodeJs) writeDataCFCF(cf, cf1, candidate, false);
//										else writeDataCFCFNode(cf, cf1, candidate, false);
//									}
//								}
//							}
//						}
//					}
//					else {
//						if (cfTJ != null){
//							Map<String, ITree> nameToITree = cfTJ.nameToITree;
//							Map<String, ITree> typedNameToITree = cfTJ.typedNameToITree;
//							for (String func : cfTJ.funcSet) {
//								ClientFunc candidate = new ClientFunc(func, nameToITree.get(func), typedNameToITree.get(func), cfTJ.jsFilePath, cfTJ);
//								if (!cfs.contains(candidate)) {
//									if (!cf.tJroot.isNodeJs) writeDataCFCF(cf, cf1, candidate, false);
//									else writeDataCFCFNode(cf, cf1, candidate, false);
//								}
//							}
//						}
//					}
//				}	
//			}
////			*/
//			
//			/*
//			 * Important avcf
//			 */
////			/*
//			
//			for (ReferenceNode node: jgrapht.vertexSet()) {
//				if (node.type != ReferenceNode.AV)
//					continue;
//				Set<ClientFunc> cfs = buildAvDataset(node, jgrapht);
//				if (cfs == null)
//					continue;
//				TestChange2.avSet.add(TestChange2.thisVersion + "++" + bugName);
//				ClientVar av = (ClientVar) node.ref;
//				String avSig = av.getSignature();
////				AFPredictionDataset.put(afSig, cfs);
//				for (ClientFunc cf1 : cfs) {
//					for (ClientFunc cf2 : cfs) {
//						if (cf1.equals(cf2)) continue;
//						if (!av.tJroot.isNodeJs) writeDataAVCF(av, cf1, cf2, true);
//						else writeDataAVCFNode(av, cf1, cf2, true);
//					}
//					TraverseJsonTyped avTJ = av.tJroot;
//					TraverseJsonTyped avOldTJ = avToOldTJ.get(av);
//					
//					if (avTJ.isExportEntity.contains(av.name)) {
//						if (oldTJ != null) {
//							for (TraverseJsonTyped tJ : oldTJ) {
//								Map<String, ITree> nameToITree = tJ.nameToITree;
//								Map<String, ITree> typedNameToITree = tJ.typedNameToITree;
//								for (String func : tJ.funcSet) {
//									ClientFunc candidate = new ClientFunc(func, nameToITree.get(func), typedNameToITree.get(func), tJ.jsFilePath, tJ);
//									if (!cfs.contains(candidate)) {
//										if (!av.tJroot.isNodeJs) writeDataAVCF(av, cf1, candidate, false);
//										else writeDataAVCFNode(av, cf1, candidate, false);
//									}
//								}
//							}
//						}
//						
//					}
//					else if (avTJ.isPureExportEntity.contains(av.name)) {
//						if (oldTJ != null) {
//							for (TraverseJsonTyped tJ : oldTJ) {
//								if (tJ.jsFilePath.equals(avTJ.jsFilePath)) continue;
//								Map<String, ITree> nameToITree = tJ.nameToITree;
//								Map<String, ITree> typedNameToITree = tJ.typedNameToITree;
//								for (String func : tJ.funcSet) {
//									ClientFunc candidate = new ClientFunc(func, nameToITree.get(func), typedNameToITree.get(func), tJ.jsFilePath, tJ);
//									if (!cfs.contains(candidate)) {
//										if (!av.tJroot.isNodeJs) writeDataAVCF(av, cf1, candidate, false);
//										else writeDataAVCFNode(av, cf1, candidate, false);
//									}
//								}
//							}
//						}
//					}
//					else {
//						if (avOldTJ != null){
//							Map<String, ITree> nameToITree = avOldTJ.nameToITree;
//							Map<String, ITree> typedNameToITree = avOldTJ.typedNameToITree;
//							for (String func : avOldTJ.funcSet) {
//								ClientFunc candidate = new ClientFunc(func, nameToITree.get(func), typedNameToITree.get(func), avOldTJ.jsFilePath, avOldTJ);
//								if (!cfs.contains(candidate)) {
//									if (!av.tJroot.isNodeJs) writeDataAVCF(av, cf1, candidate, false);
//									else writeDataAVCFNode(av, cf1, candidate, false);
//								}
//							}
//						}
//					}
//				}	
//			}
//			*/
//			
			/*
			 * avcf ends here, the following are important to extract the data values
			 */
			
			
//			
			boolean storeNodes = true;
			if (storeNodes) {
				GraphDataWithNodesJson graphJson = new GraphDataWithNodesJson();
				for (ReferenceNode node: jgrapht.vertexSet()) {
					graphJson.addNode(node);
				}
				for (ReferenceEdge edge: jgrapht.edgeSet()) {
					graphJson.addEdge(edge);
				}
				graphDataValues.add(gson.toJson(graphJson));
			} else {
				GraphDataJson graphDataJson = new GraphDataJson();
				for (ReferenceEdge edge: jgrapht.edgeSet()) {
					graphDataJson.addEdge(edge);
				}
				graphDataValues.add(gson.toJson(graphDataJson));
			}
			
		}
		
		for(int i = 0; i < graphDataValues.size(); i++) {
			System.out.println(graphDataValues.get(i));
		}
		
		if (!impactGraphs.isEmpty()) {
			StringBuilder editScriptSqlBuilder = new StringBuilder();
			String resultTable = TestChange2.editScriptTable;
			editScriptSqlBuilder.append("INSERT INTO " + resultTable + " (bug_name, graph_num, graph_data) VALUES ");
			for (int sqlValueNum = 0; sqlValueNum < impactGraphs.size() - 1; sqlValueNum++) {
				editScriptSqlBuilder.append("(\"");
				editScriptSqlBuilder.append(bugName);
				editScriptSqlBuilder.append("\",");
				editScriptSqlBuilder.append(sqlValueNum);
				editScriptSqlBuilder.append(",?),");
			}
			editScriptSqlBuilder.append("(\"");
			editScriptSqlBuilder.append(bugName);
			editScriptSqlBuilder.append("\",");
			editScriptSqlBuilder.append(impactGraphs.size() - 1);
			editScriptSqlBuilder.append(",?)");
			
			Connection connection = SqliteManager.getConnection();
			try {
				java.sql.PreparedStatement stmt = connection.prepareStatement(editScriptSqlBuilder.toString());
				for (int i = 0; i < impactGraphs.size(); i++) {
					System.out.println(impactGraphs.size() + " vs " + i);
					stmt.setString(i + 1, graphDataValues.get(i));
				}
				stmt.executeUpdate();
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}

		}	
	}
	
	
	public void writeDataCFCF(ClientFunc cf, ClientFunc cf1, ClientFunc cf2, boolean changed) {
//		CFCFCommon common = new CFCFCommon(afToOldTJ.get(af));
		CFCFCommon common = new CFCFCommon(cf.tJroot);
//		System.out.println(af.sig + " " + afToOldTJ.get(af));
//		feature1
		int commonFuncInvoke = common.commonFuncInvo(cf1, cf2, cf);
//		feature2
		int commonFieldAccess = common.commonVarAccess(cf1, cf2, cf);
//		feature3
		System.out.println("af is " + cf.sig);
		boolean sameReturnType = common.sameReturnType(cf1, cf2);
//		feature4
		int commonParameters = common.commonParameters(cf1, cf2);
		String afReturnType = common.getReturnType(cf);
		AllTypeGetter aTG2 = new AllTypeGetter(cf2.typedNode);
		Set<String> typeInsideCF2 = aTG2.getTypeSet();
//		feature5
		boolean hasAFReturnType = true;
		if (afReturnType != null && !afReturnType.equals("void") && !afReturnType.equals("any") &&
				!typeInsideCF2.contains(afReturnType)) {
			hasAFReturnType = false;
		}
		int containParameters = 0;
		List<String> typeInsideCF2List = aTG2.getTypeList();
		List<String[]> afPara = common.getParameters(cf);
//		feature6
		containParameters = common.containHowManyParameters(typeInsideCF2List, afPara);
//		feature7
		boolean sameType = common.sameType(cf1, cf2);
//		feature 8
		int similarity = common.tokenSimilarity(cf1, cf2);
//		feature9
		int similarState = common.similarStatementV2(cf1, cf2);
//		feature10
		int historyRecords = RosePrediction.historyRecords(cf1.sig, cf2.sig, TestChange2.roseTable, bugName);
		Connection conn = SqliteManager.getConnection();
		try {
			String tableName = TestChange2.cfcfTable;
			System.out.println("Yes! We can insert some data!");
			PreparedStatement ps = conn.prepareStatement("INSERT INTO " + tableName + " (cfSig,cf1Sig,candiSig,changed,f1,f2,f3,f4,f5,f6,f7,f8,f9,f10) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			ps.setString(1, bugName + " " + cf.sig);
			ps.setString(2, cf1.sig);
			ps.setString(3, cf2.sig);
			ps.setString(4, changed + "");
			ps.setInt(5, commonFuncInvoke);
			ps.setInt(6, commonFieldAccess);
			ps.setString(7, sameReturnType + "");
			ps.setInt(8, commonParameters);
			ps.setString(9, hasAFReturnType + "");
			ps.setInt(10, containParameters);
			ps.setString(11, sameType + "");
			ps.setInt(12, similarity);
			ps.setInt(13, similarState);
			ps.setInt(14, historyRecords);
			ps.executeUpdate();
			ps.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	
	public void writeDataAFCF(ClientFunc af, ClientFunc cf1, ClientFunc cf2, boolean changed) {
		CFCFCommon common = new CFCFCommon(afToOldTJ.get(af));
//		CFCFCommon common = new CFCFCommon(af.tJroot);
//		System.out.println(af.sig + " " + afToOldTJ.get(af));
//		feature1
		int commonFuncInvoke = common.commonFuncInvo(cf1, cf2, af);
//		feature2
		int commonFieldAccess = common.commonVarAccess(cf1, cf2, af);
//		feature3
		System.out.println("af is " + af.sig);
		boolean sameReturnType = common.sameReturnType(cf1, cf2);
//		feature4
		int commonParameters = common.commonParameters(cf1, cf2);
		String afReturnType = common.getReturnType(af);
		AllTypeGetter aTG2 = new AllTypeGetter(cf2.typedNode);
		Set<String> typeInsideCF2 = aTG2.getTypeSet();
//		feature5
		boolean hasAFReturnType = true;
		if (afReturnType != null && !afReturnType.equals("void") && !afReturnType.equals("any") &&
				!typeInsideCF2.contains(afReturnType)) {
			hasAFReturnType = false;
		}
		int containParameters = 0;
		List<String> typeInsideCF2List = aTG2.getTypeList();
		List<String[]> afPara = common.getParameters(af);
//		feature6
		containParameters = common.containHowManyParameters(typeInsideCF2List, afPara);
//		feature7
		boolean sameType = common.sameType(cf1, cf2);
//		feature 8
		int similarity = common.tokenSimilarity(cf1, cf2);
//		feature9
		int similarState = common.similarStatementV2(cf1, cf2);
//		feature10
		int historyRecords = RosePrediction.historyRecords(cf1.sig, cf2.sig, TestChange2.roseTable, bugName);
		Connection conn = SqliteManager.getConnection();
		try {
			String tableName = TestChange2.afcfTable;
			System.out.println("Yes! We can insert some data!");
			PreparedStatement ps = conn.prepareStatement("INSERT INTO " + tableName + " (afSig,cf1Sig,candiSig,changed,f1,f2,f3,f4,f5,f6,f7,f8,f9,f10) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			ps.setString(1, bugName + " " + af.sig);
			ps.setString(2, cf1.sig);
			ps.setString(3, cf2.sig);
			ps.setString(4, changed + "");
			ps.setInt(5, commonFuncInvoke);
			ps.setInt(6, commonFieldAccess);
			ps.setString(7, sameReturnType + "");
			ps.setInt(8, commonParameters);
			ps.setString(9, hasAFReturnType + "");
			ps.setInt(10, containParameters);
			ps.setString(11, sameType + "");
			ps.setInt(12, similarity);
			ps.setInt(13, similarState);
			ps.setInt(14, historyRecords);
			ps.executeUpdate();
			ps.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public void writeDataAFCFNode(ClientFunc af, ClientFunc cf1, ClientFunc cf2, boolean changed) {
		CFCFCommon common = new CFCFCommon(afToOldTJ.get(af));
//		CFCFCommon common = new CFCFCommon(af.tJroot);
//		System.out.println(af.sig + " " + afToOldTJ.get(af));
//		feature1
		int commonFuncInvoke = common.commonFuncInvo(cf1, cf2, af);
//		feature2
		int commonFieldAccess = common.commonVarAccess(cf1, cf2, af);
//		feature3
		boolean sameType = common.sameType(cf1, cf2);
//		feature4
		int similarity = common.tokenSimilarity(cf1, cf2);
//		feature5
		int similarState = common.similarStatementV2(cf1, cf2);
//		feature6
		int historyRecords = RosePrediction.historyRecords(cf1.sig, cf2.sig, TestChange2.roseTable, bugName);
		
		System.out.println(af.sig);
//		boolean sameReturnType = common.sameReturnType(cf1, cf2);
//		feature4
//		int commonParameters = common.commonParameters(cf1, cf2);
//		String afReturnType = common.getReturnType(af);
//		AllTypeGetter aTG2 = new AllTypeGetter(cf2.typedNode);
//		Set<String> typeInsideCF2 = aTG2.getTypeSet();
//		feature5
//		boolean hasAFReturnType = true;
//		if (afReturnType != null && !afReturnType.equals("void") && !afReturnType.equals("any") &&
//				!typeInsideCF2.contains(afReturnType)) {
//			hasAFReturnType = false;
//		}
//		int containParameters = 0;
//		List<String> typeInsideCF2List = aTG2.getTypeList();
//		List<String[]> afPara = common.getParameters(af);
////		feature6
//		containParameters = common.containHowManyParameters(typeInsideCF2List, afPara);
		Connection conn = SqliteManager.getConnection();
		try {
			String tableName = TestChange2.afcfTable;
			System.out.println("Yes! We can insert some data!");
			PreparedStatement ps = conn.prepareStatement("INSERT INTO " + tableName + " (afSig,cf1Sig,candiSig,changed,f1,f2,f3,f4,f5,f6) VALUES (?,?,?,?,?,?,?,?,?,?)");
			ps.setString(1, bugName + " " + af.sig);
			ps.setString(2, cf1.sig);
			ps.setString(3, cf2.sig);
			ps.setString(4, changed + "");
			ps.setInt(5, commonFuncInvoke);
			ps.setInt(6, commonFieldAccess);
			ps.setString(7, sameType + "");
			ps.setInt(8, similarity);
			ps.setInt(9, similarState);
			ps.setInt(10, historyRecords);
//			ps.setInt(7, sameReturnType? 1 : 0);
//			ps.setInt(8, commonParameters);
//			ps.setInt(9, hasAFReturnType? 1 : 0);
//			ps.setInt(10, containParameters);
			ps.executeUpdate();
			ps.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public void writeDataCFCFNode(ClientFunc af, ClientFunc cf1, ClientFunc cf2, boolean changed) {
//		CFCFCommon common = new CFCFCommon(afToOldTJ.get(af));
		CFCFCommon common = new CFCFCommon(af.tJroot);
//		System.out.println(af.sig + " " + afToOldTJ.get(af));
//		feature1
		int commonFuncInvoke = common.commonFuncInvo(cf1, cf2, af);
//		feature2
		int commonFieldAccess = common.commonVarAccess(cf1, cf2, af);
//		feature3
		boolean sameType = common.sameType(cf1, cf2);
//		feature4
		int similarity = common.tokenSimilarity(cf1, cf2);
//		feature5
		int similarState = common.similarStatementV2(cf1, cf2);
//		feature6
		int historyRecords = RosePrediction.historyRecords(cf1.sig, cf2.sig, TestChange2.roseTable, bugName);
		
		System.out.println(af.sig);
//		boolean sameReturnType = common.sameReturnType(cf1, cf2);
//		feature4
//		int commonParameters = common.commonParameters(cf1, cf2);
//		String afReturnType = common.getReturnType(af);
//		AllTypeGetter aTG2 = new AllTypeGetter(cf2.typedNode);
//		Set<String> typeInsideCF2 = aTG2.getTypeSet();
//		feature5
//		boolean hasAFReturnType = true;
//		if (afReturnType != null && !afReturnType.equals("void") && !afReturnType.equals("any") &&
//				!typeInsideCF2.contains(afReturnType)) {
//			hasAFReturnType = false;
//		}
//		int containParameters = 0;
//		List<String> typeInsideCF2List = aTG2.getTypeList();
//		List<String[]> afPara = common.getParameters(af);
////		feature6
//		containParameters = common.containHowManyParameters(typeInsideCF2List, afPara);
		Connection conn = SqliteManager.getConnection();
		try {
			String tableName = TestChange2.cfcfTable;
			System.out.println("Yes! We can insert some data!");
			PreparedStatement ps = conn.prepareStatement("INSERT INTO " + tableName + " (cfSig,cf1Sig,candiSig,changed,f1,f2,f3,f4,f5,f6) VALUES (?,?,?,?,?,?,?,?,?,?)");
			ps.setString(1, bugName + " " + af.sig);
			ps.setString(2, cf1.sig);
			ps.setString(3, cf2.sig);
			ps.setString(4, changed + "");
			ps.setInt(5, commonFuncInvoke);
			ps.setInt(6, commonFieldAccess);
			ps.setString(7, sameType + "");
			ps.setInt(8, similarity);
			ps.setInt(9, similarState);
			ps.setInt(10, historyRecords);
//			ps.setInt(7, sameReturnType? 1 : 0);
//			ps.setInt(8, commonParameters);
//			ps.setInt(9, hasAFReturnType? 1 : 0);
//			ps.setInt(10, containParameters);
			ps.executeUpdate();
			ps.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public void writeDataAVCF(ClientVar av, ClientFunc cf1, ClientFunc cf2, boolean changed) {
//		CFCFCommon common = new CFCFCommon(afToOldTJ.get(af));
		AVCFCommon common = new AVCFCommon(avToOldTJ.get(av));
//		System.out.println(af.sig + " " + afToOldTJ.get(af));
//		feature1
		int commonFuncInvoke = common.commonFuncInvo(cf1, cf2, av);
//		feature2
		int commonFieldAccess = common.commonVarAccess(cf1, cf2, av);
//		feature3
		System.out.println(av.sig + " What is this av");
		boolean sameReturnType = common.sameReturnType(cf1, cf2);
//		feature4
		int commonParameters = common.commonParameters(cf1, cf2);
		String avType = common.getAVType(av);
		AllTypeGetter aTG2 = new AllTypeGetter(cf2.typedNode);
		Set<String> typeInsideCF2 = aTG2.getTypeSet();
//		feature5
		boolean hasAVReturnType = true;
		if (avType != null && !avType.equals("any") &&
				!typeInsideCF2.contains(avType)) {
			hasAVReturnType = false;
		}
//		feature6
		boolean sameType = common.sameType(cf1, cf2);
//		feature7
		int similarity = common.tokenSimilarity(cf1, cf2);
//		feature8
		int similarState = common.similarStatementV2(cf1, cf2);
//		feature9
		int historyRecords = RosePrediction.historyRecords(cf1.sig, cf2.sig, TestChange2.roseTable, bugName);
		Connection conn = SqliteManager.getConnection();
		try {
			String tableName = TestChange2.avcfTable;
			System.out.println("Yes! We can insert some data!");
			PreparedStatement ps = conn.prepareStatement("INSERT INTO " + tableName + " (avSig,cf1Sig,candiSig,changed,f1,f2,f3,f4,f5,f6,f7,f8,f9) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)");
			ps.setString(1, bugName + " " + av.sig);
			ps.setString(2, cf1.sig);
			ps.setString(3, cf2.sig);
			ps.setString(4, changed + "");
			ps.setInt(5, commonFuncInvoke);
			ps.setInt(6, commonFieldAccess);
			ps.setString(7, sameReturnType + "");
			ps.setInt(8, commonParameters);
			ps.setString(9, hasAVReturnType + "");
			ps.setString(10, sameType + "");
			ps.setInt(11, similarity);
			ps.setInt(12, similarState);
			ps.setInt(13, historyRecords);
			ps.executeUpdate();
			ps.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public void writeDataAVCFNode(ClientVar av, ClientFunc cf1, ClientFunc cf2, boolean changed) {
//		CFCFCommon common = new CFCFCommon(afToOldTJ.get(af));
		AVCFCommon common = new AVCFCommon(avToOldTJ.get(av));
//		System.out.println(af.sig + " " + afToOldTJ.get(af));
//		feature1
		int commonFuncInvoke = common.commonFuncInvo(cf1, cf2, av);
//		feature2
		int commonFieldAccess = common.commonVarAccess(cf1, cf2, av);
//		feature3
		boolean sameType = common.sameType(cf1, cf2);
//		feature4
		int similarity = common.tokenSimilarity(cf1, cf2);
		
//		feature5
		int similarState = common.similarStatementV2(cf1, cf2);
		
//		feature6
		int historyRecords = RosePrediction.historyRecords(cf1.sig, cf2.sig, TestChange2.roseTable, bugName);
		System.out.println(av.sig + "What is this av");
//		boolean sameReturnType = common.sameReturnType(cf1, cf2);
////		feature4
//		int commonParameters = common.commonParameters(cf1, cf2);
//		String avType = common.getAVType(av);
//		AllTypeGetter aTG2 = new AllTypeGetter(cf2.typedNode);
//		Set<String> typeInsideCF2 = aTG2.getTypeSet();
////		feature5
//		boolean hasAVReturnType = true;
//		if (avType != null && !avType.equals("any") &&
//				!typeInsideCF2.contains(avType)) {
//			hasAVReturnType = false;
//		}
		Connection conn = SqliteManager.getConnection();
		try {
			String tableName = TestChange2.avcfTable;
			System.out.println("Yes! We can insert some data!");
			PreparedStatement ps = conn.prepareStatement("INSERT INTO " + tableName + " (avSig,cf1Sig,candiSig,changed,f1,f2,f3,f4,f5,f6) VALUES (?,?,?,?,?,?,?,?,?,?)");
			ps.setString(1, bugName + " " + av.sig);
			ps.setString(2, cf1.sig);
			ps.setString(3, cf2.sig);
			ps.setString(4, changed + "");
			ps.setInt(5, commonFuncInvoke);
			ps.setInt(6, commonFieldAccess);
			ps.setString(7, sameType + "");
			ps.setInt(8, similarity);
			ps.setInt(9, similarState);
			ps.setInt(10, historyRecords);
//			ps.setInt(7, sameReturnType? 1 : 0);
//			ps.setInt(8, commonParameters);
//			ps.setInt(9, hasAVReturnType? 1 : 0);
			ps.executeUpdate();
			ps.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public Set<ClientFunc> buildAfDataset(ReferenceNode afNode, Graph<ReferenceNode, ReferenceEdge> jgrapht) {
		Set<ClientFunc> cfSet = new HashSet<ClientFunc>();
		for (ReferenceEdge edge: jgrapht.edgesOf(afNode)) {
			if (edge.type != ReferenceEdge.FUNC_INVOKE)
				continue;
			ReferenceNode srcNode = edge.from;
			if (srcNode.type != ReferenceNode.CF)
				continue;
			if (srcNode.equals(afNode)) continue;
			cfSet.add((ClientFunc) srcNode.ref);
			
		}
		if (cfSet.size() < 2)
			return null;
		return cfSet;
	}
	
	public Set<ClientFunc> buildAvDataset(ReferenceNode avNode, Graph<ReferenceNode, ReferenceEdge> jgrapht) {
		Set<ClientFunc> cfSet = new HashSet<ClientFunc>();
		for (ReferenceEdge edge: jgrapht.edgesOf(avNode)) {
			if (edge.type != ReferenceEdge.VAR_ACCESS)
				continue;
			ReferenceNode srcNode = edge.from;
			if (srcNode.type != ReferenceNode.CF)
				continue;
			if (srcNode.equals(avNode)) continue;
			cfSet.add((ClientFunc) srcNode.ref);
			
		}
		if (cfSet.size() < 2)
			return null;
		return cfSet;
	}
	
//	public Set<ClientFunc> buildAFDataSetUn(ClientFunc af)
	
	public static void writeRelationGraph(DirectedSparseGraph<ReferenceNode, ReferenceEdge> g, String filename) {
		String xmlFile = filename + ".xml";
		xmlFile = xmlFile.replaceAll("<", "");
		xmlFile = xmlFile.replaceAll(">", "");
		XStream xstream = new XStream(new StaxDriver());
		try{
			 File file = new File(xmlFile);
			 FileWriter writer=new FileWriter(file);
			 String content = xstream.toXML(g);
			 writer.write(content);
			 writer.close();
		} catch (IOException e){
			 e.printStackTrace();
//			 consolegsydit.ExceptionHandler.process(e);
		}
	}
	
	public static Graph<ReferenceNode, ReferenceEdge>
	convertJungToJGraphT(DirectedSparseGraph<ReferenceNode, ReferenceEdge> graph) {
		Graph<ReferenceNode, ReferenceEdge> g = new DefaultDirectedGraph<ReferenceNode, ReferenceEdge>(ReferenceEdge.class);

		Collection<ReferenceNode> vertices = graph.getVertices();
		for (ReferenceNode vertex: vertices) {
			g.addVertex(vertex);
		}

		Collection<ReferenceEdge> edges = graph.getEdges();
		for (ReferenceEdge edge: edges) {
			ReferenceNode sourceVertex = edge.from;
			ReferenceNode targetVertex = edge.to;
			try {
				g.addEdge(sourceVertex, targetVertex, edge);
			} catch (Exception e) {
				e.printStackTrace();
//				consolegsydit.ExceptionHandler.process(e);
			}
		}
		
		return g;
}
	
	private List<DirectedSparseGraph<ReferenceNode, ReferenceEdge>> mergeChanges(List<DirectedSparseGraph<ReferenceNode, ReferenceEdge>> graphs) {
		List<DirectedSparseGraph<ReferenceNode, ReferenceEdge>> result = new ArrayList<DirectedSparseGraph<ReferenceNode, ReferenceEdge>>();
	
		DirectedSparseGraph<ReferenceNode, ReferenceEdge> g1 = null, g2 = null;
		boolean isChanged = true;
		Set<ReferenceNode> nSet1 = null, nSet2 = null;
		while(isChanged) {
			isChanged = false;
			for (int i = 0; i < graphs.size() - 1; i++) {
				g1 = graphs.get(i);
				if (g1.getVertexCount() == 0)
					continue;
				nSet1 = new HashSet<ReferenceNode>(g1.getVertices());
				for (int j = i + 1; j < graphs.size(); j++) {
					// added by Ye Wang, to fix bug
					boolean isChangedThisTime = false;
					
					g2 = graphs.get(j);
					if (g2.getVertexCount() == 0)
						continue;
					for (ReferenceNode n : nSet1) {
						if (g2.containsVertex(n)) {
							// added by Ye Wang, to fix bug
							isChangedThisTime = true;
							
							isChanged = true;
							break;
						}
					}
					// if (isChanged) {
					// changed by Ye Wang, to fix bug
					if (isChangedThisTime) {
						for (ReferenceNode n : g2.getVertices()) {
							if (!g1.containsVertex(n)) {								
								g1.addVertex(n);
							}
						}
						for (ReferenceEdge e : g2.getEdges()) {
							if (!g1.containsEdge(e)) {
								g1.addEdge(e, e.from, e.to);
							}
						}
						nSet2 = new HashSet<ReferenceNode>(g2.getVertices());
						for (ReferenceNode n : nSet2) {
							g2.removeVertex(n);
						}
						break;
					}
				}
			}
		}
		for (int i = 0; i < graphs.size(); i++) {
			g1 = graphs.get(i);
			if (g1.getVertexCount() != 0) {
				result.add(g1);
//				for (ReferenceEdge e : g1.getEdges()) {
//					System.out.println(e);
//				}
			}
		}
		return result;
	}
	
	private String tokenSig(TraverseJsonTyped tJ, List<String> tokens) {
		
	
//		
//		for(String var : tJ.importEntityToJs.keySet()){
//			
//			if(tokens.contains(var)) tokens.set(tokens.indexOf(var), tJ.importEntityToJs.get(var));
//		}
//		for(String var : tJ.importVarToJs.keySet()) {
//			System.out.println(var +  " " + tokens.contains(var) + " " + tJ.importVarToJs.size());
//			if(tokens.contains(var)) tokens.set(tokens.indexOf(var), tJ.importVarToJs.get(var));
//		}
		Map<String, String> varToJs = new HashMap<String, String>();
		StringBuilder sb = new StringBuilder();
//		System.out.println(tokens.size());
		
		/*
		 * old version of token sig getter
		 
		for(String s : tokens) {
			if(s != null && s.equals(".")) s = "++";
			else if(tJ.importEntityToJs.keySet().contains(s)) s = tJ.importEntityToJs.get(s);
			else if(tJ.importVarToJs.keySet().contains(s)) s = tJ.importVarToJs.get(s);
			sb.append(s);
		}
		*/
		
		/*
		 * A new version of token sig getter
		 */
		
		for(int i = 0; i < tokens.size(); i++) {
			String s = tokens.get(i);
			if(s != null && s.equals(".")) s = "++";
			else if(s.equals("require") && i <= tokens.size() - 4 && tokens.get(i + 1).equals("(") && (i == 0 || !tokens.get(i - 1).equals("."))) {
				String jsFile = tokens.get(i + 2).replace("'", "");
				jsFile = jsFile.replace("\\\"","");
				jsFile = jsFile.replace("\"", "");
				if(jsFile.endsWith(".js")) jsFile = jsFile.replace(".js", "");
				else if(jsFile.endsWith(".json")) jsFile = jsFile.replace(".json", "");
				String getPathSource = null;
				if(jsFile.length() > 0 && jsFile.charAt(0) == '.' || tJ.isNodeJs == true) {
					if (tJ.isNodeJs == true && jsFile.charAt(0) != '.') {
						getPathSource = "lib" + "++" + jsFile.replace("/", "++");
					}
					else getPathSource = TraverseJsonTyped.getPath(tJ.jsFilePath, jsFile);
				}
				
				else getPathSource = jsFile.replace("/", "++");
				
				
				s = getPathSource;
				i += 3;
			}
			
			else if (i >= 2 && i < tokens.size() - 1 && s.equals("new") && tokens.get(i - 1).equals("=") && tJ.importVarToJs.containsKey
					(tokens.get(i + 1))) {
				varToJs.put(tokens.get(i - 2), tJ.importVarToJs.get(tokens.get(i + 1)));
			}
			
					
			else if((i == 0 || (i > 0 && !tokens.get(i - 1).equals("."))) && tJ.importEntityToJs.keySet().contains(s)) s = tJ.importEntityToJs.get(s);
			else if((i == 0 || (i > 0 && !tokens.get(i - 1).equals("."))) && tJ.importVarToJs.keySet().contains(s)) s = tJ.importVarToJs.get(s);
			else if((i == 0 || (i > 0 && !tokens.get(i - 1).equals(".")) || (i > 1 && (tokens.get(i - 2).equals("this") || tokens.get(i - 2).equals("self")) && tokens.get(i - 1).equals(".")))){
				if (tJ.varToClass.containsKey(s)) s = tJ.varToClass.get(s);
				else if (varToJs.containsKey(s)) s= varToJs.get(s);
			}
			sb.append(s);
		}
		
		return sb.toString();
	}
	
	
	
	private String indexTokenSig(TraverseJsonTyped tJ, List<String> tokens) {
//		for(String var : tJ.importIndexCheck.keySet()){
//			
//			if(tokens.contains(var)) tokens.set(tokens.indexOf(var), tJ.importIndexCheck.get(var));
//		}
		StringBuilder sb = new StringBuilder();
//		System.out.println(tokens.size());
		
		/*
		 * old version of index sig token getter
		 */
//		for(String s : tokens) {
//			if(s != null && s.equals(".")) s = "++";
//			else if(tJ.importIndexCheck.keySet().contains(s)) s = tJ.importIndexCheck.get(s);
//			sb.append(s);
//		}
		
		/*
		 * A new version of index sig token getter
		 */
		
		for(int i = 0; i < tokens.size(); i++) {
			String s = tokens.get(i);
			if(s != null && s.equals(".")) s = "++";
			if(s.equals("require") && i <= tokens.size() - 4 && tokens.get(i + 1).equals("(") && (i == 0 || !tokens.get(i - 1).equals("."))) {
				String jsFile = tokens.get(i + 2).replace("'", "");
				jsFile = jsFile.replace("\\\"","");
				jsFile = jsFile.replace("\"", "");
				if(jsFile.endsWith(".js")) jsFile = jsFile.replace(".js", "");
				else if(jsFile.endsWith(".json")) jsFile = jsFile.replace(".json", "");
				String getPathSource = null;
				if(jsFile.length() > 0 && jsFile.charAt(0) == '.' || tJ.isNodeJs == true) {
					if (tJ.isNodeJs == true && jsFile.charAt(0) != '.') {
						getPathSource = "lib" + "++" + jsFile.replace("/", "++");
					}
					else getPathSource = TraverseJsonTyped.getPath(tJ.jsFilePath, jsFile);
				}
				
				else getPathSource = jsFile.replace("/", "++");
				
				
				s = getPathSource;
				i += 3;
			}
			else if(i > 0 && !tokens.get(i - 1).equals(".") && tJ.importIndexCheck.keySet().contains(s)) s = tJ.importIndexCheck.get(s);
			sb.append(s);
		}
		
		return sb.toString();
	}
	
	private void addIsolatedNodes(List<DirectedSparseGraph<ReferenceNode, ReferenceEdge>> graphs,
			Set<? extends ClientMember> refs, int nodeType) {
		List<DirectedSparseGraph<ReferenceNode, ReferenceEdge>> graphsToBeAdded = new ArrayList<>();
		for (ClientMember ref: refs) {
			boolean refExistsInGraphs = false;
			for (DirectedSparseGraph<ReferenceNode, ReferenceEdge> graph: graphs) {
				for (ReferenceNode node: graph.getVertices()) {
					if (node.ref == ref) {
						refExistsInGraphs = true;
						break;
					}
				}
				if (refExistsInGraphs)
					break;
			}
			if (!refExistsInGraphs) {
				DirectedSparseGraph<ReferenceNode, ReferenceEdge> g = new DirectedSparseGraph<ReferenceNode, ReferenceEdge>();
				ReferenceNode node = new ReferenceNode(ref, nodeType);
				g.addVertex(node);
				graphsToBeAdded.add(g);
			}
		}
		graphs.addAll(graphsToBeAdded);
	}
	
//	This method is going to check if the token is defined somewhere else.
	
	
	
	private static List<DirectedSparseGraph<ReferenceNode, ReferenceEdge>> convertNewVersionToOldVersion(
			List<DirectedSparseGraph<ReferenceNode, ReferenceEdge>> graphs,
			Map<? extends ClientMember, ? extends ClientMember> newToOldVersionMap) {
		List<DirectedSparseGraph<ReferenceNode, ReferenceEdge>> result = new ArrayList<>();
		for (DirectedSparseGraph<ReferenceNode, ReferenceEdge> graph: graphs) {
			
			if (graph.getVertexCount() == 0)
				continue;
			
			DirectedSparseGraph<ReferenceNode, ReferenceEdge> modifiedGraph =
					new DirectedSparseGraph<ReferenceNode, ReferenceEdge>();
			
			Map<ClientMember, ReferenceNode> refToNode = new HashMap<>();
			
			Collection<ReferenceNode> vertices = graph.getVertices();
			for (ReferenceNode vertex: vertices) {
				if (newToOldVersionMap.containsKey(vertex.ref)) {
					ClientMember oldVersionRef = newToOldVersionMap.get(vertex.ref);
					ReferenceNode oldVersionVertex = new ReferenceNode(oldVersionRef, vertex.type);
					modifiedGraph.addVertex(oldVersionVertex);
					refToNode.put((ClientMember) vertex.ref, oldVersionVertex);
				} else {
					modifiedGraph.addVertex(vertex);
					refToNode.put((ClientMember) vertex.ref, vertex);
				}
			}
			
			Collection<ReferenceEdge> edges = graph.getEdges();
			for (ReferenceEdge edge: edges) {
				ReferenceNode src = edge.from;
				ReferenceNode dst = edge.to;
				ReferenceNode newSrc = refToNode.get(src.ref);
				ReferenceNode newDst = refToNode.get(dst.ref);
				
				ReferenceEdge modifiedEdge = new ReferenceEdge(newSrc, newDst, edge.type);
				modifiedEdge.dep = edge.dep;
//				System.out.println("src: " + src);
//				System.out.println("dst: " + dst);
//				System.out.println("newSrc: " + newSrc);
//				System.out.println("newDst: " + newDst);
				modifiedGraph.addEdge(modifiedEdge, newSrc, newDst);
			}
			
			result.add(modifiedGraph);
		}
		
		return result;
	}
	
	
	private void analyzeVarAccessFunc(List<DirectedSparseGraph<ReferenceNode, ReferenceEdge>> graphs,
			Set<ClientFunc> functions, int rootFunctionNodeType,
			Set<ClientVar> varRefs, int varNodeType) {
		if (varRefs.isEmpty()) return;
		for (ClientFunc func: functions) {
			DirectedSparseGraph<ReferenceNode, ReferenceEdge> graph =
					new DirectedSparseGraph<ReferenceNode, ReferenceEdge>();
			graphs.add(graph);
			ReferenceNode root = new ReferenceNode(func, rootFunctionNodeType);
			
			TraverseJsonTyped tJf = func.tJroot;
			String funcName = func.name;
			ITree astNode = func.node;
			SigGetter insideSigs = new SigGetter(astNode, tJf, funcName);
			
			for (ClientVar var : varRefs) {
				if(var.path.equals(func.path)) {
					if(insideSigs.varSigs.contains(var.sig)) {
						
						TestChange2.inOrOut[0]++;
						
						ReferenceNode n = new ReferenceNode(var, varNodeType);
						ReferenceEdge edge = graph.findEdge(root, n);
						if (edge == null) {
							edge = new ReferenceEdge(root, n, ReferenceEdge.VAR_ACCESS);
							graph.addEdge(edge, root, n);
						}
						edge.increaseCount();
					}
					else continue;
				}
				else {
					String sigedFunc = tokenSig(tJf, tJf.expNameToToken.get(funcName));
					String indexSigedFunc = indexTokenSig(tJf, tJf.expNameToToken.get(funcName));
					System.out.println(sigedFunc);
					System.out.println(var.exportedSig);
					String sig = var.exportedSig, path = var.path, name = var.exportedName;
					String shortSig = sig;
					if (name.contains("++")) {
						shortSig = path + name.substring(name.indexOf("++"));
					}
					if(sigedFunc.contains(sig) || sigedFunc.contains(shortSig) || indexSigedFunc.contains(sig)|| indexSigedFunc.contains(shortSig) || sigedFunc.contains(path) && var.tJroot.exportVarName.equals(name)
							|| indexSigedFunc.contains(path) && var.tJroot.exportVarName.equals(name)) {
						TestChange2.inOrOut[1]++;
						ReferenceNode n = new ReferenceNode(var, varNodeType);
						ReferenceEdge edge = graph.findEdge(root, n);
						if (edge == null) {
							edge = new ReferenceEdge(root, n, ReferenceEdge.VAR_ACCESS);
							graph.addEdge(edge, root, n);
						}
						edge.increaseCount();
					}	
				}
			}
		}
	}
	
	
	
	private void analyzeFuncAccessClass(List<DirectedSparseGraph<ReferenceNode, ReferenceEdge>> graphs,
	Set<ClientClass> classes, int rootClassNodeType,
	Set<ClientFunc> funcRefs, int funcNodeType) {
		if (funcRefs.isEmpty()) return;
		for (ClientClass klass: classes) {
			DirectedSparseGraph<ReferenceNode, ReferenceEdge> graph =
					new DirectedSparseGraph<ReferenceNode, ReferenceEdge>();
			graphs.add(graph);
			ReferenceNode root = new ReferenceNode(klass, rootClassNodeType);
			String className = klass.name;
			String[] clas = className.split(" ");
			className = clas[0];
			for (ClientFunc toFunc : funcRefs) {
				if(toFunc.name.contains(className + "++")) {
					TestChange2.inOrOut[0]++;
					ReferenceNode n = new ReferenceNode(toFunc, funcNodeType);
					ReferenceEdge edge = graph.findEdge(root, n);
					if (edge == null) {
						edge = new ReferenceEdge(root, n, ReferenceEdge.CLASS_CONTAIN);
						graph.addEdge(edge, root, n);
					}
					edge.increaseCount();
				}
			}
		}
	}
	
	private void analyzeVarAccessClass(List<DirectedSparseGraph<ReferenceNode, ReferenceEdge>> graphs,
			Set<ClientClass> classes, int rootClassNodeType,
			Set<ClientVar> varRefs, int varNodeType) {
			if (varRefs.isEmpty()) return;
			for (ClientClass klass: classes) {
				DirectedSparseGraph<ReferenceNode, ReferenceEdge> graph =
						new DirectedSparseGraph<ReferenceNode, ReferenceEdge>();
				graphs.add(graph);
				ReferenceNode root = new ReferenceNode(klass, rootClassNodeType);
				String className = klass.name;
				String[] clas = className.split(" ");
				className = clas[0];
				for (ClientVar toVar : varRefs) {
					if(toVar.name.contains(className + "++")) {
						TestChange2.inOrOut[0]++;
						ReferenceNode n = new ReferenceNode(toVar, varNodeType);
						ReferenceEdge edge = graph.findEdge(root, n);
						if (edge == null) {
							edge = new ReferenceEdge(root, n, ReferenceEdge.CLASS_CONTAIN);
							graph.addEdge(edge, root, n);
						}
						edge.increaseCount();
					}
				}
			}
		}
	
	
	private void analyzeVarAccessVar(List<DirectedSparseGraph<ReferenceNode, ReferenceEdge>> graphs,
			Set<ClientVar> vars, int rootVarNodeType,
			Set<ClientVar> varRefs, int varNodeType) {
		if (varRefs.isEmpty()) return;
		for (ClientVar var: vars) {
			DirectedSparseGraph<ReferenceNode, ReferenceEdge> graph =
					new DirectedSparseGraph<ReferenceNode, ReferenceEdge>();
			graphs.add(graph);
			ReferenceNode root = new ReferenceNode(var, rootVarNodeType);
			
			
			TraverseJsonTyped tJf = var.tJroot;
			String varName = var.name;
			ITree astNode = var.node;
			SigGetter insideSigs = new SigGetter(astNode, tJf, varName);
			
			
			for (ClientVar toVar : varRefs) {
				if(toVar.equals(var)) continue;
				if(toVar.path.equals(var.path)) {
					if(insideSigs.varSigs.contains(toVar.sig)) {
						TestChange2.inOrOut[0]++;
						ReferenceNode n = new ReferenceNode(toVar, varNodeType);
						ReferenceEdge edge = graph.findEdge(root, n);
						if (edge == null) {
							edge = new ReferenceEdge(root, n, ReferenceEdge.VAR_ACCESS);
							graph.addEdge(edge, root, n);
						}
						edge.increaseCount();
					}
					else continue;
				}
				else {
					String siged = tokenSig(tJf, tJf.expNameToToken.get(varName));
					String indexSiged = indexTokenSig(tJf, tJf.expNameToToken.get(varName));
					System.out.println(siged);
					System.out.println(toVar.sig);
					String shortSig = toVar.exportedSig, name = toVar.exportedName, path = toVar.path;
					if (name.contains("++")) {
						shortSig = toVar.path + name.substring(name.indexOf("++"));
					}
					if(siged.contains(shortSig) || indexSiged.contains(shortSig) || siged.contains(toVar.exportedSig) || indexSiged.contains(toVar.exportedSig) || siged.contains(toVar.path) && toVar.tJroot.exportVarName.equals(toVar.exportedName) ||
							indexSiged.contains(toVar.path) && toVar.tJroot.exportVarName.equals(toVar.exportedName)) {
						TestChange2.inOrOut[1]++;
						ReferenceNode n = new ReferenceNode(toVar, varNodeType);
						ReferenceEdge edge = graph.findEdge(root, n);
						if (edge == null) {
							edge = new ReferenceEdge(root, n, ReferenceEdge.VAR_ACCESS);
							graph.addEdge(edge, root, n);
						}
						edge.increaseCount();
					}	
				}
			}
		}
	}
	
	private void analyzeVarAccessExp(List<DirectedSparseGraph<ReferenceNode, ReferenceEdge>> graphs,
			Set<ClientExp> exps, int rootExpNodeType,
			Set<ClientVar> varRefs, int varNodeType) {
		if (varRefs.isEmpty()) return;
		for (ClientExp exp: exps) {
			DirectedSparseGraph<ReferenceNode, ReferenceEdge> graph =
					new DirectedSparseGraph<ReferenceNode, ReferenceEdge>();
			graphs.add(graph);
			ReferenceNode root = new ReferenceNode(exp, rootExpNodeType);
			

			TraverseJsonTyped tJf = exp.tJroot;
			String expName = exp.name;
			ITree astNode = exp.node;
			SigGetter insideSigs = new SigGetter(astNode, tJf, expName);
			
			
			
			
			for (ClientVar toVar : varRefs) {
				if(toVar.path.equals(exp.path)) {
					if(insideSigs.varSigs.contains(toVar.sig)) {

						boolean isDefDef = false;

//						
						if(isDefDef == false) {
							TestChange2.inOrOut[0]++;
							ReferenceNode n = new ReferenceNode(toVar, varNodeType);
							ReferenceEdge edge = graph.findEdge(root, n);
							if (edge == null) {
								edge = new ReferenceEdge(root, n, ReferenceEdge.VAR_ACCESS);
								graph.addEdge(edge, root, n);
							}
							edge.increaseCount();
						}
					}
					else continue;
				}
				else {
					System.out.println(expName);
					String siged = tokenSig(tJf, tJf.expNameToToken.get(expName));
					String indexSiged = indexTokenSig(tJf, tJf.expNameToToken.get(expName));
					System.out.println(siged);
					System.out.println(toVar.sig);
					String shortSig = toVar.exportedSig, name = toVar.exportedName, path = toVar.path;
					if (name.contains("++")) {
						shortSig = toVar.path + name.substring(name.indexOf("++"));
					}
					if(siged.contains(toVar.exportedSig) || siged.contains(shortSig) || siged.contains(toVar.path) && toVar.tJroot.exportVarName.equals(toVar.exportedName)
							|| indexSiged.contains(toVar.exportedSig) || indexSiged.contains(shortSig) || indexSiged.contains(toVar.path) && toVar.tJroot.exportVarName.equals(toVar.exportedName)) {
						boolean isDefDef = false;

						
						if(isDefDef == false){
							TestChange2.inOrOut[1]++;
							ReferenceNode n = new ReferenceNode(toVar, varNodeType);
							ReferenceEdge edge = graph.findEdge(root, n);
							if (edge == null) {
								edge = new ReferenceEdge(root, n, ReferenceEdge.VAR_ACCESS);
								graph.addEdge(edge, root, n);
							}
							edge.increaseCount();
						}
					}	
				}
			}
			
		}
	}
	
//	private void analyzeVarAccessClass(List<DirectedSparseGraph<ReferenceNode, ReferenceEdge>> graphs,
//			Set<ClientClass> classes, int rootClassNodeType,
//			Set<ClientVar> varRefs, int varNodeType) {
//		if (varRefs.isEmpty()) return;
//		for (ClientClass klass: classes) {
//			DirectedSparseGraph<ReferenceNode, ReferenceEdge> graph =
//					new DirectedSparseGraph<ReferenceNode, ReferenceEdge>();
//			graphs.add(graph);
//			ReferenceNode root = new ReferenceNode(klass, rootClassNodeType);
//			
//			TraverseJsonTyped tJf = klass.tJroot;
//			String className = klass.name;
//			ITree astNode = klass.node;
//			SigGetter insideSigs = new SigGetter(astNode, tJf, className);
//			
//			
//			for (ClientVar toVar : varRefs) {
//				if(toVar.path.equals(klass.path)) {
//					if(insideSigs.varSigs.contains(toVar.sig)) {
//
//						TestChange2.inOrOut[0]++;
//						ReferenceNode n = new ReferenceNode(toVar, varNodeType);
//						ReferenceEdge edge = graph.findEdge(root, n);
//						if (edge == null) {
//							edge = new ReferenceEdge(root, n, ReferenceEdge.VAR_ACCESS);
//							graph.addEdge(edge, root, n);
//						}
//						edge.increaseCount();
//					}
//					else continue;
//				}
//				else {
//					String siged = tokenSig(tJf, tJf.expNameToToken.get(className));
//					String indexSiged = indexTokenSig(tJf, tJf.expNameToToken.get(className));
//					System.out.println(siged);
//					System.out.println(toVar.sig);
//					String shortSig = toVar.exportedSig, name = toVar.exportedName, path = toVar.path;
//					if (name.contains("++")) {
//						shortSig = toVar.path + name.substring(name.indexOf("++"));
//					}
//					if(siged.contains(toVar.exportedSig) || siged.contains(shortSig) || siged.contains(toVar.path) && toVar.tJroot.exportVarName.equals(toVar.exportedName)
//							|| indexSiged.contains(toVar.exportedSig) || indexSiged.contains(shortSig) || indexSiged.contains(toVar.path) && toVar.tJroot.exportVarName.equals(toVar.exportedName)) {
//						TestChange2.inOrOut[1]++;
//						ReferenceNode n = new ReferenceNode(toVar, varNodeType);
//						ReferenceEdge edge = graph.findEdge(root, n);
//						if (edge == null) {
//							edge = new ReferenceEdge(root, n, ReferenceEdge.VAR_ACCESS);
//							graph.addEdge(edge, root, n);
//						}
//						edge.increaseCount();
//					}	
//				}
//			}
//		}
//	}
	
	private void analyzeFuncAccessVar(List<DirectedSparseGraph<ReferenceNode, ReferenceEdge>> graphs,
			Set<ClientVar> vars, int rootVarNodeType,
			Set<ClientFunc> funcRefs, int funcNodeType) {
		if (funcRefs.isEmpty()) return;
		for (ClientVar var: vars) {
			DirectedSparseGraph<ReferenceNode, ReferenceEdge> graph =
					new DirectedSparseGraph<ReferenceNode, ReferenceEdge>();
			graphs.add(graph);
			ReferenceNode root = new ReferenceNode(var, rootVarNodeType);
			
			TraverseJsonTyped tJf = var.tJroot;
			String varName = var.name;
			ITree astNode = var.node;
			SigGetter insideSigs = new SigGetter(astNode, tJf, varName);
			
			
			for (ClientFunc func : funcRefs) {
				if(func.path.equals(var.path)) {
					if(insideSigs.funcSigs.contains(func.sig)) {
						TestChange2.inOrOut[0]++;
						ReferenceNode n = new ReferenceNode(func, funcNodeType);
						ReferenceEdge edge = graph.findEdge(root, n);
						if (edge == null) {
							edge = new ReferenceEdge(root, n, ReferenceEdge.FUNC_INVOKE);
							graph.addEdge(edge, root, n);
						}
						edge.increaseCount();
					}
					else continue;
				}
				else {
					String siged = tokenSig(tJf, tJf.expNameToToken.get(varName));
					String indexSiged = indexTokenSig(tJf, tJf.expNameToToken.get(varName));
					System.out.println(siged);
					System.out.println(func.sig);
					String shortSig = func.exportedSig, name = func.exportedName, path = func.path;
					if (name.contains("++")) {
						shortSig = path + name.substring(name.indexOf("++"));
					}
					
					if(siged.contains(func.exportedSig + "(") || siged.contains(shortSig + "(") || siged.contains(func.path) && func.tJroot.exportVarName.equals(func.exportedName)
							|| indexSiged.contains(func.exportedSig + "(") || indexSiged.contains(shortSig + "(") || indexSiged.contains(func.path) && func.tJroot.exportVarName.equals(func.exportedName)) {
						TestChange2.inOrOut[1]++;
						ReferenceNode n = new ReferenceNode(func, funcNodeType);
						ReferenceEdge edge = graph.findEdge(root, n);
						if (edge == null) {
							edge = new ReferenceEdge(root, n, ReferenceEdge.FUNC_INVOKE);
							graph.addEdge(edge, root, n);
						}
						edge.increaseCount();
					}	
				}
			}
		}
	}
	
	private void analyzeFuncAccessFunc(List<DirectedSparseGraph<ReferenceNode, ReferenceEdge>> graphs,
			Set<ClientFunc> funcs, int rootFuncNodeType,
			Set<ClientFunc> funcRefs, int funcNodeType) {
		if (funcRefs.isEmpty()) return;
		for (ClientFunc func: funcs) {
			DirectedSparseGraph<ReferenceNode, ReferenceEdge> graph =
					new DirectedSparseGraph<ReferenceNode, ReferenceEdge>();
			graphs.add(graph);
			ReferenceNode root = new ReferenceNode(func, rootFuncNodeType);
			

			TraverseJsonTyped tJf = func.tJroot;
			String funcName = func.name;
			ITree astNode = func.node;
			SigGetter insideSigs = new SigGetter(astNode, tJf, funcName);
			
			
			for (ClientFunc toFunc : funcRefs) {
				if(toFunc.equals(func)) continue;
				
				
				if(toFunc.path.equals(func.path)) {
					if(insideSigs.funcSigs.contains(toFunc.sig)) {
						TestChange2.inOrOut[0]++;
						ReferenceNode n = new ReferenceNode(toFunc, funcNodeType);
						ReferenceEdge edge = graph.findEdge(root, n);
						if (edge == null) {
							edge = new ReferenceEdge(root, n, ReferenceEdge.FUNC_INVOKE);
							graph.addEdge(edge, root, n);
						}
						edge.increaseCount();
					}
					else continue;
				}
				else {
					String siged = tokenSig(tJf, tJf.expNameToToken.get(funcName));
					String indexSiged = indexTokenSig(tJf, tJf.expNameToToken.get(funcName));
					System.out.println(siged);
					System.out.println(toFunc.sig);
					String shortSig = toFunc.exportedSig, name = toFunc.exportedName, path = toFunc.path;
					if (name.contains("++")) {
						shortSig = path + name.substring(name.indexOf("++"));
					}
					if(siged.contains(toFunc.exportedSig + "(") || siged.contains(shortSig + "(") || siged.contains(toFunc.path) && toFunc.tJroot.exportVarName.equals(toFunc.exportedName)
							|| indexSiged.contains(toFunc.exportedSig + "(") || indexSiged.contains(shortSig + "(") || indexSiged.contains(toFunc.path) && toFunc.tJroot.exportVarName.equals(toFunc.exportedName)) {
						TestChange2.inOrOut[1]++;
						ReferenceNode n = new ReferenceNode(toFunc, funcNodeType);
						ReferenceEdge edge = graph.findEdge(root, n);
						if (edge == null) {
							edge = new ReferenceEdge(root, n, ReferenceEdge.FUNC_INVOKE);
							graph.addEdge(edge, root, n);
						}
						edge.increaseCount();
					}	
				}
			}
		}
	}
	
//	private void analyzeFuncAccessClass(List<DirectedSparseGraph<ReferenceNode, ReferenceEdge>> graphs,
//			Set<ClientClass> classes, int rootClassNodeType,
//			Set<ClientFunc> funcRefs, int funcNodeType) {
//		if (funcRefs.isEmpty()) return;
//		for (ClientClass klass: classes) {
//			DirectedSparseGraph<ReferenceNode, ReferenceEdge> graph =
//					new DirectedSparseGraph<ReferenceNode, ReferenceEdge>();
//			graphs.add(graph);
//			ReferenceNode root = new ReferenceNode(klass, rootClassNodeType);
//			
//			TraverseJsonTyped tJf = klass.tJroot;
//			String className = klass.name;
//			ITree astNode = klass.node;
//			SigGetter insideSigs = new SigGetter(astNode, tJf, className);
//			
//			
//			for (ClientFunc toFunc : funcRefs) {
//				if(toFunc.path.equals(klass.path)) {
//					if(insideSigs.funcSigs.contains(toFunc.sig)) {
//						TestChange2.inOrOut[0]++;
//						ReferenceNode n = new ReferenceNode(toFunc, funcNodeType);
//						ReferenceEdge edge = graph.findEdge(root, n);
//						if (edge == null) {
//							edge = new ReferenceEdge(root, n, ReferenceEdge.FUNC_INVOKE);
//							graph.addEdge(edge, root, n);
//						}
//						edge.increaseCount();
//					}
//					else continue;
//				}
//				else {
//					String siged = tokenSig(tJf, tJf.expNameToToken.get(className));
//					String indexSiged = indexTokenSig(tJf, tJf.expNameToToken.get(className));
//					System.out.println(siged);
//					System.out.println(toFunc.sig);
//					String shortSig = toFunc.exportedSig, name = toFunc.exportedName, path = toFunc.path;
//					if (name.contains("++")) {
//						shortSig = path + name.substring(name.indexOf("++"));
//					}
//					if(siged.contains(toFunc.exportedSig + "(") || siged.contains(shortSig + "(") || siged.contains(toFunc.path) && toFunc.tJroot.exportVarName.equals(toFunc.exportedName)
//							|| indexSiged.contains(toFunc.exportedSig + "(") || indexSiged.contains(shortSig + "(") || indexSiged.contains(toFunc.path) && toFunc.tJroot.exportVarName.equals(toFunc.exportedName)) {
//						TestChange2.inOrOut[1]++;
//						ReferenceNode n = new ReferenceNode(toFunc, funcNodeType);
//						ReferenceEdge edge = graph.findEdge(root, n);
//						if (edge == null) {
//							edge = new ReferenceEdge(root, n, ReferenceEdge.FUNC_INVOKE);
//							graph.addEdge(edge, root, n);
//						}
//						edge.increaseCount();
//					}	
//				}
//			}
//		}
//	}
	
	private void analyzeFuncAccessExp(List<DirectedSparseGraph<ReferenceNode, ReferenceEdge>> graphs,
			Set<ClientExp> exps, int rootExpNodeType,
			Set<ClientFunc> funcRefs, int funcNodeType) {
		if (funcRefs.isEmpty()) return;
		for (ClientExp exp: exps) {
			DirectedSparseGraph<ReferenceNode, ReferenceEdge> graph =
					new DirectedSparseGraph<ReferenceNode, ReferenceEdge>();
			graphs.add(graph);
			ReferenceNode root = new ReferenceNode(exp, rootExpNodeType);
			
			TraverseJsonTyped tJf = exp.tJroot;
			String expName = exp.name;
			ITree astNode = exp.node;
			SigGetter insideSigs = new SigGetter(astNode, tJf, expName);
			
			
			for (ClientFunc toFunc : funcRefs) {
				if(toFunc.path.equals(exp.path)) {
					if(insideSigs.funcSigs.contains(toFunc.sig)) {
						TestChange2.inOrOut[0]++;
						ReferenceNode n = new ReferenceNode(toFunc, funcNodeType);
						ReferenceEdge edge = graph.findEdge(root, n);
						if (edge == null) {
							edge = new ReferenceEdge(root, n, ReferenceEdge.FUNC_INVOKE);
							graph.addEdge(edge, root, n);
						}
						edge.increaseCount();
					}
					else continue;
				}
				else {
					String siged = tokenSig(tJf, tJf.expNameToToken.get(expName));
					String indexSiged = indexTokenSig(tJf, tJf.expNameToToken.get(expName));
					System.out.println(siged);
					System.out.println(toFunc.sig);
					String shortSig = toFunc.exportedSig, name = toFunc.exportedName, path = toFunc.path;
					if (name.contains("++")) {
						shortSig = path + name.substring(name.indexOf("++"));
					}
					if(siged.contains(toFunc.exportedSig + "(") || siged.contains(shortSig + "(") || siged.contains(toFunc.path) && toFunc.tJroot.exportVarName.equals(toFunc.exportedName)
							|| indexSiged.contains(toFunc.exportedSig) || indexSiged.contains(shortSig + "(") || indexSiged.contains(toFunc.path) && toFunc.tJroot.exportVarName.equals(toFunc.exportedName)) {
						TestChange2.inOrOut[1]++;
						ReferenceNode n = new ReferenceNode(toFunc, funcNodeType);
						ReferenceEdge edge = graph.findEdge(root, n);
						if (edge == null) {
							edge = new ReferenceEdge(root, n, ReferenceEdge.FUNC_INVOKE);
							graph.addEdge(edge, root, n);
						}
						edge.increaseCount();
					}	
				}
			}
		}
	}
	
//	private void analyzeClassAccessVar(List<DirectedSparseGraph<ReferenceNode, ReferenceEdge>> graphs,
//			Set<ClientVar> vars, int rootVarNodeType,
//			Set<ClientClass> classRefs, int classNodeType) {
//		if (classRefs.isEmpty()) return;
//		for (ClientVar var: vars) {
//			DirectedSparseGraph<ReferenceNode, ReferenceEdge> graph =
//					new DirectedSparseGraph<ReferenceNode, ReferenceEdge>();
//			graphs.add(graph);
//			ReferenceNode root = new ReferenceNode(var, rootVarNodeType);
//			
//			TraverseJsonTyped tJf = var.tJroot;
//			String varName = var.name;
//			ITree astNode = var.node;
//			SigGetter insideSigs = new SigGetter(astNode, tJf, varName);
//			
//			
//			for (ClientClass klass : classRefs) {
//				if(klass.path.equals(var.path)) {
//					if(insideSigs.classSigs.contains(klass.sig)) {
//						TestChange2.inOrOut[0]++;
//						ReferenceNode n = new ReferenceNode(klass, classNodeType);
//						ReferenceEdge edge = graph.findEdge(root, n);
//						if (edge == null) {
//							edge = new ReferenceEdge(root, n, ReferenceEdge.CLASS_ACCESS);
//							graph.addEdge(edge, root, n);
//						}
//						edge.increaseCount();
//					}
//					else continue;
//				}
//				else {
//					String siged = tokenSig(tJf, tJf.expNameToToken.get(varName));
//					String indexSiged = indexTokenSig(tJf, tJf.expNameToToken.get(varName));
//					System.out.println(siged);
//					System.out.println(klass.sig);
//					if(siged.contains(klass.exportedSig) || siged.contains(klass.path) && klass.tJroot.exportVarName.equals(klass.exportedName)
//							|| indexSiged.contains(klass.exportedSig) || indexSiged.contains(klass.path) && klass.tJroot.exportVarName.equals(klass.exportedName)) {
//						TestChange2.inOrOut[1]++;
//						ReferenceNode n = new ReferenceNode(klass, classNodeType);
//						ReferenceEdge edge = graph.findEdge(root, n);
//						if (edge == null) {
//							edge = new ReferenceEdge(root, n, ReferenceEdge.CLASS_ACCESS);
//							graph.addEdge(edge, root, n);
//						}
//						edge.increaseCount();
//					}	
//				}
//			}
//			
//		}
//	}
//	
//	private void analyzeClassAccessFunc(List<DirectedSparseGraph<ReferenceNode, ReferenceEdge>> graphs,
//			Set<ClientFunc> funcs, int rootFuncNodeType,
//			Set<ClientClass> classRefs, int classNodeType) {
//		if (classRefs.isEmpty()) return;
//		for (ClientFunc func: funcs) {
//			DirectedSparseGraph<ReferenceNode, ReferenceEdge> graph =
//					new DirectedSparseGraph<ReferenceNode, ReferenceEdge>();
//			graphs.add(graph);
//			ReferenceNode root = new ReferenceNode(func, rootFuncNodeType);
//			
//			TraverseJsonTyped tJf = func.tJroot;
//			String funcName = func.name;
//			ITree astNode = func.node;
//			SigGetter insideSigs = new SigGetter(astNode, tJf, funcName);
//			
//			
//			for (ClientClass klass : classRefs) {
//				if(klass.path.equals(func.path)) {
//					if(insideSigs.classSigs.contains(klass.sig)) {
//						TestChange2.inOrOut[0]++;
//						ReferenceNode n = new ReferenceNode(klass, classNodeType);
//						ReferenceEdge edge = graph.findEdge(root, n);
//						if (edge == null) {
//							edge = new ReferenceEdge(root, n, ReferenceEdge.CLASS_ACCESS);
//							graph.addEdge(edge, root, n);
//						}
//						edge.increaseCount();
//					}
//					else continue;
//				}
//				else {
//					String siged = tokenSig(tJf, tJf.expNameToToken.get(funcName));
//					String indexSiged = indexTokenSig(tJf, tJf.expNameToToken.get(funcName));
//					System.out.println(siged);
//					System.out.println(klass.sig);
//					if(siged.contains(klass.exportedSig) || siged.contains(klass.path) && klass.tJroot.exportVarName.equals(klass.exportedName)
//							|| indexSiged.contains(klass.exportedSig) || indexSiged.contains(klass.path) && klass.tJroot.exportVarName.equals(klass.exportedName)) {
//						TestChange2.inOrOut[1]++;
//						ReferenceNode n = new ReferenceNode(klass, classNodeType);
//						ReferenceEdge edge = graph.findEdge(root, n);
//						if (edge == null) {
//							edge = new ReferenceEdge(root, n, ReferenceEdge.CLASS_ACCESS);
//							graph.addEdge(edge, root, n);
//						}
//						edge.increaseCount();
//					}	
//				}
//			}
//		}
//	}
//	
	private void analyzeClassAccessClass(List<DirectedSparseGraph<ReferenceNode, ReferenceEdge>> graphs,
			Set<ClientClass> classes, int rootClassNodeType,
			Set<ClientClass> classRefs, int classNodeType) {
		if (classRefs.isEmpty()) return;
		for (ClientClass klass: classes) {
			DirectedSparseGraph<ReferenceNode, ReferenceEdge> graph =
					new DirectedSparseGraph<ReferenceNode, ReferenceEdge>();
			graphs.add(graph);
			ReferenceNode root = new ReferenceNode(klass, rootClassNodeType);
			

			TraverseJsonTyped tJf = klass.tJroot;
			String className = klass.name;
			String superClass = tJf.superClassFinder.get(className);
			for (ClientClass toKlass : classRefs) {
				
				if(toKlass.equals(klass)) continue;
				
				if(superClass != null && superClass.equals(toKlass.name)) {
					TestChange2.inOrOut[2]++;
					ReferenceNode n = new ReferenceNode(toKlass, classNodeType);
					ReferenceEdge edge = graph.findEdge(root, n);
					if (edge == null) {
						edge = new ReferenceEdge(root, n, ReferenceEdge.CLASS_INHERITANCE);
						graph.addEdge(edge, root, n);
					}
					edge.increaseCount();
					continue;
				}
			}
		}
	}
//	
//	private void analyzeClassAccessExp(List<DirectedSparseGraph<ReferenceNode, ReferenceEdge>> graphs,
//			Set<ClientExp> exps, int rootExpNodeType,
//			Set<ClientClass> classRefs, int classNodeType) {
//		if (classRefs.isEmpty()) return;
//		for (ClientExp exp: exps) {
//			DirectedSparseGraph<ReferenceNode, ReferenceEdge> graph =
//					new DirectedSparseGraph<ReferenceNode, ReferenceEdge>();
//			graphs.add(graph);
//			ReferenceNode root = new ReferenceNode(exp, rootExpNodeType);
//			
//
//			TraverseJsonTyped tJf = exp.tJroot;
//			String expName = exp.name;
//			ITree astNode = exp.node;
//			SigGetter insideSigs = new SigGetter(astNode, tJf, expName);
//			
//			
//			for (ClientClass klass : classRefs) {
//				if(klass.path.equals(exp.path)) {
//					if(insideSigs.classSigs.contains(klass.sig)) {
//						TestChange2.inOrOut[0]++;
//						ReferenceNode n = new ReferenceNode(klass, classNodeType);
//						ReferenceEdge edge = graph.findEdge(root, n);
//						if (edge == null) {
//							edge = new ReferenceEdge(root, n, ReferenceEdge.CLASS_ACCESS);
//							graph.addEdge(edge, root, n);
//						}
//						edge.increaseCount();
//					}
//					else continue;
//				}
//				else {
//					String siged = tokenSig(tJf, tJf.expNameToToken.get(expName));
//					String indexSiged = indexTokenSig(tJf, tJf.expNameToToken.get(expName));
//					System.out.println(siged);
//					System.out.println(klass.sig);
//					if(siged.contains(klass.exportedSig) || siged.contains(klass.path) && klass.tJroot.exportVarName.equals(klass.exportedName)
//							|| indexSiged.contains(klass.exportedSig) || indexSiged.contains(klass.path) && klass.tJroot.exportVarName.equals(klass.exportedName)) {
//						TestChange2.inOrOut[1]++;
//						ReferenceNode n = new ReferenceNode(klass, classNodeType);
//						ReferenceEdge edge = graph.findEdge(root, n);
//						if (edge == null) {
//							edge = new ReferenceEdge(root, n, ReferenceEdge.CLASS_ACCESS);
//							graph.addEdge(edge, root, n);
//						}
//						edge.increaseCount();
//					}	
//				}
//			}
//		}
//	}
	
	
}