package alpha.rulp.ximpl.sql;

import static alpha.rulp.ximpl.sql.Constant.*;
import java.util.ArrayList;
import java.util.List;

import alpha.rulp.lang.IRArray;
import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.runtime.IRFactorBody;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.node.IRNamedNode;

public class XRFactorBodyCreateTable implements IRFactorBody {

//
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
		// Check model object
		/**************************************************/
		IRModel model = RuleUtil.getDefaultModel(frame);
		if (model == null) {
			throw new RException("no model be specified");
		}

		/**************************************************/
		// Check table name
		/**************************************************/
		String tableName = RulpUtil.asAtom(args.get(2)).getName();
		if (tableName == null) {
			throw new RException("Invalid table name: " + args);
		}

		IRArray columnDefArray = RulpUtil.asArray(args.get(3));

		IRSQLSchema schema = SQLUtil.getSchema(model);
		if (schema.getAllTableNames().contains(tableName)) {
			throw new RException("table name already exist: " + tableName);
		}

		List<RSQLColumn> columns = SQLUtil.toColumn(columnDefArray);
		ArrayList<String> columnNames = new ArrayList<>();
		for (RSQLColumn column : columns) {
			columnNames.add(column.columnName);
		}

		/**************************************************/
		// Check named list
		/**************************************************/
		IRList namedList = RulpFactory.createNamedList(RulpFactory.createListOfString(columnNames).iterator(),
				tableName);
		int anyIndex = ReteUtil.indexOfVarArgStmt(namedList);
		if (anyIndex != -1) {
			throw new RException(String.format("Can't create var arg node: %s", namedList));
		}

		/**************************************************/
		// Find node
		/**************************************************/
		IRNamedNode node = ReteUtil.findNameNode(model.getNodeGraph(), namedList);
		if (node == null) {

			// Create node
			node = model.getNodeGraph().getNamedNode(namedList.getNamedName(), ReteUtil.getFilerEntryLength(namedList));
		}
		
		

		return RulpFactory.createInteger(SQLCODE_SUCC);

//		TableColumnConstraint[] columnConstraint = getColumnConstraint(tableList);
//		int tableLen = tableList.size();
//
//		IRNodeGraph nodeGraph = model.getNodeGraph();
//
//		IRNamedNode tableNode = nodeGraph.findNamedNode(tableName);
//		if (tableNode != null) {
//
//			if (tableNode.getReteType() != RReteType.NAME0) {
//				throw new RException("invalid named node: " + tableNode);
//			}
//
//			if (tableNode.getEntryLength() != tableLen) {
//				throw new RException(String.format("Entry Length not match: expect=%d, actual=%d, node=%s", tableLen,
//						tableNode.getEntryLength(), tableNode));
//			}
//
//			if (columnConstraint != null && tableNode.getConstraintCount() != 0) {
//				throw new RException("node has constraint already: " + tableNode);
//			}
//
//		} else {
//
//			tableNode = nodeGraph.getNamedNode(tableName, tableLen);
//		}
//
//		if (columnConstraint != null) {
//
//		}

	}
}
