package alpha.rulp.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RRelationalOperator;
import alpha.rulp.lang.RType;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.runtime.IRIterator;

public class MatchTree {

	static class MLinkNode {

		public static MLinkNode build(MTreeNode fromNode, MTreeNode toNode, Set<String> stmtVarNames)
				throws RException {

			int type = _getType(fromNode.tree, toNode.tree);
			switch (type) {
			case LINK_L_L:
				break;

			case LINK_L_E:
			case LINK_L_V:
			case LINK_V_E:

				// no var in expr, for example: (fun1)
				if (toNode.varCount == 0) {
					return null;
				}

				for (String exprVar : toNode.varList) {

					// ignore external var
					if (!stmtVarNames.contains(exprVar)) {
						continue;
					}

					// unexpected vars found in expression
					if (!fromNode.varList.contains(exprVar)) {
						return null;
					}
				}

				break;

			case LINK_E_L:
			case LINK_E_V:
			case LINK_V_L:
				return build(toNode, fromNode, stmtVarNames);

			case LINK_E_E:
			case LINK_V_V:
				return null;
			}

			MLinkNode link = new MLinkNode();

			link.fromNode = fromNode;
			link.toNode = toNode;

			if (type == LINK_L_E) {
				link.varList = fromNode.varList;
			} else {
				link.varList = _retainAll(fromNode.varList, toNode.varList);
			}

			link.commonVarSize = link.varList.size();
			link.maxVarSize = Math.max(fromNode.varList.size(), toNode.varList.size());
			link.maxLevel = Math.max(fromNode.level, toNode.level);
			link.fromNode.linkList.add(link);
			link.toNode.linkList.add(link);

			return link;
		}

		static int compare(MLinkNode l1, MLinkNode l2) throws RException {

			int d = l1.getType() - l2.getType();

			if (d == 0) {
				d = l2.maxVarSize - l1.maxVarSize;
			}

			if (d == 0) {
				d = l1.commonVarSize - l2.commonVarSize;
			}

			if (d == 0) {
				d = l1.maxLevel - l2.maxLevel;
			}

			return -d;
		}

		public int _PossibleReteLevel = -1;

		public int _type = -1;

		public int commonVarSize;

		public MTreeNode fromNode;

		public int maxLevel;

		public int maxVarSize;

		public MTreeNode toNode;

		public List<String> varList;

		public int getPossibleReteLevel() throws RException {

			if (_PossibleReteLevel == -1) {
				_PossibleReteLevel = fromNode.getPossibleReteLevel() * toNode.getPossibleReteLevel();
			}

			return _PossibleReteLevel;
		}

		public int getType() throws RException {

			if (_type == -1) {
				_type = _getType(fromNode.tree, toNode.tree);
			}

			return _type;
		}

		public boolean isInvalidLink() throws RException {

			if (varList.isEmpty()) {
				return true;
			}

			if (fromNode.tree == null || toNode.tree == null) {
				return true;
			}

			// (EXPR, EXPR)
			switch (getType()) {
			case LINK_E_E:
				return true;
			}

			return false;
		}

		public String toString() {
			return fromNode.toString() + " --> " + toNode.toString();
		}
	}

	static class MTreeNode {

		public static MTreeNode buildBete3(MTreeNode n1, MTreeNode n2, MTreeNode n3) throws RException {

			if (compare(n1, n2) > 0) {
				return buildBete3(n2, n1, n3);
			}

			MTreeNode node = new MTreeNode();

			node.tree = RulpFactory.createList(n1.tree, n2.tree, n3.tree);
			node.varList = _unionAll(n1.varList, n2.varList);
			node.varCount = node.varList.size();
			node.level = Math.max(Math.max(n1.level, n2.level), n3.level) + 1;
			n1.tree = null;
			n2.tree = null;
			n3.tree = null;
			return node;
		}

		public static MTreeNode buildBetex(List<MTreeNode> nodeList, MTreeNode exprNode) throws RException {

			Collections.sort(nodeList, (n1, n2) -> {
				return MTreeNode.compare(n1, n2);
			});

			MTreeNode node = new MTreeNode();
			List<String> varList = new ArrayList<>();
			int level = exprNode.level;

			ArrayList<IRList> list = new ArrayList<>();
			for (MTreeNode tn : nodeList) {
				list.add(tn.tree);
				varList = _unionAll(varList, tn.varList);
				level = Math.max(level, tn.level);
				tn.tree = null;
			}

			list.add(exprNode.tree);
			exprNode.tree = null;

			node.tree = RulpFactory.createList(list);
			node.varList = _unionAll(varList, exprNode.varList);
			node.varCount = node.varList.size();
			node.level = level + 1;
			return node;
		}

		static MTreeNode buildConst(MTreeNode n1, IRList constStmt) throws RException {

			MTreeNode node = new MTreeNode();

			node.tree = RulpFactory.createList(n1.tree, constStmt);
			node.varList = n1.varList;
			node.varCount = node.varList.size();
			node.level = n1.level + 1;
			n1.tree = null;

			return node;
		}

		static MTreeNode buildNode(IRList stmt) throws RException {

			MTreeNode node = new MTreeNode();
			node.tree = stmt;
			node.level = 0;
			node.varList = ReteUtil.varList(stmt);
			node.varCount = node.varList.size();
			return node;
		}

		static MTreeNode buildNode(MTreeNode n1, MTreeNode n2) throws RException {

			if (compare(n1, n2) > 0) {
				return buildNode(n2, n1);
			}

			MTreeNode node = new MTreeNode();

			RRelationalOperator op = null;

			if (n1.level == 0 && n1.tree.getType() == RType.LIST && n2.level == 0 && n2.tree.getType() == RType.EXPR
					&& n2.tree.size() == 3 && (op = RulpUtil.toRelationalOperator(n2.tree.get(0).asString())) != null) {

				List<String> list1 = RulpUtil.toStringList(n1.tree);

				int idx1 = list1.indexOf(n2.tree.get(1).asString());
				int idx2 = list1.indexOf(n2.tree.get(2).asString());

				// '(?a ?b ?c) (!= ?c ?b) ==> '(?a ?b ?c) (!= ?b ?c)
				// '(?a ?b ?c) (> 1 ?b) ==> '(?a ?b ?c) (<= ?b 1)
				// '(?a ?b ?c) (< ?x ?b) ==> '(?a ?b ?c) (> ?b ?x)
				if ((idx1 == -1 && idx2 >= 0) || (idx1 >= 0 && idx2 >= 0 && idx1 > idx2)) {
					n2.tree = RulpFactory.createExpression(RRelationalOperator.oppositeOf(op).getAtom(), n2.tree.get(2),
							n2.tree.get(1));
				} else {
					n2.tree = RulpFactory.createExpression(op.getAtom(), n2.tree.get(1), n2.tree.get(2));
				}
			}

			if (n1.tree.getType() == RType.EXPR) {
				node.tree = RulpFactory.createExpression(n1.tree, n2.tree);
			} else {
				node.tree = RulpFactory.createList(n1.tree, n2.tree);
			}

			node.varList = _unionAll(n1.varList, n2.varList);
			node.varCount = node.varList.size();
			node.level = Math.max(n1.level, n2.level) + 1;
			n1.tree = null;
			n2.tree = null;

			return node;
		}

		static int compare(MTreeNode n1, MTreeNode n2) {

			try {

				switch (_getType(n1.tree, n2.tree)) {
				case LINK_V_E:
					return -1;

				case LINK_E_V:
					return 1;

				case LINK_L_V:
					return -1;

				case LINK_V_L:
					return 1;

				case LINK_L_E:
					return -1;

				case LINK_E_L:
					return 1;

				default:
					break;
				}

			} catch (RException e) {
				e.printStackTrace();
				return 0;
			}

			int d = n1.level - n2.level;

			if (d == 0) {
				d = n1.varCount - n2.varCount;
			}

			if (d == 0) {
				return n1.tree.toString().compareTo(n2.tree.toString());
			}

			return -d;
		}

		public int _PossibleReteLevel = -1;

		public int level;

		public List<MLinkNode> linkList = new LinkedList<>();

		public IRList tree;

		public int varCount;

		public List<String> varList = new LinkedList<>();

		public int getPossibleReteLevel() throws RException {

			// Root (?x ?y ?z): 1
			// Alpha (?x ?y c) : 2
			// Alpha (?x c1 c2) : 3
			// Other (?x c1 c2) : 4

			if (_PossibleReteLevel == -1) {

				if (ReteUtil.isAlphaMatchTree(tree)) {
					_PossibleReteLevel = 4 - varCount;
				} else {
					_PossibleReteLevel = 4;
				}
			}

			return _PossibleReteLevel;
		}

		public String toString() {
			return tree == null ? "null" : tree.toString();
		}
	}

	static final int LINK_E_E = 3;

	static final int LINK_E_L = 5;

	static final int LINK_E_V = 4;

	static final int LINK_L_E = 7;

	static final int LINK_L_L = 8;

	static final int LINK_L_V = 6;

	static final int LINK_V_E = 1;

	static final int LINK_V_L = 2;

	static final int LINK_V_V = 0;

	static final int LINK_XTYPE[][] = {
			// LIST_L(0), LIST_E(1), LIST_V(2)
			{ LINK_L_L, LINK_L_E, LINK_L_V }, // LIST_L
			{ LINK_E_L, LINK_E_E, LINK_E_V }, // LIST_E
			{ LINK_V_L, LINK_V_E, LINK_V_V }, // LIST_V
	};

	static final int LIST_EXPR = 1; // expression

	static final int LIST_LIST = 0; // List

	static final int LIST_VAR = 2; // var expression

	private static IRList _buildMTree(ArrayList<MTreeNode> nodeList, Set<String> stmtVarNames) throws RException {

		/**************************************************/
		// Loop until all nodes are linked
		/**************************************************/
		MATCH: while (true) {

			// link 2 nodes
			if (nodeList.size() > 1) {

				ArrayList<MLinkNode> linkList = new ArrayList<>();
				int nodeSize = nodeList.size();
				for (int i = 0; i < nodeSize; ++i) {
					for (int j = i + 1; j < nodeSize; ++j) {
						linkList.add(MLinkNode.build(nodeList.get(i), nodeList.get(j), stmtVarNames));
					}
				}

				_cleanLinkNode(linkList);
				_cleanTreeNode(nodeList);

				int update = 0;

				while (!linkList.isEmpty()) {

					MLinkNode link = linkList.remove(linkList.size() - 1);
					MTreeNode newNode = MTreeNode.buildNode(link.fromNode, link.toNode);

					for (MTreeNode oldNode : nodeList) {
						if (oldNode != link.fromNode && oldNode != link.toNode && oldNode.tree != null) {
							linkList.add(MLinkNode.build(oldNode, newNode, stmtVarNames));
						}
					}

					nodeList.add(newNode);
					++update;

					_cleanLinkNode(linkList);
					_cleanTreeNode(nodeList);
				}

				if (update > 0) {
					continue MATCH;
				}
			}

			// There must be multi nodes here
			ArrayList<MTreeNode> treeList = new ArrayList<>();
			ArrayList<MTreeNode> unLinkExprList = new ArrayList<>();
			ArrayList<MTreeNode> unLinkVarExprList = new ArrayList<>();

			for (MTreeNode node : nodeList) {

				if (node.tree == null) {
					continue;
				}

				if (node.tree.getType() == RType.LIST) {
					treeList.add(node);
					continue;
				}

				if (ReteUtil.isVarChangeExpr(node.tree)) {
					unLinkVarExprList.add(node);
					continue;
				}

				boolean hasStmtVar = false;

				for (String varName : node.varList) {
					if (stmtVarNames.contains(varName)) {
						hasStmtVar = true;
						break;
					}
				}

				// there is no stmt var, so that this is a pure external var expression
				if (!hasStmtVar) {
					unLinkVarExprList.add(node);
					continue;
				}

				unLinkExprList.add(node);
			}

			// link 3 nodes
			if (!unLinkExprList.isEmpty()) {

				/********************************************/
				// TreeA(?x1 ?y1 ?z1) TreeB(?x2 ?y2 ?z2) ExprC(fun ?x3 ?y3)
				// - TreeA and TreeB has no common variables
				// - TreeA and ExprC has common variables
				// - TreeB and ExprC has common variables
				// Output:
				// BetaX (TreeA, TreeB)
				// (BetaX ExprC)
				/********************************************/
				if (treeList.size() >= 2) {

					for (MTreeNode unLinkExprNode : unLinkExprList) {

						ArrayList<MTreeNode> commonVarNodeList = new ArrayList<>();
						for (MTreeNode node : treeList) {
							// has common variables
							if (!_retainAll(unLinkExprNode.varList, node.varList).isEmpty()) {
								commonVarNodeList.add(node);
							}
						}

						List<MTreeNode> findList = new ArrayList<>();

						if (_findMatchNodeList(commonVarNodeList, 0, findList, unLinkExprNode.varList,
								new ArrayList<>())) {

							if (findList.size() == 2) {
								nodeList.add(MTreeNode.buildBete3(findList.get(0), findList.get(1), unLinkExprNode));
								_cleanTreeNode(nodeList);
								continue MATCH;

							} else if (findList.size() > 2) {

								nodeList.add(MTreeNode.buildBetex(findList, unLinkExprNode));
								_cleanTreeNode(nodeList);
								continue MATCH;

							}
						}

//						int commonSize = commonVarNodeList.size();
//						if (commonSize >= 2) {
//
//							for (int i = 0; i < commonSize - 1; ++i) {
//
//								MTreeNode aNode = commonVarNodeList.get(i);
//
//								for (int j = i + 1; j < commonSize; ++j) {
//
//									MTreeNode bNode = commonVarNodeList.get(j);
//
//									List<String> allVars = _unionAll(aNode.varList, bNode.varList);
//									if (allVars.containsAll(unLinkExprNode.varList)) {
//
//										nodeList.add(MTreeNode.buildBete3(aNode, bNode, unLinkExprNode));
//										_cleanTreeNode(nodeList);
//										continue MATCH;
//									}
//								}
//							}
//						}

					}

				}

				throw new RException("unlink expression: " + unLinkExprList);
			}

			IRList tree = null;
			if (treeList.size() == 1) {
				tree = treeList.get(0).tree;

			} else if (treeList.size() >= 2) {
//				tree = RulpFactory.createList(treeList.get(0).tree, treeList.get(1).tree);
//
//			} else {

				ArrayList<IRList> list = new ArrayList<>();
				for (MTreeNode tn : treeList) {
					list.add(tn.tree);
				}

				tree = RulpFactory.createList(list);

//				throw new RException("too many tree node: " + treeList);
			}

			if (!unLinkVarExprList.isEmpty()) {
				for (int i = 0; i < unLinkVarExprList.size(); ++i) {

					if (tree == null) {
						tree = unLinkVarExprList.get(i).tree;
					} else {
						tree = RulpFactory.createList(tree, unLinkVarExprList.get(i).tree);
					}
				}
			}

			// No beta join node here
			return tree;
		}
	}

	static void _cleanLinkNode(ArrayList<MLinkNode> linkList) throws RException {

		Iterator<MLinkNode> it = linkList.iterator();
		while (it.hasNext()) {
			MLinkNode link = it.next();
			if (link == null || link.isInvalidLink()) {
				it.remove();
			}
		}

		Collections.sort(linkList, (l1, l2) -> {
			try {
				return MLinkNode.compare(l1, l2);
			} catch (RException e) {
				e.printStackTrace();
				throw new RuntimeException(e.toString());
			}
		});
	}

	static void _cleanTreeNode(ArrayList<MTreeNode> nodeList) throws RException {

		Iterator<MTreeNode> it = nodeList.iterator();
		while (it.hasNext()) {
			MTreeNode node = it.next();
			if (node.tree == null) {
				it.remove();
			}
		}

		Collections.sort(nodeList, (n1, n2) -> {
			return MTreeNode.compare(n1, n2);
		});
	}

	static boolean _findMatchNodeList(List<MTreeNode> nodeList, int curIndex, List<MTreeNode> findList,
			List<String> allVars, List<String> matchedVars) {

		if (curIndex >= nodeList.size()) {
			return false;
		}

		MTreeNode node = nodeList.get(curIndex);
		if (!hasCommonElement(node.varList, allVars)) {
			return _findMatchNodeList(nodeList, curIndex + 1, findList, allVars, matchedVars);
		}

		matchedVars = _unionAll(matchedVars, node.varList);
		findList.add(node);
		if (matchedVars.containsAll(allVars)) {
			return true;
		}

		return _findMatchNodeList(nodeList, curIndex + 1, findList, allVars, matchedVars);
	}

	private static ArrayList<String> _getAtomConstNames(IRList stmt) throws RException {

		ArrayList<String> atomNames = new ArrayList<>();
		int atomNum = 0;

		IRIterator<? extends IRObject> iter = stmt.iterator();
		while (iter.hasNext()) {

			IRObject obj = iter.next();
			if (obj.getType() == RType.ATOM) {
				String atomName = RulpUtil.asAtom(obj).getName();
				if (!RulpUtil.isVarName(atomName)) {
					atomNames.add(atomName);
					++atomNum;
					continue;
				}
			}

			atomNames.add(null);
		}

		return atomNum == 0 ? null : atomNames;
	}

	static int _getType(IRList t1) throws RException {

		if (t1.getType() == RType.LIST) {
			return LIST_LIST;

		} else if (ReteUtil.isVarChangeExpr(t1)) {
			return LIST_VAR;

		} else {
			return LIST_EXPR;
		}
	}

	static int _getType(IRList t1, IRList t2) throws RException {
		return LINK_XTYPE[_getType(t1)][_getType(t2)];
	}

	static List<String> _retainAll(List<String> a, List<String> b) {

		HashSet<String> s = new HashSet<>(a);
		s.retainAll(b);

		List<String> c = new LinkedList<>(s);
		Collections.sort(c);

		return c;
	}

	static List<String> _unionAll(List<String> a, List<String> b) {

		HashSet<String> s = new HashSet<>(a);
		s.addAll(b);

		List<String> c = new LinkedList<>(s);
		Collections.sort(c);

		return c;
	}

	public static IRList build(List<IRList> matchStmtList, IRInterpreter interpreter, IRFrame frame) throws RException {

		/**************************************************/
		// Create const/var stmts
		/**************************************************/
		ArrayList<IRList> constStmtList = new ArrayList<>();
		ArrayList<IRList> varStmtList = new ArrayList<>();
		Set<String> stmtVarNames = new HashSet<>();

		NEXT: for (IRList stmt : matchStmtList) {

			// const expr
			if (stmt.getType() == RType.LIST && ReteUtil.getStmtVarCount(stmt) == 0) {
				constStmtList.add(stmt);
				continue;
			}

			if (stmt.getType() == RType.EXPR) {

				IRObject rst = OptimizeUtil.optimizeExpr((IRExpr) stmt, interpreter, frame);

				switch (rst.getType()) {
				case EXPR:
					stmt = (IRList) rst;

					// Check (inherit '(?b p ?c) ?b)
					if (ReteUtil.isInheritExpr2(stmt)) {

						List<String> leftVars = ReteUtil.varList((IRList) stmt.get(1));

						IRIterator<? extends IRObject> it = stmt.listIterator(2);
						while (it.hasNext()) {

							String varName = RulpUtil.asAtom(it.next()).getName();
							if (!leftVars.contains(varName)) {
								throw new RException("invalid var<" + varName + "> in stmt: " + stmt);
							}
						}
					}

					break;

				case BOOL:
					if (RulpUtil.asBoolean(rst).asBoolean()) {
						continue NEXT;
					} else {
						throw new RException("false tree found: " + matchStmtList);
					}

				default:
					throw new RException("Invalid optimize result: " + rst);
				}
			}

			int anyIndex = ReteUtil.indexOfVarArgStmt(stmt);
			if (anyIndex != -1) {

				if (stmt.getNamedName() == null) {
					throw new RException(String.format("need named for any stmt: %s", stmt));
				}

				if (anyIndex != (stmt.size() - 1)) {
					throw new RException(String.format("invalid any stmt: %s", stmt));
				}

			}

			varStmtList.add(stmt);
			ArrayList<IRObject> stmtVars = ReteUtil.buildVarList(stmt);

			// Check index vars: ?0 ?1
			for (IRObject obj : stmtVars) {
				if (ReteUtil.isIndexVarName(RulpUtil.asAtom(obj).getName())) {
					throw new RException("Invalid index var<" + obj + "> found in: " + matchStmtList);
				}
			}

			// Get all stmt vars
			if (stmt.getType() == RType.LIST) {
				for (IRObject obj : stmtVars) {
					stmtVarNames.add(RulpUtil.asAtom(obj).getName());
				}
			}
		}

		Collections.sort(constStmtList, (s1, s2) -> {
			return s1.toString().compareTo(s2.toString());
		});

		/**************************************************/
		// build var tree
		/**************************************************/

		switch (varStmtList.size()) {
		case 0:
			throw new RException("no var stmt found: " + matchStmtList);

		case 1:
			IRList mTree = varStmtList.get(0);
			/**************************************************/
			// Add independent const Stmt
			/**************************************************/
			for (IRList constStmt : constStmtList) {
				mTree = RulpFactory.createList(mTree, constStmt);
			}

			return mTree;

		default:
			break;
		}

		/**************************************************/
		// Create all nodes
		/**************************************************/
		ArrayList<MTreeNode> nodeList = new ArrayList<>();

		for (IRList stmt : varStmtList) {
			nodeList.add(MTreeNode.buildNode(stmt));
		}

		/**************************************************/
		// Attach const stmt to nodes
		// for example: '(a b c) + '(a ?p ?y) ==> '('(a ?p ?y) '(a b c))
		/**************************************************/
		// Node, the node list match the order of varStmtList
		ArrayList<IRList> independentConstStmtList = new ArrayList<>();
		int nodeSize = varStmtList.size();

		for (IRList constStmt : constStmtList) {

			ArrayList<String> constAtomNames = _getAtomConstNames(constStmt);

			int attachIndex = -1;
			int maxMatchLevel = -1;

			for (int i = 0; i < nodeSize; ++i) {

				IRList varStmt = varStmtList.get(i);
				if (varStmt.getType() != RType.LIST) {
					continue;
				}

				ArrayList<String> varAtomNames = _getAtomConstNames(varStmt);

				// no const atom be found
				if (varAtomNames == null) {
					continue;
				}

				int matchLevel = 0;

				// find common names
				for (int j = 0; j < constAtomNames.size(); ++j) {

					String constName = constAtomNames.get(j);
					if (constName == null) {
						continue;
					}

					int k = varAtomNames.indexOf(constName);
					if (k == -1) {
						continue;
					}

					// the best match position
					if (k == j) {
						matchLevel += constAtomNames.size();
					} else {
						matchLevel += constAtomNames.size() - k;
					}
				}

				// no match found
				if (matchLevel == 0) {
					continue;
				}

				if (matchLevel > maxMatchLevel) {
					maxMatchLevel = matchLevel;
					attachIndex = i;
				}
			}

			// no attach found
			if (attachIndex == -1) {
				independentConstStmtList.add(constStmt);
				continue;
			}

			nodeList.set(attachIndex, MTreeNode.buildConst(nodeList.get(attachIndex), constStmt));
		}

		IRList mTree = _buildMTree(nodeList, stmtVarNames);

		/**************************************************/
		// Add independent const Stmt
		/**************************************************/
		for (IRList constStmt : independentConstStmtList) {
			mTree = RulpFactory.createList(mTree, constStmt);
		}

		return mTree;
	}

//	private static IRList _attachConstStmt(IRList varTree, IRList constStmt, Set<String> constAtomNames)
//			throws RException {
//
//		if (constAtomNames == null) {
//			constAtomNames = _getAtomConstNames(constStmt);
//		}
//
//		// only one var stmt
//		if (ReteUtility.isReteStmt(varTree)) {
//
//			if (!_hasCommonSet(_getAtomConstNames(varTree), constAtomNames)) {
//				return varTree;
//			}
//
//			return RulpFactory.createList(varTree, constStmt);
//		}
//
//		IRList attachChildTree = null;
//		int attachIndex = -1;
//
//		int size = varTree.size();
//		for (int i = 0; i < size; ++i) {
//
//			IRObject ex = varTree.get(i);
//			if (ex.getType() == RType.LIST) {
//
//				attachChildTree = _attachConstStmt((IRList) ex, constStmt, constAtomNames);
//
//				// attached
//				if (attachChildTree != ex) {
//					attachIndex = i;
//					break;
//				}
//			}
//		}
//
//		// no attach found
//		if (attachIndex == -1) {
//			return varTree;
//		}
//
//		/**************************************************/
//		// Create new tree
//		/**************************************************/
//		ArrayList<IRObject> newElements = new ArrayList<>();
//		for (int i = 0; i < size; ++i) {
//			if (i == attachIndex) {
//				newElements.add(attachChildTree);
//			} else {
//				newElements.add(varTree.get(i));
//			}
//		}
//
//		return RulpFactory.createList(newElements);
//	}

	public static <T> boolean hasCommonElement(Collection<T> a, Collection<T> b) {

		if (a == null || b == null || a.isEmpty() || b.isEmpty()) {
			return false;
		}

		if (a instanceof Set && !(b instanceof Set)) {
			Collection<T> c = a;
			a = b;
			b = c;
		}

		for (T x : a) {
			if (b.contains(x)) {
				return true;
			}
		}

		return false;
	}

}
