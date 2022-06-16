package alpha.rulp.ximpl.action;

import static alpha.rulp.lang.Constant.A_DO;
import static alpha.rulp.lang.Constant.F_IF;
import static alpha.rulp.rule.Constant.F_ADD_STMT;
import static alpha.rulp.rule.Constant.F_ASSUME_STMT;
import static alpha.rulp.rule.Constant.F_DEFS_S;

import java.util.ArrayList;
import java.util.List;

import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RType;
import alpha.rulp.rule.IRModel;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.utils.StmtUtil;

public class ActionUtil {

	private static void _buildRelatedStmtUniqNames(IRObject obj, List<String> uniqNames) throws RException {

		if (obj.getType() != RType.EXPR) {
			return;
		}

		IRExpr expr = (IRExpr) obj;
		if (expr.isEmpty()) {
			return;
		}

		IRObject e0 = expr.get(0);
		switch (e0.getType()) {
		case ATOM:
			_buildRelatedStmtUniqNames(RulpUtil.asAtom(e0).getName(), expr, uniqNames);
			break;

		case FACTOR:
			_buildRelatedStmtUniqNames(RulpUtil.asFactor(e0).getName(), expr, uniqNames);
			break;

		default:
			break;
		}

	}

	private static void _buildRelatedStmtUniqNames(String factorName, IRExpr expr, List<String> uniqNames)
			throws RException {

		switch (factorName) {
		case F_ADD_STMT:
		case F_DEFS_S:
		case F_ASSUME_STMT:
			IRList stmt = RulpUtil.asList(StmtUtil.getStmt3Object(expr));
			if (!ReteUtil.isActionEntry(stmt)) {
				throw new RException("Invalid stmt found: " + stmt);
			}

			uniqNames.add(ReteUtil.uniqName(stmt));
			return;

		case A_DO:
		case F_IF:

			IRIterator<? extends IRObject> it = expr.listIterator(1);
			while (it.hasNext()) {
				_buildRelatedStmtUniqNames(it.next(), uniqNames);
			}

			return;

		default:

		}
	}

	private static IRExpr _rebuildActionExpr(IRModel model, IRExpr expr, IRObject[] varEntry, List<IAction> actionList)
			throws RException {

		if (expr.isEmpty()) {
			return null;
		}

		IRObject e0 = expr.get(0);
		if (e0.getType() != RType.ATOM) {
			return expr;
		}

		switch (RulpUtil.asAtom(e0).getName()) {
		case F_ADD_STMT:
		case F_DEFS_S:

			int index = 0;

			IRIterator<? extends IRObject> it1 = expr.listIterator(1);
			NEXT: while (it1.hasNext()) {

				IRObject ex = it1.next();
				index++;

				if (!ReteUtil.isReteStmt(ex)) {

					// (-> m '(stmt))
					if (index == 1 && ex.getType() == RType.ATOM
							&& RulpUtil.asAtom(ex).asString().equals(model.getModelName())) {
						continue NEXT;
					}

					return expr;
				}

				IRList varStmt = (IRList) ex;

				int stmtSize = varStmt.size();
				int inheritIndexs[] = new int[stmtSize];
				IRObject stmtObjs[] = new IRObject[stmtSize];
				int inheritCount = 0;

				for (int i = 0; i < stmtSize; ++i) {

					IRObject obj = varStmt.get(i);

					if (!RulpUtil.isVarAtom(obj)) {
						stmtObjs[i] = obj;
						inheritIndexs[i] = -1;
						continue;
					}

					int inheritIndex = -1;

					for (int j = 0; j < varEntry.length; ++j) {

						IRObject varObj = varEntry[j];
						if (varObj == null) {
							continue;
						}

						if (RulpUtil.equal(obj, varObj)) {
							inheritIndex = j;
							break;
						}
					}

					if (inheritIndex == -1) {
						return expr;
					}

					inheritIndexs[i] = inheritIndex;
					stmtObjs[i] = null;
					inheritCount++;
				}

				if (inheritCount == 0) {
					throw new RException("not var found: " + varStmt);
				}

				XActionAddStmt action = new XActionAddStmt(inheritIndexs, inheritCount, stmtObjs, stmtSize,
						varStmt.getNamedName());
				action.setExpr(expr);

				actionList.add(action);
			}

			return null;

		case A_DO:

			int pos = 1;
			int size = expr.size();
			IRExpr newExpr = null;

			for (; pos < size; ++pos) {

				IRObject obj = expr.get(pos);
				if (obj.getType() != RType.EXPR) {
					break;
				}

				IRExpr ex = (IRExpr) obj;
				IRExpr ey = _rebuildActionExpr(model, ex, varEntry, actionList);
				if (ex == ey || ey != null) {
					newExpr = ey;
					break;
				}
			}

			if (pos == 0) {
				return expr;
			}

			if (pos == size || newExpr == null) {
				return null;
			}

			if (pos == (size - 1)) {
				return newExpr;
			}

			ArrayList<IRObject> newArray = new ArrayList<>();
			newArray.add(newExpr);

			for (int j = pos + 1; j < size; ++j) {
				newArray.add(expr.get(j));
			}

			return RulpUtil.toDoExpr(newArray);

		default:
			return expr;
		}
	}

	public static List<IAction> buildActions(IRModel model, IRObject[] varEntry, List<IRExpr> exprList)
			throws RException {

		List<IAction> actionList = new ArrayList<>();
		IRExpr newExpr = _rebuildActionExpr(model, RulpUtil.toDoExpr(exprList), varEntry, actionList);
		if (newExpr != null) {
			actionList.add(new XActionExecExpr(newExpr));
		}

		return actionList;
	}

	public static List<String> buildRelatedStmtUniqNames(IRExpr expr) throws RException {

		ArrayList<String> uniqNames = new ArrayList<>();
		_buildRelatedStmtUniqNames(expr, uniqNames);
		return uniqNames;
	}

}
