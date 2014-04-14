/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.callgraph.launch;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementVisitor;

public class TranslationUnitVisitor implements ICElementVisitor {
	private String functions;

	public TranslationUnitVisitor() {
		super();
		functions = ""; //$NON-NLS-1$
	}

	@Override
	public boolean visit(ICElement arg0) {
		if (arg0.getElementType() == ICElement.C_FUNCTION) {
			functions += arg0.getElementName() + " "; //$NON-NLS-1$
			return false;
		}
		return true;
	}

	public String getFunctions() {
		return functions;
	}
}
