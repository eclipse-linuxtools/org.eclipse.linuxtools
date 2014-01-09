/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.cachegrind.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;

public class CachegrindOutput implements ICachegrindElement {
	protected List<CachegrindDescription> descriptions;
	protected List<CachegrindFile> files;
	protected Integer pid;
	protected String cmd;
	protected String[] events;
	protected long[] summary;
	
	public CachegrindOutput() {
		descriptions = new ArrayList<>();
		files = new ArrayList<>();
	}
	
	public void addDescription(CachegrindDescription desc) {
		descriptions.add(desc);
	}
	
	public void addFile(CachegrindFile file) {
		files.add(file);
	}
	
	public void setCommand(String cmd) {
		this.cmd = cmd;
	}
	
	public void setEvents(String[] events) {
		this.events = events;
	}
	
	public void setSummary(long[] summary) {
		this.summary = summary;
	}
	
	public void setPid(Integer pid) {
		this.pid = pid;
	}
	
	public String getCmd() {
		return cmd;
	}
	
	public CachegrindDescription[] getDescriptions() {
		return descriptions.toArray(new CachegrindDescription[descriptions.size()]);
	}
	
	public String[] getEvents() {
		return events;
	}
	
	public CachegrindFile[] getFiles() {
		return files.toArray(new CachegrindFile[files.size()]);
	}
	
	public long[] getSummary() {
		return summary;
	}

	@Override
	public ICachegrindElement[] getChildren() {
		return getFiles();
	}

	@Override
	public ICachegrindElement getParent() {
		return null;
	}
	
	public Integer getPid() {
		return pid;
	}
	
	@Override
	public int compareTo(ICachegrindElement o) {
		int result = 0;
		if (o instanceof CachegrindOutput) {
			result = pid - ((CachegrindOutput) o).getPid();
		}
		return result;
	}

	@Override
	public IAdaptable getModel() {
		return null;
	}
	
}
