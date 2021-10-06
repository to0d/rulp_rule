package alpha.rulp.ximpl.scope;

import static alpha.rulp.lang.Constant.O_Nil;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.IRVar;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RType;
import alpha.rulp.rule.IRModel;
import alpha.rulp.utils.RulpFactory;

public class ScopeUtils {

	public static IRVar addVar(IRScope scope, String varName, IRList values) throws RException {

		if (varName == null) {
			throw new RException("null var name");
		}

		IRModel model = scope.getModel();

		/********************************************/
		// Check duplicated variable
		/********************************************/
		IRVar oldVar = model.getVar(varName);
		if (oldVar != null) {
			throw new RException("Duplicated var found: " + varName);
		}

		/********************************************/
		// Create variable
		/********************************************/
		IRVar var = RulpFactory.createVar(varName);
		model.getFrame().setEntry(varName, var);

		if (values != null) {

			if (values.size() == 0) {
				throw new RException("invalid values: " + values);
			}

			scope.setVarScope(var, values);
		}

		return var;
	}

	public static IRVar addVar(IRScope scope, String varName, RType varType, IRObject fromValue, IRObject toValue,
			IRObject stepValue) throws RException {

		if (varName == null) {
			throw new RException("null var name");
		}

		if (fromValue == O_Nil) {
			fromValue = null;
		}

		if (toValue == O_Nil) {
			toValue = null;
		}

		if (stepValue == O_Nil) {
			stepValue = null;
		}

		IRModel model = scope.getModel();

		/********************************************/
		// Check duplicated variable
		/********************************************/
		IRVar oldVar = model.getVar(varName);
		if (oldVar != null) {
			throw new RException("Duplicated var found: " + varName);
		}

		/********************************************/
		// Create variable
		/********************************************/
		IRVar var = RulpFactory.createVar(varName);
		model.getFrame().setEntry(varName, var);

		if (varType == null) {

			if (fromValue != null || toValue != null || stepValue != null) {
				throw new RException("var type not defined:" + varName);
			}

		} else {
			scope.setVarScope(var, varType, fromValue, toValue, stepValue);
		}

		return var;
	}

}
