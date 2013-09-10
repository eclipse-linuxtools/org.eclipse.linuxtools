/*******************************************************************************
 * Copyright (c) 2006 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kyu Lee <klee@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.changelog.core.editors;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * Hyperlink that opens up editor for a file.
 *
 * @author klee (Kyu Lee)
 *
 */
public class FileHyperlink implements IHyperlink {

	private IFile fileLoc;

	private IRegion region;

	public FileHyperlink(IRegion regionIn, IFile fileIn) {
		fileLoc = fileIn;
		region = regionIn;
	}

	@Override
	public IRegion getHyperlinkRegion() {
		return region;
	}

	@Override
	public String getTypeLabel() {
		return null;
	}

	@Override
	public String getHyperlinkText() {
		return null;
	}

	/**
	 * Opens the hyperlink in new editor window.
	 */
	@Override
	public void open() {
		IWorkbench ws = PlatformUI.getWorkbench();
		try {
			org.eclipse.ui.ide.IDE.openEditor(ws.getActiveWorkbenchWindow()
					.getActivePage(), fileLoc, true);
		} catch (PartInitException e) {
			e.printStackTrace();

		}

	}
}
