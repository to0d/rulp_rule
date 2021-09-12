package alpha.rulp.ximpl.rbs;

import static alpha.rulp.ximpl.rbs.Constant.F_PARSE_COLUMN_DEF_LIST;
import static alpha.rulp.ximpl.rbs.Constant.F_PARSE_VALUES_LIST;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import alpha.rulp.lang.IRArray;
import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RType;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.LoadUtil;
import alpha.rulp.utils.RulpUtil;

public class RBSUtil {

	public static void init(IRInterpreter interpreter, IRFrame frame) throws RException, IOException {

		LoadUtil.loadRulpFromJar(interpreter, frame, "alpha/resource/rbs.rulp", "utf-8");

		RulpUtil.addFrameObject(frame, new XRFactorParseColumnDefList(F_PARSE_COLUMN_DEF_LIST));
		RulpUtil.addFrameObject(frame, new XRFactorParseValuesList(F_PARSE_VALUES_LIST));
	}

	public static String printRBSTable(IRArray table) throws RException {

		if (table.getDimension() != 2) {
			throw new RException("invalid array dimension: " + table.getDimension());
		}

		StringBuilder sb = new StringBuilder();

		int rowCount = table.size(0);
		int columnCount = table.size(1);

//		sb.append("table name  : " + table.getTableName() + "\n");
//		sb.append("column count: " + columnCount + "\n");
//		sb.append("row count   : " + rowCount + "\n");
//		sb.append("\n");
//
//		if (columnCount > 0) {
//
//			sb.append("row  | ");
//
//			/*******************************************************************************/
//			// Get max length of every column
//			/*******************************************************************************/
//			int maxLength[] = new int[columnCount];
//			{
//				for (int columnIndex = 0; columnIndex < columnCount; ++columnIndex) {
//					maxLength[columnIndex] = -1;
//				}
//
//				for (int columnIndex = 0; columnIndex < columnCount; ++columnIndex) {
//					String columnName = table.getColumnMetas().get(columnIndex).getColumnName();
//					if (columnName == null) {
//						columnName = "-";
//					}
//
//					int length = columnName.length();
//					if (length > maxLength[columnIndex]) {
//						maxLength[columnIndex] = length;
//					}
//				}
//
//				for (int rowIndex = 0; rowIndex < rowCount; ++rowIndex) {
//
//					for (int columnIndex = 0; columnIndex < columnCount; ++columnIndex) {
//
//						String value = "-";
//						IRObject val = table.getValue(rowIndex, columnIndex);
//						if (val != null && val != O_Nil) {
//							value = RulpUtility.toString(table.getValue(rowIndex, columnIndex));
//						}
//
//						int length = value.length();
//						if (length > maxLength[columnIndex]) {
//							maxLength[columnIndex] = length;
//						}
//					}
//
//				}
//			}
//
//			String formatStr[] = new String[columnCount];
//			for (int columnIndex = 0; columnIndex < columnCount; ++columnIndex) {
//				formatStr[columnIndex] = "%-" + maxLength[columnIndex] + "s";
//			}
//
//			/*******************************************************************************/
//			// Output column name
//			/*******************************************************************************/
//			for (int columnIndex = 0; columnIndex < columnCount; ++columnIndex) {
//
//				String columnName = table.getColumnMetas().get(columnIndex).getColumnName();
//				if (columnName == null) {
//					columnName = "-";
//				}
//
//				sb.append(String.format(formatStr[columnIndex], columnName) + " | ");
//			}
//			sb.append("\n");
//
//			/*******************************************************************************/
//			// Output row line
//			/*******************************************************************************/
//			for (int rowIndex = 0; rowIndex < rowCount; ++rowIndex) {
//
//				sb.append(String.format("%04d | ", rowIndex));
//
//				for (int columnIndex = 0; columnIndex < columnCount; ++columnIndex) {
//
//					String value = "-";
//					IRObject val = table.getValue(rowIndex, columnIndex);
//					if (val != null && val != O_Nil) {
//						value = RulpUtility.toString(table.getValue(rowIndex, columnIndex));
//					}
//
//					sb.append(String.format(formatStr[columnIndex], value) + " | ");
//				}
//
//				sb.append("\n");
//			}
//		}

		return sb.toString();
	}

	public static List<RSQLColumn> toColumn(IRArray columnDefArray) throws RException {

		List<RSQLColumn> columns = new ArrayList<>();
		Set<String> columnNames = new HashSet<>();

		if (columnDefArray.getDimension() == 1) {

			int size = columnDefArray.size();
			for (int i = 0; i < size; ++i) {

				RSQLColumn column = toColumn(columnDefArray.get(i));
				if (columnNames.contains(column.columnName)) {
					throw new RException("duplicated column: " + column.columnName);
				}

				columns.add(column);
				columnNames.add(column.columnName);
			}

			return columns;
		}

		throw new RException("not support column defination: " + columnDefArray);
	}

	public static RSQLColumn toColumn(IRObject obj) throws RException {

		String columnName = null;
		RType columnType = null;

		switch (obj.getType()) {
		case LIST:

			IRList columnArgs = RulpUtil.asList(obj);
			int columnSize = columnArgs.size();
			int columnIndex = 0;
			if (columnSize == 0) {
				throw new RException("invalid column obj: " + obj);
			}

			// Column name
			columnName = RulpUtil.asAtom(columnArgs.get(columnIndex++)).getName();

			// Column type
			if (columnIndex < columnSize) {

				IRObject typeObj = columnArgs.get(columnIndex);
				if (typeObj.getType() == RType.ATOM) {
					columnType = RType.toType(RulpUtil.asAtom(typeObj).getName());
					++columnIndex;
				} else {
					throw new RException("invalid column obj: " + obj);
				}
			}

			if (columnIndex != columnSize) {
				throw new RException("invalid column obj: " + obj);
			}

			return new RSQLColumn(columnName, columnType);

		default:
			throw new RException("invalid column obj: " + obj);
		}

	}
}
