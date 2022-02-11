package alpha.rulp.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RType;
import alpha.rulp.rule.IRModel;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.SystemUtil.OSType;
import alpha.rulp.ximpl.entry.XREntryTable;
import alpha.rulp.ximpl.search.IRASMachine;

public class RuleTestBase extends RulpTestBase {

	static boolean PRIME_MODE = true;

	static {
		RulpFactory.registerLoader(RRuleLoader.class);
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

	protected Boolean traceModel = null;

//	protected void _test(String input) {
//		System.out.println(input);
//		System.out.println(";;;");
//		System.out.println();
//	}
//
//	protected void _test(String input, String out) {
//
//		System.out.println(input);
//		System.out.println(";=>");
//		System.out.println();
//	}

	protected IRASMachine _asm(String asmName) throws RException, IOException {

		try {

			IRObject obj = _getInterpreter().getObject(asmName);
			if (obj.getType() == RType.VAR) {
				obj = RulpUtil.asVar(obj).getValue();
			}

			return RuleUtil.asASM(obj);

		} catch (RException | IOException e) {
			e.printStackTrace();
			fail(e.toString());
			return null;
		}

	}

	protected void _asmInfo(String asmName) {
		_asmInfo(asmName, getCachePath() + ".asm.txt");
	}

	protected void _asmInfo(String asmName, String expectFile) {

		try {

			String outoputInfo = StatsUtil.printASMInfo(_asm(asmName));

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

	protected void _dumpEntryTable(String modelName) {
		_dumpEntryTable(modelName, getCachePath() + ".dump_entry.txt");
	}

	protected void _dumpEntryTable(String modelName, String expectFile) {

		try {

			String outline = StatsUtil.dumpAndCheckEntryTable(_model(modelName).getEntryTable());
			ArrayList<String> lines = new ArrayList<>();
			lines.add(outline);
			FileUtil.saveTxtFile(expectFile, lines, "utf-8");

		} catch (Exception e) {
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

	protected IRModel _model(String modelName) {

		try {
			return RuleUtil.asModel(_getInterpreter().getObject(modelName));
		} catch (RException | IOException e) {
			e.printStackTrace();
			fail(e.toString());
			return null;
		}
	}

	protected void _nodeInfo(String modelName, String expectFile) {

		try {

			String outoputInfo = StatsUtil.printNodeInfo(_model(modelName));

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

	protected void _refInfo(String modelName) {
		_refInfo(modelName, getCachePath() + ".ref.txt");
	}

	protected void _refInfo(String modelName, String expectFile) {

		try {

			String outoputInfo = StatsUtil.printRefInfo(_model(modelName));

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

	protected void _save_model_cache(String modelName) {

		String cachePath = getCachePath();
		if (SystemUtil.getOSType() == OSType.Win) {
			cachePath = StringUtil.addEscape(cachePath);
		}

		_test("(set-model-cache-path " + modelName + " \"" + cachePath + "\")");
		_test("(save-model " + modelName + ")");
	}

	@Override
	protected void _setup() {
		super._setup();
		XREntryTable.TRACE = false;
		traceModel = null;
		RuleUtil.reset();
	}

	protected void _statsInfo(String modelName) {
		_statsInfo(modelName, getCachePath() + ".stats.txt");
	}

	protected void _statsInfo(String modelName, String expectFile) {

		try {

			String outoputInfo = StatsUtil.printStatsInfo(_model(modelName));

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