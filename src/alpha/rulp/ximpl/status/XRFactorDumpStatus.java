package alpha.rulp.ximpl.status;

import static alpha.rulp.lang.Constant.O_Nil;

import java.io.IOException;
import java.util.ArrayList;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.runtime.IRFactor;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.FileUtil;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.utils.StatsUtil;
import alpha.rulp.ximpl.factor.AbsAtomFactorAdapter;
import alpha.rulp.ximpl.model.IRuleFactor;

public class XRFactorDumpStatus extends AbsAtomFactorAdapter implements IRFactor, IRuleFactor {

	public XRFactorDumpStatus(String factorName) {
		super(factorName);
	}

	@Override
	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

		if (args.size() != 3) {
			throw new RException("Invalid parameters: " + args);
		}

		IRModel model = RuleUtil.asModel(interpreter.compute(frame, args.get(1)));
		String dumpPath = RulpUtil.asString(interpreter.compute(frame, args.get(2))).asString();

		ArrayList<String> out = new ArrayList<>();
		out.add(StatsUtil.printStatsInfo(model));
		out.add(StatsUtil.formatModelCount(model.getCounter()));

		try {
			FileUtil.saveTxtFile(dumpPath, out);
		} catch (IOException e) {
			throw new RException(e.toString());
		}

		return O_Nil;
	}

}
