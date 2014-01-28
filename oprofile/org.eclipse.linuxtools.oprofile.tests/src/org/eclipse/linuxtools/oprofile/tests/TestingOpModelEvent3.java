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
 * A faked OpModelSession object when there are multiple images in the session,
 * simulating when the session was not created from within the eclipse plugin.
 */
public class TestingOpModelEvent3 extends OpModelEvent {
	private static final String REL_PATH_TO_TEST_XML = "resources/test_model-data_multiple_image.xml"; //$NON-NLS-1$

	public TestingOpModelEvent3(OpModelSession session, String name) {
		super(session, name);
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
