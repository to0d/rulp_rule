package alpha.rulp.ximpl.entry;

import static alpha.rulp.rule.RReteStatus.CLEAN;
import static alpha.rulp.rule.RReteStatus.REMOVE;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RType;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.rule.RReteStatus;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.runtime.IRListener1;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.XRRListener1Adapter;
import alpha.rulp.ximpl.lang.XRListNative;

public class XREntryTable implements IREntryTable {

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

	static class XRReference implements IRReference, IFixEntry {

		public XRReteEntry childEntry;

		public IRReteNode node;

		public int parentEntryCount;

		public final XRReteEntry[] parentEntrys;

		public int refId;

		public XRReference(IRReteNode node, XRReteEntry childEntry, int parentEntryCount, XRReteEntry[] parentEntrys) {
			super();
			this.node = node;
			this.childEntry = childEntry;
			this.parentEntryCount = parentEntryCount;
			this.parentEntrys = parentEntrys;
		}

		public void drop() {
			this.parentEntryCount = -1;
		}

		@Override
		public IRReteEntry getChildEntry() {
			return childEntry;
		}

		@Override
		public int getEntryId() {
			return refId;
		}

		@Override
		public IRReteNode getNode() {
			return node;
		}

		@Override
		public IRReteEntry getParentEntry(int index) {
			return parentEntrys[index];
		}

		@Override
		public int getParentEntryCount() {
			return parentEntryCount;
		}

		@Override
		public boolean isDroped() {
			return parentEntryCount == -1;
		}

		@Override
		public void setEntryId(int id) {
			this.refId = id;
		}
	}

	static class XRReteEntry extends XRListNative implements IRReteEntry, IFixEntry {

		private List<XRReference> childList = null;

		private int entryId = 0;

		private int entryIndex = 0;

		private XRRListener1Adapter<IRReteEntry> entryRemovedlistener = null;

		private boolean isStmt = false;

		private List<XRReference> referenceList = null;

		private RReteStatus status = RReteStatus.DEFINE;

		public XRReteEntry(int entryIndex, String namedName, IRObject[] elements) {
			super(elements, RType.LIST, namedName, false);
			this.entryIndex = entryIndex;
		}

		public void addChild(XRReference ref) {

			if (childList == null) {
				childList = new LinkedList<>();
			}

			childList.add(ref);
		}

		@Override
		public void addEntryRemovedListener(IRListener1<IRReteEntry> listener) {

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
		public int getChildCount() {
			return childList == null ? 0 : childList.size();
		}

		@Override
		public Iterator<? extends IRReference> getChildIterator() {
			return childList == null ? Collections.emptyIterator() : childList.iterator();
		}

		@Override
		public int getEntryId() {
			return entryId;
		}

		@Override
		public int getEntryIndex() {
			return entryIndex;
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
		public boolean isDroped() {
			return this.status == REMOVE || this.status == CLEAN || this.status == null;
		}

		@Override
		public boolean isStmt() {
			return isStmt;
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
		public void removeEntryRemovedListener(IRListener1<IRReteEntry> listener) {

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

		public void setStmt(boolean isStmt) {
			this.isStmt = isStmt;
		}
	}

	static int FIX_ETA_DEF_SIZE = 256;

	static int FIX_ETA_MAX_SIZE = 102400;

	public static boolean TRACE = false;

	protected static int _indexOf(int[] array, int id) throws RException {

		if (array != null) {
			for (int i = 0; i < array.length; ++i) {
				if (array[i] == id) {
					return i;
				}
			}
		}

		return -1;
	}

	static boolean _isFix(XRReteEntry entry) {
		return entry != null && entry.getStatus() == RReteStatus.FIXED_;
	}

	static boolean _isTemp(XRReteEntry entry) {
		return entry != null && entry.getStatus() == RReteStatus.TEMP__;
	}

	static boolean _isValidEntry(XRReteEntry entry) {
		return entry != null && entry.status != REMOVE && entry.status != null;
	}

	static boolean _isValidReference(XRReference ref) {
		return ref != null && !ref.isDroped();
	}

	protected int entryCount = 0;

	protected XFixEntryArray<XRReteEntry> entryFixArray = new XFixEntryArray<>();

	protected ETAQueue etaQueue = new ETAQueue(FIX_ETA_DEF_SIZE);

	protected int maxActionSize = 0;

	protected XFixEntryArray<XRReference> refFixArray = new XFixEntryArray<>();

	protected void _addReference(XRReteEntry childEntry, IRReteNode node, XRReteEntry[] parentEntrys)
			throws RException {

		/****************************************************/
		// Optimization: "fix" entry only need one reference
		/****************************************************/
		if (_isFix(childEntry) && childEntry.getReferenceCount() > 0) {
			return;
		}

		int parentEntryCount = parentEntrys == null ? 0 : parentEntrys.length;

		XRReference ref = new XRReference(node, childEntry, parentEntryCount, parentEntrys);

		// Add links to child
		childEntry.addReference(ref);

		// Add links to parent
		if (parentEntrys != null) {
			for (XRReteEntry parent : parentEntrys) {
				if (!_isFix(parent)) {
					parent.addChild(ref);
				}
			}
		}

		refFixArray.addEntry(ref);
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

		if (_isFix(entry)) {
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
					IRReteEntry parent = ref.getParentEntry(i);
					if (parent != null && parent.getEntryId() == entryId) {
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

		entry.status = null; // mark entry as "drop"

		entryFixArray.removeEntry(entry);
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

				if (!_isFix(parentEntry)) {

					Iterator<XRReference> it = parentEntry.childList.iterator();
					while (it.hasNext()) {
						if (it.next() == ref) {
							it.remove();
						}
					}

					if (parentEntry.childList.isEmpty()) {
						parentEntry.childList = null;
					}
				}

				// Break the link between reference and parent entry
				ref.parentEntrys[i] = null;
			}
		}

		// mark it as invalid
		ref.drop();
		refFixArray.removeEntry(ref);
	}

	protected void _pushRemoveEntry(XRReteEntry entry) throws RException {

		if (_isFix(entry)) {
			throw new RException("Can't remove fix entry: " + entry);
		}

		etaQueue.push(ETA_Type.REMOVE_ENTRY, entry);
	}

	protected void _pushRemoveRef(XRReference ref) throws RException {

		if (!_isValidReference(ref)) {
			throw new RException("Invalid Ref: " + ref);
		}

		etaQueue.push(ETA_Type.REMOVE_REF, ref);
	}

	protected void _setEntryStatus(XRReteEntry entry, RReteStatus status) throws RException {

		entry.setStatus(status);

		if (status == RReteStatus.FIXED_) {
			entry.childList = null;
		}
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

	protected XRReteEntry[] _toValidParentEntry(IRReteEntry[] entrys) throws RException {

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

			XRReteEntry xEntry = (XRReteEntry) entry;
			if (!_isValidEntry(xEntry)) {
				throw new RException("Invalid entry: " + entry);
			}

			XRReteEntry oEntry = getEntry(entry.getEntryId());
			if (xEntry != oEntry) {
				throw new RException(
						String.format("unmatch entry: id=%d, input=%s, actual=%s", entry.getEntryId(), entry, oEntry));
			}

			// ignore temp entry
			if (_isTemp(xEntry)) {
				continue;
			}

			++size;
		}

		if (size == 0) {
			return null;
		}

		XRReteEntry[] newEntrys = new XRReteEntry[size];
		int idx = 0;

		for (IRReteEntry entry : entrys) {

			if (entry != null) {

				XRReteEntry xEntry = (XRReteEntry) entry;
				if (_isTemp(xEntry)) { // ignore temp entry
					continue;
				}

				newEntrys[idx++] = xEntry;
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

		_addReference(_toEntry(entry), node, _toValidParentEntry(parents));
	}

	@Override
	public IRReteEntry createEntry(String namedName, IRObject[] elements, RReteStatus status, boolean isStmt) {

		XRReteEntry entry = new XRReteEntry(++entryCount, namedName, elements);
		entry.setStatus(status);
		entry.setStmt(isStmt);
		entryFixArray.addEntry(entry);

		return entry;
	}

	@Override
	public int doGC() throws RException {

		if (XREntryTable.TRACE) {
			System.out.println("==> doGC:");
		}

		return entryFixArray.doGC();
	}

	@Override
	public XRReteEntry getEntry(int entryId) throws RException {
		return entryFixArray.getEntry(entryId);
	}

	@Override
	public int getEntryCount() {
		return entryFixArray.getEntryCount();
	}

	@Override
	public IFixEntryArray<? extends IRReteEntry> getEntryFixArray() {
		return entryFixArray;
	}

	@Override
	public int getEntryMaxId() {
		return entryFixArray.getEntryMaxId();
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
	public IFixEntryArray<? extends IRReference> getReferenceFixArray() {
		return refFixArray;
	}

	@Override
	public List<? extends IRReteEntry> listAllEntries() {
		return entryFixArray.entryArray;
	}

	@Override
	public void removeEntry(IRReteEntry entry) throws RException {

		if (XREntryTable.TRACE) {
			System.out.println("==> removeEntry: entry=" + entry);
		}

		XRReteEntry xEntry = _toEntry(entry);
		RReteStatus status = xEntry.getStatus();

		_pushRemoveEntry(_toEntry(xEntry));
		_processEtaQueue();

		// Remove assume from uniq node
		if (status == RReteStatus.ASSUME) {
			xEntry.status = CLEAN;
		} else {
			xEntry.status = REMOVE; // mark entry as "remove"
		}
	}

	@Override
	public void removeEntryReference(IRReteEntry entry, IRReteNode node) throws RException {

		if (XREntryTable.TRACE) {
			System.out.println("==> removeEntryReference: entry=" + entry + ", node=" + node);
		}

		XRReteEntry xEntry = _toEntry(entry);

		int find = 0;

		if (xEntry.referenceList != null) {

			Iterator<XRReference> it = xEntry.referenceList.iterator();
			while (it.hasNext()) {

				XRReference ref = it.next();
				if (ref.node != node) {
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

		if (xEntry.referenceList == null || xEntry.referenceList.isEmpty()) {
			_pushRemoveEntry(xEntry);
			_processEtaQueue();
		}
	}

	@Override
	public void setEntryStatus(IRReteEntry entry, RReteStatus status) throws RException {

		if (XREntryTable.TRACE) {
			System.out.println("==> setEntryStatus: entry=" + entry + ", status=" + status);
		}

		_setEntryStatus(_toEntry(entry), status);

	}

}