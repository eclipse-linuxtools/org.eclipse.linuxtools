/*******************************************************************************
 * Copyright (c) 2010 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexander Kurtakov - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.ui;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.ui.console.IPatternMatchListenerDelegate;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;

/**
 * Matcher to handle missing dependencies.
 *
 */
public class MissingDependencyMatcher implements IPatternMatchListenerDelegate {

	private RpmConsole console;

	public void connect(TextConsole console) {
		this.console = (RpmConsole)console;
	}

	public void disconnect() {
		this.console = null;
	}

	public void matchFound(PatternMatchEvent event) {
		String line = null;
		try {
			line = console.getDocument().get(event.getOffset(),
					event.getLength());
			console.addMissingRpm(line.substring(0, line.indexOf("is needed by")).trim()); //$NON-NLS-1$
		} catch (BadLocationException e1) {
			return;
		}

	}

}
