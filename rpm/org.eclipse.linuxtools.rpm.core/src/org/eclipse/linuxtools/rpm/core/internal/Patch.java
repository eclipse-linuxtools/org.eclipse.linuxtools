/*
 * (c) 2005 Red Hat, Inc.
 *
 * This program is open source software licensed under the 
 * Eclipse Public License ver. 1
 */

package org.eclipse.linuxtools.rpm.core.internal;

import org.eclipse.core.resources.IFile;
import org.eclipse.linuxtools.rpm.core.IPatch;

public class Patch implements IPatch {

	private String changelogEntry;
	private String patchName;
	private IFile file;
	
	public String getChangelogEntry() {
		return changelogEntry;
	}
	public void setChangelogEntry(String changelogEntry) {
		this.changelogEntry = changelogEntry;
	}
	public IFile getFile() {
		return file;
	}
	public void setFile(IFile file) {
		this.file = file;
	}
	public String getPatchName() {
		return patchName;
	}
	public void setPatchName(String patchName) {
		this.patchName = patchName;
	}

}
