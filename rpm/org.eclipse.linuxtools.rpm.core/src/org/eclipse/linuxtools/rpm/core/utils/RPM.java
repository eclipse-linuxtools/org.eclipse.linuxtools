/*******************************************************************************
 * Copyright (c) 2005, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.core.utils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.linuxtools.rpm.core.IRPMConfiguration;
import org.eclipse.linuxtools.rpm.core.IRPMConstants;
import org.eclipse.linuxtools.rpm.core.RPMCorePlugin;
import org.eclipse.linuxtools.rpm.core.internal.utils.ShellScript;

/**
 * A utility class for executing RPM commands.
 *
 */
public class RPM {
    
	private String macroDefines;
	private String rpmCmd;
	
	/**
	 * Constructs a new RPM object.
	 * @param config the RPM configuration to use
	 */
    public RPM(IRPMConfiguration config) {
    	IEclipsePreferences node = new DefaultScope().getNode(RPMCorePlugin.ID);
		rpmCmd = node.get(IRPMConstants.RPM_CMD, "") + 	" -v "; //$NON-NLS-1$ //$NON-NLS-2$
		System.out.println("Rpm cmd1:"+rpmCmd); //$NON-NLS-1$
		macroDefines = " --define '_sourcedir " + //$NON-NLS-1$
			config.getSourcesFolder().getLocation().toOSString() + "' "; //$NON-NLS-1$
		macroDefines += "--define '_srcrpmdir " + //$NON-NLS-1$
			config.getSrpmsFolder().getLocation().toOSString() + "' "; //$NON-NLS-1$
		macroDefines += "--define '_builddir " + //$NON-NLS-1$
			config.getBuildFolder().getLocation().toOSString() + "' "; //$NON-NLS-1$
		macroDefines += "--define '_rpmdir " + //$NON-NLS-1$
			config.getRpmsFolder().getLocation().toOSString() + "' "; //$NON-NLS-1$
		macroDefines += "--define '_specdir " + //$NON-NLS-1$
			config.getSpecsFolder().getLocation().toOSString() + "' "; //$NON-NLS-1$
    }
    
	/**
	 * Installs a given source RPM 
	 * @param sourceRPM
	 * @throws CoreException
	 */
    public void install(IFile sourceRPM) throws CoreException {
        String command = rpmCmd;
		command += macroDefines;
        command += " -i " + sourceRPM.getLocation().toOSString(); //$NON-NLS-1$
        ShellScript script = new ShellScript(command, 0);
		script.exec();
    }
}
