package alpha.rulp.ximpl.constraint;

import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRContext;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.ximpl.entry.IRReteEntry;

public abstract class AbsRConstraintX extends AbsRConstraint implements IRConstraintX {

	protected int entryLength;

	public AbsRConstraintX(int entryLength) {
		super();
		this.entryLength = entryLength;
	}

	protected abstract boolean _addEntry(IRReteEntry[] entries, IRContext context) throws RException;

	@Override
	public boolean addEntry(IRReteEntry[] entries, IRContext context) throws RException {

		if (entryLength != entries.length) {
			throw new RException(
					String.format("unmatch entry count: expect=%d, actual=%d", entryLength, entries.length));
		}

		this.matchCount++;

		boolean rc = _addEntry(entries, context);
		if (!rc) {
			this.failCount++;
		}

		return rc;
	}

	@Override
	public void close() {

	}

	@Override
	public void decRef() throws RException {

	}

	public String getCacheInfo() {
		return "";
	}

	@Override
	public RConstraintKind getConstraintKind() {
		return RConstraintKind.CX;
	}

	public int getEntryLength() {
		return entryLength;
	}

	@Override
	public void incRef() throws RException {
	}

	@Override
	public boolean isDeleted() {
		return false;
	}

	public void setNode(IRReteNode node) {

	}
}
