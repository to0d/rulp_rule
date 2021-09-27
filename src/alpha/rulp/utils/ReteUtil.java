package alpha.rulp.utils;

import static alpha.rulp.lang.Constant.A_NIL;
import static alpha.rulp.lang.Constant.O_Nil;
import static alpha.rulp.lang.Constant.S_QUESTION;
import static alpha.rulp.lang.Constant.S_QUESTION_C;
import static alpha.rulp.lang.Constant.S_QUESTION_LIST;
import static alpha.rulp.rule.Constant.*;
import static alpha.rulp.rule.RReteStatus.ASSUME;
import static alpha.rulp.rule.RReteStatus.DEFINE;
import static alpha.rulp.rule.RReteStatus.*;
import static alpha.rulp.rule.RReteStatus.REASON;
import static alpha.rulp.rule.RReteStatus.REMOVE;
import static alpha.rulp.rule.RReteStatus.TEMP__;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import alpha.rulp.lang.IRAtom;
import alpha.rulp.lang.IRBoolean;
import alpha.rulp.lang.IRDouble;
import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.IRFloat;
import alpha.rulp.lang.IRInteger;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRLong;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.IRString;
import alpha.rulp.lang.IRVar;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RType;
import alpha.rulp.rule.RReteStatus;
import alpha.rulp.runtime.IRFactor;
import alpha.rulp.runtime.IRFunction;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.ximpl.entry.IRReteEntry;
import alpha.rulp.ximpl.model.IRObjBuilder;
import alpha.rulp.ximpl.node.IRNamedNode;
import alpha.rulp.ximpl.node.IRNodeGraph;
import alpha.rulp.ximpl.node.IRReteNode;
import alpha.rulp.ximpl.node.IRReteNode.InheritIndex;
import alpha.rulp.ximpl.node.XTempVarBuilder;

public class ReteUtil {

	static IRObjBuilder nanBuilder = new IRObjBuilder() {

		@Override
		public IRObject build(IRObject obj) throws RException {
			return obj;
		}
	};

	static RReteStatus reteStatusConvertArray[][] = {
			// DEFINE,REASON, ASSUME, REMOVE, FIXED, TEMP
			{ DEFINE, DEFINE, DEFINE, REMOVE, FIXED_, DEFINE }, // DEFINE
			{ DEFINE, REASON, REASON, REMOVE, FIXED_, REASON }, // REASON
			{ DEFINE, REASON, ASSUME, REMOVE, FIXED_, ASSUME }, // ASSUME
			{ REMOVE, REMOVE, REMOVE, REMOVE, null, REMOVE }, // REMOVE
			{ FIXED_, FIXED_, FIXED_, null, FIXED_, FIXED_ }, // FIX
			{ DEFINE, REASON, ASSUME, REMOVE, FIXED_, TEMP__ }, // TEMP
	};

	static void _fillVarList(IRList stmt, Set<String> varSet) throws RException {

//		if (stmt.getType() == RType.EXPR) {

		IRIterator<? extends IRObject> iter = ((IRList) stmt).iterator();
		while (iter.hasNext()) {

			IRObject obj = iter.next();

			if (RulpUtil.isVarAtom(obj)) {
				varSet.add(RulpUtil.asAtom(obj).getName().trim());
				continue;
			}

			if (obj.getType() == RType.LIST || obj.getType() == RType.EXPR) {
				_fillVarList((IRList) obj, varSet);
			}
		}

//		}
//		// Must be List
//		else {
//
//			IRIterator<? extends IRObject> iter = ((IRList) stmt).iterator();
//			while (iter.hasNext()) {
//				IRObject obj = iter.next();
//				if (RulpUtil.isVarAtom(obj)) {
//					varSet.add(RulpUtil.asAtom(obj).getName().trim());
//				}
//			}
//		}
	}

	static int _getVarUniqIndex(int stmtLen, IRObject var) throws RException {

		String varName = var.asString().substring(1);
		if (!StringUtil.isNumber(varName)) {
			return -1;
		}

		int varIndex = Integer.valueOf(varName);
		if (varIndex < 0 || varIndex >= stmtLen) {
			return -1;
		}

		return varIndex;
	}

	private static String _toUniq(IRObject obj, Map<String, String> varMap, List<IRObject> varList) throws RException {

		if (obj == null) {
			return A_NIL;
		}

		switch (obj.getType()) {
		case NIL:
			return A_NIL;

		case ATOM:

			String atomName = ((IRAtom) obj).getName();
			if (RulpUtil.isVarName(atomName)) {
				String newName = varMap.get(atomName);
				if (newName == null) {
					newName = S_QUESTION + varMap.size();
					varMap.put(atomName, newName);

					if (varList != null) {
						varList.add(obj);
					}

				}

				atomName = newName;
			}

			return atomName;

		case VAR:
			return ((IRVar) obj).getName();

		case FACTOR:
			return ((IRFactor) obj).getName();

		case INT:
			return "" + ((IRInteger) obj).asInteger();

		case LONG:
			return "" + ((IRLong) obj).asLong() + "L";

		case FLOAT:
			return "" + ((IRFloat) obj).asFloat();

		case DOUBLE:
			return "" + ((IRDouble) obj).asDouble() + "D";

		case BOOL:
			return "" + ((IRBoolean) obj).asBoolean();

		case LIST:

			IRList list = (IRList) obj;
			String name = list.getNamedName();

			if (name == null)
				return "'(" + _toUniqIterator(list.iterator(), varMap, varList) + ")";
			else
				return name + ":'(" + _toUniqIterator(list.iterator(), varMap, varList) + ")";

		case EXPR:

			IRExpr expr = (IRExpr) obj;
			IRObject e0 = expr.get(0);

			if (ReteUtil.isVarChangeExpr(expr) && e0.getType() == RType.ATOM) {

				String name0 = RulpUtil.asAtom(e0).getName();

				switch (name0) {
				// (var-changed varName old-value new-value)
				// (var-changed varName new-value)
				case F_VAR_CHANGED:

					// e1 must be a var name
					String out = "(" + name0 + " " + RulpUtil.asAtom(expr.get(1)).getName();
					for (int i = 2; i < expr.size(); ++i) {
						out += " " + _toUniq(expr.get(i), varMap, varList);
					}

					out += ")";

					return out;

				default:
					throw new RException("not support: " + obj);
				}
			}

			return "(" + _toUniqIterator(((IRList) obj).iterator(), varMap, varList) + ")";

		case FUNC:
			return ((IRFunction) obj).getSignature();

		case STRING:
			return "\"" + ((IRString) obj).asString() + "\"";

		default:
			throw new RException("unsupport type: " + obj.getType());

		}
	}

	private static String _toUniq(IRObject[] entry, Map<String, String> varMap, List<IRObject> varList)
			throws RException {

		StringBuffer sb = new StringBuffer();
		int i = 0;
		for (IRObject e : entry) {
			if (i++ != 0) {
				sb.append(' ');
			}

			sb.append(_toUniq(e, varMap, varList));
		}

		return sb.toString();
	}

	private static String _toUniqIterator(IRIterator<? extends IRObject> iterator, Map<String, String> varMap,
			List<IRObject> varList) throws RException {

		StringBuffer sb = new StringBuffer();
		int i = 0;
		while (iterator.hasNext()) {
			if (i++ != 0) {
				sb.append(' ');
			}

			sb.append(_toUniq(iterator.next(), varMap, varList));
		}

		return sb.toString();
	}

	private static boolean _tryPutVarIndex(Map<String, Integer> indexMap, IRObject obj, int index) throws RException {

		if (obj == null) {
			return false;
		}

		if (!RulpUtil.isVarAtom(obj)) {
			return false;
		}

		String varName = RulpUtil.asAtom(obj).getName();
		if (indexMap.containsKey(varName)) {
			return false;
		}

		indexMap.put(varName, index);
		return true;
	}

	public static IRObject[] _varEntry(IRObject[] oldEntry) {

		IRObject[] newEntry = new IRObject[oldEntry.length];
		for (int i = 0; i < oldEntry.length; ++i) {
			IRObject obj = oldEntry[i];
			if (obj != null && RulpUtil.isVarAtom(obj)) {
				newEntry[i] = obj;
			}
		}

		return newEntry;
	}

	public static IRObject[] _varEntry(List<IRObject> vars) {

		IRObject[] objEntry = new IRObject[vars.size()];
		for (int i = 0; i < vars.size(); ++i) {
			objEntry[i] = vars.get(i);
		}

		return _varEntry(objEntry);
	}

	public static IRList asReteStmt(IRObject obj) throws RException {

		if (obj.getType() != RType.EXPR && !isReteTree(obj)) {
			throw new RException("Can't convert to stmt: " + obj);
		}

		return (IRList) obj;
	}

	public static ArrayList<IRObject> buildTreeVarList(IRList reteTree) throws RException {
		return ReteUtil.buildTreeVarList(reteTree, new HashMap<>());
	}

	public static ArrayList<IRObject> buildTreeVarList(IRList reteTree, Map<String, Integer> indexMap)
			throws RException {

		if (ReteUtil.isVarChangeExpr(reteTree)) {

			IRObject e0 = reteTree.get(0);

			if (reteTree.get(0).getType() == RType.EXPR) {
				return buildTreeVarList((IRList) e0, indexMap);
			}

			switch (RulpUtil.asAtom(e0).getName()) {

			// (var-changed ?varName old-value new-value)
			case F_VAR_CHANGED:

				ArrayList<IRObject> vars = new ArrayList<>();
				for (int i = 1; i < reteTree.size(); ++i) {

					IRObject ei = reteTree.get(i);

					if (_tryPutVarIndex(indexMap, ei, i - 1)) {
						vars.add(ei);
					} else {
						vars.add(null);
					}
				}

				return vars;

			default:
				throw new RException("factor not support: " + e0);
			}
		}

		// Alpha node
		if (ReteUtil.isReteStmt(reteTree)) {

			ArrayList<IRObject> objs = new ArrayList<>();
			for (int i = 0; i < reteTree.size(); ++i) {

				IRObject obj = reteTree.get(i);

				if (_tryPutVarIndex(indexMap, obj, i)) {
					objs.add(obj);
				} else {
					objs.add(null);
				}
			}

			return objs;
		}

		// Expr node
		if (reteTree.size() == 2 && reteTree.get(0).getType() == RType.LIST
				&& reteTree.get(1).getType() == RType.EXPR) {

			IRExpr expr1 = (IRExpr) reteTree.get(1);

			// (var-changed ?varName old-value new-value) ==> this should be beta node
			if (ReteUtil.isVarChangeExpr(expr1)) {

				ArrayList<IRObject> vars = new ArrayList<>();
				for (IRObject v : buildTreeVarList((IRList) reteTree.get(0), new HashMap<>())) {
					if (_tryPutVarIndex(indexMap, v, vars.size())) {
						vars.add(v);
					}
				}

				for (IRObject v : buildTreeVarList(expr1, new HashMap<>())) {
					if (_tryPutVarIndex(indexMap, v, vars.size())) {
						vars.add(v);
					}
				}

				return vars;

			}
			// '(?a ?b ?c) (factor ?x) ==> this should be expr node
			else {

				ArrayList<IRObject> vars = buildTreeVarList((IRList) reteTree.get(0), indexMap);

				for (IRObject v : ReteUtil.buildVarList(expr1)) {
					if (_tryPutVarIndex(indexMap, v, vars.size())) {
						vars.add(v);
					}
				}

				return vars;
			}
		}

		// Beta node
		if (isBetaTree(reteTree, reteTree.size())) {

			ArrayList<IRObject> vars = new ArrayList<>();
			for (IRObject v : buildTreeVarList((IRList) reteTree.get(0), new HashMap<>())) {
				if (_tryPutVarIndex(indexMap, v, vars.size())) {
					vars.add(v);
				}
			}

			for (IRObject v : buildTreeVarList((IRList) reteTree.get(1), new HashMap<>())) {
				if (_tryPutVarIndex(indexMap, v, vars.size())) {
					vars.add(v);
				}
			}

			return vars;
		}

		// beta3: '(?a b c) '(?x y z) (not-equal ?a ?x)
		if (isBeta3Tree(reteTree, reteTree.size())) {

			ArrayList<IRObject> vars = new ArrayList<>();
			for (IRObject v : buildTreeVarList((IRList) reteTree.get(0), new HashMap<>())) {
				if (_tryPutVarIndex(indexMap, v, vars.size())) {
					vars.add(v);
				}
			}

			for (IRObject v : buildTreeVarList((IRList) reteTree.get(1), new HashMap<>())) {
				if (_tryPutVarIndex(indexMap, v, vars.size())) {
					vars.add(v);
				}
			}

			for (IRObject v : buildVarList((IRExpr) reteTree.get(2))) {
				if (v != null && !indexMap.containsKey(RulpUtil.asAtom(v).getName())) {
					throw new RException("Invalid node3 found: " + reteTree);
				}
			}

			return vars;
		}

		// beta: '((var-changed ?x ?xv) (var-changed ?y ?yv))
		if (reteTree.size() == 2 && ReteUtil.isVarChangeExpr(reteTree.get(0))
				&& ReteUtil.isVarChangeExpr(reteTree.get(1))) {

			ArrayList<IRObject> vars = new ArrayList<>();
			for (IRObject v : buildTreeVarList((IRList) reteTree.get(0), new HashMap<>())) {
				if (_tryPutVarIndex(indexMap, v, vars.size())) {
					vars.add(v);
				}
			}

			for (IRObject v : buildTreeVarList((IRList) reteTree.get(1), new HashMap<>())) {
				if (_tryPutVarIndex(indexMap, v, vars.size())) {
					vars.add(v);
				}
			}

			return vars;
		}

		int ModifierCount = ReteUtil.getReteTreeModifierCount(reteTree);
		if (ModifierCount > 0) {

			ArrayList<IRObject> vars = new ArrayList<>();

			for (int index = reteTree.size() - ModifierCount; index > 0; --index) {
				for (IRObject v : buildTreeVarList((IRList) reteTree.get(index - 1), new HashMap<>())) {
					if (_tryPutVarIndex(indexMap, v, vars.size())) {
						vars.add(v);
					}
				}
			}

			return vars;
		}

		throw new RException("Invalid tree node found: " + reteTree);
	}

	public static boolean isBetaTree(IRList reteTree, int treeSize) throws RException {
		return treeSize == 2 && reteTree.getType() == RType.LIST && reteTree.get(0).getType() == RType.LIST
				&& reteTree.get(1).getType() == RType.LIST;
	}

	public static boolean isBeta3Tree(IRList reteTree, int treeSize) throws RException {
		return treeSize == 3 && reteTree.get(0).getType() == RType.LIST && reteTree.get(1).getType() == RType.LIST
				&& reteTree.get(2).getType() == RType.EXPR;
	}

	public static ArrayList<IRObject> buildVarList(IRList reteTree) throws RException {
		ArrayList<IRObject> varList = new ArrayList<>();
		buildVarList(reteTree, varList, new HashSet<String>());
		return varList;
	}

	public static void buildVarList(IRObject obj, List<IRObject> varList, Set<String> uniqNames) throws RException {

		if (obj == null) {
			return;
		}

		switch (obj.getType()) {
		case NIL:
		case INT:
		case FLOAT:
		case LONG:
		case BOOL:
		case FACTOR:
		case STRING:
		case FUNC:
			break;

		case ATOM:

			String atomName = ((IRAtom) obj).getName();
			if (RulpUtil.isVarName(atomName) && !uniqNames.contains(atomName)) {
				varList.add(obj);
				uniqNames.add(atomName);
			}

			break;

		case LIST:
		case EXPR:
			IRIterator<? extends IRObject> iter = ((IRList) obj).iterator();
			while (iter.hasNext()) {
				buildVarList(iter.next(), varList, uniqNames);
			}

			break;

		default:

			throw new RException("unsupport type: " + obj.getType());
		}
	}

	public static ArrayList<IRObject> buildVarList(List<? extends IRList> treeList) throws RException {

		ArrayList<IRObject> varList = new ArrayList<>();
		for (IRList reteTree : treeList) {
			buildVarList(reteTree, varList, new HashSet<String>());
		}

		return varList;
	}

	public static boolean equal(IRObject a, IRObject b) {

		if (a == null || a == O_Nil) {
			return b == null || b == O_Nil;
		}

		if (b == null || b == O_Nil) {
			return false;
		}

		RType type = a.getType();
		if (type != b.getType()) {
			return false;
		}

		switch (type) {
		case INT:
			return ((IRInteger) a).asInteger() == ((IRInteger) b).asInteger();

		case FLOAT:
			return ((IRFloat) a).asFloat() == ((IRFloat) b).asFloat();

		case STRING:
			return ((IRString) a).asString().equals(((IRString) b).asString());

		case BOOL:
			return ((IRBoolean) a).asBoolean() == ((IRBoolean) b).asBoolean();

		case ATOM:
			return ((IRAtom) a).getName().equals(((IRAtom) b).getName());

		default:
			return false;
		}
	}

	public static IRNamedNode findNameNode(IRNodeGraph graph, IRList filter) throws RException {

		String namedName = filter.getNamedName();
		if (namedName == null) {
			throw new RException("Invalid named filter: " + filter);
		}

		int nodeEntryLengh = getFilerEntryLength(filter);
		if (!ReteUtil.isValidStmtLen(nodeEntryLengh)) {
			throw new RException("Invalid entry length: " + filter);
		}

		/******************************************************/
		// n:'(?...) or n:'(?x ?...)
		/******************************************************/
		int varArgIndex = ReteUtil.indexOfVarArgStmt(filter);
		if (varArgIndex != -1 && varArgIndex != (nodeEntryLengh - 1)) {
			throw new RException(String.format("invalid named filter: %s", filter));
		}

		/**************************************************/
		// Check entry length
		/**************************************************/
		IRNamedNode namedNode = graph.findNamedNode(namedName);
		if (namedNode != null) {

			if (varArgIndex == -1) {
				if (namedNode.getEntryLength() != nodeEntryLengh) {
					throw new RException(String.format("unmatch entry length: expect=%d, actual=%d", nodeEntryLengh,
							namedNode.getEntryLength()));
				}
			} else {

				if (varArgIndex > namedNode.getEntryLength()) {
					throw new RException(String.format("unmatch vararg entry length: vararg=%d, actual=%d", varArgIndex,
							namedNode.getEntryLength()));
				}
			}

		}

		return namedNode;
	}

	public static RReteStatus getChildStatus(IRReteEntry... parents) throws RException {

//		RReteStatus status = 

		if (parents == null) {
			return DEFINE;
		}

		int fixCount = 0;

		for (IRReteEntry parent : parents) {

			if (parent == null || parent.isDroped()) {
				throw new RException("Invalid parent entry: " + parent);
			}

			switch (parent.getStatus()) {
			case ASSUME:
				return ASSUME;

			case TEMP__:
				return DEFINE;

			case DEFINE:
			case REASON:
				return REASON;

			case FIXED_:
				++fixCount;
				break;

			default:
				throw new RException("Invalid parent entry: " + parent);
			}
		}

		return fixCount > 0 ? FIXED_ : DEFINE;
	}

	public static int getFilerEntryLength(IRList filter) throws RException {

		int nodeEntryLengh = -1;

		/**************************************************/
		// name:'(3) format
		/**************************************************/
		if (filter.size() == 1 && filter.get(0).getType() == RType.INT) {

			nodeEntryLengh = RulpUtil.asInteger(filter.get(0)).asInteger();

		} else {

			/******************************************************/
			// Check var name
			/******************************************************/
			Set<String> varSet = new HashSet<>();
			for (int i = 0; i < filter.size(); ++i) {

				IRObject obj = filter.get(i);
				String varName = obj.asString();
				if (obj.getType() != RType.ATOM || varName.charAt(0) != S_QUESTION_C) {
					throw new RException(String.format("Invalid obj<%s> in filter: %s", obj, filter));
				}

				switch (varName) {
				case S_QUESTION:
				case S_QUESTION_LIST:
					break;
				default:
					if (varSet.contains(varName)) {
						throw new RException(String.format("duplicate name<%s> in filter: %s", varName, filter));
					}

					varSet.add(varName);
				}
			}

			nodeEntryLengh = filter.size();
		}

		return nodeEntryLengh;
	}

	public static int getMainInheritIndex(InheritIndex inheritIndexs[]) {

		int mainInheritIndex = -2;

		for (int i = 0; mainInheritIndex != -1 && i < inheritIndexs.length; ++i) {
			int parentIndex = inheritIndexs[i].parentIndex;
			if (parentIndex != mainInheritIndex) {
				if (mainInheritIndex == -2) {
					mainInheritIndex = parentIndex;
				} else {
					mainInheritIndex = -1;
					break;
				}
			}
		}

		return mainInheritIndex;
	}

	public static String getNamedUniqName(String name, int stmtLen) {
		return name + ":" + ReteUtil.getRootUniqName(stmtLen);
	}

	public static String getNodeName(IRReteNode node) {

		switch (node.getReteType()) {
		case ALPH0:
			return String.format("A0%03d", node.getNodeId());

		case ALPH1:
			return String.format("A1%03d", node.getNodeId());

		case ALPH2:
			return String.format("A2%03d", node.getNodeId());

		case BETA0:
			return String.format("B0%03d", node.getNodeId());

		case BETA1:
			return String.format("B1%03d", node.getNodeId());

		case BETA2:
			return String.format("B2%03d", node.getNodeId());

		case BETA3:
			return String.format("B3%03d", node.getNodeId());

		case EXPR0:
			return String.format("E0%03d", node.getNodeId());

		case EXPR1:
			return String.format("E1%03d", node.getNodeId());

		case EXPR2:
			return String.format("E2%03d", node.getNodeId());

		case EXPR3:
			return String.format("E3%03d", node.getNodeId());

		case EXPR4:
			return String.format("E4%03d", node.getNodeId());

		case ROOT0:
			return String.format("R0%03d", node.getNodeId());

		case NAME0:
			return String.format("N0%03d", node.getNodeId());

		case VAR:
			return String.format("V%04d", node.getNodeId());

		case WORK:
			return String.format("W%04d", node.getNodeId());

		case RULE:
			return String.format("U%04d", node.getNodeId());

		case CONST:
			return String.format("C%04d", node.getNodeId());

		case DROP:
			return String.format("D1%03d", node.getNodeId());

		default:
			return String.format("X%04d", node.getNodeId());
		}
	}

	public static RReteStatus getReteStatus(RReteStatus fromStatus, RReteStatus toStatus) {
		return reteStatusConvertArray[fromStatus.getIndex()][toStatus.getIndex()];
	}

	public static String getRootUniqName(int stmtLen) {

		String uniqName = "'(";
		for (int i = 0; i < stmtLen; ++i) {
			if (i != 0) {
				uniqName += " ";
			}
			uniqName += S_QUESTION + i;
		}
		uniqName += ")";

		return uniqName;
	}

	public static int getStmtVarCount(IRList stmt) throws RException {

		int varCount = 0;

		/********************************************/
		// Check "?x" exist
		/********************************************/
		for (int i = 0; i < stmt.size(); ++i) {

			IRObject obj = stmt.get(i);
			switch (obj.getType()) {
			case ATOM:
				String objName = obj.asString().trim();
				if (RulpUtil.isVarName(objName)) {
					varCount++;
				}
				break;
			case LIST:
			case EXPR:
				varCount += getStmtVarCount((IRList) obj);
				break;

			default:
				break;

			}
		}

		return varCount;
	}

	public static int indexOfVarArgStmt(IRList stmt) throws RException {

		if (!isReteStmt(stmt)) {
			return -1;
		}

		IRIterator<? extends IRObject> iter = stmt.iterator();
		for (int stmtIndex = 0; iter.hasNext(); stmtIndex++) {
			if (isVarArg(iter.next())) {
				return stmtIndex;
			}
		}

		return -1;
	}

	public static boolean isAlphaMatchTree(IRList matchTree) throws RException {
		return isValidStmtLen(matchTree.size()) && ReteUtil.isEntryValueType(matchTree.get(0).getType());
	}

	public static boolean isAnyVar(IRObject obj) {
		return obj.getType() == RType.ATOM && ((IRAtom) obj).getName().equals(S_QUESTION);
	}

	public static boolean isCond(IRList cond) throws RException {

		if (!ReteUtil.isReteStmt(cond)) {
			return false;
		}

		IRIterator<? extends IRObject> iter = cond.iterator();
		while (iter.hasNext()) {
			if (iter.next().asString().equals(S_QUESTION)) {
				return false;
			}
		}

		return true;
	}

	public static boolean isCondList(IRList condList) throws RException {

		IRIterator<? extends IRObject> condIter = condList.iterator();
		while (condIter.hasNext()) {

			IRObject cond = condIter.next();

			if (ReteUtil.isReteStmt(cond) || ReteUtil.isReteTree(cond) || RulpUtil.isExpression(cond)) {
				continue;
			}

			return false;
		}

		return true;
	}

	public static boolean isEntryValueType(RType type) throws RException {

		switch (type) {
		case INT:
		case ATOM:
		case FLOAT:
		case STRING:
		case LONG:
		case DOUBLE:
		case BOOL:
			return true;
		default:
			return false;
		}
	}

	public static boolean isIndexVarName(String var) {

		if (var.length() <= 1 || var.charAt(0) != S_QUESTION_C) {
			return false;
		}

		return StringUtil.isNumber(var.substring(1));
	}

	public static boolean isIndexVarStmt(IRList stmt) throws RException {

		if (stmt.getType() == RType.EXPR || isReteStmt(stmt)) {
			for (IRObject var : ReteUtil.buildVarList(stmt)) {
				if (ReteUtil.isIndexVarName(RulpUtil.asAtom(var).getName())) {
					return true;
				}
			}
		}

		return false;
	}

	public static boolean isReteStmt(IRList stmt) throws RException {

		if (!isValidStmtLen(stmt.size())) {
			return false;
		}

		int index = 0;
		IRIterator<? extends IRObject> iter = stmt.iterator();

		while (iter.hasNext()) {

			++index;

			IRObject obj = iter.next();
			RType type = obj.getType();

			switch (type) {
			case NIL:
			case INT:
			case FLOAT:
			case STRING:
			case LONG:
			case DOUBLE:
			case BOOL:
				if (stmt.getNamedName() == null && index <= 2) {
					return false;
				}
				break;

			case ATOM:
			case FUNC:
				break;

			default:
				return false;
			}

		}

		return true;
	}

	public static boolean isReteStmt(IRObject obj) throws RException {
		return obj.getType() == RType.LIST && isReteStmt((IRList) obj);
	}

	public static boolean isReteStmtNoVar(IRList stmt) throws RException {

		if (!isValidStmtLen(stmt.size())) {
			return false;
		}

		boolean isNamedStmt = stmt.getNamedName() != null;

		int index = 0;
		IRIterator<? extends IRObject> iter = stmt.iterator();
		while (iter.hasNext()) {

			++index;

			IRObject obj = iter.next();
			RType type = obj == null ? RType.NIL : obj.getType();

			switch (type) {

			// Value type
			case NIL:
			case INT:
			case FLOAT:
			case STRING:
			case LONG:
			case DOUBLE:
			case BOOL:
				if (!isNamedStmt && index <= 2) {
					return false;
				}
				break;

			case ATOM:

				if (RulpUtil.isVarName(((IRAtom) obj).getName())) {
					return false;
				}
				break;

			case FUNC:
				break;

			default:
				return false;
			}
		}

		return true;
	}

	public static boolean isReteTreeModifierAtom(IRAtom obj) throws RException {

		switch (obj.getName()) {
		case A_ENTRY_ORDER:
			return true;

		default:
			return false;
		}
	}

	public static int getReteTreeModifierCount(IRList tree) throws RException {

		if (tree.isEmpty()) {
			return 0;
		}

		int modifierCount = 0;
		for (int index = tree.size() - 1; index >= 0; --index) {
			IRObject obj = tree.get(index);
			if (obj.getType() == RType.ATOM && isReteTreeModifierAtom(RulpUtil.asAtom(obj))) {
				++modifierCount;
				continue;
			}
			break;
		}

		return modifierCount;
	}

	public static boolean isReteTree(IRObject tree) throws RException {

		if (tree.getType() != RType.LIST) {
			return false;
		}

		IRList list = (IRList) tree;
		if (list.isEmpty()) {
			return true;
		}

		IRObject e0 = list.get(0);
		if (e0.getType() == RType.LIST) {

			/************************************/
			// 1. '(ReteTree ReteTree)
			// 2. '(ReteTree Expression)
			// 3. '(ReteTree modifier-atoms) : '('(?a p ?b) '(?b p ?c) order-by-entry-id)
			/************************************/
			if (!isReteTree(e0)) {
				return false;
			}

			// Skip modifier atoms
			int size = list.size() - getReteTreeModifierCount((IRList) tree);

			for (int index = 1; index < size; ++index) {

				IRObject obj = list.get(index);

				// not expression and not ReteTree
				if (obj.getType() != RType.EXPR && !isReteTree(obj)) {
					return false;
				}
			}

			return true;

		} else {

			return ReteUtil.isReteStmt(list);
		}
	}

	public static boolean isSame(IRObject a, IRObject b) {

		if (a == b) {
			return true;
		}

		RType type = a.getType();
		if (type != b.getType()) {
			return false;
		}

		switch (type) {
		case INT:
			return ((IRInteger) a).asInteger() == ((IRInteger) b).asInteger();

		case FLOAT:
			return ((IRFloat) a).asFloat() == ((IRFloat) b).asFloat();

		case ATOM:
			return ((IRAtom) a).getName().equals(((IRAtom) b).getName());

		case STRING:
			return ((IRString) a).asString().equals(((IRString) b).asString());

		default:
			return false;
		}
	}

	public static boolean isSameInheritIndex(InheritIndex inheritIndexs[]) {

		for (int i = 0; i < inheritIndexs.length; ++i) {
			if (i != inheritIndexs[i].elementIndex) {
				return false;
			}
		}

		return true;
	}

	public static boolean isUniqReteStmt(IRList stmt) throws RException {

		if (!isReteStmt(stmt)) {
			return false;
		}

		int stmtLen = stmt.size();
		IRIterator<? extends IRObject> iter = stmt.iterator();

		int varCount = 0;

		while (iter.hasNext()) {

			IRObject obj = iter.next();
			if (RulpUtil.isVarAtom(obj)) {

				// Only support ?0 ?1 ?2 variables, the statement should be ordered
				int varIndex = _getVarUniqIndex(stmtLen, obj);
				if (varIndex < 0) {
					return false;
				}

				if (varIndex > varCount) {
					return false;

				} else if (varIndex == varCount) {
					++varCount;
				}

			}
		}

		return true;
	}

	public static boolean isValidStmtLen(int len) throws RException {
		return len >= STMT_MIN_LEN && len < STMT_MAX_LEN;
	}

	public static boolean isVarArg(IRObject obj) {
		return obj.getType() == RType.ATOM && ((IRAtom) obj).getName().equals(S_QUESTION_LIST);
	}

	public static boolean isVarChangeExpr(IRObject obj) throws RException {

		if (obj.getType() != RType.EXPR || ((IRList) obj).size() == 0) {
			return false;
		}

		IRExpr expr = (IRExpr) obj;

		IRObject e0 = expr.get(0);

		if (e0.getType() == RType.ATOM) {

			switch (RulpUtil.asAtom(e0).getName()) {

			// (var-changed ?varName old-value new-value)
			case F_VAR_CHANGED:

				if (expr.size() != 4 && expr.size() != 3) {
					return false;
				}

				// The second object should be a variable
				if (expr.get(1).getType() != RType.ATOM) {
					return false;
				}

				// the e1 must be a var name format
				if (!RulpUtil.isVarName(RulpUtil.asAtom(expr.get(1)).getName())) {
					throw new RException("the 2nd element must be a var name format: " + expr);
				}

				// The next two object can be a variable or a value
				if (!ReteUtil.isEntryValueType(expr.get(2).getType())
						|| !ReteUtil.isEntryValueType(expr.get(2).getType())) {
					return false;
				}

				return true;

			default:
				return false;
			}

		}

		if (e0.getType() == RType.EXPR) {
			return isVarChangeExpr((IRExpr) e0);
		}

		return false;
	}

	public static boolean matchReteStatus(IRReteEntry entry, int mask) {

		if (mask == 0) {
			if (entry.isDroped()) {
				return false;
			}
		} else {
			if (!ReteUtil.matchReteStatus(entry.getStatus(), mask)) {
				return false;
			}
		}

		return true;
	}

	public static boolean matchReteStatus(RReteStatus status, int mask) {
		return (status.getMask() & mask) > 0;
	}

	public static boolean matchUniqStmt(IRList srcStmt, IRList dstStmt) throws RException {

		if (srcStmt.size() != dstStmt.size()) {
			return false;
		}

		if (!isUniqReteStmt(srcStmt) || !isUniqReteStmt(dstStmt)) {
			return false;
		}

		if (!RuleUtil.equal(srcStmt.getNamedName(), dstStmt.getNamedName())) {
			return false;
		}

		int stmtLen = srcStmt.size();

		// Only support ?0 ?1 ?2 variables, the statement should be ordered
		IRObject srcVarValues[] = new IRObject[stmtLen];
		IRObject dstVarValues[] = new IRObject[stmtLen];

		for (int i = 0; i < stmtLen; ++i) {

			IRObject srcObj = srcStmt.get(i);
			IRObject dstObj = dstStmt.get(i);

			boolean isSrcVar = RulpUtil.isVarAtom(srcObj);
			boolean isDstVar = RulpUtil.isVarAtom(dstObj);

			// all are not var
			if (!isSrcVar && !isDstVar) {

				if (!isSame(srcObj, dstObj)) {
					return false;
				}

				continue;
			}

			int srcVarIndex = -1;
			int dstVarIndex = -1;

			IRObject srcVal = null;
			IRObject dstVal = null;

			if (isSrcVar) {
				srcVarIndex = _getVarUniqIndex(stmtLen, srcObj);
				if (srcVarValues[srcVarIndex] != null) {
					srcVal = srcVarValues[srcVarIndex];
				}
			}

			if (isDstVar) {
				dstVarIndex = _getVarUniqIndex(stmtLen, dstObj);
				if (dstVarValues[dstVarIndex] != null) {
					dstVal = dstVarValues[dstVarIndex];
				}
			}

			// ?x ==> obj
			if (isSrcVar && !isDstVar) {

				if (srcVal == null) {
					srcVarValues[srcVarIndex] = dstObj;

				} else if (!isSame(srcVal, dstObj)) {
					return false;
				}

				continue;
			}

			// obj ==> ?x
			if (!isSrcVar && isDstVar) {

				if (dstVal == null) {
					dstVarValues[dstVarIndex] = srcObj;

				} else if (!isSame(srcObj, dstVal)) {
					return false;
				}

				continue;
			}

			// ?x ==>?y
			if (srcVal == null && dstVal != null) {
				srcVarValues[srcVarIndex] = dstVal;

			} else if (srcVal != null && dstVal == null) {
				dstVarValues[dstVarIndex] = srcVal;

			} else if (srcVal != null && dstVal != null) {
				if (!isSame(srcVal, dstVal)) {
					return false;
				}
			}
		}

		return true;
	}

	public static List<IRList> toCondList(IRList condList, IRNodeGraph graph) throws RException {
		return toCondList(condList, graph, nanBuilder);
	}

	public static List<IRList> toCondList(IRList condList, IRNodeGraph graph, IRObjBuilder builder) throws RException {

		if (!ReteUtil.isCondList(condList)) {
			throw new RException("Invalid CondList: " + condList);
		}

		List<IRList> matchStmtList = new LinkedList<>();
		XTempVarBuilder agVarBuilder = null;
		XTempVarBuilder vgVarBuilder = null;

		/*****************************************************/
		// Check condition list
		/*****************************************************/

		IRIterator<? extends IRObject> condIter = condList.iterator();
		while (condIter.hasNext()) {

			IRObject cond = condIter.next();

			if (ReteUtil.isReteStmt(cond)) {

				IRList stmt = (IRList) cond;
				String namedName = stmt.getNamedName();

				int anyIndex = ReteUtil.indexOfVarArgStmt(stmt);
				if (anyIndex != -1) {

					if (anyIndex != (stmt.size() - 1)) {
						throw new RException(String.format("invalid stmt: %s", stmt));
					}

					/******************************************************/
					// '(?...) or '(?x ?...)
					/******************************************************/
					if (namedName == null) {
						throw new RException(String.format("invalid stmt: %s", stmt));
					}

					/******************************************************/
					// n:'(?...) or n:'(?x ?...)
					/******************************************************/
					IRNamedNode namedNode = graph.findNamedNode(namedName);
					if (namedNode == null) {
						throw new RException(String.format("namedNode not found: %s", namedName));
					}

					if (anyIndex > namedNode.getEntryLength()) {
						throw new RException(String.format("length<%s> invalid for namedNode: %s", stmt, namedNode));
					}

					ArrayList<IRObject> filterObjs = new ArrayList<>();
					for (int i = 0; i < anyIndex; ++i) {
						filterObjs.add(stmt.get(i));
					}

					for (int i = anyIndex; i < namedNode.getEntryLength(); ++i) {

						if (vgVarBuilder == null) {
							vgVarBuilder = new XTempVarBuilder("?_vg_");
						}

						filterObjs.add(vgVarBuilder.next());
					}

					stmt = RulpFactory.createNamedList(filterObjs, namedName);
				}

				/******************************************************/
				// '(a ? ?)
				/******************************************************/
				ArrayList<IRObject> filterObjs = null;

				IRIterator<? extends IRObject> iter = stmt.iterator();
				for (int stmtIndex = 0; iter.hasNext(); stmtIndex++) {

					IRObject obj = iter.next();

					if (ReteUtil.isAnyVar(obj)) {

						if (filterObjs == null) {
							filterObjs = new ArrayList<>();
							for (int i = 0; i < stmtIndex; ++i) {
								filterObjs.add(stmt.get(i));
							}
						}

						if (agVarBuilder == null) {
							agVarBuilder = new XTempVarBuilder("?_ag_");
						}

						filterObjs.add(agVarBuilder.next());

					} else {

						if (filterObjs != null) {
							filterObjs.add(obj);
						}
					}
				}

				if (filterObjs != null) {
					if (namedName == null) {
						stmt = RulpFactory.createList(filterObjs);
					} else {
						stmt = RulpFactory.createNamedList(filterObjs, namedName);
					}
				}

				if (!ReteUtil.isCond(stmt)) {
					throw new RException("Invalid condtion: " + stmt);
				}

				matchStmtList.add((IRList) builder.build(stmt));

			} else if (RulpUtil.isExpression(cond)) {

				matchStmtList.add((IRExpr) builder.build(OptimizeUtil.optimizeExpr(cond)));

			} else if (ReteUtil.isReteTree(cond)) {

				matchStmtList.add((IRList) builder.build(cond));

			} else {
				throw new RException("Invalid condition: " + cond);
			}
		}

		return matchStmtList;
	}

	public static ArrayList<IRExpr> toExprList(IRList list) throws RException {

		ArrayList<IRExpr> array = new ArrayList<>();

		IRIterator<? extends IRObject> iter = list.iterator();
		while (iter.hasNext()) {
			array.add(RulpUtil.asExpression(iter.next()));
		}

		return array;
	}

	public static IRReteNode[] toNodesArray(IRReteNode... nodes) {
		return nodes;
	}

	public static String uniqName(IRList tree) throws RException {
		return _toUniq(tree, new HashMap<>(), null);
	}

	public static String uniqName(IRObject obj) throws RException {
		return _toUniq(obj, new HashMap<>(), null);
	}

	public static String uniqName(IRObject[] entry) throws RException {
		return _toUniq(entry, new HashMap<>(), null);
	}

	public static String uniqName(IRReteEntry entry) throws RException {
		return _toUniq(entry, new HashMap<>(), null);
	}

	public static List<IRObject> uniqVarList(IRList tree) throws RException {
		ArrayList<IRObject> varList = new ArrayList<>();
		_toUniq(tree, new HashMap<>(), varList);
		return varList;
	}

	public static int updateMask(RReteStatus status, int mask) {
		return status.getMask() | mask;
	}

	public static List<String> varList(IRList stmt) throws RException {

		HashSet<String> varList = new HashSet<>();
		_fillVarList(stmt, varList);

		List<String> c = new LinkedList<>(varList);
		Collections.sort(c);
		return c;
	}

	public static List<String> varList(List<? extends IRList> stmtList) throws RException {

		HashSet<String> varList = new HashSet<>();
		for (IRList stmt : stmtList) {
			_fillVarList(stmt, varList);
		}
		List<String> c = new LinkedList<>(varList);
		Collections.sort(c);
		return c;
	}
}
