/*******************************************************************************
 * Copyright (c) 2010-2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.oprofile.core.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.internal.oprofile.core.opxml.checkevent.CheckEventAdapter;
import org.eclipse.linuxtools.internal.oprofile.core.opxml.info.InfoAdapter;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.osgi.framework.FrameworkUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Test cases for checking the validity of the info parsed from oprofile
 * that is modified to mimic the format expected by the SAX parser.
 * The oprofile module must be loaded and the driver interface must be
 * available. ie. run opcontrol --init
 */
public class TestCheckEventsPreParse {

	private static final String REL_PATH_TO_CHECKEVENT_BAD_COUNTER = "resources/test_check-event_invalid_counter.xml";
	private static final String REL_PATH_TO_CHECKEVENT_BAD_UMASK = "resources/test_check-event_invalid_umask.xml";
	private static final String REL_PATH_TO_CHECKEVENT_OK = "resources/test_check-event_ok.xml";
	private static final String REL_PATH_TO_INFO_PRE_PARSE_RAW = "resources/test_info_pre_parse_raw.xml";

	// the values are checked for validity in the order they
	// appear here (ctr, event, umask)
	private String ctr;
	private String umask;
	private CheckEventAdapter cea;

	/**
	 * Set the counter, existing event and its default unit mask.
	 */
	@Before
	public void setUp (){
		String devOprofileAbsFilePath = null;
		Path devOprofilePath = new Path("resources/dev/oprofile/");
		URL devOprofileURL = FileLocator.find(FrameworkUtil.getBundle(this.getClass()), devOprofilePath , null);
		try {
			devOprofileAbsFilePath = FileLocator.toFileURL(devOprofileURL).getFile();
		} catch (IOException e) {
			fail("Failed to convert the resource file's path.");
		}
		InfoAdapter.setOprofileDir(devOprofileAbsFilePath);

		File devFile = new File(InfoAdapter.DEV_OPROFILE + "0");
		if (devFile.exists()){
			ctr = "0";
		}
		File cpuFile = new File(InfoAdapter.CPUTYPE);

		try (BufferedReader bi = new BufferedReader(new FileReader(cpuFile))) {
			String cpuType = bi.readLine();
			File opArchEvents = new File(InfoAdapter.OP_SHARE + cpuType + "/"
					+ InfoAdapter.EVENTS);
			File opArchUnitMasks = new File(InfoAdapter.OP_SHARE + cpuType
					+ "/" + InfoAdapter.UNIT_MASKS);

			try (BufferedReader eventReader = new BufferedReader(
					new FileReader(opArchEvents))) {
				String line;
				while ((line = eventReader.readLine()) != null) {
					// find the first event and use it
					if (line.contains("name:")) {
						int start = line.indexOf("name:") + 5;
						int end = line.indexOf(" ", start);

						// get the string that references the unit mask type
						start = line.indexOf("um:") + 3;
						end = line.indexOf(" ", start);
						String um = line.substring(start, end);

						try (BufferedReader unitMaskReader = new BufferedReader(
								new FileReader(opArchUnitMasks))) {
							while ((line = unitMaskReader.readLine()) != null) {
								if (line.contains("name:" + um + " ")) {
									start = line.indexOf("default:") + 8;
									String unitMaskDef = line.substring(start);
									// convert from hex. to dec.
									unitMaskDef = unitMaskDef.replaceFirst(
											"0x", "");
									umask = String.valueOf(Integer.parseInt(
											unitMaskDef, 16));
									break;
								}
							}
						}
						break;
					}
				}
			}
		} catch (IOException e) {
		}
	}

	@Test
	public void testBadCounter () {
		ctr = "999";
		assertValidity(REL_PATH_TO_CHECKEVENT_BAD_COUNTER);
	}
	@Test
	public void testBadUnitMask (){
		umask = "999";
		assertValidity(REL_PATH_TO_CHECKEVENT_BAD_UMASK);
	}
	@Test @Ignore
	public void testOk (){
		assertValidity(REL_PATH_TO_CHECKEVENT_OK);
	}

	public void assertValidity (String path){
		IFileStore fileStore = null;
		String infoAbsFilePath = null;

		Path infoFilePath = new Path(REL_PATH_TO_INFO_PRE_PARSE_RAW);
		URL infoFileURL = FileLocator.find(FrameworkUtil.getBundle(this.getClass()), infoFilePath, null);
		try {
			infoAbsFilePath = FileLocator.toFileURL(infoFileURL).getFile();
			fileStore = EFS.getLocalFileSystem().getStore(new Path(infoAbsFilePath));
		} catch (IOException e) {
			fail("Failed to convert the resource file's path.");
		}

		InfoAdapter ia = new InfoAdapter(fileStore);
		ia.process();

		cea = new CheckEventAdapter(ctr, "CPU_CLK_UNHALTED", umask);
		cea.process();
		Document actualDocument = cea.getDocument();
		Element actualRoot = (Element) actualDocument.getElementsByTagName(CheckEventAdapter.CHECK_EVENTS).item(0);

		Path filePath = new Path(path);
		URL fileURL = FileLocator.find(FrameworkUtil.getBundle(this.getClass()), filePath, null);
		Element expectedRoot = null;
		try {
			String absFilePath = FileLocator.toFileURL(fileURL).getFile();
			File file = new File (absFilePath);
			FileInputStream inp = new FileInputStream(file);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder;
			builder = factory.newDocumentBuilder();
			Document expectedDocument = builder.parse(inp);
			expectedRoot = (Element) expectedDocument.getElementsByTagName(CheckEventAdapter.CHECK_EVENTS).item(0);

		} catch (FileNotFoundException e) {
			fail("File was not found.");
		} catch (IOException e) {
			fail("Failed to convert the resource file's path.");
		} catch (SAXException e) {
			fail("Failed to parse the XML.");
		} catch (ParserConfigurationException e) {
			fail("Failed to create a document builder.");
		}

		Element expectedResultTag = (Element) expectedRoot.getElementsByTagName(CheckEventAdapter.RESULT).item(0);
		Element actualResultTag = (Element) actualRoot.getElementsByTagName(CheckEventAdapter.RESULT).item(0);
		assertEquals(expectedResultTag.getTextContent(), actualResultTag.getTextContent());
	}
}
