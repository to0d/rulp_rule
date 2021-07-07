package alpha.rulp.ximpl.factor;

import static alpha.rulp.lang.Constant.O_Nil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.runtime.IRFactor;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.runtime.IROut;
import alpha.rulp.utils.RefPrinter;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.entry.IRReference;
import alpha.rulp.ximpl.entry.IRReteEntry;
import alpha.rulp.ximpl.model.IRuleFactor;
import alpha.rulp.ximpl.node.IRReteNode;
import alpha.rulp.ximpl.node.IRRootNode;

public class XRFactorProveStmt extends AbsRFactorAdapter implements IRFactor, IRuleFactor {

	public XRFactorProveStmt(String factorName) {
		super(factorName);
	}

	static class StmtProveUtil {

		static class ProveEntry {

			public List<String> factStmtList = null;

			public List<IRReteNode> relatedNodes = null;

			public List<Integer> refList = null;

			public String _refPathStr = null;

			public String getRefPathString() {

				if (_refPathStr == null) {
					if (_refPathStr == null || _refPathStr.isEmpty()) {
						_refPathStr = "";
					} else {
						_refPathStr = refList.toString();
					}
				}
				return _refPathStr;
			}
		}

		static class ProveNode {

			public final String stmtUniqName;

			public ProveNode(String stmtUniqName, IRReteEntry stmtEntry) {
				super();
				this.stmtUniqName = stmtUniqName;
				this.stmtEntry = stmtEntry;
			}

			public final IRReteEntry stmtEntry;

			public List<ProveEntry> proveEntryList = null;

			public int getProveEntryCount() {
				return proveEntryList == null ? 0 : proveEntryList.size();
			}

			public boolean visitCompleted = false;
		}

		private Map<String, ProveNode> proveMap = new HashMap<>();

		private IRModel model;

		public StmtProveUtil(IRModel model) {
			super();
			this.model = model;
		}

		public ProveNode getProveNode(IRReteEntry entry, int limit) {

			String uniqName = entry.toString();

			ProveNode proveNode = proveMap.get(uniqName);
			if (proveNode != null) {

				if (proveNode.visitCompleted) {
					return proveNode;
				}

				if (limit < 0 && proveNode.getProveEntryCount() >= limit) {
					return proveNode;
				}
			} else {
				proveNode = new ProveNode(uniqName, entry);
				buildProveNode(proveNode, entry, new LinkedList<>(), limit);
			}

			return proveNode;
		}

		public void buildProveNode(ProveNode proveNode, IRReteEntry entry, Queue<IRReference> refPath, int limit) {

			
		}

		public String proveStmt(IRList stmt) throws RException {

			/****************************************************/
			// Find root node
			/****************************************************/
			IRRootNode rootNode = null;
			if (stmt.getNamedName() == null) {
				rootNode = model.getNodeGraph().findRootNode(stmt.size());
			} else {
				rootNode = model.getNodeGraph().findNamedNode(stmt.getNamedName());
				if (rootNode != null && rootNode.getEntryLength() != stmt.size()) {
					rootNode = null;
				}
			}

			String uniqName = RulpUtil.toString(stmt);

			if (rootNode == null) {
				return String.format("==> %s: node not found\n", uniqName);
			}

			IRReteEntry entry = rootNode.getStmt(uniqName);
			if (entry == null) {
				return String.format("==> %s: entry not found\n", uniqName);
			}

			StringBuilder sb = new StringBuilder();
//			printEntry(sb, entry.getEntryId(), 0);

			return sb.toString();
		}
	}

	@Override
	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

		if (args.size() < 3) {
			throw new RException("Invalid parameters: " + args);
		}

		IRModel model = RuleUtil.asModel(interpreter.compute(frame, args.get(1)));
		IRList stmt = RulpUtil.asList(interpreter.compute(frame, args.get(2)));
		interpreter.getOut().out(new StmtProveUtil(model).proveStmt(stmt));

		return O_Nil;
	}
}
