/*******************************************************************************
 * Copyright (c) 2010 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.linuxtools.changelog.tests.fixtures;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.eclipse.cdt.core.model.CoreModel;

/**
 * @author Redhat Inc.
 *
 */
public class TestChangeLogTestProject {

    private ChangeLogTestProject project;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        project = new ChangeLogTestProject("com.redhat.testchangelog.project");
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        // dispose
        project.getTestProject().delete(true, null);
    }

    /**
     * Test method for
     * {@link org.eclipse.linuxtools.changelog.tests.fixtures.ChangeLogTestProject#addFileToProject(java.lang.String, java.lang.String, java.io.InputStream)}
     * .
     */
    @Test
    public void testAddFileToProject() throws Exception {
        String fileContent = "some content";
        InputStream newFileInputStream = new ByteArrayInputStream(
                fileContent.getBytes());
        // Create file "testfile.txt" with content "some content" at
        // "/this/is/a/testpath"
        project.addFileToProject("/this/is/a/testpath", "testfile.txt",
                newFileInputStream);
        IProject p = project.getTestProject();

        // Paths along the way should have been created
        IResource member = p.findMember(new Path("/this"));
        assertNotNull(member);
        assertEquals("this", member.getName());
        member = p.findMember(new Path("/this/is"));
        assertNotNull(member);
        assertEquals("is", member.getName());
        member = p.findMember(new Path("/this/is/a"));
        assertNotNull(member);
        assertEquals("a", member.getName());
        member = p.findMember(new Path("/this/is/a/testpath"));
        assertNotNull(member);
        assertEquals("testpath", member.getName());

        // Testfile should be around
        IResource createdFile = p.findMember(new Path("/this/is/a/testpath/testfile.txt"));
        assertNotNull(createdFile);
        assertEquals("this/is/a/testpath/testfile.txt", createdFile.getProjectRelativePath().toString());
        assertEquals("testfile.txt", createdFile.getName());
        assertTrue(createdFile instanceof IFile);

        // Content should be "some content"
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(createdFile.getLocation().toFile())))) {
            String actualContent = br.readLine();
            assertEquals(fileContent, actualContent);
        }

        // this should throw an IllegalStateException
        boolean exceptionTrown = false;
        try {
            project.addFileToProject("donnot/care", "not_important.java",
                    null /* should cause exception */);
        } catch (IllegalStateException e) {
            // pass
            exceptionTrown = true;
        }
        assertTrue(exceptionTrown);
    }

    /**
     * We should be able to add a Java nature to a project.
     *
     * @throws Exception
     */
    @Test
    public void canAddJavaNature() throws Exception {
        IJavaProject javaProject = this.project.addJavaNature();
        assertNotNull(javaProject);
    }

    /**
     * We should be able to add a C nature to a project.
     *
     * @throws Exception
     */
    @Test
    public void canAddCNature() throws Exception {
        IProject cProject = this.project.getTestProject();
        assertFalse(CoreModel.hasCNature(cProject));
        // Add C nature
        this.project.addCNature();
        cProject = this.project.getTestProject();
        assertTrue(CoreModel.hasCNature(cProject));
    }

    /**
     * We should be able to add a C++ nature to a project.
     *
     * @throws Exception
     */
    @Test
    public void canAddCCNature() throws Exception {
        IProject ccProject = this.project.getTestProject();
        assertFalse(CoreModel.hasCCNature(ccProject));
        // Add C++ nature
        this.project.addCCNature();
        ccProject = this.project.getTestProject();
        assertTrue(CoreModel.hasCCNature(ccProject));
    }

}
