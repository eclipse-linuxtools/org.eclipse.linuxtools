/*******************************************************************************
 * Copyright (c) 2012, 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *     Neil Guzman - stub specfile in proper dir (B#414589)
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpmstubby;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

/**
 * Abstract class holding the common part of generators.
 *
 */
public abstract class AbstractGenerator {

	String projectName;
	String specfileName;

	/**
	 * Writes the given contents to a file with the given fileName in the
	 * specified project.
	 */
	public void writeContent() {
		String contents = generateSpecfile();
		InputStream contentInputStream = new ByteArrayInputStream(
				contents.getBytes());
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(new Path(projectName));
		if (!resource.exists() || !(resource instanceof IContainer)) {
			IStatus status = new Status(IStatus.ERROR, StubbyPlugin.PLUGIN_ID,
					IStatus.OK, "Project \"" + projectName + "\" does not exist.", null);
			StubbyLog.logError(new CoreException(status));
		}
		IContainer container = (IContainer) resource;
		IResource specsFolder = container.getProject().findMember("SPECS"); //$NON-NLS-1$
		IFile file = container.getFile(new Path(specfileName));
		if (specsFolder != null) {
			file = ((IFolder) specsFolder).getFile(new Path(specfileName));
		}
		final IFile openFile = file;
		try {
			InputStream stream = contentInputStream;
			if (file.exists()) {
				file.setContents(stream, true, true, null);
			} else {
				file.create(stream, true, null);
			}
			stream.close();
		} catch (IOException e) {
			StubbyLog.logError(e);
		} catch (CoreException e) {
			StubbyLog.logError(e);
		}
		Display.getCurrent().asyncExec(new Runnable() {
					@Override
					public void run() {
						IWorkbenchPage page = PlatformUI.getWorkbench()
								.getActiveWorkbenchWindow().getActivePage();
						try {
							IDE.openEditor(page, openFile, true);
						} catch (PartInitException e) {
							StubbyLog.logError(e);
						}
					}
				});
	}

	/**
	 * The method that returns the string representation of the spec file.
	 *
	 * @return The specfile.
	 */
	public abstract String generateSpecfile();

	/**
	 * Generate changelog
	 *
	 * @param buffer Buffer to write content to
	 */
	protected static void generateChangelog(StringBuilder buffer) {
		buffer.append("%changelog\n");
		buffer.append("#FIXME\n");
	}

}
