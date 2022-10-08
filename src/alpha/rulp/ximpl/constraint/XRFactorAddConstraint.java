package alpha.rulp.ximpl.constraint;

import static alpha.rulp.lang.Constant.A_LAMBDA;
import static alpha.rulp.lang.Constant.O_False;

import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RType;
import alpha.rulp.rule.IRModel;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.runtime.IRFactor;
import alpha.rulp.runtime.IRFunction;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.utils.StmtUtil;
import alpha.rulp.ximpl.factor.AbsAtomFactorAdapter;
import alpha.rulp.ximpl.model.IRuleFactor;

public class XRFactorAddConstraint extends AbsAtomFactorAdapter implements IRFactor, IRuleFactor {

	static IRObject _addFuncConstraint(IRFunction func, String funcName, IRModel model, IRReteNode node)
			throws RException {

		if (func.getArgCount() != 1) {
			throw new RException(
					String.format("Fail to add constraint function: %s, unmatch para<actual=%d, expect=%d>", func,
							func.getArgCount(), 2));
		}

		if (funcName == null) {
			funcName = "(" + func.getName() + ")";
		}

		IRConstraint1 cons = ConstraintFactory.func(func, funcName);
		return RulpFactory.createBoolean(model.addConstraint(node, cons));
	}

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

			int varyIndex = ReteUtil.indexOfVaryArgStmt(namedList);
			if (varyIndex != -1) {
				throw new RException(String.format("Can't create var arg node: %s", namedList));
			}

			// Create node
			node = model.getNodeGraph().createNodeRoot(namedList.getNamedName(),
					ReteUtil.getFilerEntryLength(namedList));
			if (node == null) {
				throw new RException(String.format("Fail to create named node: %s", namedList));
			}
		}

		/**************************************************/
		// Check constraint expression
		/**************************************************/
		IRExpr constraintExpr = RulpUtil.asExpression(args.get(args.size() - 1));

		// user defined function
		if (constraintExpr.size() == 1) {
			IRObject obj = RulpUtil.lookup(constraintExpr.get(0), interpreter, frame);
			if (obj.getType() == RType.FUNC) {
				return _addFuncConstraint(RulpUtil.asFunction(obj), null, model, node);
			}
		}

		// lambda function
		if (constraintExpr.size() > 1 && RulpUtil.isObject(constraintExpr.get(0), A_LAMBDA, RType.ATOM, RType.FACTOR)) {
			IRFunction func = RulpUtil.asFunction(interpreter.compute(frame, constraintExpr));
			return _addFuncConstraint(func, RulpUtil.toString(constraintExpr), model, node);
		}

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
