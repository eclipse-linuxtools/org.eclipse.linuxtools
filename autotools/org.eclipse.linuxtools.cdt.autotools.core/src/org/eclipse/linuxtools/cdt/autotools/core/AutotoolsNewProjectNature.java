/*******************************************************************************
 * Copyright (c) 2008, 2009 Red Hat Inc.. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial implementation
 *     IBM Rational Software - add and remove nature static methods
 *******************************************************************************/
package org.eclipse.linuxtools.cdt.autotools.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.linuxtools.internal.cdt.autotools.core.AutotoolsConfigurationBuilder;

public class AutotoolsNewProjectNature implements IProjectNature {

	public static final String AUTOTOOLS_NATURE_ID = AutotoolsPlugin.getUniqueIdentifier() + ".autotoolsNatureV2";  //$NON-NLS-1$
	public final static String BUILDER_ID = ManagedBuilderCorePlugin.getUniqueIdentifier() + ".genmakebuilder"; //$NON-NLS-1$

	private IProject project;
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#configure()
	 */
	public void configure() throws CoreException {
		addAutotoolsBuilder(project, new NullProgressMonitor());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#deconfigure()
	 */
	public void deconfigure() throws CoreException {
		// TODO remove builder from here
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#getProject()
	 */
	public IProject getProject() {
		return project;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#setProject(org.eclipse.core.resources.IProject)
	 */
	public void setProject(IProject project) {
		this.project = project;
	}
	
	/**
	 * Add the Autotools builder to the project
	 * @param project
	 * @param monitor
	 * @throws CoreException
	 */
	public static void addAutotoolsBuilder(IProject project, IProgressMonitor monitor) throws CoreException {
		// Add the builder to the project
		IProjectDescription description = project.getDescription();
		ICommand[] commands = description.getBuildSpec();
		ArrayList<ICommand> commandList = new ArrayList<ICommand>();
	
		// Make sure the Autotools Configuration builder just precedes the Common Builder
		for (int i = 0; i < commands.length; i++) {
			ICommand command = commands[i];
			if (command.getBuilderName().equals(AutotoolsConfigurationBuilder.BUILDER_ID)) {
				// ignore it
			} else {
				if (command.getBuilderName().equals(BUILDER_ID)) {
					// add Autotools Configuration builder just before builder
					ICommand newCommand = description.newCommand();
					newCommand.setBuilderName(AutotoolsConfigurationBuilder.BUILDER_ID);
					commandList.add(newCommand);
				}
				commandList.add(command);
			}
		}
		ICommand[] newCommands = new ICommand[commandList.size()];
		description.setBuildSpec(commandList.toArray(newCommands));
		project.setDescription(description, new NullProgressMonitor());
	}

	/**
	 * Utility method for adding an autotools nature to a project.
	 * 
	 * @param proj the project to add the autotools nature to.
	 * @param monitor a progress monitor to indicate the duration of the operation, or
	 * <code>null</code> if progress reporting is not required.
	 */
	public static void addAutotoolsNature(IProject project, IProgressMonitor monitor) throws CoreException {
		addNature(project, AUTOTOOLS_NATURE_ID, monitor);
	}

	/**
	 * Utility method for adding a nature to a project.
	 * 
	 * @param proj the project to add the nature to.
	 * @param natureId the id of the nature to assign to the project
	 * @param monitor a progress monitor to indicate the duration of the operation, or
	 * <code>null</code> if progress reporting is not required.
	 */
	public static void addNature(IProject project, String natureId, IProgressMonitor monitor) throws CoreException {
		IProjectDescription description = project.getDescription();
		String[] prevNatures = description.getNatureIds();
		for (int i = 0; i < prevNatures.length; i++) {
			if (natureId.equals(prevNatures[i]))
				return;
		}
		String[] newNatures = new String[prevNatures.length + 1];
		System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
		newNatures[prevNatures.length] = natureId;
		description.setNatureIds(newNatures);
		project.setDescription(description, monitor);
	}
	
	/**
	 * Utility method to remove the autotools nature from a project.
	 * 
	 * @param project to remove the autotools nature from
	 * @param mon progress monitor to indicate the duration of the operation, or 
	 * <code>null</code> if progress reporting is not required. 
	 * @throws CoreException
	 */
	public static void removeAutotoolsNature(IProject project, IProgressMonitor mon) throws CoreException {
		removeNature(project, AUTOTOOLS_NATURE_ID, mon);
	}

	/**
	 * Utility method for removing a project nature from a project.
	 * 
	 * @param proj the project to remove the nature from
	 * @param natureId the nature id to remove
	 * @param monitor a progress monitor to indicate the duration of the operation, or
	 * <code>null</code> if progress reporting is not required.
	 */
	public static void removeNature(IProject project, String natureId, IProgressMonitor monitor) throws CoreException {
		IProjectDescription description = project.getDescription();
		String[] prevNatures = description.getNatureIds();
		List<String> newNatures = new ArrayList<String>(Arrays.asList(prevNatures));
		newNatures.remove(natureId);
		description.setNatureIds((String[])newNatures.toArray(new String[newNatures.size()]));
		project.setDescription(description, monitor);
	}

}
