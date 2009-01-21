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

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IFunction;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.graphics.Image;

public class CachegrindFunction implements ICachegrindElement {
	protected CachegrindFile parent;
	protected String name;
	protected List<CachegrindLine> lines;
	
	protected IAdaptable model;
	
	public CachegrindFunction(CachegrindFile parent, String name) {
		this.parent = parent;
		this.name = name;
		lines = new ArrayList<CachegrindLine>();
		
		IAdaptable pModel = parent.getModel();
		if (pModel instanceof ICElement) {
			ICElement element = (ICElement) pModel;
			try {
				if (element instanceof ITranslationUnit) {
					//FIXME Does this work for C++?
					List<ICElement> funcs = ((ITranslationUnit) element).getChildrenOfType(ICElement.C_FUNCTION);
					for (int i = 0; i < funcs.size(); i++) {
						ICElement func = funcs.get(i);
						if (func instanceof IFunction && func.getElementName().equals(name)) {
							model = func;
						}
					}
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void addLine(CachegrindLine line) {
		lines.add(line);
	}
	
	public String getName() {
		return name;
	}
	
	public IAdaptable getModel() {
		return model;
	}
	
	public CachegrindLine[] getLines() {
		return lines.toArray(new CachegrindLine[lines.size()]);
	}

	public ICachegrindElement[] getChildren() {
		return getLines();
	}

	public Image getImage(int index) {
		return null;
	}

	public ICachegrindElement getParent() {
		return parent;
	}

	public String getText(int index) {
		return index == 0 ? name : null;
	}
}
