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

package org.eclipse.linuxtools.systemtap.ui.editor;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.linuxtools.internal.systemtap.ui.editor.Localization;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.ILocationProvider;



public class PathEditorInput implements IPathEditorInput, ILocationProvider {
	private IPath fPath;
	private IWorkbenchWindow fMainWindow;
	public boolean temp = false;

	public PathEditorInput(IPath path) {
		if (path == null) {
			throw new IllegalArgumentException();
		}
		this.fPath = path;
	}
	public PathEditorInput(IPath path, IWorkbenchWindow window) {
		this(path);
		this.fMainWindow = window;
	}

	public PathEditorInput() throws IOException	{
		temp = true;
		File file = File.createTempFile(Localization.getString("PathEditorInput.Untitled") , ".stp"); //$NON-NLS-1$ //$NON-NLS-2$
		fPath = new Path(file.getAbsolutePath());
	}

	@Override
	public int hashCode() {
		return fPath.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof PathEditorInput))
			return false;
		PathEditorInput other = (PathEditorInput) obj;

		return fPath.equals(other.fPath);
	}

	@Override
	public boolean exists() {
		return fPath.toFile().exists();
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return PlatformUI.getWorkbench().getEditorRegistry().getImageDescriptor(fPath.toString());
	}

	@Override
	public String getName() {
		String[] substr = fPath.segments();
		return substr[substr.length -1];
	}

	@Override
	public String getToolTipText() {
		return fPath.makeRelative().toOSString();
	}

	@Override
	public IPath getPath() {
		return fPath;
	}

	@Override
	public Object getAdapter(Class adapter) {
		return null;
	}

	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	public IWorkbenchWindow getMainWindow() {
		return fMainWindow;
	}

	@Override
	public IPath getPath(Object element) {
		if(element instanceof PathEditorInput) {
			return ((PathEditorInput)element).getPath();
		}
		return null;
	}

	public void setPath(IPath newPath) {
		fPath = newPath;
	}
}
