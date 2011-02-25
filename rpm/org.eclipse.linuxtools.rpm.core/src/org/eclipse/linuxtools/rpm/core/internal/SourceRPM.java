/*
 * (c) 2005 Red Hat, Inc.
 *
 * This program is open source software licensed under the 
 * Eclipse Public License ver. 1
 */
package org.eclipse.linuxtools.rpm.core.internal;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.linuxtools.rpm.core.ISourceRPM;

public class SourceRPM implements ISourceRPM {
    
	private IFile sourceRPM;
	private IFolder sourcesFolder;
	
	public SourceRPM(IFile sourceRPM) {
		this.sourceRPM = sourceRPM;
	}
	
	public IFile getFile() {
		return sourceRPM;
	}
	
	public IFolder getSourcesFolder() {
		return sourcesFolder;
	}
	
	public void setSourcesFolder(IFolder sourcesFolder) {
		this.sourcesFolder = sourcesFolder;
	}
}
