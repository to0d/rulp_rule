package alpha.rulp.ximpl.factor;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.runtime.IRFactor;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.model.IRuleFactor;

public class XRFactorAddNode extends AbsRFactorAdapter implements IRFactor, IRuleFactor {

	public XRFactorAddNode(String factorName) {
		super(factorName);
	}

	@Override
	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

		int argSize = args.size();

		/********************************************/
		// Check parameters
		/********************************************/
		if (argSize != 2 && argSize != 3) {
			throw new RException("Invalid parameters: " + args);
		}

		IRModel model = null;
		int argIndex = 1;

		/**************************************************/
		// Check model
		/**************************************************/
		if (argSize == 3) {
			model = RuleUtil.asModel(interpreter.compute(frame, args.get(argIndex)));
			++argIndex;
		} else {
			model = RuleUtil.getDefaultModel(frame);
			if (model == null) {
				throw new RException("no model be specified");
			}
		}

		/**************************************************/
		// Check named list
		/**************************************************/
		IRList namedList = RulpUtil.asList(interpreter.compute(frame, args.get(argIndex++)));
		int anyIndex = ReteUtil.indexOfVarArgStmt(namedList);
		if (anyIndex != -1) {
			throw new RException(String.format("Can't create var arg node: %s", namedList));
		}

		/**************************************************/
		// Find node
		/**************************************************/
		IRReteNode node = ReteUtil.findNameNode(model.getNodeGraph(), namedList);
		if (node != null) {
			return RulpFactory.createInteger(0);
		}

		/**************************************************/
		// Create node
		/**************************************************/
		node = model.getNodeGraph().getNamedNode(namedList.getNamedName(), ReteUtil.getFilerEntryLength(namedList));
		if (node == null) {
			throw new RException(String.format("Fail to create named node: %s", namedList));
		}

		return RulpFactory.createInteger(1);
	}
}
