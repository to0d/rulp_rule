package alpha.rulp.ximpl.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.rule.IRRListener1;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.utils.XRRListener1Adapter;
import alpha.rulp.ximpl.entry.IREntryQueue;
import alpha.rulp.ximpl.entry.IRReteEntry;
import alpha.rulp.ximpl.node.RReteType;

public class XRStmtListenUpdater extends XRNodeOrderedUpdater {

	static class ListenNode {

		int lastUpdateIndex = 0;

		final XRRListener1Adapter<IRList> listenerList = new XRRListener1Adapter<>();

		final IRReteNode reteNode;

		final int[] varIndexs;

		public ListenNode(IRReteNode reteNode) throws RException {

			super();

			this.reteNode = reteNode;
			this.lastUpdateIndex = reteNode.getEntryQueue().size();

			if (RReteType.isAlphaType(reteNode.getReteType())) {

				LinkedList<Integer> indexList = new LinkedList<>();
				IRObject[] vars = reteNode.getVarEntry();

				for (int i = 0; i < vars.length; ++i) {
					IRObject var = vars[i];
					if (var != null && RulpUtil.isVarAtom(var)) {
						indexList.add(i);
					}
				}

				if (indexList.isEmpty()) {
					throw new RException("Invalid alpha node: " + reteNode.toString());
				}

				varIndexs = new int[indexList.size()];
				for (int i = 0; i < indexList.size(); ++i) {
					varIndexs[i] = indexList.get(i);
				}

			} else {
				varIndexs = null;
			}
		}

		public void addListener(IRRListener1<IRList> listener) {
			listenerList.addListener(listener);
		}

		public void updateEntry() throws RException {

			IREntryQueue entryQueue = reteNode.getEntryQueue();

			int maxEntryCount = entryQueue.size();
			if (lastUpdateIndex >= maxEntryCount) {
				return;
			}

			LinkedList<IRList> newStmts = new LinkedList<>();

			for (; lastUpdateIndex < maxEntryCount; ++lastUpdateIndex) {
				IRReteEntry entry = entryQueue.getEntryAt(lastUpdateIndex);
				IRList stmt = entry;

				if (varIndexs != null) {
					LinkedList<IRObject> aList = new LinkedList<>();
					for (int index : varIndexs) {
						aList.add(entry.get(index));
					}
					stmt = RulpFactory.createList(aList);
				}

				newStmts.add(stmt);
			}

			for (IRList stmt : newStmts) {
				listenerList.doAction(stmt);
			}
		}
	}

	protected boolean isProcessing = false;

	protected Map<IRReteNode, ListenNode> listenMap = new HashMap<>();

	protected List<ListenNode> listenNodeList = new ArrayList<>();

	public void addStatementListener(IRReteNode reteNode, IRRListener1<IRList> listener) throws RException {

		ListenNode listenNode = listenMap.get(reteNode);
		if (listenNode == null) {
			listenNode = new ListenNode(reteNode);
			listenMap.put(reteNode, listenNode);
			listenNodeList.add(listenNode);
		}

		listenNode.addListener(listener);

		// add update list
		addNodeUpdateList(reteNode);
	}

	public void update(IRModel model) throws RException {

		// Let loop running in top level
		if (XRStmtListenUpdater.this.isProcessing) {
			return;
		}

		XRStmtListenUpdater.this.isProcessing = true;

		try {

			process(model, -1);

			for (ListenNode listenNode : listenNodeList) {
				listenNode.updateEntry();
			}

		} finally {

			XRStmtListenUpdater.this.isProcessing = false;
		}
	}

}
