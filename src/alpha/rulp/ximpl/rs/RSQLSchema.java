package alpha.rulp.ximpl.rs;

import static alpha.rulp.ximpl.rs.Constant.STR_SCHEMA_NAME;

import alpha.rulp.rule.IRModel;
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
