/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.cdt.autotools;

import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.core.MakeTargetEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.internal.cdt.autotools.MakeTarget;
import org.eclipse.linuxtools.internal.cdt.autotools.MakeTargetManager;
import org.eclipse.linuxtools.internal.cdt.autotools.ProjectTargets;


public class AutotoolsMakeTargetManager extends MakeTargetManager {
	private String TARGET_BUILDER_ID = "genmakebuilder";
	
	public AutotoolsMakeTargetManager() {
	}

	public boolean hasTargetBuilder(IProject project) {
		return AutotoolsMakefileBuilder.hasTargetBuilder(project);
	}
	
	public String[] getTargetBuilders(IProject project) {
		if (hasTargetBuilder(project)) {
			String[] ids = new String[] {TARGET_BUILDER_ID};
			return ids;
		}
		return new String[0];
	}
	
	public String getBuilderID(String targetBuilderID) {
		if (targetBuilderID.equals(TARGET_BUILDER_ID))
			return AutotoolsMakefileBuilder.getBuilderId();
		return null;
	}

	// The following method is added to provide a mass MakeTarget update that doesn't
	// write out the .cdtproject file over and over again.  This should save significant
	// time when creating and building a project for the first time.
	public void addTargets(IProject project, IMakeTarget[] targets) throws CoreException {
		ProjectTargets projectTargets = (ProjectTargets)projectMap.get(project);
		if (projectTargets == null) {
			projectTargets = readTargets(project);
		}
		for (int i = 0; i < targets.length; ++i) {
			IMakeTarget target = targets[i];
			((MakeTarget) target).setContainer(project);
			try {
				projectTargets.add((MakeTarget) target);
			} catch (CoreException e) {
				// ignore duplicate entries
			}
		}
		try {
			writeTargets(projectTargets);
			for (int i = 0; i < targets.length; ++i) {
				IMakeTarget target = targets[i];
				notifyListeners(new MakeTargetEvent(this, MakeTargetEvent.TARGET_ADD, target));
			}
		} catch (CoreException e) {
			// remove all entries if we couldn't write out to the file
			for (int i = 0; i < targets.length; ++i) {
				IMakeTarget target = targets[i];
				projectTargets.remove((MakeTarget) target);
			}
			throw e;
		}
	}
}
