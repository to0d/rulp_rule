package alpha.rulp.ximpl.model;

import alpha.rulp.lang.IRClass;
import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRInstance;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRSubject;
import alpha.rulp.lang.RException;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.RuleFactory;
import alpha.rulp.ximpl.rclass.AbsRClass;

public class XRModelClass extends AbsRClass implements IRClass {

	public XRModelClass(String className, IRFrame definedFrame) {
		super(className, definedFrame, null);
	}

	@Override
	public IRInstance newInstance(String modelName, IRList args, IRInterpreter interpreter, IRFrame frame)
			throws RException {

		if (args.size() != 3) {
			throw new RException("Invalid construct parameters: " + args);
		}

		return RuleFactory.createModel(modelName, this, frame);
	}

	@Override
	public IRSubject getParent() {
		return null;
	}

}
