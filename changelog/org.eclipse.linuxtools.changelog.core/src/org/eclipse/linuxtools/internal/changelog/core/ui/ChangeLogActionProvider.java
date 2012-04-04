/*******************************************************************************
 * Copyright (c) 2006 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kyu Lee <klee@redhat.com> - initial API and implementation 
 *******************************************************************************/
package org.eclipse.linuxtools.internal.changelog.core.ui;

import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.linuxtools.internal.changelog.core.actions.PrepareChangeLogAction;
import org.eclipse.team.core.mapping.ISynchronizationContext;
import org.eclipse.team.ui.mapping.ISynchronizationCompareAdapter;
import org.eclipse.team.ui.mapping.ITeamContentProviderManager;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.IExtensionStateModel;


/**
 * 
 * @author klee
 *
 */
public class ChangeLogActionProvider extends CommonActionProvider {
	private Action exampleAction;

	/**
	 * Return the configuration from the synchronize page that contains the
	 * common viewer.
	 * 
	 * @return the configuration from the synchronize page that contains the
	 *         common viewer
	 */
	protected final ISynchronizePageConfiguration getSynchronizePageConfiguration() {
		return (ISynchronizePageConfiguration) getExtensionStateModel()
				.getProperty(
						ITeamContentProviderManager.P_SYNCHRONIZATION_PAGE_CONFIGURATION);
	}

	/**
	 * Return the extension state model for the content provider associated with
	 * action provider.
	 * 
	 * @return the extension state model for the content provider associated
	 *         with action provider
	 */
	protected final IExtensionStateModel getExtensionStateModel() {
		return getActionSite().getExtensionStateModel();
	}

	/**
	 * Return the synchronization context to which the actions of this provider
	 * apply.
	 * 
	 * @return the synchronization context to which the actions of this provider
	 *         apply
	 */
	protected final ISynchronizationContext getSynchronizationContext() {
		return (ISynchronizationContext) getExtensionStateModel().getProperty(
				ITeamContentProviderManager.P_SYNCHRONIZATION_CONTEXT);
	}

	@Override
	public void init(ICommonActionExtensionSite aSite) {
		super.init(aSite);
		/*
		 * exampleAction = new Action("Prepare ChangeLog") { public void run() {
		 * StringBuffer buffer = new StringBuffer(); boolean addComma = false;
		 * IStructuredSelection selection =
		 * (IStructuredSelection)getContext().getSelection(); ResourceMapping[]
		 * mappings = getResourceMappings(selection.toArray()); for (int i = 0;
		 * i < mappings.length; i++) { ResourceMapping mapping = mappings[i];
		 * ISynchronizationCompareAdapter adapter = getCompareAdpater(mapping);
		 * if (adapter != null) {
		 * 
		 * String name = adapter.getPathString(mapping); if (addComma) {
		 * buffer.append(", "); } buffer.append(name); addComma = true; } }
		 * MessageDialog.openInformation(getActionSite().getViewSite().getShell(),
		 * "Example Action", "You have executed a third party action on the
		 * selected elements: " + buffer.toString()); } };
		 */

		exampleAction = new PrepareChangeLogAction() {
			@Override
			public void run() {
				setSelection((IStructuredSelection) getContext().getSelection());
				doRun();
			}

		};

	}

	protected ISynchronizationCompareAdapter getCompareAdapter(
			ResourceMapping mapping) {
		if (mapping != null) {
			ModelProvider provider = mapping.getModelProvider();
			if (provider != null) {
				Object o = provider
						.getAdapter(ISynchronizationCompareAdapter.class);
				if (o instanceof ISynchronizationCompareAdapter) {
					return (ISynchronizationCompareAdapter) o;
				}
			}
		}
		return null;
	}

	@Override
	public void fillContextMenu(IMenuManager menu) {
		super.fillContextMenu(menu);
		menu.add(exampleAction);
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {

		actionBars.setGlobalActionHandler(ICommonActionConstants.OPEN,
				exampleAction);
	}
}
