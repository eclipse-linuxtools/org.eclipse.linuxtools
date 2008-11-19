package org.eclipse.linuxtools.valgrind.massif;

import java.util.ArrayList;
import java.util.List;

public class MassifHeapTreeNode {
	protected MassifHeapTreeNode parent;
	protected String text;
	protected String filename;
	protected int line;
	protected List<MassifHeapTreeNode> children;
	
	public MassifHeapTreeNode(MassifHeapTreeNode parent, String text) {
		this.parent = parent;
		this.text = text;
		children = new ArrayList<MassifHeapTreeNode>();
	}
	
	public void addChild(MassifHeapTreeNode child) {
		children.add(child);
	}
	
	public MassifHeapTreeNode getParent() {
		return parent;
	}
	
	public MassifHeapTreeNode[] getChildren() {
		return children.toArray(new MassifHeapTreeNode[children.size()]);
	}
	
	public String getText() {
		return text;
	}
	
	protected void setText(String text) {
		this.text = text;
	}
	
	public String getFilename() {
		return filename;
	}
	
	protected void setFilename(String filename) {
		this.filename = filename;
	}
	
	public int getLine() {
		return line;
	}
	
	protected void setLine(int line) {
		this.line = line;
	}
	
	@Override
	public String toString() {
		return text;
	}

	public boolean hasSourceFile() {
		return filename != null;
	}
}
