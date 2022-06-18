package alpha.rulp.ximpl.node;

import java.util.ArrayList;
import java.util.List;

import alpha.rulp.rule.IRReteNode;
import alpha.rulp.ximpl.action.IAction;

public class SourceNode {

	public List<IAction> actionList = new ArrayList<>();

	public IRReteNode node;

	public String uniqStmt;
}
