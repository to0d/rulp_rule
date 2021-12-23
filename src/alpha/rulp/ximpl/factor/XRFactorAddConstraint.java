package alpha.rulp.ximpl.factor;

import static alpha.rulp.lang.Constant.O_False;

import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.runtime.IRFactor;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.utils.StmtUtil;
import alpha.rulp.ximpl.constraint.ConstraintBuilder;
import alpha.rulp.ximpl.constraint.IRConstraint1;
import alpha.rulp.ximpl.model.IRuleFactor;

public class XRFactorAddConstraint extends AbsAtomFactorAdapter implements IRFactor, IRuleFactor {

	public XRFactorAddConstraint(String factorName) {
		super(factorName);
	}

	@Override
	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

		int argSize = args.size();

		/********************************************/
		// Check parameters
		/********************************************/
		if (argSize != 3 && argSize != 4) {
			throw new RException("Invalid parameters: " + args);
		}

		IRModel model = StmtUtil.getStmtModel(args, interpreter, frame, 4);

		/**************************************************/
		// Check named list
		/**************************************************/
		IRList namedList = RulpUtil.asList(interpreter.compute(frame, args.get(args.size() - 2)));
		IRReteNode node = ReteUtil.findNameNode(model.getNodeGraph(), namedList);
		if (node == null) {

			int anyIndex = ReteUtil.indexOfVarArgStmt(namedList);
			if (anyIndex != -1) {
				throw new RException(String.format("Can't create var arg node: %s", namedList));
			}

			// Create node
			node = model.getNodeGraph().getNamedNode(namedList.getNamedName(), ReteUtil.getFilerEntryLength(namedList));
			if (node == null) {
				throw new RException(String.format("Fail to create named node: %s", namedList));
			}
		}

		/**************************************************/
		// Check constraint expression
		/**************************************************/
		IRExpr constraintExpr = RulpUtil.asExpression(args.get(args.size() - 1));
		IRConstraint1 cons = new ConstraintBuilder(ReteUtil._varEntry(ReteUtil.buildTreeVarList(namedList)))
				.build(constraintExpr, interpreter, frame);

		// The constraint maybe exist already
		if (cons == null) {
			return O_False;
		}

		/********************************************/
		// Update Constraint
		/********************************************/
		return RulpFactory.createBoolean(model.addConstraint(node, cons));
	}

}
