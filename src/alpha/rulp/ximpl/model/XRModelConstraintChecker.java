package alpha.rulp.ximpl.model;

import static alpha.rulp.rule.Constant.A_Max;
import static alpha.rulp.rule.Constant.A_Min;
import static alpha.rulp.rule.Constant.A_NOT_NULL;
import static alpha.rulp.rule.Constant.A_Type;
import static alpha.rulp.rule.Constant.A_Uniq;

import alpha.rulp.lang.IRAtom;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RType;
import alpha.rulp.rule.IRRListener2;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.ximpl.constraint.IRConstraint1;
import alpha.rulp.ximpl.constraint.IRConstraint1Type;

public class XRModelConstraintChecker implements IRRListener2<IRReteNode, IRConstraint1> {

	private XRModel model;

	public XRModelConstraintChecker(XRModel model) {
		super();
		this.model = model;
	}

	static final String F_CST_ADD_CONSTRAINT_TYPE = "add_cst_constraint_type";

	private IRAtom atomAddCstConstraintType = null;

	public IRAtom getAtomAddCstConstraintType() {
		if (atomAddCstConstraintType == null) {
			atomAddCstConstraintType = RulpFactory.createAtom(F_CST_ADD_CONSTRAINT_TYPE);
		}
		return atomAddCstConstraintType;
	}

	@Override
	public void doAction(IRReteNode node, IRConstraint1 constraint) throws RException {

		switch (constraint.getConstraintName()) {
		case A_Type:

			IRConstraint1Type typeConstraint = (IRConstraint1Type) constraint;

			// $cst_type$:'(?node ?index ?type)
			model.getInterpreter().compute(model.getFrame(),
					RulpFactory.createExpression(getAtomAddCstConstraintType(), model,
							RulpFactory.createString(RuleUtil.asNamedNode(node).getNamedName()),
							RulpFactory.createInteger(typeConstraint.getColumnIndex()),
							RType.toObject(typeConstraint.getColumnType())));
			break;
		case A_Max:

		case A_Min:
		case A_Uniq:
		case A_NOT_NULL:
		default:
			break;
		}
	}
}
