package alpha.rulp.utils;

import static alpha.rulp.lang.Constant.O_False;
import static alpha.rulp.lang.Constant.O_Nil;
import static alpha.rulp.lang.Constant.P_FINAL;
import static alpha.rulp.rule.Constant.A_DEFAULT_MODEL;
import static alpha.rulp.rule.Constant.A_M_TRACE;
import static alpha.rulp.rule.Constant.F_MBR_RULE_GROUP_NAMES;
import static alpha.rulp.rule.Constant.F_MBR_RULE_GROUP_PRE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRMember;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.IRVar;
import alpha.rulp.lang.RAccessType;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RType;
import alpha.rulp.rule.IRModel;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.rule.IRRule;
import alpha.rulp.rule.IRWorker;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.runtime.IRListener3;
import alpha.rulp.runtime.IRParser;
import alpha.rulp.ximpl.bs.BSFactory;
import alpha.rulp.ximpl.constraint.ConstraintFactory;
import alpha.rulp.ximpl.constraint.IRConstraint1Uniq;
import alpha.rulp.ximpl.entry.IREntryIteratorBuilder;
import alpha.rulp.ximpl.entry.IREntryQueueUniq;
import alpha.rulp.ximpl.entry.IRReteEntry;
import alpha.rulp.ximpl.factor.AbsAtomFactorAdapter;
import alpha.rulp.ximpl.node.IRBetaNode;
import alpha.rulp.ximpl.node.IRNamedNode;
import alpha.rulp.ximpl.node.IRNodeGraph;
import alpha.rulp.ximpl.node.RReteType;
import alpha.rulp.ximpl.node.SourceNode;
import alpha.rulp.ximpl.node.XRNodeNamed;

public class RuleUtil {

	public static interface IReteNodeVisitor {
		public boolean visit(IRReteNode node) throws RException;
	}

	static class MultiStmtItrator implements IRIterator<IRList> {

		LinkedList<IRIterator<? extends IRObject>> iteratorStack = new LinkedList<>();
		IRList nextStmt = null;

		public MultiStmtItrator(IRIterator<? extends IRObject> mainIterator) {
			super();
			this.iteratorStack.add(mainIterator);
		}

		@Override
		public boolean hasNext() throws RException {

			while (nextStmt == null && !iteratorStack.isEmpty()) {

				IRIterator<? extends IRObject> topIterator = iteratorStack.getLast();
				if (!topIterator.hasNext()) {
					iteratorStack.removeLast();
					continue;
				}

				IRObject nextObj = topIterator.next();
				if (ReteUtil.isReteStmt(nextObj)) {
					nextStmt = (IRList) nextObj;
					break;
				}

				if (nextObj.getType() != RType.LIST) {
					throw new RException("not list: " + nextObj);
				}

				IRIterator<? extends IRObject> subIterator = ((IRList) nextObj).iterator();
				iteratorStack.addLast(subIterator);
			}

			return nextStmt != null;
		}

		@Override
		public IRList next() throws RException {

			if (!hasNext()) {
				return null;
			}

			IRList rt = nextStmt;
			nextStmt = null;
			return rt;
		}

	}

	static class RelocatedEntry {

		public int maxStmtIndex = -1;

		public ArrayList<Integer> relocatedStmtIndexs = null;

		public int relocatePos = -1;

		public IRReteNode rootNode;

		public ArrayList<Integer> stmtIndexs = new ArrayList<>();

		public void addIndex(int index) {
			stmtIndexs.add(index);
			maxStmtIndex = Math.max(maxStmtIndex, index);
		}
	}

	static class RuleActionFactor extends AbsAtomFactorAdapter {

		private IRListener3<IRList, IRRule, IRFrame> actioner;

		private IRRule rule;

		public RuleActionFactor(String factorName, IRListener3<IRList, IRRule, IRFrame> actioner) {
			super(factorName);
			this.actioner = actioner;
		}

		@Override
		public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

			IRList list = (IRList) interpreter.compute(frame, RulpFactory.createList(args.listIterator(1)));
			actioner.doAction(list, rule, frame);
			return O_Nil;
		}

		public void setRule(IRRule rule) {
			this.rule = rule;
		}
	}

	static class SingleStmtItrator implements IRIterator<IRList> {

		IRList stmt;

		public SingleStmtItrator(IRList stmt) {
			super();
			this.stmt = stmt;
		}

		@Override
		public boolean hasNext() throws RException {
			return stmt != null;
		}

		@Override
		public IRList next() throws RException {

			if (!hasNext()) {
				return null;
			}

			IRList rt = stmt;
			stmt = null;
			return rt;
		}
	}

	private static AtomicInteger anonymousRuleActionIndex = new AtomicInteger(0);

	static IRParser parser = null;

	static StaticVar varTraceModel = new StaticVar(A_M_TRACE, O_False);

	public static IRNamedNode addNodeFunc(IRModel model, IRList funcExprList) throws RException {

		/**************************************************/
		// Check node
		/**************************************************/
		String namedName = funcExprList.getNamedName();
		if (namedName == null) {
			throw new RException(String.format("need named name: %s", funcExprList));
		}

		int entryLen = funcExprList.size();

		IRNamedNode node = (IRNamedNode) model.getNodeGraph().findRootNode(namedName, -1);
		if (node != null) {

			if (node.getEntryLength() != entryLen) {
				throw new RException(
						String.format("entry lengh not match: actual=%d, expect=%s", node.getEntryLength(), entryLen));
			}

			if (node.getFuncEntry() != null) {
				throw new RException(String.format("func entry already exist in node: %s", node));
			}
		}

		/**************************************************/
		// Check func entry
		/**************************************************/
		IRObject[] funcEntry = new IRObject[entryLen];
		Set<String> nodeVarNames = new HashSet<>();

		for (int i = 0; i < entryLen; ++i) {

			IRObject obj = funcExprList.get(i);
			if (RulpUtil.isVarAtom(obj)) {

				String varName = obj.asString();
				if (nodeVarNames.contains(varName)) {
					throw new RException(String.format("duplicate var name: %s", obj));
				}

				nodeVarNames.add(varName);

				funcEntry[i] = obj;

			} else if (obj.getType() == RType.EXPR) {

				funcEntry[i] = obj;

			} else {
				throw new RException(String.format("unsupport object: %s", obj));
			}
		}

		int varCount = nodeVarNames.size();
		if (varCount == 0) {
			throw new RException(String.format("no var found: %s", funcExprList));
		}

		if (varCount == entryLen) {
			throw new RException(String.format("no expr found: %s", funcExprList));
		}

		/**************************************************/
		// Create uniq index
		/**************************************************/
		int[] columnIndexs = new int[varCount];
		{
			int idx = 0;
			for (int i = 0; i < entryLen; ++i) {
				if (funcEntry[i].getType() == RType.ATOM) {
					columnIndexs[idx++] = i;
				}
			}
		}

		/**************************************************/
		// Create node if not exist
		/**************************************************/
		if (node == null) {
			node = (IRNamedNode) model.getNodeGraph().createNodeRoot(namedName, entryLen);
			if (node == null) {
				throw new RException(String.format("Fail to create named node: %s, len=%d", namedName, entryLen));
			}
		}

		/**************************************************/
		// Create uniq index
		/**************************************************/
		XRNodeNamed implNode = (XRNodeNamed) node;
		IRConstraint1Uniq cons = ConstraintFactory.uniq(columnIndexs);
		implNode.setFuncUniqConstraint(cons);
		model.getNodeGraph().addConstraint(implNode, cons);

		/**************************************************/
		// Update func entry
		/**************************************************/
		implNode.setFuncEntry(funcEntry);
		
		return implNode;
	}

	public static IRRule addRule(IRModel model, String ruleName, String condExpr,
			IRListener3<IRList, IRRule, IRFrame> actioner) throws RException {

		IRList condList = RuleUtil.toCondList(condExpr);

		String uniqName;
		if (ruleName == null) {
			uniqName = "RAF-" + model.getModelName() + "-"
					+ String.format("A%03d", anonymousRuleActionIndex.getAndIncrement()) + "";
		} else {
			uniqName = "RAF-" + model.getModelName() + "-" + ruleName + "";
		}

		if (model.getFrame().getObject(uniqName) != null) {
			throw new RException("duplicated uniq name: " + uniqName);
		}

		RuleActionFactor ruleFactor = new RuleActionFactor(uniqName, actioner);

		ArrayList<IRObject> actionList = new ArrayList<>();
		actionList.add(ruleFactor);
		actionList.addAll(ReteUtil.buildVarList(condList));

		IRRule rule = model.addRule(ruleName, condList,
				RulpFactory.createList(RulpFactory.createExpression(actionList)));

		ruleFactor.setRule(rule);

		return rule;
	}

	public static void addRuleToGroup(IRModel model, IRRule rule, String groupName) throws RException {

		/*****************************************************/
		// Add new group
		/*****************************************************/
		List<String> groupNames = RulpUtil.toStringList(getRuleGroupList(model));
		if (!groupNames.contains(groupName)) {
			IRVar var = RulpUtil.asVar(model.getMember(F_MBR_RULE_GROUP_NAMES).getValue());
			ArrayList<String> newNames = new ArrayList<>(groupNames);
			newNames.add(groupName);
			var.setValue(RulpFactory.createListOfString(newNames));
		}

		/*****************************************************/
		// Add rule to group
		/*****************************************************/
		List<IRObject> rules = RulpUtil.toArray(getRuleGroupRuleList(model, groupName));
		if (!rules.contains(rule)) {
			IRVar var = RulpUtil.asVar(model.getMember(F_MBR_RULE_GROUP_PRE + groupName).getValue());
			ArrayList<IRObject> newRules = new ArrayList<>(rules);
			newRules.add(rule);
			var.setValue(RulpFactory.createList(newRules));
		}
	}

	public static int addStatements(IRModel model, IRIterator<? extends IRList> stmtIterator) throws RException {

		int updateCount = 0;
		while (stmtIterator.hasNext()) {
			if (model.addStatement(stmtIterator.next())) {
				updateCount++;
			}
		}

		return updateCount;
	}

	public static int addStatements(IRModel model, Iterator<? extends IRList> stmtIterator) throws RException {

		int updateCount = 0;
		while (stmtIterator.hasNext()) {
			if (model.addStatement(stmtIterator.next())) {
				updateCount++;
			}
		}

		return updateCount;
	}

	public static void addWorker(IRModel model, IRList condList, IRWorker worker) throws RException {

		IRReteNode fromNode = model.getNodeGraph().createWorkNode(null, worker);
		IRReteNode toNode = model.findNode(condList);
		model.getNodeGraph().bindNode(fromNode, toNode);
	}

	public static IRBetaNode asBetaNode(IRReteNode node) throws RException {

		if (!RReteType.isBetaType(node.getReteType())) {
			throw new RException("Can't convert to beta node: " + node);
		}

		return (IRBetaNode) node;
	}

	public static IRModel asModel(IRObject obj) throws RException {

		if (!(obj instanceof IRModel)) {
			throw new RException("Can't convert to model: " + obj);
		}

		return (IRModel) obj;
	}

	public static IRReteNode asNamedNode(IRReteNode node) throws RException {

		if (node.getReteType() != RReteType.NAME0) {
			throw new RException("Can't convert to named node: " + node);
		}

		return node;
	}

	public static IRReteNode asNode(IRObject obj) throws RException {

		if (!(obj instanceof IRReteNode)) {
			throw new RException("Can't convert to node: " + obj);
		}

		return (IRReteNode) obj;
	}

	public static IRRule asRule(IRObject obj) throws RException {

		if (!(obj instanceof IRRule)) {
			throw new RException("Can't convert to rule: " + obj);
		}

		return (IRRule) obj;
	}

//	public static List<IRObject> compute(IRModel model, String input) throws RException {
//
//		IRInterpreter interpreter = model.getInterpreter();
//		IRFrame modelFrame = model.getFrame();
//		IRParser parser = interpreter.getParser();
//
//		List<IRObject> objs;
//
//		synchronized (parser) {
//			objs = parser.parse(input);
//		}
//
//		try {
//
//			List<IRObject> rsts = new LinkedList<>();
//
//			for (IRObject obj : objs) {
//				rsts.add(interpreter.compute(modelFrame, obj));
//			}
//
//			return rsts;
//
//		} catch (RIException e) {
//
//			if (XRInterpreter.TRACE) {
//				e.printStackTrace();
//			}
//
//			throw new RException("Unhandled internal exception: " + e.toString());
//
//		} catch (RError e) {
//
//			if (XRInterpreter.TRACE) {
//				e.printStackTrace();
//			}
//
//			RException newExp = new RException("" + e.getError());
//
//			for (String addMsg : e.getAdditionalMessages()) {
//				newExp.addMessage(addMsg);
//			}
//
//			throw newExp;
//		}
//	}

	public static IRVar createModelVar(IRModel model, String varName, IRObject value) throws RException {

		IRVar var = RulpFactory.createVar(varName);
		if (value != null) {
			var.setValue(value);
		}

		RulpUtil.setMember(model, varName, var);

		return var;
	}

	public static boolean equal(String a, String b) throws RException {

		if (a == null) {
			return b == null;
		}

		return b != null && a.equals(b);
	}

	public static int executeRule(IRRule rule, List<IRList> stmts) throws RException {

		final IRModel model = rule.getModel();
		final IRNodeGraph graph = rule.getModel().getNodeGraph();

		ArrayList<RelocatedEntry> relocatedEntryList = new ArrayList<>();
		Map<IRReteNode, RelocatedEntry> relocatedEntryMap = new HashMap<>();

		/*************************************************/
		// Relocate stmts
		/*************************************************/
		for (IRList stmt : stmts) {

			IRReteNode rootNode = graph.findRootNode(stmt.getNamedName(), stmt.size());
			if (!rule.getAllNodes().contains(rootNode)) {
				continue;
			}

			IREntryQueueUniq uniqQueue = ((IREntryQueueUniq) rootNode.getEntryQueue());
			String uniqName = ReteUtil.uniqName(stmt);

			int stmtIndex = uniqQueue.getStmtIndex(uniqName);
			if (stmtIndex == -1) {
				if (!model.addStatement(stmt)) {
					continue;
				}

				stmtIndex = uniqQueue.getStmtIndex(uniqName);
				if (stmtIndex == -1) {
					continue;
				}
			}

			RelocatedEntry se = relocatedEntryMap.get(rootNode);
			if (se == null) {
				se = new RelocatedEntry();
				se.rootNode = rootNode;
				relocatedEntryMap.put(rootNode, se);
				relocatedEntryList.add(se);
			}

			se.addIndex(stmtIndex);
		}

		/*************************************************/
		// Check relocated position
		/*************************************************/
		for (RelocatedEntry se : relocatedEntryList) {

			// Get the max visit index
			int childMaxVisitIndex = ReteUtil.findChildMaxVisitIndex(se.rootNode, null);

			// All statements are already passed to child nodes
			if (childMaxVisitIndex > se.maxStmtIndex) {
				continue;
			}

			for (int index : se.stmtIndexs) {
				if (index >= childMaxVisitIndex) {
					if (se.relocatedStmtIndexs == null) {
						se.relocatedStmtIndexs = new ArrayList<>();
					}
					se.relocatedStmtIndexs.add(index);
				}
			}

			if (se.relocatedStmtIndexs != null) {

				BSFactory.incBscOpRelocate(se.relocatedStmtIndexs.size());

				se.relocatePos = ((IREntryQueueUniq) se.rootNode.getEntryQueue()).relocate(childMaxVisitIndex,
						se.relocatedStmtIndexs);
			}
		}

		/*************************************************/
		// Check all root nodes
		/*************************************************/
		ArrayList<IRReteNode> rootNodes = new ArrayList<>();
		for (IRReteNode node : rule.getAllNodes()) {

			if (!RReteType.isRootType(node.getReteType())) {
				continue;
			}

			rootNodes.add(node);

			RelocatedEntry se = relocatedEntryMap.get(node);
			if (se != null) {

				if (se.relocatePos != -1) {
					se.rootNode.getEntryQueue().setRelocateSize(se.relocatePos);
					continue;
				}

				if (se.maxStmtIndex != -1) {
					se.rootNode.getEntryQueue().setRelocateSize(se.maxStmtIndex + 1);
					continue;
				}
			}

			se.rootNode.getEntryQueue().setRelocateSize(0);
		}

		AtomicInteger rc = new AtomicInteger(0);
		/*************************************************/
		// Process
		/*************************************************/
		try {

			RuleUtil.travelReteParentNodeByPostorder(rule, (_node) -> {

				if (!RReteType.isRootType(_node.getReteType())) {

					int update = model.execute(_node, -1);
					if (update > 0) {
						rc.incrementAndGet();
					}
				}

				return false;
			});

		} finally {

			for (IRReteNode node : rootNodes) {
				node.getEntryQueue().setRelocateSize(-1);
			}
		}

		return rc.get();
	}

//	public static int getNodeMaxPriority(IRModel model) {
//
//		int maxPriority = -1;
//
//		for (IRReteNode node : model.getNodeGraph().getNodeMatrix().getAllNodes()) {
//
//			if (node.getReteType() == RReteType.ROOT0) {
//				continue;
//			}
//
//			int pirority = node.getPriority();
//			if (maxPriority < pirority) {
//				maxPriority = pirority;
//			}
//		}
//
//		return maxPriority;
//	}

	public static List<IRReteNode> getAllParentNodes(IRReteNode node) throws RException {

		List<IRReteNode> nodes = new ArrayList<>();

		RuleUtil.travelReteParentNodeByPostorder(node, (_node) -> {
			if (!nodes.contains(_node)) {
				nodes.add(_node);
			}

			return false;
		});

		return nodes;
	}

	public static IRModel getDefaultModel(IRFrame frame) throws RException {

		IRObject mobj = frame.getObject(A_DEFAULT_MODEL);
		if (mobj == null) {
			return null;
		}

		return RuleUtil.asModel(mobj);
	}

	public static IRParser getParser() {
		if (parser == null) {
			parser = RulpFactory.createParser();
		}
		return parser;
	}

	public static IRList getRuleGroupList(IRModel model) throws RException {

		IRMember mbr = model.getMember(F_MBR_RULE_GROUP_NAMES);

		if (mbr == null || mbr.getSubject() != model) {
			IRList ruleList = RulpFactory.createList();
			mbr = RulpFactory.createMember(model, F_MBR_RULE_GROUP_NAMES,
					RulpFactory.createVar(F_MBR_RULE_GROUP_NAMES, RulpFactory.createList()));
			mbr.setAccessType(RAccessType.PRIVATE);
			mbr.setProperty(P_FINAL);
			model.setMember(F_MBR_RULE_GROUP_NAMES, mbr);
			return ruleList;

		}

		return RulpUtil.asList(RulpUtil.asVar(mbr.getValue()).getValue());
	}

	public static IRList getRuleGroupRuleList(IRModel model, String groupName) throws RException {

		String innerGroupName = F_MBR_RULE_GROUP_PRE + groupName;

		IRMember mbr = model.getMember(innerGroupName);

		if (mbr == null || mbr.getSubject() != model) {
			IRList ruleList = RulpFactory.createList();
			mbr = RulpFactory.createMember(model, innerGroupName, RulpFactory.createVar(innerGroupName, ruleList));
			mbr.setProperty(P_FINAL);
			mbr.setAccessType(RAccessType.PRIVATE);
			model.setMember(innerGroupName, mbr);
			return ruleList;
		}

		return RulpUtil.asList(RulpUtil.asVar(mbr.getValue()).getValue());
	}

	public static void init(IRFrame frame) throws RException {
		varTraceModel.init(frame);
	}

//	public static Collection<SourceNode> listMatchCondition(IRList stmt, IRRule rule) throws RException {
//
//		if (!ReteUtil.isReteStmtNoVar(stmt)) {
//			throw new RException("unsupport var: " + stmt);
//		}
//
//		for (IAction action : rule.getActionList()) {
//
//		}
//		return null;
//
//	}

	public static void invokeRule(IRModel model, String ruleName) throws RException {
		model.getNodeGraph().findRule(ruleName).start(-1, -1);
	}

	public static boolean isModelTrace() throws RException {
		return varTraceModel.getBoolValue();
	}

	public static List<IRReteNode> listNodes(IRNodeGraph graph, RReteType... types) {

		ArrayList<IRReteNode> nodes = new ArrayList<>();

		for (RReteType type : types) {
			nodes.addAll(graph.listNodes(type));
		}

		return nodes;
	}

	public static Collection<SourceNode> listSource(IRModel model, IRReteNode node) throws RException {

		switch (node.getReteType()) {
		case ALPH0:
		case ROOT0:
		case NAME0:

			List<IRObject> objs = model.getInterpreter().getParser().parse(node.getUniqName());
			if (objs.size() != 1) {
				throw new RException("invalid uniqName: " + node.getUniqName());
			}

			return model.getNodeGraph().listSourceNodes(RulpUtil.asList(objs.get(0)));

		default:
			return Collections.emptySet();
		}
	}

	public static Collection<IRRule> listSourceNodes(IRModel model, IRList condList) throws RException {

		Set<IRRule> nodes = new HashSet<>();
		for (SourceNode sn : model.getNodeGraph().listSourceNodes(condList)) {
			nodes.add(sn.rule);
		}

		return nodes;
	}

	public static Collection<IRRule> listSourceNodes(IRModel model, IRReteNode node) throws RException {

		Set<IRRule> nodes = new HashSet<>();
		for (SourceNode sn : listSource(model, node)) {
			nodes.add(sn.rule);
		}

		return nodes;
	}

	public static List<? extends IRList> listStatements(IRModel model) throws RException {
		return listStatements(model, null, 0, 0, false, null);
	}

	public static List<? extends IRList> listStatements(IRModel model, IRList filter) throws RException {
		return listStatements(model, filter, 0, 0, false, null);
	}

	public static List<? extends IRList> listStatements(IRModel model, IRList filter, int statusMask, int limit)
			throws RException {
		return listStatements(model, filter, statusMask, limit, false, null);
	}

	public static List<? extends IRList> listStatements(IRModel model, IRList filter, int statusMask, int limit,
			boolean reverse, IREntryIteratorBuilder builder) throws RException {

		ArrayList<IRReteEntry> list = new ArrayList<>();

		model.listStatements(filter, statusMask, limit, reverse, builder, (entry) -> {
			list.add(entry);
			return true;
		});

		return list;
	}

	public static int recalcuatePriority(IRModel model, IRReteNode node) throws RException {

		int priority = 0;

		for (IRRule rule : model.getNodeGraph().listRelatedRules(node)) {
			int rulePriority = rule.getPriority();
			if (rulePriority > priority) {
				priority = rulePriority;
			}
		}

		return priority;
	}

	public static void removeDefaultModel(IRFrame frame) throws RException {
		frame.removeEntry(A_DEFAULT_MODEL);
	}

	public static void reset() {
		parser = null;
	}

//	public static void changeNode(boolean trace) throws RException {
//
//	}

	public static void setDefaultModel(IRFrame frame, IRModel model) throws RException {
		frame.setEntry(A_DEFAULT_MODEL, model);
	}

	public static void setModelTrace(boolean trace) throws RException {
		varTraceModel.setBoolValue(trace);
	}

	public static IRList toCondList(IRObject obj) throws RException {

		// '(a b c)
		if (ReteUtil.isReteStmt(obj)) {
			return RulpFactory.createList(obj);
		}
		// ('(a b c))
		else if (obj.getType() == RType.EXPR) {
			return RulpUtil.asExpression(obj);
		} else {
			return RulpFactory.createExpression(RulpUtil.asList(obj).iterator());
		}
	}

//	public static List<Integer> toList(int[] ids) {
//
//		if (ids == null || ids.length == 0) {
//			return Collections.emptyList();
//		}
//
//		List<Integer> list = new ArrayList<Integer>();
//		for (int id : ids) {
//			list.add(id);
//		}
//
//		return list;
//	}

	public static IRList toCondList(String cond) throws RException {

		List<IRObject> stmts = getParser().parse(cond);
		if (stmts.size() == 1) {
			return toCondList(stmts.get(0));
		} else {
			return toCondList(RulpFactory.createList(stmts));
		}

	}

	public static IRList toStmtFilter(String filter) throws RException {

		List<IRObject> stmts = getParser().parse(filter);
		if (stmts.size() != 1) {
			throw new RException("invalid stmt filer: " + filter);
		}

		return RulpUtil.asList(stmts.get(0));
	}

	public static IRIterator<? extends IRList> toStmtList(IRIterator<? extends IRObject> stmtIterator)
			throws RException {
		return new MultiStmtItrator(stmtIterator);
	}

	public static IRIterator<? extends IRList> toStmtList(IRObject obj) throws RException {

		if (obj.getType() != RType.LIST) {
			throw new RException("not list: " + obj);
		}

		IRList list = (IRList) obj;

		if (ReteUtil.isReteStmt(list)) {
			return new SingleStmtItrator(list);
		}

		return new MultiStmtItrator(list.iterator());
	}

	public static IRIterator<? extends IRList> toStmtList(String stmts) throws RException {
		return RuleUtil.toStmtList(RulpFactory.createList(getParser().parse(stmts)));
	}

	public static IRReteNode travelReteParentNodeByPostorder(IRReteNode node, IReteNodeVisitor visitor)
			throws RException {

		LinkedList<IRReteNode> queryStack = new LinkedList<>();
		Set<IRReteNode> inStack = new HashSet<>();
		Set<IRReteNode> expendedNodes = new HashSet<>();
		queryStack.add(node);
		inStack.add(node);

		/******************************************************/
		// Post order
		/******************************************************/
		while (!queryStack.isEmpty()) {

			IRReteNode topNode = queryStack.getLast();
			if (!expendedNodes.contains(topNode)) {

				if (topNode.getParentNodes() != null) {
					for (IRReteNode parent : topNode.getParentNodes()) {
						if (!inStack.contains(parent)) {
							queryStack.add(parent);
							inStack.add(parent);
						}
					}
				}

				expendedNodes.add(topNode);

			} else {

				if (visitor.visit(topNode)) {
					return topNode;
				}

				queryStack.removeLast();
			}
		}

		return null;
	}

}
