package alpha.rulp.utils;

import java.util.ArrayList;
import java.util.List;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RType;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.runtime.IRIterator;

public class RuleIOUtil {

	public static List<? extends IRList> loadStmt(String path, IRInterpreter interpreter) throws RException {

		ArrayList<IRList> stmts = new ArrayList<>();

		IRList stmtList = LoadUtil.loadRulp(interpreter, path, "utf-8");
		IRIterator<? extends IRObject> it = stmtList.iterator();
		while (it.hasNext()) {
			stmts.add(RulpUtil.asList(it.next()));
		}

		return stmts;
	}
}
