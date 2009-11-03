/*******************************************************************************
 * Copyright (c) 2007 Red Hat Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial implementation
 *******************************************************************************/
package org.eclipse.linuxtools.cdt.autotools;

import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.internal.core.Builder;
import org.eclipse.cdt.managedbuilder.internal.core.ToolChain;
import org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.cdt.autotools.ui.properties.AutotoolsPropertyConstants;



// Proxy class for IBuilder to allow overriding of getBuildLocation().

@SuppressWarnings("restriction")
public class AutotoolsBuilder extends Builder {

	private String buildPath;
	private IProject project;
	
	public AutotoolsBuilder(IBuilder builder, IProject project, ToolChain toolChain) {
		super(toolChain, builder.getId(), builder.getName(), (Builder)builder);
		this.project = project;
	}
	
	protected IProject getProject() {
		return project;
	}
	

	public String getBuildPath() {
		// TODO Auto-generated method stub
		return buildPath;
	}


	public void setBuildPath(String path) {
		// TODO Auto-generated method stub
		this.buildPath = path;
	}

	public String getCleanBuildTarget() {
		String target = null;
		try {
			target = getProject().getPersistentProperty(AutotoolsPropertyConstants.CLEAN_MAKE_TARGET);
		} catch (CoreException ce) {
			// do nothing
		}
		if (target == null)
			target = AutotoolsPropertyConstants.CLEAN_MAKE_TARGET_DEFAULT;
		return target;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IBuilder#getBuildFileGenerator()
	 */
	public IManagedBuilderMakefileGenerator getBuildFileGenerator() {
		return new MakeGenerator();
	}

}
