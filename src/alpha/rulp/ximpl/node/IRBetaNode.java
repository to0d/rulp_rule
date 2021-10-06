package alpha.rulp.ximpl.node;

import java.util.List;

import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.ximpl.constraint.IRConstraint2;

public interface IRBetaNode extends IRReteNode {

	public List<JoinIndex> getJoinIndexList();

	public List<IRConstraint2> getConstraint2List();
	
	public boolean addConstraint2(IRConstraint2 constraint) throws RException;
	
	public int getConstraint2Count();
}
