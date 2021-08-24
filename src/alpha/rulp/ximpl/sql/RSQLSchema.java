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

	public RSQLSchema(IRModel model) {
		super(null, STR_SCHEMA_NAME, model.getModelFrame());
		this.model = model;
	}

	public void init() {

		
	}
}
