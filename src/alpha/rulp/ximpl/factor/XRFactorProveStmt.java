package alpha.rulp.ximpl.factor;

import static alpha.rulp.lang.Constant.O_Nil;

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
import alpha.rulp.ximpl.entry.IRReteEntry;
import alpha.rulp.ximpl.model.IRuleFactor;
import alpha.rulp.ximpl.node.IRRootNode;

public class XRFactorProveStmt extends AbsRFactorAdapter implements IRFactor, IRuleFactor {

	public XRFactorProveStmt(String factorName) {
		super(factorName);
	}

	static class StmtProveUtil {

		static class ProveEntry {

		}

		private IRModel model;

		public StmtProveUtil(IRModel model) {
			super();
			this.model = model;
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
			printEntry(sb, entry.getEntryId(), 0);

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
