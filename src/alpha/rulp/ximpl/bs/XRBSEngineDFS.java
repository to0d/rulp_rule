package alpha.rulp.ximpl.bs;

import java.util.HashMap;
import java.util.Map;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RulpFactory;

public class XRBSEngineDFS extends AbsBSEngine implements IRBSEngine {

	protected Map<String, IRBSNode> workingStmtOrNodeMap = new HashMap<>();

	public XRBSEngineDFS(IRModel model) {
		super(model);
	}

	protected boolean _isCircularProof(IRList stmt) throws RException {
		return workingStmtOrNodeMap.containsKey(ReteUtil.uniqName(stmt));
	}

	protected void _addNode(IRBSNode node) throws RException {

		if (node.getType() == BSNodeType.STMT_OR) {

			IRList stmt = ((IRBSNodeStmt) node).getStmt();
			String uniqName = ReteUtil.uniqName(stmt);

			IRBSNode oldNode = workingStmtOrNodeMap.get(uniqName);
			if (oldNode == null) {
				workingStmtOrNodeMap.put(uniqName, node);
				return;
			}

			// The and should fail once circular proof be found
			if (oldNode.getStatus() != BSStats.COMPLETE) {
				BSFactory.incBscCircularProof();
				throw new RException(String.format("circular proof found, stmt=%s, return false", stmt));
			}

			// Use old result
			node.setStatus(BSStats.DUPLICATE);
			node.setSucc(oldNode.isSucc());
			BSFactory.incBscCacheResult();
		}
	}

	protected IRList _search(IRList tree, boolean explain) throws RException {

		IRBSNode rootNode = BSFactory.createNode(this, tree);

		if (trace) {
			trace_outln(rootNode, "create_root, " + rootNode.toString());
		}

		IRBSNode curNode = rootNode;
		IRBSNode lastNode = null;

		++bscOpSearch;

		while (rootNode.getStatus() != BSStats.COMPLETE) {

			IRBSNode oldNode = curNode;
			BSStats oldStatus = curNode.getStatus();

			switch (oldStatus) {

			case INIT:

				++bscStatusInit;

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

				++bscStatusProcess;

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

				if (curNode.getType() == BSNodeType.ENTRY_QUERY && curNode.getStatus() == BSStats.PROCESS) {
					curNode = curNode.getParentNode();
				}

				break;

			case COMPLETE:

				++bscStatusComplete;

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

			case DUPLICATE:

				++bscStatusDuplicate;

				if (trace) {
					trace_outln(curNode, "duplicate, return to parent");
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

			bscOpLoop++;
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
