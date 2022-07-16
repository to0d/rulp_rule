package alpha.rulp.ximpl.cache;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.runtime.IRListener1;
import alpha.rulp.utils.FileUtil;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.node.RReteType;

public class XRStmtFileDefaultCacher extends XRStmtLoader implements IRStmtSaver, IRStmtLoader {

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

	private IRInterpreter interpreter;

	private String modelCachePath;

	private IRReteNode node;

	public XRStmtFileDefaultCacher(String modelCachePath, IRReteNode node) {
		super();
		this.modelCachePath = modelCachePath;
		this.node = node;
		this.interpreter = node.getInterpreter();
	}

	private String _saveLine(String line) {

		// no pre
		if (preMap.isEmpty()) {
			return line;
		}

		for (Entry<String, String> e : preMap.entrySet()) {

			String pre = e.getKey();
			String value = e.getValue();

			while (line.indexOf(value) != -1) {
				line = line.replace(value, pre);
			}
		}

		return line;
	}

	@Override
	public int getReadLines() {
		return readLines;
	}

	@Override
	public void load(IRListener1<IRList> stmtListener) throws RException, IOException {

		final String stmtName = node.getReteType() == RReteType.NAME0 ? ((IRReteNode) node).getNamedName() : null;
		final String cachePath = FileUtil.toValidPath(modelCachePath) + _nodeCacheName(node);
		if (!FileUtil.isExistFile(cachePath)) {
			return;
		}

		loadStmt(stmtListener, stmtName, node.getEntryLength(), cachePath, interpreter);
	}

	@Override
	public boolean needSave() {
		return lineNeedUpdate;
	}

	@Override
	public int save(List<? extends IRList> stmtList) throws RException, IOException {

		final String cachePath = FileUtil.toValidPath(modelCachePath) + _nodeCacheName(node);
//		System.out.println(String.format("save cache: %s", cachePath));

		// clear
		if (stmtList.size() == 0) {
			if (FileUtil.isExistFile(cachePath)) {
				new File(cachePath).delete();
			}

			return 0;
		}

		ArrayList<String> outLines = new ArrayList<>();
		// save header
		if (!preMap.isEmpty()) {

			List<String> preList = new ArrayList<>(preMap.keySet());
			Collections.sort(preList);

			for (String pre : preList) {
				outLines.add(String.format("%s '(pre \"%s\" \"%s\")", COMMNET_CMD_HEAD, pre, preMap.get(pre)));
			}
		}

		// save stmts
		for (IRList stmt : stmtList) {

			if (stmt == null) {
				continue;
			}

			if (!ReteUtil.isValidNodeStmt(node, stmt)) {
				throw new RException(String.format("Invalid stmt for node<%s>: %s", "" + node, "" + stmt));
			}

			outLines.add(_saveLine(RulpUtil.toString(stmt.iterator())));
		}

		new File(cachePath).getParentFile().mkdirs();
		FileUtil.saveTxtFile(cachePath, outLines, "utf-8");
		lineNeedUpdate = false;
		return outLines.size();
	}

}
