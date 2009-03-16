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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import junit.framework.TestCase;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.oprofile.core.model.OpModelEvent;
import org.eclipse.linuxtools.oprofile.core.model.OpModelImage;
import org.eclipse.linuxtools.oprofile.core.model.OpModelRoot;
import org.eclipse.linuxtools.oprofile.core.model.OpModelSession;
import org.eclipse.linuxtools.oprofile.core.opxml.OprofileSAXHandler;
import org.eclipse.linuxtools.oprofile.core.opxml.modeldata.ModelDataProcessor;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class TestDataModel extends TestCase {
	private static final String REL_PATH_TO_TEST_XML = "resources/test_model-data.xml"; //$NON-NLS-1$
	private static final String ROOT_OUTPUT = "Event: testEvent1\n\tSession: testSession1e1\n\t\tImage: /test/path/for/image, Count: 205000, Dependent Count: 5000\n\t\t\tSymbols: TestFunction1(int), File: /test/path/for/src/image.cpp, Count: 180000\n\t\t\t\tSample: Line #: 42, Count: 130000\n\t\t\t\tSample: Line #: 36, Count: 40000\n\t\t\t\tSample: Line #: 31, Count: 9999\n\t\t\t\tSample: Line #: 39, Count: 1\n\t\t\tSymbols: TestFunction2(int, int), File: /test/path/for/src/image2.cpp, Count: 20000\n\t\t\t\tSample: Line #: 94, Count: 19998\n\t\t\t\tSample: Line #: 12, Count: 1\n\t\t\t\tSample: Line #: 55, Count: 1\n\t\t\tDependent Image: /no-vmlinux, Count: 4400\n\t\t\tDependent Image: /lib64/ld-2.9.so, Count: 300\n\t\t\t\tSymbols: do_lookup_x, File: dl-lookup.c, Count: 299\n\t\t\t\t\tSample: Line #: 0, Count: 299\n\t\t\t\tSymbols: _dl_unload_cache, File: rawmemchr.c, Count: 1\n\t\t\t\t\tSample: Line #: 0, Count: 1\n\t\t\tDependent Image: /usr/lib64/libstdc++.so.6.0.10, Count: 160\n\t\t\tDependent Image: /lib64/libc-2.9.so, Count: 140\n\t\t\t\tSymbols: _IO_new_file_seekoff, File: , Count: 100\n\t\t\t\t\tSample: Line #: 0, Count: 100\n\t\t\t\tSymbols: bcopy, File: , Count: 40\n\t\t\t\t\tSample: Line #: 0, Count: 40\nEvent: testEvent2\n\tSession: testSession1e2\n\t\tImage: /test/path/for/image, Count: 205000, Dependent Count: 5000\n\t\t\tSymbols: TestFunction1(int), File: /test/path/for/src/image.cpp, Count: 180000\n\t\t\t\tSample: Line #: 42, Count: 130000\n\t\t\t\tSample: Line #: 36, Count: 40000\n\t\t\t\tSample: Line #: 31, Count: 9999\n\t\t\t\tSample: Line #: 39, Count: 1\n\t\t\tSymbols: TestFunction2(int, int), File: /test/path/for/src/image2.cpp, Count: 20000\n\t\t\t\tSample: Line #: 94, Count: 19998\n\t\t\t\tSample: Line #: 12, Count: 1\n\t\t\t\tSample: Line #: 55, Count: 1\n\t\t\tDependent Image: /no-vmlinux, Count: 4400\n\t\t\tDependent Image: /lib64/ld-2.9.so, Count: 300\n\t\t\t\tSymbols: do_lookup_x, File: dl-lookup.c, Count: 299\n\t\t\t\t\tSample: Line #: 0, Count: 299\n\t\t\t\tSymbols: _dl_unload_cache, File: rawmemchr.c, Count: 1\n\t\t\t\t\tSample: Line #: 0, Count: 1\n\t\t\tDependent Image: /usr/lib64/libstdc++.so.6.0.10, Count: 160\n\t\t\tDependent Image: /lib64/libc-2.9.so, Count: 140\n\t\t\t\tSymbols: _IO_new_file_seekoff, File: , Count: 100\n\t\t\t\t\tSample: Line #: 0, Count: 100\n\t\t\t\tSymbols: bcopy, File: , Count: 40\n\t\t\t\t\tSample: Line #: 0, Count: 40\n\tSession: testSession2e2\n\t\tImage: /test/path/for/image, Count: 205000, Dependent Count: 5000\n\t\t\tSymbols: TestFunction1(int), File: /test/path/for/src/image.cpp, Count: 180000\n\t\t\t\tSample: Line #: 42, Count: 130000\n\t\t\t\tSample: Line #: 36, Count: 40000\n\t\t\t\tSample: Line #: 31, Count: 9999\n\t\t\t\tSample: Line #: 39, Count: 1\n\t\t\tSymbols: TestFunction2(int, int), File: /test/path/for/src/image2.cpp, Count: 20000\n\t\t\t\tSample: Line #: 94, Count: 19998\n\t\t\t\tSample: Line #: 12, Count: 1\n\t\t\t\tSample: Line #: 55, Count: 1\n\t\t\tDependent Image: /no-vmlinux, Count: 4400\n\t\t\tDependent Image: /lib64/ld-2.9.so, Count: 300\n\t\t\t\tSymbols: do_lookup_x, File: dl-lookup.c, Count: 299\n\t\t\t\t\tSample: Line #: 0, Count: 299\n\t\t\t\tSymbols: _dl_unload_cache, File: rawmemchr.c, Count: 1\n\t\t\t\t\tSample: Line #: 0, Count: 1\n\t\t\tDependent Image: /usr/lib64/libstdc++.so.6.0.10, Count: 160\n\t\t\tDependent Image: /lib64/libc-2.9.so, Count: 140\n\t\t\t\tSymbols: _IO_new_file_seekoff, File: , Count: 100\n\t\t\t\t\tSample: Line #: 0, Count: 100\n\t\t\t\tSymbols: bcopy, File: , Count: 40\n\t\t\t\t\tSample: Line #: 0, Count: 40\n\tSession: testSession3e2\n"; //$NON-NLS-1$
	
	/* extend OpModelRoot and OpModelSession to override the code which would launch opxml */
	private class TestingOpModelRoot extends OpModelRoot {
		public static final String NAME_E1 = "testEvent1"; //$NON-NLS-1$
		public static final String NAME_E2 = "testEvent2"; //$NON-NLS-1$
		public static final String NAME_S1E1 = "testSession1e1"; //$NON-NLS-1$
		public static final String NAME_S2E1 = "testSession1e2"; //$NON-NLS-1$
		public static final String NAME_S2E2 = "testSession2e2"; //$NON-NLS-1$
		public static final String NAME_S2E3 = "testSession3e2"; //$NON-NLS-1$
		@Override
		protected OpModelEvent[] getNewEvents() {
			//fake running opxml and simply return hand-made events
			OpModelEvent[] e = {new OpModelEvent(NAME_E1), new OpModelEvent(NAME_E2)};
			e[0]._setSessions(new TestingOpModelSession[] {new TestingOpModelSession(e[0], NAME_S1E1)});
			e[1]._setSessions(new OpModelSession[] {
					new TestingOpModelSession(e[1], NAME_S2E1),
					new TestingOpModelSession(e[1], NAME_S2E2),
					new TestingOpModelSession2(e[1], NAME_S2E3)});
			return e;
		}
	}
	private class TestingOpModelSession extends OpModelSession {
		public TestingOpModelSession(OpModelEvent event, String name) {
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
				
				String filePath = FileLocator.toFileURL(FileLocator.find(CoreTestsPlugin.getDefault().getBundle(), new Path(REL_PATH_TO_TEST_XML), null)).getFile();
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
	private class TestingOpModelSession2 extends OpModelSession {
		public TestingOpModelSession2(OpModelEvent event, String name) {
			super(event, name);
		}
		@Override
		protected OpModelImage getNewImage() {
			return null;
		}
	}
	
	private TestingOpModelRoot _testRoot;
	
	public TestDataModel() {
		super("test data model"); //$NON-NLS-1$
	}
	
	@Override
	protected void setUp() throws Exception {
		_testRoot = new TestingOpModelRoot();
		_testRoot.refreshModel();
	}
	
	public void testParse() {
		OpModelEvent[] events = _testRoot.getEvents();
		assertEquals(2, events.length);
		assertEquals(TestingOpModelRoot.NAME_E1, events[0].getName());
		assertEquals(TestingOpModelRoot.NAME_E2, events[1].getName());
		
		OpModelSession[] e1_sessions = events[0].getSessions(), e2_sessions = events[1].getSessions();
		assertEquals(1, e1_sessions.length);
		assertEquals(3, e2_sessions.length);
		
		assertEquals(205000, e1_sessions[0].getCount());
		assertEquals(205000, e2_sessions[0].getCount());
		assertEquals(205000, e2_sessions[1].getCount());
		assertEquals(0, e2_sessions[2].getCount());
		
		assertEquals(TestingOpModelRoot.NAME_S1E1, e1_sessions[0].getName());
		assertEquals(TestingOpModelRoot.NAME_S2E1, e2_sessions[0].getName());
		assertEquals(TestingOpModelRoot.NAME_S2E2, e2_sessions[1].getName());
		assertEquals(TestingOpModelRoot.NAME_S2E3, e2_sessions[2].getName());
	}
	
	public void testStringOutput() {
		assertEquals(ROOT_OUTPUT, _testRoot.toString());
	}
}
