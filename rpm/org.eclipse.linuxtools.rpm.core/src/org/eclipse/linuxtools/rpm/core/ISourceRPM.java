/*
 * (c) 2005 Red Hat, Inc.
 *
 * This program is open source software licensed under the 
 * Eclipse Public License ver. 1
 */
package org.eclipse.linuxtools.rpm.core;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;

/**
 * Represents a source RPM in an RPM project.
 *
 */
public interface ISourceRPM {
	
	/**
	 * Returns the workspace file associated with the source RPM.
	 * @return the source RPM file
	 */
	public IFile getFile();
	
	/**
	 * Returns the folder contained within the RPM BUILD directory 
	 * that contains the project sources.  Typically, this is the 
	 * directory that is installed in the BUILD directory after unpacking
	 * the project sources during source preparation.
	 * @return the source RPM sources folder, or <code>null</code> if 
	 * none exists
	 */
	public IFolder getSourcesFolder();
	
	/**
	 * Sets the folder contained within the RPM BUILD directory that
	 * contains the project sources.  Typically, this is the directory
	 * that is installed in the BUILD directory after unpacking the 
	 * project sources during source preparation.
	 * @param folder the source RPM sources folder
	 */
	public void setSourcesFolder(IFolder folder);
	
}
