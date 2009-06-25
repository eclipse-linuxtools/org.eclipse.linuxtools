/*******************************************************************************
 * Copyright (c) 2005, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.core;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

/**
 * Represents an RPM project.
 *
 */
public interface IRPMProject {

	/**
	 * Prepares the sources for the RPM project according to the directives
	 * in the project's spec file.  This method deposits prepared sources 
	 * in the project's BUILD directory.  This method is the equivalent of
	 * executing <code>rpmbuild -bp /path/to/specfile.spec</code> on the 
	 * command line.
	 * @throws CoreException if:
	 * <ul>
	 * <li>The project's BUILD directory cannot be refreshed</li>
	 * <li><code>rpmbuild</code> execution fails</li>
	 * <li>The project is not an RPM project (a source RPM has not been imported)</li>
	 * </ul>
	 */
	public void buildPrep() throws CoreException;
	
	/**
	 * Builds both a source RPM and a binary RPM according to the directives
	 * in the project's spec file.  This method deposits the binary and source
	 * RPMs into the project's RPMS and SRPMS directories, respectively.
	 * This method will modify the project model and project spec file according
	 * to the given export deltas.  This method will also refresh the project
	 * sources with the new project model when the export operation is complete.
	 * @param export the deltas associated with the export operation
	 * @throws CoreException if:
	 * <ul>
	 * <li>Updating the RPM project model fails</li>
	 * <li><code>rpmbuild</code> execution fails</li>
	 * <li>Refreshing project sources fails</li>
	 * <li>The project is not an RPM project (a source RPM has not been imported)</li>
	 * </ul>
	 */
	public void buildAll() throws CoreException;
	
	/**
	 * Builds a binary RPM according to the directives in the project's spec file.
	 * This method deposits the binary RPM(s) into the project's RPMS directory.
	 * This method will modify the project model and project spec file according 
	 * to the given export deltas.  This method will also refresh the project 
	 * sources with the new project model when the export operation is complete.
	 * @param export the deltas associated with the export operation
	 * @throws CoreException if:
	 * <ul>
	 * <li>Updating the RPM project model fails</li>
	 * <li><code>rpmbuild</code> execution failse</li>
	 * <li>Refreshing project sources fails</li>
	 * <li>The project is not an RPM project (a source RPM has not been imported)</li>
	 * </ul>
	 */
	public void buildBinaryRPM() throws CoreException;
	
	/**
	 * Builds a source RPM according to the directives in the project's spec file.
	 * This method deposits the source RPM into the project's RPMS directory.
	 * This method will modify the project model and project spec file according 
	 * to the given export deltas.  This method will also refresh the project 
	 * sources with the new project model when the export operation is complete.
	 * @param export the deltas associated with the export operation
	 * @throws CoreException if:
	 * <ul>
	 * <li>Updating the RPM project model fails</li>
	 * <li><code>rpmbuild</code> execution failse</li>
	 * <li>Refreshing project sources fails</li>
	 * <li>The project is not an RPM project (a source RPM has not been imported)</li>
	 * </ul>
	 */
	public void buildSourceRPM() throws CoreException;
	
	/**
	 * Imports an external source RPM into the project and installs project 
	 * sources.  This method also adds an RPM nature to the project.
	 * @param externalFile the external source RPM
	 * @throws CoreException if:
	 * <ul>
	 * <li>The external source RPM cannot be accessed</li>
	 * <li>Source installation fails</li>
	 * <li>Spec file parsing error occurs</li>
	 * <li>Calculating project checksum fails</li>
	 * </ul>
	 */
	public void importSourceRPM(File externalFile) throws CoreException;
	
	/**
	 * Returns the project handle associated with the RPM project.
	 * @return the project handle
	 */
	public IProject getProject();
	
	/**
	 * Returns the RPM configuration associated with the RPM project.
	 * @return the RPM configuration
	 */
	public IRPMConfiguration getConfiguration();
	
	/**
	 * Returns the source RPM associated with the RPM project.
	 * @return the source RPM, or <code>null</code> if no source RPM
	 * has been imported
	 */
    public ISourceRPM getSourceRPM();
	
	/**
	 * Sets the source RPM associated with the RPM project.
	 * @param sourceRPM the source RPM
	 * @throws CoreException if:
	 * <ul>
	 * <li>Setting the project property associated with the source RPM fails</li>
	 * </ul>
	 */
	public void setSourceRPM(ISourceRPM sourceRPM) throws CoreException;
	
	/**
	 * Returns the spec file associated with the RPM project.
	 * @return the spec file, or <code>null</code> if no source RPM 
	 * has been imported
	 */
	public IFile getSpecFile();
	
	/**
	 * Sets the spec file associated with the RPM project.
	 * @param specFile the spec file
	 * @throws CoreException if:
	 * <ul>
	 * <li>Setting the project property associated with the spec file fails</li>
	 * </ul>
	 */
	public void setSpecFile(IFile specFile) throws CoreException;
	
}
