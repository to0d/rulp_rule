package alpha.rulp.error;

import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.ximpl.constraint.IRConstraint1;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class RConstraintConflict extends RException {

	private static final long serialVersionUID = 7218228450124952497L;

	private IRConstraint1 constraint;

	private IRReteEntry newEntry;

	private IRReteNode node;

	public RConstraintConflict(String msg, IRReteNode node, IRReteEntry newEntry, IRConstraint1 constraint) {
		super(msg);
		this.node = node;
		this.newEntry = newEntry;
		this.constraint = constraint;
	}

	public IRConstraint1 getConstraint() {
		return constraint;
	}

	public IRReteEntry getNewEntry() {
		return newEntry;
	}

	public IRReteNode getNode() {
		return node;
	}

}
