/*******************************************************************************
 * Copyright (c) 2012 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.profiling.launch.provider;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.linuxtools.internal.profiling.launch.provider.launch.Messages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class ProviderPreferencesPage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {

	public void init(IWorkbench workbench) {
		setDescription(Messages.ProviderPreferencesPage_0);
	}

	@Override
	protected void createFieldEditors() {
		// Content for global profiling provider preferences.
	}

}