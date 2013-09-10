/*******************************************************************************
 * Copyright (c) 2008 Alexander Kurtakov.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Kurtakov - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui.editor.hyperlink;

import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.linuxtools.internal.rpm.ui.editor.SpecfileLog;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileElement;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

/**
 * Hyperlink implementation for the following hyperlink elements: SOURCE, PATCH
 * and %define.
 */
public class SpecfileElementHyperlink implements IHyperlink {
	private IRegion region;
	private SpecfileElement source;
	private IFile file;

	public SpecfileElementHyperlink(IRegion region, SpecfileElement source,
			IFile file) {
		this.region = region;
		this.source = source;
		this.file = file;
	}

	@Override
	public IRegion getHyperlinkRegion() {
		return region;
	}

	@Override
	public String getHyperlinkText() {
		return null;
	}

	@Override
	public String getTypeLabel() {
		return null;
	}

	@Override
	public void open() {
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry()
				.getDefaultEditor(file.getName());
		HashMap<String, Object> map = new HashMap<String, Object>();
		// TODO don't increment the line number once the SpecfileSource reports
		// correct line
		map.put(IMarker.LINE_NUMBER, Integer
				.valueOf(getSource().getLineNumber() + 1));
		map.put(IDE.EDITOR_ID_ATTR, desc.getId());
		try {
			IMarker marker = file.createMarker(IMarker.TEXT);
			marker.setAttributes(map);
			IDE.openEditor(page, marker);
			marker.delete();
		} catch (CoreException e) {
			SpecfileLog.logError(e);
		}
	}

	/**
	 * @return the source
	 */
	public SpecfileElement getSource() {
		return source;
	}

}
