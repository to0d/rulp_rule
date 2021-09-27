package alpha.rulp.ximpl.node;

import java.util.List;

import alpha.rulp.ximpl.constraint.IRConstraint2;

public interface IRBetaNode extends IRReteNode {

	public List<JoinIndex> getJoinIndexList();

	public List<IRConstraint2> getConstraint2List();
	
	public int getConstraint2Count();
}
