package alpha.rulp.ximpl.bs;

import static alpha.rulp.lang.Constant.F_B_AND;
import static alpha.rulp.lang.Constant.F_B_OR;
import static alpha.rulp.rule.Constant.A_BS_TRACE;

import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RulpUtil;

public class BSUtil {

	public static boolean isBSTrace(IRFrame frame) throws RException {
		return RulpUtil.asBoolean(RulpUtil.getVarValue(frame, A_BS_TRACE)).asBoolean();
	}

	public static boolean isBSTree(IRObject obj) throws RException {

		switch (obj.getType()) {
		case LIST:
			return ReteUtil.isReteStmt(obj);

		case EXPR:

			IRExpr expr = (IRExpr) obj;
			if (expr.size() < 2) {
				return false;
			}

			switch (expr.get(0).asString()) {
			case F_B_AND:
			case F_B_OR:
				break;
			default:
				return false;
			}

			IRIterator<? extends IRObject> it = expr.listIterator(1);
			while (it.hasNext()) {
				if (!isBSTree(it.next())) {
					return false;
				}
			}

			return true;

		default:
			return false;
		}

	}

}
