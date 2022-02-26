package alpha.rulp.ximpl.cache;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.runtime.IRListener1;
import alpha.rulp.runtime.IRParser;
import alpha.rulp.utils.FileUtil;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
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

	static final String COMMNET_CMD_HEAD = ";@:";

	static final String CMD_PRE = "pre";

	static final String PRE_END = "##";

	private Map<String, String> preMap = new HashMap<>();

	private boolean lineNeedUpdate = false;

	private void _set_pre(String pre, String value) throws RException {

		if (!pre.endsWith(PRE_END)) {
			throw new RException(String.format("invalid pre: %s", pre));
		}

		String oldVal = preMap.get(pre);

		if (oldVal != null) {
			if (!oldVal.equals(value)) {
				throw new RException(String.format("duplicate pre<%s>: old=%s, new=%s", pre, oldVal, value));
			}

		} else {
			preMap.put(pre, value);
		}
	}

	private String _loadLine(String line) {

		// no pre
		if (preMap.isEmpty()) {
			return line;
		}

		for (Entry<String, String> e : preMap.entrySet()) {

			String pre = e.getKey();
			String value = e.getValue();

			if (!lineNeedUpdate && line.indexOf(value) != -1) {
				lineNeedUpdate = true;
			}

			while (line.indexOf(pre) != -1) {
				line = line.replace(pre, value);
			}
		}

		return line;
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
	public void load(IRReteNode node, IRListener1<IRList> stmtListener) throws RException, IOException {

		final IRParser parser = interpreter.getParser();
		final String stmtName = node.getReteType() == RReteType.NAME0 ? ((IRReteNode) node).getNamedName() : null;
		final String cachePath = FileUtil.toValidPath(modelCachePath) + _nodeCacheName(node);
		final int stmtLen = node.getEntryLength();

		if (!FileUtil.isExistFile(cachePath)) {
			return;
		}

		for (String line : FileUtil.openTxtFile(cachePath, "utf-8")) {

			++readLines;

			// ;@:
			if (line.startsWith(COMMNET_CMD_HEAD)) {

				String line2 = line.substring(COMMNET_CMD_HEAD.length());
				NEXT: for (IRObject obj : parser.parse(line2)) {

					IRList cmd = RulpUtil.asList(obj);
					if (cmd.size() == 3) {
						switch (cmd.get(0).asString()) {
						case CMD_PRE:
							_set_pre(RulpUtil.asString(cmd.get(1)).asString(),
									RulpUtil.asString(cmd.get(2)).asString());
							continue NEXT;
						}
					}

					throw new RException("unknown cmd: " + cmd);
				}

			} else {

				List<IRObject> list = parser.parse(_loadLine(line));

				if (list.size() == 0) {
					continue;
				}

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
	}

	@Override
	public int save(IRReteNode node, List<IRList> stmtList) throws RException, IOException {

		final String cachePath = FileUtil.toValidPath(modelCachePath) + _nodeCacheName(node);

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

	@Override
	public boolean needSave() {
		return lineNeedUpdate;
	}

}
