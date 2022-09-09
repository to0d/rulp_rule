package alpha.rulp.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import alpha.rulp.lang.RException;

/**
 * AndOrTree
 * 
 * @author 921511672
 *
 */
public class AOTreeUtil {

	public static class DLRVisitNode<T> {

		public final IAOTreeNode<T> aoNode;

		public int lastUpdateIndex = 0;

		public boolean expend = false;

		public String toString() {
			return "" + aoNode;
		}

		public ArrayList<DLRVisitNode<T>> childNodes = null;

		public DLRVisitNode(IAOTreeNode<T> aoNode) {
			super();
			this.aoNode = aoNode;
		}

		public boolean isLeaf() throws RException {
			return aoNode.isLeaf();
		}
	}

	public static interface IAOTreeNode<T> {

		public IAOTreeNode<T> getChild(int index) throws RException;

		public int getChildCount() throws RException;

		public T getObj();

		public boolean isAnd() throws RException;

		public boolean isLeaf() throws RException;
	}

	private static <T> void _expend(DLRVisitNode<T> visitTree) throws RException {

		if (visitTree.expend) {
			return;
		}

		if (!visitTree.aoNode.isLeaf()) {

			if (visitTree.aoNode.getChildCount() == 0) {
				throw new RException("no child found for node: " + visitTree.aoNode.getObj());
			}

			visitTree.childNodes = new ArrayList<>();

			// AND: visit all child
			if (visitTree.aoNode.isAnd()) {

				int odChildCount = visitTree.aoNode.getChildCount();
				for (int i = 0; i < odChildCount; ++i) {
					DLRVisitNode<T> childNode = new DLRVisitNode<T>(visitTree.aoNode.getChild(i));
					_expend(childNode);
					visitTree.childNodes.add(childNode);
				}

			}
			// OR: visit first child
			else {

				DLRVisitNode<T> childNode = new DLRVisitNode<T>(visitTree.aoNode.getChild(0));
				_expend(childNode);
				visitTree.childNodes.add(childNode);
			}
		}

		visitTree.lastUpdateIndex = 0;
		visitTree.expend = true;
	}

	private static <T> void _visit(ArrayList<T> list, DLRVisitNode<T> node, Set<String> nameSet) throws RException {

		if (node.isLeaf()) {

			T obj = node.aoNode.getObj();
			String name = obj.toString();
			if (nameSet.contains(name)) {
				return;
			}
			list.add(obj);
			nameSet.add(name);
			return;
		}

		if (node.childNodes.size() == 1) {
			_visit(list, node.childNodes.get(0), nameSet);
			return;
		}

		for (DLRVisitNode<T> child : node.childNodes) {
			_visit(list, child, nameSet);
		}
	}

	public static <T> DLRVisitNode<T> getDLRVisitFirstTree(IAOTreeNode<T> aoTree) throws RException {

		// DLR: Preorder Traversal

		DLRVisitNode<T> root = new DLRVisitNode<T>(aoTree);
		_expend(root);
		return root;
	}

	public static <T> boolean update(DLRVisitNode<T> visitTree) throws RException {

		if (visitTree.isLeaf()) {
			return false;
		}

		// And node
		if (visitTree.aoNode.isAnd()) {

			int size = visitTree.childNodes.size();
			while (visitTree.lastUpdateIndex < size) {

				DLRVisitNode<T> child = visitTree.childNodes.get(visitTree.lastUpdateIndex);

				if (update(child)) {

					// Rebuild left nodes
					for (int i = 0; i < visitTree.lastUpdateIndex; ++i) {
						DLRVisitNode<T> leftChild = new DLRVisitNode<T>(visitTree.aoNode.getChild(i));
						_expend(leftChild);
						visitTree.childNodes.set(i, leftChild);
					}

					visitTree.lastUpdateIndex = 0;

					return true;
				}

				++visitTree.lastUpdateIndex;
			}

		}
		// Or node
		else {

			DLRVisitNode<T> child = visitTree.childNodes.get(0);
			if (update(child)) {
				return true;
			}

			visitTree.lastUpdateIndex++;
			if (visitTree.lastUpdateIndex < visitTree.aoNode.getChildCount()) {
				DLRVisitNode<T> childNode = new DLRVisitNode<T>(visitTree.aoNode.getChild(visitTree.lastUpdateIndex));
				_expend(childNode);
				visitTree.childNodes.set(0, childNode);
				return true;
			}
		}

		return false;
	}

	public static <T> List<T> visit(DLRVisitNode<T> vistTree) throws RException {
		ArrayList<T> list = new ArrayList<>();
		_visit(list, vistTree, new HashSet<>());
		return list;
	}
}
