/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.structures.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.eclipse.linuxtools.systemtap.structures.ZipArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ZipArchiveTest {

	@Before
	public void setUp() throws Exception {
		File f = new File("/tmp/test/a");
		f.getParentFile().mkdirs();
		f.createNewFile();
		
		ZipArchive.zipFiles("/tmp/test/a.zip", new String[] {"/tmp/test/a"}, new String[] {"a"});
		ZipArchive.compressFile("/tmp/test/a.gz", "/tmp/test/a.zip");
	}

	@Test
	public void testZipFiles() {
		File b = new File("/tmp/test/b.zip");
		assertFalse(b.exists());
		ZipArchive.zipFiles(b.getAbsolutePath(), new String[] {"/tmp/test/a", "/tmp/test/a.zip"}, new String[] {"a", "a.zip"});
		assertTrue(b.exists());
	}

	@Test
	public void testUnzipFiles() {
		File b = new File("/tmp/test/aa/");
		assertFalse(b.exists());
		b.mkdirs();
		ZipArchive.unzipFiles("/tmp/test/a.zip", b.getAbsolutePath());
		assertTrue(b.exists());
		assertTrue(new File(b.getAbsolutePath() + "a").exists());
	}

	@Test
	public void testCompressFile() {
		File b = new File("/tmp/test/b.gz");
		assertFalse(b.exists());
		ZipArchive.compressFile(b.getAbsolutePath(), "/tmp/test/a.zip");
		assertTrue(b.exists());
	}

	@Test
	public void testUncompressFile() {
		File b = new File("/tmp/test/bb/");
		assertFalse(b.exists());
		b.mkdirs();
		ZipArchive.uncompressFile(b.getAbsolutePath() + "a.zip", "/tmp/test/a.gz");
		assertTrue(b.exists());
		assertTrue(new File(b.getAbsolutePath() + "a.zip").exists());
	}
	
	@After
	public void tearDown() {
		deleteFile(new File[]{new File("/tmp/test/")});
	}
	
	private void deleteFile(File[] files){
		for(File file: files){
			if (file.isDirectory()){
				deleteFile(file.listFiles());
			}
			file.delete();
		}
	}
}
