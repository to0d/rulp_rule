package alpha.rulp.utils;

import static alpha.rulp.lang.Constant.MAX_COUNTER_SIZE;
import static alpha.rulp.rule.Constant.RETE_PRIORITY_ASSUMED;
import static alpha.rulp.rule.Constant.RETE_PRIORITY_DEAD;
import static alpha.rulp.rule.Constant.RETE_PRIORITY_DEFAULT;
import static alpha.rulp.rule.Constant.RETE_PRIORITY_INACTIVE;
import static alpha.rulp.rule.Constant.RETE_PRIORITY_MAXIMUM;
import static alpha.rulp.rule.Constant.RETE_PRIORITY_PARTIAL_MAX;
import static alpha.rulp.rule.Constant.RETE_PRIORITY_PARTIAL_MIN;
import static alpha.rulp.rule.Constant.RETE_PRIORITY_ROOT;
import static alpha.rulp.rule.Constant.RETE_PRIORITY_SYSTEM;
import static alpha.rulp.rule.Constant.STMT_MAX_LEN;
import static alpha.rulp.rule.Constant.STMT_MIN_LEN;
import static alpha.rulp.rule.RCountType.COUNT_TYPE_NUM;
import static alpha.rulp.rule.RReteStatus.ASSUME;
import static alpha.rulp.rule.RReteStatus.DEFINE;
import static alpha.rulp.rule.RReteStatus.FIXED_;
import static alpha.rulp.rule.RReteStatus.REASON;
import static alpha.rulp.rule.RReteStatus.REMOVE;
import static alpha.rulp.rule.RReteStatus.TEMP__;
import static alpha.rulp.ximpl.node.RReteType.ALPH0;
import static alpha.rulp.ximpl.node.RReteType.ALPH1;
import static alpha.rulp.ximpl.node.RReteType.BETA0;
import static alpha.rulp.ximpl.node.RReteType.BETA1;
import static alpha.rulp.ximpl.node.RReteType.BETA2;
import static alpha.rulp.ximpl.node.RReteType.BETA3;
import static alpha.rulp.ximpl.node.RReteType.CONST;
import static alpha.rulp.ximpl.node.RReteType.EXPR0;
import static alpha.rulp.ximpl.node.RReteType.EXPR1;
import static alpha.rulp.ximpl.node.RReteType.EXPR2;
import static alpha.rulp.ximpl.node.RReteType.EXPR3;
import static alpha.rulp.ximpl.node.RReteType.RETE_TYPE_NUM;
import static alpha.rulp.ximpl.node.RReteType.RULE;
import static alpha.rulp.ximpl.node.RReteType.WORK;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import alpha.common.utils.Pair;
import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.rule.IRModelCounter;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.rule.IRReteNode.InheritIndex;
import alpha.rulp.rule.IRReteNode.JoinIndex;
import alpha.rulp.rule.IRRule;
import alpha.rulp.rule.IRRuleCounter;
import alpha.rulp.rule.RCountType;
import alpha.rulp.utils.OptimizeUtil.NodeCount;
import alpha.rulp.utils.OptimizeUtil.OutputType;
import alpha.rulp.utils.OptimizeUtil.RefArray;
import alpha.rulp.utils.OptimizeUtil.RuleCounter;
import alpha.rulp.ximpl.action.ActionUtil;
import alpha.rulp.ximpl.action.IAction;
import alpha.rulp.ximpl.action.RActionType;
import alpha.rulp.ximpl.cache.IRCacheWorker;
import alpha.rulp.ximpl.cache.IRCacheWorker.CacheStatus;
import alpha.rulp.ximpl.constraint.IRConstraint;
import alpha.rulp.ximpl.entry.IFixEntry;
import alpha.rulp.ximpl.entry.IFixEntryArray;
import alpha.rulp.ximpl.entry.IREntryQueue;
import alpha.rulp.ximpl.entry.IREntryQueue.IREntryCounter;
import alpha.rulp.ximpl.entry.IREntryTable;
import alpha.rulp.ximpl.entry.IRReference;
import alpha.rulp.ximpl.entry.IRReteEntry;
import alpha.rulp.ximpl.entry.XREntryQueueAction;
import alpha.rulp.ximpl.model.IReteNodeMatrix;
import alpha.rulp.ximpl.node.AbsReteNode;
import alpha.rulp.ximpl.node.IRBetaNode;
import alpha.rulp.ximpl.node.IRNodeGraph;
import alpha.rulp.ximpl.node.IRReteNodeCounter;
import alpha.rulp.ximpl.node.RReteType;
import alpha.rulp.ximpl.node.SourceNode;
import alpha.rulp.ximpl.node.XRNodeRule0;

public class StatsUtil {

	public static final OutputType ALL_OUT_TYPE[] = { new OutputType(RReteType.ALPH0, RCountType.DefinedCount),
			new OutputType(RReteType.BETA0, RCountType.DefinedCount),
			new OutputType(RReteType.BETA0, RCountType.MatchCount),
			new OutputType(RReteType.BETA2, RCountType.DefinedCount),
			new OutputType(RReteType.BETA2, RCountType.MatchCount),
			new OutputType(RReteType.BETA3, RCountType.DefinedCount),
			new OutputType(RReteType.BETA3, RCountType.MatchCount),
			new OutputType(RReteType.RULE, RCountType.ExecCount),
			new OutputType(RReteType.RULE, RCountType.UpdateCount) };

	static final RReteType DETAIL_RETE_TYPES[] = { ALPH0, ALPH1, BETA0, BETA1, BETA2, BETA3, EXPR0, EXPR1, EXPR2, EXPR3,
			RULE, WORK, CONST };

	static final String SEP_LINE1 = "=========================================================================="
			+ "=======================================================================================\n";

	static final String SEP_LINE2 = "--------------------------------------------------------------------------"
			+ "---------------------------------------------------------------------------------------\n";

	private static List<String> _formatTableResult(List<List<String>> result, List<Integer> maxColumnLen) {

		ArrayList<String> lines = new ArrayList<>();

		int rowCount = result.size();
		int columnCount = result.get(0).size();

		/*******************************************************************************/
		// Get max length of every column
		/*******************************************************************************/
		int maxLength[] = new int[columnCount];
		int preLength[] = new int[columnCount];

		{

			for (int columnIndex = 0; columnIndex < columnCount; ++columnIndex) {
				if (columnIndex < maxColumnLen.size() && maxColumnLen.get(columnIndex) != null) {
					maxLength[columnIndex] = maxColumnLen.get(columnIndex);
				} else {
					maxLength[columnIndex] = -1;
				}
			}

			for (int columnIndex = 0; columnIndex < columnCount; ++columnIndex) {

				if (maxLength[columnIndex] != -1) {
					continue;
				}

				for (int rowIndex = 0; rowIndex < rowCount; ++rowIndex) {

					List<String> row = result.get(rowIndex);
					String value = row.get(columnIndex);
					if (value == null) {
						continue;
					}

					int length = value.length();
					if (length > maxLength[columnIndex]) {
						maxLength[columnIndex] = length;
					}
				}
			}

			int preLen = 0;
			for (int columnIndex = 0; columnIndex < columnCount; ++columnIndex) {
				maxLength[columnIndex] = maxLength[columnIndex] + 1;
				preLength[columnIndex] = preLen;
				preLen += maxLength[columnIndex];
			}
		}

		/*******************************************************************************/
		// Output row line
		/*******************************************************************************/
		for (int rowIndex = 0; rowIndex < rowCount; ++rowIndex) {

			ArrayList<String> multiLines = new ArrayList<>();

			List<String> row = result.get(rowIndex);

			for (int columnIndex = 0; columnIndex < columnCount; ++columnIndex) {

				String value = row.get(columnIndex);
				if (value == null) {
					continue;
				}

				int preLen = preLength[columnIndex];
				int maxLen = maxLength[columnIndex];

				ArrayList<String> subValues = new ArrayList<>();
				while (value.length() >= maxLen) {

					String subValue = value.substring(0, maxLen - 1);
					int pos = subValue.lastIndexOf(' ');
					if (pos != -1) {
						subValue = value.substring(0, pos);
						value = value.substring(pos);
					} else {
						value = value.substring(maxLen - 1);
					}

					subValues.add(subValue);

				}

				if (!value.isEmpty()) {
					subValues.add(value);
				}

				for (int i = 0; i < subValues.size(); ++i) {

					String subLine;
					if (i >= multiLines.size()) {
						subLine = "";
						multiLines.add(subLine);
					} else {
						subLine = multiLines.get(i);
					}

					while (subLine.length() < preLen) {
						subLine += " ";
					}

					subLine += subValues.get(i);
					multiLines.set(i, subLine);
				}

			}

			lines.addAll(multiLines);
		}

		return lines;
	};

	private static int _getCountTypeLength(RCountType type) {

		switch (type) {
		case MatchCount:
			return 8;

		case QueryFetch:
			return 7;

		case MinLevel:
		case MaxLevel:
		case MinPriority:
		case MaxPriority:
		case FailedCount:
			return 4;

		case DropCount:
		case NullCount:
		case NodeCount:
		case ExecCount:
		case IdleCount:
		case FixedCount:
		case TempCount:
		case RemoveCount:
			return 5;

		default:
			return 6;
		}
	}

	private static String _getCountTypeName(RCountType type) {

		switch (type) {
		case AssumeCount:
			return "Assume";

		case BindFromCount:
			return "BindFr";

		case BindToCount:
			return "BindTo";

		case DefinedCount:
			return "Define";

		case FixedCount:
			return "Fixed";

		case RemoveCount:
			return "Remove";

		case TempCount:
			return "Temp";

		case DropCount:
			return "Drop";

		case ExecCount:
			return "Exec";

		case IdleCount:
			return "Idle";

		case FailedCount:
			return "Fail";

		case MatchCount:
			return "Match";

		case MaxLevel:
			return "MaxL";

		case MinLevel:
			return "MinL";

		case MaxPriority:
			return "MaxP";

		case MinPriority:
			return "MinP";

		case NodeCount:
			return "Node";

		case SourceCount:
			return "Source";

		case NullCount:
			return "Null";

		case QueryFetch:
			return "QFetch";

		case QueryMatch:
			return "QMatch";

		case ReasonCount:
			return "Reason";

		case UpdateCount:
			return "Update";

		case RedundantCount:
			return "Redunt";

		default:
			return "Unknown";
		}
	}

	private static String _getPriorityName(int priority) {
		switch (priority) {
		case RETE_PRIORITY_INACTIVE:
			return "inactive";

		case RETE_PRIORITY_ASSUMED:
			return "assumed";

		case RETE_PRIORITY_DEFAULT:
			return "default";

		case RETE_PRIORITY_MAXIMUM:
			return "maximum";

		case RETE_PRIORITY_SYSTEM:
			return "system";

		case RETE_PRIORITY_DEAD:
			return "dead";

		case RETE_PRIORITY_PARTIAL_MIN:
			return "part-min";

		case RETE_PRIORITY_PARTIAL_MAX:
			return "part-max";

		case RETE_PRIORITY_ROOT:
			return "root";

		default:
			return "";
		}
	}

	private static String _getReteTypeShortName(RReteType type) {

		switch (type) {
		case ALPH0:
			return "a0";

		case ALPH1:
			return "a1";

//		case ALPH2:
//			return "a2";

		case EXPR0:
			return "e0";

		case EXPR1:
			return "e1";

		case EXPR2:
			return "e2";

		case EXPR3:
			return "e3";

		case EXPR4:
			return "e4";

		case BETA0:
			return "b0";

		case BETA1:
			return "b1";

		case BETA2:
			return "b2";

		case BETA3:
			return "b3";

		case RULE:
			return "ru";

		case WORK:
			return "wk";

		case ROOT0:
			return "r0";

		case NAME0:
			return "n0";

		case VAR:
			return "va";

		case CONST:
			return "c0";

		default:
			return "un";
		}
	}

	private static boolean _isInactiveNode(IRReteNode node) {
		return node.getNodeExecCount() == 0 || node.getNodeExecCount() == node.getNodeIdleCount();
	}

	private static void _printCacheInfo(StringBuffer sb, IRModel model) throws RException {

		List<? extends IRCacheWorker> caches = model.listCacheWorkers();
		Collections.sort(caches, (c1, c2) -> {
			return c1.getNode().getNodeId() - c2.getNode().getNodeId();
		});

		int loadCount = 0;
		for (IRCacheWorker cache : caches) {
			if (cache.getStatus() == CacheStatus.LOADED) {
				loadCount++;
			}
		}

		sb.append(String.format("node cache info: load=%d, total=%d, path=%s\n", loadCount, caches.size(),
				model.getCachePath()));

		sb.append(SEP_LINE1);
		sb.append(String.format("%9s %7s %6s %6s %6s %6s %6s %6s %6s %6s %6s\n", "NODE[n]", "Status", "Dirty", "NStmt",
				"NLast", "CStmt", "CLast", "Load", "Save", "Read", "Write", "named"));
		sb.append(SEP_LINE2);

		for (IRCacheWorker cache : caches) {

			IRReteNode node = cache.getNode();
			IREntryQueue queue = node.getEntryQueue();
			IRReteEntry lastEntry = ReteUtil.getLastEntry(queue);

			sb.append(String.format("%9s %7s %6s %6d %6d %6d %6d %6d %6d %6d %6d %s\n",
					node.getNodeName() + "[" + node.getEntryLength() + "]", "" + cache.getStatus(),
					"" + cache.isDirty(), queue.size(), lastEntry == null ? -1 : lastEntry.getEntryId(),
					cache.getStmtCount(), cache.getCacheLastEntryId(), cache.getLoadCount(), cache.getSaveCount(),
					cache.getReadCount(), cache.getWriteCount(), "" + node.getNamedName()));
		}

		sb.append(SEP_LINE1);
	}

	private static void _printEntryTable(StringBuffer sb, IRModel model, IREntryTable entryTable) throws RException {

		Set<RReteType> typeSet = new HashSet<>();

		for (IRReteNode node : model.getNodeGraph().getNodeMatrix().getAllNodes()) {
			typeSet.add(node.getReteType());
		}

		int maxId = entryTable.getEntryMaxId();

		sb.append("Entry Table:\n");

		sb.append(String.format("\t%7s: total-action=%d, new-action=%d, max-size=%d, capacity=%d, expend=%d\n", "ETA",
				entryTable.getETATotalActionSize(), entryTable.getETAMaxActionSize(), entryTable.getETAQueueMaxSize(),
				entryTable.getETAQueueCapacity(), entryTable.getETAQueueExpendCount()));

		// output stmt count

		{
			int stmtCount = 0;
			for (int entryId = 1; entryId <= maxId; ++entryId) {

				IRReteEntry entry = entryTable.getEntry(entryId);
				if (entry == null || entry.isDroped()) {
					continue;
				}

				if (entry.isStmt()) {
					++stmtCount;
				}
			}

			sb.append(String.format("\t%7s: count=%d, stmt=%s\n", "Entry", entryTable.getEntryCount(), stmtCount));
		}

		// entry bit map
		sb.append(SEP_LINE1);
		_printFixArrayBitMap(sb, "Entry", entryTable.getEntryFixArray());

		// entry length array
		int entryMaxLen = STMT_MAX_LEN;

		{

			int entryLenArray[] = new int[STMT_MAX_LEN + 1];

			@SuppressWarnings("unchecked")
			Map<String, Integer>[] entryLenMapArray = new Map[STMT_MAX_LEN + 1];
			RefArray totalRefArray = new RefArray();

			for (int i = 0; i <= STMT_MAX_LEN; ++i) {
				entryLenArray[i] = 0;
			}

			for (int entryId = 1; entryId <= maxId; ++entryId) {

				IRReteEntry entry = entryTable.getEntry(entryId);
				if (entry == null || entry.isDroped()) {
					continue;
				}

				int len = entry.size();
				entryLenArray[len] = entryLenArray[len] + 1;

				String namedName = entry.getNamedName();
				if (namedName != null) {

					Map<String, Integer> entryLenMap = entryLenMapArray[len];
					if (entryLenMap == null) {
						entryLenMap = new HashMap<>();
						entryLenMapArray[len] = entryLenMap;
					}

					Integer count = entryLenMap.get(namedName);
					if (count == null) {
						count = 0;
					}

					entryLenMap.put(namedName, count + 1);
				}
			}

			for (; entryMaxLen >= STMT_MIN_LEN; entryMaxLen--) {
				if (entryLenArray[entryMaxLen] != 0) {
					break;
				}
			}

			int minLen = STMT_MIN_LEN;
			for (; minLen <= entryMaxLen; minLen++) {
				if (entryLenArray[minLen] != 0) {
					break;
				}
			}

			sb.append(String.format("Length Array: min=%d, max=%d\n", minLen, entryMaxLen));
			for (int i = minLen; i <= entryMaxLen; ++i) {

				StringBuffer sb2 = new StringBuffer();
				int update = 0;

				sb2.append(String.format("Entry[%04d]= %d", i, entryLenArray[i]));

				Map<String, Integer> entryLenMap = entryLenMapArray[i];
				if (entryLenMap != null) {

					ArrayList<String> namedList = new ArrayList<>(entryLenMap.keySet());
					Collections.sort(namedList);

					for (String namedName : namedList) {
						sb2.append(String.format(", %s(%d)", namedName, entryLenMap.get(namedName)));
						++update;
					}
				}

				if (entryLenArray[i] == 0 && update == 0) {
					continue;
				}

				sb.append(sb2.toString() + "\n");
			}

			Map<Integer, RefArray> entryReteCountMap = new HashMap<>();

			for (int entryId = 1; entryId <= maxId; ++entryId) {

				IRReteEntry entry = entryTable.getEntry(entryId);
				if (entry == null || entry.isDroped()) {
					continue;
				}

				int referCount = entry.getReferenceCount();
				if (referCount == 0) {
					continue;
				}

				IRReference ref = entry.getReferenceIterator().next();
				IRReteNode node = ref.getNode();
				if (node == null) {
					continue;
				}

				RefArray refArray = entryReteCountMap.get(entry.size());
				if (refArray == null) {
					refArray = new RefArray();
					entryReteCountMap.put(entry.size(), refArray);
				}

				int idx = node.getReteType().getIndex();
				refArray.reteRefCount[idx]++;
				totalRefArray.reteRefCount[idx]++;
			}

			sb.append(SEP_LINE2);
			sb.append("Length Array:\n");
			sb.append(SEP_LINE2);

			sb.append("Length:");
			for (RReteType t : RReteType.ALL_RETE_TYPE) {
				int total = totalRefArray.reteRefCount[t.getIndex()];
				if (total == 0) {
					continue;
				}
				sb.append(String.format(" %7s", "" + t));
			}
			sb.append("\n");

			ArrayList<Integer> refCountList = new ArrayList<>(entryReteCountMap.keySet());
			Collections.sort(refCountList);

			for (int refCount : refCountList) {

				RefArray refArray = entryReteCountMap.get(refCount);
				sb.append(String.format("%6d:", refCount));

				for (RReteType t : RReteType.ALL_RETE_TYPE) {

					if (totalRefArray.reteRefCount[t.getIndex()] == 0) {
						continue;
					}

					sb.append(String.format(" %7d", refArray.reteRefCount[t.getIndex()]));
				}

				sb.append("\n");
			}

			sb.append(" total:");
			for (RReteType t : RReteType.ALL_RETE_TYPE) {
				int total = totalRefArray.reteRefCount[t.getIndex()];
				if (total == 0) {
					continue;
				}
				sb.append(String.format(" %7d", total));
			}

			sb.append("\n");
		}

		// entry reference array
		{
			Map<Integer, RefArray> referCountMap = new HashMap<>();
			RefArray totalRefArray = new RefArray();

			int zeroRefCount = 0;

			for (int entryId = 1; entryId <= maxId; ++entryId) {

				IRReteEntry entry = entryTable.getEntry(entryId);
				if (entry == null || entry.isDroped()) {
					continue;
				}

				int referCount = entry.getReferenceCount();
				if (referCount == 0) {
					++zeroRefCount;
					continue;
				}

				RefArray refArray = referCountMap.get(referCount);
				if (refArray == null) {
					refArray = new RefArray();
					referCountMap.put(referCount, refArray);
				}

				Iterator<? extends IRReference> refIt = entry.getReferenceIterator();
				while (refIt.hasNext()) {
					IRReference ref = refIt.next();
					IRReteNode node = ref.getNode();
					if (node == null) {
						refArray.unRefCount++;
						totalRefArray.unRefCount++;
					} else {
						int idx = node.getReteType().getIndex();
						refArray.reteRefCount[idx]++;
						totalRefArray.reteRefCount[idx]++;
					}
				}
			}

			ArrayList<Integer> refCountList = new ArrayList<>(referCountMap.keySet());
			Collections.sort(refCountList);

			sb.append(SEP_LINE1);
			sb.append("Ref Array:\n");
			sb.append(SEP_LINE2);

			sb.append("RefCount: unRefer");
			for (RReteType t : RReteType.ALL_RETE_TYPE) {
				int total = totalRefArray.reteRefCount[t.getIndex()];
				if (total == 0) {
					continue;
				}
				sb.append(String.format(" %7s", "" + t));
			}
			sb.append("\n");

			// output zero ref
			sb.append(String.format("%8d:%8d\n", 0, zeroRefCount));

			for (int refCount : refCountList) {

				RefArray refArray = referCountMap.get(refCount);
				sb.append(String.format("%8d: %7d", refCount, refArray.unRefCount));

				for (RReteType t : RReteType.ALL_RETE_TYPE) {
					int total = totalRefArray.reteRefCount[t.getIndex()];
					if (total == 0) {
						continue;
					}
					sb.append(String.format(" %7d", refArray.reteRefCount[t.getIndex()]));
				}

				sb.append("\n");
			}

			sb.append(String.format("   total: %7d", totalRefArray.unRefCount));
			for (RReteType t : RReteType.ALL_RETE_TYPE) {
				int total = totalRefArray.reteRefCount[t.getIndex()];
				if (total == 0) {
					continue;
				}
				sb.append(String.format(" %7d", total));
			}

			sb.append("\n");

			sb.append(SEP_LINE1);
			_printFixArrayBitMap(sb, "Ref", entryTable.getReferenceFixArray());
		}

		// entry child count
		{
			ArrayList<IRReteEntry> maxChildCountEntrys = new ArrayList<>();
			int MAX_CHILD_ENTRY_SIZE = 10;
			int minChildSize = -1;

			for (int entryId = 1; entryId <= maxId; ++entryId) {

				IRReteEntry entry = entryTable.getEntry(entryId);
				if (entry == null || entry.isDroped()) {
					continue;
				}

				int childCount = entry.getChildCount();
				if (childCount == 0) {
					continue;
				}

				if (maxChildCountEntrys.size() < MAX_CHILD_ENTRY_SIZE) {
					maxChildCountEntrys.add(entry);
					continue;
				}

				if (minChildSize == -1) {

					Collections.sort(maxChildCountEntrys, (e1, e2) -> {
						return e2.getChildCount() - e1.getChildCount();
					});

					minChildSize = maxChildCountEntrys.get(MAX_CHILD_ENTRY_SIZE - 1).getChildCount();
				}

				if (childCount <= minChildSize) {
					continue;
				}

				maxChildCountEntrys.set(MAX_CHILD_ENTRY_SIZE - 1, entry);
				minChildSize = -1;
			}

			if (!maxChildCountEntrys.isEmpty()) {

				Collections.sort(maxChildCountEntrys, (e1, e2) -> {
					return e2.getChildCount() - e1.getChildCount();
				});

				sb.append("Child Count:\n");
				sb.append(SEP_LINE2);

				for (IRReteEntry entry : maxChildCountEntrys) {
					sb.append(String.format("%8d: entry=%s, status=%s\n", entry.getChildCount(), entry,
							entry.getStatus()));
				}

				sb.append(SEP_LINE2);

				int maxChildCount = maxChildCountEntrys.get(0).getChildCount();

				int avgChildCount = (maxChildCount + 9) / 10;
				int avgLen = 8;

				if (avgChildCount > 10000) {
					avgChildCount = 10000;
					avgLen = 6;
				} else if (avgChildCount > 1000) {
					avgChildCount = 1000;
					avgLen = 5;
				} else if (avgChildCount > 100) {
					avgChildCount = 100;
					avgLen = 4;
				} else if (avgChildCount > 10) {
					avgChildCount = 10;
					avgLen = 3;
				} else {
					avgChildCount = 1;
					avgLen = 2;
				}

				Map<Integer, int[]> childCountMap = new HashMap<>();
				int[] totalChildCount = new int[entryMaxLen + 1];
				for (int i = 0; i <= entryMaxLen; ++i) {
					totalChildCount[i] = 0;
				}

				Map<Integer, RefArray> entryReteCountMap = new HashMap<>();
				RefArray totalRefArray = new RefArray();

				for (int entryId = 1; entryId <= maxId; ++entryId) {

					IRReteEntry entry = entryTable.getEntry(entryId);
					if (entry == null || entry.isDroped()) {
						continue;
					}

					int childCount = entry.getChildCount();
					int idx = 0;
					if (childCount > 0) {
						idx = (childCount - 1) / avgChildCount + 1;
					}

					int[] m2 = childCountMap.get(idx);
					if (m2 == null) {
						m2 = new int[entryMaxLen + 1];
						for (int i = 0; i <= entryMaxLen; ++i) {
							m2[i] = 0;
						}

						childCountMap.put(idx, m2);
					}

					m2[entry.size()]++;
					totalChildCount[entry.size()]++;

					int referCount = entry.getReferenceCount();
					if (referCount == 0) {
						continue;
					}

					IRReference ref = entry.getReferenceIterator().next();
					IRReteNode node = ref.getNode();
					if (node == null) {
						continue;
					}

					RefArray refArray = entryReteCountMap.get(idx);
					if (refArray == null) {
						refArray = new RefArray();
						entryReteCountMap.put(idx, refArray);
					}

					int idx2 = node.getReteType().getIndex();
					refArray.reteRefCount[idx2]++;
					totalRefArray.reteRefCount[idx2]++;
				}

				{
					ArrayList<Integer> idxList = new ArrayList<>(childCountMap.keySet());
					Collections.sort(idxList);

					sb.append(String.format("%" + (avgLen * 2 + 5) + "s", ""));
					for (int i = STMT_MIN_LEN; i <= entryMaxLen; ++i) {

						if (totalChildCount[i] == 0) {
							continue;
						}

						sb.append(String.format(" %9s", "Entry[" + i + "]"));
					}

					sb.append("\n");

					for (int idx : idxList) {

						int begin = 0;
						int end = 0;
						if (idx > 0) {
							begin = avgChildCount * (idx - 1) + 1;
							end = avgChildCount * idx;
						}

						sb.append(String.format("%" + avgLen + "s - %" + avgLen + "s:", begin, end));

						int[] m2 = childCountMap.get(idx);

						for (int i = STMT_MIN_LEN; i <= entryMaxLen; ++i) {
							if (totalChildCount[i] == 0) {
								continue;
							}
							sb.append(String.format(" %9d", m2[i]));
						}

						sb.append("\n");

					}

					sb.append("  total:");
					for (int i = STMT_MIN_LEN; i <= entryMaxLen; ++i) {
						if (totalChildCount[i] == 0) {
							continue;
						}
						sb.append(String.format(" %9d", totalChildCount[i]));
					}

					sb.append("\n");

					{
						sb.append(SEP_LINE2);
						sb.append("        ");
						for (RReteType t : RReteType.ALL_RETE_TYPE) {

							if (totalRefArray.reteRefCount[t.getIndex()] == 0) {
								continue;
							}

							sb.append(String.format(" %7s", "" + t));
						}
						sb.append("\n");

						ArrayList<Integer> refCountList = new ArrayList<>(entryReteCountMap.keySet());
						Collections.sort(refCountList);

						for (int refCount : refCountList) {

							int begin = 0;
							int end = 0;
							if (refCount > 0) {
								begin = avgChildCount * (refCount - 1) + 1;
								end = avgChildCount * refCount;
							}

							RefArray refArray = entryReteCountMap.get(refCount);

							sb.append(String.format("%" + avgLen + "s - %" + avgLen + "s:", begin, end));

							for (RReteType t : RReteType.ALL_RETE_TYPE) {
								if (totalRefArray.reteRefCount[t.getIndex()] == 0) {
									continue;
								}
								sb.append(String.format(" %7d", refArray.reteRefCount[t.getIndex()]));
							}

							sb.append("\n");
						}

						sb.append(String.format("  total:"));
						for (RReteType t : RReteType.ALL_RETE_TYPE) {
							int total = totalRefArray.reteRefCount[t.getIndex()];
							if (total == 0) {
								continue;
							}
							sb.append(String.format(" %7d", total));
						}

						sb.append("\n");
					}
				}

				sb.append(SEP_LINE1);
			}
		}

	}

	private static void _printFixArrayBitMap(StringBuffer sb, String name, IFixEntryArray<? extends IFixEntry> fixArray)
			throws RException {

		int maxId = fixArray.getEntryMaxId();

		int groupSize = (maxId - 1) / 1000 + 1;

		if (groupSize >= 1 && groupSize < 10) {
			groupSize = 10;
		} else {
			groupSize = ((groupSize - 1) / 100 + 1) * 100;
		}

		int groupCount = (maxId - 1) / groupSize + 1;
		int groupSize2 = groupSize / 10;

		sb.append(
				String.format("%s Bit Map: total=%d, max-id=%d, create=%d, removed=%d, group-size=%d, group-count=%d\n",
						name, fixArray.getEntryCount(), maxId, fixArray.getCreatedCount(), fixArray.getRemovedCount(),
						groupSize, groupCount));
		sb.append(SEP_LINE2);

		int rowCount = 0;

		for (int groupIndex = 0; groupIndex < groupCount; ++groupIndex) {

			int beginEntryId = groupIndex * groupSize + 1;
			int endEntryId = beginEntryId + groupSize - 1;

			int entryCount = 0;

			for (int entryId = beginEntryId; entryId <= endEntryId; ++entryId) {
				IFixEntry entry = fixArray.getEntry(entryId);
				if (entry != null && !entry.isDroped()) {
					++entryCount;
				}
			}

			int lvl = (entryCount + groupSize2 - 1) / groupSize2;
			sb.append(lvl == 10 ? "." : "" + lvl);

			if ((groupIndex + 1) % 100 == 0) {
				sb.append(String.format(" [%d]\n", endEntryId));
				++rowCount;

			} else if ((groupIndex + 1) == groupCount) {

				if (rowCount > 0) {
					int left = 100 - groupIndex % 100 - 1;
					while (left-- > 0) {
						sb.append(" ");
					}
				}

				sb.append(String.format(" [%d]\n", maxId));
			}
		}

		sb.append(SEP_LINE1);
	}

	private static void _printFrameCounter(StringBuffer sb, IRFrame frame) throws RException {

		ArrayList<Pair<IRObject, DeCounter>> ccList = new ArrayList<>();

		for (Entry<IRObject, DeCounter> e : RuntimeUtil.getObjecCallCount(frame).entrySet()) {
			ccList.add(new Pair<>(e.getKey(), e.getValue()));
		}

		if (ccList.isEmpty()) {
			return;
		}

		Collections.sort(ccList, (e1, e2) -> {
			int d = e2.getValue().getTotalCount() - e1.getValue().getTotalCount();
			if (d == 0) {
				d = e2.getKey().asString().compareTo(e1.getKey().asString());
			}
			return d;
		});

		ArrayList<DeCounter> counters = new ArrayList<>();

		for (Pair<IRObject, DeCounter> e : ccList) {
			counters.add(e.getValue());
		}

		ArrayList<String> counterLines = TraceUtil.formatCounterTable(counters, MAX_COUNTER_SIZE);

		int counterLineIndex = 0;

		sb.append(String.format("%s\n", counterLines.get(counterLineIndex++)));
		sb.append(SEP_LINE1);
		sb.append(
				String.format("%8s %8s %-30s %s\n", "RType", "Count", "Object", counterLines.get(counterLineIndex++)));
		sb.append(SEP_LINE2);

		for (Pair<IRObject, DeCounter> e : ccList) {

			DeCounter counter = e.getValue();
			IRObject obj = e.getKey();
			int count = counter.getTotalCount();

			String objName = "" + obj;
			if (objName.length() > 30) {
				objName = objName.substring(0, 30);
			}

			sb.append(String.format("%8s %8d %-30s %s\n", "" + obj.getType(), count, objName,
					counterLines.get(counterLineIndex++)));
		}

		sb.append(SEP_LINE1);
	}

	private static void _printModelCountInfo(StringBuffer sb, IRModel model) throws RException {

		IRNodeGraph graph = model.getNodeGraph();

		sb.append(String.format("model count info:\n"));
		sb.append(SEP_LINE1);
		sb.append(String.format("%-30s %8s\n", "name", "count"));
		sb.append(SEP_LINE2);
		sb.append(String.format("%-30s %8d\n", "model-gc-trigger", model.getGcTrigger()));
		sb.append(String.format("%-30s %8d\n", "model-gc-count", model.getGcCount()));
		sb.append(String.format("%-30s %8d\n", "graph-gc-count", graph.getGcCount()));
		sb.append(String.format("%-30s %8d\n", "graph-gc-node-remove", graph.getGcRemoveNodeCount()));
		sb.append(String.format("%-30s %8d\n", "graph-gc-node-clean", graph.getGcCleanNodeCount()));
		sb.append(String.format("%-30s %8d\n", "graph-gc-inactive-leaf", graph.getGcInactiveLeafCount()));
		sb.append(String.format("%-30s %8d\n", "graph-gc-cache", graph.getGcCacheCount()));

		sb.append(SEP_LINE1);

		sb.append("\n");
		sb.append("\n");
	}

	private static void _printModelFrame(StringBuffer sb, IRModel model) throws RException {

		/*********************************************************************/
		// Model Frame
		/*********************************************************************/
		sb.append(String.format("Model<%s> frame:", "" + model.getModelName()));
		sb.append(TraceUtil.printFrame(model.getFrame()));
		_printFrameCounter(sb, model.getFrame());
		sb.append("\n");

		/*********************************************************************/
		// Node Frame
		/*********************************************************************/
		IReteNodeMatrix nodeMatrix = model.getNodeGraph().getNodeMatrix();

		ArrayList<IRReteNode> nodes = new ArrayList<>(nodeMatrix.getAllNodes());
		Collections.sort(nodes, (n1, n2) -> {
			return n1.getNodeId() - n2.getNodeId();
		});

		for (IRReteNode node : nodes) {

			IRFrame nodeFrame = node.findFrame();
			if (nodeFrame == null) {
				continue;
			}

			sb.append(String.format("Node<%s> frame:", "" + node.getNodeName()));
			_printFrameCounter(sb, nodeFrame);
			sb.append(TraceUtil.printFrame(nodeFrame));
			sb.append("\n");
		}

	}

	private static void _printModelShareIndex(StringBuffer sb, IReteNodeMatrix modelNodeMatrix, long ruleSummary[][])
			throws RException {

		IRReteNodeCounter modelCounter = RuleFactory.createReteCounter(modelNodeMatrix);

		boolean hasNode = false;
		for (RReteType reteType : RReteType.ALL_RETE_TYPE) {

			if (RReteType.isRootType(reteType) || modelCounter.getCount(reteType, RCountType.NodeCount) == 0) {
				continue;
			}

			hasNode = true;
			break;
		}

		if (!hasNode) {
			return;
		}

		sb.append(String.format("Model<%s> share index:\n", "" + modelNodeMatrix.getModel().getModelName()));

		sb.append(SEP_LINE1);
		sb.append(String.format("%5s", "NODE"));
		for (RCountType countType : RCountType.ALL_COUNT_TYPE) {
			sb.append(String.format(" %" + _getCountTypeLength(countType) + "s", _getCountTypeName(countType)));
		}
		sb.append("\n");
		sb.append(SEP_LINE2);

		for (RReteType reteType : RReteType.ALL_RETE_TYPE) {

			if (RReteType.isRootType(reteType) || modelCounter.getCount(reteType, RCountType.NodeCount) == 0) {
				continue;
			}

			sb.append(String.format("%5s", "" + reteType));
			for (RCountType countType : RCountType.ALL_COUNT_TYPE) {

				long summary = ruleSummary[reteType.getIndex()][countType.getIndex()];
				if (summary == 0) {
					sb.append(String.format(" %" + _getCountTypeLength(countType) + "s", " "));

				} else {

					float modelValue = modelCounter.getCount(reteType, countType);
					float rate = 1 - modelValue / summary;
					int v = (int) (rate * 1000);
					sb.append(String.format(" %" + _getCountTypeLength(countType) + "s", "" + v));
				}
			}

			sb.append("\n");
		}

		sb.append(SEP_LINE1);
		sb.append("\n");
	}

	private static void _printNodeInfo1(StringBuffer sb, IRModel model, List<IRReteNode> nodes, boolean printRoot)
			throws RException {

		sb.append("\nnode info:\n");
		sb.append(SEP_LINE1);
		sb.append(String.format(
				"%-12s %6s %6s %6s %6s %6s %6s %5s %5s %5s %6s %6s %6s %4s %4s %6s %4s %3s %3s %3s %5s %8s %11s\n",
				"NODE[n]", "Fixed", "Define", "Reason", "Assume", "Drop", "Remove", "Temp", "Null", "Bind", "Match",
				"Update", "Redunt", "Exec", "Idle", "Waste", "Fail", "Lvl", "Pri", "Src", "Use", "Stage", "PVisit"));
		sb.append(SEP_LINE2);

		IRNodeGraph graph = model.getNodeGraph();

		Map<IRReteNode, Integer> wasteMap = getWasteMap(nodes);
		for (IRReteNode node : nodes) {

			if (!printRoot && RReteType.isRootType(node.getReteType())) {
				continue;
			}

			String parentVisitIndex = "";
			for (int i = 0; i < node.getParentCount(); ++i) {
				if (i != 0) {
					parentVisitIndex += "/";
				}
				parentVisitIndex += node.getParentVisitIndex(i);
			}

			IREntryQueue entryQueue = node.getEntryQueue();
			IREntryCounter entryCounter = entryQueue.getEntryCounter();

			String waste = "";
			Integer wasteInteger = wasteMap.get(node);
			if (wasteInteger != null) {
				if (_isInactiveNode(node)) {
					waste = "*";
				} else {
					waste = "" + wasteInteger;
				}
			}

			sb.append(String.format(
					"%-12s %6d %6d %6d %6d %6d %6d %5d %5d %5s %6d %6d %6d %4d %4d %6s %4d %3d %3d %3d %5d %8s %11s",
					node.getNodeName() + "[" + node.getEntryLength() + "]", entryCounter.getEntryCount(FIXED_),
					entryCounter.getEntryCount(DEFINE), entryCounter.getEntryCount(REASON),
					entryCounter.getEntryCount(ASSUME), entryCounter.getEntryCount(null),
					entryCounter.getEntryCount(REMOVE), entryCounter.getEntryCount(TEMP__),
					entryCounter.getEntryNullCount(),
					"" + graph.getBindFromNodes(node).size() + "/" + graph.getBindToNodes(node).size(),
					node.getNodeMatchCount(), entryQueue.getUpdateCount(), entryQueue.getRedundantCount(),
					node.getNodeExecCount(), node.getNodeIdleCount(), waste, node.getNodeFailedCount(),
					node.getReteLevel(), node.getPriority(), graph.listSourceNodes(node).size(),
					graph.getUseCount(node), "" + node.getReteStage(), parentVisitIndex));

			sb.append("\n");
		}

		sb.append(SEP_LINE1);
		sb.append("\n");
	}

	private static void _printNodeInfo2(StringBuffer sb, IRModel model, List<IRReteNode> nodes) throws RException {

		List<List<String>> result = new ArrayList<>();
		List<Integer> maxLen = toList(13, 14, 36, 30, 30, 30);

		result.add(toList("NODE[n]", "Parent", "Child", "Rule", "Inherit", "Join"));

		for (IRReteNode node : nodes) {

			ArrayList<String> parentNames = new ArrayList<>();
			ArrayList<String> childNames = new ArrayList<>();
			ArrayList<String> ruleNames = new ArrayList<>();

			int count = 0;

			for (IRRule rule : model.getNodeGraph().getRelatedRules(node)) {
				ruleNames.add(rule.getRuleName());
				++count;
			}

			if (node.getParentNodes() != null) {
				for (IRReteNode pn : node.getParentNodes()) {
					parentNames.add(pn.getNodeName());
					++count;
				}
			}

			for (IRReteNode cn : node.getChildNodes()) {
				childNames.add(cn.getNodeName());
				++count;
			}

			String inheritExpr = null;
			InheritIndex[] inheritIndexs = node.getInheritIndex();
			if (inheritIndexs != null) {
				inheritExpr = toList(inheritIndexs).toString();
				count += inheritIndexs.length;
			}

			String joinDes = null;
			if (parentNames.size() > 1) {
				if (RReteType.isBetaType(node.getReteType())) {
					IRBetaNode betaNode = (IRBetaNode) node;
					List<JoinIndex> joinIndex = betaNode.getJoinIndexList();
					if (joinIndex == null || joinIndex.isEmpty()) {
						joinDes = "[]";
					} else {
						joinDes = OptimizeUtil.toString(joinIndex);
						count++;
					}
				} else {
					joinDes = "[]";
				}
			}

			if (count > 0) {

				result.add(toList(node.getNodeName() + "[" + node.getEntryLength() + "]",
						parentNames.isEmpty() ? null : "" + parentNames, childNames.isEmpty() ? null : "" + childNames,
						ruleNames.isEmpty() ? null : "" + ruleNames, inheritExpr, joinDes));
			}

		}

		if (result.size() == 1) {
			return;
		}

		sb.append("node info2:\n");
		sb.append(SEP_LINE1);

		int index = 0;
		for (String line : _formatTableResult(result, maxLen)) {
			if (index++ == 1) {
				sb.append(SEP_LINE2);
			}
			sb.append(line);
			sb.append("\n");
		}

		sb.append(SEP_LINE1);
		sb.append("\n");
	}

	private static void _printNodeInfo3(StringBuffer sb, IRModel model, List<IRReteNode> nodes) throws RException {

		sb.append("node info3:\n");
		sb.append(SEP_LINE1);

		sb.append(String.format("%-12s %s\n", "NODE[n]", "UniqName"));
		sb.append(SEP_LINE2);

		for (IRReteNode node : nodes) {
			sb.append(String.format("%-12s %s", node.getNodeName() + "[" + node.getEntryLength() + "]",
					node.getUniqName()));
			sb.append("\n");
		}

		sb.append(SEP_LINE1);
		sb.append("\n");
	}

	private static void _printNodeInfo5Action(StringBuffer sb, IRModel model, List<IRReteNode> nodes)
			throws RException {

		ArrayList<IRReteNode> ruleNodes = new ArrayList<>();

		for (IRReteNode node : nodes) {

			if (node.getReteType() != RReteType.RULE) {
				continue;
			}

			ruleNodes.add(node);
		}

		if (ruleNodes.isEmpty()) {
			return;
		}

		sb.append("node info5: action\n");
		sb.append(SEP_LINE1);

		sb.append(String.format("%-12s %-5s %-5s %-6s\n", "NODE[n]", "Index", "Type", "Action"));
		sb.append(SEP_LINE2);

		for (IRReteNode node : ruleNodes) {

			XREntryQueueAction entryQueue = (XREntryQueueAction) node.getEntryQueue();
			LinkedList<IAction> actions = entryQueue.getActionStmtList();
			String line0 = node.getNodeName() + "[" + node.getEntryLength() + "]";

			if (actions.size() == 0) {

				sb.append(String.format("%-12s %-5d %-5s %s\n", line0, 0, "", ""));

				continue;
			}

			for (int i = 0; i < actions.size(); ++i) {

				ArrayList<String> outLines = new ArrayList<>();
				IAction action = actions.get(i);
				if (action.getActionType() != RActionType.EXPR) {
					outLines.add(action.toString());
				} else {
					FormatUtil.format(action.getExpr(), outLines, 0);
				}

				sb.append(String.format("%-12s %-5d %-5s %s\n", i == 0 ? line0 : "", i, action.getActionType(),
						outLines.size() > 0 ? outLines.get(0) : ""));

				for (int j = 1; j < outLines.size(); ++j) {
					sb.append(String.format("%24s %s\n", "", outLines.get(j)));
				}

				if (action.getActionType() == RActionType.EXPR) {
					int k = 0;
					for (IRExpr stmtExpr : action.getStmtExprList()) {
						sb.append(String.format("%-24s %d: %s %s\n", "", k++, "" + ActionUtil.getActionType(stmtExpr),
								ActionUtil.getRelatedUniqName(ActionUtil.getRelatedStmt(stmtExpr))));

					}
				}

			}
		}

		sb.append(SEP_LINE1);
		sb.append("\n");
	}

	private static void _printNodeInfo6(StringBuffer sb, IRModel model, List<IRReteNode> nodes) throws RException {

		sb.append("node info6:\n");
		sb.append(SEP_LINE1);

		int maxNameLen = 5;
		for (IRReteNode node : nodes) {
			String name = node.getNamedName();
			if (name != null) {
				int len = name.length();
				if (len > maxNameLen) {
					maxNameLen = len;
				}
			}
		}

		sb.append(String.format("%-12s %-5s %-5s %-6s %-" + maxNameLen + "s %-6s %-5s %-4s %-4s %-4s %3s %3s %3s %s\n",
				"NODE[n]", "Type", "Class", "Queue", "Named", "Parent", "Child", "Rule", "Inhe", "Join", "C1", "C2",
				"Pri", "VarEntry"));
		sb.append(SEP_LINE2);

		for (IRReteNode node : nodes) {

			int joinCount = 0;
			if (RReteType.isBetaType(node.getReteType())) {
				IRBetaNode betaNode = (IRBetaNode) node;
				List<JoinIndex> joinIndex = betaNode.getJoinIndexList();
				if (joinIndex != null) {
					joinCount = joinIndex.size();
				}
			}

			int constraint2Count = 0;
			if (RReteType.isBetaType(node.getReteType())) {
				constraint2Count = ((IRBetaNode) node).getConstraint2Count();
			}

			String varEntry = "" + StatsUtil.toList(node.getVarEntry());

			String className = node.getClass().getSimpleName();
			if (className.startsWith("XRNode")) {
				className = className.substring(6);
			}

			sb.append(String.format("%-12s %-5s %-5s %-6s %-" + maxNameLen + "s %6d %5d %4d %4d %4d %3d %3d %3d %s",
					node.getNodeName() + "[" + node.getEntryLength() + "]", "" + node.getReteType(), className,
					"" + node.getEntryQueue().getQueueType(), node.getNamedName() == null ? "" : node.getNamedName(),
					node.getParentCount(), node.getChildNodes().size(),
					model.getNodeGraph().getRelatedRules(node).size(),
					node.getInheritIndex() == null ? 0 : node.getInheritIndex().length, joinCount,
					node.getConstraint1Count(), constraint2Count, node.getPriority(), varEntry));

			sb.append("\n");
		}

		sb.append(SEP_LINE1);
		sb.append("\n");
	}

	private static void _printNodeSource(StringBuffer sb, IReteNodeMatrix nodeMatrix, List<IRReteNode> nodes)
			throws RException {

		boolean hasSourceNode = false;
		for (IRReteNode node : nodes) {

			if (!nodeMatrix.getModel().getNodeGraph().listSourceNodes(node).isEmpty()) {
				hasSourceNode = true;
				break;
			}

			if (node.getParentCount() > 0) {
				hasSourceNode = true;
				break;
			}
		}

		if (!hasSourceNode) {
			return;
		}

		boolean outputHead = false;

		for (IRReteNode node : nodes) {

			Collection<SourceNode> sourceNodes = nodeMatrix.getModel().getNodeGraph().listSourceNodes(node);
			if (sourceNodes.isEmpty()) {
				continue;
			}

			if (!outputHead) {

				sb.append("node source info:\n");
				sb.append(SEP_LINE1);
				sb.append(String.format("%-13s   %-13s   %-6s %s\n", "NODE[n]", "Rule", "Length", "Description"));
				sb.append(SEP_LINE2);

				outputHead = true;
			}

			String nodeName = String.format("%s(%s)", node.getNodeName(), node.getReteType());

			if (node.getReteType() != RReteType.RULE) {
				sb.append(String.format("%-13s : %14s  %-6d %s\n", nodeName, "", node.getEntryLength(),
						node.getUniqName()));
			} else {
				sb.append(String.format("%-13s : %14s  %-6d %s ==> %s\n", nodeName, "", node.getEntryLength(),
						RulpUtil.toString(((XRNodeRule0) node).getMatchStmtList()),
						RulpUtil.toString(((XRNodeRule0) node).getActionStmtList())));
			}

			List<SourceNode> sourceNodeList = new ArrayList<>(sourceNodes);
			Collections.sort(sourceNodeList, (n1, n2) -> {
				return n1.rule.getNodeName().compareTo(n2.rule.getNodeName());
			});

			int nodeIndex = 0;
			for (SourceNode sn : sourceNodeList) {

				String actionStr = String.format("A(%d/%d)=[", sn.actionList.size(), sn.rule.getActionList().size());
				int index = 0;
				for (IAction action : sn.actionList) {
					if (index++ != 0) {
						actionStr += ", ";
					}
					actionStr += action.getIndex();
				}

				actionStr += "]";

				sb.append(String.format("%14d: %-14s  %-6d %s\n", nodeIndex++, sn.rule.getNodeName(),
						node.getEntryLength(), actionStr));

			}
		}

		if (outputHead) {
			sb.append(SEP_LINE1);
			sb.append("\n");
		}

	}

	private static void _printNodeStats2(StringBuffer sb, IReteNodeMatrix nodeMatrix, RReteType... reteTypes)
			throws RException {

		sb.append("MatchCount:");
		for (RReteType reteType : reteTypes) {
			sb.append(" " + reteType);
		}
		sb.append("\n");

		ArrayList<NodeCount> nodeCountList = new ArrayList<>();

		for (RReteType reteType : reteTypes) {
			for (IRReteNode node : nodeMatrix.getNodeList(reteType)) {
				NodeCount counter = new NodeCount();
				counter.node = node;
				counter.count = node.getNodeMatchCount();
				nodeCountList.add(counter);
			}
		}

		Collections.sort(nodeCountList, (c1, c2) -> {
			return (int) c2.count - (int) c1.count;
		});

		for (NodeCount count : nodeCountList) {
			sb.append(String.format("%-10s  %10d\n", count.node.getNodeName() + "[" + count.node.getEntryLength() + "]",
					count.count));
		}

		sb.append("\n");
	}

	private static void _printPriorityInfo(StringBuffer sb, IRModel model, List<IRReteNode> nodes, boolean printRoot)
			throws RException {

		Map<Integer, Map<RReteType, Integer>> priorityMap = new HashMap<>();
		ArrayList<Integer> priorityList = new ArrayList<>();
		Set<RReteType> typeSet = new HashSet<>();

		for (IRReteNode node : nodes) {

			if (!printRoot && RReteType.isRootType(node.getReteType())) {
				continue;
			}

			typeSet.add(node.getReteType());

			Map<RReteType, Integer> _map = priorityMap.get(node.getPriority());
			if (_map == null) {
				_map = new HashMap<>();
				priorityMap.put(node.getPriority(), _map);
				priorityList.add(node.getPriority());
			}

			Integer count = _map.get(node.getReteType());
			if (count == null) {
				count = 0;
			}

			_map.put(node.getReteType(), count + 1);
		}

		Collections.sort(priorityList);
		Collections.reverse(priorityList);

		sb.append("priority info:\n");
		sb.append(SEP_LINE1);

		sb.append("Priority       ");
		for (RReteType reteType : RReteType.ALL_RETE_TYPE) {
			if (!typeSet.contains(reteType)) {
				continue;
			}
			sb.append(String.format(" %5s", "" + reteType));
		}
		sb.append("\n");
		sb.append(SEP_LINE2);

		for (int priority : priorityList) {

			String head = String.format("%03d", priority);
			String pn = _getPriorityName(priority);
			if (!pn.isEmpty()) {
				head = head + "(" + pn + ")";
			}

			sb.append(String.format("%-13s: ", head));
			Map<RReteType, Integer> _map = priorityMap.get(priority);

			for (RReteType reteType : RReteType.ALL_RETE_TYPE) {

				if (!typeSet.contains(reteType)) {
					continue;
				}

				Integer count = _map.get(reteType);
				if (count == null) {
					sb.append("      ");
				} else {
					sb.append(String.format("%6d", count));
				}

			}

			sb.append("\n");
		}

		sb.append(SEP_LINE1);
		sb.append("\n");
	}

	private static void _printReteCounter(StringBuffer sb, IReteNodeMatrix nodeMatrix, boolean isRule)
			throws RException {

		IRModel model = nodeMatrix.getModel();
		IRNodeGraph graph = model.getNodeGraph();

		IRReteNodeCounter reteCounter = RuleFactory.createReteCounter(nodeMatrix);
		ArrayList<IRReteNode> nodes = new ArrayList<>(nodeMatrix.getAllNodes());
		Collections.sort(nodes, (n1, n2) -> {

			int d = n1.getReteType().getIndex() - n2.getReteType().getIndex();
			if (d == 0) {
				d = n1.getNodeId() - n2.getNodeId();
			}

			return d;
		});

		{
			sb.append(SEP_LINE1);
			sb.append(String.format("%-7s", "NODE"));
			for (RCountType countType : RCountType.ALL_COUNT_TYPE) {
				sb.append(String.format(" %" + _getCountTypeLength(countType) + "s", _getCountTypeName(countType)));
			}
			sb.append("\n");
			sb.append(SEP_LINE2);

			for (RReteType reteType : RReteType.ALL_RETE_TYPE) {

				if (reteCounter.getCount(reteType, RCountType.NodeCount) == 0) {
					continue;
				}

				if (isRule && RReteType.isRootType(reteType)) {
					continue;
				}

				sb.append(String.format("%-8s", "" + reteType));
				for (RCountType countType : RCountType.ALL_COUNT_TYPE) {
					sb.append(String.format(" %" + _getCountTypeLength(countType) + "d",
							reteCounter.getCount(reteType, countType)));
				}

				sb.append("\n");
			}

			if (!isRule && graph.getGcRemoveNodeCount() > 0) {

				sb.append(String.format("%-8s", "GC(" + graph.getGcRemoveNodeCount() + ")"));
				for (RCountType countType : RCountType.ALL_COUNT_TYPE) {
					sb.append(String.format(" %" + _getCountTypeLength(countType) + "d",
							graph.getGcNodeRemoveCount(countType)));
				}

				sb.append("\n");
			}

			sb.append(SEP_LINE1);
		}

		/****************************************************/
		// Output node info
		/****************************************************/
		_printNodeInfo1(sb, model, nodes, !isRule);

		/****************************************************/
		// Output node info2
		/****************************************************/
		if (!isRule) {

			_printNodeInfo2(sb, model, nodes);
			_printNodeInfo3(sb, model, nodes);

			// Output node constraint
			printNodeInfo4(sb, model, nodes);
			_printNodeInfo5Action(sb, model, nodes);
			_printNodeInfo6(sb, model, nodes);
		}

		/****************************************************/
		// Output priority info
		/****************************************************/
		_printPriorityInfo(sb, model, nodes, !isRule);

		/****************************************************/
		// Output model node source info
		/****************************************************/
		if (!isRule) {
			_printNodeSource(sb, nodeMatrix, nodes);
		}

		/****************************************************/
		// Output model node bind info
		/****************************************************/
		if (!isRule) {

			boolean outputHead = false;

			for (IRReteNode node : nodes) {

				Collection<IRReteNode> bindToNodes = model.getNodeGraph().getBindToNodes(node);
				if (bindToNodes.isEmpty()) {
					continue;
				}

				if (!outputHead) {
					sb.append("node bind info:\n");
					sb.append(SEP_LINE1);
					sb.append(" NODE         bind-to       UniqName\n");
					sb.append(SEP_LINE2);
					outputHead = true;
				}

				String nodeName = String.format("%s(%s)", node.getNodeName(), node.getReteType());

				if (node.getReteType() != RReteType.RULE) {
					sb.append(String.format("%12s: %12s  %s\n", nodeName, "", node.getUniqName()));
				} else {
					sb.append(String.format("%12s: %12s  %s ==> %s\n", nodeName, "",
							RulpUtil.toString(((XRNodeRule0) node).getMatchStmtList()),
							RulpUtil.toString(((XRNodeRule0) node).getActionStmtList())));
				}

				List<IRReteNode> sourceNodes = new ArrayList<>(bindToNodes);
				Collections.sort(sourceNodes, (n1, n2) -> {
					return n1.getNodeName().compareTo(n2.getNodeName());
				});

				if (node.getParentNodes() != null) {
					for (IRReteNode pnode : node.getParentNodes()) {
						sourceNodes.add(pnode);
					}
				}

				int nodeIndex = 0;
				for (IRReteNode sourceNode : sourceNodes) {

					String srcName = String.format("%s(%s)", sourceNode.getNodeName(), sourceNode.getReteType());

					if (sourceNode.getReteType() != RReteType.RULE) {
						sb.append(String.format("%12d: %-12s  %s\n", nodeIndex++, srcName, sourceNode.getUniqName()));
					} else {
						sb.append(String.format("%12d: %-12s\n", nodeIndex++, srcName));
					}

				}
			}

			if (outputHead) {
				sb.append(SEP_LINE1);
				sb.append("\n");
			}
		}
	}

	private static void _printUpdateTable(StringBuffer sb, IRModel model) throws RException {

		IReteNodeMatrix nodeMatrix = model.getNodeGraph().getNodeMatrix();

		ArrayList<IRReteNode> nodes = new ArrayList<>(nodeMatrix.getAllNodes());
		Collections.sort(nodes, (n1, n2) -> {
			return n2.getUpdateCounter().getTotalCount() - n1.getUpdateCounter().getTotalCount();
		});

		ArrayList<DeCounter> counterList = new ArrayList<>();
		ArrayList<IRReteNode> nodeList = new ArrayList<>();

		for (IRReteNode node : nodes) {
			if (node.getUpdateCounter().getTotalCount() == 0) {
				continue;
			}
			counterList.add(node.getUpdateCounter());
			nodeList.add(node);
		}

		if (nodeList.isEmpty()) {
			return;
		}

		ArrayList<String> counterLines = TraceUtil.formatCounterTable(counterList, AbsReteNode.MAX_EXEC_COUNTER_SIZE);
		int execLineIndex = 0;

		sb.append(String.format("node update info: model-exec-count=%d, %s\n", model.getCounter().getNodeExecuteCount(),
				counterLines.get(execLineIndex++)));
		sb.append(SEP_LINE1);
		sb.append(String.format("%-9s %4s %4s %4s %s\n", "NODE[n]", "Update", "Exec", "Idle",
				counterLines.get(execLineIndex++)));
		sb.append(SEP_LINE2);

		for (int i = 0; i < nodeList.size(); ++i) {
			IRReteNode node = nodeList.get(i);
			sb.append(String.format("%-9s %4d %4d %4d %s\n", node.getNodeName() + "[" + node.getEntryLength() + "]",
					counterList.get(i).getTotalCount(), node.getNodeExecCount(), node.getNodeIdleCount(),
					counterLines.get(execLineIndex++)));
		}
		sb.append(SEP_LINE1);
		sb.append("\n");

	}

	private static String _simpleCountStr(String str) {

		while (!str.isEmpty()) {

			if (str.equals("0")) {
				return "";
			}

			if (!str.endsWith(",0")) {
				break;
			}

			str = str.substring(0, str.length() - 2);
		}

		return str;
	}

	public static String dumpAndCheckEntryTable(IREntryTable entryTable) throws RException {

		StringBuffer sb = new StringBuffer();

		IFixEntryArray<? extends IRReteEntry> entryFixArray = entryTable.getEntryFixArray();
		IFixEntryArray<? extends IRReference> refFixArray = entryTable.getReferenceFixArray();

		/****************************************************/
		// Dump entry fix array
		/****************************************************/
		{

			int maxEntryId = entryFixArray.getEntryMaxId();

			sb.append(SEP_LINE1);
			sb.append(String.format("%-12s: max-id=%d, count=%d\n", "ENTRY ARRAY", maxEntryId,
					entryFixArray.getEntryCount()));
			sb.append(SEP_LINE2);

			for (int entryId = 0; entryId <= maxEntryId; ++entryId) {

				IRReteEntry entry = entryFixArray.getEntry(entryId);
				if (entry == null || entry.isDroped()) {
					continue;
				}

				sb.append(String.format("%08d: entry=%s, status=%s, child[%d]", entry.getEntryId(), entry,
						entry.getStatus(), entry.getChildCount()));

				if (entry.getChildCount() > 0) {

					sb.append("=");

					Iterator<? extends IRReference> it = entry.getChildIterator();
					while (it.hasNext()) {

						IRReference ref = it.next();
						if (refFixArray.getEntry(ref.getEntryId()) != ref) {
							throw new RException("Invalid ref: " + ref);
						}

						sb.append(" " + ref.getEntryId() + ",");
					}

				} else {
					sb.append(",");
				}

				sb.append(String.format(" ref[%d]", entry.getReferenceCount()));
				if (entry.getReferenceCount() > 0) {

					sb.append("=");

					Iterator<? extends IRReference> it = entry.getReferenceIterator();
					while (it.hasNext()) {

						IRReference ref = it.next();
						if (refFixArray.getEntry(ref.getEntryId()) != ref) {
							throw new RException("Invalid ref: " + ref);
						}

						sb.append(" " + ref.getEntryId() + ",");
					}

				} else {
					sb.append(",");
				}

				sb.append("\n");
			}

			sb.append("\n\n");
		}

		/****************************************************/
		// Dump ref fix array
		/****************************************************/
		{

			int maxEntryId = refFixArray.getEntryMaxId();

			sb.append(SEP_LINE1);
			sb.append(String.format("%-12s: max-id=%d, count=%d\n", "REF ARRAY", maxEntryId,
					refFixArray.getEntryCount()));
			sb.append(SEP_LINE2);

			for (int entryId = 0; entryId <= maxEntryId; ++entryId) {

				IRReference ref = refFixArray.getEntry(entryId);
				if (ref == null || ref.isDroped()) {
					continue;
				}

				int parentCount = ref.getParentEntryCount();

				IRReteEntry childEntry = ref.getChildEntry();
				if (entryFixArray.getEntry(childEntry.getEntryId()) != childEntry) {
					throw new RException("Invalid entry: " + childEntry);
				}

				sb.append(String.format("%08d: node=%d, child=%d, parent[%d]", ref.getEntryId(),
						ref.getNode().getNodeId(), childEntry.getEntryId(), parentCount));

				if (parentCount > 0) {

					sb.append("=");

					for (int i = 0; i < parentCount; ++i) {

						IRReteEntry parentEntry = ref.getParentEntry(i);
						if (entryFixArray.getEntry(parentEntry.getEntryId()) != parentEntry) {
							throw new RException("Invalid entry: " + parentEntry);
						}

						sb.append(" " + parentEntry.getEntryId() + ",");
					}

				} else {
					sb.append(",");
				}

				sb.append("\n");
			}
		}

		return sb.toString();
	}

	public static String formatEntryTableCount(IREntryTable entryTable) throws RException {

		String out = "";

		/****************************************************/
		// MaxId & size
		/****************************************************/
		final int endEntryId = entryTable.getEntryMaxId();
		final int beginEntryId = 1;
		out += String.format("size=%d, max-id=%d", entryTable.getEntryCount(), endEntryId);

		/****************************************************/
		// Reference count
		/****************************************************/
		{
			int dupCount = 0;
			int nullCount = 0;

			Map<Integer, Integer> referCountMap = new HashMap<>();

			for (int entryId = beginEntryId; entryId <= endEntryId; ++entryId) {

				IRReteEntry entry = entryTable.getEntry(entryId);
				if (entry == null) {
					nullCount++;
					continue;
				}

				int referCount = entry.getReferenceCount();
				Integer num = referCountMap.get(referCount);
				referCountMap.put(referCount, num == null ? 1 : num + 1);

				if (referCount <= 1) {
					continue;
				}

				Set<IRReteNode> nodes = new HashSet<>();

				Iterator<? extends IRReference> refIt = entry.getReferenceIterator();
				while (refIt.hasNext()) {
					IRReference ref = refIt.next();
					if (nodes.contains(ref.getNode())) {
						++dupCount;
					} else {
						nodes.add(ref.getNode());
					}
				}
			}

			ArrayList<Integer> refCountList = new ArrayList<>(referCountMap.size());
			Collections.sort(refCountList);

			out += String.format(", dup=%d, null=%d", dupCount, nullCount);

			for (int refCount : refCountList) {
				out += String.format(", ref-%d=%d", refCount, referCountMap.get(refCount));
			}
		}

		return out;
	}

	public static String formatModelCount(IRModelCounter counter) throws RException {

		IRModel model = counter.getModel();
		IREntryTable storageMgr = model.getEntryTable();
		Map<IRReteNode, Integer> wasteMap = getWasteMap(model.getNodeGraph().getNodeMatrix().getAllNodes());

		int wasteNodeNum = 0;
		int wasteMatchNum = 0;

		for (int wn : wasteMap.values()) {
			wasteMatchNum += wn;
			wasteNodeNum++;
		}

		return String.format(
				"stmt=%d, rule=%d, match=%d, fetch=%d, exec=%d, idle=%d, state=%d/%d, max-queue=%d, "
						+ "uniq-obj=%d, entry-cnt=%d, entry-maxid=%d, waste-node=%d, waste-match=%d",
				counter.getStatementCount(), counter.getRuleCount(), counter.getQueryMatchCount(),
				counter.getQueryFetchCount(), counter.getNodeExecuteCount(), counter.getNodeIdleCount(),
				model.getRunState().getIndex(), counter.getStateChangeCount(), counter.getProcessQueueMaxNodeCount(),
				model.getNodeGraph().getUniqueObjectCount(),
//			counter.getObjectCount(),
				storageMgr.getEntryCount(), storageMgr.getEntryMaxId(), wasteNodeNum, wasteMatchNum);

	}

	public static String formatNodeCount(IRReteNodeCounter reteCounter) throws RException {

		String out = "";

		// root0 node
		{
			String count = String.format("%d,%d,%d,%d,%d,%d,%d,%d,%d",
					reteCounter.getCount(RReteType.ROOT0, RCountType.FixedCount),
					reteCounter.getCount(RReteType.ROOT0, RCountType.DefinedCount),
					reteCounter.getCount(RReteType.ROOT0, RCountType.ReasonCount),
					reteCounter.getCount(RReteType.ROOT0, RCountType.DropCount),
					reteCounter.getCount(RReteType.ROOT0, RCountType.RemoveCount),
					reteCounter.getCount(RReteType.ROOT0, RCountType.TempCount),
					reteCounter.getCount(RReteType.ROOT0, RCountType.AssumeCount),
					reteCounter.getCount(RReteType.ROOT0, RCountType.NullCount),
					reteCounter.getCount(RReteType.ROOT0, RCountType.RedundantCount));
			count = _simpleCountStr(count);
			out += String.format("sc=[%s],", count);
		}

		// table node
		if (reteCounter.getCount(RReteType.NAME0, RCountType.NodeCount) > 0) {
			String count = String.format("%d,%d,%d,%d,%d", reteCounter.getCount(RReteType.NAME0, RCountType.FixedCount),
					reteCounter.getCount(RReteType.NAME0, RCountType.NodeCount),
					reteCounter.getCount(RReteType.NAME0, RCountType.DefinedCount),
					reteCounter.getCount(RReteType.NAME0, RCountType.ExecCount),
					reteCounter.getCount(RReteType.NAME0, RCountType.IdleCount),
					reteCounter.getCount(RReteType.NAME0, RCountType.UpdateCount));
			count = _simpleCountStr(count);
			out += String.format(" %s=[%s],", _getReteTypeShortName(RReteType.NAME0), count);
		}

		// var node
		if (reteCounter.getCount(RReteType.VAR, RCountType.NodeCount) > 0) {

			String count = String.format("%d, %d, %d, %d, %d",
					reteCounter.getCount(RReteType.VAR, RCountType.NodeCount),
					reteCounter.getCount(RReteType.VAR, RCountType.FixedCount),
					reteCounter.getCount(RReteType.VAR, RCountType.DefinedCount),
					reteCounter.getCount(RReteType.VAR, RCountType.ExecCount),
					reteCounter.getCount(RReteType.VAR, RCountType.IdleCount),
					reteCounter.getCount(RReteType.VAR, RCountType.UpdateCount));
			count = _simpleCountStr(count);
			out += String.format(" vc=[%s],", count);
		}

		for (RReteType rtype : DETAIL_RETE_TYPES) {

			if (reteCounter.getCount(rtype, RCountType.NodeCount) > 0) {

				String name = _getReteTypeShortName(rtype);

				String count1 = String.format("%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d",
						reteCounter.getCount(rtype, RCountType.NodeCount),
						reteCounter.getCount(rtype, RCountType.SourceCount),
						reteCounter.getCount(rtype, RCountType.MaxLevel),
						reteCounter.getCount(rtype, RCountType.MaxPriority),
						reteCounter.getCount(rtype, RCountType.ExecCount),
						reteCounter.getCount(rtype, RCountType.IdleCount),
						reteCounter.getCount(rtype, RCountType.FailedCount),
						reteCounter.getCount(rtype, RCountType.MatchCount),
						reteCounter.getCount(rtype, RCountType.UpdateCount),
						reteCounter.getCount(rtype, RCountType.RedundantCount),
						reteCounter.getCount(rtype, RCountType.QueryFetch),
						reteCounter.getCount(rtype, RCountType.QueryMatch),
						reteCounter.getCount(rtype, RCountType.BindFromCount),
						reteCounter.getCount(rtype, RCountType.BindToCount));

				String count2 = String.format("%d,%d,%d,%d,%d,%d,%d,%d",
						reteCounter.getCount(rtype, RCountType.FixedCount),
						reteCounter.getCount(rtype, RCountType.DefinedCount),
						reteCounter.getCount(rtype, RCountType.ReasonCount),
						reteCounter.getCount(rtype, RCountType.DropCount),
						reteCounter.getCount(rtype, RCountType.RemoveCount),
						reteCounter.getCount(rtype, RCountType.TempCount),
						reteCounter.getCount(rtype, RCountType.AssumeCount),
						reteCounter.getCount(rtype, RCountType.NullCount));

				count1 = _simpleCountStr(count1);
				count2 = _simpleCountStr(count2);

				if (count2.isEmpty()) {
					out += String.format(" %s=[%s],", name, count1);
				} else {
					out += String.format(" %s=[%s/%s],", name, count1, count2);
				}

			}
		}

		return out;

	}

	public static String formatRuleCount(IRRuleCounter counter) throws RException {
		return String.format("stmt=%d, entry=%d, node=%d, exec=%d, update=%d", counter.getStatementCount(),
				counter.getEntryCount(), counter.getNodeCount(), counter.getExecuteCount(), counter.getUpdateCount());
	}

	public static Map<IRReteNode, Integer> getWasteMap(Collection<? extends IRReteNode> nodes) {

		Map<IRReteNode, Integer> wasteMap = new HashMap<>();

		Set<IRReteNode> checkSet = new HashSet<>();
		LinkedList<IRReteNode> checkQueue = new LinkedList<>();

		/************************************/
		// find node when exec=idle
		/************************************/
		for (IRReteNode node : nodes) {

			if (!_isInactiveNode(node)) {
				continue;
			}

			wasteMap.put(node, 0);

			if (node.getParentNodes() != null) {
				for (IRReteNode pnode : node.getParentNodes()) {
					if (!checkSet.contains(pnode) && !wasteMap.containsKey(pnode)) {
						checkSet.add(pnode);
						checkQueue.add(pnode);
					}
				}
			}

		}

		while (!checkQueue.isEmpty()) {

			IRReteNode node = checkQueue.removeFirst();
			checkSet.remove(node);

			boolean noWasted = false;

			for (IRReteNode cnode : node.getChildNodes()) {
				if (!wasteMap.containsKey(cnode)) {
					noWasted = true;
					break;
				}
			}

			if (!noWasted) {

				wasteMap.put(node, node.getNodeMatchCount());

				if (node.getParentNodes() != null) {
					for (IRReteNode pnode : node.getParentNodes()) {
						if (!checkSet.contains(pnode) && wasteMap.containsKey(pnode)) {
							checkSet.add(pnode);
							checkQueue.add(pnode);
						}
					}
				}
			}
		}

		return wasteMap;
	}

	public static String printNodeInfo(IRModel model) throws RException {

		StringBuffer sb = new StringBuffer();

		ArrayList<IRReteNode> nodes = new ArrayList<>(model.getNodeGraph().getNodeMatrix().getAllNodes());
		Collections.sort(nodes, (n1, n2) -> {
			return n1.getNodeId() - n2.getNodeId();
		});

		_printNodeInfo1(sb, model, nodes, true);

		return sb.toString();
	}

	public static void printNodeInfo4(StringBuffer sb, IRModel model, List<IRReteNode> nodes) throws RException {

		ArrayList<IRReteNode> constraintNodeList = new ArrayList<>();
		for (IRReteNode node : nodes) {
			if (RReteType.isBetaType(node.getReteType())) {
				if (node.getConstraint1Count() == 0 && RuleUtil.asBetaNode(node).getConstraint2Count() == 0) {
					continue;
				}
			} else {
				if (node.getConstraint1Count() == 0) {
					continue;
				}
			}

			constraintNodeList.add(node);
		}

		if (constraintNodeList.isEmpty()) {
			return;
		}

		sb.append("node info4: constraint\n");
		sb.append(SEP_LINE1);
		sb.append(String.format("%-12s %5s %5s  %s\n", "NODE[n]", "Match", "Fail", "Constraint"));
		sb.append(SEP_LINE2);

		for (IRReteNode node : constraintNodeList) {

			ArrayList<IRConstraint> consList = new ArrayList<>();
			if (node.getConstraint1Count() > 0) {
				for (int i = 0; i < node.getConstraint1Count(); ++i) {
					consList.add(node.getConstraint1(i));
				}
			}

			if (RReteType.isBetaType(node.getReteType()) && RuleUtil.asBetaNode(node).getConstraint2Count() > 0) {
				consList.addAll(RuleUtil.asBetaNode(node).getConstraint2List());
			}

			sb.append(String.format("%-12s %5d %5d\n", node.getNodeName() + "[" + node.getEntryLength() + "]",
					node.getNodeMatchCount(), node.getAddEntryFailCount()));

			for (int i = 0; i < consList.size(); ++i) {
				IRConstraint cons = consList.get(i);
				sb.append(String.format("%11s  %5d %5d  %s\n", "", cons.getMatchCount(), cons.getFailCount(),
						cons.getConstraintKind() + ":" + cons));
			}
		}

		sb.append(SEP_LINE1);
		sb.append("\n");
	}

	public static String printRefInfo(IRModel model) throws RException {

		StringBuffer sb = new StringBuffer();

		RefPrinter refPrinter = new RefPrinter(model);

		for (IRList stmt : RuleUtil.listStatements(model)) {
			sb.append(refPrinter.printStmt(stmt));
		}

		return sb.toString();
	}

	public static String printStatsInfo(IRModel model) {

		long ruleSummary[][] = new long[RETE_TYPE_NUM][COUNT_TYPE_NUM];
		for (int i = 0; i < RETE_TYPE_NUM; ++i) {
			for (int j = 0; j < COUNT_TYPE_NUM; ++j) {
				ruleSummary[i][j] = 0;
			}
		}

		StringBuffer sb = new StringBuffer();

		try {

			IReteNodeMatrix modelNodeMatrix = model.getNodeGraph().getNodeMatrix();

			/*********************************************************************/
			// Model stats
			/*********************************************************************/
			sb.append(String.format("Model<%s> stats info:\n", "" + model.getModelName()));
			_printReteCounter(sb, modelNodeMatrix, false);

			/****************************************************/
			// Output count info
			/****************************************************/
			_printModelCountInfo(sb, model);

			/*********************************************************************/
			// Rule stats
			/*********************************************************************/
			ArrayList<RuleCounter> ruleCounterList = new ArrayList<>();
			for (IRReteNode ruleNode : model.getNodeGraph().listNodes(RReteType.RULE)) {

				RuleCounter rc = new RuleCounter();
				rc.rule = (IRRule) ruleNode;
				rc.counter = RuleFactory.createReteCounter(rc.rule.getNodeMatrix());

				sb.append(String.format("Rule<%s> stats: priority=%03d, expr=%s\n", "" + rc.rule.getRuleName(),
						rc.rule.getPriority(), rc.rule.getRuleDecription()));

				_printReteCounter(sb, rc.rule.getNodeMatrix(), true);

				ruleCounterList.add(rc);

				for (RReteType reteType : RReteType.ALL_RETE_TYPE) {
					for (RCountType countType : RCountType.ALL_COUNT_TYPE) {
						ruleSummary[reteType.getIndex()][countType.getIndex()] += rc.counter.getCount(reteType,
								countType);
					}
				}

			}

			/*********************************************************************/
			// Rule order
			/*********************************************************************/
			if (!ruleCounterList.isEmpty()) {

				sb.append("Rule resource\n");
				sb.append(SEP_LINE1);
				sb.append(String.format("%-10s: ", "RULE"));
				for (OutputType outType : ALL_OUT_TYPE) {
					sb.append(String.format("%10s", "" + _getReteTypeShortName(outType.reteType).toUpperCase() + "-"
							+ _getCountTypeName(outType.countType)));
				}

				sb.append("\n");
				sb.append(SEP_LINE2);

				Collections.sort(ruleCounterList, (c1, c2) -> {
					return c1.rule.getRuleName().compareTo(c2.rule.getRuleName());
				});

				for (RuleCounter rc : ruleCounterList) {

					sb.append(String.format("%-10s: ", rc.rule.getRuleName()));

					for (OutputType outType : ALL_OUT_TYPE) {
						sb.append(String.format("%10d", rc.counter.getCount(outType.reteType, outType.countType)));
					}

					sb.append("\n");
				}

				sb.append(SEP_LINE1);
				sb.append("\n");
			}

			/*********************************************************************/
			// Model share index
			/*********************************************************************/
			_printModelShareIndex(sb, modelNodeMatrix, ruleSummary);

			/*********************************************************************/
			// Beta Node state info
			/*********************************************************************/
			_printNodeStats2(sb, modelNodeMatrix, RReteType.BETA0, RReteType.BETA2, RReteType.BETA3);

			/*********************************************************************/
			// Unused rule list
			/*********************************************************************/

			/*********************************************************************/
			// Entry table
			/*********************************************************************/
			_printEntryTable(sb, model, model.getEntryTable());
			sb.append("\n");

			/*********************************************************************/
			// Frame info
			/*********************************************************************/
			_printModelFrame(sb, model);

			/*********************************************************************/
			// Exec table
			/*********************************************************************/
			_printUpdateTable(sb, model);
			sb.append("\n");

			/*********************************************************************/
			// Cache Info
			/*********************************************************************/
			if (model.isCacheEnable()) {
				_printCacheInfo(sb, model);
				sb.append("\n");
			}

			/*********************************************************************/
			// Global info
			/*********************************************************************/
			sb.append(TraceUtil.printGlobalInfo(model.getInterpreter()));

			sb.append("\n");

		} catch (RException e) {
			e.printStackTrace();
		}

		return sb.toString();
	}

	@SuppressWarnings("unchecked")
	public static <T> List<T> toList(T... objs) {

		if (objs == null) {
			return Collections.emptyList();
		}

		ArrayList<T> list = new ArrayList<>();
		for (T o : objs) {
			list.add(o);
		}

		return list;
	}

}
