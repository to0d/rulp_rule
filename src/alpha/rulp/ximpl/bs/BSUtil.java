package alpha.rulp.ximpl.bs;

import static alpha.rulp.lang.Constant.F_B_AND;
import static alpha.rulp.lang.Constant.F_B_OR;
import static alpha.rulp.rule.Constant.A_BS_TRACE;

import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RulpUtil;

public class BSUtil {

	public static boolean isBSTrace(IRFrame frame) throws RException {
		return RulpUtil.asBoolean(RulpUtil.getVarValue(frame, A_BS_TRACE)).asBoolean();
	}

	public static boolean isAndNode(BSType type) {

		switch (type) {
		case LOGIC_AND:
		case STMT_AND:
			return true;
		default:
			return false;
		}
	}

	public static String getBSNodeName(BSType type, int nodeId) {

		switch (type) {
		case LOGIC_AND:
			return String.format("LA%04d", nodeId);

		case LOGIC_OR:
			return String.format("LO%04d", nodeId);

		case STMT_AND:
			return String.format("SA%04d", nodeId);

		case STMT_OR:
			return String.format("SO%04d", nodeId);

		case STMT_QUERY:
			return String.format("SQ%04d", nodeId);

		default:
			return String.format("UN%04d", nodeId);
		}
	}

	public static boolean isBSTree(IRObject obj) throws RException {

		switch (obj.getType()) {
		case LIST:
			return ReteUtil.isReteStmtNoVar((IRList) obj);

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
