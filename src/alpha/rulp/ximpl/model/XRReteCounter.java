package alpha.rulp.ximpl.model;

import static alpha.rulp.rule.RCountType.COUNT_TYPE_NUM;
import static alpha.rulp.rule.RReteStatus.ASSUME;
import static alpha.rulp.rule.RReteStatus.DEFINE;
import static alpha.rulp.rule.RReteStatus.FIXED_;
import static alpha.rulp.rule.RReteStatus.REASON;
import static alpha.rulp.rule.RReteStatus.REMOVE;
import static alpha.rulp.rule.RReteStatus.TEMP__;
import static alpha.rulp.ximpl.node.RReteType.RETE_TYPE_TOTAL;

import java.util.List;

import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.rule.RCountType;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.ximpl.node.IRReteNodeCounter;
import alpha.rulp.ximpl.node.RReteType;

public class XRReteCounter implements IRReteNodeCounter {

	private final long countMatrix[][];

	private final IReteNodeMatrix reteNodeMatrix;

	public XRReteCounter(IReteNodeMatrix reteNodeMatrix) {

		super();
		this.reteNodeMatrix = reteNodeMatrix;
		this.countMatrix = new long[RETE_TYPE_TOTAL][COUNT_TYPE_NUM];
		for (int i = 0; i < RETE_TYPE_TOTAL; ++i) {
			for (int j = 0; j < COUNT_TYPE_NUM; ++j) {
				this.countMatrix[i][j] = -1;
			}
		}
	}

	@Override
	public long getCount(RReteType reteType, RCountType countType) throws RException {

		int ridx = reteType.getIndex();
		int cidx = countType.getIndex();

		long value = countMatrix[ridx][cidx];

		if (value == -1) {

			IRModel model = reteNodeMatrix.getModel();
			List<? extends IRReteNode> nodeList = reteNodeMatrix.getNodeList(reteType);
			value = 0;

			switch (countType) {
			case EntryDefinedCount:
				for (IRReteNode node : nodeList) {
					value += node.getEntryQueue().getEntryCounter().getEntryCount(DEFINE);
				}
				break;

			case EntryFixedCount:
				for (IRReteNode node : nodeList) {
					value += node.getEntryQueue().getEntryCounter().getEntryCount(FIXED_);
				}
				break;

			case EntryAssumeCount:
				for (IRReteNode node : nodeList) {
					value += node.getEntryQueue().getEntryCounter().getEntryCount(ASSUME);
				}
				break;

			case EntryReasonCount:
				for (IRReteNode node : nodeList) {
					value += node.getEntryQueue().getEntryCounter().getEntryCount(REASON);
				}
				break;

			case EntryDropCount:
				for (IRReteNode node : nodeList) {
					value += node.getEntryQueue().getEntryCounter().getEntryCount(null);
				}
				break;

			case EntryRemoveCount:
				for (IRReteNode node : nodeList) {
					value += node.getEntryQueue().getEntryCounter().getEntryCount(REMOVE);
				}
				break;

			case EntryTempCount:
				for (IRReteNode node : nodeList) {
					value += node.getEntryQueue().getEntryCounter().getEntryCount(TEMP__);
				}
				break;

			case EntryNullCount:
				for (IRReteNode node : nodeList) {
					value += node.getEntryQueue().getEntryCounter().getEntryNullCount();
				}
				break;

			case RedundantCount:
				for (IRReteNode node : nodeList) {
					value += node.getEntryQueue().getRedundantCount();
				}
				break;

			case NodeBindFromCount:
				for (IRReteNode node : nodeList) {
					value += model.getNodeGraph().listBindFromNodes(node).size();
				}
				break;

			case NodeBindToCount:
				for (IRReteNode node : nodeList) {
					value += model.getNodeGraph().listBindToNodes(node).size();
				}
				break;

			case NodeExistCount:
				value = nodeList.size();
				break;

			case NodeTotalCount:
				value = reteNodeMatrix.getNodeCreateType(reteType);
				break;

			case NodeSourceCount:
				for (IRReteNode node : nodeList) {
					value += RuleUtil.listSource(reteNodeMatrix.getModel(), node).size();
				}

				break;

			case MatchCount:
				for (IRReteNode node : nodeList) {
					value += node.getNodeMatchCount();
				}
				break;

			case ExecCount:
				for (IRReteNode node : nodeList) {
					value += node.getNodeExecCount();
				}
				break;

			case IdleCount:
				for (IRReteNode node : nodeList) {
					value += node.getNodeIdleCount();
				}
				break;

			case UpdateCount:
				for (IRReteNode node : nodeList) {
					value += node.getEntryQueue().getUpdateCount();
				}
				break;

			case FailedCount:
				for (IRReteNode node : nodeList) {
					value += node.getNodeFailedCount();
				}
				break;

			case MinLevel:

				if (!nodeList.isEmpty()) {
					value = -1;
					for (IRReteNode node : nodeList) {
						int level = node.getReteLevel();
						if (value == -1 || value > level) {
							value = level;
						}
					}
				}

				break;

			case MinPriority:

				if (!nodeList.isEmpty()) {
					value = -1;
					for (IRReteNode node : nodeList) {
						int priority = node.getPriority();
						if (value == -1 || value > priority) {
							value = priority;
						}
					}
				}

				break;

			case MaxLevel:

				if (!nodeList.isEmpty()) {
					value = -1;
					for (IRReteNode node : nodeList) {
						int level = node.getReteLevel();
						if (value == -1 || value < level) {
							value = level;
						}
					}
				}

				break;

			case MaxPriority:

				if (!nodeList.isEmpty()) {
					value = -1;
					for (IRReteNode node : nodeList) {
						int priority = node.getPriority();
						if (value == -1 || value < priority) {
							value = priority;
						}
					}
				}

				break;

			case QueryFetch:
				for (IRReteNode node : nodeList) {
					value += node.getEntryQueue().getQueryFetchCount();
				}
				break;

			case QueryMatch:
				for (IRReteNode node : nodeList) {
					value += node.getQueryMatchCount();
				}

				break;

			case EntryCreateCount:
				for (IRReteNode node : nodeList) {
					value += node.getEntryCreateCount();
				}

				break;

			case EntryDeleteCount:
				for (IRReteNode node : nodeList) {
					value += node.getEntryDeleteCount();
				}

				break;

			default:

				value = 0;
				break;
			}

			countMatrix[ridx][cidx] = value;
		}

		return value;
	}

}
