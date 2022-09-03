package alpha.rulp.ximpl.bs;

import static alpha.rulp.lang.Constant.F_B_AND;
import static alpha.rulp.lang.Constant.F_B_OR;

import java.util.ArrayList;
import java.util.List;

import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.ximpl.action.IAction;
import alpha.rulp.ximpl.node.SourceNode;

public class BSFactory {

	public static void addChild(IRBSEngine engine, AbsBSNode parent, AbsBSNode child) throws RException {

		if (engine.isTrace()) {
			engine.trace_outln(parent, String.format("add child, type=%s, name=%s", child.getType(), child.nodeName));
		}

		parent.addChild(child);
	}

	public static AbsBSEngine createEngine(IRModel model, BSSearchType st) throws RException {

		if (st != null && st != BSSearchType.DFS) {
			throw new RException("unsupport search type: " + st);
		}

		return new XRBSEngineDFS(model);
	}

	public static AbsBSNode createNode(IRBSEngine engine, IRList tree) throws RException {

		switch (tree.getType()) {
		case LIST:
			return createNodeStmtOr(engine, tree);

		case EXPR:

			IRExpr expr = (IRExpr) tree;

			ArrayList<IRList> stmtList = new ArrayList<>();
			IRIterator<? extends IRObject> it = expr.listIterator(1);
			while (it.hasNext()) {
				stmtList.add((IRList) it.next());
			}

			switch (expr.get(0).asString()) {
			case F_B_AND:
				return createNodeLogicAnd(engine, stmtList);

			case F_B_OR:
				return createNodeLogicOr(engine, stmtList);

			default:
			}

			break;

		default:
		}

		throw new RException("invalid bs tree: " + tree);
	}

	public static AbsBSNode createNodeLogicAnd(IRBSEngine engine, List<IRList> stmtList) throws RException {

		XRBSNodeLogicAnd node = new XRBSNodeLogicAnd();
		node.stmtList = stmtList;
		engine.addNode(node);

		return node;
	}

	public static AbsBSNode createNodeLogicOr(IRBSEngine engine, List<IRList> stmtList) throws RException {

		XRBSNodeLogicOr node = new XRBSNodeLogicOr();
		node.stmtList = stmtList;
		engine.addNode(node);

		return node;
	}

	public static AbsBSNode createNodeStmtAnd(IRBSEngine engine, IRList stmt, SourceNode sourceNode, IAction action)
			throws RException {

		XRBSNodeStmtAnd node = new XRBSNodeStmtAnd();
		node.stmt = stmt;
		node.sourceNode = sourceNode;
		node.action = action;
		engine.addNode(node);

		return node;
	}

	public static AbsBSNode createNodeStmtOr(IRBSEngine engine, IRList stmt) throws RException {

		XRBSNodeStmtOr node = new XRBSNodeStmtOr();
		node.stmt = stmt;
		engine.addNode(node);

		return node;
	}

	public static AbsBSNode createNodeStmtQuery(IRBSEngine engine, List<IRList> stmtList) throws RException {

		XRBSNodeStmtQuery node = new XRBSNodeStmtQuery();
		node.queryReteNodeTree = RulpFactory.createList(stmtList);
		engine.addNode(node);

		return node;
	}
}
