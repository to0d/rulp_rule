package alpha.rulp.ximpl.bs;

import java.util.ArrayList;

public abstract class AbsBSNode implements IRBSNode {

	protected XRBackSearcher bs;

	protected ArrayList<AbsBSNode> childNodes;

	protected int indexInParent = -1;

	protected int level = -1;

	protected int nodeId;

	protected String nodeName;

	protected AbsBSNode parentNode;

	protected BSStats status;

	public AbsBSNode(XRBackSearcher bs, int nodeId, String nodeName) {
		super();
		this.nodeId = nodeId;
		this.nodeName = nodeName;
		this.bs = bs;
	}

	public void addChild(AbsBSNode child) {

		if (bs._isTrace()) {
			bs._outln(this, String.format("add child, type=%s, name=%s", child.getType(), child.nodeName));
		}

		if (this.childNodes == null) {
			this.childNodes = new ArrayList<>();
		}

		child.parentNode = this;
		child.indexInParent = this.getChildCount();

		this.childNodes.add(child);
	}

	@Override
	public IRBSNode getChild(int index) {
		return this.childNodes == null || index < 0 || index >= this.childNodes.size() ? null
				: this.childNodes.get(index);
	}

	public int getChildCount() {
		return this.childNodes == null ? 0 : this.childNodes.size();
	}

	public int getIndexInParent() {
		return indexInParent;
	}

	public int getLevel() {

		if (level == -1) {
			if (this.parentNode == null) {
				level = 0;
			} else {
				level = this.parentNode.getLevel() + 1;
			}
		}

		return level;
	}

	public String getNodeName() {
		return nodeName;
	}

	public IRBSNode getParentNode() {
		return parentNode;
	}

	public BSStats getStatus() {
		return status;
	}

	@Override
	public void setStatus(BSStats status) {
		this.status = status;
	}

}
