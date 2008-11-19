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
package org.eclipse.linuxtools.valgrind.massif;

public class MassifSnapshot {
	public static enum SnapshotType { EMPTY, DETAILED, PEAK };
	
	protected int number;
	protected int time;
	protected int heapBytes;
	protected int heapExtra;
	protected int stacks;
	protected SnapshotType type;
	protected MassifHeapTreeNode root;
		
	public MassifSnapshot(int number) {
		this.number = number;
	}
	
	public int getNumber() {
		return number;
	}

	public int getHeapBytes() {
		return heapBytes;
	}
	
	public int getHeapExtra() {
		return heapExtra;
	}
	
	public int getStacks() {
		return stacks;
	}
	
	public int getTime() {
		return time;
	}
	
	public int getTotal() {
		return heapBytes + heapExtra + stacks;
	}
	
	public SnapshotType getType() {
		return type;
	}
	
	public MassifHeapTreeNode getRoot() {
		return root;
	}
	
	protected void setTime(int time) {
		this.time = time;
	}

	protected void setHeapBytes(int heapBytes) {
		this.heapBytes = heapBytes;
	}

	protected void setHeapExtra(int heapExtra) {
		this.heapExtra = heapExtra;
	}

	protected void setStacks(int stacks) {
		this.stacks = stacks;
	}
	
	protected void setType(SnapshotType type) {
		this.type = type;
	}
	
	public void setRoot(MassifHeapTreeNode root) {
		this.root = root;
	}
}
