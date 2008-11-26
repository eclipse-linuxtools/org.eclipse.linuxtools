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
package org.eclipse.linuxtools.valgrind.core;

public class HistoryEntry {
	protected String configName;
	protected String tool;
	protected String datadir;
	protected String processLabel;
	
	public HistoryEntry(String configName, String tool, String datadir, String processLabel) {
		this.configName = configName;
		this.tool = tool;
		this.datadir = datadir;
		this.processLabel = processLabel;
	}
	
	public String getDatadir() {
		return datadir;
	}
	
	public String getConfigName() {
		return configName;
	}
	
	public String getTool() {
		return tool;
	}
	
	public String getProcessLabel() {
		return processLabel;
	}
	
}
