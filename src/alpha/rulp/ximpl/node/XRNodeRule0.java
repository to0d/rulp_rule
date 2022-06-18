package alpha.rulp.ximpl.node;

import static alpha.rulp.rule.Constant.A_Type;
import static alpha.rulp.rule.Constant.RETE_PRIORITY_MAXIMUM;
import static alpha.rulp.rule.RReteStatus.REMOVE;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import alpha.rulp.lang.IRError;
import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.IRVar;
import alpha.rulp.lang.RError;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.rule.IRRule;
import alpha.rulp.rule.IRRuleCounter;
import alpha.rulp.rule.RReteStatus;
import alpha.rulp.rule.RRunState;
import alpha.rulp.runtime.IRListener1;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.utils.XRRListener1Adapter;
import alpha.rulp.ximpl.action.IAction;
import alpha.rulp.ximpl.constraint.IRConstraint1;
import alpha.rulp.ximpl.constraint.IRConstraint1Expr;
import alpha.rulp.ximpl.entry.IREntryQueue;
import alpha.rulp.ximpl.entry.IREntryQueue.IREntryCounter;
import alpha.rulp.ximpl.entry.IRReteEntry;
import alpha.rulp.ximpl.model.IReteNodeMatrix;
import alpha.rulp.ximpl.model.XRNodeOrderedUpdater;

public class XRNodeRule0 extends XRNodeRete1 implements IRRule {

	static class RuleNodeMatrix implements IReteNodeMatrix {

		private Map<RReteType, List<IRReteNode>> nodeListMap = null;

		private XRNodeRule0 ruleNode;

		public RuleNodeMatrix(XRNodeRule0 ruleNode) {
			super();
			this.ruleNode = ruleNode;
		}

		@Override
		public List<? extends IRReteNode> getAllNodes() {
			return ruleNode.getAllNodes();
		}

		@Override
		public IRModel getModel() {
			return ruleNode.getModel();
		}

		@Override
		public List<? extends IRReteNode> getNodeList(RReteType reteType) {

			if (nodeListMap == null) {

				nodeListMap = new HashMap<>();

				for (IRReteNode node : getAllNodes()) {
					RReteType type = node.getReteType();
					List<IRReteNode> nodeList = nodeListMap.get(type);
					if (nodeList == null) {
						nodeList = new LinkedList<>();
						nodeListMap.put(type, nodeList);
					}
					nodeList.add(node);
				}
			}

			List<IRReteNode> nodeList = nodeListMap.get(reteType);
			if (nodeList == null) {
				nodeList = Collections.<IRReteNode>emptyList();
				nodeListMap.put(reteType, nodeList);
			}

			return nodeList;
		}

	}

	protected LinkedList<IAction> actionList = new LinkedList<>();

	protected LinkedList<IRExpr> actionStmtList = null;

	protected List<IRReteNode> allReteNodes = new LinkedList<>();

//	protected List<IRExpr> indexExprList = new LinkedList<>();

	protected IRError lastError;

	protected IRReteEntry lastValueEntry;

	protected LinkedList<IRList> matchStmtList = new LinkedList<>();

	protected int nodeFailedCount = 0;

	protected RuleNodeMatrix nodeMatrix = null;

	protected XRNodeOrderedUpdater nodeUpdater = null;

	protected String ruleDecription;

	protected XRRListener1Adapter<IRRule> ruleExecutedListenerDispatcher = new XRRListener1Adapter<>();

	protected XRRListener1Adapter<IRRule> ruleFailedListenerDispatcher = new XRRListener1Adapter<>();

	protected IRVar[] ruleVars = null;

	public XRNodeRule0(String instanceName) {
		super(instanceName);
	}

	@Override
	protected IRFrame _createNodeFrame() throws RException {

		IRFrame ruleFrame = super._createNodeFrame();

		Set<String> indexVarNames = new HashSet<>();

		/***********************************************************/
		// Check ?1 ?2 variables in action list
		/***********************************************************/
		for (IRExpr expr : this.getActionStmtList()) {
			for (IRObject varObj : ReteUtil.buildVarList(expr)) {
				String varName = RulpUtil.asAtom(varObj).getName();
				if (ReteUtil.isIndexVarName(varName)) {
					indexVarNames.add(varName);
				}
			}
		}

		/***********************************************************/
		// Check ?1 ?2 variables in index expression
		/***********************************************************/
		if (this.getConstraint1Count() > 0) {
			for (IRConstraint1 constraint : this.constraint1List) {
				if (constraint.getConstraintName().equals(A_Type)) {
					IRConstraint1Expr typeCons = (IRConstraint1Expr) constraint;
					for (IRObject varObj : ReteUtil.buildVarList(typeCons.getExpr())) {
						String varName = RulpUtil.asAtom(varObj).getName();
						if (ReteUtil.isIndexVarName(varName)) {
							indexVarNames.add(varName);
						}
					}
				}
			}
		}

		/***********************************************************/
		// Add special vars, like ?0 ?1
		/***********************************************************/
		if (!indexVarNames.isEmpty()) {

			for (String indexVarName : indexVarNames) {

				int index = Integer.valueOf(indexVarName.substring(1));
				if (index < 0 || index >= matchStmtList.size()) {
					throw new RException("Invalid index var found: " + indexVarName);
				}

				RulpUtil.addVar(ruleFrame, indexVarName).setValue(matchStmtList.get(index));
			}
		}

		/***********************************************************/
		// Set default model
		/***********************************************************/
		RuleUtil.setDefaultModel(ruleFrame, this.getModel());

		return ruleFrame;
	}

	protected boolean _match(IRReteEntry entry) throws RException {

		if (entry == null || entry.getStatus() == REMOVE) {
			return false;
		}

		if (this.getConstraint1Count() == 0) {
			return true;
		}

		IRVar[] _vars = getVars();

		/******************************************************/
		// Update variable value
		/******************************************************/
		for (int i = 0; i < entry.size(); ++i) {
			IRVar var = _vars[i];
			if (var != null) {
				var.setValue(entry.get(i));
			}
		}

		return _testConstraint1(entry);
	}

	public void addNode(IRReteNode node) {
		this.allReteNodes.add(node);
	}

	@Override
	public void addRuleExecutedListener(IRListener1<IRRule> listener) {
		ruleExecutedListenerDispatcher.addListener(listener);
	}

	@Override
	public void addRuleFailedListener(IRListener1<IRRule> listener) {
		ruleFailedListenerDispatcher.addListener(listener);
	}

	@Override
	public String asString() {
		return this.uniqName;
	}

	@Override
	public List<IAction> getActionList() {
		return actionList;
	}

	@Override
	public List<IRExpr> getActionStmtList() {

		if (actionStmtList == null) {
			actionStmtList = new LinkedList<>();

			for (IAction action : actionList) {
				actionStmtList.add(action.getExpr());
			}
		}

		return actionStmtList;
	}

	@Override
	public List<? extends IRReteNode> getAllNodes() {
		return allReteNodes;
	}

	@Override
	public IRRuleCounter getCounter() {

		return new IRRuleCounter() {

			@Override
			public int getEntryCount() throws RException {

				int count = 0;

				for (IRReteNode node : getNodeUpdater().getNodeList()) {

					if (node.getReteType() == RReteType.ROOT0) {
						continue;
					}

					if (node == XRNodeRule0.this) {
						continue;
					}

					IREntryCounter entryCounter = node.getEntryQueue().getEntryCounter();

					count += entryCounter.getEntryCount(RReteStatus.DEFINE)
							+ entryCounter.getEntryCount(RReteStatus.REASON);
				}

				return count;
			}

			@Override
			public int getExecuteCount() {
				return XRNodeRule0.this.getNodeExecCount();
			}

			@Override
			public int getNodeCount() {
				return allReteNodes.size();
			}

			@Override
			public IRRule getRule() {
				return XRNodeRule0.this;
			}

			@Override
			public int getStatementCount() throws RException {

				int count = 0;
				for (IRReteNode node : getNodeUpdater().getNodeList()) {
					if (node.getReteType() == RReteType.ROOT0) {
						IREntryCounter entryCounter = node.getEntryQueue().getEntryCounter();
						count += entryCounter.getEntryCount(RReteStatus.DEFINE);
					}
				}

				return count;
			}

			@Override
			public int getUpdateCount() throws RException {
				return XRNodeRule0.this.getEntryQueue().getUpdateCount();
			}
		};
	}

	@Override
	public IRError getLastError() {
		return lastError;
	}

	@Override
	public IRList getLastValues() {
		return lastValueEntry;
	}

	public LinkedList<IRList> getMatchStmtList() {
		return matchStmtList;
	}

	@Override
	public int getNodeFailedCount() {
		return nodeFailedCount;
	}

	@Override
	public IReteNodeMatrix getNodeMatrix() {

		if (nodeMatrix == null) {
			nodeMatrix = new RuleNodeMatrix(this);
		}

		return nodeMatrix;
	}

	@Override
	public String getNodeName() {
		return this.uniqName;
	}

	public XRNodeOrderedUpdater getNodeUpdater() throws RException {

		if (nodeUpdater == null) {
			nodeUpdater = new XRNodeOrderedUpdater();
			nodeUpdater.addNodeUpdateList(this);
		}

		return nodeUpdater;
	}

	@Override
	public String getRuleDecription() {
		return ruleDecription;
	}

	@Override
	public String getRuleName() {
		return this.uniqName;
	}

	@Override
	public IRReteNode getRuleNode() {
		return this;
	}

	@Override
	public RRunState getRunState() {

		// Runnable is default state, should check parent's entry count
		if (runState == RRunState.Runnable) {

			// no more entry
			if (lastParentVisitIndex >= this.getParentNodes()[0].getEntryQueue().size()) {
				return RRunState.Completed;
			}
		}

		return runState;
	}

	@Override
	public IRVar[] getVars() throws RException {

		if (ruleVars == null) {
			ruleVars = new IRVar[varEntry.length];

			for (int i = 0; i < varEntry.length; ++i) {
				IRObject obj = varEntry[i];
				if (obj != null) {
					ruleVars[i] = RulpUtil.addVar(getFrame(), RulpUtil.asAtom(obj).getName());
				}
			}
		}

		return ruleVars;
	}

	@Override
	public RRunState halt() throws RException {

		RRunState _state = getRunState();
		switch (_state) {
		case Completed:
		case Runnable:
		case Running:
			setRunState(RRunState.Halting);
			return getRunState();

		case Halting:
			return RRunState.Halting;

		case Failed:
			return RRunState.Failed;

		default:
			throw new RException("unknown state: " + getRunState());
		}
	}

	public void addAction(IAction action) {
		action.setIndex(this.actionList.size());
		this.actionList.add(action);
	}

	public void setMatchStmtList(List<IRList> matchStmtList) {
		this.matchStmtList.addAll(matchStmtList);
	}

	@Override
	public void setRuleDecription(String ruleDecription) {
		this.ruleDecription = ruleDecription;
	}

	public void setRunState(RRunState runState) {
		this.runState = runState;
	}

	@Override
	public int start(int priority, final int maxStep) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("starting: " + this + ", priority=" + priority);
		}

		if (priority > RETE_PRIORITY_MAXIMUM) {
			throw new RException("Invalid priority: " + priority);
		}

		if (priority < 0) {
			priority = this.priority;
		} else if (priority < this.priority) {
			return 0;
		}

		return getNodeUpdater().process(getModel(), maxStep);
	}

	@Override
	public int update() throws RException {

		if (this.isTrace()) {
			System.out.println("update: " + this);
		}

		++nodeExecCount;

		/*********************************************/
		// idle
		/*********************************************/
		IREntryQueue parentEntryQueue = this.getParentNodes()[0].getEntryQueue();
		int maxParentCount = parentEntryQueue.size();
		if (lastParentVisitIndex == maxParentCount) {
			++nodeIdleCount;
			return 0;
		}

		int updateCount = 0;
		boolean run = true;

		try {

			// Update running status
			setRunState(RRunState.Running);

			for (; run && lastParentVisitIndex < maxParentCount; ++lastParentVisitIndex) {

				this.lastError = null;
				this.lastValueEntry = parentEntryQueue.getEntryAt(lastParentVisitIndex);
//				if (lastValueEntry == null || lastValueEntry.isDroped()) {
//					continue;
//				}

				if (!_match(lastValueEntry)) {
					continue;
				}

				if (this.isTrace()) {
					System.out.println("\tadd:" + lastValueEntry);
				}

				this.entryQueue.addEntry(lastValueEntry);

				++updateCount;
				this.ruleExecutedListenerDispatcher.doAction(XRNodeRule0.this);
			}

		} catch (RError err) {

			if (RuleUtil.isModelTrace()) {
				err.printStackTrace();
				System.err.println(String.format("Unhandled error <%s>: %s", this.getRuleName(), "" + err));
			}

			this.setRunState(RRunState.Failed);
			this.lastError = err.getError();
			this.ruleFailedListenerDispatcher.doAction(XRNodeRule0.this);
			this.nodeFailedCount++;

		} finally {

			// Update running status
			switch (runState) {
			case Completed:
			case Failed:
			case Halting:
				run = false;
				break;

			case Runnable:
				break;

			case Running:
				setRunState(RRunState.Runnable);
				break;

			default:
				throw new RException("unknown state: " + getRunState());
			}
		}

		return updateCount;
	}
}