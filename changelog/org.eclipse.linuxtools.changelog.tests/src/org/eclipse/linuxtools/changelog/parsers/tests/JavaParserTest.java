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
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.linuxtools.changelog.core.IParserChangeLogContrib;
import org.eclipse.linuxtools.changelog.tests.fixtures.ChangeLogTestProject;
import org.eclipse.linuxtools.internal.changelog.core.ChangeLogExtensionManager;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * JavaParser test suite.
 *
 */
public class JavaParserTest {

    // A unique string to mark the place of current selection in source code
    private static final String OFFSET_MARKER = "<# selection #>";
    // The parser under test
    private IParserChangeLogContrib javaParser;
    // A faked project
    private ChangeLogTestProject project;
    // The IEditorPart corresponding to the ChangeLog file
    private IEditorPart javaSourceEditorPart = null;

    @Before
    public void setUp() throws Exception {
        javaParser = ChangeLogExtensionManager.getExtensionManager().getParserContributor("CompilationUnitEditor");
        project = new ChangeLogTestProject("java-parser-test-project");
        // make it a Java project
        project.addJavaNature();
    }

    @After
    public void tearDown() throws Exception {
        // Tests in this class use javaSourceEditorPart. In order to avoid
        // spill-over from previous runs, truncate content (i.e. manually set
        // content to the empty string).
        if (javaSourceEditorPart != null) {
            AbstractTextEditor castEditor = (AbstractTextEditor) javaSourceEditorPart;
            IDocumentProvider iDocProvider = castEditor.getDocumentProvider();
            IDocument changelogContentDoc = iDocProvider.getDocument(castEditor.getEditorInput());
            changelogContentDoc.set("");
            javaSourceEditorPart.doSave(null);
            // Also close open editor in order for default content to work.
            // I.e. avoid spill over from previous test runs
            closeEditor(javaSourceEditorPart);
        }
        project.getTestProject().delete(true, true, null); // dispose
    }

    /**
     * Given an IEditorPart we should be able to retrieve the current function
     * we are in.
     *
     * @throws Exception
     */
    @Test
    public void canParseCurrentMethod() throws Exception {
        final String expectedMethodName = "doSomething";
        final String javaSourceCode = "public class JavaParserExampleClass {\n"
                + "private void " + expectedMethodName + "(String param) {\n"
                    + "// "    + OFFSET_MARKER + "\n"
                    + "}\n"
                + "}\n";

        assertNull(project.getTestProject().findMember( new Path(
                                "/src/org/eclipse/changelog/tests/JavaParserExampleClass.java")));

        // Add JavaParserExampleClass.java to project
        InputStream newFileInputStream = new ByteArrayInputStream(
                javaSourceCode.getBytes());
        IFile javaSourceFile = project.addFileToProject(
                "/src/org/eclipse/changelog/tests",
                "JavaParserExampleClass.java", newFileInputStream);

        assertNotNull(project.getTestProject().findMember( new Path(
                                "/src/org/eclipse/changelog/tests/JavaParserExampleClass.java")));

        // Open a source file and get the IEditorPart
        javaSourceEditorPart = openEditor(javaSourceFile);
        // make sure changelog editor content is right before merging
        assertEquals(javaSourceCode, getContent(javaSourceEditorPart));

        // make sure we have the proper editor type
        assertTrue( javaSourceEditorPart instanceof AbstractDecoratedTextEditor );

        // Select the snippet we want
        int selectionStart = javaSourceCode.indexOf(OFFSET_MARKER);
        assertTrue(selectionStart >= 0);
        int selectionLength = OFFSET_MARKER.length();
        AbstractDecoratedTextEditor javaEditor = (AbstractDecoratedTextEditor) javaSourceEditorPart;
        javaEditor.getSelectionProvider().setSelection(
                new TextSelection(selectionStart, selectionLength));

        final String actualMethodName = javaParser.parseCurrentFunction(javaSourceEditorPart);

        assertEquals(expectedMethodName, actualMethodName);
    }

    /**
     * Given an IEditorPart we should be able to retrieve the currently selected
     * field.
     *
     * @throws Exception
     */
    @Test
    public void canParseSelectedField() throws Exception {
        final String expectedFieldName = "testVar";
        final String javaSourceCode = "public class JavaParserExampleClass {\n"
                 + "private String " + expectedFieldName + " = null;\n"
                + "private void someMethod(String param) {\n"
                    + "// empty \n"
                    + "}\n"
                + "}\n";

        assertNull(project.getTestProject().findMember( new Path(
                                "/src/org/eclipse/changelog/tests/JavaParserExampleClass.java")));

        // Add JavaParserExampleClass.java to project
        InputStream newFileInputStream = new ByteArrayInputStream(
                javaSourceCode.getBytes());
        IFile javaSourceFile = project.addFileToProject(
                "/src/org/eclipse/changelog/tests",
                "JavaParserExampleClass.java", newFileInputStream);

        assertNotNull(project.getTestProject().findMember( new Path(
                                "/src/org/eclipse/changelog/tests/JavaParserExampleClass.java")));

        // Open a source file and get the IEditorPart
        javaSourceEditorPart = openEditor(javaSourceFile);
        // make sure changelog editor content is right before merging
        assertEquals(javaSourceCode, getContent(javaSourceEditorPart));

        // make sure we have the proper editor type
        assertTrue( javaSourceEditorPart instanceof AbstractDecoratedTextEditor );

        // Select the snippet we want
        int selectionStart = javaSourceCode.indexOf(expectedFieldName);
        assertTrue(selectionStart >= 0);
        int selectionLength = expectedFieldName.length() - 3; // Shouldn't need to select the entire field
        AbstractDecoratedTextEditor javaEditor = (AbstractDecoratedTextEditor) javaSourceEditorPart;
        javaEditor.getSelectionProvider().setSelection(
                new TextSelection(selectionStart, selectionLength));

        final String actualFieldName = javaParser.parseCurrentFunction(javaSourceEditorPart);

        assertEquals(expectedFieldName, actualFieldName);
    }

    /**
     * Given an IEditorPart and current selection is in a static instance initializer
     * block, JavaParser should be able to figure out that we were in an static
     * initializer block.
     *
     * @throws Exception
     */
    @Test
    public void canIdentifyStaticInitializerWhenInStaticInstanceInitializer() throws Exception {
        final String javaSourceCode = "public class JavaParserExampleClass {\n"
                 + "private String someStrVariable = null;\n"
                 // create static instance initializer block
                 + "{\n"
                     + "// "    + OFFSET_MARKER + "\n"
                 + "}\n"
                + "private void someMethod(String param) {\n"
                    + "// empty \n"
                    + "}\n"
                + "}\n";

        assertNull(project.getTestProject().findMember( new Path(
                                "/src/org/eclipse/changelog/tests/JavaParserExampleClass.java")));

        // Add JavaParserExampleClass.java to project
        InputStream newFileInputStream = new ByteArrayInputStream(
                javaSourceCode.getBytes());
        IFile javaSourceFile = project.addFileToProject(
                "/src/org/eclipse/changelog/tests",
                "JavaParserExampleClass.java", newFileInputStream);

        assertNotNull(project.getTestProject().findMember( new Path(
                                "/src/org/eclipse/changelog/tests/JavaParserExampleClass.java")));

        // Open a source file and get the IEditorPart
        javaSourceEditorPart = openEditor(javaSourceFile);
        // make sure changelog editor content is right before merging
        assertEquals(javaSourceCode, getContent(javaSourceEditorPart));

        // make sure we have the proper editor type
        assertTrue( javaSourceEditorPart instanceof AbstractDecoratedTextEditor );

        // Select the snippet we want
        int selectionStart = javaSourceCode.indexOf(OFFSET_MARKER);
        assertTrue(selectionStart >= 0);
        int selectionLength = OFFSET_MARKER.length();
        AbstractDecoratedTextEditor javaEditor = (AbstractDecoratedTextEditor) javaSourceEditorPart;
        javaEditor.getSelectionProvider().setSelection(
                new TextSelection(selectionStart, selectionLength));

        final String actualFunctionName = javaParser.parseCurrentFunction(javaSourceEditorPart);

        assertEquals("static initializer", actualFunctionName);
    }

    /**
     * Given an IEditorPart and current selection is in a static class initializer
     * block, JavaParser should be able to figure out that we were in an static
     * initializer block.
     *
     * @throws Exception
     */
    @Test
    public void canIdentifyStaticInitializerWhenInStaticClassInitializer() throws Exception {
        final String javaSourceCode = "public class JavaParserExampleClass {\n"
                 + "private String someStrVariable = null;\n"
                 // create static class initializer block
                 + "static {\n"
                     + "// "    + OFFSET_MARKER + "\n"
                 + "}\n"
                + "private void someMethod(String param) {\n"
                    + "// empty \n"
                    + "}\n"
                + "}\n";

        assertNull(project.getTestProject().findMember( new Path(
                                "/src/org/eclipse/changelog/tests/JavaParserExampleClass.java")));

        // Add JavaParserExampleClass.java to project
        InputStream newFileInputStream = new ByteArrayInputStream(
                javaSourceCode.getBytes());
        IFile javaSourceFile = project.addFileToProject(
                "/src/org/eclipse/changelog/tests",
                "JavaParserExampleClass.java", newFileInputStream);

        assertNotNull(project.getTestProject().findMember( new Path(
                                "/src/org/eclipse/changelog/tests/JavaParserExampleClass.java")));

        // Open a source file and get the IEditorPart
        javaSourceEditorPart = openEditor(javaSourceFile);
        // make sure changelog editor content is right before merging
        assertEquals(javaSourceCode, getContent(javaSourceEditorPart));

        // make sure we have the proper editor type
        assertTrue( javaSourceEditorPart instanceof AbstractDecoratedTextEditor );

        // Select the snippet we want
        int selectionStart = javaSourceCode.indexOf(OFFSET_MARKER);
        assertTrue(selectionStart >= 0);
        int selectionLength = OFFSET_MARKER.length();
        AbstractDecoratedTextEditor javaEditor = (AbstractDecoratedTextEditor) javaSourceEditorPart;
        javaEditor.getSelectionProvider().setSelection(
                new TextSelection(selectionStart, selectionLength));

        final String actualFunctionName = javaParser.parseCurrentFunction(javaSourceEditorPart);

        assertEquals("static initializer", actualFunctionName);
    }

    /**
     * Given an IEditorPart and current selection is inside a method within a nested
     * class, JavaParser should return a "nestedClass.methodName" construct for the
     * current function.
     *
     * @throws Exception
     */
    @Test
    public void canIdentifyMethodWithinNestedClass() throws Exception {
        final String nestedClassName = "Encapsulated";
        final String currentMethodName = "getString";
        final String javaSourceCode = "public class JavaParserExampleClass {\n"
                 + "private String someStrVariable = null;\n"
                 + "static {\n"
                     + "// empty \n"
                 + "}\n"
                + "private void someMethod(String param) {\n"
                    + "// empty \n"
                    + "}\n"
                + "private class " + nestedClassName + "{\n"
                        + "public String " + currentMethodName + "() throws Exception {\n"
                        + "// " + OFFSET_MARKER + "\n"
                        + "return \"returnString\";\n"
                        + "}\n"
                    + "}\n"
                + "}\n";

        assertNull(project.getTestProject().findMember( new Path(
                                "/src/org/eclipse/changelog/tests/JavaParserExampleClass.java")));

        // Add JavaParserExampleClass.java to project
        InputStream newFileInputStream = new ByteArrayInputStream(
                javaSourceCode.getBytes());
        IFile javaSourceFile = project.addFileToProject(
                "/src/org/eclipse/changelog/tests",
                "JavaParserExampleClass.java", newFileInputStream);

        assertNotNull(project.getTestProject().findMember( new Path(
                                "/src/org/eclipse/changelog/tests/JavaParserExampleClass.java")));

        // Open a source file and get the IEditorPart
        javaSourceEditorPart = openEditor(javaSourceFile);
        // make sure changelog editor content is right before merging
        assertEquals(javaSourceCode, getContent(javaSourceEditorPart));

        // make sure we have the proper editor type
        assertTrue( javaSourceEditorPart instanceof AbstractDecoratedTextEditor );

        // Select the snippet we want
        int selectionStart = javaSourceCode.indexOf(OFFSET_MARKER);
        assertTrue(selectionStart >= 0);
        int selectionLength = OFFSET_MARKER.length();
        AbstractDecoratedTextEditor javaEditor = (AbstractDecoratedTextEditor) javaSourceEditorPart;
        javaEditor.getSelectionProvider().setSelection(
                new TextSelection(selectionStart, selectionLength));

        final String expectedFunctionName = nestedClassName + "." + currentMethodName;
        final String actualFunctionName = javaParser.parseCurrentFunction(javaSourceEditorPart);

        assertEquals(expectedFunctionName, actualFunctionName);
    }

    /**
     * Given an IEditorPart and currently a field within a nested
     * class is selected, JavaParser should return a "nestedClass.fieldName"
     * construct for the current function.
     *
     * @throws Exception
     */
    @Test
    public void canIdentifyFieldWithinNestedClass() throws Exception {
        final String nestedClassName = "Encapsulated";
        final String currentFieldName = "variableInNestedClass";
        final String javaSourceCode = "public class JavaParserExampleClass {\n"
                 + "private String someStrVariable = null;\n"
                 + "static {\n"
                     + "// empty \n"
                 + "}\n"
                + "private void someMethod(String param) {\n"
                    + "// empty \n"
                    + "}\n"
                + "private class " + nestedClassName + "{\n"
                        + "private int " + currentFieldName + " = 10;\n"
                        + "public String getString() throws Exception {\n"
                        + "// return a String, yay ;-)\n"
                        + "return \"returnString\";\n"
                        + "}\n"
                    + "}\n"
                + "}\n";

        assertNull(project.getTestProject().findMember( new Path(
                                "/src/org/eclipse/changelog/tests/JavaParserExampleClass.java")));

        // Add JavaParserExampleClass.java to project
        InputStream newFileInputStream = new ByteArrayInputStream(
                javaSourceCode.getBytes());
        IFile javaSourceFile = project.addFileToProject(
                "/src/org/eclipse/changelog/tests",
                "JavaParserExampleClass.java", newFileInputStream);

        assertNotNull(project.getTestProject().findMember( new Path(
                                "/src/org/eclipse/changelog/tests/JavaParserExampleClass.java")));

        // Open a source file and get the IEditorPart
        javaSourceEditorPart = openEditor(javaSourceFile);
        // make sure changelog editor content is right before merging
        assertEquals(javaSourceCode, getContent(javaSourceEditorPart));

        // make sure we have the proper editor type
        assertTrue( javaSourceEditorPart instanceof AbstractDecoratedTextEditor );

        // Select the snippet we want
        int selectionStart = javaSourceCode.indexOf(currentFieldName);
        assertTrue(selectionStart >= 0);
        // select only a part of field name, as this should be sufficient
        int selectionLength = currentFieldName.length() - 3;
        AbstractDecoratedTextEditor javaEditor = (AbstractDecoratedTextEditor) javaSourceEditorPart;
        javaEditor.getSelectionProvider().setSelection(
                new TextSelection(selectionStart, selectionLength));

        final String expectedFunctionName = nestedClassName + "." + currentFieldName;
        final String actualFunctionName = javaParser.parseCurrentFunction(javaSourceEditorPart);

        assertEquals(expectedFunctionName, actualFunctionName);
    }

    /**
     * Given an IEditorPart and current selection is inside a class but not within a
     * method, not selecting a field and not in a nested class (somewhere else in the
     * class) it should return an empty string for the function.
     *
     * @throws Exception
     */
    @Test
    public void canDetermineThatSelectionIsJustInClass() throws Exception {
        // Apparently comments don't show up in the compilation units. Makes
        // sense, right? But we can't use the OFFSET_MARKER trick in this case.
        final String javaSourceCode = "public class JavaParserExampleClass {\n"
             + "private String someStrVariable = null;\n"
             + "\n" // want to select this line indexOf(';') + 2
             + "\n"
            + "private void someMethod(String param) {\n"
                + "// empty \n"
                + "}\n"
            + "}\n";

        assertNull(project.getTestProject().findMember( new Path(
                                "/src/org/eclipse/changelog/tests/JavaParserExampleClass.java")));

        // Add JavaParserExampleClass.java to project
        InputStream newFileInputStream = new ByteArrayInputStream(
                javaSourceCode.getBytes());
        IFile javaSourceFile = project.addFileToProject(
                "/src/org/eclipse/changelog/tests",
                "JavaParserExampleClass.java", newFileInputStream);

        assertNotNull(project.getTestProject().findMember( new Path(
                                "/src/org/eclipse/changelog/tests/JavaParserExampleClass.java")));

        // Open a source file and get the IEditorPart
        javaSourceEditorPart = openEditor(javaSourceFile);
        // make sure changelog editor content is right before merging
        assertEquals(javaSourceCode, getContent(javaSourceEditorPart));

        // make sure we have the proper editor type
        assertTrue( javaSourceEditorPart instanceof AbstractDecoratedTextEditor );

        // Select the right point
        int selectionStart = javaSourceCode.indexOf(';') + 2;
        assertTrue(selectionStart >= 2);
        int selectionLength = 0;
        AbstractDecoratedTextEditor javaEditor = (AbstractDecoratedTextEditor) javaSourceEditorPart;
        javaEditor.getSelectionProvider().setSelection(
                new TextSelection(selectionStart, selectionLength));

        final String actualFunctionName = javaParser.parseCurrentFunction(javaSourceEditorPart);

        assertEquals("" /* expect empty string */, actualFunctionName);
    }
}
