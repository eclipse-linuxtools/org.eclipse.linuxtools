/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *******************************************************************************/
package com.st.stgcov.test;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

public class STGcovTestUtils {
	public static final String GCOV_FILE_SUFFIX = ".gcda"; 
	public static final String GCOV_BINARY_FILE_SUFFIX = ".out";
	public static final String GCOV_DIRECTORY_SUFFIX = "_gcov_input";

	public static String[] getGCDANames(File directory) {
		String[] covFiles = directory.list(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(GCOV_FILE_SUFFIX);
			}
		});
		Arrays.sort(covFiles);
		return covFiles;
	}

	public static File[] getGCDA(File directory) {
		File[] covFiles = directory.listFiles(new FileFilter() {
			public boolean accept(File arg0) {
				return ( arg0.isDirectory() == false && arg0.getName().endsWith(GCOV_FILE_SUFFIX));
			}
		});
		Arrays.sort(covFiles);
		return covFiles;
	}

	public static List<String> getGCDAPath(File directory) {
		File[] covFiles = getGCDA(directory);
		final List<String> covPaths = new ArrayList<String>(covFiles.length);
		for (File file : covFiles) {
			covPaths.add(file.getAbsolutePath());
		}
		return covPaths;
	}

	public static File getBinary(File directory) {
		File[] binaries = directory.listFiles(new FileFilter() {
			public boolean accept(File arg1) {
				return ( !arg1.isDirectory() && arg1.getName().endsWith(GCOV_BINARY_FILE_SUFFIX));
			}
		});
		Assert.assertEquals(1, binaries.length);
		return binaries[0];
	}

}
