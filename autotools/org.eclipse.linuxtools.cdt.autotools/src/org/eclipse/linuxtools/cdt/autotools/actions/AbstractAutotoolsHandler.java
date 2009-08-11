/*******************************************************************************
 * Copyright (c) 2009 Red Hat Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.cdt.autotools.actions;

import java.util.Collection;

import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;

public abstract class AbstractAutotoolsHandler extends AbstractHandler {
	
	@SuppressWarnings("unchecked")
	protected IContainer getContainer(IEvaluationContext e) {
		IContainer fContainer = null;

		Object obj = e.getDefaultVariable();
		if (obj instanceof Collection) {
			Collection<Object> c = (Collection<Object>)obj;
			Object[] objArray = c.toArray();
			if (objArray.length > 0)
				obj = objArray[0];
		}
		if (obj instanceof ICElement) {
			if ( obj instanceof ICContainer || obj instanceof ICProject) {
				fContainer = (IContainer) ((ICElement) obj).getUnderlyingResource();
			} else {
				obj = ((ICElement)obj).getResource();
				if ( obj != null) {
					fContainer = ((IResource)obj).getParent();
				}
			}
		} else if (obj instanceof IResource) {
			if (obj instanceof IContainer) {
				fContainer = (IContainer) obj;
			} else {
				fContainer = ((IResource)obj).getParent();
			}
		} else {
			fContainer = null;
		}
		return fContainer;
	}

}
