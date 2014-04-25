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
import static org.junit.Assert.assertNull;

import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileElement;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileSection;
import org.junit.Test;

/**
 * @author overholt
 *
 */
public class HeaderRecognitionTest extends FileTestCase {

    @Test
    public void testGetSimpleSectionName() {
        String testText = "%prep";
        SpecfileElement element;

        newFile(testText);
        element = parser.parseLine(testText, specfile, 0);
        assertEquals(SpecfileSection.class, element.getClass());
        assertEquals(testText.substring(1), ((SpecfileSection) element)
                .getName());
    }
    @Test
    public void testGetComplexSectionName1() {
        String testText = "%post";
        SpecfileElement element;

        newFile(testText);
        element = parser.parseLine(testText, specfile, 0);
        assertEquals(SpecfileSection.class, element.getClass());
        SpecfileSection section = (SpecfileSection) element;
        assertEquals(testText.substring(1), section.getName());
        assertNull(section.getPackage());
    }
    @Test
    public void testGetComplexSectionName2() {
        String testText = "%post -n";
        newFile(testText);
        specfile = parser.parse(testDocument);

        SpecfileTestFailure failure = getFailures().get(0);
        assertEquals(0, failure.getPosition().getOffset());
        assertEquals("No package name after -n in post section.",
                failure.getAnnotation().getText());
        assertEquals(testText.length(),
                failure.getPosition().getLength());
    }
    @Test
    public void testGetComplexSectionName3() {
        String testText = "%post -n name";
        String[] tokens = testText.split("\\s+");
        SpecfileElement element;

        newFile(testText);
        element = parser.parseLine(testText, specfile, 0);
        assertEquals(SpecfileSection.class, element.getClass());
        SpecfileSection section = (SpecfileSection) element;
        assertEquals(tokens[0].substring(1), section.getName());
        assertEquals(tokens[2], section.getPackage().getPackageName());
    }
    @Test
    public void testGetComplexSectionName4() {
        // FIXME: check for rest of line when -p is implemented
        // this should be an error case
        String testText = "%post -n name -p";
        String[] tokens = testText.split("\\s+");
        SpecfileElement element;

        newFile(testText);
        element = parser.parseLine(testText, specfile, 0);
        assertEquals(SpecfileSection.class, element.getClass());
        SpecfileSection section = (SpecfileSection) element;
        assertEquals(tokens[0].substring(1), section.getName());
        assertEquals(tokens[2], section.getPackage().getPackageName());
    }
    @Test
    public void testGetComplexSectionName5() {
        // FIXME: check for rest of line when -p is implemented
        // "blah bleh" should become the actual text of the section
        String testText = "%post -n name -p blah bleh";
        String[] tokens = testText.split("\\s+");
        SpecfileElement element;

        newFile(testText);
        element = parser.parseLine(testText, specfile, 0);
        assertEquals(SpecfileSection.class, element.getClass());
        SpecfileSection section = (SpecfileSection) element;
        assertEquals(tokens[0].substring(1), section.getName());
        assertEquals(tokens[2], section.getPackage().getPackageName());
    }
    @Test
    public void testGetComplexSectionName6() {
        String testText = "%post -p blah bleh";
        // FIXME: check for rest of line when -p is implemented
        // "blah bleh" should become the actual text of the section

        String[] tokens = testText.split("\\s+");
        SpecfileElement element;

        newFile(testText);
        element = parser.parseLine(testText, specfile, 0);
        assertEquals(SpecfileSection.class, element.getClass());
        SpecfileSection section = (SpecfileSection) element;
        assertEquals(tokens[0].substring(1), section.getName());
        assertNull(section.getPackage());
    }
    @Test
    public void testGetComplexSectionName7() {
        String testText = "%post -n -p blah";

        newFile(testText);
        specfile = parser.parse(testDocument);

        SpecfileTestFailure failure = getFailures().get(0);
        assertEquals(0, failure.getPosition().getOffset());
        assertEquals("Package name must not start with '-': -p.",
                failure.getAnnotation().getText());
        assertEquals(testText.length(),
                failure.getPosition().getLength());
    }
}
