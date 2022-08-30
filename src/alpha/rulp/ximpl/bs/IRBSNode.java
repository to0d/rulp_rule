package alpha.rulp.ximpl.bs;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.RException;

public interface IRBSNode {

	public IRList buildResultTree(boolean explain) throws RException;

	public void complete() throws RException;

	public IRBSNode getChild(int index);

	public int getChildCount();

	public int getIndexInParent();

	public int getLevel();

	public boolean needComplete();

	public String getNodeName();

	public IRBSNode getParentNode();

	public BSStats getStatus();

	public String getStatusString();

	public BSType getType();

	public void init() throws RException;

	public boolean isSucc();

	public void process(IRBSNode lastNode) throws RException;

	public void setStatus(BSStats status);

	public void setSucc(boolean succ) throws RException;
}
