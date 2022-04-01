package beta.test.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import alpha.rulp.lang.IRAtom;
import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.utils.AndOrTreeUtil;
import alpha.rulp.utils.AndOrTreeUtil.DLRVisitNode;
import alpha.rulp.utils.AndOrTreeUtil.IAOTreeNode;
import alpha.rulp.utils.RuleTestBase;
import alpha.rulp.utils.RulpFactory;

class AndOrTreeUtilTest extends RuleTestBase {

	static class XAtomAOTreeNode implements IAOTreeNode<IRAtom> {

		private IRAtom atom;

		public XAtomAOTreeNode(IRAtom atom) {
			super();
			this.atom = atom;
		}

		@Override
		public boolean isAnd() throws RException {
			return false;
		}

		@Override
		public int getChildCount() throws RException {
			return 0;
		}

		@Override
		public IAOTreeNode<IRAtom> getChild(int index) {
			return null;
		}

		@Override
		public boolean isLeaf() throws RException {
			return true;
		}

		@Override
		public IRAtom getObj() {
			return atom;
		}

	}

	static class XListAOTreeNode implements IAOTreeNode<IRAtom> {

		private IRExpr expr;

		private Boolean isAnd = null;

		private ArrayList<IAOTreeNode<IRAtom>> childs = null;

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

		public XListAOTreeNode(IRExpr expr) {
			super();
			this.expr = expr;
		}

		@Override
		public boolean isAnd() throws RException {

			if (isAnd == null) {
				isAnd = expr.get(0).asString().equals("and");
			}

			return isAnd;
		}

		@Override
		public int getChildCount() throws RException {
			return getChilds().size();
		}

		@Override
		public IAOTreeNode<IRAtom> getChild(int index) throws RException {
			return getChilds().get(index);
		}

		@Override
		public boolean isLeaf() throws RException {
			return false;
		}

		@Override
		public IRAtom getObj() {
			return null;
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

	protected void _test_all(String inputTreeExpr, String expectResult) {

		try {

			List<IRObject> objs = this._getParser().parse(inputTreeExpr);
			assertEquals(objs.size(), 1);

			IAOTreeNode<IRAtom> aoTree = _createAONode(objs.get(0));

			ArrayList<String> visitPaths = new ArrayList<>();
			DLRVisitNode<IRAtom> vistTree = AndOrTreeUtil.getDLRVisitFirstTree(aoTree);

			{
				List<IRAtom> vistAtoms = AndOrTreeUtil.visit(vistTree);
				Collections.sort(vistAtoms, (o1, o2) -> {
					return o1.toString().compareTo(o2.toString());
				});
				String visitPath = RulpFactory.createExpression(vistAtoms).asString();
				visitPaths.add(visitPath);
			}

			while (AndOrTreeUtil.update(vistTree)) {

				List<IRAtom> vistAtoms = AndOrTreeUtil.visit(vistTree);
				Collections.sort(vistAtoms, (o1, o2) -> {
					return o1.toString().compareTo(o2.toString());
				});
				String visitPath = RulpFactory.createExpression(vistAtoms).asString();

				if (!visitPaths.contains(visitPath)) {
					visitPaths.add(visitPath);
				}
			}

			Collections.sort(visitPaths);

			assertEquals(expectResult, visitPaths.toString());

		} catch (RException e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}

	@Test
	void test() {
		_setup();
		_test_all("(and A B)", "[(A B)]");
		_test_all("(and (or A B) C)", "[(A C), (B C)]");
		_test_all("(and (or A B) (or C D))", "[(A C), (A D), (B C), (B D)]");
		_test_all("(and (or A B) (or C D) (or E F))",
				"[(A C E), (A C F), (A D E), (A D F), (B C E), (B C F), (B D E), (B D F)]");
		_test_all("(and (or (and (or A B) C) D) (or E F))", "[(A C E), (A C F), (B C E), (B C F), (D E), (D F)]");
		_test_all("(and (or A B) (or B A B))", "[(A B), (A), (B)]");
		_test_all("(and (or A B) (or D D))", "[(A D), (B D)]");
	}

}
