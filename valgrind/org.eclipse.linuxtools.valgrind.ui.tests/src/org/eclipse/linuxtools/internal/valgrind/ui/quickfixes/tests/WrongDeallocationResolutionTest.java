/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rafael Medeiros Teixeira <rafaelmt@linux.vnet.ibm.com> - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.valgrind.ui.quickfixes.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.linuxtools.internal.valgrind.tests.AbstractValgrindTest;
import org.eclipse.linuxtools.internal.valgrind.ui.quickfixes.WrongDeallocationResolution;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class WrongDeallocationResolutionTest extends AbstractValgrindTest {

	private final String EMPTY_STRING = ""; //$NON-NLS-1$
	private final String VALGRIND_MARKER_TYPE = "org.eclipse.linuxtools.valgrind.launch.marker"; //$NON-NLS-1$
	private static Document document;
	private static ICProject proj;
	private IMarker[] markers;

	@Override
	protected String getToolID() {
		return "org.eclipse.linuxtools.valgrind.launch.memcheck"; //$NON-NLS-1$
	}

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		proj = createProjectAndBuild("wrongDeallocTest"); //$NON-NLS-1$
		ILaunchConfiguration config = createConfiguration(proj.getProject());
		doLaunch(config, "wrongDeallocTest"); //$NON-NLS-1$

		document = new Document();
		InputStream fileInputStream = proj.getProject()
				.getFile("wrongDealloc.cpp").getContents(); //$NON-NLS-1$
		Scanner scanner = new Scanner(fileInputStream).useDelimiter("\\A"); //$NON-NLS-1$
		String content;
		if (scanner.hasNext()) {
			content = scanner.next();
		} else {
			content = EMPTY_STRING;
		}
		document.set(content);
		markers = proj.getProject().findMarkers(VALGRIND_MARKER_TYPE, true, 1);
		Arrays.sort(markers, new MarkerComparator());
	}

	@Override
	@After
	public void tearDown() throws CoreException {
		deleteProject(proj);
		super.tearDown();
	}

	@Test
	public void testMallocDeleteQuickFix() throws CoreException {
		IMarker mallocDeleteMarker = markers[1];
		int markerLine = mallocDeleteMarker.getAttribute(IMarker.LINE_NUMBER,
				-1);

		createResolutionAndApply(mallocDeleteMarker);

		String newContent = getLineContent(document, markerLine);
		assertTrue(newContent.contains("free")); //$NON-NLS-1$
		assertFalse(newContent.contains("delete")); //$NON-NLS-1$
	}

	@Test
	public void testMallocDeleteArrayQuickFix() throws CoreException {
		IMarker mallocDeleteArrayMarker = markers[3];
		int markerLine = mallocDeleteArrayMarker.getAttribute(
				IMarker.LINE_NUMBER, -1);

		createResolutionAndApply(mallocDeleteArrayMarker);

		String newContent = getLineContent(document, markerLine);
		assertTrue(newContent.contains("free")); //$NON-NLS-1$
		assertFalse(newContent.contains("delete")); //$NON-NLS-1$
		assertFalse(newContent.contains("[")); //$NON-NLS-1$
	}

	@Test
	public void testNewFreeQuickFix() throws CoreException {
		IMarker newFreeMarker = markers[5];
		int markerLine = newFreeMarker.getAttribute(IMarker.LINE_NUMBER, -1);

		createResolutionAndApply(newFreeMarker);

		String newContent = getLineContent(document, markerLine);
		assertTrue(newContent.contains("delete")); //$NON-NLS-1$
		assertFalse(newContent.contains("free")); //$NON-NLS-1$
	}

	@Test
	public void testNewArrayFreeQuickFix() throws CoreException {
		IMarker newArrayFreeMarker = markers[7];
		int markerLine = newArrayFreeMarker.getAttribute(IMarker.LINE_NUMBER,
				-1);

		createResolutionAndApply(newArrayFreeMarker);

		String newContent = getLineContent(document, markerLine);
		assertTrue(newContent.contains("delete[]")); //$NON-NLS-1$
		assertFalse(newContent.contains("free")); //$NON-NLS-1$
	}

	@Test
	public void testNewArrayDeleteQuickFix() throws CoreException {
		IMarker newArrayDeleteMarker = markers[9];
		int markerLine = newArrayDeleteMarker.getAttribute(IMarker.LINE_NUMBER,
				-1);

		createResolutionAndApply(newArrayDeleteMarker);

		String newContent = getLineContent(document, markerLine);
		assertTrue(newContent.contains("delete[]")); //$NON-NLS-1$
	}

	private void createResolutionAndApply(IMarker marker) throws CoreException {
		WrongDeallocationResolution resolution = new WrongDeallocationResolution(
				marker);
		assertNotNull(resolution);

		int numMarkersBefore = proj.getProject().findMarkers(
				VALGRIND_MARKER_TYPE, true, 1).length;
		resolution.apply(marker, document);
		int numMarkersAfter = proj.getProject().findMarkers(
				VALGRIND_MARKER_TYPE, true, 1).length;

		assertEquals(numMarkersAfter, numMarkersBefore - 2);
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

	private static class MarkerComparator implements Comparator<IMarker> {
		@Override
		public int compare(IMarker marker1, IMarker marker2) {
			int line1 = marker1.getAttribute(IMarker.LINE_NUMBER, -1);
			int line2 = marker2.getAttribute(IMarker.LINE_NUMBER, -1);

			if (line1 > line2) {
				return 1;
			} else if (line2 < line1) {
				return -1;
			}
			return 0;
		}
	}
}
