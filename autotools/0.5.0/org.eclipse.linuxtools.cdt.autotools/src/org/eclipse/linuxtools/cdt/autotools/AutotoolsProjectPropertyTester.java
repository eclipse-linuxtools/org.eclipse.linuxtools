/*******************************************************************************
 * Copyright (c) 2007 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.cdt.autotools;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

public class AutotoolsProjectPropertyTester extends PropertyTester {

	public AutotoolsProjectPropertyTester() {
		// nothing needed
	}

	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		IProject project = null;
		IResource resource = (IResource)receiver;
		project = resource.getProject();
		return AutotoolsMakefileBuilder.hasTargetBuilder(project);
	}

}
