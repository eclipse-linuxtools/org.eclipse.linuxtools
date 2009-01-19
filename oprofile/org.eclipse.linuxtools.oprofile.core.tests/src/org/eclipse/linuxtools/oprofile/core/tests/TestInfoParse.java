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
package org.eclipse.linuxtools.oprofile.core.tests;

import java.io.FileReader;

import javax.xml.parsers.SAXParserFactory;

import junit.framework.TestCase;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.oprofile.core.daemon.OpEvent;
import org.eclipse.linuxtools.oprofile.core.daemon.OpInfo;
import org.eclipse.linuxtools.oprofile.core.opxml.OprofileSAXHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

public class TestInfoParse extends TestCase {
	private static final String REL_PATH_TO_TEST_XML = "resources/test_info.xml"; //$NON-NLS-1$
	
	private OpInfo info; 

	
	public TestInfoParse() {
		super("test info parsers"); //$NON-NLS-1$
	}
	
	@Override
	protected void setUp() throws Exception {
		/* this code mostly taken from OpxmlRunner */
		XMLReader reader = null;
		info = new OpInfo();
		OprofileSAXHandler handler = OprofileSAXHandler.getInstance(info);
		
		// Create XMLReader
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
			reader = factory.newSAXParser().getXMLReader();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Set content/error handlers
		reader.setContentHandler(handler);
		reader.setErrorHandler(handler);
		
		String filePath = FileLocator.toFileURL(FileLocator.find(CoreTestsPlugin.getDefault().getBundle(), new Path(REL_PATH_TO_TEST_XML), null)).getFile();
		reader.parse(new InputSource(new FileReader(filePath)));
	}
	
	public void testParse() {
		assertEquals("/var/lib/oprofile/samples/", info.getDefault(OpInfo.DEFAULT_SAMPLE_DIR)); //$NON-NLS-1$
		assertEquals("/var/lib/oprofile/lock", info.getDefault(OpInfo.DEFAULT_LOCK_FILE)); //$NON-NLS-1$
		assertEquals("/var/lib/oprofile/samples/oprofiled.log", info.getDefault(OpInfo.DEFAULT_LOG_FILE)); //$NON-NLS-1$
		assertEquals("/var/lib/oprofile/complete_dump", info.getDefault(OpInfo.DEFAULT_DUMP_STATUS)); //$NON-NLS-1$
		
		assertEquals((double)800, info.getCPUSpeed());
		assertEquals(2, info.getNrCounters());

		OpEvent[] ctr0_events = info.getEvents(0), ctr1_events = info.getEvents(1);
		assertEquals(3, ctr0_events.length);
		assertEquals(3, ctr1_events.length);
		
	}
}
