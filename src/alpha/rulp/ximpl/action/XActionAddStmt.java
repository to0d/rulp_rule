package alpha.rulp.ximpl.action;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRContext;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XActionAddStmt implements IAction {

	protected String _toString;

	protected IRExpr expr;

	protected int index = -1;

	protected int inheritCount;

	protected int inheritIndexs[];

	protected String stmtName;

	protected IRObject stmtObjs[];

	protected int stmtSize;

	public XActionAddStmt(int[] inheritIndexs, int inheritCount, IRObject[] stmtObjs, int stmtSize, String stmtName) {

		super();
		this.inheritIndexs = inheritIndexs;
		this.inheritCount = inheritCount;
		this.stmtObjs = stmtObjs;
		this.stmtSize = stmtSize;
		this.stmtName = stmtName;

	}

	@Override
	public void doAction(IRReteEntry entry, IRContext context) throws RException {

		ArrayList<IRObject> elements = new ArrayList<>();
		for (int i = 0; i < stmtSize; ++i) {

			IRObject obj = stmtObjs[i];
			if (obj == null) {

				int inheritIndex = inheritIndexs[i];
				if (inheritIndex == -1) {
					throw new RException("invalid7 inherit index: " + inheritIndex);
				}

				obj = entry.get(inheritIndex);
			}

			elements.add(obj);
		}

		if (stmtName != null) {
			context.getModel().addStatement(RulpFactory.createNamedList(elements, stmtName));
		} else {
			context.getModel().addStatement(RulpFactory.createList(elements));
		}
	}

	@Override
	public RActionType getActionType() {
		return RActionType.ADD;
	}

	@Override
	public IRExpr getExpr() {
		return expr;
	}

	@Override
	public int getIndex() {
		return index;
	}

	public void setExpr(IRExpr expr) {
		this.expr = expr;
	}

	@Override
	public void setIndex(int index) {
		this.index = index;
	}

	@Override
	public String toString() {

		if (_toString == null) {

			StringBuffer sb = new StringBuffer();

			if (stmtName != null) {
				sb.append(String.format("%s:", stmtName));
			}

			sb.append("'(");

			for (int i = 0; i < stmtSize; ++i) {

				if (i != 0) {
					sb.append(", ");
				}

				IRObject obj = stmtObjs[i];
				if (obj == null) {
					sb.append(String.format("?%d", inheritIndexs[i]));
				} else {
					sb.append(obj.toString());
				}

			}

			sb.append(")");

			_toString = sb.toString();
		}

		return _toString;

	}

	protected List<IRExpr> stmtExprList = null;

	@Override
	public List<IRExpr> getStmtExprList() {
		if (stmtExprList == null) {
			stmtExprList = new LinkedList<>();
			stmtExprList.add(expr);
		}
		return stmtExprList;
	}

}