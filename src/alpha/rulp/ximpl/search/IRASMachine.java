package alpha.rulp.ximpl.search;

import java.util.List;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRInstance;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.rule.IRModel;
import alpha.rulp.rule.IRRunnable;

public interface IRASMachine extends IRInstance, IRRunnable {

	public List<String> getAllResultVarNames();

	public List<String> getAllSearchNodeNames();

	public List<String> getAllSearchVarNames();

	public IRModel getModel();

	public IRFrame getResultFrame();

	public IRList getRstList();

	public ISScope<List<List<IRObject>>> getScope();

	public List<? extends IRSEntry> listSEntry();

}
