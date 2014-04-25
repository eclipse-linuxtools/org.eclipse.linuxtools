/*******************************************************************************
 * Copyright (c) 2010 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.linuxtools.changelog.parsers.tests;

import static org.eclipse.linuxtools.changelog.tests.helpers.EditorHelper.closeEditor;
import static org.eclipse.linuxtools.changelog.tests.helpers.EditorHelper.getContent;
import static org.eclipse.linuxtools.changelog.tests.helpers.EditorHelper.openEditor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.linuxtools.changelog.core.IParserChangeLogContrib;
import org.eclipse.linuxtools.changelog.tests.fixtures.CStringStorage;
import org.eclipse.linuxtools.changelog.tests.fixtures.CStringStorageInput;
import org.eclipse.linuxtools.changelog.tests.fixtures.ChangeLogTestProject;
import org.eclipse.linuxtools.internal.changelog.core.ChangeLogExtensionManager;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * CParser test suite.
 *
 */
public class CParserTest {

    // A unique string to mark the place of current selection in source code
    private static final String OFFSET_MARKER = "<# selection #>";
    // The parser under test
    private IParserChangeLogContrib cParser;
    // A faked project
    private ChangeLogTestProject project;
    // The IEditorPart corresponding to the ChangeLog file
    private IEditorPart cppSourceEditorPart = null;

    @Before
    public void setUp() throws Exception {
        cParser = ChangeLogExtensionManager.getExtensionManager().getParserContributor("CEditor");
        project = new ChangeLogTestProject("c-parser-test-project");
    }

    @After
    public void tearDown() throws Exception {
        // Tests in this class use javaSourceEditorPart. In order to avoid
        // spill-over from previous runs, truncate content (i.e. manually set
        // content to the empty string).
        if (cppSourceEditorPart != null) {
            AbstractTextEditor castEditor = (AbstractTextEditor) cppSourceEditorPart;
            IDocumentProvider iDocProvider = castEditor.getDocumentProvider();
            IDocument changelogContentDoc = iDocProvider.getDocument(castEditor.getEditorInput());
            changelogContentDoc.set("");
            cppSourceEditorPart.doSave(null);
            // Also close open editor in order for default content to work.
            // I.e. avoid spill over from previous test runs
            closeEditor(cppSourceEditorPart);
        }
        project.getTestProject().delete(true, true, null); // dispose
    }

    /**
     * Given an IEditorPart we should be able to retrieve the currently
     * function active C function inside a C source file.
     *
     * @throws Exception
     */
    @Test
    public void canParseCurrentFunctionFromCFile() throws Exception {
        // make testproject a C project
        project.addCNature();

        final String expectedFunctionName = "doSomething";
        final String cSourceCode = "static int " + expectedFunctionName + "(char *test)\n"
                + "{\n"
                    + "int index = 0;\n"
                    + "// " + OFFSET_MARKER + "\n"
                    + "return 0;\n"
                + "}\n";

        assertNull(project.getTestProject().findMember( new Path(
                                "/src/some_c_file.c")));

        // Add some_c_file.c to project
        InputStream newFileInputStream = new ByteArrayInputStream(
                cSourceCode.getBytes());
        IFile cSourceFile = project.addFileToProject(
                "/src",
                "some_c_file.c", newFileInputStream);

        assertNotNull(project.getTestProject().findMember( new Path(
                                "/src/some_c_file.c")));

        // Open a source file and get the IEditorPart
        cppSourceEditorPart = openEditor(cSourceFile);
        assertEquals(cSourceCode, getContent(cppSourceEditorPart));

        // make sure we have the proper editor type
        assertTrue( cppSourceEditorPart instanceof AbstractTextEditor );

        // Select the snippet we want
        int selectionStart = cSourceCode.indexOf(OFFSET_MARKER);
        assertTrue(selectionStart >= 0);
        int selectionLength = OFFSET_MARKER.length();
        AbstractTextEditor cEditor = (AbstractTextEditor) cppSourceEditorPart;
        cEditor.getSelectionProvider().setSelection(
                new TextSelection(selectionStart, selectionLength));

        final String actualFunctionName = cParser.parseCurrentFunction(cppSourceEditorPart);

        assertEquals(expectedFunctionName, actualFunctionName);
    }

    /**
     * Given an IEditorPart and not being inside any function within a C
     * source file, no function should be determined.
     *
     * @throws Exception
     */
    @Test
    public void canDetermineThatInNoFunctionInCFile() throws Exception {
        // make test project a C project
        project.addCNature();

        final String cSourceCode = "// Prototype " + OFFSET_MARKER + "\n"
            + "static int doSomething(char *test);\n\n"
            + "static int doSomething(char *test)\n"
                + "{\n"
                    + "int index = 0;\n"
                    + "return 0;\n"
                + "}\n";

        assertNull(project.getTestProject().findMember( new Path(
                                "/src/some_c_file.c")));

        // Add some_c_file.c to project
        InputStream newFileInputStream = new ByteArrayInputStream(
                cSourceCode.getBytes());
        IFile cSourceFile = project.addFileToProject(
                "/src",
                "some_c_file.c", newFileInputStream);

        assertNotNull(project.getTestProject().findMember( new Path(
                                "/src/some_c_file.c")));

        // Open a source file and get the IEditorPart
        cppSourceEditorPart = openEditor(cSourceFile);
        assertEquals(cSourceCode, getContent(cppSourceEditorPart));

        // make sure we have the proper editor type
        assertTrue( cppSourceEditorPart instanceof AbstractTextEditor );

        // Select the snippet we want
        int selectionStart = cSourceCode.indexOf(OFFSET_MARKER);
        assertTrue(selectionStart >= 0);
        int selectionLength = OFFSET_MARKER.length();
        AbstractTextEditor cEditor = (AbstractTextEditor) cppSourceEditorPart;
        cEditor.getSelectionProvider().setSelection(
                new TextSelection(selectionStart, selectionLength));

        final String actualFunctionName = cParser.parseCurrentFunction(cppSourceEditorPart);

        assertEquals("" /* expect empty function name */, actualFunctionName);
    }

    /**
     * Given an IEditorPart we should be able to retrieve the currently selected
     * variable identifier inside a C++ file.
     *
     * @throws Exception
     */
    @Test
    public void canParseCurrentlySelectedVariableIdentifierInCppFile() throws Exception {
        // make test project a C++ project
        project.addCCNature();

        final String expectedIdentifier = "myIdentifier";
        final String className = "shape";
        final String cppSourceCode = "class " + className + " {\n"
                + "int x;\n"
                + "int y;\n"
            + "private:\n"
                + "int color;\n"
                + "float " + expectedIdentifier + ";\n"
                + "void set_color(int color);\n"
            +"}\n";

        assertNull(project.getTestProject().findMember( new Path(
                                "/src/shape.h")));

        // Add shape.h to project
        InputStream newFileInputStream = new ByteArrayInputStream(
                cppSourceCode.getBytes());
        IFile cppSourceFile = project.addFileToProject(
                "/src",
                "shape.h", newFileInputStream);

        assertNotNull(project.getTestProject().findMember( new Path(
                                "/src/shape.h")));

        // Open a source file and get the IEditorPart
        cppSourceEditorPart = openEditor(cppSourceFile);
        // make sure editor content is correct
        assertEquals(cppSourceCode, getContent(cppSourceEditorPart));

        // make sure we have the proper editor type
        assertTrue( cppSourceEditorPart instanceof AbstractTextEditor );

        // Select the snippet we want
        int selectionStart = cppSourceCode.indexOf(expectedIdentifier);
        assertTrue(selectionStart >= 0);
        // shouldn't need to mark whole length of identifier.
        int selectionLength = expectedIdentifier.length() - 3;
        AbstractTextEditor cppEditor = (AbstractTextEditor) cppSourceEditorPart;
        cppEditor.getSelectionProvider().setSelection(
                new TextSelection(selectionStart, selectionLength));

        final String actualIdentifier = cParser.parseCurrentFunction(cppSourceEditorPart);

        assertEquals(className + "." + expectedIdentifier, actualIdentifier);
    }

    /**
     * Given an IEditorPart and not selected any variable identifier in a class, we should
     * get the class name as selected function name only.
     *
     * @throws Exception
     */
    @Test
    public void canParseClassNameIfNoVariableIdentifierSelectedInCppFile() throws Exception {
        // make test project a C++ project
        project.addCCNature();

        final String className = "shape";
        final String cppSourceCode = "class " + className + " {\n"
                + "int x;\n"
                + "int y;\n"
                + "// " + OFFSET_MARKER + "\n"
            + "private:\n"
                + "int color;\n"
                + "void set_color(int color);\n"
            +"}\n";

        assertNull(project.getTestProject().findMember( new Path(
                                "/src/shape.h")));

        // Add shape.h to project
        InputStream newFileInputStream = new ByteArrayInputStream(
                cppSourceCode.getBytes());
        IFile cppSourceFile = project.addFileToProject(
                "/src",
                "shape.h", newFileInputStream);

        assertNotNull(project.getTestProject().findMember( new Path(
                                "/src/shape.h")));

        // Open a source file and get the IEditorPart
        cppSourceEditorPart = openEditor(cppSourceFile);
        // make sure editor content is correct
        assertEquals(cppSourceCode, getContent(cppSourceEditorPart));

        // make sure we have the proper editor type
        assertTrue( cppSourceEditorPart instanceof AbstractTextEditor );

        // Select the snippet we want
        int selectionStart = cppSourceCode.indexOf(OFFSET_MARKER);
        assertTrue(selectionStart >= 0);
        int selectionLength = OFFSET_MARKER.length();
        AbstractTextEditor cppEditor = (AbstractTextEditor) cppSourceEditorPart;
        cppEditor.getSelectionProvider().setSelection(
                new TextSelection(selectionStart, selectionLength));

        final String actualFunction = cParser.parseCurrentFunction(cppSourceEditorPart);

        assertEquals(className, actualFunction);
    }

    /**
     * Given an IEditorPart and current selection is inside a method,
     * CParser should be able to figure that out.
     *
     * @throws Exception
     */
    @Test
    public void canParseCurrentMethodNameInCppFile() throws Exception {
        // make test project a C++ project
        project.addCCNature();

        final String expectedMethodName = "circle::area";
        final String cppSourceCode = "#include \"circle.h\"\n"
            + "#include <math.h>\n"
            + "float " + expectedMethodName + "() {\n"
                + "// " + OFFSET_MARKER + "\n"
                + "return this->radius * this-> radius * M_PI\n"
            + "}\n";

        assertNull(project.getTestProject().findMember( new Path(
                                "/src/circle.cpp")));

        // Add shape.h to project
        InputStream newFileInputStream = new ByteArrayInputStream(
                cppSourceCode.getBytes());
        IFile cppSourceFile = project.addFileToProject(
                "/src",
                "circle.cpp", newFileInputStream);

        assertNotNull(project.getTestProject().findMember( new Path(
                                "/src/circle.cpp")));

        // Open a source file and get the IEditorPart
        cppSourceEditorPart = openEditor(cppSourceFile);
        // make sure editor content is correct
        assertEquals(cppSourceCode, getContent(cppSourceEditorPart));

        // make sure we have the proper editor type
        assertTrue( cppSourceEditorPart instanceof AbstractTextEditor );

        // Select the snippet we want
        int selectionStart = cppSourceCode.indexOf(OFFSET_MARKER);
        assertTrue(selectionStart >= 0);
        int selectionLength = OFFSET_MARKER.length();
        AbstractTextEditor cppEditor = (AbstractTextEditor) cppSourceEditorPart;
        cppEditor.getSelectionProvider().setSelection(
                new TextSelection(selectionStart, selectionLength));

        final String actualMethodName = cParser.parseCurrentFunction(cppSourceEditorPart);

        assertEquals(expectedMethodName, actualMethodName);
    }

    /**
     * Given an IEditorPart and current selection is inside a method,
     * CParser should be able to figure that out.
     *
     * @throws Exception
     */
    @Test
    public void canParseCurrentFunctionInCppFile() throws Exception {
        // make test project a C++ project
        project.addCCNature();

        final String expectedFunction = "main";
        final String cppSourceCode = "#include \"circle.h\"\n"
            + "#include <math.h>\n"
            + "float circle::area() {\n"
                + "return this->radius * this-> radius * M_PI\n"
            + "}\n"
            + "int " + expectedFunction + "() {\n"
                + "int some_var = 0;\n"
                + "// " + OFFSET_MARKER + "\n"
                + "return 0;\n"
            + "}\n";

        assertNull(project.getTestProject().findMember( new Path(
                                "/src/circle.cpp")));

        // Add shape.h to project
        InputStream newFileInputStream = new ByteArrayInputStream(
                cppSourceCode.getBytes());
        IFile cppSourceFile = project.addFileToProject(
                "/src",
                "circle.cpp", newFileInputStream);

        assertNotNull(project.getTestProject().findMember( new Path(
                                "/src/circle.cpp")));

        // Open a source file and get the IEditorPart
        cppSourceEditorPart = openEditor(cppSourceFile);
        // make sure editor content is correct
        assertEquals(cppSourceCode, getContent(cppSourceEditorPart));

        // make sure we have the proper editor type
        assertTrue( cppSourceEditorPart instanceof AbstractTextEditor );

        // Select the snippet we want
        int selectionStart = cppSourceCode.indexOf(OFFSET_MARKER);
        assertTrue(selectionStart >= 0);
        int selectionLength = OFFSET_MARKER.length();
        AbstractTextEditor cppEditor = (AbstractTextEditor) cppSourceEditorPart;
        cppEditor.getSelectionProvider().setSelection(
                new TextSelection(selectionStart, selectionLength));

        final String actualFunction = cParser.parseCurrentFunction(cppSourceEditorPart);

        assertEquals(expectedFunction, actualFunction);
    }

    /**
     * Given an IStorageEditorInput we should be able to retrieve the currently
     * active C function.
     *
     * @throws Exception
     */
    @Test
    public void canParseCurrentFunctionFromCStringInIStorageEditorInput() throws Exception {
        final String expectedFunctionName = "doSomething";
        final String cSourceCode = "static int " + expectedFunctionName + "(char *test)\n"
                + "{\n"
                    + "int index = 0;\n"
                    + "// " + OFFSET_MARKER + "\n"
                    + "return 0;\n"
                + "}\n";

        // prepare IStorageEditorInput
        IStorage cStringStorage = new CStringStorage(cSourceCode);
        IStorageEditorInput cStringStorageEditorInput = new CStringStorageInput(cStringStorage);

        // Figure out the desired offset
        int selectOffset = cSourceCode.indexOf(OFFSET_MARKER);
        assertTrue(selectOffset >= 0);

        final String actualFunctionName = cParser.parseCurrentFunction(cStringStorageEditorInput, selectOffset);

        assertEquals(expectedFunctionName, actualFunctionName);
    }
}
