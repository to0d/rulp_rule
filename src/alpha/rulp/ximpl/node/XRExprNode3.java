package alpha.rulp.ximpl.node;

import static alpha.rulp.rule.RReteStatus.REASONED;
import static alpha.rulp.rule.RReteStatus.REMOVED;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.ximpl.constraint.IRConstraint1;
import alpha.rulp.ximpl.entry.IREntryQueue;
import alpha.rulp.ximpl.entry.IREntryTable;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRExprNode3 extends XRReteNode1 {

	protected IREntryTable entryTable;

	private int leftEnryLength;

	protected boolean _match() throws RException {

		if (constraintList == null) {
			return true;
		}

		++nodeMatchCount;

		for (IRConstraint1 matchNode : constraintList) {
			if (!matchNode.addEntry(null, this.getModel().getInterpreter(), this.getNodeFrame(true))) {
				return false;
			}
		}

		return true;
	}

	public boolean addReteEntry(IRReteEntry entry, IRObject[] externalVars) throws RException {

		if (entry.size() != leftEnryLength) {
			throw new RException("invalid left entry: " + entry);
		}

		IRObject[] newElements = new IRObject[entryLength];

		// Copy left entry
		for (int i = 0; i < leftEnryLength; ++i) {
			newElements[i] = entry.get(i);
		}

		// Update right
		for (int i = leftEnryLength; i < entryLength; ++i) {
			newElements[i] = externalVars[i - leftEnryLength];
		}

		IRReteEntry newEntry = entryTable.createEntry(entry.getNamedName(), newElements, REASONED);
		entryTable.addReference(newEntry, this.getNodeId(), entry.getEntryId());
		
		return entryQueue.addEntry(newEntry, this.getModel().getInterpreter(), this.getNodeFrame(true));
	}

	public void setEntryTable(IREntryTable entryTable) {
		this.entryTable = entryTable;
	}

	public void setLeftEnryLength(int leftEnryLength) {
		this.leftEnryLength = leftEnryLength;
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
		IREntryQueue parentEntryQueue = parentNodes[0].getEntryQueue();
		int parentEntryCount = parentEntryQueue.size();
		if (lastParentVisitIndex == parentEntryCount) {
			++nodeIdleCount;
			return 0;
		}

		/*********************************************/
		// - no left variable in expr - '(?a ?b ?c) (factor ?x)
		// - No need to pass any entry
		/*********************************************/
		if (!_match()) {
			return 0;
		}

		/*********************************************/
		// compute right expression
		/*********************************************/
		IRFrame modelFrame = model.getModelFrame();
		IRObject[] externalVars = new IRObject[this.getEntryLength() - leftEnryLength];
		for (int i = leftEnryLength; i < this.getEntryLength(); ++i) {
			IRObject extVar = this.getVarEntry()[i];
			IRObject extVal = model.getInterpreter().compute(modelFrame, extVar);
			externalVars[i - leftEnryLength] = extVal;
		}

		/*********************************************/
		// Process
		/*********************************************/
		int updateCount = 0;
		for (; lastParentVisitIndex < parentEntryCount; ++lastParentVisitIndex) {

			IRReteEntry entry = parentEntryQueue.getEntryAt(lastParentVisitIndex);
			if (entry == null || entry.getStatus() == REMOVED) {
				continue;
			}

			if (addReteEntry(entry, externalVars)) {
				++updateCount;
			}
		}

		return updateCount;
	}

}
