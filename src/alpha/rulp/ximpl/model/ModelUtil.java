package alpha.rulp.ximpl.model;

import static alpha.rulp.lang.Constant.O_Nil;
import static alpha.rulp.rule.Constant.A_NOT_NULL;
import static alpha.rulp.rule.Constant.A_Type;
import static alpha.rulp.rule.Constant.A_Uniq;
import static alpha.rulp.rule.Constant.F_MBR_RULE_GROUP_NAMES;
import static alpha.rulp.rule.Constant.F_MBR_RULE_GROUP_PRE;
import static alpha.rulp.rule.Constant.O_CST_ADD_CONSTRAINT_TYPE;
import static alpha.rulp.rule.Constant.RETE_PRIORITY_DEFAULT;
import static alpha.rulp.rule.Constant.RETE_PRIORITY_DISABLED;
import static alpha.rulp.rule.Constant.RETE_PRIORITY_MAXIMUM;
import static alpha.rulp.rule.Constant.RETE_PRIORITY_PARTIAL_MAX;
import static alpha.rulp.rule.Constant.RETE_PRIORITY_PARTIAL_MIN;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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
import alpha.rulp.rule.IRRListener3;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.rule.IRRule;
import alpha.rulp.rule.IRWorker;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.constraint.IRConstraint1;
import alpha.rulp.ximpl.constraint.IRConstraint1Type;
import alpha.rulp.ximpl.factor.AbsRFactorAdapter;
import alpha.rulp.ximpl.model.XRSubNodeGraph.QuerySourceEntry;
import alpha.rulp.ximpl.model.XRSubNodeGraph.QuerySourceInfo;
import alpha.rulp.ximpl.node.IRNodeGraph;
import alpha.rulp.ximpl.node.RReteType;
import alpha.rulp.ximpl.node.XRRuleNode;

public class ModelUtil {

	public static interface IReteNodeVisitor {
		public boolean visit(IRReteNode node) throws RException;
	}

	static class RuleActionFactor extends AbsRFactorAdapter {

		private IRRListener3<IRList, IRRule, IRFrame> actioner;

		private IRRule rule;

		public RuleActionFactor(String factorName, IRRListener3<IRList, IRRule, IRFrame> actioner) {
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

	private static AtomicInteger anonymousRuleActionIndex = new AtomicInteger(0);

	public static boolean addConstraint(IRModel model, IRReteNode node, IRConstraint1 constraint) throws RException {

		IRInterpreter interpreter = model.getInterpreter();
		IRFrame frame = model.getFrame();

		switch (constraint.getConstraintName()) {
		case A_Type:
			IRConstraint1Type typeConstraint = (IRConstraint1Type) constraint;

			// $cst_type$:'(?node ?index ?type)
			interpreter.compute(frame,
					RulpFactory.createExpression(O_CST_ADD_CONSTRAINT_TYPE, model,
							RulpFactory.createString(RuleUtil.asNamedNode(node).getNamedName()),
							RulpFactory.createInteger(typeConstraint.getColumnIndex()),
							RType.toObject(typeConstraint.getColumnType())));
			break;

		case A_Uniq:
		case A_NOT_NULL:
		default:
			break;
		}

		return node.addConstraint1(constraint);
	}

	public static IRRule addRule(IRModel model, String ruleName, String condExpr,
			IRRListener3<IRList, IRRule, IRFrame> actioner) throws RException {

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

	public static void addWorker(IRModel model, IRList condList, IRWorker worker) throws RException {

		IRReteNode fromNode = model.getNodeGraph().addWorker(null, worker);
		IRReteNode toNode = model.findNode(condList);
		model.getNodeGraph().bindNode(fromNode, toNode);
	}

	public static IRSubNodeGraph buildSubNodeGroupForQuery(IRModel model, IRReteNode queryNode) throws RException {

		IRNodeGraph nodeGraph = model.getNodeGraph();

		XRSubNodeGraph subGraph = new XRSubNodeGraph(nodeGraph);

		boolean isRootMode = RReteType.isRootType(queryNode.getReteType());

		/******************************************************/
		// Build source graph
		/******************************************************/
		LinkedList<QuerySourceEntry> visitStack = new LinkedList<>();
		visitStack.add(new QuerySourceEntry(null, queryNode));
		Set<IRReteNode> visitedNodes = new HashSet<>();

		while (!visitStack.isEmpty()) {

			QuerySourceEntry entry = visitStack.pop();
			IRReteNode fromNode = entry.fromNode;
			IRReteNode sourceNode = entry.sourceNode;

			// ignore visited node
			if (visitedNodes.contains(sourceNode)) {
				continue;
			}

			visitedNodes.add(sourceNode);

			if (sourceNode.getPriority() <= RETE_PRIORITY_DISABLED) {
				continue;
			}

			if (!isRootMode && RReteType.isRootType(sourceNode.getReteType())) {
				continue;
			}

			int new_priority = RETE_PRIORITY_PARTIAL_MAX;
			if (sourceNode != queryNode) {
				new_priority = Math.min(sourceNode.getPriority(), fromNode.getPriority()) - 1;
				if (new_priority < RETE_PRIORITY_PARTIAL_MIN) {
					new_priority = RETE_PRIORITY_PARTIAL_MIN;
				}
			}

			subGraph.addNode(sourceNode, new_priority);

			if (sourceNode.getParentNodes() != null) {
				for (IRReteNode newSrcNode : sourceNode.getParentNodes()) {
					visitStack.add(new QuerySourceEntry(sourceNode, newSrcNode));
				}
			}

			for (IRReteNode newSrcNode : nodeGraph.getBindFromNodes(sourceNode)) {
				visitStack.add(new QuerySourceEntry(sourceNode, newSrcNode));
			}

			for (IRReteNode newSrcNode : nodeGraph.listSourceNodes(sourceNode)) {
				visitStack.add(new QuerySourceEntry(sourceNode, newSrcNode));
			}
		}

		ModelUtil.travelReteParentNodeByPostorder(queryNode, (node) -> {

			if (!subGraph.containNode(node)) {
				subGraph.addNode(node, RETE_PRIORITY_PARTIAL_MIN);
				visitStack.add(new QuerySourceEntry(null, node));
				visitedNodes.add(node);
			}

			return false;
		});

		return subGraph;
	}

	public static IRSubNodeGraph buildSubNodeGroupForRuleGroup(IRModel model, String ruleGroupName) throws RException {

		IRNodeGraph nodeGraph = model.getNodeGraph();
		XRSubNodeGraph subGraph = new XRSubNodeGraph(nodeGraph);

		IRList ruleList = ModelUtil.getRuleGroupRuleList(model, ruleGroupName);
		if (ruleList.size() == 0) {
			throw new RException("no rule found for group: " + ruleGroupName);
		}

		IRIterator<? extends IRObject> it = ruleList.iterator();
		while (it.hasNext()) {

			ModelUtil.travelReteParentNodeByPostorder(RuleUtil.asRule(it.next()), (node) -> {

				if (!subGraph.containNode(node) && node.getPriority() < RETE_PRIORITY_MAXIMUM
						&& node.getPriority() > RETE_PRIORITY_DISABLED) {
					subGraph.addNode(node, RETE_PRIORITY_DEFAULT);
				}

				return false;
			});
		}

		subGraph.disableAllOtherNodes(RETE_PRIORITY_DEFAULT, RETE_PRIORITY_DISABLED);

		for (IRReteNode node : subGraph.getAllNodes()) {
			model.addUpdateNode(node);
		}

		return subGraph;
	}

	public static void createModelVar(IRModel model, String varName, IRObject value) throws RException {

		IRVar var = RulpFactory.createVar(varName);
		if (value != null) {
			var.setValue(value);
		}

		RulpUtil.setMember(model, varName, var);
	}

	public static int getNodeMaxPriority(IRModel model) {

		int maxPriority = -1;

		for (IRReteNode node : model.getNodeGraph().getNodeMatrix().getAllNodes()) {

			if (node.getReteType() == RReteType.ROOT0) {
				continue;
			}

			int pirority = node.getPriority();
			if (maxPriority < pirority) {
				maxPriority = pirority;
			}
		}

		return maxPriority;
	}

	public static IRList getRuleGroupList(IRModel model) throws RException {

		IRMember mbr = model.getMember(F_MBR_RULE_GROUP_NAMES);

		if (mbr == null || mbr.getSubject() != model) {
			IRList ruleList = RulpFactory.createList();
			mbr = RulpFactory.createMember(model, F_MBR_RULE_GROUP_NAMES,
					RulpFactory.createVar(F_MBR_RULE_GROUP_NAMES, RulpFactory.createList()));
			mbr.setFinal(true);
			mbr.setInherit(false);
			mbr.setAccessType(RAccessType.PRIVATE);
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
			mbr.setFinal(true);
			mbr.setInherit(false);
			mbr.setAccessType(RAccessType.PRIVATE);
			model.setMember(innerGroupName, mbr);
			return ruleList;
		}

		return RulpUtil.asList(RulpUtil.asVar(mbr.getValue()).getValue());
	}

	public static int recalcuatePriority(IRModel model, IRReteNode node) throws RException {

		int priority = 0;

		for (IRRule rule : model.getNodeGraph().getRelatedRules(node)) {
			int rulePriority = rule.getPriority();
			if (rulePriority > priority) {
				priority = rulePriority;
			}
		}

		return priority;
	}

	public static void setRulePriority(IRRule rule, int priority) throws RException {

		if (priority < 0 || priority > RETE_PRIORITY_MAXIMUM) {
			throw new RException("Invalid priority: " + priority);
		}

		if (rule.getPriority() == priority) {
			return;
		}

		XRRuleNode ruleNode = (XRRuleNode) rule;
		ruleNode.setPriority(priority);

		IRModel model = ruleNode.getModel();

		/******************************************************/
		// Update all rule node priority
		/******************************************************/
		ModelUtil.travelReteParentNodeByPostorder(ruleNode, (node) -> {

			if (node.getReteType() != RReteType.ROOT0 && node != ruleNode) {
				int newPriority = ModelUtil.recalcuatePriority(model, node);
				node.setPriority(newPriority);
			}

			return false;
		});
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
