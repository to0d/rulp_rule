package alpha.rulp.ximpl.cache;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import alpha.common.utils.FileUtil;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.runtime.IRListener1;
import alpha.rulp.runtime.IRParser;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;

public class XRStmtLoader {

	static final String PRE_END = "##";

	static final String CMD_PRE = "pre";

	static final String COMMNET_CMD_HEAD = ";@:";

	protected boolean lineNeedUpdate = false;

	protected Map<String, String> preMap = new HashMap<>();

	protected int readLines = 0;

	private String _loadLine(String line) {

		// no pre
		if (preMap.isEmpty()) {
			return line;
		}

		for (Entry<String, String> e : preMap.entrySet()) {

			String pre = e.getKey();
			String value = e.getValue();

			if (!lineNeedUpdate && line.indexOf(value) != -1) {
//				System.out.println(String.format("update lineNeedUpdate: %s", line));
				lineNeedUpdate = true;
			}

			while (line.indexOf(pre) != -1) {
				line = line.replace(pre, value);
			}
		}

		return line;
	}

	private void _set_pre(String pre, String value) throws RException {

//		System.out.println(String.format("_set_pre: %s, %s", pre, value));

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

	public void loadStmt(IRListener1<IRList> stmtListener, String stmtName, int stmtLen, String cachePath,
			IRInterpreter interpreter) throws RException, IOException {

		IRParser parser = interpreter.getParser();

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
}
