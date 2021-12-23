package alpha.rulp.ximpl.factor;

import static alpha.rulp.lang.Constant.O_Nan;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.runtime.IRFactor;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.FileUtil;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.cache.XRStmtFileDefaultCacher;
import alpha.rulp.ximpl.model.IRuleFactor;

public class XRFactorSetNodeCachePath extends AbsAtomFactorAdapter implements IRFactor, IRuleFactor {

	public XRFactorSetNodeCachePath(String factorName) {
		super(factorName);
	}

	@Override
	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

		/********************************************/
		// Check parameters
		/********************************************/
		int argSize = args.size();
		if (argSize != 4) {
			throw new RException("Invalid parameters: " + args);
		}

		IRModel model = RuleUtil.asModel(interpreter.compute(frame, args.get(1)));
		IRList filter = RulpUtil.asList(args.get(2));
		String cachePath = RulpUtil.asString(interpreter.compute(frame, args.get(3))).asString();

		IRReteNode node = model.findNode(RuleUtil.toCondList(filter));
		String nodeCachePath = FileUtil.toValidPath(cachePath) + XRStmtFileDefaultCacher.getNodeCacheName(node);

		XRStmtFileDefaultCacher cacher = new XRStmtFileDefaultCacher(nodeCachePath, model.getInterpreter());
		model.setNodeLoader(node, cacher);
		model.setNodeSaver(node, cacher);

		return O_Nan;
	}

}
