package alpha.rulp.ximpl.factor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.runtime.IRFactor;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.model.IRuleFactor;

public class XRFactorListSourceNode extends AbsAtomFactorAdapter implements IRFactor, IRuleFactor {

	public XRFactorListSourceNode(String factorName) {
		super(factorName);
	}

	@Override
	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

		/********************************************/
		// Check parameters
		/********************************************/
		int argSize = args.size();
		if (argSize != 3) {
			throw new RException("Invalid parameters: " + args);
		}

		IRModel model = RuleUtil.asModel(interpreter.compute(frame, args.get(1)));
		IRList cond = RulpUtil.asList(args.get(2));

		List<IRReteNode> sourceNodes = new ArrayList<>(RuleUtil.listSourceNodes(model, cond));
		Collections.sort(sourceNodes, (n1, n2) -> {
			return n1.getNodeName().compareTo(n2.getNodeName());
		});

		return RulpFactory.createList(sourceNodes);
	}
}
