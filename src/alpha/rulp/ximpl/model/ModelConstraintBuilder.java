package alpha.rulp.ximpl.model;

import static alpha.rulp.lang.Constant.A_EXPRESSION;
import static alpha.rulp.rule.Constant.A_Max;
import static alpha.rulp.rule.Constant.A_Min;
import static alpha.rulp.rule.Constant.A_Type;
import static alpha.rulp.rule.Constant.A_Uniq;

import java.util.HashMap;
import java.util.Map;

import alpha.rulp.lang.IRAtom;
import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RRelationalOperator;
import alpha.rulp.lang.RType;
import alpha.rulp.rule.IRModel;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.constraint.ConstraintFactory;
import alpha.rulp.ximpl.constraint.IRConstraint1;
import alpha.rulp.ximpl.constraint.IRConstraint1Expr;
import alpha.rulp.ximpl.constraint.IRConstraint1Max;
import alpha.rulp.ximpl.constraint.IRConstraint1Min;
import alpha.rulp.ximpl.constraint.IRConstraint1Type;
import alpha.rulp.ximpl.constraint.XRConstraint1Expr0X;

public class ModelConstraintBuilder {

	static final String F_CST_ADD_CONSTRAINT_MAX = "add_cst_constraint_max";

	static final String F_CST_ADD_CONSTRAINT_MIN = "add_cst_constraint_min";

	static final String F_CST_ADD_CONSTRAINT_TYPE = "add_cst_constraint_type";

	static final String F_CST_RMV_CONSTRAINT_MAX = "remove_cst_constraint_max";

	static final String F_CST_RMV_CONSTRAINT_MIN = "remove_cst_constraint_min";

	static final String F_CST_RMV_CONSTRAINT_TYPE = "remove_cst_constraint_type";

	private Map<String, IRAtom> atomMap = new HashMap<>();

	private IRModel model;

	public ModelConstraintBuilder(IRModel model) {
		super();
		this.model = model;
	}

	protected boolean _addMaxConstraint(IRReteNode node, IRConstraint1Max constraint) throws RException {

		// $cst_max$:'(?node ?index ?value)
		IRObject rst = model.getInterpreter().compute(model.getFrame(),
				RulpFactory.createExpression(_getAtom(F_CST_ADD_CONSTRAINT_MAX), model,
						RulpFactory.createString(RuleUtil.asNamedNode(node).getNamedName()),
						RulpFactory.createInteger(constraint.getColumnIndex()), constraint.getValue()));

		return RulpUtil.asBoolean(rst).asBoolean();
	}

	protected boolean _addMinConstraint(IRReteNode node, IRConstraint1Min constraint) throws RException {

		// $cst_max$:'(?node ?index ?value)
		IRObject rst = model.getInterpreter().compute(model.getFrame(),
				RulpFactory.createExpression(_getAtom(F_CST_ADD_CONSTRAINT_MIN), model,
						RulpFactory.createString(RuleUtil.asNamedNode(node).getNamedName()),
						RulpFactory.createInteger(constraint.getColumnIndex()), constraint.getValue()));

		return RulpUtil.asBoolean(rst).asBoolean();
	}

	protected boolean _addTypeConstraint(IRReteNode node, IRConstraint1Type constraint) throws RException {

		// $cst_type$:'(?node ?index ?type)
		IRObject rst = model.getInterpreter().compute(model.getFrame(),
				RulpFactory.createExpression(_getAtom(F_CST_ADD_CONSTRAINT_TYPE), model,
						RulpFactory.createString(RuleUtil.asNamedNode(node).getNamedName()),
						RulpFactory.createInteger(constraint.getColumnIndex()),
						RType.toObject(constraint.getColumnType())));

		return RulpUtil.asBoolean(rst).asBoolean();
	}

	protected IRAtom _getAtom(String name) {

		IRAtom atom = atomMap.get(name);
		if (atom == null) {
			atom = RulpFactory.createAtom(name);
			atomMap.put(name, atom);
		}

		return atom;
	}

	protected int _getExprLevel(IRObject obj) throws RException {

		switch (obj.getType()) {
		case LIST:
		case EXPR:

			int max_level = 0;
			IRIterator<? extends IRObject> it = ((IRList) obj).iterator();
			while (it.hasNext()) {

				int level = _getExprLevel(it.next());
				if (max_level < level) {
					max_level = level;
				}
			}

			return obj.getType() == RType.LIST ? max_level : (max_level + 1);

		default:
			return 0;
		}
	}

	protected IRConstraint1 _rebuildConstraint(IRReteNode node, IRConstraint1 constraint) throws RException {

		switch (constraint.getConstraintName()) {
		case A_EXPRESSION:
			return _rebuildExprConstraint(node, (IRConstraint1Expr) constraint);

		default:
		}

		return null;
	}

	protected IRConstraint1 _rebuildExprConstraint(IRReteNode node, IRConstraint1Expr constraint) throws RException {

		IRExpr expr = constraint.getExpr();
		RRelationalOperator op = null;

		if ((op = ReteUtil.toRelationalOperator(expr.get(0).asString())) != null && _getExprLevel(expr) == 1
				&& expr.size() == 3 && constraint instanceof XRConstraint1Expr0X) {

			XRConstraint1Expr0X consExprX = (XRConstraint1Expr0X) constraint;
			if (consExprX.getConstraintIndex().length == 1 && consExprX.getExternalVarCount() == 0) {

				int constraintIndex = consExprX.getConstraintIndex()[0];
				String constraintIndexName = String.format("?%d", constraintIndex);

				// (op ?0 value)
				if (expr.get(1).asString().equals(constraintIndexName)) {
					return ConstraintFactory.createConstraintCompareValue(op, constraintIndex, expr.get(2));
				}

				// (op value ?0)
				if (expr.get(2).asString().equals(constraintIndexName)) {
					return ConstraintFactory.createConstraintCompareValue(RRelationalOperator.toOpposite(op),
							constraintIndex, expr.get(1));
				}
			}

			// (!= ?0 ?1)
			if (consExprX.getConstraintIndex().length == 2 && consExprX.getExternalVarCount() == 0) {
				return ConstraintFactory.createConstraintCompareIndex(op, consExprX.getConstraintIndex()[0],
						consExprX.getConstraintIndex()[1]);
			}
		}

		return null;
	}

	protected IRObject _removeMaxConstraint(IRReteNode node, IRConstraint1Max constraint) throws RException {

		// $cst_max$:'(?node ?index ?value)
		return model.getInterpreter().compute(model.getFrame(),
				RulpFactory.createExpression(_getAtom(F_CST_RMV_CONSTRAINT_MAX), model,
						RulpFactory.createString(RuleUtil.asNamedNode(node).getNamedName()),
						RulpFactory.createInteger(constraint.getColumnIndex()), constraint.getValue()));
	}

	protected IRObject _removeMinConstraint(IRReteNode node, IRConstraint1Min constraint) throws RException {

		// $cst_max$:'(?node ?index ?value)
		return model.getInterpreter().compute(model.getFrame(),
				RulpFactory.createExpression(_getAtom(F_CST_RMV_CONSTRAINT_MIN), model,
						RulpFactory.createString(RuleUtil.asNamedNode(node).getNamedName()),
						RulpFactory.createInteger(constraint.getColumnIndex()), constraint.getValue()));
	}

	protected IRObject _removeTypeConstraint(IRReteNode node, IRConstraint1Type constraint) throws RException {

		// $cst_type$:'(?node ?index ?type)
		return model.getInterpreter().compute(model.getFrame(),
				RulpFactory.createExpression(_getAtom(F_CST_RMV_CONSTRAINT_TYPE), model,
						RulpFactory.createString(RuleUtil.asNamedNode(node).getNamedName()),
						RulpFactory.createInteger(constraint.getColumnIndex()),
						RType.toObject(constraint.getColumnType())));
	}

	public boolean addConstraint(IRReteNode node, IRConstraint1 constraint) throws RException {

		IRConstraint1 lastConstraint = constraint;
		while (true) {

			IRConstraint1 newConstraint = _rebuildConstraint(node, lastConstraint);
			if (newConstraint == null) {
				break;
			}

			lastConstraint = newConstraint;
		}

		switch (lastConstraint.getConstraintName()) {
		case A_Type:
			return _addTypeConstraint(node, (IRConstraint1Type) lastConstraint);

		case A_Max:
			return _addMaxConstraint(node, (IRConstraint1Max) lastConstraint);

		case A_Min:
			return _addMinConstraint(node, (IRConstraint1Min) lastConstraint);

		case A_Uniq:
		default:
			return model.getNodeGraph().addConstraint(node, lastConstraint);
		}
	}

	public IRObject removeConstraint(IRReteNode node, IRConstraint1 constraint) throws RException {

		switch (constraint.getConstraintName()) {
		case A_Type:
			return _removeTypeConstraint(node, (IRConstraint1Type) constraint);

		case A_Max:
			return _removeMaxConstraint(node, (IRConstraint1Max) constraint);

		case A_Min:
			return _removeMinConstraint(node, (IRConstraint1Min) constraint);

		case A_Uniq:
		default:
			return model.getNodeGraph().removeConstraint(node, constraint);
		}
	}
}
