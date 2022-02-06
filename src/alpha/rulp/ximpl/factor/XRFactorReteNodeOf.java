package alpha.rulp.ximpl.factor;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.rule.IRRule;
import alpha.rulp.runtime.IRFactor;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.model.IRuleFactor;
import alpha.rulp.ximpl.node.RReteType;

public class XRFactorReteNodeOf extends AbsAtomFactorAdapter implements IRFactor, IRuleFactor {

	static List<? extends IRReteNode> getNodesOfModel(IRModel model, RReteType type) {

		if (type == null) {
			return model.getNodeGraph().getNodeMatrix().getAllNodes();
		}

		return model.getNodeGraph().listNodes(type);
	}

	static List<? extends IRReteNode> getNodesOfRule(IRRule rule, RReteType type) {

		if (type == null) {
			return rule.getAllNodes();
		}

		return rule.getNodeMatrix().getNodeList(type);
	}

	public XRFactorReteNodeOf(String factorName) {
		super(factorName);
	}

	@Override
	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

		/********************************************/
		// Check parameters
		/********************************************/
		int argSize = args.size();
		if (argSize < 2 && argSize > 4) {
			throw new RException("Invalid parameters: " + args);
		}

		IRObject obj = interpreter.compute(frame, args.get(1));
		RReteType type = null;
		if (argSize >= 3) {
			type = RReteType.getRetetType(RulpUtil.asInteger(interpreter.compute(frame, args.get(2))).asInteger());
		}

		List<IRReteNode> nodes = new LinkedList<>();

		if (obj instanceof IRModel) {
			nodes.addAll(getNodesOfModel((IRModel) obj, type));

		} else if (obj instanceof IRRule) {
			nodes.addAll(getNodesOfRule((IRRule) obj, type));

		} else {
			throw new RException("unsupport object: " + obj);
		}

		if (argSize >= 4) {

			IRObject arg = interpreter.compute(frame, args.get(3));
			Iterator<IRReteNode> it = nodes.iterator();

			switch (type) {
			case ROOT0: {
				int len = RulpUtil.asInteger(arg).asInteger();
				while (it.hasNext()) {
					if (it.next().getEntryLength() != len) {
						it.remove();
					}
				}
			}
				break;

			case NAME0: {
				String name = RulpUtil.asString(arg).asString();
				while (it.hasNext()) {
					if (!name.equals(it.next().getNamedName())) {
						it.remove();
					}
				}
			}
				break;

			case RULE: {
				String name = RulpUtil.asString(arg).asString();
				while (it.hasNext()) {
					IRRule rule = (IRRule) it.next();
					if (!name.equals(rule.getRuleName())) {
						it.remove();
					}
				}
			}
				break;

			default:
				throw new RException("Invalid parameters: " + args);
			}
		}

		return RulpFactory.createList(nodes);
	}
}
