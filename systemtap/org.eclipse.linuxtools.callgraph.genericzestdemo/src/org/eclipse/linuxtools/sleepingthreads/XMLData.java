package org.eclipse.linuxtools.sleepingthreads;

public class XMLData {
	
	private int parent;
	private String name;
	private boolean isNode;
	private String text;
	private Object object;
	
	public boolean isNode() {
		return isNode;
	}
	
	public void setIsNode(boolean val) {
		isNode = val;
	}

	public void setParent(int val) {
		parent = val;
	}
	
	public int getParent() {
		return parent;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}
	

}
