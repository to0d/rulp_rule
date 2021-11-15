package alpha.rulp.ximpl.model;

import static alpha.rulp.rule.Constant.A_Max;
import static alpha.rulp.rule.Constant.A_Min;
import static alpha.rulp.rule.Constant.A_NOT_NULL;
import static alpha.rulp.rule.Constant.A_Type;
import static alpha.rulp.rule.Constant.A_Uniq;

import java.util.HashMap;
import java.util.Map;

import alpha.rulp.lang.IRAtom;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RType;
import alpha.rulp.rule.IRRListener2;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.constraint.IRConstraint1;
import alpha.rulp.ximpl.constraint.IRConstraint1Max;
import alpha.rulp.ximpl.constraint.IRConstraint1Type;
import alpha.rulp.ximpl.constraint.IRConstraint1Min;

public class XRModelConstraintChecker {

	static final String F_CST_ADD_CONSTRAINT_MAX = "add_cst_constraint_max";

	static final String F_CST_ADD_CONSTRAINT_MIN = "add_cst_constraint_min";

	static final String F_CST_ADD_CONSTRAINT_TYPE = "add_cst_constraint_type";

	private Map<String, IRAtom> atomMap = new HashMap<>();

	private XRModel model;

	public XRModelConstraintChecker(XRModel model) {
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

	public boolean addConstraint(IRReteNode node, IRConstraint1 constraint) throws RException {

		switch (constraint.getConstraintName()) {
		case A_Type:
			return _addTypeConstraint(node, (IRConstraint1Type) constraint);

		case A_Max:
			return _addMaxConstraint(node, (IRConstraint1Max) constraint);

		case A_Min:
			return _addMinConstraint(node, (IRConstraint1Min) constraint);

		case A_Uniq:
		case A_NOT_NULL:
		default:
			return model.getNodeGraph().addConstraint(node, constraint);
		}
	}
}
