package alpha.rulp.ximpl.cache;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.runtime.IRListener1;
import alpha.rulp.runtime.IRParser;
import alpha.rulp.utils.FileUtil;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.utils.StringUtil;
import alpha.rulp.ximpl.node.RReteType;

public class XRStmtFileDefaultCacher implements IRStmtSaver, IRStmtLoader {

	static final String MODEL_CACHE_SUFFIX = ".mc";

	static String _nodeCacheName(IRReteNode node) throws RException {

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

	private String modelCachePath;

	private int readLines = 0;

	public XRStmtFileDefaultCacher(String modelCachePath, IRInterpreter interpreter) {
		super();
		this.modelCachePath = modelCachePath;
		this.interpreter = interpreter;
	}

	@Override
	public int getReadLines() {
		return readLines;
	}

	@Override
	public void load(IRReteNode node, IRListener1<IRList> stmtListener) throws RException, IOException {

		final IRParser parser = interpreter.getParser();
		final String stmtName = node.getReteType() == RReteType.NAME0 ? ((IRReteNode) node).getNamedName() : null;
		final String nodeCachePath = FileUtil.toValidPath(modelCachePath) + _nodeCacheName(node);
		final int stmtLen = node.getEntryLength();

		if (!FileUtil.isExistFile(nodeCachePath)) {
			return;
		}

		for (String line : FileUtil.openTxtFile(nodeCachePath, "utf-8")) {

			++readLines;

			List<IRObject> list = parser.parse(line);
			if (list.size() != stmtLen) {
				throw new RException(String.format("Invalid stmt for node<%s:%d>: %s", stmtName, stmtLen, list));
			}

			IRList stmt;
			if (stmtName == null) {
				stmt = RulpFactory.createList(list);
			} else {
				stmt = RulpFactory.createNamedList(list, stmtName);
			}

			stmtListener.doAction(stmt);
		}
		
	}

	@Override
	public int save(IRReteNode node, List<IRList> stmtList) throws RException, IOException {

		final String stmtName = node.getReteType() == RReteType.NAME0 ? ((IRReteNode) node).getNamedName() : null;
		final String nodeCachePath = FileUtil.toValidPath(modelCachePath) + _nodeCacheName(node);
		final int stmtLen = node.getEntryLength();

		return 0;
	}

}
