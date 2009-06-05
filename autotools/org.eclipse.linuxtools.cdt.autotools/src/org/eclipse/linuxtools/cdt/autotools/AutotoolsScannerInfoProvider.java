/*******************************************************************************
 * Copyright (c) 2006, 2007 Red Hat Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.cdt.autotools;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.cdt.core.AbstractCExtension;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoChangeListener;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.cdt.autotools.ui.properties.AutotoolsPropertyConstants;


public class AutotoolsScannerInfoProvider extends AbstractCExtension implements IScannerInfoProvider {

	static private Map infoCollections = new HashMap();
	static public final String INTERFACE_IDENTITY = 
		AutotoolsPlugin.PLUGIN_ID + "." + "AutotoolsScannerInfoProvider"; // $NON-NLS-1$

	protected String getCollectionName(IProject project) {
		IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
		String config = project.getName() + "." + buildInfo.getConfigurationName();
		return config;
	}
	
	public synchronized IScannerInfo getScannerInformation(IResource resource) {
		IResource res = AutotoolsScannerInfo.followIncludeChain(resource);
		// We keep scanner info separate per configuration.
		// This is because one configuration may result in a file being
		// compiled with one include path while another configuration
		// may change the include path / defined symbols.
		IProject project = resource.getProject();
		
		// We punt for non-Autotools projects.  This ScannerInfoProvider is used for
		// all C Projects and we might get called for a non-Autotools project (e.g.
		// unsubscribe operation when converting to C project).
		if (!AutotoolsMakefileBuilder.hasTargetBuilder(project))
			return null;
		
		// Check if the scanner info has been marked dirty, in which case we need
		// to mark all entries dirty.
		Boolean isDirty = Boolean.FALSE;
		try {
			isDirty = (Boolean)project.getSessionProperty(AutotoolsPropertyConstants.SCANNER_INFO_DIRTY);
		} catch (CoreException e) {
			// do nothing
		}
		if (isDirty != null && isDirty.equals(Boolean.TRUE)) {
			setDirty(project);
			try {
			project.setSessionProperty(AutotoolsPropertyConstants.SCANNER_INFO_DIRTY, Boolean.FALSE);
			} catch (CoreException ce) {
				// do nothing
			}
		}
		String config = getCollectionName(project);
		// Get the ScannerInfo collection for current configuration or else
		// create an empty collection if one doesn't already exist.
		Map infoCollection = (Map)infoCollections.get(config);
		if (infoCollection == null) {
			infoCollection = new HashMap();
			infoCollections.put(config, infoCollection);
		}
		AutotoolsScannerInfo info = (AutotoolsScannerInfo)infoCollection.get(res);
		if (info == null) {
			info = new AutotoolsScannerInfo(res);
			infoCollection.put(res, info);
		}
		return info; 
	}

	private void setDirty(IProject project) {
		String config = getCollectionName(project);
		Map infoCollection = (Map)infoCollections.get(config);
		if (infoCollection != null) {
			Collection s = infoCollection.values();
			for (Iterator i = s.iterator(); i.hasNext();) {
				AutotoolsScannerInfo info = (AutotoolsScannerInfo)i.next();
				info.setDirty(true);
			}
		}
	}
	
	public void subscribe(IResource resource,
			IScannerInfoChangeListener listener) {
		AutotoolsScannerInfo info = (AutotoolsScannerInfo)getScannerInformation(resource);
		info.addListener(listener);
	}

	public void unsubscribe(IResource resource,
			IScannerInfoChangeListener listener) {
		AutotoolsScannerInfo info = (AutotoolsScannerInfo)getScannerInformation(resource);
		if (info != null)
			info.removeListener(listener);
	}
}
