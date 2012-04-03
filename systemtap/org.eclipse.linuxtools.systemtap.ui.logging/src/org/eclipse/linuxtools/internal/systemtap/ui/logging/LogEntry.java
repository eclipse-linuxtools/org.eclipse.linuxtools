/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.logging;

/**
 * This class represents a single log entry.
 * @author Henry Hughes
 */
public class LogEntry {
	public int level;
	public String message;
	public LogEntry(int level, String message) {
		this.level = level;
		this.message = message;
	}
}
