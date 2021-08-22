package alpha.rulp.ximpl.sql;

import static alpha.rulp.ximpl.sql.Constant.*;

import java.util.ArrayList;
import java.util.List;

import alpha.rulp.rule.IRModel;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.ximpl.node.IRNamedNode;
import alpha.rulp.ximpl.node.IRNodeGraph;
import alpha.rulp.ximpl.rclass.XRDefInstance;

public class RSQLSchema extends XRDefInstance {

	public IRModel model;

	public List<String> tableNames = new ArrayList<>();

	public RSQLSchema(IRModel model) {
		super(null, STR_SCHEMA_NAME, model.getModelFrame());
		this.model = model;
	}

	public void load() {

		IRNodeGraph nodeGraph = model.getNodeGraph();

		/**************************************************/
		// Find node
		/**************************************************/
		IRNamedNode tableNameNode = nodeGraph.findNamedNode(STR_TABLE_NAMES);

		IRNamedNode node = ReteUtil.findNameNode(model.getNodeGraph(), namedList);
		if (node == null) {

			// Create node
			node = nodeGraph.getNamedNode(namedList.getNamedName(), ReteUtil.getFilerEntryLength(namedList));
		}
	}
}
