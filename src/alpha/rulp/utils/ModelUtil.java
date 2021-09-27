package alpha.rulp.utils;

import static alpha.rulp.rule.Constant.F_MBR_RULE_GROUP_NAMES;
import static alpha.rulp.rule.Constant.F_MBR_RULE_GROUP_PRE;
import static alpha.rulp.rule.Constant.O_CST_ADD_CONSTRAINT_TYPE;
import static alpha.rulp.rule.Constant.RETE_PRIORITY_DEFAULT;
import static alpha.rulp.rule.Constant.RETE_PRIORITY_DISABLED;
import static alpha.rulp.rule.Constant.RETE_PRIORITY_MAXIMUM;

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
import alpha.rulp.rule.IRRule;
import alpha.rulp.rule.IRWorker;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.utils.RuleUtil.RuleActionFactor;
import alpha.rulp.ximpl.constraint.IRConstraint1;
import alpha.rulp.ximpl.constraint.IRConstraint1Type;
import alpha.rulp.ximpl.model.XRSubNodeGraph;
import alpha.rulp.ximpl.node.IRReteNode;
import alpha.rulp.ximpl.node.RReteType;
import alpha.rulp.ximpl.node.XRRuleNode;

public class ModelUtil {

	public static interface IReteNodeVisitor {
		public boolean visit(IRReteNode node) throws RException;
	}

	private static AtomicInteger anonymousRuleActionIndex = new AtomicInteger(0);

	public static XRSubNodeGraph activeRuleGroup(IRModel model, String ruleGroupName) throws RException {

		XRSubNodeGraph subGraph = new XRSubNodeGraph(model.getNodeGraph());

		IRList ruleList = ModelUtil.getRuleGroupRuleList(model, ruleGroupName);
		if (ruleList.size() == 0) {
			throw new RException("no rule found for group: " + ruleGroupName);
		}

		IRIterator<? extends IRObject> it = ruleList.iterator();
		while (it.hasNext()) {
			subGraph.addRule(RuleUtil.asRule(it.next()), RETE_PRIORITY_DEFAULT);
		}

		subGraph.disableAllOtherNodes(RETE_PRIORITY_DEFAULT, RETE_PRIORITY_DISABLED);

		for (IRReteNode node : subGraph.getAllNodes()) {
			model.addUpdateNode(node);
		}

		return subGraph;
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

		if (model.getModelFrame().getObject(uniqName) != null) {
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
		ArrayList<IRObject> rules = RulpUtil.toArray(getRuleGroupRuleList(model, groupName));
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

	public static boolean addConstraint(IRModel model, IRReteNode node, IRConstraint1 constraint) throws RException {

		IRInterpreter interpreter = model.getInterpreter();
		IRFrame frame = model.getModelFrame();

		switch (constraint.getConstraintType()) {
		case TYPE:
			IRConstraint1Type typeConstraint = (IRConstraint1Type) constraint;
			interpreter.compute(frame,
					RulpFactory.createExpression(O_CST_ADD_CONSTRAINT_TYPE, model,
							RulpFactory.createString(RuleUtil.asNamedNode(node).getNamedName()),
							RulpFactory.createInteger(typeConstraint.getColumnIndex()),
							RType.toObject(typeConstraint.getColumnType())));
			break;
			
		case UNIQ:
			
		default:
			break;
		}

		return node.addConstraint(constraint);
	}
}
