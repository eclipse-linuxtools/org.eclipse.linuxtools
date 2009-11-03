/*******************************************************************************
 * Copyright (c) 2007, 2009 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.cdt.autotools.ui.properties;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.ui.newui.AbstractPage;
import org.eclipse.cdt.ui.newui.CDTPropertyManager;
import org.eclipse.linuxtools.internal.cdt.autotools.core.IConfigurationCloneListener;
import org.eclipse.linuxtools.internal.cdt.autotools.core.configure.AutotoolsConfigurationManager;
import org.eclipse.linuxtools.internal.cdt.autotools.core.configure.IAConfiguration;

public class AutotoolsConfigurePropertyPage extends AbstractPage implements IConfigurationCloneListener {
	
	protected boolean isSingle() {
		return true;
	}
	
	// Keep a private list of cloned and created Autotools configurations.  If the user
	// cancels, these won't end up connected to the project.
	private static Map<String, IAConfiguration> cfgs = new HashMap<String, IAConfiguration>();
	
	
	/**
	 * Default constructor
	 */
	public AutotoolsConfigurePropertyPage() {
		super();
		if (CDTPropertyManager.getPagesCount() == 0) {
			cfgs.clear();
		}
	}
	
	public void cloneCfg(String cloneName, IConfiguration c) {
		// First verify that the configuration cloning is for this project
		if (!c.getManagedProject().getOwner().getName().equals(getProject().getName()))
			return;
		// Find config to clone or create it if we haven't seen it before
		IAConfiguration clonee = cfgs.get(cloneName);
		if (clonee == null) {
			AutotoolsConfigurationManager.getInstance().getConfiguration(getProject(), cloneName, false);
		}
		String newName = c.getName();
		IAConfiguration newCfg = clonee.copy(newName);
		cfgs.put(newName, newCfg);
	}
	
	public IAConfiguration getConfiguration(String name) {
		IAConfiguration acfg = cfgs.get(name);
		if (acfg == null)
			acfg = 
				AutotoolsConfigurationManager.getInstance().getConfiguration(getProject(), 
						name, false);
		cfgs.put(name, acfg);
		return acfg;
	}
	
	
	protected void cfgChanged(ICConfigurationDescription _cfgd) {
		// Let super update all pages
		super.cfgChanged(_cfgd);
	}
	
}

