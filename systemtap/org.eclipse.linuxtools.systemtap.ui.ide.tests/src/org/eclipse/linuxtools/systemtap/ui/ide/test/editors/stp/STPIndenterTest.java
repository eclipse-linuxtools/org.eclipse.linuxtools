/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.linuxtools.systemtap.ui.ide.test.editors.stp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.LineRange;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp.IndentUtil;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp.STPDocumentProvider;
import org.junit.Assert;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleReference;

/**
 * Tests for the CIndenter.
 *
 * @since 4.0
 */
public class STPIndenterTest extends TestCase {

	private static class MockSTPDocumentProvider extends STPDocumentProvider{
		private IDocument document;

		MockSTPDocumentProvider(IDocument document){
			this.document = document;
			this.setupDocument(document);
		}

		@Override
		protected IDocument createDocument(Object element) {
			return document;
		}
	}

    /**
     * Reads multiple sections in comments from the source of the given class.
     * @since 4.0
     */
	public StringBuilder[] getContentsForTest(int sections) throws IOException {
		ClassLoader cl = getClass().getClassLoader();
		Bundle bundle = null;
		if (cl instanceof BundleReference)
			bundle = ((BundleReference) cl).getBundle();

		return getContentsForTest(bundle, "src",  //$NON-NLs-1$
				getClass(), getName(), sections);
	}

	/**
	 * Returns an array of StringBuilder objects for each comment section found preceding the named
	 * test in the source code.
	 *
	 * @param bundle the bundle containing the source, if {@code null} can try to load using
	 *      classpath (source folder has to be in the classpath for this to work)
	 * @param srcRoot the directory inside the bundle containing the packages
	 * @param clazz the name of the class containing the test
	 * @param testName the name of the test
	 * @param numSections the number of comment sections preceding the named test to return.
	 *     Pass zero to get all available sections.
	 * @return an array of StringBuilder objects for each comment section found preceding the named
	 *     test in the source code.
	 * @throws IOException
	 */
	public static StringBuilder[] getContentsForTest(Bundle bundle, String srcRoot, Class<?> clazz,
			final String testName, int numSections) throws IOException {
		// Walk up the class inheritance chain until we find the test method.
		try {
			while (clazz.getMethod(testName).getDeclaringClass() != clazz) {
				clazz = clazz.getSuperclass();
			}
		} catch (SecurityException e) {
			Assert.fail(e.getMessage());
		} catch (NoSuchMethodException e) {
			Assert.fail(e.getMessage());
		}

		while (true) {
			// Find and open the .java file for the class clazz.
			String fqn = clazz.getName().replace('.', '/');
			fqn = fqn.indexOf("$") == -1 ? fqn : fqn.substring(0, fqn.indexOf("$"));
			String classFile = fqn + ".java";
			IPath filePath= new Path(srcRoot + '/' + classFile);

			InputStream in;
			Class<?> superclass = clazz.getSuperclass();
			try {
				if (bundle != null) {
					in = FileLocator.openStream(bundle, filePath, false);
				} else {
					in = clazz.getResourceAsStream('/' + classFile);
				}
			} catch (IOException e) {
				if (superclass == null || !superclass.getPackage().equals(clazz.getPackage())) {
					throw e;
				}
				clazz = superclass;
				continue;
			}

		    BufferedReader br = new BufferedReader(new InputStreamReader(in));
		    try {
		    	// Read the java file collecting comments until we encounter the test method.
			    List<StringBuilder> contents = new ArrayList<StringBuilder>();
			    StringBuilder content = new StringBuilder();
			    for (String line = br.readLine(); line != null; line = br.readLine()) {
			    	line = line.replaceFirst("^\\s*", ""); // Replace leading whitespace, preserve trailing
			    	if (line.startsWith("//")) {
			    		content.append(line.substring(2) + "\n");
			    	} else {
			    		if (!line.startsWith("@") && content.length() > 0) {
			    			contents.add(content);
			    			if (numSections > 0 && contents.size() == numSections + 1)
			    				contents.remove(0);
			    			content = new StringBuilder();
			    		}
			    		if (line.length() > 0 && !contents.isEmpty()) {
			    			int idx= line.indexOf(testName);
			    			if (idx != -1 && !Character.isJavaIdentifierPart(line.charAt(idx + testName.length()))) {
			    				return contents.toArray(new StringBuilder[contents.size()]);
			    			}
			    			if (!line.startsWith("@")) {
			    				contents.clear();
			    			}
			    		}
			    	}
			    }
		    } finally {
		    	br.close();
		    }

			if (superclass == null || !superclass.getPackage().equals(clazz.getPackage())) {
			    throw new IOException("Test data not found for " + clazz.getName() + "." + testName);
			}
			clazz = superclass;
		}
	}

	protected void assertIndenterResult() throws Exception {
		StringBuilder[] contents= getContentsForTest(2);
		String before= contents[0].toString();
		IDocument document= new Document(before);
		String expected= contents[1].toString();
		new MockSTPDocumentProvider(document);
		int numLines = document.getNumberOfLines();
		if (document.getLineLength(numLines - 1) == 0) {
			numLines--;  // Exclude an empty line at the end.
		}
		IndentUtil.indentLines(document, new LineRange(0, numLines), null, null);
		assertEquals(expected, document.get());
	}

	//if (a == b) {
	// k = 7

	//if (a == b) {
	//	k = 7
	public void testIfStatement() throws Exception {
		assertIndenterResult();
	}

	//if (a == b)
	// k = 7
	//   else
	//k = 9

	//if (a == b)
	//	k = 7
	//else
	//	k = 9
	public void testIfElseStatement() throws Exception {
		assertIndenterResult();
	}

	//for (i = 0; i < 3; ++i) {
	// k = 7

	//for (i = 0; i < 3; ++i) {
	//	k = 7
	public void testForStatement() throws Exception {
		assertIndenterResult();
	}

	//while (i < 3) {
	// k = 7

	//while (i < 3) {
	//	k = 7
	public void testWhileStatement() throws Exception {
		assertIndenterResult();
	}

	//foreach (i+ in arr) {
	// k = 7

	//foreach (i+ in arr) {
	//	k = 7
	public void testForeachStatement() throws Exception {
		assertIndenterResult();
	}

	//foo(arg,
	//"string");

	//foo(arg,
	//	"string");
	public void testStringLiteralAsLastArgument_1_Bug192412() throws Exception {
		assertIndenterResult();
	}


	//if (1)
	//foo->bar();
	//dontIndent();

	//if (1)
	//	foo->bar();
	//dontIndent();
	public void testIndentationAfterArrowOperator_Bug192412() throws Exception {
		assertIndenterResult();
	}

	//if (1)
	//foo>>bar;
	//  dontIndent();

	//if (1)
	//	foo>>bar;
	//dontIndent();
	public void testIndentationAfterShiftRight_Bug192412() throws Exception {
		assertIndenterResult();
	}

	//if (1)
	//foo >= bar();
	//  dontIndent();

	//if (1)
	//	foo >= bar();
	//dontIndent();
	public void testIndentationAfterGreaterOrEquals_Bug192412() throws Exception {
		assertIndenterResult();
	}

	//int a[]=
	//{
	//1,
	//2
	//};

	//int a[]=
	//{
	// 1,
	// 2
	//};
	public void testInitializerLists_Bug194585() throws Exception {
		assertIndenterResult();
	}


	//x =
	//0;

	//x =
	//		0;
	public void testWrappedAssignment_1_Bug277624() throws Exception {
		assertIndenterResult();
	}

	//{
	//a = 0;
	//x = 2 +
	//2 +
	//2;

	//{
	//	a = 0;
	//	x = 2 +
	//			2 +
	//			2;
	public void testWrappedAssignment_2_Bug277624() throws Exception {
		assertIndenterResult();
	}

	//if (1 > 0) {
	//double d = a * b /
	//c;

	//if (1 > 0) {
	//	double d = a * b /
	//			c;
	public void testWrappedAssignment_3_Bug277624() throws Exception {
		assertIndenterResult();
	}

	//int x = 1 < 2 ?
	//f(0) :
	//1;
	//g();

	//int x = 1 < 2 ?
	//		f(0) :
	//		1;
	//g();
	public void testConditionalExpression_Bug283970() throws Exception {
		assertIndenterResult();
	}

	//for (int i = 0;
	//i < 2; i++)

	//for (int i = 0;
	//		i < 2; i++)
	public void testWrappedFor_1_Bug277625() throws Exception {
		assertIndenterResult();
	}

	//for (int i = 0; i < 2;
	//i++)

	//for (int i = 0; i < 2;
	//		i++)
	public void testWrappedFor_2_Bug277625() throws Exception {
		assertIndenterResult();
	}

	//for (int i = 0;
	//i < 2;
	//i++)
	//{

	//for (int i = 0;
	//		i < 2;
	//		i++)
	//{
	public void testWrappedFor_3_Bug277625() throws Exception {
		assertIndenterResult();
	}

}
