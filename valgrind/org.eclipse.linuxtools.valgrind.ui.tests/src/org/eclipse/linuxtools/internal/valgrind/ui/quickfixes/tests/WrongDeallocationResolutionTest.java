/*******************************************************************************
 * Copyright (c) 2013, 2024 IBM Corporation and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Rafael Medeiros Teixeira <rafaelmt@linux.vnet.ibm.com> - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.valgrind.ui.quickfixes.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.linuxtools.internal.valgrind.tests.AbstractValgrindTest;
import org.eclipse.linuxtools.internal.valgrind.ui.quickfixes.WrongDeallocationResolution;
import org.junit.After;
import org.junit.Test;

public class WrongDeallocationResolutionTest extends AbstractValgrindTest {

	private final String VALGRIND_MARKER_TYPE = "org.eclipse.linuxtools.valgrind.launch.marker"; //$NON-NLS-1$
	private Document document;
	private IMarker[] markers;

	@Override
	protected String getToolID() {
		return "org.eclipse.linuxtools.valgrind.launch.memcheck"; //$NON-NLS-1$
	}

	private void prep(String statements) throws Exception {
		proj = createProject(getBundle(), "wrongDeallocTest");
		IFile cppFile = proj.getProject().getFile("wrongDealloc.cpp");
		InputStream preContentStream = cppFile.getContents();
		String content = new String(preContentStream.readAllBytes());
		content = content.replace("__VALGRIND__", statements);
		cppFile.setContents(new ByteArrayInputStream(content.getBytes()) , 0, null);
		buildProject(proj);

		ILaunchConfiguration config = createConfiguration(proj.getProject());
		doLaunch(config, "wrongDeallocTest"); //$NON-NLS-1$

		document = new Document();
		document.set(content);
		markers = proj.getProject().findMarkers(VALGRIND_MARKER_TYPE, true, 1);
		Arrays.sort(markers, (marker1, marker2) -> {
			int line1 = marker1.getAttribute(IMarker.LINE_NUMBER, -1);
			int line2 = marker2.getAttribute(IMarker.LINE_NUMBER, -1);

			if (line1 > line2) {
				return 1;
			} else if (line2 > line1) {
				return -1;
			}

			String message1 = "";
			String message2 = "";
			try {
				message1 = (String) marker1.getAttribute(IMarker.MESSAGE);
				message2 = (String) marker2.getAttribute(IMarker.MESSAGE);
			} catch (CoreException e) {
			}
			return message1.compareTo(message2);
		});
	}

	@Override
	@After
	public void tearDown() throws CoreException {
		deleteProject(proj);
		super.tearDown();
	}

	@Test
	public void testMallocDeleteQuickFix() throws Exception {
		prep("char *p1 = (char *)malloc(sizeof(char) * SIZE);\n"
				+ "	delete(p1);");
		IMarker mallocDeleteMarker = markers[2];
		int markerLine = mallocDeleteMarker.getAttribute(IMarker.LINE_NUMBER, -1);

		createResolutionAndApply(mallocDeleteMarker, 3);

		String newContent = getLineContent(document, markerLine);
		assertTrue(newContent.contains("free")); //$NON-NLS-1$
		assertFalse(newContent.contains("delete")); //$NON-NLS-1$
	}

	@Test
	public void testMallocDeleteArrayQuickFix() throws Exception {
		prep("char* p2 = (char *)malloc(5 * sizeof(char) * SIZE);\n"
				+ "	delete[] p2;");
		IMarker mallocDeleteArrayMarker = markers[1];
		int markerLine = mallocDeleteArrayMarker.getAttribute(IMarker.LINE_NUMBER, -1);

		createResolutionAndApply(mallocDeleteArrayMarker, 2);

		String newContent = getLineContent(document, markerLine);
		assertTrue(newContent.contains("free")); //$NON-NLS-1$
		assertFalse(newContent.contains("delete")); //$NON-NLS-1$
		assertFalse(newContent.contains("[")); //$NON-NLS-1$
	}

	@Test
	public void testNewFreeQuickFix() throws Exception {
		prep("char *p3 = new char;\n"
				+ "	free(p3);");
		IMarker newFreeMarker = markers[1];
		int markerLine = newFreeMarker.getAttribute(IMarker.LINE_NUMBER, -1);

		createResolutionAndApply(newFreeMarker, 2);

		String newContent = getLineContent(document, markerLine);
		assertTrue(newContent.contains("delete")); //$NON-NLS-1$
		assertFalse(newContent.contains("free")); //$NON-NLS-1$
	}

	@Test
	public void testNewArrayFreeQuickFix() throws Exception {
		prep("char *p4 = new char[5];\n"
				+ "	free(p4);");
		IMarker newArrayFreeMarker = markers[1];
		int markerLine = newArrayFreeMarker.getAttribute(IMarker.LINE_NUMBER, -1);

		createResolutionAndApply(newArrayFreeMarker, 2);

		String newContent = getLineContent(document, markerLine);
		assertTrue(newContent.contains("delete[]")); //$NON-NLS-1$
		assertFalse(newContent.contains("free")); //$NON-NLS-1$
	}

	@Test
	public void testNewArrayDeleteQuickFix() throws Exception {
		prep("char* p5 = new char[5];\n"
				+ "	delete p5;");
		IMarker newArrayDeleteMarker = markers[2];
		int markerLine = newArrayDeleteMarker.getAttribute(IMarker.LINE_NUMBER, -1);

		createResolutionAndApply(newArrayDeleteMarker, 3);

		String newContent = getLineContent(document, markerLine);
		assertTrue(newContent.contains("delete[]")); //$NON-NLS-1$
	}

	private void createResolutionAndApply(IMarker marker, int fixedMarkers) throws CoreException {
		WrongDeallocationResolution resolution = new WrongDeallocationResolution();
		assertNotNull(resolution);

		int numMarkersBefore = proj.getProject().findMarkers(VALGRIND_MARKER_TYPE, true, 1).length;
		resolution.apply(marker, document);
		IMarker[] markers2 = proj.getProject().findMarkers(VALGRIND_MARKER_TYPE, true, 1);
		int numMarkersAfter = markers2.length;

		assertEquals(numMarkersAfter, numMarkersBefore - fixedMarkers);
	}

	private String getLineContent(Document document, int line) {
		try {
			int lineOffset = document.getLineOffset(line - 1);
			int lineLength = document.getLineLength(line - 1);
			return document.get(lineOffset, lineLength);
		} catch (BadLocationException e) {
			return null;
		}
	}
}
