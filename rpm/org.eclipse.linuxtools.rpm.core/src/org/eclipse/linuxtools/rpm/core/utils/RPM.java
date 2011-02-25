/*
 * (c) 2005 Red Hat, Inc.
 *
 * This program is open source software licensed under the 
 * Eclipse Public License ver. 1
 */
package org.eclipse.linuxtools.rpm.core.utils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.rpm.core.IRPMConfiguration;
import org.eclipse.linuxtools.rpm.core.IRPMConstants;
import org.eclipse.linuxtools.rpm.core.RPMCorePlugin;
import org.eclipse.linuxtools.rpm.core.utils.internal.ShellScript;

/**
 * A utility class for executing RPM commands.
 *
 */
public class RPM {
    
	private String macroDefines;
	private String rpmCmd;
	private IRPMConfiguration config;
	
	/**
	 * Constructs a new RPM object.
	 * @param config the RPM configuration to use
	 */
    public RPM(IRPMConfiguration config) {
		this.config = config;
		rpmCmd = RPMCorePlugin.getDefault().getPluginPreferences().getString(IRPMConstants.RPM_CMD) + 
			" -v "; //$NON-NLS-1$
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
