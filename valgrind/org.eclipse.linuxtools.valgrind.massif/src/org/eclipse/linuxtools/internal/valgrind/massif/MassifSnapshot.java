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

public class MassifSnapshot {
    public static enum TimeUnit { INSTRUCTIONS, MILLISECONDS, BYTES }
    public static enum SnapshotType { EMPTY, DETAILED, PEAK }

    protected long number;
    protected long time;
    protected long heapBytes;
    protected long heapExtra;
    protected long stacks;

    protected String cmd;
    protected TimeUnit unit;
    protected SnapshotType type;
    protected MassifHeapTreeNode root;

    public MassifSnapshot(int number) {
        this.number = number;
    }

    public long getNumber() {
        return number;
    }

    public long getHeapBytes() {
        return heapBytes;
    }

    public long getHeapExtra() {
        return heapExtra;
    }

    public long getStacks() {
        return stacks;
    }

    public long getTime() {
        return time;
    }

    public long getTotal() {
        return heapBytes + heapExtra + stacks;
    }

    public SnapshotType getType() {
        return type;
    }

    public String getCmd() {
        return cmd;
    }

    public TimeUnit getUnit() {
        return unit;
    }

    public MassifHeapTreeNode getRoot() {
        return root;
    }

    public boolean isDetailed() {
        return !type.equals(SnapshotType.EMPTY);
    }

    protected void setTime(long time) {
        this.time = time;
    }

    protected void setHeapBytes(long heapBytes) {
        this.heapBytes = heapBytes;
    }

    protected void setHeapExtra(long heapExtra) {
        this.heapExtra = heapExtra;
    }

    protected void setStacks(long stacks) {
        this.stacks = stacks;
    }

    protected void setType(SnapshotType type) {
        this.type = type;
    }

    protected void setCmd(String cmd) {
        this.cmd = cmd;
    }

    protected void setUnit(TimeUnit unit) {
        this.unit = unit;
    }

    protected void setRoot(MassifHeapTreeNode root) {
        this.root = root;
    }
}
