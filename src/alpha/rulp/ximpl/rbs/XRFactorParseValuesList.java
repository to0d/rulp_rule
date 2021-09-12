package alpha.rulp.ximpl.rbs;

import java.util.ArrayList;

import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.runtime.IRFactor;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.factor.AbsRFactorAdapter;

public class XRFactorParseValuesList extends AbsRFactorAdapter implements IRFactor {

	public XRFactorParseValuesList(String factorName) {
		super(factorName);
	}

	static boolean isComma(IRObject obj) {
		return obj.asString().equals(",");
	}

	@Override
	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

		if (args.size() != 2) {
			throw new RException("Invalid parameters: " + args);
		}

		/**************************************************/
		// (a, b, c)
		/**************************************************/
		IRExpr valueExpr = RulpUtil.asExpression(args.get(1));
		if (valueExpr.size() % 2 != 1) {
			throw new RException("Invalid values: " + valueExpr);
		}

		ArrayList<IRObject> values = new ArrayList<>();
		IRIterator<? extends IRObject> it = valueExpr.iterator();
		while (it.hasNext()) {

			IRObject obj = it.next();
			if (isComma(obj)) {
				throw new RException(String.format("no value specified before <%s> in values: %s", obj, valueExpr));
			}

			values.add(obj);

			if (it.hasNext()) {
				obj = it.next();
				if (!isComma(obj)) {
					throw new RException(String.format("Not comma<%s> is specified in values: %s", obj, valueExpr));
				}
			}
		}

		return RulpFactory.createList(values);
	}
}
