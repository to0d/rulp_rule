package alpha.rulp.ximpl.node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class XRGraphCount {

	public static final String CK_GC_addConstraint = "graph-addConstraint";

	public static final String CK_GC_bindNode = "graph-bindNode";

	public static final String CK_GC_CACHE = "graph-gc-cache";

	public static final String CK_GC_createNodeByTree = "graph-createNodeByTree";

	public static final String CK_GC_createNodeIndex = "graph-createNodeIndex";

	public static final String CK_GC_createNodeRoot = "graph-createNodeRoot";

	public static final String CK_GC_createNodeRule = "graph-createNodeRule";

	public static final String CK_GC_createSubGraphForConstraintCheck = "graph-createSubGraphForConstraintCheck";

	public static final String CK_GC_createSubGraphForQueryNode = "graph-createSubGraphForQueryNode";

	public static final String CK_GC_createSubGraphForRuleGroup = "graph-createSubGraphForRuleGroup";

	public static final String CK_GC_createWorkNode = "graph-createWorkNode";

	public static final String CK_GC_DO_GC = "graph-doGc";

	public static final String CK_GC_DO_OPT = "graph-doOptimize";

	public static final String CK_GC_INACTIVE_LEAF = "graph-gc-inactive-leaf";

	public static final String CK_GC_listSourceNodes = "graph-listSourceNodes";

	public static final String CK_GC_NODE_CLEAN = "graph-gc-node-clean";

	public static final String CK_GC_NODE_REMOVE = "graph-gc-node-remove";

	public static final String CK_GC_removeConstraint = "graph-removeConstraint";

	public static final String CK_GC_removeNode = "graph-removeNode";

	public static final String CK_GC_setRulePriority = "graph-setRulePriority";

	public static List<String> graphCountKeyList = new ArrayList<>();

	static {

		graphCountKeyList.add(CK_GC_DO_GC);
		graphCountKeyList.add(CK_GC_DO_OPT);
		graphCountKeyList.add(CK_GC_NODE_REMOVE);
		graphCountKeyList.add(CK_GC_NODE_CLEAN);
		graphCountKeyList.add(CK_GC_INACTIVE_LEAF);
		graphCountKeyList.add(CK_GC_CACHE);
		graphCountKeyList.add(CK_GC_addConstraint);
		graphCountKeyList.add(CK_GC_bindNode);
		graphCountKeyList.add(CK_GC_createNodeByTree);
		graphCountKeyList.add(CK_GC_createNodeIndex);
		graphCountKeyList.add(CK_GC_createNodeRoot);
		graphCountKeyList.add(CK_GC_createNodeRule);
		graphCountKeyList.add(CK_GC_createSubGraphForConstraintCheck);
		graphCountKeyList.add(CK_GC_createSubGraphForQueryNode);
		graphCountKeyList.add(CK_GC_createSubGraphForRuleGroup);
		graphCountKeyList.add(CK_GC_createWorkNode);
		graphCountKeyList.add(CK_GC_listSourceNodes);
		graphCountKeyList.add(CK_GC_removeConstraint);
		graphCountKeyList.add(CK_GC_removeNode);
		graphCountKeyList.add(CK_GC_setRulePriority);

		graphCountKeyList = Collections.unmodifiableList(graphCountKeyList);
	}

	public int addConstraint = 0;

	public int bindNode = 0;

	public int createNodeByTree = 0;

	public int createNodeIndex = 0;

	public int createNodeRoot = 0;

	public int createNodeRule = 0;

	public int createSubGraphForConstraintCheck = 0;

	public int createSubGraphForQueryNode = 0;

	public int createSubGraphForRuleGroup = 0;

	public int createWorkNode = 0;

	public int doGc = 0;

	public int doOptimize = 0;

	public int gcCacheCount = 0;

	public int gcCleanNodeCount = 0;

	public int gcInactiveLeafCount = 0;

	public int gcRemoveNodeCount = 0;

	public int listSourceNodes = 0;

	public int removeConstraint = 0;

	public int removeNode = 0;

	public int setRulePriority = 0;

	public static List<String> getCounterKeyList() {
		return graphCountKeyList;
	}

	public long getCounterValue(String countkey) {

		switch (countkey) {
		case CK_GC_DO_GC:
			return doGc;

		case CK_GC_DO_OPT:
			return doOptimize;

		case CK_GC_NODE_REMOVE:
			return gcRemoveNodeCount;

		case CK_GC_NODE_CLEAN:
			return gcCleanNodeCount;

		case CK_GC_INACTIVE_LEAF:
			return gcInactiveLeafCount;

		case CK_GC_CACHE:
			return gcCacheCount;

		case CK_GC_addConstraint:
			return addConstraint;

		case CK_GC_bindNode:
			return bindNode;

		case CK_GC_createNodeByTree:
			return createNodeByTree;

		case CK_GC_createNodeIndex:
			return createNodeIndex;

		case CK_GC_createNodeRoot:
			return createNodeRoot;

		case CK_GC_createNodeRule:
			return createNodeRule;

		case CK_GC_createSubGraphForConstraintCheck:
			return createSubGraphForConstraintCheck;

		case CK_GC_createSubGraphForQueryNode:
			return createSubGraphForQueryNode;

		case CK_GC_createSubGraphForRuleGroup:
			return createSubGraphForRuleGroup;

		case CK_GC_createWorkNode:
			return createWorkNode;

		case CK_GC_listSourceNodes:
			return listSourceNodes;

		case CK_GC_removeConstraint:
			return removeConstraint;

		case CK_GC_removeNode:
			return removeNode;

		case CK_GC_setRulePriority:
			return setRulePriority;

		}

		return 0;
	}
}
