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
package org.eclipse.linuxtools.valgrind.history;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.XMLMemento;

public class HistoryEntry {
	protected static final String SEPARATOR = "\t"; //$NON-NLS-1$
	protected static final int NUM_FIELDS = 2;
	
	protected String description;
	protected File datadir;
	protected XMLMemento memento;
	
	public HistoryEntry(String description, XMLMemento memento, File datadir) throws CoreException, IOException {
		this.description = description;
		this.datadir = datadir;
		this.memento = memento;
	}
	
	public String getDescription() {
		return description;
	}
	
	public File getDatadir() {
		return datadir;
	}
	
	public XMLMemento getMemento() {
		return memento;
	}
	
	public static HistoryEntry fromString(String line) throws IOException, CoreException {
		String[] parts = line.split(SEPARATOR);
		if (parts.length != NUM_FIELDS) {
			throw new IOException(Messages.getString("HistoryFile.History_file_is_corrupt")); //$NON-NLS-1$
		}
		
		File datadir = new File(HistoryFile.DATA_PATH, parts[1]);
		FileReader fr = new FileReader(new File(datadir, HistoryFile.MEMENTO_FILE));
		XMLMemento memento = XMLMemento.createReadRoot(fr);
		
		return new HistoryEntry(parts[0], memento, datadir);
	}
	
	public void destroy() {
		for (File file : datadir.listFiles()) {
			file.delete();
		}
		datadir.delete();
	}
	
	@Override
	public String toString() {
		return description + SEPARATOR + datadir.getName();
	}
	
}
