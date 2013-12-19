/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.memcheck.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.linuxtools.internal.valgrind.core.ValgrindError;
import org.eclipse.linuxtools.internal.valgrind.core.ValgrindStackFrame;
import org.eclipse.linuxtools.internal.valgrind.launch.ValgrindLaunchPlugin;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindUIPlugin;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindViewPart;
import org.eclipse.linuxtools.valgrind.core.IValgrindMessage;
import org.junit.Test;

public class LinkedResourceMarkerTest extends AbstractLinkedResourceMemcheckTest {
	@Test
	public void testLinkedMarkers() throws Exception {
		ILaunchConfiguration config = createConfiguration(proj.getProject());
		doLaunch(config, "testLinkedMarkers"); //$NON-NLS-1$

		ValgrindViewPart view = ValgrindUIPlugin.getDefault().getView();
		IValgrindMessage[] errors = view.getMessages();

		ArrayList<IMarker> markers = new ArrayList<>(Arrays.asList(proj
				.getProject().findMarkers(ValgrindLaunchPlugin.MARKER_TYPE,
						true, IResource.DEPTH_INFINITE)));
		assertEquals(5, markers.size());
		for (IValgrindMessage error : errors) {
			findMarker(markers, error);
		}
		assertEquals(0, markers.size());
	}

	private void findMarker(ArrayList<IMarker> markers, IValgrindMessage error)
			throws Exception, CoreException {
		ValgrindStackFrame frame = null;
		IValgrindMessage[] children = error.getChildren();
		for (int i = 0; i < children.length; i++) {
			if (frame == null && children[i] instanceof ValgrindStackFrame
					&& isWorkspaceFrame((ValgrindStackFrame) children[i])) {
				frame = (ValgrindStackFrame) children[i];
			} else if (children[i] instanceof ValgrindError) {
				findMarker(markers, children[i]);
			}
		}

		int ix = -1;
		for (int i = 0; i < markers.size(); i++) {
			IMarker marker = markers.get(i);
			if (marker.getAttribute(IMarker.MESSAGE).equals(error.getText())
					&& marker.getResource().getName().equals(frame.getFile())
					&& marker.getAttribute(IMarker.LINE_NUMBER).equals(
							frame.getLine())) {
				ix = i;
			}
		}
		if (ix < 0) {
			fail();
		}
		markers.remove(ix);
	}

	private boolean isWorkspaceFrame(ValgrindStackFrame frame) {
		ISourceLocator locator = frame.getLaunch().getSourceLocator();
		Object result = DebugUITools.lookupSource(frame.getFile(), locator)
				.getSourceElement();
		return result != null && result instanceof IResource;
	}
}
