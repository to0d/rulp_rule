package alpha.rulp.ximpl.node;

import alpha.rulp.lang.IRExpr;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.ximpl.constraint.IRConstraint1Uniq;

public interface IRFuncNode extends IRReteNode {

	public IRConstraint1Uniq getFuncUniqConstraint();

	public IRExpr[] getFuncEntry();

}
