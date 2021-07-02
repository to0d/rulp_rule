/* Copyright Prolog                                  */
/*                                                   */
/* RULP(Run a Lisp Processer) on Java                */
/* 													 */
/* Copyright (C) 2020 Todd (to0d@outlook.com)        */
/* This program comes with ABSOLUTELY NO WARRANTY;   */
/* This is free software, and you are welcome to     */
/* redistribute it under certain conditions.         */

package beta.rulp.utils;

import static alpha.rulp.lang.Constant.A_NIL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RType;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.runtime.IRParser;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;

public class FormatUtil {

	static class Loc {
		int deep;
		int num;
		int width;

		public Loc(int deep, int num, int width) {
			super();
			this.deep = deep;
			this.num = num;
			this.width = width;
		}
	}

	static final int MAX_LEN = 80;

	static Loc nil_loc = new Loc(0, 1, A_NIL.length());

	static Map<IRObject, Loc> objLocMap = new HashMap<>();

	static String spaceLines[] = new String[1024];

	private static String _getSpaceLine(int len) {

		if (len < 0) {
			len = 0;
		}

		String line = spaceLines[len];
		if (line == null) {

			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < len * 4; ++i) {
				sb.append(' ');
			}
			line = sb.toString();
			spaceLines[len] = line;
		}

		return line;
	}

	private static void _output(IRObject obj, List<String> outLines) throws RException {

		objLocMap.clear();

		_output(obj, outLines, 0);
	}

	private static void _output(IRObject obj, List<String> outLines, int level) throws RException {

		if (obj.getType() == RType.EXPR) {

			switch (((IRList) obj).get(0).asString()) {
			case "if":
				_output_if((IRList) obj, outLines, level);
				return;
			}
		}

		boolean outputOneLine = false;

		switch (obj.getType()) {
		case EXPR:
			outputOneLine = true;
			break;

		case LIST:

			if (ReteUtil.isReteStmt(obj)) {
				outputOneLine = true;
			} else {
				outputOneLine = false;
			}
			break;
		default:
			outputOneLine = true;
		}

		if (outputOneLine) {
			outLines.add(_getSpaceLine(level) + RulpUtil.toString(obj));
			return;
		}

		IRList list = (IRList) obj;

		String head = obj.getType() == RType.EXPR ? "(" : "'(";
		outLines.add(_getSpaceLine(level) + head);

		IRIterator<? extends IRObject> it = list.listIterator(0);
		while (it.hasNext()) {
			_output(it.next(), outLines, level + 1);
		}

		outLines.add(_getSpaceLine(level) + (obj.getType() == RType.EXPR ? ")" : " )"));
	}

	private static void _output_if(IRList expr, List<String> outLines, int level) throws RException {

		outLines.add(_getSpaceLine(level) + "(" + RulpUtil.toString(expr.get(0)));

		for (int i = 1; i < expr.size(); ++i) {
			_output(expr.get(i), outLines, level + 1);
		}

		outLines.add(_getSpaceLine(level) + ")");
	}

	public static List<String> format(List<String> lines) throws RException {

		ArrayList<String> outLines = new ArrayList<>();

		IRParser parser = RulpFactory.createParser();

		int size = lines.size();
		int index = 0;

		StringBuffer sb = new StringBuffer();
		int lastInCompletedIndex = -1;

		NEXT_LINE: for (; index < size; ++index) {

			String line = lines.get(index);

			if (line.trim().isEmpty() || line.trim().startsWith(";")) {
				if (lastInCompletedIndex == -1) {
					outLines.add(line);
					continue;
				} else {
					throw new RException(String.format("Not support internal comment yet: line=%d, %s", index, line));
				}
			}

			if (lastInCompletedIndex != -1) {
				sb.append("\n");
			}

			sb.append(line);

			List<IRObject> objs = new ArrayList<>();

			try {
				objs.addAll(parser.parse(sb.toString()));
			} catch (RException e) {
				// statements maybe not in-completed
				if (lastInCompletedIndex == -1) {
					lastInCompletedIndex = index;
				}

				continue NEXT_LINE;
			}

			for (IRObject obj : objs) {
				_output(obj, outLines);
			}

			lastInCompletedIndex = -1;
			sb.setLength(0);
		}

		if (lastInCompletedIndex != -1) {
			throw new RException(String.format("Incomplete line found: line=%d, %s", lastInCompletedIndex,
					lines.get(lastInCompletedIndex)));
		}

		return outLines;
	}
}
