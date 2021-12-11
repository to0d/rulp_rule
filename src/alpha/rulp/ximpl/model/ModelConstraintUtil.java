package alpha.rulp.ximpl.model;

import static alpha.rulp.lang.Constant.O_From;
import static alpha.rulp.rule.Constant.A_Max;
import static alpha.rulp.rule.Constant.A_Min;
import static alpha.rulp.rule.Constant.A_One_Of;
import static alpha.rulp.rule.Constant.A_Type;
import static alpha.rulp.rule.Constant.A_Uniq;
import static alpha.rulp.rule.Constant.F_LIST_STMT;

import java.util.HashMap;
import java.util.Map;

import alpha.rulp.lang.IRAtom;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RType;
import alpha.rulp.rule.IRModel;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.constraint.ConstraintFactory;
import alpha.rulp.ximpl.constraint.IRConstraint1;
import alpha.rulp.ximpl.constraint.IRConstraint1OneValue;
import alpha.rulp.ximpl.constraint.IRConstraint1Type;

public class ModelConstraintUtil {

	static final String F_CST_ADD_CONSTRAINT_MAX = "add_cst_constraint_max";

	static final String F_CST_ADD_CONSTRAINT_MIN = "add_cst_constraint_min";

	static final String F_CST_ADD_CONSTRAINT_ONE_OF = "add_cst_constraint_one_of";

	static final String F_CST_ADD_CONSTRAINT_TYPE = "add_cst_constraint_type";

	static final String F_CST_RMV_CONSTRAINT_MAX = "remove_cst_constraint_max";

	static final String F_CST_RMV_CONSTRAINT_MIN = "remove_cst_constraint_min";

	static final String F_CST_RMV_CONSTRAINT_ONE_OF = "remove_cst_constraint_one_of";

	static final String F_CST_RMV_CONSTRAINT_TYPE = "remove_cst_constraint_type";

	static final String N_CST_MAX = "$cst_max$";

	static final String N_CST_MIN = "$cst_min$";

	static final String N_CST_TYPE = "$cst_type$";

	static final String V_INDEX = "?index";

	static final String V_NODE = "?node";

	static final String V_TYPE = "?type";

	private Map<String, IRAtom> atomMap = new HashMap<>();

	private IRModel model;

	public ModelConstraintUtil(IRModel model) {
		super();
		this.model = model;
	}

	protected boolean _addMaxConstraint(IRReteNode node, IRConstraint1OneValue constraint) throws RException {

		// $cst_max$:'(?node ?index ?value)
		IRObject rst = model.getInterpreter().compute(model.getFrame(),
				RulpFactory.createExpression(_getAtom(F_CST_ADD_CONSTRAINT_MAX), model,
						RulpFactory.createString(RuleUtil.asNamedNode(node).getNamedName()),
						RulpFactory.createInteger(constraint.getColumnIndex()), constraint.getValue()));

		return RulpUtil.asBoolean(rst).asBoolean();
	}

	protected boolean _addMinConstraint(IRReteNode node, IRConstraint1OneValue constraint) throws RException {

		// $cst_max$:'(?node ?index ?value)
		IRObject rst = model.getInterpreter().compute(model.getFrame(),
				RulpFactory.createExpression(_getAtom(F_CST_ADD_CONSTRAINT_MIN), model,
						RulpFactory.createString(RuleUtil.asNamedNode(node).getNamedName()),
						RulpFactory.createInteger(constraint.getColumnIndex()), constraint.getValue()));

		return RulpUtil.asBoolean(rst).asBoolean();
	}

	protected boolean _addOneOfConstraint(IRReteNode node, IRConstraint1OneValue constraint) throws RException {

		// $cst_one_of$:'(?node ?index '(values))
		IRObject rst = model.getInterpreter().compute(model.getFrame(),
				RulpFactory.createExpression(_getAtom(F_CST_ADD_CONSTRAINT_ONE_OF), model,
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

	protected IRObject _removeMaxConstraint(IRReteNode node, IRConstraint1OneValue constraint) throws RException {

		// $cst_max$:'(?node ?index ?value)
		return model.getInterpreter().compute(model.getFrame(),
				RulpFactory.createExpression(_getAtom(F_CST_RMV_CONSTRAINT_MAX), model,
						RulpFactory.createString(RuleUtil.asNamedNode(node).getNamedName()),
						RulpFactory.createInteger(constraint.getColumnIndex()), constraint.getValue()));

	}

	protected IRObject _removeMinConstraint(IRReteNode node, IRConstraint1OneValue constraint) throws RException {

		// $cst_max$:'(?node ?index ?value)
		return model.getInterpreter().compute(model.getFrame(),
				RulpFactory.createExpression(_getAtom(F_CST_RMV_CONSTRAINT_MIN), model,
						RulpFactory.createString(RuleUtil.asNamedNode(node).getNamedName()),
						RulpFactory.createInteger(constraint.getColumnIndex()), constraint.getValue()));

	}

	protected IRObject _removeOneOfConstraint(IRReteNode node, IRConstraint1OneValue constraint) throws RException {

		// $cst_one_of$:'(?node ?index '(values))
		return model.getInterpreter().compute(model.getFrame(),
				RulpFactory.createExpression(_getAtom(F_CST_RMV_CONSTRAINT_ONE_OF), model,
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

		constraint = ConstraintFactory.rebuildConstraint(node, constraint);

		switch (constraint.getConstraintName()) {
		case A_Type:
			return _addTypeConstraint(node, (IRConstraint1Type) constraint);

		case A_Max:
			return _addMaxConstraint(node, (IRConstraint1OneValue) constraint);

		case A_Min:
			return _addMinConstraint(node, (IRConstraint1OneValue) constraint);

		case A_One_Of:
			return _addOneOfConstraint(node, (IRConstraint1OneValue) constraint);

		case A_Uniq:
		default:
			return model.getNodeGraph().addConstraint(node, constraint);
		}
	}

	public IRObject getMaxConstraint(IRReteNode node, int index) throws RException {

		// (list-stmt m from $cst_type$:'(?node ?index ?type))
		IRList stmtlist = RulpUtil.asList(model.getInterpreter().compute(model.getFrame(),
				RulpFactory.createExpression(_getAtom(F_LIST_STMT), model, O_From,
						RulpFactory.createNamedList(N_CST_MAX,
								RulpFactory.createString(RuleUtil.asNamedNode(node).getNamedName()),
								RulpFactory.createInteger(index), _getAtom(V_TYPE)))));

		if (stmtlist.size() == 0) {
			return null;
		}

		if (stmtlist.size() > 1) {
			throw new RException("multi type found: " + node.getNodeName() + ", index=" + index);
		}

		return RulpUtil.asList(stmtlist.get(0)).get(2);
	}

	public IRObject getMinConstraint(IRReteNode node, int index) throws RException {

		// (list-stmt m from $cst_type$:'(?node ?index ?type))
		IRList stmtlist = RulpUtil.asList(model.getInterpreter().compute(model.getFrame(),
				RulpFactory.createExpression(_getAtom(F_LIST_STMT), model, O_From,
						RulpFactory.createNamedList(N_CST_MIN,
								RulpFactory.createString(RuleUtil.asNamedNode(node).getNamedName()),
								RulpFactory.createInteger(index), _getAtom(V_TYPE)))));

		if (stmtlist.size() == 0) {
			return null;
		}

		if (stmtlist.size() > 1) {
			throw new RException("multi type found: " + node.getNodeName() + ", index=" + index);
		}

		return RulpUtil.asList(stmtlist.get(0)).get(2);
	}

	public RType getTypeConstraint(IRReteNode node, int index) throws RException {

		// (list-stmt m from $cst_type$:'(?node ?index ?type))
		IRList stmtlist = RulpUtil.asList(model.getInterpreter().compute(model.getFrame(),
				RulpFactory.createExpression(_getAtom(F_LIST_STMT), model, O_From,
						RulpFactory.createNamedList(N_CST_TYPE,
								RulpFactory.createString(RuleUtil.asNamedNode(node).getNamedName()),
								RulpFactory.createInteger(index), _getAtom(V_TYPE)))));

		if (stmtlist.size() == 0) {
			return null;
		}

		if (stmtlist.size() > 1) {
			throw new RException("multi type found: " + node.getNodeName() + ", index=" + index);
		}

		return RType.toType(RulpUtil.asAtom(RulpUtil.asList(stmtlist.get(0)).get(2)).getName());
	}

	public IRObject removeConstraint(IRReteNode node, IRConstraint1 constraint) throws RException {

		switch (constraint.getConstraintName()) {
		case A_Type:
			return _removeTypeConstraint(node, (IRConstraint1Type) constraint);

		case A_Max:
			return _removeMaxConstraint(node, (IRConstraint1OneValue) constraint);

		case A_Min:
			return _removeMinConstraint(node, (IRConstraint1OneValue) constraint);

		case A_One_Of:
			return _removeOneOfConstraint(node, (IRConstraint1OneValue) constraint);

		case A_Uniq:
		default:
			return model.getNodeGraph().removeConstraint(node, constraint);
		}
	}
}
