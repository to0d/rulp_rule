package alpha.rulp.ximpl.bs;

import java.util.HashMap;
import java.util.Map;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RulpFactory;

public class XRBSEngineDFS extends AbsBSEngine implements IRBSEngine {

	protected Map<String, AbsBSNode> visitingOrNodeMap = new HashMap<>();

	public XRBSEngineDFS(IRModel model) {
		super(model);
	}

	protected void _addNode(AbsBSNode node) throws RException {

		if (node.getType() == BSNodeType.STMT_OR) {

			IRList stmt = ((IRBSNodeStmt) node).getStmt();
			String uniqName = ReteUtil.uniqName(stmt);

			// The and should fail once circular proof be found
			if (visitingOrNodeMap.containsKey(uniqName)) {
				this.bscCircularProof++;
				throw new RException(String.format("circular proof found, stmt=%s, return false", stmt));
			}

			visitingOrNodeMap.put(uniqName, node);
		}
	}

	protected boolean _isCircularProof(IRList stmt) throws RException {
		return visitingOrNodeMap.containsKey(ReteUtil.uniqName(stmt));
	}

	protected IRList _search(IRList tree, boolean explain) throws RException {

		IRBSNode rootNode = BSFactory.createNode(this, tree);

		if (trace) {
			trace_outln(rootNode, "create_root, " + rootNode.toString());
		}

		IRBSNode curNode = rootNode;
		IRBSNode lastNode = null;

		this.bscOpSearch++;

		while (rootNode.getStatus() != BSStats.COMPLETE) {

			IRBSNode oldNode = curNode;
			BSStats oldStatus = curNode.getStatus();

			switch (oldStatus) {

			case INIT:
				this.bscStatusInit++;

				if (trace) {
					trace_outln(curNode, "init begin, " + curNode.toString());
				}

				try {
					curNode.init();
				} catch (RException e) {

					if (trace) {
						trace_outln(curNode, e.getExceptionMessage());
					}

					curNode.setStatus(BSStats.COMPLETE);
					curNode.setSucc(false);

				} finally {

					if (trace) {
						trace_outln(curNode, String.format("init end, rst=%s, status=%s, %s", "" + curNode.isSucc(),
								curNode.getStatus(), curNode.getStatusString()));
					}
				}

				if (curNode.getStatus() == BSStats.PROCESS && curNode.getChildCount() > 0) {
					curNode = curNode.getChild(0);
				}

				break;

			case PROCESS:

				this.bscStatusProcess++;

				if (trace) {
					trace_outln(curNode, "process begin");
				}

				boolean hasChild = curNode.getChildCount() > 0;

				if (hasChild) {
					if (lastNode.getParentNode() != curNode) {
						throw new RException(
								String.format("%s is not child of %s", lastNode.getNodeName(), curNode.getNodeName()));
					}
				} else {
					if (lastNode != curNode) {
						throw new RException(String.format("node match: last=%s,cur=%s", lastNode.getNodeName(),
								curNode.getNodeName()));
					}
				}

				int nextChildIndex = lastNode.getIndexInParent() + 1;

				try {

					curNode.process(lastNode);

					// No child need update, mark the parent's result is true
					if (curNode.getStatus() == BSStats.PROCESS && curNode.getChildCount() > 0
							&& nextChildIndex >= curNode.getChildCount()) {
						curNode.setStatus(BSStats.COMPLETE);
						curNode.setSucc(BSUtil.isAndNode(curNode.getType()));
					}

				} finally {

					if (trace) {

						if (curNode.getChildCount() > 0) {
							trace_outln(curNode,
									String.format("process end, rst=%s, status=%s, child=%d/%d, %s",
											"" + curNode.isSucc(), curNode.getStatus(), nextChildIndex,
											curNode.getChildCount(), curNode.getStatusString()));
						} else {
							trace_outln(curNode, String.format("process end, rst=%s, status=%s, %s",
									"" + curNode.isSucc(), curNode.getStatus(), curNode.getStatusString()));
						}
					}
				}

				if (curNode.getChildCount() > 0) {
					// Process next node if have more child
					if (curNode.getStatus() == BSStats.PROCESS && nextChildIndex < curNode.getChildCount()) {
						curNode = curNode.getChild(nextChildIndex);
					}
				}

				break;

			case COMPLETE:

				this.bscStatusComplete++;

				// re-check
				if (curNode.isSucc() && curNode.needComplete()) {

					if (trace) {
						trace_outln(curNode, "complete begin");
					}

					try {
						curNode.complete();
					} finally {
						if (trace) {
							trace_outln(curNode, String.format("complete end, rst=%s", "" + curNode.isSucc()));
						}
					}
				}

				if (curNode == rootNode) {
					break;
				}

				curNode = curNode.getParentNode();
				break;

			default:
				throw new RException("unknown status: " + curNode.getStatus());

			}

			if (curNode == oldNode && curNode.getStatus() == oldStatus) {
				throw new RException("dead loop found: " + curNode);

			} else {

				lastNode = oldNode;

				if (trace) {
					if (curNode != oldNode) {
						trace_outln(oldNode, "route to " + curNode.getNodeName());
					}
				}
			}

			this.bscOpLoop++;
		}

		rootNode.complete();
		if (trace) {
			trace_outln(rootNode, String.format("return %s", "" + rootNode.isSucc()));
		}

		if (!rootNode.isSucc()) {
			return RulpFactory.emptyConstList();
		}

		return rootNode.buildResultTree(explain);
	}

}
