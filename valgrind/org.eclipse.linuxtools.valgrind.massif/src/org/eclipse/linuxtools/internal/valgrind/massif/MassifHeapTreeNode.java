/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.internal.valgrind.massif;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MassifHeapTreeNode {
	protected MassifHeapTreeNode parent;
	protected String text;
	protected double percent;
	protected long bytes;
	protected String address;
	protected String function;
	protected String filename;
	protected int line;
	protected List<MassifHeapTreeNode> children;
	
	public MassifHeapTreeNode(MassifHeapTreeNode parent, double percent, long bytes, String address, String function, String filename, int line) {
		this.parent = parent;
		
		StringBuffer nodeText = new StringBuffer();
		formatBytes(percent, bytes, nodeText);
		nodeText.append(address).append(":"); //$NON-NLS-1$
		if (function.length() > 0) {
			nodeText.append(" "); //$NON-NLS-1$
			nodeText.append(function);
		}
		if (filename != null) {
			nodeText.append(" (").append(filename); //$NON-NLS-1$
			if (line > 0) {
				nodeText.append(":").append(line);//$NON-NLS-1$
			}
			nodeText.append(")"); //$NON-NLS-1$
		}
		this.percent = percent;
		this.bytes = bytes;
		this.address = address;
		this.function = function;
		this.filename = filename;
		this.line = line;
		this.text = nodeText.toString();
		children = new ArrayList<>();
	}

	public MassifHeapTreeNode(MassifHeapTreeNode parent, double percent, long bytes, String text) {
		this.parent = parent;
		
		StringBuffer nodeText = new StringBuffer();
		formatBytes(percent, bytes, nodeText);
		nodeText.append(text);
		this.percent = percent;
		this.bytes = bytes;
		this.address = null;
		this.function = null;
		this.filename = null;
		this.line = 0;
		this.text = nodeText.toString();
		children = new ArrayList<>();
	}

	private void formatBytes(double percent, long bytes, StringBuffer buffer) {
		buffer.append(new DecimalFormat("0.##").format(percent) + "%"); //$NON-NLS-1$ //$NON-NLS-2$
		buffer.append(" ("); //$NON-NLS-1$
		buffer.append(new DecimalFormat("#,##0").format(bytes) + "B"); //$NON-NLS-1$ //$NON-NLS-2$
		buffer.append(") "); //$NON-NLS-1$
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
	
	public void setText(String text) {
		this.text = text;
	}
	
	public double getPercent() {
		return percent;
	}
	
	public long getBytes() {
		return bytes;
	}
	
	public String getAddress() {
		return address;
	}
	
	public String getFunction() {
		return function;
	}
	
	public String getFilename() {
		return filename;
	}
	
	public int getLine() {
		return line;
	}
		
	@Override
	public String toString() {
		return text;
	}

	public boolean hasSourceFile() {
		return filename != null && line > 0;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof MassifHeapTreeNode 
		&& text.equals(((MassifHeapTreeNode) obj).getText()); 
	}
	
	@Override
	public int hashCode() {
		return text.hashCode();
	}
}
