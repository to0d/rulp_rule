package alpha.rulp.ximpl.node;

import java.util.ArrayList;
import java.util.List;

import alpha.rulp.rule.IRRule;
import alpha.rulp.ximpl.action.IAction;

public class SourceNode {

	public List<IAction> actionList = new ArrayList<>();

	public IRRule rule;

	public String uniqStmt;
}
