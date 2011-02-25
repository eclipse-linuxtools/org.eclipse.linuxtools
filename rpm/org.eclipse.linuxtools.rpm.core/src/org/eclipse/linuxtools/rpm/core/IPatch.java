/*
 * (c) 2005 Red Hat, Inc.
 *
 * This program is open source software licensed under the 
 * Eclipse Public License ver. 1
 */

package org.eclipse.linuxtools.rpm.core;

import org.eclipse.core.resources.IFile;

/** 
 * Represents a patch to a source RPM.
 */
public interface IPatch {

	/**
	 * Returns the spec file ChangeLog entry associated with this patch.
	 * @return the ChangeLog entry, or <code>null</code> if not set
	 */
	public String getChangelogEntry();
	
	/**
	 * Sets the spec file ChangeLog entry associated with this patch.
	 * @param changelogEntry the ChangeLog entry
	 */
	public void setChangelogEntry(String changelogEntry);
	
	/**
	 * Returns the workspace patch file that contains the source code diffs
	 * associated with this patch.
	 * @return the patch file, or <code>null</code> if not set
	 */
	public IFile getFile();
	
	/**
	 * Sets the workspace patch file that contains the source code diffs 
	 * associated with this patch.
	 * @param file the patch file
	 */
	public void setFile(IFile file);
	
	/**
	 * Returns the name of the patch file.
	 * @return the patch file name, or <code>null</code> if not set.
	 */
	public String getPatchName();
	
	/**
	 * Sets the name of the patch file.  Note that if the workspace patch file has 
	 * already been set using <code>setFile</code>, this method will not modify the 
	 * name of the patch file on disk.
	 * @param patchName the name of the patch file
	 */
	public void setPatchName(String patchName);
	
}
