/*******************************************************************************
 * Copyright (c) 2015, 2017 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Red Hat Inc. - modified for use with OpenShift.io
 *******************************************************************************/

package org.eclipse.linuxtools.internal.mylyn.osio.rest.ui.provisional;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestConfiguration;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestConnector;
import org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.QueryPageDetails;
import org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.SimpleURLQueryPageSchema;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskData;

public class OSIORestUIUtil {

	protected static OSIORestSearchQueryPage createSimpleURLQueryPage(TaskData taskData,
			OSIORestConnector connectorREST, TaskRepository repository) {
		try {
			SimpleURLQueryPageSchema.getInstance().initialize(taskData);
			OSIORestConfiguration config = connectorREST.getRepositoryConfiguration(repository);
			if (config != null) {
				config.updateSpaceOptions(taskData);
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new OSIORestSearchQueryPage(Messages.OSIORestUiUtil_CreateQueryFromURL, repository, null,
				SimpleURLQueryPageSchema.getInstance(), taskData,
				new QueryPageDetails(true, "buglist.cgi?", Messages.OSIORestUiUtil_EnterQueryParameter, //$NON-NLS-1$
						Messages.OSIORestUiUtil_EnterTitleAndURL,
						"([a-zA-Z][a-zA-Z+.-]{0,10}://[a-zA-Z0-9%._~!$&?#'()*+,;:@/=-]+)", "SimpleURLQueryPage")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	protected static OSIORestSearchQueryPage createOSIORestSearchQueryPage(TaskData taskData,
			OSIORestConnector connectorREST, TaskRepository repository) {
		try {
			OSIORestSearchQueryPageSchema.getInstance().initialize(taskData);
			OSIORestConfiguration config = connectorREST.getRepositoryConfiguration(repository);
			if (config != null) {
				config.updateSpaceOptions(taskData);
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new OSIORestSearchQueryPage(Messages.OSIORestUiUtil_CreateQueryFromForm, repository, null,
				OSIORestSearchQueryPageSchema.getInstance(), taskData,
				new QueryPageDetails(true, "buglist.cgi?", Messages.OSIORestUiUtil_FillForm, //$NON-NLS-1$
						Messages.OSIORestUiUtil_enterTitleAndFillForm,
						"([a-zA-Z][a-zA-Z+.-]{0,10}://[a-zA-Z0-9%._~!$&?#'()*+,;:@/=-]+)", null)); //$NON-NLS-1$
	}

	public static OSIORestSearchQueryPage createOSIORestSearchPage(boolean simplePage, boolean updateMode,
			TaskData taskData, OSIORestConnector connectorREST, TaskRepository repository, IRepositoryQuery query) {
		OSIORestSearchQueryPage result = null;
		if (simplePage && !updateMode) {
			result = createSimpleURLQueryPage(taskData, connectorREST, repository);
		}
		if (!simplePage && !updateMode) {
			result = createOSIORestSearchQueryPage(taskData, connectorREST, repository);
		}
		if (simplePage && updateMode) {
			result = updateSimpleURLQueryPage(taskData, connectorREST, repository, query);
		}
		if (!simplePage && updateMode) {
			result = updateOSIORestSearchQueryPage(taskData, connectorREST, repository, query);
		}
		return result;
	}

	protected static OSIORestSearchQueryPage updateSimpleURLQueryPage(TaskData taskData,
			OSIORestConnector connectorREST, TaskRepository repository, IRepositoryQuery query) {
		try {
			SimpleURLQueryPageSchema.getInstance().initialize(taskData);
			connectorREST.getRepositoryConfiguration(repository).updateSpaceOptions(taskData);
			OSIORestSearchQueryPageSchema.getInstance().initialize(taskData);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new OSIORestSearchQueryPage(Messages.OSIORestUiUtil_CreateQueryFromURL, repository, query,
				SimpleURLQueryPageSchema.getInstance(), taskData,
				new QueryPageDetails(true, "buglist.cgi?", Messages.OSIORestUiUtil_EnterQueryParameters, //$NON-NLS-1$
						Messages.OSIORestUiUtil_EnterTitleAndURL1,
						"([a-zA-Z][a-zA-Z+.-]{0,10}://[a-zA-Z0-9%._~!$&?#'()*+,;:@/=-]+)", "SimpleURLQueryPage")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	protected static OSIORestSearchQueryPage updateOSIORestSearchQueryPage(TaskData taskData,
			OSIORestConnector connectorREST, TaskRepository repository, IRepositoryQuery query) {
		try {
			OSIORestSearchQueryPageSchema.getInstance().initialize(taskData);
			connectorREST.getTaskDataHandler().initializeTaskData(repository, taskData, null,
					new NullProgressMonitor());
			OSIORestSearchQueryPageSchema.getInstance().initialize(taskData);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new OSIORestSearchQueryPage(Messages.OSIORestUiUtil_CreateQueryFromForm, repository, query,
				OSIORestSearchQueryPageSchema.getInstance(), taskData,
				new QueryPageDetails(true, "buglist.cgi?", Messages.OSIORestUiUtil_fillForm, //$NON-NLS-1$
						Messages.OSIORestUiUtil_EnterTitleAndFillForm,
						"([a-zA-Z][a-zA-Z+.-]{0,10}://[a-zA-Z0-9%._~!$&?#'()*+,;:@/=-]+)", null)); //$NON-NLS-1$
	}

}
