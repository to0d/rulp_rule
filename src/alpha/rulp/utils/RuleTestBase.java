package alpha.rulp.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.rule.IRRule;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.ximpl.entry.XREntryTable;
import alpha.rulp.ximpl.node.IRReteNodeCounter;
import alpha.rulp.ximpl.scope.IRScope;
import alpha.rulp.ximpl.scope.XRScope;

public class RuleTestBase extends RulpTestBase {

	static class TestEntry {

		private String _key;

		String className;

		String funcName;

		String testName;

		String value;

		public TestEntry(String className, String funcName, String testName, String value) {
			super();
			this.className = className;
			this.funcName = funcName;
			this.testName = testName;
			this.value = value;
		}

		public String getKey() {
			if (_key == null) {
				_key = _getTestKey(className, funcName, testName);
			}
			return _key;
		}

		@Override
		public String toString() {
			return getKey() + ":" + value;
		}
	}

	static boolean PRIME_MODE = true;

	static String ruleStatusFile = "result/rule_status.txt";

	static Map<String, TestEntry> testEntryMap = new HashMap<>();

	static ArrayList<TestEntry> testEntrys = null;

	static int UPDATE_COUNT = 0;

	static {
		new RuleFactory();
	}

	protected static void _clean_cache(String cachePath) {
		if (FileUtil.isExistDirectory(cachePath)) {
			for (File file : new File(cachePath).listFiles()) {
				if (file.isFile()) {
					file.delete();
				}
			}
		}
	}

	static String _getTestKey(String className, String funcName, String testName) {
		return className + ":" + funcName + ":" + testName;
	}

	protected static void _saveTest() {

		if (PRIME_MODE && UPDATE_COUNT > 0) {

			Collections.sort(testEntrys, (e1, e2) -> {

				int d = e1.className.compareTo(e2.className);

				if (d == 0) {
					d = e1.funcName.compareTo(e2.funcName);
				}

				if (d == 0) {
					d = e1.testName.compareTo(e2.testName);
				}

				return d;
			});

			ArrayList<String> lines = new ArrayList<>();
			for (TestEntry entry : testEntrys) {
				lines.add(entry.toString());
			}

			try {
				FileUtil.saveTxtFile(ruleStatusFile, lines, "utf-8");
			} catch (IOException e) {
				e.printStackTrace();
			}

			UPDATE_COUNT = 0;
		}
	}

	static void assertValue(String testName, String expect) throws IOException {

		openTestEntry();

		StackTraceElement stack[] = Thread.currentThread().getStackTrace();

		String className = stack[3].getClassName();
		String funcName = stack[3].getMethodName();

		String key = _getTestKey(className, funcName, testName);

		TestEntry testEntry = testEntryMap.get(key);
		if (testEntry == null) {

			if (!PRIME_MODE) {
				fail("test value not found: " + key);
			}

			testEntry = new TestEntry(className, funcName, testName, expect);

			testEntrys.add(testEntry);
			testEntryMap.put(key, testEntry);
			++UPDATE_COUNT;

			return;
		}

		if (!PRIME_MODE) {
			assertEquals(expect, testEntry.value);
		} else {
			if (!expect.equals(testEntry.value)) {
				testEntry.value = expect;
				++UPDATE_COUNT;
			}
		}

	}

	static void openTestEntry() throws IOException {

		if (testEntrys != null) {
			return;
		}

		testEntrys = new ArrayList<>();

		if (!FileUtil.isExistFile(ruleStatusFile)) {
			if (!PRIME_MODE) {
				throw new IOException(String.format("file not found:%s", ruleStatusFile));
			} else {
				return;
			}
		}

		int index = 0;

		NEXT_LINE: for (String line : FileUtil.openTxtFile(ruleStatusFile, "utf-8")) {

			String newLine = line;
			ArrayList<String> values = new ArrayList<>();

			for (int i = 0; i < 3; ++i) {

				int pos = newLine.indexOf(':');

				// invalid format
				if (pos == -1) {

					if (!PRIME_MODE) {
						throw new IOException(String.format("invalid line: line=%d, %s", index, line));
					} else {
						continue NEXT_LINE;
					}
				}

				values.add(newLine.substring(0, pos));
				newLine = newLine.substring(pos + 1);
			}

			values.add(newLine);

			testEntrys.add(new TestEntry(values.get(0), values.get(1), values.get(2), values.get(3)));

			++index;
		}

		for (TestEntry entry : testEntrys) {

			if (testEntryMap.containsKey(entry.getKey())) {
				throw new IOException("duplicated entry found: " + entry);
			}

			testEntryMap.put(entry.getKey(), entry);
		}
	}

	protected Boolean traceModel = null;

	protected void _clean_model_cache() {

		String cachePath = getCachePath();

		if (FileUtil.isExistDirectory(cachePath)) {
			for (File file : new File(cachePath).listFiles()) {
				if (file.isFile()) {
					file.delete();
				}
			}
		}
	}

	protected void _dumpEntryTable(String modelName, String expectFile) {

		try {

			String outline = OptimizeUtil.dumpAndCheckEntryTable(_model(modelName).getEntryTable());
			ArrayList<String> lines = new ArrayList<>();
			lines.add(outline);
			FileUtil.saveTxtFile(expectFile, lines, "utf-8");

		} catch (Exception e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}

	protected void _eCount(int index, String modelName) {

		try {

			IRInterpreter interpreter = _getInterpreter();
			IRModel model = RuleUtil.asModel(interpreter.getObject(modelName));
			assertNotNull(model);

			assertValue("" + index + "-eCount", OptimizeUtil.formatEntryTableCount(model.getEntryTable()));

		} catch (RException | IOException e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}

	protected void _enableTrace() {
		try {
			RuleUtil.setModelTrace(true);
			traceModel = true;
		} catch (RException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected IRInterpreter _getInterpreter() throws RException, IOException {

		IRInterpreter interpreter = super._getInterpreter();
		if (traceModel != null) {
			try {
				RuleUtil.setModelTrace(traceModel);
			} catch (RException e) {
				e.printStackTrace();
			}
		}

		return interpreter;
	}

	protected void _mCount(int index, String modelName) {

		try {

			IRInterpreter interpreter = _getInterpreter();

			IRModel model = RuleUtil.asModel(interpreter.getObject(modelName));
			assertNotNull(model);

			assertValue("" + index + "-mCount", OptimizeUtil.formatModelCount(model.getCounter()));

		} catch (RException | IOException e) {
			e.printStackTrace();
			fail(e.toString());
		}

	}

	protected IRModel _model(String modelName) {

		try {
			return RuleUtil.asModel(_getInterpreter().getObject(modelName));
		} catch (RException | IOException e) {
			e.printStackTrace();
			fail(e.toString());
			return null;
		}
	}

	protected void _mStatus(int index, String modelName) {

		try {

			IRInterpreter interpreter = _getInterpreter();
			IRModel model = RuleUtil.asModel(interpreter.getObject(modelName));
			assertNotNull(model);

			IRReteNodeCounter counter = RuleFactory.createReteCounter(model.getNodeGraph().getNodeMatrix());
			assertNotNull(counter);

			assertValue("" + index + "-mStatus", OptimizeUtil.formatNodeCount(counter));

		} catch (RException | IOException e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}

	protected void _nodeInfo(String modelName, String expectFile) {

		try {

			String outoputInfo = OptimizeUtil.printNodeInfo(_model(modelName));

			if (PRIME_MODE) {

				ArrayList<String> lines = new ArrayList<>();
				lines.add(outoputInfo);

				FileUtil.saveTxtFile(expectFile, lines, "utf-8");

			} else {

				String expectInfo = StringUtil.toOneLine(FileUtil.openTxtFile(expectFile, "utf-8")) + "\n\n";
				assertEquals(expectInfo.trim(), outoputInfo.trim());
			}

		} catch (Exception e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}

	protected void _oStatus(int index, String modelName) {

		try {

			IRInterpreter interpreter = _getInterpreter();

			IRModel model = RuleUtil.asModel(interpreter.getObject(modelName));
			assertNotNull(model);

			assertValue("" + index + "-oStatus", String.format("shared=%d", OptimizeUtil.getSharedNodeCount(model)));

		} catch (RException | IOException e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}

	protected void _refInfo(String modelName, String expectFile) {

		try {

			String outoputInfo = OptimizeUtil.printRefInfo(_model(modelName));

			if (PRIME_MODE) {

				ArrayList<String> lines = new ArrayList<>();
				lines.add(outoputInfo);

				FileUtil.saveTxtFile(expectFile, lines, "utf-8");

			} else {

				String expectInfo = StringUtil.toOneLine(FileUtil.openTxtFile(expectFile, "utf-8")) + "\n\n";
				assertEquals(expectInfo.trim(), outoputInfo.trim());
			}

		} catch (Exception e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}

	protected void _rStatus(int index, String modelName, String ruleName) {

		try {

			IRInterpreter interpreter = _getInterpreter();

			IRModel model = RuleUtil.asModel(interpreter.getObject(modelName));
			assertNotNull(model);

			IRRule rule = model.getNodeGraph().getRule(ruleName);
			assertNotNull(rule);

			IRReteNodeCounter reteCounter = RuleFactory.createReteCounter(rule.getNodeMatrix());
			assertNotNull(reteCounter);

			assertValue("" + index + "-rStatus" + "-" + ruleName, OptimizeUtil.formatNodeCount(reteCounter));

		} catch (RException | IOException e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}

	protected void _save_model_cache(String modelName) {
		String cachePath = getCachePath();
		_test("(set-model-cache-path " + modelName + " \"" + cachePath + "\")");
		_test("(save-model " + modelName + ")");
	}

	protected IRScope _scope(String scopeName) throws RException, IOException {

		List<IRObject> rst = _getInterpreter().compute(String.format("(%s::get-impl)", scopeName));
		assertEquals(1, rst.size());

		return RuleUtil.asScope(rst.get(0));
	}

	protected void _scopeInfo(String scopeName, String expectFile) {

		try {

			String outoputInfo = OptimizeUtil.printScopeInfo(_scope(scopeName));

			if (PRIME_MODE) {

				ArrayList<String> lines = new ArrayList<>();
				lines.add(outoputInfo);

				FileUtil.saveTxtFile(expectFile, lines, "utf-8");

			} else {

				String expectInfo = StringUtil.toOneLine(FileUtil.openTxtFile(expectFile, "utf-8")) + "\n\n";
				assertEquals(expectInfo.trim(), outoputInfo.trim());
			}

		} catch (Exception e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}

	protected void _scopeInfo(String scopeName) {
		_scopeInfo(scopeName, getCachePath() + ".scope.txt");
	}

	@Override
	protected void _setup() {
		super._setup();
		XRScope.TRACE = false;
		XREntryTable.TRACE = false;
		traceModel = null;
	}

	protected void _smCount(int index, String scopeName) {

		try {

			IRInterpreter interpreter = _getInterpreter();

			List<IRObject> rst = interpreter.compute(String.format("(%s::get-model)", scopeName));
			assertEquals(1, rst.size());

			IRModel model = RuleUtil.asModel(rst.get(0));

			assertValue("" + index + "-mOfSCopeCount", OptimizeUtil.formatModelCount(model.getCounter()));

		} catch (RException | IOException e) {
			e.printStackTrace();
			fail(e.toString());
		}

	}

	protected void _statsInfo(String modelName) {
		_statsInfo(modelName, getCachePath() + ".txt");
	}

	protected void _statsInfo(String modelName, String expectFile) {

		try {

			String outoputInfo = OptimizeUtil.printStatsInfo(_model(modelName));

			if (PRIME_MODE) {

				ArrayList<String> lines = new ArrayList<>();
				lines.add(outoputInfo);

				FileUtil.saveTxtFile(expectFile, lines, "utf-8");

			} else {

				String expectInfo = StringUtil.toOneLine(FileUtil.openTxtFile(expectFile, "utf-8")) + "\n\n";
				assertEquals(expectInfo.trim(), outoputInfo.trim());
			}

		} catch (Exception e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}

}