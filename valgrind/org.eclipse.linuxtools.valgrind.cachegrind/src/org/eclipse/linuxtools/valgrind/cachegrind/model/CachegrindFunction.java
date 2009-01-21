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
import org.eclipse.cdt.core.model.IStructure;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;

public class CachegrindFunction implements ICachegrindElement {
	protected CachegrindFile parent;
	protected String name;
	protected List<CachegrindLine> lines;
	protected long[] totals;

	protected IAdaptable model;

	private static String SCOPE_RESOLUTION = "::"; //$NON-NLS-1$

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
					//TODO create CachegrindClass and nest methods inside of it, like how the outline view does it
//					if (name.contains(SCOPE_RESOLUTION)) {
//						IStructure structure = null;
//						String structureName = name.substring(0, name.indexOf(SCOPE_RESOLUTION));
//						List<ICElement> dom = ((ITranslationUnit) element).getChildrenOfType(ICElement.C_CLASS);
//						dom.addAll(((ITranslationUnit) element).getChildrenOfType(ICElement.C_STRUCT));
//						dom.addAll(((ITranslationUnit) element).getChildrenOfType(ICElement.C_UNION));
//						for (int i = 0; i < dom.size(); i++) {
//							ICElement e = dom.get(i);
//							if (e instanceof IStructure && e.getElementName().equals(structureName)) {
//								structure = (IStructure) e;
//							}
//						}
//						if (structure != null) {
//							
//						}
//					}
//					else {
						List<ICElement> dom = ((ITranslationUnit) element).getChildrenOfType(ICElement.C_FUNCTION);
						for (int i = 0; i < dom.size(); i++) {
							ICElement func = dom.get(i);
							if (func instanceof IFunction && func.getElementName().equals(name)) {
								model = func;
							}
						}
//					}
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}

	public void addLine(CachegrindLine line) {
		long[] values = line.getValues();
		if (totals == null) {
			totals = new long[values.length];
		}
		for (int i = 0; i < values.length; i++) {
			totals[i] += values[i];
		}
		lines.add(line);
	}

	public String getName() {
		return name;
	}

	public IAdaptable getModel() {
		return model;
	}

	public long[] getTotals() {
		return totals;
	}

	public CachegrindLine[] getLines() {
		return lines.toArray(new CachegrindLine[lines.size()]);
	}

	public ICachegrindElement[] getChildren() {
		ICachegrindElement[] children = null;
		// if there is only a summary don't return any children
		if (lines.get(0).getLine() > 0) {
			children = getLines();
		}
		return children;
	}

	public ICachegrindElement getParent() {
		return parent;
	}

}
