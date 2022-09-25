package alpha.rulp.ximpl.node;

import static alpha.rulp.rule.Constant.A_Uniq;
import static alpha.rulp.rule.Constant.RETE_PRIORITY_DEAD;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RType;
import alpha.rulp.rule.IRModel;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.rule.RRunState;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.DeCounter;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.cache.IRCacheWorker;
import alpha.rulp.ximpl.constraint.ConstraintBuilder;
import alpha.rulp.ximpl.constraint.IRConstraint1;
import alpha.rulp.ximpl.constraint.IRConstraint1Uniq;
import alpha.rulp.ximpl.entry.IREntryQueue;
import alpha.rulp.ximpl.entry.IREntryTable;
import alpha.rulp.ximpl.entry.IRReteEntry;
import alpha.rulp.ximpl.model.IGraphInfo;
import alpha.rulp.ximpl.rclass.AbsRInstance;

public abstract class AbsReteNode extends AbsRInstance implements IRReteNode {

	public static int MAX_EXEC_COUNTER_SIZE = 128;

	protected int _reteLevel = -1;

	protected List<IRConstraint1Uniq> _uniqConstraints = null;

	protected List<RUniqInfo> _uniqInfoList = null;

	protected int addEntryFailCount = 0;

	protected List<IRReteNode> allChildNodes = null;

	protected List<IRReteNode> autoUpdateChildNodes = null;

	protected IRCacheWorker cache;

	protected List<IRConstraint1> constraint1List = null;

	protected Map<String, IRConstraint1> constraintExprMap = null;

	protected int entryCreateCount = 0;

	protected int entryDeleteCount = 0;

	protected int entryLength;

	protected IREntryQueue entryQueue = null;

	protected IREntryTable entryTable;

	protected DeCounter execCounter = new DeCounter(MAX_EXEC_COUNTER_SIZE);

	protected IGraphInfo graphInfo;

	protected InheritIndex inheritIndexs[];

	protected IRConstraint1 lastFailedConstraint1 = null;

	protected IRModel model;

	protected String namedName;

	protected int nodeExecCount = 0;

	protected IRFrame nodeFrame = null;

	protected int nodeId;

	protected int nodeIdleCount = 0;

	protected int nodeMatchCount = 0;

	protected String nodeName = null;

	protected int parentCount = 0;

	protected IRReteNode[] parentNodes;

	protected int priority = 0;

	protected int queryMatchCount = 0;

	protected RReteStage reteStage = RReteStage.InActive;

	protected IRList reteTree;

	protected RReteType reteType;

	protected RRunState runState = RRunState.Runnable; // default

	protected boolean trace = false;

	protected String uniqName = null;

	protected IRObject[] varEntry;

	public AbsReteNode(String instanceName) {
		super(null, instanceName, null);
	}

	protected IRFrame _createNodeFrame() throws RException {
		return RNodeFactory.createNodeFrame(this);
	}

	protected List<RUniqInfo> _rebuildUniqInfoList() {

		List<RUniqInfo> uniqInfoList;

		List<IRConstraint1Uniq> uniqConstraints = listUniqConstraints();

		if (uniqConstraints.isEmpty()) {
			uniqInfoList = Collections.emptyList();

		} else {

			uniqInfoList = new ArrayList<>();
			for (IRConstraint1Uniq cons : uniqConstraints) {
				RUniqInfo uniqInfo = new RUniqInfo();
				uniqInfo.uniqIndexs = cons.getConstraintIndex();
				uniqInfoList.add(uniqInfo);
			}
		}

		return uniqInfoList;
	}

	protected boolean _testConstraint1(IRReteEntry entry) throws RException {

		if (constraint1List == null) {
			return true;
		}

		this.nodeMatchCount++;
		this.lastFailedConstraint1 = null;

//		if (entry.toString().indexOf("life:'(10") != -1) {
//			System.out.println();
//		}

		for (IRConstraint1 constraint : constraint1List) {
			if (!constraint.addEntry(entry, this)) {
				this.addEntryFailCount++;
				this.lastFailedConstraint1 = constraint;
				return false;
			}
		}

		return true;
	}

	public void addChildNode(IRReteNode child) {

		if (allChildNodes == null) {
			allChildNodes = new LinkedList<>();
		}

		allChildNodes.add(child);

		if (autoUpdateChildNodes == null) {
			autoUpdateChildNodes = new LinkedList<>();
		}

		autoUpdateChildNodes.add(child);
	}

	@Override
	public boolean addConstraint1(IRConstraint1 constraint) throws RException {

		IRConstraint1 removeOldConstraint = null;
		boolean isUniqConstraint = false;

		/***********************************************/
		// Check constraint list
		/***********************************************/
		if (getConstraint1Count() != 0) {

			if (constraint.getConstraintName().equals(A_Uniq)) {

				isUniqConstraint = true;

				/***********************************************/
				// Check conflict uniq constraint
				/***********************************************/
				int[] newUniqIndexs = constraint.getConstraintIndex();

				for (IRConstraint1 oldCons : listUniqConstraints()) {

					int[] oldUniqIndexs = oldCons.getConstraintIndex();

					if (ConstraintBuilder.hasPartIndexs(oldUniqIndexs, newUniqIndexs)) {

						// (uniq ?x ?y) add (uniq ?x ?y) => (uniq ?x ?y)
						if (oldUniqIndexs.length == newUniqIndexs.length) {
							return true;
						}
						// (uniq ?x ?y) add (uniq ?x) => (uniq ?x)
						else {
							removeOldConstraint = oldCons;
						}
					}
					// (uniq ?x) add (uniq ?x ?y) => (uniq ?x) no change
					else if (ConstraintBuilder.hasPartIndexs(newUniqIndexs, oldUniqIndexs)) {
						return true;
					}
				}

			} else {

				// Duplicate constraint
				if (constraintExprMap.containsKey(constraint.getConstraintExpression())) {
					return true;
				}
			}

		}

		/***********************************************/
		// Check old entries
		/***********************************************/
		int size = entryQueue.size();
		for (int i = 0; i < size; ++i) {

			IRReteEntry entry = entryQueue.getEntryAt(i);
			if (entry == null || entry.isDroped()) {
				continue;
			}

			if (!constraint.addEntry(entry, this)) {
				constraint.close();
				throw new RException(String.format("Unable to add constraint<%s> due to entry<%s>", constraint, entry));
			}
		}

		/***********************************************/
		// Add new constraint
		/***********************************************/

		if (constraintExprMap == null) {
			constraintExprMap = new HashMap<>();
		}
		constraintExprMap.put(constraint.getConstraintExpression(), constraint);

		if (constraint1List == null) {
			constraint1List = new ArrayList<>();
		}

		constraint1List.add(constraint);

		if (isUniqConstraint) {
			_uniqConstraints = null;
			_uniqInfoList = null;
		}

		/***********************************************/
		// Remove old constraint
		/***********************************************/
		if (removeOldConstraint != null) {
			constraint1List.remove(removeOldConstraint);
			constraintExprMap.remove(removeOldConstraint.getConstraintExpression());
		}

		return true;
	}

	@Override
	public void addQueryMatchCount(int add) {
		queryMatchCount += add;
	}

	@Override
	public boolean addReteEntry(IRReteEntry entry) throws RException {

		if (entry.size() != this.getEntryLength() || entry.isDeleted()) {
			throw new RException("invalid entry: " + entry);
		}

		if (!_testConstraint1(entry)) {
			return false;
		}

		return entryQueue.addEntry(entry);
	}

	@Override
	public String asString() {
		return this.uniqName;
	}

	@Override
	public void clean() throws RException {

		if (nodeFrame != null && nodeFrame.listEntries().isEmpty()) {
			nodeFrame.release();
			RulpUtil.decRef(nodeFrame);
			nodeFrame = null;

		}
	}

	@Override
	public void decRef() throws RException {

	}

	@Override
	public void delete(IRInterpreter interpreter, IRFrame frame) throws RException {

		if (this.nodeFrame != null) {
			RulpUtil.decRef(this.nodeFrame);
			this.nodeFrame = null;
		}

		this._delete();
	}

	@Override
	public int doGC() {
		return entryQueue.doGC();
	}

	@Override
	public IRFrame findFrame() {
		return this.nodeFrame;
	}

	@Override
	public int getAddEntryFailCount() {
		return addEntryFailCount;
	}

	public String getCacheInfo() {
		return "";
	}

	@Override
	public IRCacheWorker getCacheWorker() {
		return cache;
	}

	@Override
	public List<IRReteNode> getChildNodes() {
		return getChildNodes(false);
	}

	@Override
	public List<IRReteNode> getChildNodes(boolean onlyAutoUpdate) {

		if (onlyAutoUpdate) {
			return autoUpdateChildNodes == null ? Collections.emptyList() : autoUpdateChildNodes;
		} else {
			return allChildNodes == null ? Collections.emptyList() : allChildNodes;
		}
	}

	@Override
	public IRConstraint1 getConstraint1(int index) {

		if (constraint1List == null || index < 0 || index >= constraint1List.size()) {
			return null;
		}

		return constraint1List.get(index);
	}

	@Override
	public int getConstraint1Count() {
		return constraint1List == null ? 0 : constraint1List.size();
	}

	@Override
	public int getEntryCreateCount() {
		return entryCreateCount;
	}

	@Override
	public int getEntryDeleteCount() {
		return entryDeleteCount;
	}

	@Override
	public int getEntryLength() {
		return entryLength;
	}

	@Override
	public IREntryQueue getEntryQueue() {
		return this.entryQueue;
	}

	@Override
	public IRFrame getFrame() throws RException {

		if (nodeFrame == null) {
			nodeFrame = _createNodeFrame();
			RulpUtil.incRef(nodeFrame);
		}

		return nodeFrame;
	}

	@Override
	public IGraphInfo getGraphInfo() {
		return graphInfo;
	}

	@Override
	public InheritIndex[] getInheritIndex() {
		return inheritIndexs;
	}

	@Override
	public IRInterpreter getInterpreter() {
		return this.getModel().getInterpreter();
	}

	@Override
	public IRConstraint1 getLastFailedConstraint1() {
		return lastFailedConstraint1;
	}

	@Override
	public IRModel getModel() {
		return this.model;
	}

	@Override
	public String getNamedName() {
		return namedName;
	}

	@Override
	public int getNodeExecCount() {
		return nodeExecCount;
	}

	@Override
	public int getNodeFailedCount() {
		return 0;
	}

	@Override
	public int getNodeId() {
		return nodeId;
	}

	@Override
	public int getNodeIdleCount() {
		return nodeIdleCount;
	}

	@Override
	public int getNodeMatchCount() {
		return nodeMatchCount;
	}

	@Override
	public String getNodeName() {

		if (nodeName == null) {
			nodeName = ReteUtil.getNodeName(this.getReteType(), this.getNodeId());
		}

		return nodeName;
	}

	@Override
	public int getParentCount() {
		return parentNodes == null ? 0 : parentNodes.length;
	}

	@Override
	public IRReteNode[] getParentNodes() {
		return parentNodes;
	}

	@Override
	public int getPriority() {
		return priority;
	}

	@Override
	public int getQueryMatchCount() {
		return queryMatchCount;
	}

	@Override
	public int getRef() {
		return 0;
	}

	@Override
	public int getReteLevel() {

		if (_reteLevel == -1) {

			if (this.getParentNodes() != null) {
				for (IRReteNode parent : this.getParentNodes()) {
					int level = parent.getReteLevel() + 1;
					if (_reteLevel < level) {
						_reteLevel = level;
					}
				}
			} else {
				_reteLevel = 0;
			}

		}

		return _reteLevel;
	}

	@Override
	public RReteStage getReteStage() {
		return reteStage;
	}

	@Override
	public IRList getReteTree() {
		return reteTree;
	}

	@Override
	public RReteType getReteType() {
		return reteType;
	}

	@Override
	public RRunState getRunState() {
		return runState;
	}

	@Override
	public RType getType() {
		return RType.INSTANCE;
	}

	@Override
	public String getUniqName() {
		return uniqName;
	}

	public DeCounter getUpdateCounter() {
		return execCounter;
	}

	@Override
	public IRObject[] getVarEntry() {
		return varEntry;
	}

	@Override
	public RRunState halt() throws RException {
		this.runState = RRunState.Halting;
		return this.runState;
	}

	public void incAddEntryFailCount() {
		this.addEntryFailCount++;
	}

	public void incEntryCreateCount() {
		entryCreateCount++;
	}

	public void incEntryDeleteCount() {
		entryDeleteCount++;
	}

	@Override
	public void incExecCount(int execId) {
		execCounter.add(execId);
	}

	@Override
	public void incNodeMatchCount() {
		this.nodeMatchCount++;
	}

	@Override
	public void incRef() throws RException {
	}

	@Override
	public boolean isDeleted() {
		return false;
	}

	public boolean isTrace() {
		return trace;
	}

	@Override
	public List<IRConstraint1Uniq> listUniqConstraints() {

		if (_uniqConstraints == null) {
			if (constraint1List != null) {
				for (IRConstraint1 oldCons : constraint1List) {
					if (oldCons.getConstraintName().equals(A_Uniq)) {
						if (_uniqConstraints == null) {
							_uniqConstraints = new ArrayList<>();
						}
						_uniqConstraints.add((IRConstraint1Uniq) oldCons);
					}
				}
			}

			if (_uniqConstraints == null) {
				_uniqConstraints = Collections.emptyList();
			}
		}

		return _uniqConstraints;
	}

	@Override
	public List<RUniqInfo> listUniqInfos() {

		if (_uniqInfoList == null) {
			_uniqInfoList = _rebuildUniqInfoList();
		}

		return _uniqInfoList;
	}

//	public void removeChild(IRReteNode child) {
//
//		if (allChildNodes != null) {
//			allChildNodes.remove(child);
//
//			if (allChildNodes.isEmpty()) {
//				allChildNodes = null;
//			}
//		}
//
//		if (autoUpdateChildNodes != null) {
//			autoUpdateChildNodes.remove(child);
//
//			if (autoUpdateChildNodes.isEmpty()) {
//				autoUpdateChildNodes = null;
//			}
//		}
//	}

	@Override
	public void removeChildNode(IRReteNode child) {

		if (allChildNodes != null) {
			allChildNodes.remove(child);

			if (allChildNodes.isEmpty()) {
				allChildNodes = null;
			}
		}

		if (autoUpdateChildNodes != null) {
			autoUpdateChildNodes.remove(child);

			if (autoUpdateChildNodes.isEmpty()) {
				autoUpdateChildNodes = null;
			}
		}
	}

	@Override
	public IRConstraint1 removeConstraint(String constraintExpression) {

		IRConstraint1 constraint = constraintExprMap.remove(constraintExpression);
		if (constraint != null) {
			constraint1List.remove(constraint);
			constraint.close();
		}

		if (constraint1List.isEmpty()) {
			constraint1List = null;
			constraintExprMap = null;
		}

		return constraint;
	}

	@Override
	public void setCacheWorker(IRCacheWorker cache) {
		this.cache = cache;
	}

	@Override
	public void setChildNodeUpdateMode(IRReteNode child, boolean auto) throws RException {

		if (!allChildNodes.contains(child)) {
			throw new RException("child not found: " + child);
		}

		if (auto) {
			if (!autoUpdateChildNodes.contains(child)) {
				autoUpdateChildNodes.add(child);
			}
		} else {
			if (autoUpdateChildNodes.contains(child)) {
				autoUpdateChildNodes.remove(child);
			}
		}
	}

	public void setEntryLength(int entryLength) {
		this.entryLength = entryLength;
	}

	@Override
	public void setEntryQueue(IREntryQueue entryQueue) {
		this.entryQueue = entryQueue;
	}

	public void setEntryTable(IREntryTable entryTable) {
		this.entryTable = entryTable;
	}

	@Override
	public void setGraphInfo(IGraphInfo graphInfo) {
		this.graphInfo = graphInfo;
	}

	public void setInheritIndexs(InheritIndex[] inheritIndexs) {
		this.inheritIndexs = inheritIndexs;
	}

	public void setModel(IRModel model) {
		this.model = model;
	}

	public void setNamedName(String entryName) {
		this.namedName = entryName;
	}

	public void setNodeId(int nodeId) {
		this.nodeId = nodeId;
	}

	public void setParentNodes(IRReteNode[] parentNodes) throws RException {
		this.parentNodes = parentNodes;
		this.parentCount = parentNodes.length;
	}

	@Override
	public void setPriority(int priority) throws RException {

		if (this.priority <= RETE_PRIORITY_DEAD) {
			throw new RException("node is dead: " + this.toString());
		}

		this.priority = priority;
	}

	@Override
	public void setReteStage(RReteStage reteStage) {
		this.reteStage = reteStage;
	}

	@Override
	public void setReteTree(IRList reteTree) {
		this.reteTree = reteTree;
	}

	public void setReteType(RReteType reteType) {
		this.reteType = reteType;
	}

	@Override
	public void setTrace(boolean trace) {
		this.trace = trace;
	}

	@Override
	public void setUniqName(String uniqName) {
		this.uniqName = uniqName;
	}

	public void setVarEntry(IRObject[] varEntry) {
		this.varEntry = varEntry;
	}

	@Override
	public int start(int priority, int maxStep) throws RException {
		throw new RException("invalid operation: can't start node: " + this.toString());
	}

	public String toString() {
		return String.format("%s: %s", getNodeName(), this.uniqName);
	}
}
