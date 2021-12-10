package alpha.rulp.ximpl.cache;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.runtime.IRListener1;
import alpha.rulp.utils.FileUtil;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.utils.StringUtil;

public class XRStmtFileDefaultCacher implements IRStmtSaver, IRStmtLoader {

	static final String MODEL_CACHE_SUFFIX = ".mc";

	public static String getNodeCacheName(IRReteNode node) throws RException {

		switch (node.getReteType()) {
		case ROOT0:
			return "." + node.getEntryLength() + MODEL_CACHE_SUFFIX;

		case NAME0:
			return node.getNamedName() + "." + node.getEntryLength() + MODEL_CACHE_SUFFIX;

		default:
			throw new RException("not support node: " + node);
		}
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

	private String cachePath;

	public XRStmtFileDefaultCacher(String cachePath, IRInterpreter interpreter) {
		super();
		this.cachePath = cachePath;
		this.interpreter = interpreter;
	}

	@Override
	public int save(List<IRList> stmtList) throws RException, IOException {
		return save(stmtList, cachePath);
	}

	@Override
	public void load(IRListener1<IRList> stmtListener) throws RException, IOException {
		interpreter.compute(StringUtil.toOneLine(FileUtil.openTxtFile(cachePath, "utf-8")), (rst) -> {
			stmtListener.doAction(RulpUtil.asList(rst));
		});
	}

}
