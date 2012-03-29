/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.oprofile.tests;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.internal.oprofile.core.model.OpModelEvent;
import org.eclipse.linuxtools.internal.oprofile.core.model.OpModelImage;
import org.eclipse.linuxtools.internal.oprofile.core.model.OpModelSession;
import org.eclipse.linuxtools.internal.oprofile.core.opxml.OprofileSAXHandler;
import org.eclipse.linuxtools.internal.oprofile.core.opxml.modeldata.ModelDataProcessor;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/*
 * A faked OpModelSession object of a typical image with no dependent images.
 */
public class TestingOpModelSession2 extends OpModelSession {
	private static final String REL_PATH_TO_TEST_XML = "resources/test_model-data_nodepimage.xml"; //$NON-NLS-1$
	public static final String IMAGE_OUTPUT = "/test/path/for/image, Count: 205000, Dependent Count: 5000\nSymbols: TestFunction1(int), File: /test/path/for/src/image.cpp, Count: 180000\n\tSample: Line #: 42, Count: 130000\n\tSample: Line #: 36, Count: 40000\n\tSample: Line #: 31, Count: 9999\n\tSample: Line #: 39, Count: 1\nSymbols: TestFunction2(int, int), File: /test/path/for/src/image2.cpp, Count: 20000\n\tSample: Line #: 94, Count: 19998\n\tSample: Line #: 12, Count: 1\n\tSample: Line #: 55, Count: 1\n"; //$NON-NLS-1$
	public static final String IMAGE_OUTPUT_WITHTAB = "/test/path/for/image, Count: 205000, Dependent Count: 5000\n\tSymbols: TestFunction1(int), File: /test/path/for/src/image.cpp, Count: 180000\n\t\tSample: Line #: 42, Count: 130000\n\t\tSample: Line #: 36, Count: 40000\n\t\tSample: Line #: 31, Count: 9999\n\t\tSample: Line #: 39, Count: 1\n\tSymbols: TestFunction2(int, int), File: /test/path/for/src/image2.cpp, Count: 20000\n\t\tSample: Line #: 94, Count: 19998\n\t\tSample: Line #: 12, Count: 1\n\t\tSample: Line #: 55, Count: 1\n"; //$NON-NLS-1$

	public TestingOpModelSession2(OpModelEvent event, String name) {
		super(event, name);
	}
	@Override
	protected OpModelImage getNewImage() {
		/* this code mostly taken from OpxmlRunner */
		OpModelImage parsedImage = null;
		try {
			XMLReader reader = null;
			parsedImage = new OpModelImage();
			ModelDataProcessor.CallData image = new ModelDataProcessor.CallData(parsedImage);
			OprofileSAXHandler handler = OprofileSAXHandler.getInstance(image);
			
			// Create XMLReader
			SAXParserFactory factory = SAXParserFactory.newInstance();
			reader = factory.newSAXParser().getXMLReader();
			
			// Set content/error handlers
			reader.setContentHandler(handler);
			reader.setErrorHandler(handler);
			
			String filePath = FileLocator.toFileURL(FileLocator.find(TestPlugin.getDefault().getBundle(), new Path(REL_PATH_TO_TEST_XML), null)).getFile();
			reader.parse(new InputSource(new FileReader(filePath)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		
		return parsedImage;
	}
}
