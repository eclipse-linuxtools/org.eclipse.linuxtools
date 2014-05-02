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

import java.io.FileReader;

import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.internal.oprofile.core.opxml.OprofileSAXHandler;
import org.eclipse.linuxtools.internal.oprofile.core.opxml.checkevent.CheckEventsProcessor;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.FrameworkUtil;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

public class TestCheckEventsParse {
    private static final String REL_PATH_TO_TEST_XML_OK = "resources/test_check-event_ok.xml"; //$NON-NLS-1$
    private static final String REL_PATH_TO_TEST_XML_INVALID_UMASK = "resources/test_check-event_invalid_umask.xml"; //$NON-NLS-1$
    private static final String REL_PATH_TO_TEST_XML_INVALID_COUNTER = "resources/test_check-event_invalid_counter.xml"; //$NON-NLS-1$
    private int[] test_ok, test_invalid_umask, test_invalid_counter;

    @Before
    public void setUp() throws Exception {
        test_ok = new int[1];
        setUpHelper(REL_PATH_TO_TEST_XML_OK, test_ok);
        test_invalid_umask = new int[1];
        setUpHelper(REL_PATH_TO_TEST_XML_INVALID_UMASK, test_invalid_umask);
        test_invalid_counter = new int[1];
        setUpHelper(REL_PATH_TO_TEST_XML_INVALID_COUNTER, test_invalid_counter);
    }

    //helper
    private void setUpHelper(String fileToParse, final int[] resultArray) throws Exception {
        /* this code mostly taken from OpxmlRunner */
        XMLReader reader = null;
        OprofileSAXHandler handler = OprofileSAXHandler.getInstance(resultArray);

        // Create XMLReader
        SAXParserFactory factory = SAXParserFactory.newInstance();
        reader = factory.newSAXParser().getXMLReader();

        // Set content/error handlers
        reader.setContentHandler(handler);
        reader.setErrorHandler(handler);

        String filePath = FileLocator.toFileURL(FileLocator.find(FrameworkUtil.getBundle(this.getClass()), new Path(fileToParse), null)).getFile();
        reader.parse(new InputSource(new FileReader(filePath)));
    }

    @Test
    public void testParse() {
        assertEquals(CheckEventsProcessor.EVENT_OK, test_ok[0]);
        assertEquals(CheckEventsProcessor.INVALID_UMASK, test_invalid_umask[0]);
        assertEquals(CheckEventsProcessor.INVALID_COUNTER, test_invalid_counter[0]);
    }
}
