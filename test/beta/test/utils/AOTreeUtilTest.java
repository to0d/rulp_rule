package beta.test.utils;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import alpha.rulp.lang.IRAtom;
import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.utils.AOTreeUtil;
import alpha.rulp.utils.AOTreeUtil.DLRVisitNode;
import alpha.rulp.utils.AOTreeUtil.IAOTreeNode;
import alpha.rulp.utils.RuleTestBase;
import alpha.rulp.utils.RulpFactory;

class AOTreeUtilTest extends RuleTestBase {

	static class XAtomAOTreeNode implements IAOTreeNode<IRAtom> {

		private IRAtom atom;

		public XAtomAOTreeNode(IRAtom atom) {
			super();
			this.atom = atom;
		}

		@Override
		public IAOTreeNode<IRAtom> getChild(int index) {
			return null;
		}

		@Override
		public int getChildCount() throws RException {
			return 0;
		}

		@Override
		public IRAtom getObj() {
			return atom;
		}

		@Override
		public boolean isAnd() throws RException {
			return false;
		}

		@Override
		public boolean isLeaf() throws RException {
			return true;
		}

		public String toString() {
			return "" + atom;
		}

	}

	static class XListAOTreeNode implements IAOTreeNode<IRAtom> {

		private ArrayList<IAOTreeNode<IRAtom>> childs = null;

		private IRExpr expr;

		private Boolean isAnd = null;

		public XListAOTreeNode(IRExpr expr) {
			super();
			this.expr = expr;
		}

		@Override
		public IAOTreeNode<IRAtom> getChild(int index) throws RException {
			return getChilds().get(index);
		}

		@Override
		public int getChildCount() throws RException {
			return getChilds().size();
		}

		public ArrayList<IAOTreeNode<IRAtom>> getChilds() throws RException {

			if (childs == null) {

				childs = new ArrayList<>();

				int size = expr.size();
				for (int i = 1; i < size; ++i) {
					childs.add(_createAONode(expr.get(i)));
				}
			}

			return childs;
		}

		@Override
		public IRAtom getObj() {
			return null;
		}

		@Override
		public boolean isAnd() throws RException {

			if (isAnd == null) {
				isAnd = expr.get(0).asString().equals("and");
			}

			return isAnd;
		}

		@Override
		public boolean isLeaf() throws RException {
			return false;
		}

		public String toString() {
			return "" + expr;
		}

	}

	static IAOTreeNode<IRAtom> _createAONode(IRObject obj) throws RException {

		switch (obj.getType()) {
		case ATOM:
			return new XAtomAOTreeNode((IRAtom) obj);

		case EXPR:
			return new XListAOTreeNode((IRExpr) obj);

		default:
			throw new RException("invalid type: " + obj);
		}
	}

	protected String _expand_and_or_tree(String input) throws RException {

		List<IRObject> objs = this._getParser().parse(input);
		assertEquals(objs.size(), 1);

		IAOTreeNode<IRAtom> aoTree = _createAONode(objs.get(0));

		ArrayList<String> visitPaths = new ArrayList<>();
		DLRVisitNode<IRAtom> vistTree = AOTreeUtil.getDLRVisitFirstTree(aoTree);

		{
			List<IRAtom> vistAtoms = AOTreeUtil.visit(vistTree);
			Collections.sort(vistAtoms, (o1, o2) -> {
				return o1.toString().compareTo(o2.toString());
			});
			String visitPath = RulpFactory.createExpression(vistAtoms).asString();
			visitPaths.add(visitPath);
		}

		while (AOTreeUtil.update(vistTree)) {

			List<IRAtom> vistAtoms = AOTreeUtil.visit(vistTree);
			Collections.sort(vistAtoms, (o1, o2) -> {
				return o1.toString().compareTo(o2.toString());
			});

			String visitPath = RulpFactory.createExpression(vistAtoms).asString();

			if (!visitPaths.contains(visitPath)) {
				visitPaths.add(visitPath);
			}
		}

		Collections.sort(visitPaths);

		return visitPaths.toString();
	}

	@Test
	void test_expand_and_or_tree_1() {

		_setup();

		_test((input) -> {
			return _expand_and_or_tree(input);
		});
	}

}
