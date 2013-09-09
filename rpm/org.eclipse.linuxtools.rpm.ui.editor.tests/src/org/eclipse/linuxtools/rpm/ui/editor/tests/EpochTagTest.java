/*******************************************************************************
 * Copyright (c) 2007, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.rpm.ui.editor.tests;

import static org.junit.Assert.assertEquals;

import org.eclipse.linuxtools.rpm.ui.editor.markers.SpecfileErrorHandler;
import org.junit.Test;

public class EpochTagTest extends FileTestCase {

	@Test
	public void testEpochTag() {
		String testText = "Epoch: 1";
		newFile(testText);
		assertEquals(1, specfile.getEpoch());
	}

	@Test
	public void testEpochTag2() {
		String testText = "Epoch:	1";
		newFile(testText);
		assertEquals(1, specfile.getEpoch());
	}

	@Test
	public void testNullEpochTag() {
		String testText = "Epoch:";
		newFile(testText);
		SpecfileTestFailure failure = getFailures().get(0);
		assertEquals(0, failure.getPosition().getOffset());
		assertEquals(6, failure.getPosition().getLength());
		assertEquals(SpecfileErrorHandler.ANNOTATION_ERROR, failure
				.getAnnotation().getType());
		assertEquals("Epoch declaration without value.", failure
				.getAnnotation().getText());
	}

	@Test
	public void testNullEpochTag2() {
		String testText = "Epoch:	";
		newFile(testText);
		SpecfileTestFailure failure = getFailures().get(0);
		assertEquals(0, failure.getPosition().getOffset());
		assertEquals(7, failure.getPosition().getLength());
		assertEquals(SpecfileErrorHandler.ANNOTATION_ERROR, failure
				.getAnnotation().getType());
		assertEquals("Epoch declaration without value.", failure
				.getAnnotation().getText());
	}

	@Test
	public void testMultipleEpochsTag() {
		String testText = "Epoch: 1 2";
		newFile(testText);
		SpecfileTestFailure failure = getFailures().get(0);
		assertEquals(0, failure.getPosition().getOffset());
		assertEquals(10, failure.getPosition().getLength());
		assertEquals(SpecfileErrorHandler.ANNOTATION_ERROR, failure
				.getAnnotation().getType());
		assertEquals("Epoch cannot have multiple values.", failure
				.getAnnotation().getText());
	}

	@Test
	public void testMultipleEpochsTag2() {
		String testText = "Epoch: 	1 2";
		newFile(testText);
		SpecfileTestFailure failure = getFailures().get(0);
		assertEquals(0, failure.getPosition().getOffset());
		assertEquals(11, failure.getPosition().getLength());
		assertEquals(SpecfileErrorHandler.ANNOTATION_ERROR, failure
				.getAnnotation().getType());
		assertEquals("Epoch cannot have multiple values.", failure
				.getAnnotation().getText());
	}

	@Test
	public void testNonIntegerEpoch() {
		String testText = "Epoch: blah";
		newFile(testText);
		SpecfileTestFailure failure = getFailures().get(0);
		assertEquals(0, failure.getPosition().getOffset());
		assertEquals(11, failure.getPosition().getLength());
		assertEquals(SpecfileErrorHandler.ANNOTATION_ERROR, failure
				.getAnnotation().getType());
		assertEquals("Epoch cannot have non-integer value.", failure
				.getAnnotation().getText());
	}

	@Test
	public void testNonIntegerEpoch2() {
		String testText = "Epoch:	blah";
		newFile(testText);
		SpecfileTestFailure failure = getFailures().get(0);
		assertEquals(0, failure.getPosition().getOffset());
		assertEquals(11, failure.getPosition().getLength());
		assertEquals(SpecfileErrorHandler.ANNOTATION_ERROR, failure
				.getAnnotation().getType());
		assertEquals("Epoch cannot have non-integer value.", failure
				.getAnnotation().getText());
	}
}
