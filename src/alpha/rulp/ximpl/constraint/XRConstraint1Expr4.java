package alpha.rulp.ximpl.constraint;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import alpha.rulp.lang.IRAtom;
import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RType;
import alpha.rulp.rule.IRContext;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRConstraint1Expr4 extends AbsRConstraint1Expr implements IRConstraint1Expr {

	protected LinkedList<IRList> matchStmtList;

	public XRConstraint1Expr4(IRExpr expr, List<IRList> matchStmtList) {

		super(expr);

		this.matchStmtList = new LinkedList<>(matchStmtList);
	}

	@Override
	public boolean addEntry(IRReteEntry entry, IRContext context) throws RException {

		IRExpr newExpr = (IRExpr) rebuiltIndexExpr(expr);
		IRObject rst = context.getInterpreter().compute(context.getFrame(), newExpr);
		return RulpUtil.asBoolean(rst).asBoolean();
	}

	public IRObject rebuiltIndexExpr(IRObject obj) throws RException {

		if (obj == null) {
			return obj;
		}

		switch (obj.getType()) {
		case ATOM: {

			IRAtom atom = (IRAtom) obj;
			if (!ReteUtil.isIndexVarName(atom.getName())) {
				return obj;
			}

			String indexVarName = atom.getName();

			int index = Integer.valueOf(indexVarName.substring(1));
			if (index < 0 || index >= matchStmtList.size()) {
				throw new RException("Invalid index var found: " + indexVarName);
			}

			return matchStmtList.get(index);
		}

		case EXPR:
		case LIST: {

			ArrayList<IRObject> newList = new ArrayList<>();
			IRIterator<? extends IRObject> it = ((IRList) obj).iterator();
			while (it.hasNext()) {
				newList.add(rebuiltIndexExpr(it.next()));
			}

			if (obj.getType() == RType.LIST) {
				return RulpFactory.createList(newList);
			}

			IRExpr expr = (IRExpr) obj;

			return expr.isEarly() ? RulpFactory.createExpressionEarly(newList) : RulpFactory.createExpression(newList);
		}

		default:
			return obj;
		}
	}
}
