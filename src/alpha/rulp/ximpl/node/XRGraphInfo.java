package alpha.rulp.ximpl.node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.rule.IRRule;
import alpha.rulp.ximpl.action.ActionUtil;
import alpha.rulp.ximpl.action.IAction;
import alpha.rulp.ximpl.model.IGraphInfo;

public class XRGraphInfo implements IGraphInfo {

	public Map<IAction, List<IRList>> actionUniqStmtListMap = null;

	public List<IRReteNode> bindFromNodeList = null;

	public List<IRReteNode> bindToNodeList = null;

	public IRReteNode node;

	public List<IRRule> relatedRules = null;

	public int useCount = 0;

	public XRGraphInfo(IRReteNode node) {
		this.node = node;
	}

	public void addRule(IRRule rule) {

		if (relatedRules == null) {
			relatedRules = new LinkedList<>();
		}

		relatedRules.add(rule);
	}

	public void bind(XRGraphInfo toNode) {

		if (this.bindToNodeList == null) {
			this.bindToNodeList = new LinkedList<>();
		}

		if (toNode.bindFromNodeList == null) {
			toNode.bindFromNodeList = new LinkedList<>();
		}

		/*************************************/
		// Bind toNode <--- fromNode
		/*************************************/
		if (!this.bindToNodeList.contains(toNode.node)) {
			this.bindToNodeList.add(toNode.node);
		}

		/*************************************/
		// Bind fromNode ---> toNode
		/*************************************/
		if (!toNode.bindFromNodeList.contains(this.node)) {
			toNode.bindFromNodeList.add(this.node);
		}
	}

	public List<IRList> getRuleActionUniqStmt(IAction action, XRNodeGraph graph) throws RException {

		if (actionUniqStmtListMap == null) {
			actionUniqStmtListMap = new HashMap<>();
		}

		List<IRList> actionUniqStmtList = actionUniqStmtListMap.get(action);
		if (actionUniqStmtList == null) {

			List<String> uniqNames = ActionUtil.buildRelatedStmtUniqNames(action.getStmtExprList());
			if (uniqNames.isEmpty()) {
				actionUniqStmtList = Collections.emptyList();
			} else {
				actionUniqStmtList = new ArrayList<>();
				for (String uniqName : uniqNames) {
					actionUniqStmtList.add(graph._toList(uniqName));
				}
			}

			actionUniqStmtListMap.put(action, actionUniqStmtList);
		}

		return actionUniqStmtList;
	}

	public int getUseCount() {
		return this.useCount;
	}

	public boolean hasBindNode() {
		return bindFromNodeList != null && !bindFromNodeList.isEmpty();
	}

	public void incUseCount() {
		this.useCount++;
	}
}
