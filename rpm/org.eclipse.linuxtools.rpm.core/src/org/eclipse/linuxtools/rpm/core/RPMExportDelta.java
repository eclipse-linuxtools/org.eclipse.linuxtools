/*
 * (c) 2005 Red Hat, Inc.
 *
 * This program is open source software licensed under the 
 * Eclipse Public License ver. 1
 */

package org.eclipse.linuxtools.rpm.core;

import org.eclipse.core.resources.IFile;

/**
 * Represents a set of changes (delta) to be made to the RPM project
 * model during export.
 *
 */
public class RPMExportDelta {

	private IFile specFile;
	private String version;
	private String release;
	private String patchName;
	private String changelogEntry;
	
	/**
	 * Returns the ChangeLog entry associated with the export delta.
	 * @return the ChangeLog entry, or <code>null</code> if none set
	 */
	public String getChangelogEntry() {
		return changelogEntry;
	}
	
	/**
	 * Sets the ChangeLog entry associated with the export delta.
	 * @param changelogEntry the ChangeLog entry
	 */
	public void setChangelogEntry(String changelogEntry) {
		this.changelogEntry = changelogEntry;
	}
	
	/**
	 * Returns the name of the patch file associated with the export delta.
	 * @return the patch file name, or <code>null</code> if none set
	 */
	public String getPatchName() {
		return patchName;
	}
	
	/**
	 * Sets the name of the patch file associated with the export delta.
	 * @param patchName the patch file name
	 */
	public void setPatchName(String patchName) {
		this.patchName = patchName;
	}
	
	/**
	 * Returns the release associated with the export delta.
	 * @return the release
	 */
	public String getRelease() {
		return release;
	}
	
	/**
	 * Sets the release associated with the export delta.
	 * @param release the release
	 */
	public void setRelease(String release) {
		this.release = release;
	}
	
	/**
	 * Returns the workspace file handle for the spec file associated 
	 * with this export delta.
	 * @return the spec file handle
	 */
	public IFile getSpecFile() {
		return specFile;
	}
	
	/**
	 * Sets the workspace file handle for the spec file associated
	 * with this export delta.
	 * @param specFile the spec file handle
	 */
	public void setSpecFile(IFile specFile) {
		this.specFile = specFile;
	}
	
	/**
	 * Returns the the version associated with the export delta.
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}
	
	/**
	 * Sets the version associated with the export delta.
	 * @param version the version
	 */
	public void setVersion(String version) {
		this.version = version;
	}
	
}
