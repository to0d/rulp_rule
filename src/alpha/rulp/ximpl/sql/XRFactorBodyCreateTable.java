package alpha.rulp.ximpl.sql;

import static alpha.rulp.lang.Constant.A_NIL;
import static alpha.rulp.ximpl.sql.Constant.F_ADD_SQL_COLUMN;
import static alpha.rulp.ximpl.sql.Constant.F_ADD_SQL_TABLE;
import static alpha.rulp.ximpl.sql.Constant.SQLCODE_SUCC;

import java.util.List;

import alpha.rulp.lang.IRArray;
import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RType;
import alpha.rulp.rule.IRModel;
import alpha.rulp.runtime.IRFactorBody;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;

public class XRFactorBodyCreateTable implements IRFactorBody {

//	static TableColumnConstraint[] getColumnConstraint(IRList tableList) throws RException {
//
//		int tableLen = tableList.size();
//		TableColumnConstraint[] columnConstraint = new TableColumnConstraint[tableLen];
//
//		int constraintCount = 0;
//
//		for (int i = 0; i < tableLen; ++i) {
//
//			IRObject columnObj = tableList.get(i);
//			switch (columnObj.getType()) {
//			case ATOM:
//				// atom should be: ?x or ?
//				if (!RulpUtil.isVarAtom(columnObj) && !ReteUtil.isAnyVar(columnObj)) {
//					throw new RException("Invalid atom column: " + columnObj);
//				}
//				break;
//
//			case LIST:
//
//			default:
//				throw new RException("unsupport column: " + columnObj);
//			}
//		}
//
//		if (constraintCount == 0) {
//			return null;
//		}
//
//		return columnConstraint;
//	}

	@Override
	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

		/********************************************/
		// Check parameters
		/********************************************/
		if (args.size() < 2) {
			throw new RException("Invalid parameters: " + args);
		}

		/**************************************************/
		// model object
		/**************************************************/
		IRModel model = RuleUtil.getDefaultModel(frame);
		if (model == null) {
			throw new RException("no model be specified");
		}

		/**************************************************/
		// table name
		/**************************************************/
		String tableName = RulpUtil.asAtom(args.get(2)).getName();
		if (tableName == null) {
			throw new RException("Invalid table name: " + args);
		}

		/**************************************************/
		// Column definition
		/**************************************************/
		IRArray columnDefArray = RulpUtil.asArray(args.get(3));
		List<RSQLColumn> columns = SQLUtil.toColumn(columnDefArray);

		for (RSQLColumn column : columns) {
			switch (column.columnType) {
			case FLOAT:
			case INT:
			case BOOL:
				break;
			default:
				throw new RException("not support column type: " + column.columnType);
			}
		}

		/**************************************************/
		// check table name exist
		/**************************************************/
		if (SQLUtil.hasTableNames(model, tableName)) {
			throw new RException("table name already exist: " + tableName);
		}

		/**************************************************/
		// Create table
		/**************************************************/
		RuleUtil.compute(model,
				String.format("(%s %s \"%s\" %d)", F_ADD_SQL_TABLE, model.getModelName(), tableName, columns.size()));

		/**************************************************/
		// Create columns
		/**************************************************/
		int columnIndex = 0;
		for (RSQLColumn column : columns) {
			RuleUtil.compute(model,
					String.format("(%s %s \"%s\" \"%s\" %d %s)", F_ADD_SQL_COLUMN, model.getModelName(), tableName,
							column.columnName, columnIndex++,
							column.columnType == null ? A_NIL : RType.toObject(column.columnType).getName()));
		}

		return RulpFactory.createInteger(SQLCODE_SUCC);
	}
}
