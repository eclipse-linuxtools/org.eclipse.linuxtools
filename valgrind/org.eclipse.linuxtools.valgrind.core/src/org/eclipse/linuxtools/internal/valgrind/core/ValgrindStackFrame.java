/*******************************************************************************
 * Copyright (c) 2008, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *    Alena Laskavaia - Bug 482947 - Valgrind Message API's: get rid of launch dependency
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.core;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.linuxtools.valgrind.core.IValgrindMessage;

/**
 * Valgrind stack frame message, i.e. message that carry a location of the error or a single stack frame info
 */
public class ValgrindStackFrame extends AbstractValgrindMessage {
	protected String file;
	protected int line;
	private ISourceLocator locator;

    /**
     * Constructor
     * @param parent - parent message
     * @param text - message test cannot be null
     * @param launch - launch object can be null
     * @param locator - source location, used to find source files, can be null
     * @param file - string representation of a source file (path)
     * @param line - line number of the source
     */
	public ValgrindStackFrame(IValgrindMessage parent, String text, ILaunch launch, ISourceLocator locator, String file, int line) {
		super(parent, text, launch);
		this.file = file;
		this.line = line;
		this.locator = locator;
	}

	/**
	 * Source file
	 *
	 * Note: new Valgrind versions (e.g. 3.10) prints the full path of file,
	 * not just the source file name.
	 *
	 * @return a source file string
	 */
	public String getFile() {
		return file;
	}

	/**
	 * Line number
	 * @return line number
	 */
	public int getLine() {
		return line;
	}

	/**
	 * Source locator
	 * @return source locator object, can be null
	 */
	public ISourceLocator getSourceLocator() {
		if (locator != null)
			return locator;
		if (getLaunch() != null) {
			return getLaunch().getSourceLocator();
		}
		return null;
	}
}
