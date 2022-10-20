package alpha.rulp.ximpl.factor;

import static alpha.rulp.lang.Constant.O_True;

import java.util.HashSet;
import java.util.Set;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RType;
import alpha.rulp.rule.IRModel;
import alpha.rulp.runtime.IRFactor;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.utils.StmtUtil;
import alpha.rulp.ximpl.constraint.ConstraintFactory;
import alpha.rulp.ximpl.constraint.IRConstraint1Uniq;
import alpha.rulp.ximpl.model.IRuleFactor;
import alpha.rulp.ximpl.node.IRNamedNode;
import alpha.rulp.ximpl.node.XRNodeNamed;

public class XRFactorAddNodeFunc extends AbsAtomFactorAdapter implements IRFactor, IRuleFactor {

	public XRFactorAddNodeFunc(String factorName) {
		super(factorName);
	}

	@Override
	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

		int argSize = args.size();

		/********************************************/
		// Check parameters
		/********************************************/
		if (argSize != 2 && argSize != 3) {
			throw new RException("Invalid parameters: " + args);
		}

		IRModel model = StmtUtil.getStmtModel(args, interpreter, frame, 3);
		IRList namedList = RulpUtil.asList(StmtUtil.getStmt3Object(args));

		/**************************************************/
		// Check node
		/**************************************************/
		String namedName = namedList.getNamedName();
		if (namedName == null) {
			throw new RException(String.format("need named name: %s", namedList));
		}

		int entryLen = namedList.size();

		IRNamedNode node = (IRNamedNode) model.getNodeGraph().findRootNode(namedName, -1);
		if (node != null) {

			if (node.getEntryLength() != entryLen) {
				throw new RException(
						String.format("entry lengh not match: actual=%d, expect=%s", node.getEntryLength(), entryLen));
			}

			if (node.getFuncEntry() != null) {
				throw new RException(String.format("func entry already exist in node: %s", node));
			}
		}

		/**************************************************/
		// Check func entry
		/**************************************************/
		IRObject[] funcEntry = new IRObject[entryLen];
		Set<String> nodeVarNames = new HashSet<>();

		for (int i = 0; i < entryLen; ++i) {

			IRObject obj = namedList.get(i);
			if (RulpUtil.isVarAtom(obj)) {

				String varName = obj.asString();
				if (nodeVarNames.contains(varName)) {
					throw new RException(String.format("duplicate var name: %s", obj));
				}

				nodeVarNames.add(varName);

				funcEntry[i] = obj;

			} else if (obj.getType() == RType.EXPR) {

				funcEntry[i] = obj;

			} else {
				throw new RException(String.format("unsupport object: %s", obj));
			}
		}

		int varCount = nodeVarNames.size();
		if (varCount == 0) {
			throw new RException(String.format("no var found: %s", namedList));
		}

		if (varCount == entryLen) {
			throw new RException(String.format("no expr found: %s", namedList));
		}

		/**************************************************/
		// Create uniq index
		/**************************************************/
		int[] columnIndexs = new int[varCount];
		{
			int idx = 0;
			for (int i = 0; i < entryLen; ++i) {
				if (funcEntry[i].getType() == RType.ATOM) {
					columnIndexs[idx++] = i;
				}
			}
		}

		/**************************************************/
		// Create node if not exist
		/**************************************************/
		if (node == null) {
			node = (IRNamedNode) model.getNodeGraph().createNodeRoot(namedName, entryLen);
			if (node == null) {
				throw new RException(String.format("Fail to create named node: %s, len=%d", namedName, entryLen));
			}
		}

		/**************************************************/
		// Create uniq index
		/**************************************************/
		XRNodeNamed implNode = (XRNodeNamed) node;
		IRConstraint1Uniq cons = ConstraintFactory.uniq(columnIndexs);
		implNode.setFuncUniqConstraint(cons);
		model.getNodeGraph().addConstraint(implNode, cons);

		/**************************************************/
		// Update func entry
		/**************************************************/
		implNode.setFuncEntry(funcEntry);

		return O_True;
	}
}
