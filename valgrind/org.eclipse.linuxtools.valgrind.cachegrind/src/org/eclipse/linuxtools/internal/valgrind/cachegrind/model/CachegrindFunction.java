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
package org.eclipse.linuxtools.internal.valgrind.cachegrind.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IFunction;
import org.eclipse.cdt.core.model.IMethod;
import org.eclipse.cdt.core.model.IParent;
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
		lines = new ArrayList<>();

		IAdaptable pModel = parent.getModel();
		if (pModel instanceof ICElement) {
			ICElement element = (ICElement) pModel;
			try {
				if (element instanceof ITranslationUnit) {
					// Cachegrind labels parameter types for C++ methods
					int paramIndex = name.indexOf("("); //$NON-NLS-1$
					if (paramIndex >= 0) {
						name = name.substring(0, paramIndex);
					}

					model = findElement(name, (IParent) element);
					if (model == null) {
						while (name.contains(SCOPE_RESOLUTION)) {
							String[] parts = name.split(SCOPE_RESOLUTION, 2);
							String structureName = parts[0];
							name = parts[1];

							List<ICElement> dom = ((IParent) element).getChildrenOfType(ICElement.C_CLASS);
							dom.addAll(((IParent) element).getChildrenOfType(ICElement.C_STRUCT));
							dom.addAll(((IParent) element).getChildrenOfType(ICElement.C_UNION));
							for (int i = 0; i < dom.size(); i++) {
								ICElement e = dom.get(i);
								if (e instanceof IStructure && e.getElementName().equals(structureName)) {
									element = e;
								}
							}
						}
						model = findElement(name, (IParent) element);
					}
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}

	protected ICElement findElement(String name, IParent parent)
	throws CModelException {
		ICElement element = null;
		List<ICElement> dom = parent.getChildrenOfType(ICElement.C_FUNCTION);
		dom.addAll(parent.getChildrenOfType(ICElement.C_METHOD));
		for (int i = 0; i < dom.size(); i++) {
			ICElement func = dom.get(i);
			if ((func instanceof IFunction || func instanceof IMethod) && func.getElementName().equals(name)) {
				element = func;
			}
		}
		return element;
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

	@Override
	public IAdaptable getModel() {
		return model;
	}

	public long[] getTotals() {
		return totals;
	}

	public CachegrindLine[] getLines() {
		return lines.toArray(new CachegrindLine[lines.size()]);
	}

	@Override
	public ICachegrindElement[] getChildren() {
		ICachegrindElement[] children = null;
		// if there is only a summary don't return any children
		if (lines.get(0).getLine() > 0) {
			children = getLines();
		}
		return children;
	}

	@Override
	public ICachegrindElement getParent() {
		return parent;
	}
	
	@Override
	public int compareTo(ICachegrindElement o) {
		int result = 0;
		if (o instanceof CachegrindFunction) {
			result = name.compareTo(((CachegrindFunction) o).getName());
		}
		return result;
	}

}
