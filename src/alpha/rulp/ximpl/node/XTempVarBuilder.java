package alpha.rulp.ximpl.node;

import java.util.ArrayList;

import alpha.rulp.lang.IRAtom;
import alpha.rulp.utils.RulpFactory;

public class XTempVarBuilder {

	private String tempNamePre = "?_tmp_";

	private int tempVarIndex = 0;

	private ArrayList<IRAtom> tempVarList = new ArrayList<>();

	public XTempVarBuilder() {
		super();
	}

	public XTempVarBuilder(String tempNamePre) {
		super();
		this.tempNamePre = tempNamePre;
	}

	public IRAtom next() {

		while (tempVarIndex >= tempVarList.size()) {
			tempVarList.add(RulpFactory.createAtom(tempNamePre + tempVarList.size()));
		}

		return tempVarList.get(tempVarIndex++);
	}

	public void reset() {
		this.tempVarIndex = 0;
	}
}
