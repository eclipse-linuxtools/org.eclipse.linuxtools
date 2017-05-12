/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - Alexander Kurtakov
 *******************************************************************************/
package org.eclipse.linuxtools.internal.systemtap.consolelog;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.ui.console.IPatternMatchListenerDelegate;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;

public class ErrorLineMatcher implements IPatternMatchListenerDelegate {

	private TextConsole console;

	@Override
	public void connect(TextConsole console) {
		this.console = console;

	}

	@Override
	public void disconnect() {
		this.console = null;

	}

	@Override
	public void matchFound(PatternMatchEvent event) {
		try {
			String line = console.getDocument().get(event.getOffset(), event.getLength());
			String file = line.substring(line.indexOf('/'));
			String[] splitted = file.split(":"); //$NON-NLS-1$
			Path path = new Path(splitted[0]);
			if (path.toFile().exists()) {
				IFileStore iFileStore = EFS.getLocalFileSystem().getStore(path);
				FileHyperlink fileLink = new FileHyperlink(iFileStore, Integer.valueOf(splitted[1]));
				console.addHyperlink(fileLink, line.indexOf('/') + event.getOffset(), file.length());
			}
		} catch (BadLocationException e1) {
			return;
		}

	}

}
