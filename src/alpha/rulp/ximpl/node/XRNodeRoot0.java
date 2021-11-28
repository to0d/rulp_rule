//package alpha.rulp.ximpl.node;
//
//public class XRNodeRoot0 extends XRNodeRete0 implements IRRootNode {
//
////	protected Map<String, IRReteEntry> stmtMap = new HashMap<>();
////
////	@Override
////	public boolean addReteEntry(IRReteEntry entry) throws RException {
////
////		if (entry.size() != this.getEntryLength() || entry.isDeleted()) {
////			throw new RException("invalid entry: " + entry);
////		}
////
////		if (!entryQueue.addEntry(entry)) {
////			return false;
////		}
////
////		stmtMap.put(ReteUtil.uniqName(entry), entry);
////		return true;
////	}
////
////	@Override
////	public int doGC() {
////
////		Iterator<Entry<String, IRReteEntry>> it = stmtMap.entrySet().iterator();
////		while (it.hasNext()) {
////
////			Entry<String, IRReteEntry> e = it.next();
////
////			IRReteEntry entry = e.getValue();
////			if (entry == null || entry.getStatus() == null) {
////				it.remove();
////				continue;
////			}
////		}
////
////		return super.doGC();
////	}
//
////	@Override
////	public IRReteEntry getStmt(String uniqName) throws RException {
////		return stmtMap.get(uniqName);
////	}
//
//}
