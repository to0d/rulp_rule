package alpha.rulp.utils;

import static alpha.rulp.rule.Constant.A_TABLE;
import static alpha.rulp.rule.Constant.A_VIEW;
import static alpha.rulp.rule.Constant.F_CREATE;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.RException;
import alpha.rulp.ximpl.table.XRFactorBodyCreateTable;
import alpha.rulp.ximpl.table.XRFactorBodyCreateView;

public class SQLUtil {

	public static void init(IRFrame frame) throws RException {

		RulpUtil.addFactor(frame, F_CREATE, A_TABLE, new XRFactorBodyCreateTable());
		RulpUtil.addFactor(frame, F_CREATE, A_VIEW, new XRFactorBodyCreateView());

	}
}
