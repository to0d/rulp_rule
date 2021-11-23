package alpha.rulp.ximpl.search;

import java.io.IOException;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.LoadUtil;

public class SearchUtil {

	public static void init(IRInterpreter interpreter, IRFrame frame) throws RException, IOException {
		LoadUtil.loadRulpFromJar(interpreter, frame, "alpha/resource/mts.rulp", "utf-8");
	}

	public static IRAutoSearchMachine createASM(IRModel model, IRList rstList, IRList condList) throws RException {

		if (model.getNodeGraph().findNamedNode(rstList.getNamedName()) != null) {
			throw new RException("result node already exist: " + rstList);
		}

		IRReteNode queryNode = model.findNode(condList);
		return null;

	}
}
