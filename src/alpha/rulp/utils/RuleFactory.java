package alpha.rulp.utils;

import alpha.rulp.lang.IRClass;
import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.ximpl.model.IReteNodeMatrix;
import alpha.rulp.ximpl.model.XRModel;
import alpha.rulp.ximpl.model.XRReteCounter;
import alpha.rulp.ximpl.node.IRReteNodeCounter;

import static alpha.rulp.rule.Constant.*;

public class RuleFactory {

	static {
		// Base Loader
		RulpFactory.registerLoader(RRuleLoader.class);
	}

	public static IRModel createModel(String name, IRClass rclass, IRFrame frame) throws RException {
		return new XRModel(name, rclass, frame);
	}

	public static IRModel createModel(String name, IRFrame frame) throws RException {
		return new XRModel(name, RulpUtil.asClass(RuntimeUtil.lookupFrameEntry(frame, A_MODEL).getValue()), frame);
	}

	public static IRReteNodeCounter createReteCounter(IReteNodeMatrix reteNodeMatrix) {
		return new XRReteCounter(reteNodeMatrix);
	}

}
