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

import org.junit.Test;

public class NameTagTest extends FileTestCase {
    @Test
    public void testResolvedNameTag() {
        String testText = "%define blah notblah\nName: %{blah}";
        newFile(testText);
        assertEquals("notblah", specfile.getName());
    }

    @Test
    public void testResolvedSetName() {
        String testText = "%define blah notblah\nName: %{blah}";
        newFile(testText);
        assertEquals("notblah", specfile.getName());
    }

    @Test
    public void testNameTag() {
        String testText = "Name: blah";
        newFile(testText);
        assertEquals("blah", specfile.getName());
    }

    @Test
    public void testNameTag2() {
        String testText = "Name:    blah";
        newFile(testText);
        assertEquals("blah", specfile.getName());
    }

    @Test
    public void testNullNameTag() {
        String testText = "Name:    ";
        newFile(testText);
        SpecfileTestFailure failure = getFailures().get(0);
        assertEquals(0, failure.getPosition().getOffset());
        assertEquals(testText.length(), failure.getPosition().getLength());
        assertEquals("Name declaration without value.", failure.getAnnotation()
                .getText());
    }

    @Test
    public void testNullNameTag2() {
        String testText = "Name: ";
        newFile(testText);
        SpecfileTestFailure failure = getFailures().get(0);
        assertEquals(0, failure.getPosition().getOffset());
        assertEquals(testText.length(), failure.getPosition().getLength());
        assertEquals("Name declaration without value.", failure.getAnnotation()
                .getText());
    }

    @Test
    public void testMultipleNamesTag() {
        String testText = "Name: blah bleh";
        newFile(testText);
        SpecfileTestFailure failure = getFailures().get(0);
        assertEquals(0, failure.getPosition().getOffset());
        assertEquals(testText.length(), failure.getPosition().getLength());
        assertEquals("Name cannot have multiple values.", failure
                .getAnnotation().getText());
    }

    @Test
    public void testMultipleNamesTag2() {
        String testText = "Name:     blah bleh";
        newFile(testText);
        SpecfileTestFailure failure = getFailures().get(0);
        assertEquals(0, failure.getPosition().getOffset());
        assertEquals(testText.length(), failure.getPosition().getLength());
        assertEquals("Name cannot have multiple values.", failure
                .getAnnotation().getText());
    }
}
