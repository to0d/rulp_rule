package alpha.rulp.ximpl.action;

import java.util.ArrayList;

import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XActionAddStmt implements IAction {

	protected int inheritCount;

	protected int inheritIndexs[];

	protected IRObject stmtObjs[];

	protected int stmtSize;

	protected String stmtName;

	protected String _toString;

	public XActionAddStmt(int[] inheritIndexs, int inheritCount, IRObject[] stmtObjs, int stmtSize,
			String stmtName) {

		super();
		this.inheritIndexs = inheritIndexs;
		this.inheritCount = inheritCount;
		this.stmtObjs = stmtObjs;
		this.stmtSize = stmtSize;
		this.stmtName = stmtName;

	}

	@Override
	public void doAction(IRReteNode node, IRReteEntry entry) throws RException {

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
			node.getModel().addStatement(RulpFactory.createNamedList(elements, stmtName));
		} else {
			node.getModel().addStatement(RulpFactory.createList(elements));
		}
	}

	public String toString() {

		if (_toString == null) {

			StringBuffer sb = new StringBuffer();

			sb.append("(-> ");

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

			sb.append("))");

			_toString = sb.toString();
		}

		return _toString;
	}
}