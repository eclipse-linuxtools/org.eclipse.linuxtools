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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.FileReader;
import java.util.ArrayList;

import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.internal.oprofile.core.model.OpModelEvent;
import org.eclipse.linuxtools.internal.oprofile.core.model.OpModelSession;
import org.eclipse.linuxtools.internal.oprofile.core.opxml.OprofileSAXHandler;
import org.eclipse.linuxtools.internal.oprofile.core.opxml.sessions.SessionsProcessor;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.FrameworkUtil;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

public class TestSessionsParse {
	private static final String REL_PATH_TO_TEST_XML = "resources/test_sessions.xml"; //$NON-NLS-1$
	private static final String EVENT1_OUTPUT = "BR_INST_EXEC\nSession: current\n"; //$NON-NLS-1$
	private static final String EVENT1_OUTPUT_WITHTAB = "BR_INST_EXEC\n\tSession: current\n"; //$NON-NLS-1$
	private static final String EVENT2_OUTPUT = "CPU_CLK_UNHALTED\nSession: saved\n"; //$NON-NLS-1$
	private static final String EVENT2_OUTPUT_WITHTAB = "CPU_CLK_UNHALTED\n\tSession: saved\n"; //$NON-NLS-1$
	private static final String EVENT3_OUTPUT = "UOPS_RETIRED\nSession: current\nSession: \"<>&'\n"; //$NON-NLS-1$
	private static final String EVENT3_OUTPUT_WITHTAB = "UOPS_RETIRED\n\tSession: current\n\tSession: \"<>&'\n"; //$NON-NLS-1$

	private ArrayList<OpModelEvent> eventList;

	@Before
	public void setUp() throws Exception {
		/* this code mostly taken from OpxmlRunner */
		XMLReader reader = null;
		eventList = new ArrayList<OpModelEvent>();
		SessionsProcessor.SessionInfo sessioninfo = new SessionsProcessor.SessionInfo(eventList);
		OprofileSAXHandler handler = OprofileSAXHandler.getInstance(sessioninfo);

		// Create XMLReader
        SAXParserFactory factory = SAXParserFactory.newInstance();
		reader = factory.newSAXParser().getXMLReader();

		// Set content/error handlers
		reader.setContentHandler(handler);
		reader.setErrorHandler(handler);

		String filePath = FileLocator.toFileURL(FileLocator.find(FrameworkUtil.getBundle(this.getClass()), new Path(REL_PATH_TO_TEST_XML), null)).getFile();
		reader.parse(new InputSource(new FileReader(filePath)));
	}

	@Test
	public void testParse() {
		assertEquals(3, eventList.size());
		OpModelEvent evt1 = eventList.get(0), evt2 = eventList.get(1), evt3 = eventList.get(2);

		assertEquals("BR_INST_EXEC", evt1.getName()); //$NON-NLS-1$
		assertEquals("CPU_CLK_UNHALTED", evt2.getName()); //$NON-NLS-1$
		assertEquals("UOPS_RETIRED", evt3.getName()); //$NON-NLS-1$

		OpModelSession[] evt1_ss = evt1.getSessions(), evt2_ss = evt2.getSessions(), evt3_ss = evt3.getSessions();
		assertEquals(1, evt1_ss.length);
		assertEquals(1, evt2_ss.length);
		assertEquals(2, evt3_ss.length);
		OpModelSession evt1_ss_s1 = evt1_ss[0];
		OpModelSession evt2_ss_s1 = evt2_ss[0];
		OpModelSession evt3_ss_s1 = evt3_ss[0];
		OpModelSession evt3_ss_s2 = evt3_ss[1];

		assertEquals("current", evt1_ss_s1.getName()); //$NON-NLS-1$
		assertEquals(true, evt1_ss_s1.isDefaultSession());
		assertNull(evt1_ss_s1.getImage());
		assertEquals(0, evt1_ss_s1.getCount());
		assertEquals(evt1, evt1_ss_s1.getEvent());

		assertEquals("saved", evt2_ss_s1.getName()); //$NON-NLS-1$
		assertEquals(false, evt2_ss_s1.isDefaultSession());
		assertNull(evt2_ss_s1.getImage());
		assertEquals(0, evt2_ss_s1.getCount());
		assertEquals(evt2, evt2_ss_s1.getEvent());

		assertEquals("current", evt3_ss_s1.getName()); //$NON-NLS-1$
		assertEquals(true, evt3_ss_s1.isDefaultSession());
		assertNull(evt3_ss_s1.getImage());
		assertEquals(0, evt3_ss_s1.getCount());
		assertEquals(evt3, evt3_ss_s1.getEvent());

		assertEquals("\"<>&'", evt3_ss_s2.getName()); //$NON-NLS-1$
		assertEquals(false, evt3_ss_s2.isDefaultSession());
		assertNull(evt3_ss_s2.getImage());
		assertEquals(0, evt3_ss_s2.getCount());
		assertEquals(evt3, evt3_ss_s2.getEvent());
	}

	@Test
	public void testStringOutput() {
		assertEquals(EVENT1_OUTPUT, eventList.get(0).toString());
		assertEquals(EVENT1_OUTPUT_WITHTAB, eventList.get(0).toString("\t")); //$NON-NLS-1$
		assertEquals(EVENT2_OUTPUT, eventList.get(1).toString());
		assertEquals(EVENT2_OUTPUT_WITHTAB, eventList.get(1).toString("\t")); //$NON-NLS-1$
		assertEquals(EVENT3_OUTPUT, eventList.get(2).toString());
		assertEquals(EVENT3_OUTPUT_WITHTAB, eventList.get(2).toString("\t")); //$NON-NLS-1$
	}
}
