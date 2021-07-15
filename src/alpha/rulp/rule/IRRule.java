package alpha.rulp.rule;

import java.util.List;

import alpha.rulp.lang.IRError;
import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRVar;
import alpha.rulp.lang.RException;
import alpha.rulp.ximpl.model.IReteNodeMatrix;
import alpha.rulp.ximpl.node.IRReteNode;

public interface IRRule extends IRReteNode, IRRunnable {

	public void addRuleExecutedListener(IRRListener1<IRRule> listener);

	public void addRuleFailedListener(IRRListener1<IRRule> listener);

	public List<IRExpr> getActionStmtList();

	public List<? extends IRReteNode> getAllNodes();

	public IRRuleCounter getCounter();

	public IRError getLastError();

	public IRList getLastValues();

	public IRModel getModel();

	public IReteNodeMatrix getNodeMatrix();

	public int getPriority();

	public String getRuleDecription();

	public String getRuleName();

	public IRReteNode getRuleNode();

	public IRVar[] getVars() throws RException;

	public void setRuleDecription(String ruleDecription);

}
