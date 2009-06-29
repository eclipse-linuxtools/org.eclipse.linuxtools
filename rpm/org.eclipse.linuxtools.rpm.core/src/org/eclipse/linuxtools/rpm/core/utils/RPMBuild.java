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
 * A utility class for executing rpmbuild commands.
 *
 */
public class RPMBuild {
    
	private String macroDefines;
	
	private String rpmBuildCmd;
    
	/**
	 * Constructs a new object.
	 * @param config the RPM configuration to use
	 */
    public RPMBuild(IRPMConfiguration config) {
    	IEclipsePreferences node = new DefaultScope().getNode(RPMCorePlugin.ID);
		rpmBuildCmd = node.get(IRPMConstants.RPMBUILD_CMD, "") + " -v "; //$NON-NLS-1$ //$NON-NLS-2$
		macroDefines = " --define '_sourcedir " +  //$NON-NLS-1$
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
	 * Prepares the sources for a given spec file.
	 * @param specFile the spec file
	 * @throws CoreException if the operation fails
	 */
    public void buildPrep(IFile specFile) throws CoreException {
        String command = rpmBuildCmd;
        command += macroDefines;
        command += " -bp " + specFile.getLocation().toOSString(); //$NON-NLS-1$
        ShellScript script = new ShellScript(command, 0);
		script.exec();
    }
	
	/**
	 * Builds a binary RPM for a given spec file.
	 * @param specFile the spec file
	 * @throws CoreException if the operation fails
	 */
	public void buildBinary(IFile specFile) throws CoreException {
		String command = rpmBuildCmd;
        command += macroDefines;
        command += " -bb " + specFile.getLocation().toOSString(); //$NON-NLS-1$
        ShellScript script = new ShellScript(command, 0);
		script.exec();
	}
	
	/**
	 * Rebuilds a binary RPM from a given source RPM.
	 * @param sourceRPM the source RPM
	 * @throws CoreException if the operation fails
	 */
    public void rebuild(IFile sourceRPM) throws CoreException {
        String command = rpmBuildCmd;
        command += macroDefines;
        command += " --rebuild " + sourceRPM.getLocation().toOSString(); //$NON-NLS-1$
        ShellScript script = new ShellScript(command, 0);
		script.exec();
    }
	
	/**
	 * Builds both a binary and source RPM for a given spec file.
	 * @param specFile the spec file
	 * @throws CoreException if the operation fails
	 */
    public void buildAll(IFile specFile) throws CoreException {
        String command = rpmBuildCmd;
        command += macroDefines;
        command += " -ba " + specFile.getLocation().toOSString(); //$NON-NLS-1$
        ShellScript script = new ShellScript(command, 0);
		script.exec();
    }
	
	/**
	 * Builds a source RPM for a given spec file.
	 * @param specFile the spec file
	 * @throws CoreException if the operation fails
	 */
    public void buildSource(IFile specFile) throws CoreException {
        String command = rpmBuildCmd;
        command += macroDefines;
        command += " -bs " + specFile.getLocation().toOSString(); //$NON-NLS-1$
        ShellScript script = new ShellScript(command, 0);
		script.exec();
    }
}
