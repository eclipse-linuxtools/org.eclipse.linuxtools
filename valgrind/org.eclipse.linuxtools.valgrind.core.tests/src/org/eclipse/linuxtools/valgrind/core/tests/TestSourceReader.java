/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alena Laskavaia - Initial API and Implementation
 *******************************************************************************/
package org.eclipse.linuxtools.valgrind.core.tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.Assert;
import org.osgi.framework.Bundle;

/**
 * Utilities for reading test source code from plug-in .java sources
 */
public class TestSourceReader {
    private final Bundle bundle;
    private final String srcRoot;
    private final Class<?> clazz;
    private final int numSections;

    /**
     * @param bundle
     *            the bundle containing the source, if {@code null} can try to load using classpath (source folder has
     *            to be in the classpath for this to work)
     * @param srcRoot
     *            the directory inside the bundle containing the packages
     * @param clazz
     *            the name of the class containing the test
     */
    public TestSourceReader(Bundle bundle, String srcRoot, Class<?> clazz) {
        this(bundle, srcRoot, clazz, 0);
    }

    /**
     * @param bundle
     *            the bundle containing the source, if {@code null} can try to load using classpath (source folder has
     *            to be in the classpath for this to work)
     * @param srcRoot
     *            the directory inside the bundle containing the packages
     * @param clazz
     *            the name of the class containing the test
     * @param numSections
     *            the number of comment sections preceding the named test to return. Pass zero to get all available
     *            sections.
     */
    public TestSourceReader(Bundle bundle, String srcRoot, Class<?> clazz, int numSections) {
        this.bundle = bundle;
        this.srcRoot = srcRoot;
        this.clazz = clazz;
        this.numSections = numSections;
    }

    public StringBuilder[] getContentsForTest(final String testName) throws IOException {
        return getContentsForTest(bundle, srcRoot, clazz, testName, numSections);
    }
    /**
     * Returns an array of StringBuilder objects for each comment section found preceding the named test in the source
     * code.
     *
     * @param bundle
     *            the bundle containing the source, if {@code null} can try to load using classpath (source folder has
     *            to be in the classpath for this to work)
     * @param srcRoot
     *            the directory inside the bundle containing the packages
     * @param clazz
     *            the name of the class containing the test
     * @param testName
     *            the name of the test
     * @param numSections
     *            the number of comment sections preceding the named test to return. Pass zero to get all available
     *            sections.
     * @return an array of StringBuilder objects for each comment section found preceding the named test in the source
     *         code.
     * @throws IOException if a source file is not found
     */
    public static StringBuilder[] getContentsForTest(Bundle bundle, String srcRoot, Class<?> clazz, final String testName,
            int numSections) throws IOException {
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
            IPath filePath = new Path(srcRoot + '/' + classFile);

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

			try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
				// Read the java file collecting comments until we encounter the test method.
				List<StringBuilder> contents = new ArrayList<>();
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
							int idx = line.indexOf(testName);
							if (idx != -1 && !Character.isJavaIdentifierPart(line.charAt(idx + testName.length()))) {
								return contents.toArray(new StringBuilder[contents.size()]);
							}
							if (!line.startsWith("@")) {
								contents.clear();
							}
						}
					}
				}
			}

            if (superclass == null || !superclass.getPackage().equals(clazz.getPackage())) {
                throw new IOException("Test data not found for " + clazz.getName() + "." + testName);
            }
            clazz = superclass;
        }
    }
}
