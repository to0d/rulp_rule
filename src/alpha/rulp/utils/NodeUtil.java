package alpha.rulp.utils;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.rule.IRRule;
import alpha.rulp.ximpl.node.IRReteNode;

public class NodeUtil {

	public static interface IReteNodeVisitor {
		public boolean visit(IRReteNode node) throws RException;
	}

	public static int recalcuatePriority(IRModel model, IRReteNode node) throws RException {

		int priority = 0;

		for (IRRule rule : model.getNodeGraph().getRelatedRules(node)) {
			int rulePriority = rule.getPriority();
			if (rulePriority > priority) {
				priority = rulePriority;
			}
		}

		return priority;
	}

	public static IRReteNode travelReteParentNodeByPostorder(IRReteNode node, IReteNodeVisitor visitor)
			throws RException {

		LinkedList<IRReteNode> queryStack = new LinkedList<>();
		Set<IRReteNode> inStack = new HashSet<>();
		Set<IRReteNode> expendedNodes = new HashSet<>();
		queryStack.add(node);
		inStack.add(node);

		/******************************************************/
		// Post order
		/******************************************************/
		while (!queryStack.isEmpty()) {

			IRReteNode topNode = queryStack.getLast();
			if (!expendedNodes.contains(topNode)) {

				if (topNode.getParentNodes() != null) {
					for (IRReteNode parent : topNode.getParentNodes()) {
						if (!inStack.contains(parent)) {
							queryStack.add(parent);
							inStack.add(parent);
						}
					}
				}

				expendedNodes.add(topNode);

			} else {

				if (visitor.visit(topNode)) {
					return topNode;
				}

				queryStack.removeLast();
			}

		}

		return null;
	}
}
