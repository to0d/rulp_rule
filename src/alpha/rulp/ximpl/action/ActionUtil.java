package alpha.rulp.ximpl.action;

import static alpha.rulp.lang.Constant.F_DO;
import static alpha.rulp.rule.Constant.F_ADD_STMT;
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
import alpha.rulp.ximpl.factor.XRFactorAddStmt;

public class ActionUtil {

	private static boolean _buildActionNodes(IRModel model, IRExpr expr, IRObject[] varEntry, List<IAction> actionList)
			throws RException {

		if (expr.isEmpty()) {
			return false;
		}

		IRObject e0 = expr.get(0);
		switch (e0.getType()) {
		case ATOM:
			return _buildActionNodes(model, RulpUtil.asAtom(e0).getName(), expr, varEntry, actionList);

		default:
			return false;
		}

	}

	private static boolean _buildActionNodes(IRModel model, String factorName, IRExpr expr, IRObject[] varEntry,
			List<IAction> actionList) throws RException {

		if (factorName == null) {
			return false;
		}

		switch (factorName) {
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

					return false;
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

						if (ReteUtil.equal(obj, varObj)) {
							inheritIndex = j;
							break;
						}
					}

					if (inheritIndex == -1) {
						return false;
					}

					inheritIndexs[i] = inheritIndex;
					stmtObjs[i] = null;
					inheritCount++;
				}

				if (inheritCount == 0) {
					throw new RException("not var found: " + varStmt);
				}

				actionList.add(
						new XActionAddStmt(inheritIndexs, inheritCount, stmtObjs, stmtSize, varStmt.getNamedName()));
			}

			return true;

		case F_DO:

			IRIterator<? extends IRObject> it2 = expr.listIterator(1);
			while (it2.hasNext()) {

				IRObject ex = it2.next();
				if (ex.getType() != RType.EXPR) {
					return false;
				}

				if (!_buildActionNodes(model, (IRExpr) ex, varEntry, actionList)) {
					return false;
				}
			}

			return true;

		default:
			return false;
		}
	}

	private static void _buildRelatedStmtUniqNames(IRExpr expr, List<String> uniqNames) throws RException {

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

			IRIterator<? extends IRObject> stmtIt = XRFactorAddStmt.getStmtList(expr);
			while (stmtIt.hasNext()) {

				IRList stmt = RulpUtil.asList(stmtIt.next());

				if (!ReteUtil.isActionEntry(stmt)) {
					throw new RException("Invalid stmt found: " + stmt);
				}

				uniqNames.add(ReteUtil.uniqName(stmt));
			}

			break;

		case F_DO:

			IRIterator<? extends IRObject> it = expr.listIterator(1);
			while (it.hasNext()) {

				IRObject ex = it.next();
				if (ex.getType() == RType.EXPR) {
					_buildRelatedStmtUniqNames((IRExpr) ex, uniqNames);
				}

			}

			break;

		default:

		}
	}

	public static List<String> buildRelatedStmtUniqNames(IRExpr expr) throws RException {

		ArrayList<String> uniqNames = new ArrayList<>();
		_buildRelatedStmtUniqNames(expr, uniqNames);
		return uniqNames;
	}

	public static List<IAction> buildActions(IRModel model, IRObject[] varEntry, IRExpr expr) throws RException {

		List<IAction> actionList = new ArrayList<>();

		if (!_buildActionNodes(model, expr, varEntry, actionList)) {
			actionList.clear();
			actionList.add(new XActionExecExpr(expr));
		}

		return actionList;
	}
}
