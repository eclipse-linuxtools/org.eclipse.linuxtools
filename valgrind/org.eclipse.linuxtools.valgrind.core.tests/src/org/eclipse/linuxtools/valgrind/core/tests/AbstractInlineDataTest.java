/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.valgrind.core.tests;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * This is abstract class for juni4 test classes that use line method comments to extract test data.
 * Test plugin must have "src" folder on its bundle class path.
 */
public abstract class AbstractInlineDataTest {
	public static final String UTF_8 = "UTF-8"; //$NON-NLS-1$
	public static final Charset CHARSET_UTF_8 = Charset.forName(UTF_8);
	@Rule public TemporaryFolder tmpfiles = new TemporaryFolder();
	@Rule public TestName testName = new TestName();

	/**
	 * Gets last comment section (comments that use //) above current test method
	 * @return comment section without leading // and leading whitespaces
	 */
	protected String getAboveComment() {
		return getAboveComment(getName());
	}

	/**
	 * @return current test method name
	 */
	protected String getName() {
		return testName.getMethodName();
	}

	/**
	 * Gets last comment section (comments that use //) above method with a given name
	 * @param name method name
	 * @return comment section without leading // and leading whitespaces
	 */
	protected String getAboveComment(String name) {
		return getContents(1, name)[0].toString();
	}

	/**
	 * Saves comment above test method in the named file in temp dir, if fileName is null it will get a random name.
	 * File will be auto-removed after test is finished
	 * @param fileName - base file name of the temp file or null
	 * @return temp file
	 * @throws IOException if failed to write file
	 */
	protected File getAboveCommentAndSaveFile(String fileName) throws IOException {
		String value = getAboveComment(getName());
		File file = fileName == null ? tmpfiles.newFile() : tmpfiles.newFile(fileName);
		saveToFile(value, file);
		return file;
	}

	protected void saveToFile(String value, File file) throws IOException, FileNotFoundException {
		try (FileOutputStream st = new FileOutputStream(file)) {
			st.write(value.getBytes(CHARSET_UTF_8));
		}
	}

	protected StringBuilder[] getContents(int sections, String name) {
		try {
			return TestSourceReader.getContentsForTest(getBundle(), getSourcePrefix(), getClass(), name, sections);
		} catch (IOException e) {
			fail(e.getMessage());
			return null;
		}
	}

	/**
	 * Source directory of the test bundle. Can be overriden if source files that are used to pull comments are not in src.
	 * @return source dir prefix
	 */
	protected String getSourcePrefix() {
		return "src";
	}

	protected Bundle getBundle() {
		return FrameworkUtil.getBundle(getClass());
	}
}
