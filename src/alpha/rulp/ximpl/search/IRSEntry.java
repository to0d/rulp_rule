package alpha.rulp.ximpl.search;

import java.util.List;

import alpha.rulp.lang.IRObject;
import alpha.rulp.rule.IRReteNode;

public interface IRSEntry {

	public ISScope<List<IRObject>> getScope();

	public IRReteNode getSearchNode();

	public List<? extends IRSVar> listSVar();
}
