package alpha.rulp.ximpl.action;

import static alpha.rulp.lang.Constant.A_DO;
import static alpha.rulp.lang.Constant.F_IF;
import static alpha.rulp.lang.Constant.F_LET;
import static alpha.rulp.rule.Constant.F_ADD_STMT;
import static alpha.rulp.rule.Constant.F_DEFS_S;
import static alpha.rulp.rule.Constant.F_FIX_STMT;
import static alpha.rulp.rule.Constant.F_REMOVE_STMT;

import java.util.ArrayList;
import java.util.List;

import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RType;
import alpha.rulp.rule.IRModel;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.utils.OptimizeUtil;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.utils.StmtUtil;
import alpha.rulp.ximpl.node.XTempVarBuilder;

public class ActionUtil {

	private static IRExpr _buildActionExpr(IRModel model, IRExpr expr, IRObject[] varEntry, List<IAction> actionList)
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
		case F_REMOVE_STMT:
		case F_FIX_STMT:

			IAction action = _buildSimpleAction(expr, model, varEntry);
			if (action == null) {
				return expr;
			}

			actionList.add(action);
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
				IRExpr ey = _buildActionExpr(model, ex, varEntry, actionList);
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

	private static void _buildRelatedStmtExprList(IRObject obj, List<IRExpr> exprList) throws RException {

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
			_buildRelatedStmtExprList(RulpUtil.asAtom(e0).getName(), expr, exprList);
			break;

		case FACTOR:
			_buildRelatedStmtExprList(RulpUtil.asFactor(e0).getName(), expr, exprList);
			break;

		default:
			break;
		}

	}

	private static void _buildRelatedStmtExprList(String factorName, IRExpr expr, List<IRExpr> exprList)
			throws RException {

		switch (factorName) {
		case F_ADD_STMT:
		case F_DEFS_S:
		case F_REMOVE_STMT:
		case F_FIX_STMT:
			IRList stmt = RulpUtil.asList(StmtUtil.getStmt3Object(expr));
			if (!ReteUtil.isActionEntry(stmt)) {
				throw new RException("Invalid stmt found: " + stmt);
			}

			exprList.add(expr);

			return;

		case A_DO:
		case F_IF:
		case F_LET:

			IRIterator<? extends IRObject> it = expr.listIterator(1);
			while (it.hasNext()) {
				_buildRelatedStmtExprList(it.next(), exprList);
			}

			return;

		default:

		}
	}

	private static IAction _buildSimpleAction(IRExpr expr, IRModel model, IRObject[] varEntry) throws RException {

		int argSize = expr.size();
		if (argSize != 2 && argSize != 3) {
			return null;
		}

		IRObject ex = null;

		if (argSize == 3) {
			IRObject e1 = expr.get(1);
			if (e1 != model
					&& (e1.getType() != RType.ATOM || !RulpUtil.asAtom(e1).asString().equals(model.getModelName()))) {
				return null;
			}

			ex = expr.get(2);
		} else {
			ex = expr.get(1);
		}

		if (!ReteUtil.isReteStmt(ex)) {
			return null;
		}

		IRList varStmt = (IRList) ex;

		int stmtSize = varStmt.size();
		int inheritIndexs[] = new int[stmtSize];
		IRObject stmtObjs[] = new IRObject[stmtSize];
//		int inheritCount = 0;

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
				return null;
			}

			inheritIndexs[i] = inheritIndex;
			stmtObjs[i] = null;
//			inheritCount++;
		}

//		if (inheritCount == 0) {
//			throw new RException("not var found: " + varStmt);
//		}

		AbsActionSimpleStmt action = null;

		switch (RulpUtil.asAtom(expr.get(0)).getName()) {
		case F_ADD_STMT:
		case F_DEFS_S:
			action = new XActionAddStmt();
			break;

		case F_FIX_STMT:
			action = new XActionFixStmt();
			break;

		case F_REMOVE_STMT:
			action = new XActionRemoveStmt();
			break;

		default:
			throw new RException("unknown factor: " + expr.get(0));
		}

		action.setInheritIndexs(inheritIndexs, stmtObjs);
		action.setStmtName(varStmt.getNamedName());
		action.setExpr(expr);

		return action;
	}

	public static List<IAction> buildActions(IRModel model, IRObject[] varEntry, List<IRExpr> exprList)
			throws RException {

		List<IAction> actionList = new ArrayList<>();
		IRExpr expr = _buildActionExpr(model, RulpUtil.toDoExpr(exprList), varEntry, actionList);
		if (expr != null) {
			expr = OptimizeUtil.optimizeActionExpr(expr, model);
			actionList.add(new XActionExecExpr(expr));
		}

		return actionList;
	}

	public static List<IRExpr> buildRelatedStmtExprList(IRExpr expr) throws RException {

		ArrayList<IRExpr> exprList = new ArrayList<>();
		_buildRelatedStmtExprList(expr, exprList);
		return exprList;
	}

	public static List<String> buildRelatedStmtUniqNames(List<IRExpr> stmtExprList) throws RException {

		ArrayList<String> uniqNameList = new ArrayList<>();
		for (IRExpr expr : stmtExprList) {

			IRList stmt = getRelatedStmt(expr);
			if (stmt == null) {
				continue;
			}

			String uniqName = getRelatedUniqName(stmt);
			if (!uniqNameList.contains(uniqName)) {
				uniqNameList.add(uniqName);
			}

		}

		return uniqNameList;
	}

	public static RActionType getActionType(IRExpr expr) throws RException {

		if (expr.isEmpty()) {
			return RActionType.EXPR;
		}

		IRObject e0 = expr.get(0);

		e0.asString();

		String factorName = null;
		switch (e0.getType()) {
		case ATOM:
		case FACTOR:
			factorName = e0.asString();
			break;

		default:
			return RActionType.EXPR;
		}

		switch (factorName) {
		case F_ADD_STMT:
		case F_DEFS_S:
		case F_FIX_STMT:
			return RActionType.ADD;

		case F_REMOVE_STMT:
			return RActionType.RMV;

		default:
			break;
		}

		return RActionType.EXPR;
	}

	public static IRList getRelatedStmt(IRExpr expr) throws RException {

		if (expr.isEmpty()) {
			return null;
		}

		IRObject e0 = expr.get(0);

		e0.asString();

		String factorName = null;
		switch (e0.getType()) {
		case ATOM:
		case FACTOR:
			factorName = e0.asString();
			break;

		default:
			return null;
		}

		switch (factorName) {
		case F_ADD_STMT:
		case F_DEFS_S:
		case F_REMOVE_STMT:
		case F_FIX_STMT:
			IRList stmt = RulpUtil.asList(StmtUtil.getStmt3Object(expr));
			if (!ReteUtil.isActionEntry(stmt)) {
				throw new RException("Invalid stmt found: " + stmt);
			}

			return stmt;

		default:
			break;
		}

		return null;
	}

	public static String getRelatedUniqName(IRList stmt) throws RException {

		int size = stmt.size();
		int pos = 0;

		XTempVarBuilder tmpVarBuilder = null;

		ArrayList<IRObject> newArr = null;

		for (; pos < size; ++pos) {

			IRObject obj = stmt.get(pos);

			if (obj.getType() == RType.EXPR) {

				if (newArr == null) {
					newArr = new ArrayList<>();
					for (int i = 0; i < pos; ++i) {
						newArr.add(stmt.get(i));
					}
				}

				if (tmpVarBuilder == null) {
					tmpVarBuilder = new XTempVarBuilder("?_ag_");
				}

				obj = tmpVarBuilder.next();
			}

			if (newArr != null) {
				newArr.add(obj);
			}
		}

		if (newArr != null) {
			stmt = RulpUtil.toList(stmt.getNamedName(), newArr);
		}

		return ReteUtil.uniqName(stmt);
	}

	public static boolean isSimpleAddStmtAction(IRExpr expr, IRModel model, IRObject[] varEntry) throws RException {

		if (expr.size() < 2) {
			return false;
		}

		switch (expr.get(0).asString()) {
		case F_ADD_STMT:
		case F_DEFS_S:
		case F_REMOVE_STMT:
		case F_FIX_STMT:
			IAction action = _buildSimpleAction(expr, model, varEntry);
			if (action == null) {
				return false;
			}

			return true;

		case A_DO:

			IRIterator<? extends IRObject> it = expr.listIterator(1);
			while (it.hasNext()) {

				IRObject obj = it.next();
				if (!RulpUtil.isExpr(obj) || !isSimpleAddStmtAction(RulpUtil.asExpression(obj), model, varEntry)) {
					return false;
				}

			}

			return true;

		default:
			return false;
		}
	}

	public static boolean isSimpleAddStmtAction(List<IRExpr> exprList, IRModel model, IRObject[] varEntry)
			throws RException {

		for (IRExpr expr : exprList) {
			if (!isSimpleAddStmtAction(expr, model, varEntry)) {
				return false;
			}
		}

		return true;
	}

}
