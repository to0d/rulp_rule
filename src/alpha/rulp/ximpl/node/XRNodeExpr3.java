package alpha.rulp.ximpl.node;

import static alpha.rulp.rule.RReteStatus.REMOVE;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.ximpl.constraint.IRConstraint1;
import alpha.rulp.ximpl.entry.IREntryQueue;
import alpha.rulp.ximpl.entry.IREntryTable;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRNodeExpr3 extends XRNodeRete1 {

	protected IREntryTable entryTable;

	private int leftEnryLength;

	protected boolean _match() throws RException {

		if (constraint1List == null) {
			return true;
		}

		++nodeMatchCount;

		for (IRConstraint1 matchNode : constraint1List) {
			if (!matchNode.addEntry(null, this)) {
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

		IRReteEntry newEntry = entryTable.createEntry(entry.getNamedName(), newElements, ReteUtil.getChildStatus(entry),
				false);
		entryTable.addReference(newEntry, this, entry);

		if (!entryQueue.addEntry(newEntry)) {
			entryTable.removeEntry(newEntry);
			return false;
		}

		return true;
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
		// - No left variable in expr - '(?a ?b ?c) (factor ?x)
		// - No need to pass any entry
		/*********************************************/
		if (!_match()) {
			return 0;
		}

		/*********************************************/
		// compute right expression
		/*********************************************/
		IRFrame modelFrame = model.getFrame();
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
			if (entry == null || entry.getStatus() == REMOVE) {
				continue;
			}

			if (addReteEntry(entry, externalVars)) {
				++updateCount;
			}
		}

		return updateCount;
	}

}
