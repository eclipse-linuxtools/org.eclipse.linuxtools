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
import java.util.List;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.graphics.Image;

public class CachegrindFile implements ICachegrindElement {
	private static final String UNKNOWN_FILE = "???"; //$NON-NLS-1$


	protected CachegrindOutput parent;
	protected String path;
	protected List<CachegrindFunction> functions;

	protected IAdaptable model;

	public CachegrindFile(CachegrindOutput parent, String path) {
		this.parent = parent;
		this.path = path;
		functions = new ArrayList<CachegrindFunction>();

		IPath pathObj = Path.fromOSString(path);
		if (path.equals(UNKNOWN_FILE)) {
			model = null;
		}
		else {
			model = CoreModel.getDefault().create(pathObj);
			if (model == null) {
				model = ResourcesPlugin.getWorkspace().getRoot().getFile(pathObj);
			}
		}
	}

	public void addFunction(CachegrindFunction func) {
		functions.add(func);
	}

	public CachegrindFunction[] getFunctions() {
		return functions.toArray(new CachegrindFunction[functions.size()]);
	}

	public ICachegrindElement[] getChildren() {
		return getFunctions();
	}

	public Image getImage(int index) {
		//		return index == 0 ? cLabelProvider.getImage(model) : null;
		return null;
	}

	public IAdaptable getModel() {
		return model;
	}

	public String getPath() {
		return path;
	}
	
	public ICachegrindElement getParent() {
		return parent;
	}

	public String getText(int index) {
		//		String text = path;
		//		if (!path.equals(UNKNOWN_FILE)) {
		//			IPath pathObj = Path.fromOSString(path);
		//			text = pathObj.lastSegment();
		//		}
		//		return index == 0 ? cLabelProvider.getText(model) : null;
		return null;
	}
}
