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
package org.eclipse.linuxtools.valgrind.cachegrind.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.graphics.Image;

public class CachegrindOutput implements ICachegrindElement {
	protected List<CachegrindDescription> descriptions;
	protected List<CachegrindFile> files;
	protected String cmd;
	protected String[] events;
	protected long[] summary;
	
	public CachegrindOutput() {
		descriptions = new ArrayList<CachegrindDescription>();
		files = new ArrayList<CachegrindFile>();
	}
	
	public void addDescription(CachegrindDescription desc) {
		if (!descriptions.contains(desc)) { // in case of another output file 
			descriptions.add(desc);
		}
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
		if (this.summary == null) {
			this.summary = summary;
		}
		else { // another output file
			for (int i = 0; i < summary.length; i++) {
				this.summary[i] += summary[i];
			}
		}
	}
	
	public String getCmd() {
		return cmd;
	}
	
	public CachegrindDescription[] getDescriptions() {
		return descriptions.toArray(new CachegrindDescription[descriptions.size()]);
	}
	
	public String[] getEvents() {
		return Arrays.copyOf(events, events.length);
	}
	
	public CachegrindFile[] getFiles() {
		return files.toArray(new CachegrindFile[files.size()]);
	}
	
	public long[] getSummary() {
		return Arrays.copyOf(summary, summary.length);
	}

	public ICachegrindElement[] getChildren() {
		return getFiles();
	}

	public Image getImage(int index) {
		return null;
	}

	public ICachegrindElement getParent() {
		return null;
	}

	public String getText(int index) {
		return null;
	}
	
//	/**
//	 * Joins two Cachegrind outputs for when a program forks.
//	 * Command, cache descriptions, events should be the same
//	 * Profiled file results must be concatenated.
//	 * Summary should be sum of the two.
//	 * @param other - the other CachegrindOutput to add to this
//	 */
//	public void join(CachegrindOutput other) {
//		files.addAll(Arrays.asList(other.getFiles()));
//		
//	}
	
}
