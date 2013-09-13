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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.internal.oprofile.core.opxml.modeldata.ModelDataAdapter;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.osgi.framework.FrameworkUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Test cases for checking validity of the model data parsed from oprofile that
 * is modified to mimic the XML format expected by the SAX parser.
 * The oprofile module must be loaded and the driver interface must be
 * available. ie. run opcontrol --init
 */
public class TestModelDataPreParse {

	private static final String REL_PATH_TO_MODEL_DATA_RAW = "resources/test_model-data_raw.xml";
	private static final String REL_PATH_TO_MODEL_DATA_EXPECTED = "resources/test_model-data_expected.xml";

	ModelDataAdapter mda;
	Element [] rootList;
	ArrayList<ArrayList<String>> valueList;

	@Before
	public void setUp (){
		String absFilePath;
		Path filePath = new Path(REL_PATH_TO_MODEL_DATA_RAW);
		URL fileURL = FileLocator.find(FrameworkUtil.getBundle(this.getClass()), filePath, null);

		try {
			absFilePath = FileLocator.toFileURL(fileURL).getFile();
			File file = new File (absFilePath);
			FileInputStream inp = new FileInputStream(file);
			mda = new ModelDataAdapter(inp);
			mda.process();
			System.out.println(mda.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}

		mda.process();
		Document actualDocument = mda.getDocument();
		Element actualRoot = (Element) actualDocument.getElementsByTagName(ModelDataAdapter.MODEL_DATA).item(0);

		filePath = new Path(REL_PATH_TO_MODEL_DATA_EXPECTED);
		fileURL = FileLocator.find(FrameworkUtil.getBundle(this.getClass()), filePath, null);
		Element expectedRoot = null;

		try {
			absFilePath = FileLocator.toFileURL(fileURL).getFile();
			File file = new File (absFilePath);
			FileInputStream inp = new FileInputStream(file);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder;
			builder = factory.newDocumentBuilder();
			Document expectedDocument = builder.parse(inp);
			expectedRoot = (Element) expectedDocument.getElementsByTagName(ModelDataAdapter.MODEL_DATA).item(0);
		} catch (FileNotFoundException e) {
			fail("File was not found.");
		} catch (IOException e) {
			fail("Failed to convert the resource file's path.");
		} catch (SAXException e) {
			fail("Failed to parse the XML.");
		} catch (ParserConfigurationException e) {
			fail("Failed to create a document builder.");
		}

		rootList = new Element [] {expectedRoot, actualRoot};
		valueList = new ArrayList<ArrayList<String>> ();

		for (int i = 0; i < rootList.length; i++){
			valueList.add(new ArrayList<String>());
		}
	}

	@Test
	public void testBasic (){
		final String [] imageAttrs = new String [] {ModelDataAdapter.NAME, ModelDataAdapter.COUNT};
		final String [] symbolAttrs = new String [] {ModelDataAdapter.NAME, ModelDataAdapter.FILE, ModelDataAdapter.COUNT};
		final String [] sampleTags = new String [] {ModelDataAdapter.COUNT, ModelDataAdapter.LINE};

		for (int i = 0; i < rootList.length; i++){
			Element imageTag = (Element) rootList[i].getElementsByTagName(ModelDataAdapter.IMAGE).item(0);
			// image name, count
			for (int j = 0; j < imageAttrs.length; j++){
				String attr = imageTag.getAttribute(imageAttrs[j]);
				valueList.get(i).add(attr);
			}

			Element symbolsTag = (Element) rootList[i].getElementsByTagName(ModelDataAdapter.SYMBOLS).item(0);
			NodeList symbolList = symbolsTag.getElementsByTagName(ModelDataAdapter.SYMBOL);
			// go through each symbol
			for (int j = 0; j < symbolList.getLength(); j++){
				Element symbolTag = (Element) symbolList.item(j);
				// symbol name, file, count
				for (int k = 0; k < symbolAttrs.length; k++){
					String attr = symbolTag.getAttribute(symbolAttrs[k]);
					valueList.get(i).add(attr);
				}

				NodeList sampleList = symbolsTag.getElementsByTagName(ModelDataAdapter.SAMPLE);
				// go through each sample
				for (int k = 0; k < sampleList.getLength(); k++){
					Element sampleTag = (Element) sampleList.item(k);

					for (int n = 0; n < sampleTags.length; n++){
						Element elem = (Element) sampleTag.getElementsByTagName(sampleTags[n]).item(0);
						valueList.get(i).add(elem.getTextContent());
					}
				}
			}
		}
		assertSameValues(valueList);
	}

	private void assertSameValues(ArrayList<ArrayList<String>> valueList) {
		for (int i = 0; i < valueList.get(0).size(); i++){
			assertEquals(valueList.get(0).get(i), valueList.get(1).get(i));
		}
	}
}
