package alpha.rulp.ximpl.node;

import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.RException;
import alpha.rulp.ximpl.constraint.IRConstraint1Uniq;

public class XRNodeFunc extends XRNodeNamed implements IRFuncNode {

	protected IRExpr[] funcEntry;

	protected IRConstraint1Uniq funcUniqConstraint;

	public XRNodeFunc(String instanceName) {
		super(instanceName);
	}

	@Override
	public IRExpr[] getFuncEntry() {
		return funcEntry;
	}

	@Override
	public IRConstraint1Uniq getFuncUniqConstraint() {
		return funcUniqConstraint;
	}

	public void setFuncEntry(IRExpr[] funcEntry) {
		this.funcEntry = funcEntry;
	}

	public void setFuncUniqConstraint(IRConstraint1Uniq funcUniqConstraint) throws RException {

		this.funcUniqConstraint = funcUniqConstraint;
		this.addConstraint1(funcUniqConstraint);

	}

}
