package alpha.rulp.utils;

import static alpha.rulp.lang.Constant.A_NIL;
import static alpha.rulp.lang.Constant.A_QUESTION;
import static alpha.rulp.lang.Constant.A_QUESTION_C;
import static alpha.rulp.lang.Constant.A_QUESTION_LIST;
import static alpha.rulp.rule.Constant.A_Asc;
import static alpha.rulp.rule.Constant.A_Desc;
import static alpha.rulp.rule.Constant.A_Dup;
import static alpha.rulp.rule.Constant.A_ENTRY_ORDER;
import static alpha.rulp.rule.Constant.A_Index;
import static alpha.rulp.rule.Constant.A_Inherit;
import static alpha.rulp.rule.Constant.A_Order_by;
import static alpha.rulp.rule.Constant.A_Uniq;
import static alpha.rulp.rule.Constant.F_QUERY_STMT;
import static alpha.rulp.rule.Constant.F_VAR_CHANGED;
import static alpha.rulp.rule.Constant.STMT_MAX_LEN;
import static alpha.rulp.rule.Constant.STMT_MIN_LEN;
import static alpha.rulp.rule.RReteStatus.ASSUME;
import static alpha.rulp.rule.RReteStatus.CLEAN;
import static alpha.rulp.rule.RReteStatus.DEFINE;
import static alpha.rulp.rule.RReteStatus.FIXED_;
import static alpha.rulp.rule.RReteStatus.REASON;
import static alpha.rulp.rule.RReteStatus.REMOVE;
import static alpha.rulp.rule.RReteStatus.TEMP__;

import java.util.ArrayList;
import java.util.Collection;
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
import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRInteger;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRLong;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.IRString;
import alpha.rulp.lang.IRVar;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RType;
import alpha.rulp.rule.IRModel;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.rule.IRReteNode.InheritIndex;
import alpha.rulp.rule.RReteStatus;
import alpha.rulp.runtime.IRFactor;
import alpha.rulp.runtime.IRFunction;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.ximpl.constraint.IRConstraint1;
import alpha.rulp.ximpl.constraint.IRConstraint1Uniq;
import alpha.rulp.ximpl.entry.IREntryQueue;
import alpha.rulp.ximpl.entry.IRResultQueue;
import alpha.rulp.ximpl.entry.IRReteEntry;
import alpha.rulp.ximpl.entry.REntryFactory;
import alpha.rulp.ximpl.entry.REntryQueueType;
import alpha.rulp.ximpl.model.IRObjBuilder;
import alpha.rulp.ximpl.model.XRMultiResultQueue;
import alpha.rulp.ximpl.node.AbsReteNode;
import alpha.rulp.ximpl.node.IRNodeGraph;
import alpha.rulp.ximpl.node.RReteType;
import alpha.rulp.ximpl.node.XTempVarBuilder;

public class ReteUtil {

	static class ReplaceMap implements Map<String, IRObject> {

		private Map<String, String> nameMap;

		private Map<Object, IRObject> objectMap;

		public ReplaceMap(Map<String, String> nameMap) {
			super();
			this.nameMap = nameMap;
		}

		@Override
		public void clear() {
			nameMap.clear();
			objectMap.clear();
		}

		@Override
		public boolean containsKey(Object key) {
			return nameMap.containsKey(key);
		}

		@Override
		public boolean containsValue(Object value) {
			throw new RuntimeException("Not support");
		}

		@Override
		public Set<Entry<String, IRObject>> entrySet() {
			throw new RuntimeException("Not support");
		}

		@Override
		public IRObject get(Object key) {

			IRObject obj = null;
			if (objectMap != null) {
				obj = objectMap.get(key);
			}

			if (obj == null) {
				String objName = nameMap.get(key);
				if (objName != null) {
					obj = RulpFactory.createAtom(objName);
					if (objectMap == null) {
						objectMap = new HashMap<>();
					}
					objectMap.put(key, obj);
				}
			}

			return obj;
		}

		@Override
		public boolean isEmpty() {
			return nameMap.isEmpty();
		}

		@Override
		public Set<String> keySet() {
			return nameMap.keySet();
		}

		@Override
		public IRObject put(String key, IRObject value) {
			return objectMap.put(key, value);
		}

		@Override
		public void putAll(Map<? extends String, ? extends IRObject> m) {
			throw new RuntimeException("Not support");
		}

		@Override
		public IRObject remove(Object key) {
			throw new RuntimeException("Not support");
		}

		@Override
		public int size() {
			return nameMap.size();
		}

		@Override
		public Collection<IRObject> values() {
			throw new RuntimeException("Not support");
		}

	}

	static final String INDEX_VAR_PRE = A_QUESTION + "_";

	static IRObjBuilder nanBuilder = new IRObjBuilder() {

		@Override
		public IRObject build(IRObject obj) throws RException {
			return obj;
		}
	};

	static RReteStatus reteStatusConvertArray[][] = {
			// DEFINE,REASON, ASSUME, REMOVE, FIXED, TEMP__, CLEAN
			{ DEFINE, DEFINE, DEFINE, REMOVE, FIXED_, DEFINE, DEFINE }, // DEFINE
			{ DEFINE, REASON, REASON, REMOVE, FIXED_, REASON, REASON }, // REASON
			{ DEFINE, REASON, ASSUME, REMOVE, FIXED_, ASSUME, ASSUME }, // ASSUME
			{ REMOVE, REMOVE, REMOVE, REMOVE, null, REMOVE, REMOVE }, // REMOVE
			{ FIXED_, FIXED_, FIXED_, null, FIXED_, FIXED_, FIXED_ }, // FIX
			{ DEFINE, REASON, ASSUME, REMOVE, FIXED_, TEMP__, TEMP__ }, // TEMP
			{ DEFINE, REASON, ASSUME, REMOVE, FIXED_, TEMP__, CLEAN }, // CLEAN
	};

	private static IRList _enlargeVaryStmt(IRList stmt, int len) throws RException {

		int stmtLen = stmt.size();
		int lastVarIndex = -1;

		for (int i = stmtLen - 2; i >= 0; --i) {

			IRObject obj = stmt.get(i);
			if (RulpUtil.isVarAtom(obj)) {

				// Only support ?0 ?1 ?2 variables, the statement should be ordered
				int varIndex = _getVarUniqIndex(stmtLen, obj);
				if (varIndex != -1) {
					lastVarIndex = varIndex;
					break;
				}
			}
		}

		List<IRObject> srcSubList = RulpUtil.subList(stmt, 0, stmt.size() - 1);
		while (srcSubList.size() < len) {
			srcSubList.add(RulpFactory.createAtom(getIndexVarName(++lastVarIndex)));
		}

		return RulpFactory.createList(srcSubList);
	}

	static int _getVarUniqIndex(int stmtLen, IRObject var) throws RException {

		String varName = var.asString();
		if (!varName.startsWith(INDEX_VAR_PRE)) {
			return -1;
		}

		String varNum = varName.substring(INDEX_VAR_PRE.length());
		if (!StringUtil.isNumber(varNum)) {
			return -1;
		}

		int varIndex = Integer.valueOf(varNum);
		if (varIndex < 0 || varIndex >= stmtLen) {
			return -1;
		}

		return varIndex;
	}

	static void _matchNodes(IRNodeGraph nodeGraph, IRList filter, List<IRReteNode> nodes) throws RException {

		String namedName = filter.getNamedName();

		/******************************************************/
		// '(a ? ?)
		/******************************************************/
		{
			ArrayList<IRObject> filterObjs = null;
			XTempVarBuilder tmpVarBuilder = null;

			IRIterator<? extends IRObject> iter = filter.iterator();
			for (int stmtIndex = 0; iter.hasNext(); stmtIndex++) {

				IRObject obj = iter.next();

				if (RulpUtil.isAnyVar(obj)) {

					if (filterObjs == null) {
						filterObjs = new ArrayList<>();
						for (int i = 0; i < stmtIndex; ++i) {
							filterObjs.add(filter.get(i));
						}
					}

					if (tmpVarBuilder == null) {
						tmpVarBuilder = new XTempVarBuilder("?_ag_");
					}

					filterObjs.add(tmpVarBuilder.next());

				} else {

					if (filterObjs != null) {
						filterObjs.add(obj);
					}
				}
			}

			if (filterObjs != null) {
				filter = RulpUtil.toList(namedName, filterObjs);
			}
		}

		/******************************************************/
		// '(?...) or '(?x ?...)
		// n:'(?...) or n:'(?x ?...)
		/******************************************************/
		int varyIndex = ReteUtil.indexOfVaryArgStmt(filter);
		if (varyIndex != -1) {

			if (varyIndex != (filter.size() - 1)) {
				throw new RException(String.format("invalid filter: %s", filter));
			}

			ArrayList<IRObject> extendFilterObjs = new ArrayList<>();
			XTempVarBuilder tmpVarBuilder = new XTempVarBuilder("?_vg_");

			for (int i = 0; i < varyIndex; ++i) {
				extendFilterObjs.add(filter.get(i));
			}

			/******************************************************/
			// '(?...)
			/******************************************************/
			if (namedName == null) {

				while (extendFilterObjs.size() <= nodeGraph.getMaxRootStmtLen()) {
					if (nodeGraph.findRootNode(null, extendFilterObjs.size()) != null) {
						_matchNodes(nodeGraph, RulpFactory.createNamedList(namedName, extendFilterObjs), nodes);
					}
				}

				return;
			}

			IRReteNode namedNode = nodeGraph.findRootNode(namedName, -1);
			if (namedNode == null || extendFilterObjs.size() > namedNode.getEntryLength()) {
				return;
			}

			for (int i = varyIndex; i < namedNode.getEntryLength(); ++i) {
				extendFilterObjs.add(tmpVarBuilder.next());
			}

			filter = RulpFactory.createNamedList(namedName, extendFilterObjs);
		}

		/******************************************************/
		// Check named node
		/******************************************************/
		if (namedName != null) {

			IRReteNode namedNode = nodeGraph.findRootNode(namedName, -1);
			if (namedNode == null || namedNode.getEntryLength() != filter.size()) {
				return;
			}
		}

		/******************************************************/
		// Query uniq stmt
		/******************************************************/
		IRReteNode node;

		if (ReteUtil.getStmtVarCount(filter) == 0) {
			node = nodeGraph.createNodeRoot(filter.getNamedName(), filter.size());
		} else {
			node = nodeGraph.createNodeByTree(filter);
		}

		if (!RReteType.isRootType(node.getReteType()) && node.getReteType() != RReteType.ALPH0) {
			throw new RException("Invalid list node: " + node);
		}

		if (!nodes.contains(node)) {
			nodes.add(node);
		}

		return;
	}

	private static String _toUniq(IRObject obj, Map<String, String> varMap, boolean create) throws RException {

		if (obj == null) {
			return A_NIL;
		}

		switch (obj.getType()) {
		case NIL:
			return A_NIL;

		case ATOM:

			if (ReteUtil.isVaryArg(obj)) {
				return obj.asString();
			}

			String atomName = ((IRAtom) obj).getName();

			if (varMap != null && RulpUtil.isVarName(atomName)) {
				String newName = varMap.get(atomName);
				if (newName == null) {
					if (create) {
						newName = getIndexVarName(varMap.size());
						varMap.put(atomName, newName);
					} else {
						newName = atomName;
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

		case LIST: {

			IRList list = (IRList) obj;
			String out = list.getNamedName() == null ? "" : (list.getNamedName() + ":");

			// the external var in right expression should be not changed
			// '(?a ?b ?c) (> ?b ?x)
			if (list.size() == 2 && list.get(0).getType() == RType.LIST && list.get(1).getType() == RType.EXPR) {
				out += "'(" + _toUniq(list.get(0), varMap, true) + " "
						+ _toUniq(RuntimeUtil.rebuild(list.get(1), new ReplaceMap(varMap)), null, true) + ")";

			} else {
				out += "'(" + _toUniqIterator(list.iterator(), varMap, true) + ")";
			}

			return out;
		}

		case EXPR:

			IRExpr expr = (IRExpr) obj;
			IRObject e0 = expr.get(0);

			if (ReteUtil.isVarChangeExpr(expr) && e0.getType() == RType.ATOM) {

				String name0 = RulpUtil.asAtom(e0).getName();

				switch (name0) {
				// (var-changed varName old-value new-value)
				// (var-changed varName new-value)
				case F_VAR_CHANGED:

					String varName = RulpUtil.asAtom(expr.get(1)).getName();

					// e1 must be a var name
					String out = "(" + name0 + " " + varName;

					// (var-changed varName new-value)
					if (expr.size() == 3) {

						IRObject newValue = expr.get(2);
						if (RulpUtil.isVarAtom(newValue)) {
							String newVarName = varChangeNewName(varName);
							out += " " + newVarName;
							if (varMap != null) {
								varMap.put(newValue.asString(), newVarName);
							}
						} else {
							out += " " + newValue;
						}

					}
					// (var-changed varName old-value new-value)
					else {

						IRObject oldValue = expr.get(2);
						if (RulpUtil.isVarAtom(oldValue)) {
							String oldVarName = varChangeOldName(varName);
							out += " " + oldVarName;
							if (varMap != null) {
								varMap.put(oldValue.asString(), oldVarName);
							}
						} else {
							out += " " + oldValue;
						}

						IRObject newValue = expr.get(3);
						if (RulpUtil.isVarAtom(newValue)) {
							String newVarName = varChangeNewName(varName);
							out += " " + newVarName;
							if (varMap != null) {
								varMap.put(newValue.asString(), newVarName);
							}
						} else {
							out += " " + newValue;
						}
					}

					out += ")";

					return out;

				default:
					throw new RException("not support: " + obj);
				}
			}

			return "(" + _toUniqIterator(((IRList) obj).iterator(), varMap, false) + ")";

		case FUNC:
			return ((IRFunction) obj).getSignature();

		case STRING:
			return "\"" + ((IRString) obj).asString() + "\"";

		default:
			throw new RException("unsupport type: " + obj.getType());

		}
	}

	private static String _toUniq(IRObject[] entry, Map<String, String> varMap, boolean create) throws RException {

		StringBuffer sb = new StringBuffer();
		int i = 0;
		for (IRObject e : entry) {
			if (i++ != 0) {
				sb.append(' ');
			}

			sb.append(_toUniq(e, varMap, create));
		}

		return sb.toString();
	}

	private static String _toUniqIterator(IRIterator<? extends IRObject> iterator, Map<String, String> varMap,
			boolean create) throws RException {

		StringBuffer sb = new StringBuffer();
		int i = 0;
		while (iterator.hasNext()) {
			if (i++ != 0) {
				sb.append(' ');
			}

			sb.append(_toUniq(iterator.next(), varMap, create));
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

		int size = reteTree.size();

		if (ReteUtil.isVarChangeExpr(reteTree)) {

			IRObject e0 = reteTree.get(0);

			if (reteTree.get(0).getType() == RType.EXPR) {
				return buildTreeVarList((IRList) e0, indexMap);
			}

			switch (RulpUtil.asAtom(e0).getName()) {

			// (var-changed ?varName old-value new-value)
			case F_VAR_CHANGED:

				ArrayList<IRObject> vars = new ArrayList<>();
				for (int i = 1; i < size; ++i) {

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
			for (int i = 0; i < size; ++i) {
				IRObject obj = reteTree.get(i);
				if (_tryPutVarIndex(indexMap, obj, i)) {
					objs.add(obj);
				} else {
					objs.add(null);
				}
			}

			return objs;
		}

		// Beta node
		if (isBetaTree(reteTree, size)) {

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

		// Expr node
		if (size == 2 && reteTree.get(0).getType() == RType.LIST && reteTree.get(1).getType() == RType.EXPR) {

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

		// beta3: '(?a b c) '(?x y z) (not-equal ?a ?x)
		if (isBeta3Tree(reteTree, size)) {

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
		if (size == 2 && ReteUtil.isVarChangeExpr(reteTree.get(0)) && ReteUtil.isVarChangeExpr(reteTree.get(1))) {

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

			for (int index = 0; index < size - ModifierCount; ++index) {
				for (IRObject v : buildTreeVarList((IRList) reteTree.get(index), new HashMap<>())) {
					if (_tryPutVarIndex(indexMap, v, vars.size())) {
						vars.add(v);
					}
				}
			}

			return vars;
		}

		if (ReteUtil.isZetaTree(reteTree, size)) {
			ArrayList<IRObject> vars = new ArrayList<>();
			for (int i = 0; i < size; ++i) {
				IRObject ex = reteTree.get(i);
				if (ex.getType() == RType.LIST) {
					for (IRObject v : buildTreeVarList((IRList) ex, new HashMap<>())) {
						if (_tryPutVarIndex(indexMap, v, vars.size())) {
							vars.add(v);
						}
					}
				}
			}

			return vars;
		}

		if (ReteUtil.isInheritExpr2(reteTree)) {

			ArrayList<IRObject> objs = new ArrayList<>();
			for (int i = 2; i < size; ++i) {
				IRObject obj = reteTree.get(i);
				if (_tryPutVarIndex(indexMap, obj, i - 2)) {
					objs.add(obj);
				} else {
					objs.add(null);
				}
			}

			return objs;
		}

		throw new RException("Invalid tree node found: " + reteTree);
	}

	public static IRObject[] buildVarEntry(IRModel model, IRList condList) throws RException {

		/******************************************************/
		// Build var list
		/******************************************************/
		List<IRList> matchStmtList = ReteUtil.toCondList(condList, model.getNodeGraph());
		IRList matchTree = MatchTree.build(matchStmtList, model.getInterpreter(), model.getFrame());
		IRObject[] varEntry = ReteUtil._varEntry(ReteUtil.buildTreeVarList(matchTree));

		return varEntry;
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

	public static String combine(String a, String b) {

		if (a == null || a.isEmpty()) {
			return b;
		}

		if (b == null || b.isEmpty()) {
			return a;
		}

		return a + "; " + b;
	}

	public static int compareEntry(IRList e1, IRList e2, List<OrderEntry> orderEntries) throws RException {

		int d = 0;

		for (OrderEntry order : orderEntries) {

			IRObject o1 = e1.get(order.index);
			IRObject o2 = e2.get(order.index);

			if (order.asc) {
				d = RulpUtil.compare(o2, o1);
			} else {
				d = RulpUtil.compare(o1, o2);
			}

			if (d != 0) {
				break;
			}
		}

		return d;
	}

	public static IRResultQueue createResultQueue(IRModel model, IRObject rstExpr, IRList condList) throws RException {

		/******************************************************/
		// Build frame
		/******************************************************/
		IRFrame queryFrame = RulpFactory.createFrame(model.getFrame(), F_QUERY_STMT);
		RuleUtil.setDefaultModel(queryFrame, model);
		RulpUtil.incRef(queryFrame);

		/******************************************************/
		// Build var list
		/******************************************************/
		IRObject[] varEntry = buildVarEntry(model, condList);
		IRVar[] vars = new IRVar[varEntry.length];

		for (int i = 0; i < varEntry.length; ++i) {
			IRObject obj = varEntry[i];
			if (obj != null) {
				vars[i] = RulpUtil.addVar(queryFrame, RulpUtil.asAtom(obj).getName());
			}
		}

		return new XRMultiResultQueue(model.getInterpreter(), queryFrame, rstExpr, vars);
	}

	public static void enableAutoGC(IRReteNode node) {

		switch (node.getReteType()) {
		case ALPH0:
		case ALPH2:
		case BETA0:
		case BETA1:
		case BETA2:
		case BETA3:
		case EXPR0:
		case EXPR1:
		case EXPR2:
		case EXPR3:
		case EXPR4:
		case INHER:
		case ZETA0:
			node.setAutoGC(true);
			break;

		default:
			break;
		}
	}

	public static void fillVarList(IRList stmt, Collection<String> varList) throws RException {

		IRIterator<? extends IRObject> iter = ((IRList) stmt).iterator();
		while (iter.hasNext()) {

			IRObject obj = iter.next();

			if (RulpUtil.isVarAtom(obj)) {
				varList.add(RulpUtil.asAtom(obj).getName().trim());
				continue;
			}

			if (obj.getType() == RType.LIST || obj.getType() == RType.EXPR) {
				fillVarList((IRList) obj, varList);
			}
		}
	}

	public static int findChildMaxVisitIndex(IRReteNode node, Collection<? extends IRReteNode> nodes)
			throws RException {

		// Get the max visit index
		int childMaxVisitIndex = -1;
		for (IRReteNode child : node.getChildNodes()) {

			if (nodes != null && !nodes.contains(child)) {
				continue;
			}

			int parentVisitIndex = -1;
			int parentCount = child.getParentCount();

			for (int j = 0; j < parentCount; ++j) {
				if (child.getParentNodes()[j] == node) {
					parentVisitIndex = child.getParentVisitIndex(j);
					break;
				}
			}

			if (parentVisitIndex == -1) {
				throw new RException("parentVisitIndex not found: " + child);
			}

			childMaxVisitIndex = Math.max(childMaxVisitIndex, parentVisitIndex);
		}

		return childMaxVisitIndex;
	}

//	public static IRReteNode findDupNode(IRReteNode node) {
//		return matchChildNode(node, RReteType.DUP, null);
//	}

	public static IRReteNode findNameNode(IRNodeGraph graph, IRList filter) throws RException {

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
		int varyIndex = ReteUtil.indexOfVaryArgStmt(filter);
		if (varyIndex != -1 && varyIndex != (nodeEntryLengh - 1)) {
			throw new RException(String.format("invalid named filter: %s", filter));
		}

		/**************************************************/
		// Check entry length
		/**************************************************/
		IRReteNode namedNode = graph.findRootNode(namedName, -1);
		if (namedNode != null) {

			if (varyIndex == -1) {

				if (namedNode.getEntryLength() != nodeEntryLengh) {
					throw new RException(String.format("unmatch entry length: expect=%d, actual=%d", nodeEntryLengh,
							namedNode.getEntryLength()));
				}

			} else {

				if (varyIndex > namedNode.getEntryLength()) {
					throw new RException(String.format("unmatch vararg entry length: vararg=%d, actual=%d", varyIndex,
							namedNode.getEntryLength()));
				}
			}

		}

		return namedNode;
	}

	public static List<IRReteEntry> getAllEntries(IREntryQueue queue) throws RException {

		int size = queue.size();

		ArrayList<IRReteEntry> stmtList = new ArrayList<>();

		for (int i = 0; i < size; ++i) {

			IRReteEntry entry = queue.getEntryAt(i);
			if (entry == null || entry.isDroped()) {
				continue;
			}

			if (stmtList == null) {
				stmtList = new ArrayList<>();
			}

			stmtList.add(entry);
		}

		return stmtList == null ? Collections.emptyList() : stmtList;
	}

	public static List<IRReteNode> getAllNodes(IRNodeGraph graph) {

		ArrayList<IRReteNode> allNodeList = new ArrayList<>();
		for (RReteType t : RReteType.ALL_RETE_TYPE) {
			allNodeList.addAll(graph.listNodes(t));
		}

		return allNodeList;
	}

	public static RReteStatus getChildStatus(IRReteEntry... parents) throws RException {

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

	public static String getDupNodeUniqName(String parentUniqName) {

		StringBuffer sb = new StringBuffer();
		sb.append("(");
		sb.append(A_Dup);
		sb.append(" ");
		sb.append(parentUniqName);
		sb.append(")");

		return sb.toString();
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
				if (obj.getType() != RType.ATOM || varName.charAt(0) != A_QUESTION_C) {
					throw new RException(String.format("Invalid obj<%s> in filter: %s", obj, filter));
				}

				switch (varName) {
				case A_QUESTION:
				case A_QUESTION_LIST:
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

	public static String getIndexNodeUniqName(String parentUniqName, List<OrderEntry> orderList) {

		StringBuffer sb = new StringBuffer();
		sb.append("(");
		sb.append(A_Index);
		sb.append(" ");
		sb.append(parentUniqName);

		for (OrderEntry order : orderList) {
			sb.append(" ");
			sb.append(A_Order_by);
			sb.append(" ");
			sb.append(order.index);
			sb.append(" ");
			sb.append(order.asc ? A_Asc : A_Desc);
		}

		sb.append(")");

		return sb.toString();
	}

	public static String getIndexVarName(int index) {
		return INDEX_VAR_PRE + index;
	}

	public static IRReteEntry getLastEntry(IREntryQueue queue) throws RException {

		int size = queue.size();
		for (int i = size - 1; i >= 0; --i) {
			IRReteEntry entry = queue.getEntryAt(i);
			if (entry != null && !entry.isDroped()) {
				return entry;
			}
		}

		return null;
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

	public static List<IRConstraint1> getNodeConstraint1List(IRReteNode node) {

		int count = node.getConstraint1Count();
		if (count == 0) {
			return Collections.emptyList();
		}

		ArrayList<IRConstraint1> cons = new ArrayList<>();
		for (int i = 0; i < count; ++i) {
			cons.add(node.getConstraint1(i));
		}

		return cons;
	}

	public static String getNodeName(RReteType type, int nodeId) {

		switch (type) {
		case ALPH0:
			return String.format("A0%04d", nodeId);

		case ALPH1:
			return String.format("A1%04d", nodeId);

		case ALPH2:
			return String.format("A2%04d", nodeId);

		case BETA0:
			return String.format("B0%04d", nodeId);

		case BETA1:
			return String.format("B1%04d", nodeId);

		case BETA2:
			return String.format("B2%04d", nodeId);

		case BETA3:
			return String.format("B3%04d", nodeId);

		case ZETA0:
			return String.format("Z0%04d", nodeId);

		case EXPR0:
			return String.format("E0%04d", nodeId);

		case EXPR1:
			return String.format("E1%04d", nodeId);

		case EXPR2:
			return String.format("E2%04d", nodeId);

		case EXPR3:
			return String.format("E3%04d", nodeId);

		case EXPR4:
			return String.format("E4%04d", nodeId);

		case ROOT0:
			return String.format("R0%04d", nodeId);

		case NAME0:
			return String.format("N0%04d", nodeId);

		case VAR:
			return String.format("V%05d", nodeId);

		case WORK:
			return String.format("W%05d", nodeId);

		case RULE:
			return String.format("U%05d", nodeId);

		case CONST:
			return String.format("C%05d", nodeId);

		case INDEX:
			return String.format("I0%04d", nodeId);

		case INHER:
			return String.format("I1%04d", nodeId);

		case OR0:
			return String.format("O0%04d", nodeId);

		case DUP:
			return String.format("D0%04d", nodeId);

		default:
			return String.format("X%05d", nodeId);
		}
	}

	public static RReteStatus getReteStatus(RReteStatus fromStatus, RReteStatus toStatus) {
		return reteStatusConvertArray[fromStatus.getIndex()][toStatus.getIndex()];
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

	public static String getRootUniqName(String name, int stmtLen) {

		String uniqName = "'(";

		for (int i = 0; i < stmtLen; ++i) {
			if (i != 0) {
				uniqName += " ";
			}
			uniqName += getIndexVarName(i);
		}
		uniqName += ")";

		return name == null ? uniqName : (name + ":" + uniqName);
	}

	public static IRReteEntry getStmt(IRReteNode rootNode, IRList stmt) throws RException {

		// Named Root node which has uniq constraint, its entry queue is mulit
		// Check whether there is any uniq constraint that can match the stmt
		if (rootNode.getReteType() == RReteType.NAME0 && !rootNode.listUniqConstraints().isEmpty()) {
			IRConstraint1Uniq cons = rootNode.listUniqConstraints().get(0);
			return cons.getReteEntry(cons.getUniqString(stmt));
		}

		return getStmt(rootNode, ReteUtil.uniqName(stmt));
	}

	public static IRReteEntry getStmt(IRReteNode rootNode, String uniqName) throws RException {
		return rootNode.getEntryQueue().getStmt(uniqName);
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

	public static int indexOfVaryArgStmt(IRList stmt) throws RException {

		if (!isReteStmt(stmt)) {
			return -1;
		}

		IRIterator<? extends IRObject> iter = stmt.iterator();
		for (int stmtIndex = 0; iter.hasNext(); stmtIndex++) {
			if (isVaryArg(iter.next())) {
				return stmtIndex;
			}
		}

		return -1;
	}

	public static boolean isActionEntry(IRList actionEntry) throws RException {
		return isValidStmtLen(actionEntry.size()) && ReteUtil.isActionEntryValueType(actionEntry.get(0).getType());
	}

	public static boolean isActionEntryValueType(RType type) throws RException {

		switch (type) {
		case INT:
		case ATOM:
		case FLOAT:
		case STRING:
		case LONG:
		case DOUBLE:
		case BOOL:
		case EXPR:
			return true;
		default:
			return false;
		}
	}

	public static boolean isAlphaMatchTree(IRList matchTree) throws RException {
		return isValidStmtLen(matchTree.size()) && ReteUtil.isEntryValueType(matchTree.get(0).getType());
	}

	public static boolean isAnyStmt(IRList stmt) throws RException {
		return isReteStmt(stmt) && stmt.size() == 1 && isVaryArg(stmt.get(0));
	}

	public static boolean isBeta3Tree(IRList reteTree, int treeSize) throws RException {
		return treeSize == 3 && reteTree.get(0).getType() == RType.LIST && reteTree.get(1).getType() == RType.LIST
				&& reteTree.get(2).getType() == RType.EXPR;
	}

	public static boolean isBetaTree(IRList reteTree, int treeSize) throws RException {
		return treeSize == 2 && reteTree.getType() == RType.LIST && isListTree(reteTree.get(0))
				&& isListTree(reteTree.get(1));
	}

	public static boolean isCond(IRList cond) throws RException {

		if (!ReteUtil.isReteStmt(cond)) {
			return false;
		}

		IRIterator<? extends IRObject> iter = cond.iterator();
		while (iter.hasNext()) {
			if (iter.next().asString().equals(A_QUESTION)) {
				return false;
			}
		}

		return true;
	}

	public static boolean isCondList(IRList condList) throws RException {

		IRIterator<? extends IRObject> condIter = condList.iterator();
		while (condIter.hasNext()) {

			IRObject cond = condIter.next();

			if (ReteUtil.isReteStmt(cond) || ReteUtil.isReteTree(cond) || RulpUtil.isExpr(cond)) {
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

	public static boolean isIndexVarAtom(IRObject obj) {
		return obj.getType() == RType.ATOM && isIndexVarName(obj.asString());
	}

	public static boolean isIndexVarName(String var) {

		if (var.length() <= 1 || var.charAt(0) != A_QUESTION_C) {
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

	public static boolean isInheritExpr1(IRObject obj) throws RException {

		if (obj.getType() != RType.EXPR || ((IRList) obj).size() < 3) {
			return false;
		}

		IRExpr expr = (IRExpr) obj;

		if (!RulpUtil.isFactor(expr.get(0), A_Inherit)) {
			return false;
		}

		IRObject e1 = expr.get(1);
		if (e1.getType() != RType.EXPR && e1.getType() != RType.LIST) {
			return false;
		}

		IRIterator<? extends IRObject> it = expr.listIterator(2);
		while (it.hasNext()) {
			if (it.next().getType() != RType.INT) {
				return false;
			}
		}

		return true;
	}

	public static boolean isInheritExpr2(IRObject obj) throws RException {

		if (obj.getType() != RType.EXPR || ((IRList) obj).size() < 3) {
			return false;
		}

		IRExpr expr = (IRExpr) obj;

		if (!RulpUtil.isFactor(expr.get(0), A_Inherit)) {
			return false;
		}

		IRObject e1 = expr.get(1);
		if (e1.getType() != RType.EXPR && e1.getType() != RType.LIST) {
			return false;
		}

		IRIterator<? extends IRObject> it = expr.listIterator(2);
		while (it.hasNext()) {
			if (!RulpUtil.isVarAtom(it.next())) {
				return false;
			}
		}

		return true;
	}

	public static boolean isListTree(IRObject obj) throws RException {

		if (obj.getType() == RType.LIST) {
			return true;
		}

		if (obj.getType() == RType.EXPR) {

			IRExpr expr = (IRExpr) obj;
			if (expr.size() > 0 && RulpUtil.isAtom(expr.get(0), A_Inherit)) {
				return true;
			}
		}

		return false;
	}

	public static boolean isRemovedEntry(IRReteEntry entry) throws RException {
		return entry == null || entry.getStatus() == null || entry.getStatus() == CLEAN;
	}

	public static boolean isReteStmt(IRList stmt) throws RException {

		if (!isValidStmtLen(stmt.size())) {
			return false;
		}

//		int index = 0;
		IRIterator<? extends IRObject> iter = stmt.iterator();

		while (iter.hasNext()) {

//			++index;

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
//				if (stmt.getNamedName() == null && index <= 2) {
//					return false;
//				}
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

			case LIST:
				break;

			default:
				return false;
			}
		}

		return true;
	}

//	public static boolean isReteStmtNoVar(IRList stmt) throws RException {
//
//	}

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

	public static boolean isReteTreeModifierAtom(IRAtom obj) throws RException {

		switch (obj.getName()) {
		case A_ENTRY_ORDER:
			return true;

		default:
			return false;
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
		int varCount = 0;
		for (int i = 0; i < stmtLen; ++i) {

			IRObject obj = stmt.get(i);

			// '(a b ?...)
			if (ReteUtil.isVaryArg(obj)) {
				return i == (stmtLen - 1);
			}

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

	public static boolean isValidNodeStmt(IRReteNode node, IRList stmt) throws RException {
		String nodeName = node.getReteType() == RReteType.NAME0 ? node.getNamedName() : null;
		return RuleUtil.equal(nodeName, stmt.getNamedName()) && node.getEntryLength() == stmt.size();
	}

	public static boolean isValidStmtLen(int len) throws RException {
		return len >= STMT_MIN_LEN && len <= STMT_MAX_LEN;
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
						|| (expr.size() == 4 && !ReteUtil.isEntryValueType(expr.get(3).getType()))) {
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

	public static boolean isVaryArg(IRObject obj) {
		return obj.getType() == RType.ATOM && ((IRAtom) obj).getName().equals(A_QUESTION_LIST);
	}

	public static boolean isVaryStmt(IRList stmt) throws RException {
		return isReteStmt(stmt) && isVaryArg(stmt.get(stmt.size() - 1));
	}

	public static boolean isZetaTree(IRList reteTree, int treeSize) throws RException {

		if (treeSize <= 2 || reteTree.getType() != RType.LIST) {
			return false;
		}

		for (int i = 0; i < treeSize - 1; ++i) {
			if (reteTree.get(i).getType() != RType.LIST) {
				return false;
			}
		}

		RType lastType = reteTree.get(treeSize - 1).getType();

		return lastType == RType.LIST || lastType == RType.EXPR;
	}

	public static IRReteNode matchChildNode(IRReteNode node, RReteType type, String childUniqName) {

		for (IRReteNode child : node.getChildNodes()) {

			if (type != null && child.getReteType() != type) {
				continue;
			}

			if (childUniqName != null && !child.getUniqName().equals(childUniqName)) {
				continue;
			}

			return child;
		}

		return null;
	}

	public static List<IRReteNode> matchNodes(IRNodeGraph nodeGraph, IRList filter) throws RException {
		List<IRReteNode> nodes = new ArrayList<>();
		_matchNodes(nodeGraph, filter, nodes);
		return nodes;
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

		if (!RuleUtil.equal(srcStmt.getNamedName(), dstStmt.getNamedName())) {
			return false;
		}

		if (ReteUtil.isVaryStmt(srcStmt)) {

			/****************************************/
			// before: n1:(?...) any
			// or
			// before: any n1:(?...)
			/****************************************/
			if (ReteUtil.isAnyStmt(srcStmt) || ReteUtil.isAnyStmt(dstStmt)) {
				return true;
			}

			if (ReteUtil.isVaryStmt(dstStmt)) {

				/****************************************/
				// before: n1:(?0 ?...) n1:(a ?...)
				// after : n1:(?0) n1:(a)
				/****************************************/
				if (srcStmt.size() == dstStmt.size()) {

					srcStmt = RulpFactory.createList(RulpUtil.subList(srcStmt, 0, srcStmt.size() - 1));
					dstStmt = RulpFactory.createList(RulpUtil.subList(dstStmt, 0, dstStmt.size() - 1));

				}
				/****************************************/
				// before: n1:(?0 ?...) n1:(a b c ?...)
				// after : n1:(?0 ?1 ?2) n1:(a b c)
				/****************************************/
				else {

					if (srcStmt.size() > dstStmt.size()) {
						IRList tmpStmt = srcStmt;
						srcStmt = dstStmt;
						dstStmt = tmpStmt;
					}

					dstStmt = RulpFactory.createList(RulpUtil.subList(dstStmt, 0, dstStmt.size() - 1));
					srcStmt = _enlargeVaryStmt(srcStmt, dstStmt.size());
				}

			} else {

				/****************************************/
				// before: n1:(?0 ?...) n1:(a b c)
				// after : n1:(?0 ?1 ?2) n1:(a b c)
				/****************************************/
				if (srcStmt.size() <= dstStmt.size()) {
					srcStmt = _enlargeVaryStmt(srcStmt, dstStmt.size());
				}
				/****************************************/
				// before: n1:(?0 ?1 ?...) n1:(a b)
				// after : n1:(?0 ?1) n1:(a b)
				/****************************************/
				else if (srcStmt.size() == (dstStmt.size() + 1)) {
					srcStmt = RulpFactory.createList(RulpUtil.subList(srcStmt, 0, srcStmt.size() - 1));
				}
				/****************************************/
				// before: n1:(?x ?y ?z ?...) n1:(a b)
				// after : false
				/****************************************/
				else {
					return false;
				}
			}

		} else if (ReteUtil.isVaryStmt(dstStmt)) {
			return matchUniqStmt(dstStmt, srcStmt);
		}

		if (srcStmt.size() != dstStmt.size()) {
			return false;
		}

		if (!isUniqReteStmt(srcStmt) || !isUniqReteStmt(dstStmt)) {
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

	public static IRList rebuildVaryStmt(IRNodeGraph graph, IRList stmt, Map<String, List<IRObject>> anyVarListMap,
			boolean force) throws RException {

		int anyIndex = ReteUtil.indexOfVaryArgStmt(stmt);
		if (anyIndex == -1) {
			return stmt;
		}

		String namedName = stmt.getNamedName();
		if (namedName == null) {
			if (!force) {
				return null;
			}
			throw new RException(String.format("need named for any filter: %s", stmt));
		}

		if (anyIndex != (stmt.size() - 1)) {
			if (!force) {
				return null;
			}
			throw new RException(String.format("invalid any filter: %s", stmt));
		}

		IRReteNode namedNode = graph.findRootNode(namedName, -1);
		if (namedNode == null) {
			if (!force) {
				return null;
			}
			throw new RException(String.format("named node not found: %s", stmt));
		}

		String anyVarName = stmt.get(anyIndex).asString();
		List<IRObject> anyVarList = anyVarListMap.get(anyVarName);
		if (anyVarList == null) {

			if (namedNode.getEntryLength() < stmt.size()) {
				if (!force) {
					return null;
				}
				throw new RException(
						String.format("named node length<%d> not match: %s", namedNode.getEntryLength(), stmt));
			}

			anyVarList = new ArrayList<>();
			XTempVarBuilder tmpVarBuilder = new XTempVarBuilder(namedName + "_any");
			for (int j = anyIndex; j < namedNode.getEntryLength(); ++j) {
				anyVarList.add(tmpVarBuilder.next());
			}

			anyVarListMap.put(anyVarName, anyVarList);
		}

		if ((anyIndex + anyVarList.size()) != namedNode.getEntryLength()) {
			if (!force) {
				return null;
			}
			throw new RException(
					String.format("named node length<%d> not match: %s", namedNode.getEntryLength(), stmt));
		}

		ArrayList<IRObject> newObjList = new ArrayList<>();
		for (int j = 0; j < anyIndex; ++j) {
			newObjList.add(stmt.get(j));
		}

		newObjList.addAll(anyVarList);
		return RulpFactory.createNamedList(namedName, newObjList);
	}

	public static IRList rebuildVaryStmtList(IRNodeGraph graph, IRList condList,
			Map<String, List<IRObject>> varyVarListMap) throws RException {

		ArrayList<IRObject> filterObjs = null;

		int condSize = condList.size();

		for (int i = 0; i < condSize; ++i) {

			IRObject obj = condList.get(i);

			boolean update = false;

			if (obj.getType() == RType.LIST) {
				IRList filter = RulpUtil.asList(obj);
				IRList newFilter = rebuildVaryStmt(graph, filter, varyVarListMap, true);
				if (newFilter != filter) {
					obj = newFilter;
					update = true;
				}
			}

			if (update) {

				if (filterObjs == null) {
					filterObjs = new ArrayList<>();
					for (int j = 0; j < i; ++j) {
						filterObjs.add(condList.get(j));
					}
				}

				filterObjs.add(obj);

			} else {

				if (filterObjs != null) {
					filterObjs.add(obj);
				}
			}
		}

		if (filterObjs == null) {
			return condList;
		}

		return RulpFactory.createList(filterObjs);
	}

	public static void setParentNodes(AbsReteNode child, IRReteNode... parents) throws RException {

		if (parents == null || parents.length == 0) {
			return;
		}

		int len = parents.length;

		IRReteNode[] realParents = new IRReteNode[len];
		for (int i = 0; i < len; ++i) {

			IRReteNode parent = parents[i];

//			if (parent.getReteType() == RReteType.ROOT0 || parent.getReteType() == RReteType.NAME0) {
//				IRReteNode dupNode = findDupNode(parent);
//				if (dupNode != null) {
//					parent = dupNode;
//				}
//			}

			parent.addChildNode(child);
			realParents[i] = parent;
		}

		child.setParentNodes(realParents);
	}

	public static boolean supportIndexStmt(IRList stmt) throws RException {

		if (!ReteUtil.isAlphaMatchTree(stmt)) {
			return false;
		}

		ArrayList<String> nodeVarList = new ArrayList<>();
		ReteUtil.fillVarList(stmt, nodeVarList);

		if (nodeVarList.isEmpty()) {
			return false;
		}

		HashSet<String> nodeVarSet = new HashSet<>(nodeVarList);
		if (nodeVarSet.size() != nodeVarList.size()) {
			return false;
		}

		return true;
	}

	public static boolean supportUpdateIncrementally(IRReteNode node) {
		switch (node.getReteType()) {
		case ZETA0:
		case BETA3:
			return true;

		default:
			return false;
		}
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

				int varyIndex = ReteUtil.indexOfVaryArgStmt(stmt);
				if (varyIndex != -1) {

					if (varyIndex != (stmt.size() - 1)) {
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
					IRReteNode namedNode = graph.findRootNode(namedName, -1);
					if (namedNode == null) {
						throw new RException(String.format("namedNode not found: %s", namedName));
					}

					if (varyIndex > namedNode.getEntryLength()) {
						throw new RException(String.format("length<%s> invalid for namedNode: %s", stmt, namedNode));
					}

					ArrayList<IRObject> filterObjs = new ArrayList<>();
					for (int i = 0; i < varyIndex; ++i) {
						filterObjs.add(stmt.get(i));
					}

					for (int i = varyIndex; i < namedNode.getEntryLength(); ++i) {

						if (vgVarBuilder == null) {
							vgVarBuilder = new XTempVarBuilder("?_vg_");
						}

						filterObjs.add(vgVarBuilder.next());
					}

					stmt = RulpFactory.createNamedList(namedName, filterObjs);
				}

				/******************************************************/
				// '(a ? ?)
				/******************************************************/
				ArrayList<IRObject> filterObjs = null;

				IRIterator<? extends IRObject> iter = stmt.iterator();
				for (int stmtIndex = 0; iter.hasNext(); stmtIndex++) {

					IRObject obj = iter.next();

					if (RulpUtil.isAnyVar(obj)) {

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
						stmt = RulpFactory.createNamedList(namedName, filterObjs);
					}
				}

				if (!ReteUtil.isCond(stmt)) {
					throw new RException("Invalid condtion: " + stmt);
				}

				matchStmtList.add((IRList) builder.build(stmt));

			} else if (RulpUtil.isExpr(cond)) {

				matchStmtList.add((IRExpr) builder.build(cond));

			} else if (ReteUtil.isReteTree(cond)) {

				matchStmtList.add((IRList) builder.build(cond));

			} else {
				throw new RException("Invalid condition: " + cond);
			}
		}

		return matchStmtList;
	}

	public static IRReteNode[] toNodesArray(IRReteNode... nodes) {
		return nodes;
	}

	public static boolean tryChangeNodeQueue(IRReteNode node, REntryQueueType fromType, REntryQueueType toType)
			throws RException {

		IREntryQueue oldQueue = node.getEntryQueue();
		REntryQueueType oldType = oldQueue.getQueueType();
		if (oldType != fromType) {
			return false;
		}

		if (oldType == toType) {
			return true;
		}

		if (fromType == REntryQueueType.MULTI && toType == REntryQueueType.UNIQ) {

			for (IRConstraint1 cons : ReteUtil.getNodeConstraint1List(node)) {
				switch (cons.getConstraintName()) {

				// No need to change since the following constraints will make sure that entries
				// are uniq
				case A_Uniq:
				case A_Order_by:
					return false;
				}
			}
		}

		int oldSize = oldQueue.size();
		IREntryQueue newQueue = REntryFactory.createQueue(toType, node);

		boolean ignoreInvalidEntry = node.getChildNodes().size() == 0;

		if (oldSize > 0) {

			for (int i = 0; i < oldSize; ++i) {

				IRReteEntry entry = oldQueue.getEntryAt(i);

				// Should
				if (ignoreInvalidEntry && (entry == null || entry.isDroped())) {
					continue;
				}

				if (!newQueue.addEntry(entry)) {
					return false;
				}
			}
		}

		node.setEntryQueue(newQueue);
		return true;
	}

	public static String uniqName(IRList tree) throws RException {
		return _toUniq(tree, new HashMap<>(), true);
	}

	public static String uniqName(IRObject[] entry) throws RException {
		return _toUniq(entry, new HashMap<>(), true);
	}

	public static String uniqName(IRReteEntry entry) throws RException {
		return _toUniq(entry, new HashMap<>(), true);
	}

	public static int updateMask(RReteStatus status, int mask) {
		return status.getMask() | mask;
	}

	public static String varChangeNewName(String varName) {
		return varName + ".new";
	}

	public static String varChangeOldName(String varName) {
		return varName + ".old";
	}

	public static List<String> varList(IRList stmt) throws RException {

		HashSet<String> varList = new HashSet<>();
		fillVarList(stmt, varList);

		List<String> c = new LinkedList<>(varList);
		Collections.sort(c);
		return c;
	}

	public static List<String> varList(List<? extends IRList> stmtList) throws RException {

		HashSet<String> varList = new HashSet<>();
		for (IRList stmt : stmtList) {
			fillVarList(stmt, varList);
		}
		List<String> c = new LinkedList<>(varList);
		Collections.sort(c);
		return c;
	}
}
