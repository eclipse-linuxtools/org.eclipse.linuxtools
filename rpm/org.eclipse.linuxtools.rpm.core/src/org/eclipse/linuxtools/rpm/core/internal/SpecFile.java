/*
 * (c) 2005 Red Hat, Inc.
 *
 * This program is open source software licensed under the 
 * Eclipse Public License ver. 1
 */
package org.eclipse.linuxtools.rpm.core.internal;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.rpm.core.IPatch;
import org.eclipse.linuxtools.rpm.core.IRPMConstants;
import org.eclipse.linuxtools.rpm.core.ISpecFile;

public class SpecFile implements ISpecFile {
	
    private String name;
    private String version;
    private String release;
    private IPatch patch;
    private IFile specFile;
	
	private int lastSourceLine;
	private int lastPatchLine;
	private int setupLine;
	private int lastPatchMacroLine;
	private int numPatches;
    
    private String configureArgs;
    
    public SpecFile(IFile specFile) throws CoreException {
        this.specFile = specFile;
        SpecFileParser parser = new SpecFileParser(this.specFile);
        parser.parse();
        name = parser.getName();
        version = parser.getVersion();
        release = parser.getRelease();
        configureArgs = parser.getConfigureArgs();
		lastSourceLine = parser.getLastSourceLine();
		lastPatchLine = parser.getLastPatchLine();
		setupLine = parser.getSetupLine();
		lastPatchMacroLine = parser.getLastPatchMacroLine();
		numPatches = parser.getNumPatches();
    }
    
    public void addPatch(IPatch patch) {
        this.patch = patch;
    }

    public void write() throws CoreException {
		String patch_name = null;
		String patchLine = null;
		String patchMacroLine = null;
		if(patch != null) {
		   patch_name = patch.getPatchName();

		   // Figure out the format of the lines to add
		   patchLine = "Patch" + numPatches + ": " + patch_name + IRPMConstants.LINE_SEP; //$NON-NLS-1$ //$NON-NLS-2$
		   patchMacroLine = "%patch" + numPatches + " -p1" + IRPMConstants.LINE_SEP; //$NON-NLS-1$ //$NON-NLS-2$
		}
        
		if(lastPatchLine == 0) {
			if(lastSourceLine == 0) {
				lastPatchLine = setupLine;
			} 
			else {
				lastPatchLine = lastSourceLine;
			}
		}
		if(lastPatchMacroLine == 0) {
			lastPatchMacroLine = setupLine;
		}

		// Now read the spec file line by line and write it to the final 
		// spec file adding in the lines to perform the patching.
		IFile newSpecFile = 
			getFile().getParent().getFile(new Path(getFile().getName() + ".new")); //$NON-NLS-1$
		String path_to_newspecfile = newSpecFile.getLocation().toOSString();
		
		FileReader fr = null;
		try {
			fr = new FileReader(getFile().getLocation().toOSString());
		} catch(FileNotFoundException e) {
			String throw_message = Messages.getString("RPMCore.Failed_to_find_a_spec_file_at") + //$NON-NLS-1$
			  getFile().getLocation().toOSString();
			IStatus error = new Status(IStatus.ERROR, IRPMConstants.ERROR, 1,
					throw_message, null);
			throw new CoreException(error);
		}
		BufferedReader br = new BufferedReader(fr);
		FileWriter fw;

		try {
			fw = new FileWriter(path_to_newspecfile);
		} catch (IOException e) {
			String throw_message = Messages.getString(
					"RPMCore.Failed_to_open_the_output_spec_file_at__123") + //$NON-NLS-1$
					path_to_newspecfile;
			IStatus error = new Status(IStatus.ERROR, IRPMConstants.ERROR, 1, throw_message,
					null);
			throw new CoreException(error);
		}

		int line_ctr = 0;
		String input_line;
		boolean found_changelog = false;

		// Setup the lines that set the version and release numbers
		String new_version_line = "Version: " + getVersion(); //$NON-NLS-1$
		String new_release_line = "Release: " + getRelease(); //$NON-NLS-1$

		try {
			while ((input_line = br.readLine()) != null) {
				if (input_line.length() > 8) {
					if (input_line.startsWith("Version")) { //$NON-NLS-1$
						input_line = new_version_line;
					} else if (input_line.startsWith("Release")) { //$NON-NLS-1$
						input_line = new_release_line;
					}
				}

				fw.write(input_line + IRPMConstants.LINE_SEP);

				// See if this was the "%changelog" line just written, if it was, write out the new entry
				if (input_line.length() == 10 && patch != null) { //$NON-NLS-1$
					if (input_line.startsWith("%changelog")) { //$NON-NLS-1$
						fw.write(patch.getChangelogEntry());
						found_changelog = true;
					}
				}

				line_ctr++;

				// Check to see if this is one of the lines I should add something after
				if(patch != null) { //$NON-NLS-1$
				   if(line_ctr == lastPatchLine) {
					   fw.write(patchLine);
				   }
				   else if(line_ctr == lastPatchMacroLine) {
					   fw.write(patchMacroLine);
				   }
				}
			}

			// if there was not a "%changelog" section, make one
			if (!found_changelog && patch != null) { //$NON-NLS-1$
				fw.write("%changelog" + IRPMConstants.LINE_SEP + patch.getChangelogEntry()); //$NON-NLS-1$
			}

			fw.close();
		} catch (IOException e) {
			String throw_message = Messages.getString(
					"RPMCore.Error_trying_to_modify__132") + //$NON-NLS-1$
				getFile().getLocation().toOSString();
			IStatus error = new Status(IStatus.ERROR, IRPMConstants.ERROR, 1, throw_message,
					null);
			throw new CoreException(error);
		}
		
		newSpecFile.refreshLocal(IResource.DEPTH_INFINITE, null);
		getFile().delete(false, true, null);
		newSpecFile.move(getFile().getFullPath(), false, false, null);
		getFile().refreshLocal(IResource.DEPTH_INFINITE, null);
    }
    
    public IFile getFile() {
        return specFile;
    }

    public String getConfigureArgs() {
        return configureArgs;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getRelease() {
        return release;
    }

    public void setRelease(String release) {
        this.release = release;
    }
}
