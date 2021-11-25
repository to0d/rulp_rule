package alpha.rulp.ximpl.search;

import java.io.IOException;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.LoadUtil;
import alpha.rulp.utils.RulpUtil;

public class SearchUtil {

	public static void init(IRInterpreter interpreter, IRFrame frame) throws RException, IOException {
		LoadUtil.loadRulpFromJar(interpreter, frame, "alpha/resource/mts.rulp", "utf-8");
	}

	public static IRAutoSearchMachine createASM(IRModel model, IRList rstList, IRList searchNodes) throws RException {

		XRAutoSearchMachine asm = new XRAutoSearchMachine();

		asm.setModel(model);

		/********************************************/
		// Add search node
		/********************************************/
		for (IRObject searchNodeObj : RulpUtil.toArray(searchNodes)) {
			asm.addSearchEntry(RulpUtil.asList(searchNodeObj));
		}

		/********************************************/
		// Set result list
		/********************************************/
		if (model.getNodeGraph().findNamedNode(rstList.getNamedName()) != null) {
			throw new RException("result node already exist: " + rstList);
		}

		asm.setRstList(rstList);

		return asm;
	}
}
