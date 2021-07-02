package alpha.rulp.ximpl.model;

import java.util.List;

import alpha.rulp.rule.IRModel;
import alpha.rulp.ximpl.node.IRReteNode;
import alpha.rulp.ximpl.node.RReteType;

public interface IReteNodeMatrix {

	public List<? extends IRReteNode> getNodeList(RReteType reteType);

	public List<? extends IRReteNode> getAllNodes();

	public IRModel getModel();
}
