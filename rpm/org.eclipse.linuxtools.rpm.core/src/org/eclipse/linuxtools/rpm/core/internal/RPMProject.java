/*
 * (c) 2005 Red Hat, Inc.
 *
 * This program is open source software licensed under the 
 * Eclipse Public License ver. 1
 */
package org.eclipse.linuxtools.rpm.core.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.rpm.core.IRPMConfiguration;
import org.eclipse.linuxtools.rpm.core.IRPMConstants;
import org.eclipse.linuxtools.rpm.core.IRPMProject;
import org.eclipse.linuxtools.rpm.core.ISourceRPM;
import org.eclipse.linuxtools.rpm.core.RPMCorePlugin;
import org.eclipse.linuxtools.rpm.core.RPMExportDelta;
import org.eclipse.linuxtools.rpm.core.RPMProjectNature;
import org.eclipse.linuxtools.rpm.core.utils.RPM;
import org.eclipse.linuxtools.rpm.core.utils.RPMBuild;

public class RPMProject implements IRPMProject {

	private IProject project;
	private ISourceRPM sourceRPM;
	private IFile specFile;
	private IRPMConfiguration rpmConfig;

	public RPMProject(IProject project) throws CoreException {
		this.project = project;
		rpmConfig = new RPMConfiguration(this.project);
	}

	public IProject getProject() {
		return project;
	}

	public ISourceRPM getSourceRPM() {
		return sourceRPM;
	}

	public void setSourceRPM(ISourceRPM sourceRPM) throws CoreException {
		this.sourceRPM = sourceRPM;
		getProject()
				.setPersistentProperty(
						new QualifiedName(RPMCorePlugin.ID,
								IRPMConstants.SRPM_PROPERTY),
						sourceRPM.getFile().getName());
	}

	public IRPMConfiguration getConfiguration() {
		return rpmConfig;
	}

	public IFile getSpecFile() {
		return specFile;
	}

	public void setSpecFile(IFile specFile) throws CoreException {
		this.specFile = specFile;
		getProject().setPersistentProperty(
				new QualifiedName(RPMCorePlugin.ID,
						IRPMConstants.SPEC_FILE_PROPERTY), specFile.getName());
	}

	public void importSourceRPM(File externalFile) throws CoreException {
		// Copy original SRPM to workspace
		IFile srpmFile = getConfiguration().getSrpmsFolder().getFile(
				externalFile.getName());
		try {
			srpmFile.create(new FileInputStream(externalFile), false, null);
		} catch (FileNotFoundException e) {
			String throw_message = Messages
					.getString("RPMCore.Error_trying_to_copy__") + //$NON-NLS-1$
					rpmConfig.getSpecsFolder().getLocation().toOSString();
			IStatus error = new Status(IStatus.ERROR, IRPMConstants.ERROR, 1,
					throw_message, null);
			throw new CoreException(error);
		}
		setSourceRPM(new SourceRPM(srpmFile));

		// Install the SRPM
		RPM rpm = new RPM(getConfiguration());
		rpm.install(getSourceRPM().getFile());
		getProject().refreshLocal(IResource.DEPTH_INFINITE, null);

		// Set the spec file
		IResource[] installedSpecs = {};
		installedSpecs = getConfiguration().getSpecsFolder().members();
		if (installedSpecs.length != 1) {
			String throw_message = Messages
					.getString("RPMCore.spec_file_ambiguous") + //$NON-NLS-1$
					rpmConfig.getSpecsFolder().getLocation().toOSString();
			IStatus error = new Status(IStatus.ERROR, IRPMConstants.ERROR, 1,
					throw_message, null);
			throw new CoreException(error);
		}
		setSpecFile(getConfiguration().getSpecsFolder().getFile(
				installedSpecs[0].getName()));

		// Prepare the sources
		buildPrep();
		getProject().refreshLocal(IResource.DEPTH_INFINITE, null);

		// Copy sources from build root
		copySources();

		// Set the project nature
		RPMProjectNature.addRPMNature(getProject(), null);

		// Generate and store project checksum
		long checksum = generateProjectChecksum(getProject().getLocation()
				.toOSString(), 0);
		getProject().setPersistentProperty(
				new QualifiedName(RPMCorePlugin.ID,
						IRPMConstants.CHECKSUM_PROPERTY),
				new Long(checksum).toString());
	}

	public void buildAll(RPMExportDelta exportOp) throws CoreException {
		prepareExport(exportOp);
		RPMBuild rpmbuild = new RPMBuild(getConfiguration());
		rpmbuild.buildAll(getSpecFile());

		getConfiguration().getBuildFolder().refreshLocal(
				IResource.DEPTH_INFINITE, null);
		getConfiguration().getRpmsFolder().refreshLocal(
				IResource.DEPTH_INFINITE, null);
		getConfiguration().getSrpmsFolder().refreshLocal(
				IResource.DEPTH_INFINITE, null);
		buildPrep();
		copySources();
	}

	public void buildBinaryRPM(RPMExportDelta exportOp) throws CoreException {
		prepareExport(exportOp);
		RPMBuild rpmbuild = new RPMBuild(getConfiguration());
		rpmbuild.buildBinary(getSpecFile());

		getConfiguration().getBuildFolder().refreshLocal(
				IResource.DEPTH_INFINITE, null);
		getConfiguration().getRpmsFolder().refreshLocal(
				IResource.DEPTH_INFINITE, null);
	}

	public void buildSourceRPM(RPMExportDelta exportOp) throws CoreException {
		prepareExport(exportOp);
		RPMBuild rpmbuild = new RPMBuild(getConfiguration());
		rpmbuild.buildSource(getSpecFile());

		getConfiguration().getBuildFolder().refreshLocal(
				IResource.DEPTH_INFINITE, null);
		getConfiguration().getSrpmsFolder().refreshLocal(
				IResource.DEPTH_INFINITE, null);
		buildPrep();
		copySources();
	}

	public void buildPrep() throws CoreException {
		RPMBuild rpmbuild = new RPMBuild(getConfiguration());
		rpmbuild.buildPrep(getSpecFile());
		getConfiguration().getBuildFolder().refreshLocal(
				IResource.DEPTH_INFINITE, null);
		IResource[] sources = getConfiguration().getBuildFolder().members();
		// If there is one folder, assume it contains all the sources
		if (sources.length == 1 && sources[0].getType() == IResource.FOLDER) {
			IFolder foo = getProject().getFolder(
					sources[0].getProjectRelativePath());
			getSourceRPM().setSourcesFolder(foo);
		}
	}

	/**
	 * Copies sources from the project's BUILD directory to the project root.
	 * 
	 * @throws CoreException
	 *             if copying fails
	 */
	private void copySources() throws CoreException {
		// Copy all sources to the project root
		IResource[] sources = null;
		if (getSourceRPM().getSourcesFolder() != null) {
			sources = getSourceRPM().getSourcesFolder().members();
		} else {
			getConfiguration().getBuildFolder().members();
		}
		for (int i = 0; i < sources.length; i++) {
			IPath path = getProject().getFullPath().addTrailingSeparator();
			path = path.append(sources[i].getName());
			if (sources[i].getType() == IResource.FILE) {
				IFile oldFile = getProject().getParent().getFile(path);
				IFile newFile = getProject().getFile(
						sources[i].getProjectRelativePath());
				if (oldFile.exists()) {
					oldFile.setContents(newFile.getContents(), false, true,
							null);
				} else {
					sources[i].copy(path, false, null);
				}
			} else if (sources[i].getType() == IResource.FOLDER) {
				IFolder oldDir = getProject().getParent().getFolder(path);
				if (oldDir.exists()) {
					oldDir.delete(false, true, null);
				}
				sources[i].copy(path, false, null);
			}
		}
		getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
	}

	/**
	 * Prepares for project export. This method updates the project model with
	 * the given RPM project export delta by:
	 * <ul>
	 * <li>Parsing the given spec file and updating the model accordingly</li>
	 * <li>Updating the spec file model and writing it to disk</li>
	 * <li>Determining if a patch is needed and generating a patch</li>
	 * </ul>
	 * 
	 * @param exportOp
	 *            the export delta
	 * @throws CoreException
	 *             if:
	 *             <ul>
	 *             <li>The project does not have an RPM nature</li>
	 *             <li>Parsing the spec file fails</li>
	 *             <li>Patch generation fails</li>
	 *             <li>Writing the spec file fails</li>
	 *             </ul>
	 */
	private void prepareExport(RPMExportDelta exportOp) throws CoreException {
		/* Don't support exporting projects that have not been imported as SRPMs */
		if (!getProject().hasNature(RPMProjectNature.RPM_NATURE_ID)) {
			String throw_message = Messages
					.getString("RPMCore.RPMProject.prepareExport") + //$NON-NLS-1$
					getProject().getName();
			IStatus error = new Status(IStatus.ERROR, IRPMConstants.ERROR, 1,
					throw_message, null);
			throw new CoreException(error);
		}

		// We need to reset the spec file (which may be user-defined)
		if (exportOp.getSpecFile() != null
				&& !getSpecFile().getProjectRelativePath().equals(
						exportOp.getSpecFile().getProjectRelativePath())) {
			setSpecFile(exportOp.getSpecFile());
		} else {
			setSpecFile(getSpecFile());
		}

		// Do a buildPrep again to make sure the BUILD folder is pristine
		buildPrep();
		// Generate and store new project checksum
		long checksum = generateProjectChecksum(getProject().getLocation()
				.toOSString(), 0);
		getProject().setPersistentProperty(
				new QualifiedName(RPMCorePlugin.ID,
						IRPMConstants.CHECKSUM_PROPERTY),
				new Long(checksum).toString());
		// write changes to spec file on disk

		getConfiguration().getSourcesFolder().refreshLocal(
				IResource.DEPTH_INFINITE, null);
		getConfiguration().getSpecsFolder().refreshLocal(
				IResource.DEPTH_INFINITE, null);
	}

	/**
	 * Generates the checksum for a given project path.
	 * 
	 * @param project_path
	 *            the absolute path of the project
	 * @param proj_checksum
	 *            input 0
	 * @return
	 * @throws CoreException
	 *             if the operation fails
	 */
	private long generateProjectChecksum(String project_path, long proj_checksum)
			throws CoreException {
		File dir = new File(project_path);

		if (dir.isDirectory()) {
			String[] children = dir.list();

			for (int i = 0; i < children.length; i++) {

				File temp = new File(project_path + IRPMConstants.FILE_SEP
						+ children[i]);

				if (temp.isDirectory()) {
					IFolder folder = getProject().getFolder(
							new Path(children[i]));
					if (!folder.isDerived()) {
						proj_checksum = generateProjectChecksum(project_path
								+ IRPMConstants.FILE_SEP + children[i],
								proj_checksum);
					}
				} else {
					IFile file = getProject().getFile(new Path(children[i]));
					if (!file.isDerived()
							|| file.getProjectRelativePath().equals(
									getSpecFile().getProjectRelativePath())) {
						proj_checksum += generateFileCheckSum(temp);
					}
					if (children[i].equals("Makefile") & !getProject().getFile("configure").exists()) { //$NON-NLS-1$ //$NON-//$NON-NLS-2$
						proj_checksum += generateFileCheckSum(temp);
					}
				}
			}
		}

		return proj_checksum;
	}

	private long generateFileCheckSum(File input) throws CoreException {
		String input_line;
		long chksum = 0;
		try {
			BufferedReader br = new BufferedReader(new FileReader(input
					.toString()));
			while ((input_line = br.readLine()) != null) {
				for (int i = 0; i < input_line.length(); i++)
					chksum += input_line.charAt(i);
			}
			br.close();
		} catch (FileNotFoundException e) {
			String throw_message = Messages.getString("RPMCore.0") + //$NON-NLS-1$
					input.getName();
			IStatus error = new Status(IStatus.ERROR, IRPMConstants.ERROR, 1,
					throw_message, null);
			throw new CoreException(error);
		} catch (IOException e) {
			String throw_message = Messages.getString("RPMCore.0") + //$NON-NLS-1$
					input.getName();
			IStatus error = new Status(IStatus.ERROR, IRPMConstants.ERROR, 1,
					throw_message, null);
			throw new CoreException(error);
		}
		return chksum;
	}
}
