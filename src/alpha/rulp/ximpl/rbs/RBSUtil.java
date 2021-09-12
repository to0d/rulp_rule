package alpha.rulp.ximpl.rbs;

import static alpha.rulp.ximpl.rbs.Constant.F_PARSE_COLUMN_DEF_LIST;
import static alpha.rulp.ximpl.rbs.Constant.F_HAS_SQL_TABLE_NAME;
import static alpha.rulp.ximpl.rbs.Constant.RBS_NS;
import static alpha.rulp.ximpl.rbs.Constant.STR_SCHEMA_NAME;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import alpha.rulp.lang.IRArray;
import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRFrameEntry;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RType;
import alpha.rulp.rule.IRModel;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.runtime.IRNameSpace;
import alpha.rulp.utils.LoadUtil;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpUtil;

public class RBSUtil {

	public static RSQLSchema asSchema(IRObject obj) throws RException {

		if (!(obj instanceof RSQLSchema)) {
			throw new RException("Can't convert to schema: " + obj);
		}

		return (RSQLSchema) obj;
	}

	public static RSQLSchema getSchema(IRModel model) throws RException {

		IRFrame modelFrame = model.getModelFrame();

		IRFrameEntry schemaFrameEntry = modelFrame.getEntry(STR_SCHEMA_NAME);
		if (schemaFrameEntry != null && schemaFrameEntry.getFrame() != modelFrame) {
			return RBSUtil.asSchema(schemaFrameEntry.getObject());
		}

		RSQLSchema schema = new RSQLSchema(model);
		modelFrame.setEntry(STR_SCHEMA_NAME, schema);
		return schema;
	}

	public static boolean hasTableNames(IRModel model, String tableName) throws RException {

		List<IRObject> rst = RuleUtil.compute(model,
				String.format("(%s %s \"%s\")", F_HAS_SQL_TABLE_NAME, model.getModelName(), tableName));

		return rst.size() == 1 && RulpUtil.asBoolean(rst.get(0)).asBoolean();
	}

	public static void init(IRInterpreter interpreter, IRFrame frame) throws RException, IOException {

		LoadUtil.loadRulpFromJar(interpreter, frame, "alpha/resource/rbs.rulp", "utf-8");

		RulpUtil.addFrameObject(frame, new XRFactorParseColumnDefList(F_PARSE_COLUMN_DEF_LIST));
	}

	public static List<RSQLColumn> toColumn(IRArray columnDefArray) throws RException {

		List<RSQLColumn> columns = new ArrayList<>();
		Set<String> columnNames = new HashSet<>();

		if (columnDefArray.dimension() == 1) {

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
