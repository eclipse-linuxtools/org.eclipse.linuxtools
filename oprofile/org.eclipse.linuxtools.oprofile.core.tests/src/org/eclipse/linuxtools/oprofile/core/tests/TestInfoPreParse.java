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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.internal.oprofile.core.opxml.info.InfoAdapter;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.FrameworkUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Test cases for checking validity of the info parsed from oprofile that is
 * modified to mimic the XML format expected by the SAX parser.
 * The oprofile module must be loaded and the driver interface must be
 * available. ie. run opcontrol --init
 */
public class TestInfoPreParse {

	private static final String REL_PATH_TO_INFO_PRE_PARSE_RAW = "resources/test_info_pre_parse_raw.xml";
	private static final String REL_PATH_TO_INFO_PRE_PARSE_EXEPECTED = "resources/test_info_pre_parse_expected.xml";
	private Element [] rootList;
	private ArrayList<ArrayList<String>> valueList;

	@Before
	public void setUp() {
		String devOprofileAbsFilePath = null;
		Path devOprofilePath = new Path("resources/dev/oprofile/");
		URL devOprofileURL = FileLocator.find(FrameworkUtil.getBundle(this.getClass()), devOprofilePath , null);
		try {
			devOprofileAbsFilePath = FileLocator.toFileURL(devOprofileURL).getFile();
		} catch (IOException e) {
			fail("Failed to convert the resource file's path.");
		}
		InfoAdapter.setOprofileDir(devOprofileAbsFilePath);

		IFileStore fileStore = null;
		String absFilePath = null;

		Path filePath = new Path(REL_PATH_TO_INFO_PRE_PARSE_RAW);
		URL fileURL = FileLocator.find(FrameworkUtil.getBundle(this.getClass()), filePath, null);
		try {
			absFilePath = FileLocator.toFileURL(fileURL).getFile();
			fileStore = EFS.getLocalFileSystem().getStore(new Path(absFilePath));
		} catch (IOException e) {
			fail("Failed to convert the resource file's path.");
		}
		InfoAdapter ia = new InfoAdapter(fileStore);
		ia.process();
		Document actualDocument = ia.getDocument();
		Element actualRoot = (Element) actualDocument.getElementsByTagName(InfoAdapter.INFO).item(0);

		filePath = new Path(REL_PATH_TO_INFO_PRE_PARSE_EXEPECTED);
		fileURL = FileLocator.find(FrameworkUtil.getBundle(this.getClass()), filePath, null);
		Element expectedRoot = null;
		try {
			absFilePath = FileLocator.toFileURL(fileURL).getFile();
			fileStore = EFS.getLocalFileSystem().getStore(new Path(absFilePath));
			InputStream inp = fileStore.openInputStream(EFS.NONE, new NullProgressMonitor());
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder;
			builder = factory.newDocumentBuilder();
			Document expectedDocument = builder.parse(inp);
			expectedRoot = (Element) expectedDocument.getElementsByTagName(InfoAdapter.INFO).item(0);

		} catch (FileNotFoundException e) {
			fail("File was not found.");
		} catch (IOException e) {
			fail("Failed to convert the resource file's path.");
		} catch (SAXException e) {
			fail("Failed to parse the XML.");
		} catch (ParserConfigurationException e) {
			fail("Failed to create a document builder.");
		} catch (CoreException e) {
			fail("Failed to open output stream");
		}

		rootList = new Element [] {expectedRoot, actualRoot};
		valueList = new ArrayList<ArrayList<String>> ();

		for (int i = 0; i < rootList.length; i++){
			valueList.add(new ArrayList<String>());
		}
	}

	@Test
	public void testBasicConfig (){
		final String [] tags = new String [] {InfoAdapter.NUM_COUNTERS, InfoAdapter.TIMER_MODE};
		final String [] defTags = new String [] {InfoAdapter.SAMPLE_DIR, InfoAdapter.LOCK_FILE, InfoAdapter.LOG_FILE, InfoAdapter.DUMP_STATUS};


		// compare num-counters and timer-mode
		for (int i = 0; i < rootList.length; i++){
			for (int j = 0; j < tags.length; j++){
				Element elm = (Element) rootList[i].getElementsByTagName(tags[j]).item(0);
				valueList.get(i).add(elm.getTextContent());
			}
		}
		assertSameValues(valueList);
		clearValues(valueList);

		// compare defaults
		for (int i = 0; i < rootList.length; i++){
			Element defTag = (Element) rootList[i].getElementsByTagName(InfoAdapter.DEFAULTS).item(0);
			for (int j = 0; j < defTags.length; j++){
				Element elm = (Element) defTag.getElementsByTagName(defTags[j]).item(0);
				valueList.get(i).add(elm.getTextContent());
			}
		}
		assertSameValues(valueList);
		clearValues(valueList);
	}

	@Test
	public void testEventData (){
		final String [] eventTags = new String [] {InfoAdapter.NAME, InfoAdapter.DESCRIPTION, InfoAdapter.MINIMUM};
		final String [] unitMaskTags = new String [] {InfoAdapter.DEFAULT};
		final String [] maskTags = new String [] {InfoAdapter.VALUE};

		// compare the event data
		for (int i = 0; i < rootList.length; i++){
			Element eventListTag = (Element) rootList[i].getElementsByTagName(InfoAdapter.EVENT_LIST).item(0);
			NodeList eventTagList = eventListTag.getElementsByTagName(InfoAdapter.EVENT);
			for (int j = 0; j < eventTagList.getLength(); j++){
				//name description value minimum
				Element event = (Element) eventTagList.item(j);
				for (int k = 0; k < eventTags.length; k++){
					Element elm = (Element) (event.getElementsByTagName(eventTags[k]).item(0));
					valueList.get(i).add(elm.getTextContent());
				}

				//type default
				Element unitMaskTag = (Element) event.getElementsByTagName(InfoAdapter.UNITMASK).item(0);
				for (int k = 0; k < unitMaskTags.length; k++){
					Element elem = (Element) unitMaskTag.getElementsByTagName(unitMaskTags[k]).item(0);
					valueList.get(i).add(elem.getTextContent());
				}

				// value description
				// description is omitted (whitespace differences cause failure)
				NodeList maskTagList = unitMaskTag.getElementsByTagName(InfoAdapter.MASK);
				for (int k = 0; k < maskTagList.getLength(); k++){
					Element mask = (Element) maskTagList.item(k);
					for (int n = 0; n < maskTags.length; n++){
						Element maskVal = (Element) mask.getElementsByTagName(maskTags[n]).item(0);
						valueList.get(i).add(maskVal.getTextContent());
					}
				}
			}
		}
		assertSameValues(valueList);
		clearValues(valueList);
	}

	private void clearValues(ArrayList<ArrayList<String>> valueList) {
		for (int i = 0; i < valueList.size(); i++){
			valueList.get(i).clear();
		}
	}

	private void assertSameValues(ArrayList<ArrayList<String>> valueList) {
		for (int i = 0; i < valueList.get(0).size(); i++){
			assertEquals(valueList.get(0).get(i), valueList.get(1).get(i));
		}
	}
}
