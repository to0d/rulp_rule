package alpha.rulp.ximpl.action;

import static alpha.rulp.lang.Constant.F_DO;
import static alpha.rulp.rule.Constant.F_ADD_STATEMENT;
import static alpha.rulp.rule.Constant.F_DEFS_S;

import java.util.ArrayList;
import java.util.List;

import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RType;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.entry.IRReteEntry;
import alpha.rulp.ximpl.factor.XRFactorAddStmt;
import alpha.rulp.ximpl.node.IRReteNode;

public class ActionUtil {

	static class XActionNodeAddStmt implements IActionNode1 {

		protected int inheritCount;

		protected int inheritIndexs[];

		protected IRObject stmtObjs[];

		protected int stmtSize;

		protected IRList varStmt;

		public XActionNodeAddStmt(int[] inheritIndexs, int inheritCount, IRObject[] stmtObjs, int stmtSize,
				IRList varStmt) {
			super();
			this.inheritIndexs = inheritIndexs;
			this.inheritCount = inheritCount;
			this.stmtObjs = stmtObjs;
			this.stmtSize = stmtSize;
			this.varStmt = varStmt;
		}

		@Override
		public void doAction(IRReteNode node, IRReteEntry entry) throws RException {

			ArrayList<IRObject> elements = new ArrayList<>();
			for (int i = 0; i < stmtSize; ++i) {

				IRObject obj = stmtObjs[i];
				if (obj == null) {

					int inheritIndex = inheritIndexs[i];
					if (inheritIndex == -1) {
						throw new RException("invalid inherit index: " + inheritIndex);
					}

					obj = entry.get(inheritIndex);
				}

				elements.add(obj);
			}

			if (varStmt.getNamedName() != null) {
				node.getModel().addStatement(RulpFactory.createNamedList(elements, varStmt.getNamedName()));
			} else {
				node.getModel().addStatement(RulpFactory.createList(elements));
			}
		}

		public String toString() {
			return "" + varStmt;
		}
	}

	private static boolean _buildActionNodes(IRExpr expr, IRObject[] varEntry, List<IActionNode1> actionList)
			throws RException {

		if (expr.isEmpty()) {
			return false;
		}

		IRObject e0 = expr.get(0);
		switch (e0.getType()) {
		case ATOM:
			return _buildActionNodes(RulpUtil.asAtom(e0).getName(), expr, varEntry, actionList);

		default:
			return false;
		}

	}

	private static boolean _buildActionNodes(String factorName, IRExpr expr, IRObject[] varEntry,
			List<IActionNode1> actionList) throws RException {

		if (factorName == null) {
			return false;
		}

		switch (factorName) {
		case F_ADD_STATEMENT:
		case F_DEFS_S:

			IRIterator<? extends IRObject> it1 = expr.listIterator(1);
			while (it1.hasNext()) {

				IRObject ex = it1.next();
				if (!ReteUtil.isReteStmt(ex)) {
					return false;
				}

				actionList.add(addStmtAction((IRList) ex, varEntry));
			}

			return true;

		case F_DO:

			IRIterator<? extends IRObject> it2 = expr.listIterator(1);
			while (it2.hasNext()) {

				IRObject ex = it2.next();
				if (ex.getType() != RType.EXPR) {
					return false;
				}

				if (!_buildActionNodes((IRExpr) ex, varEntry, actionList)) {
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
		case F_ADD_STATEMENT:
		case F_DEFS_S:

			IRIterator<? extends IRObject> stmtIt = XRFactorAddStmt.getStmtList(expr);
			while (stmtIt.hasNext()) {

				IRList stmt = RulpUtil.asList(stmtIt.next());

				if (!ReteUtil.isAlphaMatchTree(stmt)) {
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

	public static IActionNode1 addStmtAction(IRList varStmt, IRObject[] varEntry) throws RException {

		if (!ReteUtil.isReteStmt(varStmt)) {
			throw new RException("invalid var stmt: " + varStmt);
		}

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
				throw new RException("Inherit Index not found: " + varStmt);
			}

			inheritIndexs[i] = inheritIndex;
			stmtObjs[i] = null;
			inheritCount++;
		}

		if (inheritCount == 0) {
			throw new RException("not var found: " + varStmt);
		}

		return new XActionNodeAddStmt(inheritIndexs, inheritCount, stmtObjs, stmtSize, varStmt);
	}

	public static List<String> buildRelatedStmtUniqNames(IRExpr expr) throws RException {

		ArrayList<String> uniqNames = new ArrayList<>();
		_buildRelatedStmtUniqNames(expr, uniqNames);
		return uniqNames;
	}

	public static List<IActionNode1> tryBuildActionNodes(IRObject[] varEntry, IRExpr expr) throws RException {

		List<IActionNode1> actionList = new ArrayList<>();

		if (!_buildActionNodes(expr, varEntry, actionList)) {
			return null;
		}

		return actionList;
	}
}
