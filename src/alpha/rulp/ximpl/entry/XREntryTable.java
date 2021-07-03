package alpha.rulp.ximpl.entry;

import static alpha.rulp.rule.RReteStatus.REMOVED;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RType;
import alpha.rulp.rule.IRRListener1;
import alpha.rulp.rule.RReteStatus;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.utils.FixIndexArray;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.utils.XRRListener1Adapter;
import alpha.rulp.ximpl.lang.AbsAtomObject;
import alpha.rulp.ximpl.node.IRReteNode;

public class XREntryTable implements IREntryTable {

	static class EntryArray<T> {

	}

	static class ETA_Entry {

		public Object obj;

		public ETA_Type type;

		public String toString() {
			return String.format("ETA: type=%s, obj=%s", type, obj);
		}

	}

	enum ETA_Type {
		REMOVE_ENTRY, REMOVE_REF
	}

	static class ETAQueue {

		private int actionSize;

		private ETA_Entry curEntry = new ETA_Entry();

		private int expendCount;

		private int lastPos;

		private int maxQueueSize;

		private Object[] objectQueue;

		private int queueCapacity;

		private int queueSize;

		private int startPos;

		private ETA_Type[] typeQueue;

		public ETAQueue(int queueCapacity) {

			super();

			this.startPos = 0;
			this.lastPos = 0;
			this.queueSize = 0;
			this.maxQueueSize = 0;
			this.actionSize = 0;
			this.expendCount = 0;
			this.queueCapacity = queueCapacity;
			this.typeQueue = new ETA_Type[queueCapacity];
			this.objectQueue = new Object[queueCapacity];
		}

		private void _ensureCapacity(int minCapacity) throws RException {

			if (queueCapacity >= minCapacity) {
				return;
			}

			int newCapacity = queueCapacity * 2;
			while (newCapacity < minCapacity) {
				newCapacity *= 2;
			}

			if (newCapacity >= FIX_ETA_MAX_SIZE) {
				throw new RException("ETA queue out of space");
			}

			ETA_Type[] newTypeQueue = new ETA_Type[newCapacity];
			Object[] newObjectQueue = new Object[newCapacity];

			int newLastPos = 0;

			if (startPos < lastPos) {

				for (int i = startPos; i < lastPos; ++i, ++newLastPos) {
					newTypeQueue[newLastPos] = typeQueue[i];
					newObjectQueue[newLastPos] = objectQueue[i];
				}

			} else {

				for (int i = startPos; i < queueCapacity; ++i, ++newLastPos) {
					newTypeQueue[newLastPos] = typeQueue[i];
					newObjectQueue[newLastPos] = objectQueue[i];
				}

				for (int i = 0; i < lastPos; ++i, ++newLastPos) {
					newTypeQueue[newLastPos] = typeQueue[i];
					newObjectQueue[newLastPos] = objectQueue[i];
				}
			}

			this.queueCapacity = newCapacity;
			this.typeQueue = newTypeQueue;
			this.objectQueue = newObjectQueue;
			this.startPos = 0;
			this.lastPos = newLastPos;
			this.expendCount++;
		}

		public int getActionSize() {
			return actionSize;
		}

		public int getExpendCount() {
			return expendCount;
		}

		public int getMaxQueueSize() {
			return maxQueueSize;
		}

		public int getQueueCapacity() {
			return queueCapacity;
		}

		public boolean hasNext() {
			return queueSize > 0;
		}

		public ETA_Entry pop() {

			curEntry.type = typeQueue[startPos];
			curEntry.obj = objectQueue[startPos];
			objectQueue[startPos] = null;

			if (++startPos == queueCapacity) {
				startPos = 0;
			}

			--queueSize;
			return curEntry;
		}

		public void push(ETA_Type type, Object obj) throws RException {

			if (XREntryTable.TRACE) {
				System.out.println(String.format("    eta-push: type=%s, obj=%s", type, obj));
			}

			_ensureCapacity(this.queueSize + 1);

			typeQueue[lastPos] = type;
			objectQueue[lastPos] = obj;

			if (++lastPos == queueCapacity) {
				lastPos = 0;
			}

			this.queueSize++;
			if (this.queueSize > this.maxQueueSize) {
				this.maxQueueSize = this.queueSize;
			}

			this.actionSize++;
		}
	}

	static class XRReference implements IRReference {

		public XRReteEntry childEntry;

		public int nodeId;

		public int parentEntryCount;

		public final XRReteEntry[] parentEntrys;

		public int refId;

		public XRReference(int nodeId, XRReteEntry childEntry, int parentEntryCount, XRReteEntry[] parentEntrys) {
			super();
			this.nodeId = nodeId;
			this.childEntry = childEntry;
			this.parentEntryCount = parentEntryCount;
			this.parentEntrys = parentEntrys;
		}

		@Override
		public int getNodeId() {
			return nodeId;
		}

		@Override
		public int getParentEntryCount() {
			return parentEntryCount;
		}

		@Override
		public int getParentEntryID(int index) {

			XRReteEntry parent = parentEntrys[index];
			if (parent == null) {
				return 0;
			}

			return parent.getEntryId();
		}
	}

	static class XRReteEntry extends AbsAtomObject implements IRReteEntry {

		private List<XRReference> childList = null;

		private final IRObject elements[];

		private int entryId = 0;

		private XRRListener1Adapter<IRReteEntry> entryRemovedlistener = null;

		private String namedName;

		private List<XRReference> referenceList = null;

		private RReteStatus status = RReteStatus.DEFINED;

		public XRReteEntry(String namedName, IRObject[] elements) {
			this.namedName = namedName;
			this.elements = elements;
		}

		public void addChild(XRReference ref) {

			if (childList == null) {
				childList = new LinkedList<>();
			}

			childList.add(ref);
		}

		@Override
		public void addEntryRemovedListener(IRRListener1<IRReteEntry> listener) {

			if (entryRemovedlistener == null) {
				entryRemovedlistener = new XRRListener1Adapter<>();
			}

			entryRemovedlistener.addListener(listener);
		}

		public void addReference(XRReference ref) {

			if (referenceList == null) {
				referenceList = new LinkedList<>();
			}

			referenceList.add(ref);
		}

		@Override
		public String asString() {

			try {
				return RulpUtil.toString(this);
			} catch (RException e) {
				e.printStackTrace();
				return "";
			}
		}

		@Override
		public IRObject get(int index) {
			return index < elements.length ? elements[index] : null;
		}

		@Override
		public int getChildCount() {
			return childList == null ? 0 : childList.size();
		}

		@Override
		public int getEntryId() {
			return entryId;
		}

		@Override
		public String getNamedName() {
			return namedName;
		}

		@Override
		public int getReferenceCount() {
			return referenceList == null ? 0 : referenceList.size();
		}

		@Override
		public Iterator<? extends IRReference> getReferenceIterator() {
			return referenceList == null ? Collections.emptyIterator() : referenceList.iterator();
		}

		@Override
		public RReteStatus getStatus() {
			return status;
		}

		@Override
		public RType getType() {
			return RType.LIST;
		}

		@Override
		public boolean isDroped() {
			return this.status == REMOVED;
		}

		@Override
		public boolean isEmpty() {
			return false;
		}

		@Override
		public IRIterator<? extends IRObject> iterator() {
			return listIterator(0);
		}

		@Override
		public IRIterator<IRObject> listIterator(int fromIndex) {

			return new IRIterator<IRObject>() {

				int index = fromIndex;

				@Override
				public boolean hasNext() {
					return index < size();
				}

				@Override
				public IRObject next() {
					return get(index++);
				}
			};
		}

		@Override
		public void removeEntryRemovedListener(IRRListener1<IRReteEntry> listener) {

			if (entryRemovedlistener == null) {
				return;
			}

			entryRemovedlistener.removeListener(listener);
		}

		public void setEntryId(int entryId) {
			this.entryId = entryId;
		}

		public void setStatus(RReteStatus status) {
			this.status = status;
		}

		@Override
		public int size() {
			return elements.length;
		}

		@Override
		public String toString() {
			return asString();
		}
	}

	static int FIX_ETA_DEF_SIZE = 256;

	static int FIX_ETA_MAX_SIZE = 102400;

	public static boolean REUSE_ENTRY_ID = false;

	public static boolean TRACE = false;

	static boolean _isValidEntry(XRReteEntry entry) {
		return entry != null && entry.status != REMOVED;
	}

	static boolean _isValidReference(XRReference ref) {
		return ref != null && ref.parentEntryCount != -1;
	}

	protected ArrayList<XRReteEntry> entryArray = new ArrayList<>();

	protected int entryCount = 0;

	protected ETAQueue etaQueue = new ETAQueue(FIX_ETA_DEF_SIZE);

	protected LinkedList<Integer> freeEntryIdList = new LinkedList<>();

	protected int maxActionSize = 0;

	protected void _addReference(XRReteEntry entry, IRReteNode node, XRReteEntry[] parentEntrys) throws RException {

		int parentEntryCount = parentEntrys == null ? 0 : parentEntrys.length;

		XRReference ref = new XRReference(node.getNodeId(), entry, parentEntryCount, parentEntrys);

		// Add links to child
		entry.addReference(ref);

		// Add links to parent
		if (parentEntrys != null) {
			for (XRReteEntry parent : parentEntrys) {
				parent.addChild(ref);
			}
		}
	}

	protected int _indexOf(int[] array, int id) throws RException {

		if (array != null) {
			for (int i = 0; i < array.length; ++i) {
				if (array[i] == id) {
					return i;
				}
			}
		}

		return -1;
	}

	protected void _processEtaQueue() throws RException {

		int lastAcionSize = etaQueue.getActionSize();

		while (etaQueue.hasNext()) {

			ETA_Entry etaEntry = etaQueue.pop();

			switch (etaEntry.type) {
			case REMOVE_ENTRY:
				_processRemoveEntry((XRReteEntry) etaEntry.obj);
				break;

			case REMOVE_REF:
				_processRemoveRef((XRReference) etaEntry.obj);
				break;

			default:
				throw new RException("Unkown action: " + etaEntry.type);
			}
		}

		int actionSize = etaQueue.getActionSize() - lastAcionSize;
		if (actionSize > maxActionSize) {
			maxActionSize = actionSize;
		}
	}

	protected void _processRemoveEntry(XRReteEntry entry) throws RException {

		if (!_isValidEntry(entry)) {
			return;
		}

		int entryId = entry.getEntryId();

		if (XREntryTable.TRACE) {
			System.out.println("    removeEntry: id=" + entryId + ", entry=" + entry);
		}

		if (entry.childList != null) {

			for (XRReference ref : entry.childList) {

				int parentCount = ref.getParentEntryCount();
				for (int i = 0; i < parentCount; ++i) {

					// Break the link between reference and parent entry
					if (ref.getParentEntryID(i) == entryId) {
						ref.parentEntrys[i] = null;
					}
				}

				_processRemoveRef(ref);
			}

			entry.childList = null;

		}

		if (entry.referenceList != null) {

			for (XRReference ref : entry.referenceList) {
				// Break the link between child entry and reference
				ref.childEntry = null;
				_processRemoveRef(ref);
			}
		}

		if (entry.entryRemovedlistener != null) {
			entry.entryRemovedlistener.doAction(entry);
			entry.entryRemovedlistener = null;
		}

		entry.status = REMOVED;

		this.entryArray.set(entryId - 1, null);
		--entryCount;

		if (REUSE_ENTRY_ID) {
			this.freeEntryIdList.addLast(entryId);
		}
	}

	protected void _processRemoveRef(XRReference ref) throws RException {

		if (!_isValidReference(ref)) {
			return;
		}

		if (ref.childEntry != null) {

			Iterator<XRReference> it = ref.childEntry.referenceList.iterator();

			while (it.hasNext()) {
				if (it.next() == ref) {
					it.remove();
				}
			}

			if (ref.childEntry.referenceList.isEmpty()) {
				ref.childEntry.referenceList = null;
				_pushRemoveEntry(ref.childEntry);
			}

			ref.childEntry = null;
		}

		int parentCount = ref.getParentEntryCount();
		for (int i = 0; i < parentCount; ++i) {

			XRReteEntry parentEntry = ref.parentEntrys[i];
			if (parentEntry != null) {

				Iterator<XRReference> it = parentEntry.childList.iterator();
				while (it.hasNext()) {
					if (it.next() == ref) {
						it.remove();
					}
				}

				if (parentEntry.childList.isEmpty()) {
					parentEntry.childList = null;
				}

				// Break the link between reference and parent entry
				ref.parentEntrys[i] = null;
			}
		}

		// mark it as invalid
		ref.parentEntryCount = -1;
	}

	protected void _pushRemoveEntry(XRReteEntry entry) throws RException {
		etaQueue.push(ETA_Type.REMOVE_ENTRY, entry);
	}

	protected void _pushRemoveRef(XRReference ref) throws RException {

		if (!_isValidReference(ref)) {
			throw new RException("Invalid Ref: " + ref);
		}

		etaQueue.push(ETA_Type.REMOVE_REF, ref);
	}

	protected void _setEntryStatus(int entryId, XRReteEntry entry, RReteStatus status) throws RException {

		if (entry == null) {
			entry = getEntry(entryId);
			if (entry == null) {
				return;
			}
		}

		entry.setStatus(status);
	}

	protected XRReteEntry _toEntry(IRReteEntry entry) throws RException {

		if (!_isValidEntry((XRReteEntry) entry)) {
			throw new RException("Invalid entry: " + entry);
		}

		XRReteEntry oEntry = getEntry(entry.getEntryId());
		if (entry != oEntry) {
			throw new RException(
					String.format("unmatch entry: id=%d, input=%s, actual=%s", entry.getEntryId(), entry, oEntry));
		}

		return oEntry;
	}

	protected XRReteEntry[] _toEntry(IRReteEntry[] entrys) throws RException {

		if (entrys == null || entrys.length == 0) {
			return null;
		}

		/******************************************/
		// Scan entry & check valid
		/******************************************/
		int size = 0;

		for (IRReteEntry entry : entrys) {

			if (entry == null) {
				continue;
			}

			if (!_isValidEntry((XRReteEntry) entry)) {
				throw new RException("Invalid entry: " + entry);
			}

			XRReteEntry oEntry = getEntry(entry.getEntryId());
			if (entry != oEntry) {
				throw new RException(
						String.format("unmatch entry: id=%d, input=%s, actual=%s", entry.getEntryId(), entry, oEntry));
			}

			++size;
		}

		if (size == 0) {
			return null;
		}

//		if (size == entrys.length) {
//			return (XRReteEntry[]) entrys;
//		}

		XRReteEntry[] newEntrys = new XRReteEntry[size];
		int idx = 0;
		for (IRReteEntry entry : entrys) {
			if (entry != null) {
				newEntrys[idx++] = (XRReteEntry) entry;
			}
		}

		return newEntrys;
	}

	@Override
	public void addReference(IRReteEntry entry, IRReteNode node, IRReteEntry... parents) throws RException {

		if (XREntryTable.TRACE) {
			System.out
					.println("==> addReference: " + entry + ", node=" + node + ", parents=" + RuleUtil.toList(parents));
		}

		if (parents.length > 3) {
			throw new RException("Not support parentIds: " + RuleUtil.toList(parents));
		}

		_addReference(_toEntry(entry), node, _toEntry(parents));
	}

	@Override
	public IRReteEntry createEntry(String namedName, IRObject[] elements, RReteStatus status) {

		XRReteEntry entry = new XRReteEntry(namedName, elements);
		entry.setStatus(status);

		int entryId = -1;

		if (REUSE_ENTRY_ID && !freeEntryIdList.isEmpty()) {

			entryId = freeEntryIdList.pollFirst();
			if (XREntryTable.TRACE) {
				System.out.println("    reuse entry: id=" + entryId + ", entry=" + entry);
			}

			entryArray.set(entryId, entry);

		} else {

			entryId = entryArray.size() + 1;
			if (XREntryTable.TRACE) {
				System.out.println("    new-entry: id=" + entryId + ", entry=" + entry);
			}

			entryArray.add(entry);
		}

		entry.setEntryId(entryId);
		++entryCount;

		return entry;
	}

	@Override
	public int doGC() throws RException {

		if (XREntryTable.TRACE) {
			System.out.println("==> doGC:");
		}

		int update = 0;

		int size = entryArray.size();
		for (int i = 0; i < size; ++i) {
			XRReteEntry entry = entryArray.get(i);
			if (entry != null && entry.isDroped()) {
				entryArray.set(i, null);
				++update;
			}
		}

		return update;
	}

	@Override
	public XRReteEntry getEntry(int entryId) throws RException {

		if (entryId == 0 || entryId > entryArray.size()) {
			return null;
		}

		return entryArray.get(entryId - 1);
	}

	@Override
	public int getEntryCount() {
		return entryCount;
	}

	@Override
	public int getEntryMaxId() {
		return entryArray.size();
	}

	@Override
	public int getETAMaxActionSize() {
		return maxActionSize;
	}

	@Override
	public int getETAQueueCapacity() {
		return etaQueue.getQueueCapacity();
	}

	@Override
	public int getETAQueueExpendCount() {
		return etaQueue.getExpendCount();
	}

	@Override
	public int getETAQueueMaxSize() {
		return etaQueue.getMaxQueueSize();
	}

	@Override
	public int getETATotalActionSize() {
		return etaQueue.getActionSize();
	}

	@Override
	public List<? extends IRReteEntry> listAllEntries() {
		return entryArray;
	}

	@Override
	public void removeEntry(IRReteEntry entry) throws RException {

		if (XREntryTable.TRACE) {
			System.out.println("==> removeEntry: entry=" + entry);
		}

		XRReteEntry xEntry = _toEntry(entry);
		_pushRemoveEntry(xEntry);
		_processEtaQueue();
	}

	@Override
	public void removeEntryReference(int entryId, int nodeId) throws RException {

		if (XREntryTable.TRACE) {
			System.out.println("==> removeEntryReference: entryId=" + entryId + ", nodeId" + nodeId);
		}

		XRReteEntry entry = getEntry(entryId);
		if (!_isValidEntry(entry)) {
			return;
		}

		int find = 0;

		if (entry.referenceList != null) {

			Iterator<XRReference> it = entry.referenceList.iterator();
			while (it.hasNext()) {

				XRReference ref = it.next();
				if (ref.nodeId != nodeId) {
					continue;
				}

				// Break the link between child entry and reference
				it.remove();
				ref.childEntry = null;
				++find;

				_pushRemoveRef(ref);
			}
		}

		if (find == 0) {
			throw new RException("no ref found");
		}

		if (entry.referenceList == null || entry.referenceList.isEmpty()) {
			_pushRemoveEntry(entry);
			_processEtaQueue();
		}
	}

	@Override
	public void setEntryStatus(int entryId, RReteStatus status) throws RException {

		if (XREntryTable.TRACE) {
			System.out.println("==> setEntryStatus: entryId=" + entryId + ", status" + status);
		}

		_setEntryStatus(entryId, null, status);
	}

}