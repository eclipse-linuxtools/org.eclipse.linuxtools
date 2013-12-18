/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Neil Guzman - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.createrepo.tree.tests;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.linuxtools.internal.rpm.createrepo.tree.CreaterepoTreeCategory;
import org.junit.Test;

/**
 * Test the CreaterepoTreeCategory class.
 */
public class CreaterepoTreeCategoryTest {

	private static final String CATEGORY_NAME = "test"; //$NON-NLS-1$
	private static final String TAG1 = "tag1"; //$NON-NLS-1$
	private static final String TAG2 = "tag2"; //$NON-NLS-1$

	/**
	 * Test the tree category to make sure it initializes and stores tags properly.
	 * Tags must be unique, so the category should not add tags that already exist
	 * within the category.
	 */
	@Test
	public void testCreaterepoTreeCategory() {
		List<String> tagsToCheck = new ArrayList<>();
		tagsToCheck.add(TAG1);
		tagsToCheck.add(TAG2);
		CreaterepoTreeCategory categoryTest = new CreaterepoTreeCategory(CATEGORY_NAME);
		assertEquals(0, categoryTest.getTags().size());
		assertEquals(CATEGORY_NAME, categoryTest.getName());
		categoryTest.addTag(TAG2);
		assertEquals(1, categoryTest.getTags().size());
		assertEquals(TAG2, categoryTest.getTags().get(0));
		categoryTest.addAllTags(tagsToCheck);
		// TAG2 should not be added again, hence size should = 2
		assertEquals(2, categoryTest.getTags().size());
	}

}
