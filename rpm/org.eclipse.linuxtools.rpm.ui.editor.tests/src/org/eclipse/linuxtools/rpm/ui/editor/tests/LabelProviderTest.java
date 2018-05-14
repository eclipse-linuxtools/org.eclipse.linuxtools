/*******************************************************************************
 * Copyright (c) 2013, 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.rpm.ui.editor.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.eclipse.linuxtools.internal.rpm.ui.editor.outline.SpecfileLabelProvider;
import org.junit.Before;
import org.junit.Test;

/**
 * Test case to make sure that the label provider is filtering out some macros
 * (e.g., %{?scl_prefix}).
 */
public class LabelProviderTest extends FileTestCase {

	private SpecfileLabelProvider labelProvider;
	private String correctResult = "eclipse-plugin";

	@Before
	public void initialize() {
		labelProvider = new SpecfileLabelProvider();
	}

	/**
	 * Test to see if %{?...} macros will be shown. They should not.
	 */
	@Test
	public void testLabelForUnresolvedMacro() {
		String testText = "%{?some_macro}eclipse-plugin";
		String result = labelProvider.getText(testText);
		assertEquals(result, correctResult);

		testText = "eclipse-plugin%{?some_macro}";
		result = labelProvider.getText(testText);
		assertEquals(result, correctResult);

		testText = "%{?some_macro}eclipse-plugin%{?some_macro}";
		result = labelProvider.getText(testText);
		assertEquals(result, correctResult);
	}

	/**
	 * Test for incorrect labels.
	 */
	@Test
	public void testLabelForIncorrectString() {
		// for sake of test, this is just to show that the filterMacros
		// method would not filter %{...} regular macros.
		// It is the job of SpecfileDefine to do that.
		String testText = "%{no_question_mark}eclipse-plugin";
		String result = labelProvider.getText(testText);
		assertNotEquals(result, correctResult);

		testText = "{?no_percent_sign}eclipse-plugin";
		result = labelProvider.getText(testText);
		assertNotEquals(result, correctResult);
	}

}
