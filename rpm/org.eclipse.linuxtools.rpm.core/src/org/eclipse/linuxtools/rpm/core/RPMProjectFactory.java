/*
 * (c) 2005 Red Hat, Inc.
 *
 * This program is open source software licensed under the 
 * Eclipse Public License ver. 1
 */

package org.eclipse.linuxtools.rpm.core;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.rpm.core.internal.Messages;
import org.eclipse.linuxtools.rpm.core.internal.RPMProject;
import org.eclipse.linuxtools.rpm.core.internal.SourceRPM;
import org.eclipse.linuxtools.rpm.core.internal.SpecFile;

/**
 * Factory class for obtaining an instance of an RPM project.
 *
 */
public class RPMProjectFactory {
	
	private RPMProjectFactory() {
	}
	
	/**
	 * Returns an instance of an RPM project given a workspace project.
	 * If the given project has an RPM nature (a source RPM was previously 
	 * imported), the RPM project model will be reconstructed.  Otherwise, 
	 * the given project will be initialized with the default properties of a
	 * new RPM project.  Note that an RPM project is not given an RPM nature 
	 * until an import operation has been completed.
	 * @param project the workspace project to use in constructing an RPM project
	 * @return an RPM project
	 * @throws CoreException if:
	 * <ul>
	 * <li>Initializing the RPM project configuration fails</li>
	 * <li>Reconstructing the existing RPM project model fails</li>
	 * </ul>
	 */
	public static IRPMProject getRPMProject(IProject project) throws CoreException {
		IRPMProject rpmProject = new RPMProject(project);
		
		if(project.hasNature(RPMProjectNature.RPM_NATURE_ID)) {
			// Construct the project's source RPM object
			String sourceRPMName = 
				project.getPersistentProperty(new QualifiedName(RPMCorePlugin.ID, IRPMConstants.SRPM_PROPERTY));
			if(sourceRPMName != null) {
				IFolder srpmsFolder = rpmProject.getConfiguration().getSrpmsFolder();
				ISourceRPM sourceRPM = new SourceRPM(srpmsFolder.getFile(sourceRPMName));
				rpmProject.setSourceRPM(sourceRPM);
			}
			else {
				String throw_message = Messages.getString("RPMCore.RPMProjectFactory.0"); //$NON-NLS-1$
				IStatus error = new Status(IStatus.ERROR, IRPMConstants.ERROR, 1,
						throw_message, null);
				throw new CoreException(error);
			}
		
			// Construct the project's spec file object
			String specFileName = 
				project.getPersistentProperty(new QualifiedName(RPMCorePlugin.ID, IRPMConstants.SPEC_FILE_PROPERTY));
			if(specFileName != null) {
				ISpecFile specFile = 
					new SpecFile(rpmProject.getConfiguration().getSpecsFolder().getFile(specFileName));
				rpmProject.setSpecFile(specFile);
			}
			else {
				String throw_message = Messages.getString("RPMCore.RPMProjectFactory.1"); //$NON-NLS-1$
				IStatus error = new Status(IStatus.ERROR, IRPMConstants.ERROR, 1,
						throw_message, null);
				throw new CoreException(error);
			}
		}
		
		return rpmProject;
	}

}
