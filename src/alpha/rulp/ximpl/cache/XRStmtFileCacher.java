package alpha.rulp.ximpl.cache;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.utils.FileUtil;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.utils.StringUtil;
import alpha.rulp.ximpl.node.IRNamedNode;
import alpha.rulp.ximpl.node.IRReteNode;

public class XRStmtFileCacher implements IRStmtLoader, IRStmtSaver {

	static final String MODEL_CACHE_SUFFIX = ".mc";

	public static String getNodeCacheName(IRReteNode node) throws RException {

		switch (node.getReteType()) {
		case ROOT0:
			return "." + node.getEntryLength() + MODEL_CACHE_SUFFIX;

		case NAME0:
			return ((IRNamedNode) node).getNamedName() + "." + node.getEntryLength() + MODEL_CACHE_SUFFIX;

		default:
			throw new RException("not support node: " + node);
		}
	}

	public static IRIterator<? extends IRList> load(IRInterpreter interpreter, String cachePath)
			throws RException, IOException {

		ArrayList<IRList> cacheStmtList = new ArrayList<>();
		for (IRObject obj : interpreter.compute(StringUtil.toOneLine(FileUtil.openTxtFile(cachePath, "utf-8")))) {
			cacheStmtList.add(RulpUtil.asList(obj));
		}

		return RuleUtil.toStmtList(RulpFactory.createList(cacheStmtList));
	}

	public static int save(List<IRList> stmtList, String cachePath) throws RException, IOException {

		// clear
		if (stmtList.size() == 0) {

			if (FileUtil.isExistFile(cachePath)) {
				new File(cachePath).delete();
			}

			return 0;
		}
		// save
		else {

			ArrayList<String> outLines = new ArrayList<>();
			int stmtCount = 0;

			for (IRList stmt : stmtList) {

				if (stmt == null) {
					continue;
				}

				++stmtCount;
				outLines.add(RulpUtil.toString(stmt));
			}

			new File(cachePath).getParentFile().mkdirs();

			FileUtil.saveTxtFile(cachePath, outLines, "utf-8");
			return stmtCount;
		}
	}

	private IRInterpreter interpreter;

	public XRStmtFileCacher(IRInterpreter interpreter) {
		super();
		this.interpreter = interpreter;
	}

	@Override
	public IRIterator<? extends IRList> load(IRObject key) throws RException, IOException {
		return load(interpreter, RulpUtil.asString(key).asString());
	}

	@Override
	public int save(List<IRList> stmtList, IRObject key) throws RException, IOException {
		return save(stmtList, RulpUtil.asString(key).asString());
	}

}
