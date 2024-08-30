/*******************************************************************************
 * Copyright (c) 2015, 2018 QNX Software Systems and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.valgrind.core.tests;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.io.TempDir;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * This is abstract class for juni4 test classes that use line method comments to extract test data.
 * Test plugin must have "src" folder on its bundle class path.
 */
public abstract class AbstractInlineDataTest {
	
	@TempDir
	protected File tmpfiles;
	
	/**
	 * Gets last comment section (comments that use //) above method with a given name
	 * @param info Test info
	 * @return comment section without leading // and leading whitespaces
	 */
	protected String getAboveComment(TestInfo info) {
		return getContents(1, info.getTestMethod().get().getName())[0].toString();
	}

	/**
	 * Saves comment above test method in the named file in temp dir, if fileName is null it will get a random name.
	 * File will be auto-removed after test is finished
	 * @param fileName - base file name of the temp file or null
	 * @param info Test info
	 * @throws IOException if failed to write file
	 */
	protected File getAboveCommentAndSaveFile(String fileName, TestInfo info) throws IOException {
		String value = getAboveComment(info);
		File file = new File(tmpfiles, fileName);
		saveToFile(value, file);
		return file;
	}

	protected void saveToFile(String value, File file) throws IOException, FileNotFoundException {
		try (FileOutputStream st = new FileOutputStream(file)) {
			st.write(value.getBytes(StandardCharsets.UTF_8));
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
