package alpha.rulp.ximpl.model;

import static alpha.rulp.rule.RCountType.COUNT_TYPE_NUM;
import static alpha.rulp.rule.RReteStatus.ASSUMED;
import static alpha.rulp.rule.RReteStatus.DEFINED;
import static alpha.rulp.rule.RReteStatus.REASONED;
import static alpha.rulp.rule.RReteStatus.REMOVED;
import static alpha.rulp.ximpl.node.RReteType.RETE_TYPE_NUM;

import java.util.List;

import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.rule.RCountType;
import alpha.rulp.ximpl.node.IRReteNode;
import alpha.rulp.ximpl.node.IRReteNodeCounter;
import alpha.rulp.ximpl.node.RReteType;

public class XRReteCounter implements IRReteNodeCounter {

	private final long countMatrix[][];

	private final IReteNodeMatrix reteNodeMatrix;

	public XRReteCounter(IReteNodeMatrix reteNodeMatrix) {

		super();
		this.reteNodeMatrix = reteNodeMatrix;
		this.countMatrix = new long[RETE_TYPE_NUM][COUNT_TYPE_NUM];
		for (int i = 0; i < RETE_TYPE_NUM; ++i) {
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
			case DefinedCount:
				for (IRReteNode node : nodeList) {
					value += node.getEntryQueue().getEntryCounter().getEntryCount(DEFINED);
				}
				break;

			case AssumeCount:
				for (IRReteNode node : nodeList) {
					value += node.getEntryQueue().getEntryCounter().getEntryCount(ASSUMED);
				}
				break;

			case ReasonCount:
				for (IRReteNode node : nodeList) {
					value += node.getEntryQueue().getEntryCounter().getEntryCount(REASONED);
				}
				break;

			case DropCount:
				for (IRReteNode node : nodeList) {
					value += node.getEntryQueue().getEntryCounter().getEntryCount(REMOVED);
				}
				break;

			case NullCount:
				for (IRReteNode node : nodeList) {
					value += node.getEntryQueue().getEntryCounter().getEntryNullCount();
				}
				break;

			case RedundantCount:
				for (IRReteNode node : nodeList) {
					value += node.getEntryQueue().getRedundantCount();
				}
				break;

			case BindFromCount:
				for (IRReteNode node : nodeList) {
					value += model.getNodeGraph().getBindFromNodes(node).size();
				}
				break;

			case BindToCount:
				for (IRReteNode node : nodeList) {
					value += model.getNodeGraph().getBindToNodes(node).size();
				}
				break;

			case NodeCount:
				value = nodeList.size();
				break;

			case SourceCount:
				for (IRReteNode node : nodeList) {
					value += reteNodeMatrix.getModel().getNodeGraph().listSourceNodes(node).size();
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

			default:

				value = 0;
				break;
			}

			countMatrix[ridx][cidx] = value;
		}

		return value;
	}

}
