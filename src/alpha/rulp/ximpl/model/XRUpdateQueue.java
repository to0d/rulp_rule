package alpha.rulp.ximpl.model;

import static alpha.rulp.rule.Constant.RETE_PRIORITY_INACTIVE;
import static alpha.rulp.rule.Constant.RETE_PRIORITY_SYSTEM;

import java.util.LinkedList;

import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.ximpl.node.RReteStage;

public class XRUpdateQueue {

	static class PriorityQueue {

		private int count = 0;

		public PriorityQueue next = null;

		public LinkedList<IRReteNode> nodeQueue = new LinkedList<>();

		public final int priority;

		public PriorityQueue(int priority) {
			super();
			this.priority = priority;
		}

		public int getPriority() {
			return priority;
		}

		public boolean isActive() {
			return count > 0;
		}

		public IRReteNode pop() {

			if (!nodeQueue.isEmpty()) {
				--count;
				return nodeQueue.removeFirst();
			}

			return null;
		}

		public void push(IRReteNode node) {
			nodeQueue.addLast(node);
			++count;
		}
	}

	private final PriorityQueue[] allQueues = new PriorityQueue[RETE_PRIORITY_SYSTEM + 1];

	private int curPriority = 0;

	private PriorityQueue curQueue = null;

	private int maxNodeCount = 0;

	private int nodeCount = 0;

	private final PriorityQueue systemQueue;

	public XRUpdateQueue() {
		super();

		systemQueue = new PriorityQueue(RETE_PRIORITY_SYSTEM);
		allQueues[systemQueue.getPriority()] = systemQueue;
		_setCurrentQueue(systemQueue);
	}

	private PriorityQueue _getQueue(int priority) {

		PriorityQueue queue = allQueues[priority];
		if (queue == null) {

			queue = new PriorityQueue(priority);
			allQueues[priority] = queue;

			/***************************************/
			// Add new queue to queue priority list
			/***************************************/
			PriorityQueue head = systemQueue;
			while (head.next != null && head.next.getPriority() > priority) {
				head = head.next;
			}

			queue.next = head.next;
			head.next = queue;
		}

		return queue;
	}

	private void _setCurrentQueue(PriorityQueue queue) {
		curQueue = queue;
		curPriority = queue.getPriority();
	}

	public int getMaxNodeCount() {
		return maxNodeCount;
	}

	public boolean hasNext() {
		return nodeCount > 0;
	}

	public IRReteNode pop() throws RException {

		if (nodeCount <= 0) {
			return null;
		}

		// Scan to next non-empty queue
		if (!curQueue.isActive()) {

			PriorityQueue nextQueue = curQueue.next;
			while (nextQueue != null && !nextQueue.isActive()) {
				nextQueue = nextQueue.next;
			}

			if (nextQueue == null) {
				throw new RException("Should have next queue");
			}

			_setCurrentQueue(nextQueue);
		}

		IRReteNode node = curQueue.pop();
		node.setReteStage(RReteStage.OutQueue);
		--nodeCount;
		return node;
	}

	public void push(IRReteNode node) throws RException {

		if (node.getReteStage() == RReteStage.InQueue) {
			return;
		}

		int priority = node.getPriority();

		// Ignore inactive nodes
		if (priority <= RETE_PRIORITY_INACTIVE) {
			return;
		}

		// Max priority
		if (priority > RETE_PRIORITY_SYSTEM) {
			priority = RETE_PRIORITY_SYSTEM;
		}

		switch (node.getRunState()) {
		case Completed:
		case Runnable:
		case Running:
			break;

		// don't process 'Failed' or 'Halting' node
		case Failed:
		case Halting:
			return;

		default:
			throw new RException("unknown state: " + node.getRunState());
		}

		PriorityQueue queue = _getQueue(priority);
		queue.push(node);
		node.setReteStage(RReteStage.InQueue);
		++nodeCount;
		if (nodeCount > maxNodeCount) {
			maxNodeCount = nodeCount;
		}

		if (priority > curPriority) {
			_setCurrentQueue(queue);
		}
	}
}
