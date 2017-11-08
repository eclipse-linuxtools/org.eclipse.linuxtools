/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.mylyn.osio.rest.ui.provisional;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestConfiguration;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestConnector;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.ui.OSIORestUIPlugin;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.osgi.util.NLS;

public class OSIORestSearchQueryPage extends RepositoryQuerySchemaPage {
	
	public OSIORestSearchQueryPage(String pageName, TaskRepository repository, IRepositoryQuery query,
			AbstractQueryPageSchema schema, TaskData data, QueryPageDetails pageDetails) {
		super(pageName, repository, query, schema, data, pageDetails);
		if (query != null) {
			setTitle(NLS.bind(Messages.OSIORestSearchQueryPage_PropertiesForQuery, query.getSummary()));
		} else {
			setTitle(Messages.OSIORestSearchQueryPage_PropertiesForNewQuery);
		}
	}

	@Override
	protected void doRefreshControls() {
		try {
			OSIORestConnector connectorREST = (OSIORestConnector) getConnector();
			connectorREST.getRepositoryConfiguration(getTaskRepository()).updateSpaceOptions(getTargetTaskData());

			for (Entry<String, AbstractAttributeEditor> entry : editorMap.entrySet()) {
				entry.getValue().refresh();
			}
		} catch (CoreException e) {
			StatusHandler.log(new Status(IStatus.ERROR, OSIORestUIPlugin.PLUGIN_ID,
					"OSIORestSearchQueryPage could not refresh!", e)); //$NON-NLS-1$
		}
	}

	@Override
	protected boolean hasRepositoryConfiguration() {
		return getRepositoryConfiguration() != null;
	}

	private OSIORestConfiguration getRepositoryConfiguration() {
		try {
			return ((OSIORestConnector) getConnector()).getRepositoryConfiguration(getTaskRepository());
		} catch (CoreException e) {
			StatusHandler.log(new Status(IStatus.ERROR, OSIORestUIPlugin.PLUGIN_ID,
					"OSIORestSearchQueryPage could get the RepositoryConfiguration!", e)); //$NON-NLS-1$
		}
		return null;
	}

	@Override
	protected boolean restoreState(IRepositoryQuery query) {
		if (query != null) {
			try {
				restoreStateFromUrl(query.getUrl());
				doRefreshControls();
				return true;
			} catch (UnsupportedEncodingException e) {
				// ignore
			}
		}
		return false;
	}

	private void restoreStateFromUrl(String queryUrl) throws UnsupportedEncodingException {
		queryUrl = queryUrl.substring(queryUrl.indexOf("?") + 1); //$NON-NLS-1$
		String[] options = queryUrl.split("&"); //$NON-NLS-1$
		for (String option : options) {
			String key;
			int endindex = option.indexOf("="); //$NON-NLS-1$
			if (endindex == -1) {
				key = null;
			} else {
				key = option.substring(0, option.indexOf("=")); //$NON-NLS-1$
			}
			if (key == null || key.equals("order")) { //$NON-NLS-1$
				continue;
			}
			String value = URLDecoder.decode(option.substring(option.indexOf("=") + 1), //$NON-NLS-1$
					getTaskRepository().getCharacterEncoding());
			TaskAttribute attr = getTargetTaskData().getRoot().getAttribute(key);
			if (attr != null) {
				if (getTargetTaskData().getRoot().getAttribute(key).getValue().equals("")) { //$NON-NLS-1$
					getTargetTaskData().getRoot().getAttribute(key).setValue(value);
				} else {
					getTargetTaskData().getRoot().getAttribute(key).addValue(value);
				}
			}
		}
	}

	@Override
	protected String getQueryUrl(String repositoryUrl) {
		return getQueryURL(repositoryUrl, getQueryParameters());
	}

	private StringBuilder getQueryParameters() {
		StringBuilder sb = new StringBuilder();
		for (Entry<String, AbstractAttributeEditor> entry : editorMap.entrySet()) {
			TaskAttribute attrib = getTargetTaskData().getRoot().getAttribute(entry.getKey());
			for (String string : attrib.getValues()) {
				if (string != null && !string.equals("")) { //$NON-NLS-1$
					try {
						appendToBuffer(sb, entry.getKey() + "=", //$NON-NLS-1$
								URLEncoder.encode(string.replaceAll(" ", "%20"), //$NON-NLS-1$//$NON-NLS-2$
										getTaskRepository().getCharacterEncoding()).replaceAll("%2520", "%20")); //$NON-NLS-1$ //$NON-NLS-2$
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		return sb;
	}

	private String getQueryURL(String repositoryUrl, StringBuilder params) {
		StringBuilder url = new StringBuilder(getQueryURLStart(repositoryUrl));
		url.append(params);
		return url.toString();
	}

	/**
	 * Creates the bugzilla query URL start. Example: https://bugs.eclipse.org/bugs/buglist.cgi?
	 */
	private String getQueryURLStart(String repositoryUrl) {
		return repositoryUrl + (repositoryUrl.endsWith("/") ? "" : "/") + "/search?"; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$//$NON-NLS-4$
	}

	private void appendToBuffer(StringBuilder sb, String key, String value) {
		if (sb.length() > 0) {
			sb.append('&');
		}
		sb.append(key);
		sb.append(value);
	}

}