package alpha.rulp.rule;

import java.util.List;

import alpha.rulp.lang.IRInstance;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.utils.DeCounter;
import alpha.rulp.ximpl.cache.IRBufferWorker;
import alpha.rulp.ximpl.constraint.IRConstraint1;
import alpha.rulp.ximpl.constraint.IRConstraint1Uniq;
import alpha.rulp.ximpl.entry.IREntryQueue;
import alpha.rulp.ximpl.entry.IRReteEntry;
import alpha.rulp.ximpl.model.IGraphInfo;
import alpha.rulp.ximpl.node.RReteStage;
import alpha.rulp.ximpl.node.RReteType;
import alpha.rulp.ximpl.node.RUniqInfo;

public interface IRReteNode extends IRRunnable, IRInstance, IRContext {

	public static class InheritIndex {

		public final int elementIndex;

		public final int parentIndex;

		public InheritIndex(int parentIndex, int elementIndex) {
			super();
			this.parentIndex = parentIndex;
			this.elementIndex = elementIndex;
		}

		public String toString() {
			return String.format("%d/%d", parentIndex, elementIndex);
		}
	}

	public static class JoinIndex {

		public final int leftIndex;

		public final int rightIndex;

		public JoinIndex(int leftIndex, int rightIndex) {
			super();
			this.leftIndex = leftIndex;
			this.rightIndex = rightIndex;
		}

		@Override
		public String toString() {
			return String.format("join(%d/%d)", leftIndex, rightIndex);
		}
	}

	public void addChildNode(IRReteNode child);

	public boolean addConstraint1(IRConstraint1 constraint) throws RException;

	public void addQueryMatchCount(int add);

	public boolean addReteEntry(IRReteEntry entry) throws RException;

	public int doGC();

	public int getAddEntryFailCount();

	public IRBufferWorker getBufferWorker();

	public String getCacheInfo();

	public List<IRReteNode> getChildNodes();

	public List<IRReteNode> getChildNodes(boolean onlyAutoUpdate);

	public IRConstraint1 getConstraint1(int index);

	public int getConstraint1Count();

	public int getEntryCreateCount();

	public int getEntryDeleteCount();

	public int getEntryLength();

	public IREntryQueue getEntryQueue();

	public IGraphInfo getGraphInfo();

	public InheritIndex[] getInheritIndex();

	public IRConstraint1 getLastFailedConstraint1();

	public String getNamedName();

	public int getNodeExecCount();

	public int getNodeFailedCount();

	public int getNodeId();

	public int getNodeIdleCount();

	public int getNodeMatchCount();

	public String getNodeName();

	public int getParentCount();

	public IRReteNode[] getParentNodes();

	public int getParentVisitIndex(int index);

	public int getPriority();

	public int getQueryMatchCount();

	public int getReteLevel();

	public RReteStage getReteStage();

	public IRList getReteTree();

	public RReteType getReteType();

	public String getUniqName();

	public DeCounter getUpdateCounter();

	public IRObject[] getVarEntry();

	public void incAddEntryFailCount();

	public void incEntryCreateCount();

	public void incEntryDeleteCount();

	public void incExecCount(int execId);

	public void incNodeMatchCount();

	public boolean isNodeFresh();

	public List<IRConstraint1Uniq> listUniqConstraints();

	public List<RUniqInfo> listUniqInfos() throws RException;

	public void removeChildNode(IRReteNode child);

	public IRConstraint1 removeConstraint(String constraintExpression);

	public void setBufferWorker(IRBufferWorker cache);

	public void setChildNodeUpdateMode(IRReteNode child, boolean auto) throws RException;

	public void setEntryQueue(IREntryQueue entryQueue);

	public void setGraphInfo(IGraphInfo graphInfo);

	public void setPriority(int priority) throws RException;

	public void setReteStage(RReteStage stage);

	public void setReteTree(IRList reteTree);

	public void setTrace(boolean trace);

	public void setUniqName(String uniqName);

	public int update(int limit) throws RException;

}
