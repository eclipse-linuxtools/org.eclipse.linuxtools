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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.linuxtools.internal.rpm.createrepo.CreaterepoPreferenceConstants;
import org.eclipse.linuxtools.internal.rpm.createrepo.CreaterepoProject;
import org.eclipse.linuxtools.internal.rpm.createrepo.ICreaterepoConstants;
import org.eclipse.linuxtools.internal.rpm.createrepo.form.tests.TestCreaterepoProject;
import org.eclipse.linuxtools.internal.rpm.createrepo.tree.CreaterepoCategoryModel;
import org.eclipse.linuxtools.internal.rpm.createrepo.tree.CreaterepoTreeCategory;
import org.eclipse.linuxtools.internal.rpm.createrepo.tree.CreaterepoTreeContentProvider;
import org.eclipse.linuxtools.internal.rpm.createrepo.tree.CreaterepoTreeLabelProvider;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Tests for tag category tree. Make sure that preferences are properly loaded
 * into the treeviewer and that the categories are the correct ones.
 */
public class CreaterepoTreeTest {

    /*
     * Tags being used to test with.
     */
    private static final String[] DISTRO_TAGS =
        {"tag1","tag2","tag3","tag4","tag5"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    private static final String[] CONTENT_TAGS = {};
    private static final String[] REPO_TAGS = {"tag1"}; //$NON-NLS-1$

    /*
     * Categories and how many tags per categories to test with.
     */
    private static final Map<String, Integer> CORRECT_CATEGORIES;
    static {
        Map<String, Integer> temp = new HashMap<>();
        temp.put(CreaterepoPreferenceConstants.PREF_DISTRO_TAG, DISTRO_TAGS.length);
        temp.put(CreaterepoPreferenceConstants.PREF_CONTENT_TAG, CONTENT_TAGS.length);
        temp.put(CreaterepoPreferenceConstants.PREF_REPO_TAG, REPO_TAGS.length);
        CORRECT_CATEGORIES = Collections.unmodifiableMap(temp);
    }

    private static TestCreaterepoProject testProject;
    private CreaterepoProject project;
    private TreeViewer viewer;
    private Tree tree;

    /**
     * Initialize the test project.
     *
     * @throws CoreException
     */
    @BeforeClass
    public static void setUpBeforeClass() throws CoreException {
        testProject = new TestCreaterepoProject();
        assertTrue(testProject.getProject().exists());
    }

    /**
     * Delete the project when tests are done.
     *
     * @throws CoreException
     */
    @AfterClass
    public static void tearDownAfterClass() throws CoreException {
        testProject.dispose();
        assertFalse(testProject.getProject().exists());
    }

    /**
     * Get the CreaterepoProject at the beginning of each test.
     *
     * @throws CoreException
     */
    @Before
    public void setUp() throws CoreException {
        project = testProject.getCreaterepoProject();
        assertNotNull(project);
    }

    /**
     * Clear the preferences after each test.
     *
     * @throws BackingStoreException
     */
    @After
    public void tearDown() throws BackingStoreException {
        IEclipsePreferences pref = project.getEclipsePreferences();
        pref.clear();
        pref.flush();
        assertEquals(0, pref.keys().length);
    }

    /**
     * Test if the treeviewer is initialized properly with the correct labels.
     */
    @Test
    public void testTreeViewerInitialization() {
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                initViewer();
                // there should only be 3 categories
                assertEquals(3, tree.getItemCount());
                // and these should be the correct categories
                for (TreeItem treeItem : tree.getItems()) {
                    assertTrue(inCategory(treeItem.getText()));
                }
            }
        });
    }

    /**
     * Test if the treeviewer properly loads the preferences.
     *
     * @throws BackingStoreException
     */
    @Test
    public void testTreeViewerPreferences() throws BackingStoreException {
        addTestPreferences();
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                initViewer();
                for (TreeItem treeItem : tree.getItems()) {
                    if (treeItem.getData() instanceof CreaterepoTreeCategory) {
                        CreaterepoTreeCategory category = (CreaterepoTreeCategory) treeItem.getData();
                        // make sure the categories are still correct
                        assertTrue(CORRECT_CATEGORIES.containsKey(category.getName()));
                        // assert that the number of tags stored is the correct amount
                        assertEquals(CORRECT_CATEGORIES.get(category.getName()).intValue(), category.getTags().size());
                    }
                }
                // do 1 test to make sure the tags were properly stored/loaded
                for (TreeItem treeItem : tree.getItems()) {
                    if (treeItem.getData() instanceof CreaterepoTreeCategory) {
                        CreaterepoTreeCategory category = (CreaterepoTreeCategory) treeItem.getData();
                        // only check 1 category's tags, as all are loaded the same way
                        if (category.getName().equals(CreaterepoPreferenceConstants.PREF_DISTRO_TAG)) {
                            assertArrayEquals(DISTRO_TAGS, category.getTags().toArray());
                            break;
                        }
                    }
                }
            }
        });
    }

    /**
     * Initialize the treeviewer and tree. Needs access to UI thread when using
     * SWTBot tests. Need to wrap tests in Display.getDefault().syncExec().
     */
    private void initViewer() {
        viewer = new TreeViewer(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
        viewer.setContentProvider(new CreaterepoTreeContentProvider());
        viewer.setLabelProvider(new CreaterepoTreeLabelProvider());
        CreaterepoCategoryModel model = new CreaterepoCategoryModel(project);
        viewer.setInput(model);
        tree = viewer.getTree();
    }

    /**
     * Add some test tags in the preferences.
     *
     * @throws BackingStoreException
     */
    private void addTestPreferences() throws BackingStoreException {
        IEclipsePreferences pref = project.getEclipsePreferences();
        pref.put(CreaterepoPreferenceConstants.PREF_DISTRO_TAG, preparePrefValue(DISTRO_TAGS));
        pref.put(CreaterepoPreferenceConstants.PREF_CONTENT_TAG, preparePrefValue(CONTENT_TAGS));
        pref.put(CreaterepoPreferenceConstants.PREF_REPO_TAG, preparePrefValue(REPO_TAGS));
        pref.flush();
    }

    /**
     * Helper method to prepare the tags the way it should be stored in preferences.
     *
     * @param values The values to store.
     * @return The string as it should be stored.
     */
    private static String preparePrefValue(String[] values) {
        String str = ICreaterepoConstants.EMPTY_STRING;
        if (values.length > 0) {
            for (String temp : values) {
                str = str.concat(temp+ICreaterepoConstants.DELIMITER);
            }
            str = str.substring(0, str.length()-1);
        }
        return str;
    }

    /**
     * Helper method to check if the item is one of the correct categories.
     *
     * @param itemToCheck The item to check.
     * @return True if the item should exist, false otherwise.
     */
    private static boolean inCategory(String itemToCheck) {
        for (String str : CORRECT_CATEGORIES.keySet()) {
            if (str.equals(itemToCheck)) {
                return true;
            }
        }
        return false;
    }

}
