/*******************************************************************************
 * Copyright (c) 2012 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Red Hat Inc. - Initial implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.cdt.libhover.devhelp;

import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.help.ITopic;
import org.eclipse.help.IUAElement;
import org.eclipse.linuxtools.cdt.libhover.devhelp.DevHelpPlugin;

public class DevHelpTopic implements ITopic {

	private String name;

	DevHelpTopic(String name) {
		this.name = name;
	}

	@Override
	public boolean isEnabled(IEvaluationContext context) {
		return true;
	}

	@Override
	public IUAElement[] getChildren() {
		return new IUAElement[0];
	}

	@Override
	public String getHref() {
		return "/" + DevHelpPlugin.PLUGIN_ID + "/" + name + "/index.html"; // $NON-NLS-1$ //$NON-NLS-2$" //$NON-NLS-3$

	}

	@Override
	public String getLabel() {
		return name;
	}

	@Override
	public ITopic[] getSubtopics() {
		return null;
	}
}