/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.valgrind.memcheck.tests;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.valgrind.memcheck.MemcheckPlugin;
import org.eclipse.linuxtools.valgrind.memcheck.MemcheckViewPart;
import org.eclipse.linuxtools.valgrind.memcheck.ValgrindError;
import org.eclipse.linuxtools.valgrind.memcheck.ValgrindStackFrame;
import org.eclipse.linuxtools.valgrind.ui.ValgrindUIPlugin;

public class MarkerTest extends AbstractMemcheckTest {
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		proj = createProjectAndBuild("basicTest"); //$NON-NLS-1$
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		deleteProject(proj);
	}

	public void testMarkers() throws Exception {
		ILaunchConfiguration config = createConfiguration(proj.getProject());
		doLaunch(config, "testMarkers"); //$NON-NLS-1$

		MemcheckViewPart view = (MemcheckViewPart) ValgrindUIPlugin.getDefault().getView().getDynamicView();
		ValgrindError[] errors = view.getErrors();

		ArrayList<IMarker> markers = new ArrayList<IMarker>(Arrays.asList(proj.getProject().findMarkers(MemcheckPlugin.MARKER_TYPE, true, IResource.DEPTH_INFINITE)));
		for (ValgrindError error : errors) {
			ValgrindStackFrame frame = getTopWorkspaceFrame(error.getFrames());

			int ix = -1;
			for (int i = 0; i < markers.size(); i++) {
				IMarker marker = markers.get(i);
				if (marker.getAttribute(IMarker.MESSAGE).equals(error.getWhat())
						&& marker.getResource().getName().equals(frame.getFile())
						&& marker.getAttribute(IMarker.LINE_NUMBER).equals(frame.getLine())) {
					ix = i;
				}
			}
			if (ix < 0) {
				fail();
			}
			markers.remove(ix);
		}
		assertEquals(0, markers.size());
	}
	
	private ValgrindStackFrame getTopWorkspaceFrame(List<ValgrindStackFrame> frames) throws Exception {
		ValgrindStackFrame result = null;
		for (int i = 0; result == null && i < frames.size(); i++) {
			ValgrindStackFrame frame = frames.get(i);
			String strpath = frame.getDir() + Path.SEPARATOR + frame.getFile();
			File file = new File(strpath);
			Path path = new Path(file.getAbsolutePath());
			
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IFile[] resource = root.findFilesForLocation(path);
			if (resource.length > 0 && resource[0].exists()) {
				result = frame;
			}
		}
		return result;
	}
}
