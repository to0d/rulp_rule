package alpha.rulp.ximpl.rbs;

import static alpha.rulp.lang.Constant.O_Nil;

import java.util.ArrayList;
import java.util.List;

import alpha.rulp.lang.IRArray;
import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RType;
import alpha.rulp.runtime.IRFactor;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.factor.AbsAtomFactorAdapter;

public class XRFactorParseColumnDefList extends AbsAtomFactorAdapter implements IRFactor {

	public XRFactorParseColumnDefList(String factorName) {
		super(factorName);
	}

	@Override
	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

		if (args.size() != 2) {
			throw new RException("Invalid parameters: " + args);
		}

		/**************************************************/
		// Column definition
		/**************************************************/
		IRArray columnDefArray = RulpUtil.asArray(interpreter.compute(frame, args.get(1)));
		List<RSQLColumn> columns = RBSUtil.toColumn(columnDefArray);

		ArrayList<IRObject> columDefList = new ArrayList<>();
		for (RSQLColumn column : columns) {
			switch (column.columnType) {
			case FLOAT:
			case INT:
			case BOOL:
				columDefList.add(RulpFactory.createList(RulpFactory.createString(column.columnName),
						column.columnType == null ? O_Nil : RType.toObject(column.columnType)));
				break;
			default:
				throw new RException("not support column type: " + column.columnType);
			}
		}

		return RulpFactory.createList(columDefList);
	}
}
