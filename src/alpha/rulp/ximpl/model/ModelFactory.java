package alpha.rulp.ximpl.model;

import java.util.List;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.IRVar;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.utils.MatchTree;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.entry.IRResultQueue;

public class ModelFactory {

	public static IRResultQueue createResultQueue(IRModel model, IRObject rstExpr, IRList condList) throws RException {

		/******************************************************/
		// Build frame
		/******************************************************/
		IRFrame queryFrame = RulpFactory.createFrame(model.getFrame(), "QUERY");
		RuleUtil.setDefaultModel(queryFrame, model);
		RulpUtil.incRef(queryFrame);

		/******************************************************/
		// Build var list
		/******************************************************/
		List<IRList> matchStmtList = ReteUtil.toCondList(condList, model.getNodeGraph());
		IRList matchTree = MatchTree.build(matchStmtList, model.getInterpreter(), model.getFrame());
		IRObject[] varEntry = ReteUtil._varEntry(ReteUtil.buildTreeVarList(matchTree));
		IRVar[] vars = new IRVar[varEntry.length];

		for (int i = 0; i < varEntry.length; ++i) {
			IRObject obj = varEntry[i];
			if (obj != null) {
				vars[i] = queryFrame.addVar(RulpUtil.asAtom(obj).getName());
			}
		}

		return new XRMultiResultQueue(model.getInterpreter(), queryFrame, rstExpr, vars);
	}
}
