/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.profiling.launch;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.ILaunchShortcut2;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;


/**
 * @since 2.0
 */
public class CApplicationLaunchShortcut implements ILaunchShortcut2 {

	private final String CDT_LAUNCH_SHORTCUT_ID = "org.eclipse.cdt.debug.ui.localCShortcut"; //$NON-NLS-1$
	private ILaunchShortcut2 proxy;

	private ILaunchShortcut2 getProxy() {
		if (proxy == null) {
			// Get a proxy to CDT's CApplicationLaunchShortcut class which is internal
			// This plug-in has a dependency on org.eclipe.cdt.debug.ui so this extension is expected to be found.
			IExtensionPoint extPoint =
					Platform.getExtensionRegistry().getExtensionPoint("org.eclipse.debug.ui.launchShortcuts"); //$NON-NLS-1$
			IConfigurationElement[] configs = extPoint.getConfigurationElements();
			for (IConfigurationElement config : configs) {
				Object obj = null;
				if (config.getName().equals("shortcut")) { //$NON-NLS-1$
					String id = config.getAttribute("id"); //$NON-NLS-1$
					if (id.equals(CDT_LAUNCH_SHORTCUT_ID)) {
						try {
							obj = config.createExecutableExtension("class"); //$NON-NLS-1$
						} catch (CoreException e) {
							ProfileLaunchPlugin.log(e);
						}
						if (obj instanceof ILaunchShortcut2) {
							proxy = (ILaunchShortcut2)obj;
							break;
						}
					}
				}
				if (proxy != null)
					break;
			}
		}
		return proxy;
	}

	@Override
	public void launch(ISelection selection, String mode) {
		getProxy().launch(selection, mode);
	}

	@Override
	public void launch(IEditorPart editor, String mode) {
		getProxy().launch(editor, mode);
	}

	@Override
	public ILaunchConfiguration[] getLaunchConfigurations(ISelection selection) {
		return getProxy().getLaunchConfigurations(selection);
	}

	@Override
	public ILaunchConfiguration[] getLaunchConfigurations(IEditorPart editorpart) {
		return getProxy().getLaunchConfigurations(editorpart);
	}

	@Override
	public IResource getLaunchableResource(ISelection selection) {
		return getProxy().getLaunchableResource(selection);
	}

	@Override
	public IResource getLaunchableResource(IEditorPart editorpart) {
		return getProxy().getLaunchableResource(editorpart);
	}

}
