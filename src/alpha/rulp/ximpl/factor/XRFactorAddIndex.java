package alpha.rulp.ximpl.factor;

import static alpha.rulp.lang.Constant.O_False;
import static alpha.rulp.lang.Constant.O_True;
import static alpha.rulp.rule.Constant.A_Order_by;

import java.util.ArrayList;
import java.util.HashSet;

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
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.utils.StmtUtil;
import alpha.rulp.ximpl.constraint.ConstraintBuilder;
import alpha.rulp.ximpl.constraint.IRConstraint1;
import alpha.rulp.ximpl.constraint.IRConstraint1OrderBy;
import alpha.rulp.ximpl.model.IRuleFactor;
import alpha.rulp.ximpl.node.IRNodeGraph;

public class XRFactorAddIndex extends AbsAtomFactorAdapter implements IRFactor, IRuleFactor {

	public XRFactorAddIndex(String factorName) {
		super(factorName);
	}

	@Override
	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

		int argSize = args.size();

		// (add-index m '(?a ?b c) (order by '(?b ?a)))
		/********************************************/
		// Check parameters
		/********************************************/
		if (argSize != 4) {
			throw new RException("Invalid parameters: " + args);
		}

		IRModel model = RuleUtil.asModel(interpreter.compute(frame, args.get(1)));
		IRList nodeExpr = RulpUtil.asList(interpreter.compute(frame, args.get(2)));

		/********************************************/
		// Check node expression
		/********************************************/
		if (!ReteUtil.isAlphaMatchTree(nodeExpr)) {
			throw new RException(String.format("Invalid node expr: %s", nodeExpr));
		}

		ArrayList<String> nodeVarList = new ArrayList<>();
		ReteUtil.fillVarList(nodeExpr, nodeVarList);

		if (nodeVarList.isEmpty()) {
			throw new RException(String.format("no var in node expr: %s", nodeExpr));
		}

		HashSet<String> nodeVarSet = new HashSet<>(nodeVarList);
		if (nodeVarSet.size() != nodeVarList.size()) {
			throw new RException(String.format("Duplicated var found in node expr: %s", nodeExpr));
		}

		/********************************************/
		// Check order expression
		/********************************************/
		IRExpr orderExpr = RulpUtil.asExpression(args.get(3));

		IRConstraint1 cons = new ConstraintBuilder(ReteUtil._varEntry(ReteUtil.buildTreeVarList(nodeExpr)))
				.build(orderExpr, interpreter, frame);

		if (cons == null || !cons.getConstraintName().equals(A_Order_by)) {
			throw new RException(String.format("Invalid order expr: %s", orderExpr));
		}

		IRNodeGraph graph = model.getNodeGraph();
		IRReteNode node = graph.getNodeByTree(nodeExpr);
		switch (node.getReteType()) {
		case ROOT0:
		case NAME0:
		case ALPH0:
			break;

		default:
			throw new RException(String.format("Can't add index to node: %s", nodeExpr));
		}

		return RulpFactory.createBoolean(graph.addIndex(node, ((IRConstraint1OrderBy) cons).getOrderList()));
	}
}
