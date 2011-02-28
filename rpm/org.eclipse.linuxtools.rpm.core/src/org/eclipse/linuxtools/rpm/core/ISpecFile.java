/*
 * (c) 2005 Red Hat, Inc.
 *
 * This program is open source software licensed under the 
 * Eclipse Public License ver. 1
 */
package org.eclipse.linuxtools.rpm.core;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

/**
 * Represents a spec file in an RPM project.
 *
 */
public interface ISpecFile {
    
	/**
	 * Adds a patch to the spec file.  Changes to the spec file are not
	 * reflected on-disk until the write method is called.
	 * @param patch the patch to add
	 */
    public void addPatch(IPatch patch);
    
	/**
	 * Returns the workspace file handle for the spec file.
	 * @return the spec file handle
	 */
    public IFile getFile();

	/**
	 * Returns the spec file's configure arguments.  For projects that 
	 * contain a <code>configure</code> script, the spec file typically 
	 * contains a <code>%configure</code> directive followed by a list
	 * of arguments.  Not all spec files contain this directive.
	 * @return the configure arguments, or <code>null</code> if none present
	 */
    public String getConfigureArgs();
    
	/**
	 * Returns the name of the RPM project according to the spec file's
	 * directives.
	 * @return the RPM project name
	 */
    public String getName();
    
	/**
	 * Sets the name of the RPM project.  Changes to the spec file are
	 * not reflected on-disk until the write method is called.
	 * @param name the RPM project name
	 */
    public void setName(String name);
    
	/**
	 * Returns the version of the RPM project according to the spec file's
	 * directives.
	 * @return the RPM project version
	 */
    public String getVersion();
    
	/**
	 * Sets the version of the RPM project.  Changes to the spec file
	 * are not reflected on-disk until the write method is called.
	 * @param version the RPM project version
	 */
    public void setVersion(String version);
    
	/**
	 * Returns the release of the RPM project according to the spec 
	 * file's directives.
	 * @return the RPM project release
	 */
    public String getRelease();
    
	/**
	 * Sets the release of the RPM proejct.  Changes to the spec file
	 * are not reflected on-disk until the write method is called.
	 * @param release the RPM project release
	 */
    public void setRelease(String release);
	
	/**
	 * Writes the current spec file model to disk.
	 * @throws CoreException if:
	 * <ul>
	 * <li>Parsing the spec file fails</li>
	 * <li>Writing to the spec file fails</li>
	 * </ul>
	 */
	public void write() throws CoreException;
	
}
