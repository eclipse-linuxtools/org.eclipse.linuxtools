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
import static org.junit.Assert.assertTrue;

import java.io.FileReader;

import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.internal.oprofile.core.daemon.OpEvent;
import org.eclipse.linuxtools.internal.oprofile.core.daemon.OpInfo;
import org.eclipse.linuxtools.internal.oprofile.core.daemon.OpUnitMask;
import org.eclipse.linuxtools.internal.oprofile.core.opxml.OprofileSAXHandler;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.FrameworkUtil;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

public class TestInfoParse {
    private static final String REL_PATH_TO_TEST_XML = "resources/test_info.xml"; //$NON-NLS-1$
    private static final String REL_PATH_TO_TEST_XML_0CTR = "resources/test_info_0ctrs.xml"; //$NON-NLS-1$

    private OpInfo info;
    private OpInfo info_0ctr;

    @Before
    public void setUp() throws Exception {
        /* this code mostly taken from OpxmlRunner */
        XMLReader reader = null;
        info = new OpInfo();
        OprofileSAXHandler handler = OprofileSAXHandler.getInstance(info);

        // Create XMLReader
        SAXParserFactory factory = SAXParserFactory.newInstance();
        reader = factory.newSAXParser().getXMLReader();

        // Set content/error handlers
        reader.setContentHandler(handler);
        reader.setErrorHandler(handler);

        String filePath = FileLocator.toFileURL(FileLocator.find(FrameworkUtil.getBundle(this.getClass()), new Path(REL_PATH_TO_TEST_XML), null)).getFile();
        reader.parse(new InputSource(new FileReader(filePath)));

        info_0ctr = new OpInfo();
        handler = OprofileSAXHandler.getInstance(info_0ctr);

        // Set content/error handlers
        reader.setContentHandler(handler);
        reader.setErrorHandler(handler);

        filePath = FileLocator.toFileURL(FileLocator.find(FrameworkUtil.getBundle(this.getClass()), new Path(REL_PATH_TO_TEST_XML_0CTR), null)).getFile();
        reader.parse(new InputSource(new FileReader(filePath)));
    }

    @Test
    public void testParse() {
        assertEquals("/var/lib/oprofile/samples/", info.getDefault(OpInfo.DEFAULT_SAMPLE_DIR)); //$NON-NLS-1$
        assertEquals("/var/lib/oprofile/lock", info.getDefault(OpInfo.DEFAULT_LOCK_FILE)); //$NON-NLS-1$
        assertEquals("/var/lib/oprofile/samples/oprofiled.log", info.getDefault(OpInfo.DEFAULT_LOG_FILE)); //$NON-NLS-1$
        assertEquals("/var/lib/oprofile/complete_dump", info.getDefault(OpInfo.DEFAULT_DUMP_STATUS)); //$NON-NLS-1$
        assertTrue(info.getTimerMode());

        assertEquals(800, info.getCPUSpeed(),0);
        assertEquals(2, info.getNrCounters());

        OpEvent[] ctr0_events = info.getEvents(0), ctr1_events = info.getEvents(1);
        assertEquals(3, ctr0_events.length);
        assertEquals(3, ctr1_events.length);

        OpEvent ctr0_e1 = ctr0_events[0], ctr0_e2 = ctr0_events[1], ctr0_e3 = ctr0_events[2],
                ctr1_e1 = ctr1_events[0], ctr1_e2 = ctr1_events[1], ctr1_e3 = ctr1_events[2];
        //events must be ordered alphabetically
        assertEquals(6000, ctr0_e1.getMinCount());
        assertEquals("CPU_CLK_UNHALTED", ctr0_e1.getText()); //$NON-NLS-1$
        assertEquals("Clock cycles when not halted", ctr0_e1.getTextDescription()); //$NON-NLS-1$
        assertEquals(500, ctr0_e2.getMinCount());
        assertEquals("DTLB_MISSES", ctr0_e2.getText()); //$NON-NLS-1$
        assertEquals("DTLB miss events", ctr0_e2.getTextDescription()); //$NON-NLS-1$
        assertEquals(6000, ctr0_e3.getMinCount());
        assertEquals("INST_RETIRED_ANY_P", ctr0_e3.getText()); //$NON-NLS-1$
        assertEquals("number of instructions retired", ctr0_e3.getTextDescription()); //$NON-NLS-1$
        assertEquals(500, ctr1_e1.getMinCount());
        assertEquals("EIST_TRANS_ALL", ctr1_e1.getText()); //$NON-NLS-1$
        assertEquals("Intel(tm) Enhanced SpeedStep(r) Technology transitions", ctr1_e1.getTextDescription()); //$NON-NLS-1$
        assertEquals(500, ctr1_e2.getMinCount());
        assertEquals("L2_LINES_OUT", ctr1_e2.getText()); //$NON-NLS-1$
        assertEquals("number of recovered lines from L2", ctr1_e2.getTextDescription()); //$NON-NLS-1$
        assertEquals(500, ctr1_e3.getMinCount());
        assertEquals("L2_M_LINES_IN", ctr1_e3.getText()); //$NON-NLS-1$
        assertEquals("number of modified lines allocated in L2", ctr1_e3.getTextDescription()); //$NON-NLS-1$

        OpUnitMask ctr0_e1_mask = ctr0_e1.getUnitMask(), ctr0_e2_mask = ctr0_e2.getUnitMask(),
                    ctr0_e3_mask = ctr0_e3.getUnitMask(), ctr1_e1_mask = ctr1_e1.getUnitMask(),
                    ctr1_e2_mask = ctr1_e2.getUnitMask(), ctr1_e3_mask = ctr1_e3.getUnitMask();

        assertEquals(0, ctr0_e1_mask.getMaskValue());
        assertEquals(OpUnitMask.EXCLUSIVE, ctr0_e1_mask.getType());
        assertEquals(3, ctr0_e1_mask.getNumMasks());
        assertEquals(0, ctr0_e1_mask.getMaskFromIndex(0));
        assertEquals("Unhalted core cycles", ctr0_e1_mask.getText(0)); //$NON-NLS-1$
        assertEquals(1, ctr0_e1_mask.getMaskFromIndex(1));
        assertEquals("Unhalted bus cycles", ctr0_e1_mask.getText(1)); //$NON-NLS-1$
        assertEquals(2, ctr0_e1_mask.getMaskFromIndex(2));
        assertEquals("Unhalted bus cycles of this core while the other core is halted", ctr0_e1_mask.getText(2)); //$NON-NLS-1$

        assertEquals(15, ctr0_e2_mask.getMaskValue());
        assertEquals(OpUnitMask.BITMASK, ctr0_e2_mask.getType());
        assertEquals(4, ctr0_e2_mask.getNumMasks());
        assertEquals(1, ctr0_e2_mask.getMaskFromIndex(0));
        assertEquals("ANY\tMemory accesses that missed the DTLB.", ctr0_e2_mask.getText(0)); //$NON-NLS-1$
        assertEquals(2, ctr0_e2_mask.getMaskFromIndex(1));
        assertEquals("MISS_LD\tDTLB misses due to load operations.", ctr0_e2_mask.getText(1)); //$NON-NLS-1$
        assertEquals(4, ctr0_e2_mask.getMaskFromIndex(2));
        assertEquals("L0_MISS_LD L0 DTLB misses due to load operations.", ctr0_e2_mask.getText(2)); //$NON-NLS-1$
        assertEquals(8, ctr0_e2_mask.getMaskFromIndex(3));
        assertEquals("MISS_ST\tTLB misses due to store operations.", ctr0_e2_mask.getText(3)); //$NON-NLS-1$

        assertEquals(0, ctr0_e3_mask.getMaskValue());
        assertEquals(OpUnitMask.MANDATORY, ctr0_e3_mask.getType());
        assertEquals(1, ctr0_e3_mask.getNumMasks());
        assertEquals(0, ctr0_e3_mask.getMaskFromIndex(0));
        assertEquals("No unit mask", ctr0_e3_mask.getText(0)); //$NON-NLS-1$

        assertEquals(1, ctr1_e1_mask.getMaskValue());
        assertEquals(OpUnitMask.INVALID, ctr1_e1_mask.getType());
        assertEquals(1, ctr1_e1_mask.getNumMasks());
        assertEquals(-1, ctr1_e1_mask.getMaskFromIndex(0));        //-1 because of invalid mask type
        assertEquals("No unit mask", ctr1_e1_mask.getText(0)); //$NON-NLS-1$

        assertEquals(112, ctr1_e2_mask.getMaskValue());
        assertEquals(OpUnitMask.BITMASK, ctr1_e2_mask.getType());
        assertEquals(5, ctr1_e2_mask.getNumMasks());
        assertEquals(192, ctr1_e2_mask.getMaskFromIndex(0));
        assertEquals("core: all cores", ctr1_e2_mask.getText(0)); //$NON-NLS-1$
        assertEquals(64, ctr1_e2_mask.getMaskFromIndex(1));
        assertEquals("core: this core", ctr1_e2_mask.getText(1)); //$NON-NLS-1$
        assertEquals(48, ctr1_e2_mask.getMaskFromIndex(2));
        assertEquals("prefetch: all inclusive", ctr1_e2_mask.getText(2)); //$NON-NLS-1$
        assertEquals(16, ctr1_e2_mask.getMaskFromIndex(3));
        assertEquals("prefetch: Hardware prefetch only", ctr1_e2_mask.getText(3)); //$NON-NLS-1$
        assertEquals(0, ctr1_e2_mask.getMaskFromIndex(4));
        assertEquals("prefetch: exclude hardware prefetch", ctr1_e2_mask.getText(4)); //$NON-NLS-1$

        assertEquals(64, ctr1_e3_mask.getMaskValue());
        assertEquals(OpUnitMask.EXCLUSIVE, ctr1_e3_mask.getType());
        assertEquals(2, ctr1_e3_mask.getNumMasks());
        assertEquals(192, ctr1_e3_mask.getMaskFromIndex(0));
        assertEquals("All cores", ctr1_e3_mask.getText(0)); //$NON-NLS-1$
        assertEquals(64, ctr1_e3_mask.getMaskFromIndex(1));
        assertEquals("This core", ctr1_e3_mask.getText(1)); //$NON-NLS-1$

        assertNull(ctr0_e1_mask.getText(-1));

        assertEquals(0, info_0ctr.getNrCounters());
    }

    @Test
    public void testUnitMask() {
        //test types of masks setting/unsetting
        OpUnitMask mask_bit1 = info.getEvents(0)[1].getUnitMask(),
//                    mask_bit2 = info.getEvents(1)[1].getUnitMask(),
                    mask_exl = info.getEvents(0)[0].getUnitMask(),
                    mask_mand = info.getEvents(0)[2].getUnitMask(),
                    mask_invalid = info.getEvents(1)[0].getUnitMask();

        //bitmask 1 test -- bitmasks all mutually exclusive
        assertEquals(15, mask_bit1.getMaskValue());
        mask_bit1.setMaskValue(0);
        mask_bit1.setMaskFromIndex(0);
        assertEquals(true, mask_bit1.isMaskSetFromIndex(0));
        assertEquals(false, mask_bit1.isMaskSetFromIndex(1));
        assertEquals(false, mask_bit1.isMaskSetFromIndex(2));
        assertEquals(false, mask_bit1.isMaskSetFromIndex(3));
        assertEquals(1, mask_bit1.getMaskValue());

        mask_bit1.setMaskFromIndex(1);
        assertEquals(true, mask_bit1.isMaskSetFromIndex(0));
        assertEquals(true, mask_bit1.isMaskSetFromIndex(1));
        assertEquals(false, mask_bit1.isMaskSetFromIndex(2));
        assertEquals(false, mask_bit1.isMaskSetFromIndex(3));
        assertEquals(3, mask_bit1.getMaskValue());

        mask_bit1.setMaskFromIndex(2);
        assertEquals(true, mask_bit1.isMaskSetFromIndex(0));
        assertEquals(true, mask_bit1.isMaskSetFromIndex(1));
        assertEquals(true, mask_bit1.isMaskSetFromIndex(2));
        assertEquals(false, mask_bit1.isMaskSetFromIndex(3));
        assertEquals(7, mask_bit1.getMaskValue());

        mask_bit1.setMaskFromIndex(3);
        assertEquals(true, mask_bit1.isMaskSetFromIndex(0));
        assertEquals(true, mask_bit1.isMaskSetFromIndex(1));
        assertEquals(true, mask_bit1.isMaskSetFromIndex(2));
        assertEquals(true, mask_bit1.isMaskSetFromIndex(3));
        assertEquals(15, mask_bit1.getMaskValue());

        mask_bit1.unSetMaskFromIndex(1);
        assertEquals(true, mask_bit1.isMaskSetFromIndex(0));
        assertEquals(false, mask_bit1.isMaskSetFromIndex(1));
        assertEquals(true, mask_bit1.isMaskSetFromIndex(2));
        assertEquals(true, mask_bit1.isMaskSetFromIndex(3));

        mask_bit1.unSetMaskFromIndex(2);
        assertEquals(true, mask_bit1.isMaskSetFromIndex(0));
        assertEquals(false, mask_bit1.isMaskSetFromIndex(1));
        assertEquals(false, mask_bit1.isMaskSetFromIndex(2));
        assertEquals(true, mask_bit1.isMaskSetFromIndex(3));

        mask_bit1.unSetMaskFromIndex(3);
        assertEquals(true, mask_bit1.isMaskSetFromIndex(0));
        assertEquals(false, mask_bit1.isMaskSetFromIndex(1));
        assertEquals(false, mask_bit1.isMaskSetFromIndex(2));
        assertEquals(false, mask_bit1.isMaskSetFromIndex(3));

        mask_bit1.setMaskFromIndex(2);
        assertEquals(true, mask_bit1.isMaskSetFromIndex(0));
        assertEquals(false, mask_bit1.isMaskSetFromIndex(1));
        assertEquals(true, mask_bit1.isMaskSetFromIndex(2));
        assertEquals(false, mask_bit1.isMaskSetFromIndex(3));
        assertEquals(5, mask_bit1.getMaskValue());

        mask_bit1.unSetMaskFromIndex(1);
        assertEquals(true, mask_bit1.isMaskSetFromIndex(0));
        assertEquals(false, mask_bit1.isMaskSetFromIndex(1));
        assertEquals(true, mask_bit1.isMaskSetFromIndex(2));
        assertEquals(false, mask_bit1.isMaskSetFromIndex(3));

        mask_bit1.unSetMaskFromIndex(3);
        assertEquals(true, mask_bit1.isMaskSetFromIndex(0));
        assertEquals(false, mask_bit1.isMaskSetFromIndex(1));
        assertEquals(true, mask_bit1.isMaskSetFromIndex(2));
        assertEquals(false, mask_bit1.isMaskSetFromIndex(3));
        assertEquals(5, mask_bit1.getMaskValue());

        mask_bit1.setMaskFromIndex(2);
        assertEquals(true, mask_bit1.isMaskSetFromIndex(0));
        assertEquals(false, mask_bit1.isMaskSetFromIndex(1));
        assertEquals(true, mask_bit1.isMaskSetFromIndex(2));
        assertEquals(false, mask_bit1.isMaskSetFromIndex(3));
        assertEquals(5, mask_bit1.getMaskValue());

        mask_bit1.setMaskValue(OpUnitMask.SET_DEFAULT_MASK);
        assertEquals(15, mask_bit1.getMaskValue());

        //bitmask 2 test -- bitmasks overlap
            /* bug related to overlapping bitmasks eclipse bz 261917 */
//        assertEquals(112, mask_bit2.getMaskValue());
//        mask_bit2.setMaskValue(0);
//        mask_bit2.setMaskFromIndex(0);
//        assertEquals(true, mask_bit2.isMaskSetFromIndex(0));
//        assertEquals(false, mask_bit2.isMaskSetFromIndex(1));
//        assertEquals(false, mask_bit2.isMaskSetFromIndex(2));
//        assertEquals(false, mask_bit2.isMaskSetFromIndex(3));
//        assertEquals(false, mask_bit2.isMaskSetFromIndex(4));
////        assertEquals(192, mask_bit2.getMaskValue());
//
//        mask_bit2.setMaskFromIndex(1);
//        assertEquals(true, mask_bit2.isMaskSetFromIndex(0));
//        assertEquals(true, mask_bit2.isMaskSetFromIndex(1));
//        assertEquals(false, mask_bit2.isMaskSetFromIndex(2));
//        assertEquals(false, mask_bit2.isMaskSetFromIndex(3));
//        assertEquals(false, mask_bit2.isMaskSetFromIndex(4));
////        assertEquals(3, mask_bit2.getMaskValue());
//
//        mask_bit2.setMaskFromIndex(2);
//        assertEquals(true, mask_bit2.isMaskSetFromIndex(0));
//        assertEquals(true, mask_bit2.isMaskSetFromIndex(1));
//        assertEquals(true, mask_bit2.isMaskSetFromIndex(2));
//        assertEquals(false, mask_bit2.isMaskSetFromIndex(3));
//        assertEquals(false, mask_bit2.isMaskSetFromIndex(4));
////        assertEquals(7, mask_bit2.getMaskValue());
//
//        mask_bit2.setMaskFromIndex(3);
//        assertEquals(true, mask_bit2.isMaskSetFromIndex(0));
//        assertEquals(true, mask_bit2.isMaskSetFromIndex(1));
//        assertEquals(true, mask_bit2.isMaskSetFromIndex(2));
//        assertEquals(true, mask_bit2.isMaskSetFromIndex(3));
//        assertEquals(false, mask_bit2.isMaskSetFromIndex(4));
////        assertEquals(15, mask_bit2.getMaskValue());
//
//        mask_bit2.setMaskFromIndex(4);
//        assertEquals(true, mask_bit2.isMaskSetFromIndex(0));
//        assertEquals(true, mask_bit2.isMaskSetFromIndex(1));
//        assertEquals(true, mask_bit2.isMaskSetFromIndex(2));
//        assertEquals(true, mask_bit2.isMaskSetFromIndex(3));
//        assertEquals(true, mask_bit2.isMaskSetFromIndex(4));
////        assertEquals(15, mask_bit2.getMaskValue());
//
//        mask_bit2.unSetMaskFromIndex(1);
//        assertEquals(true, mask_bit2.isMaskSetFromIndex(0));
//        assertEquals(false, mask_bit2.isMaskSetFromIndex(1));
//        assertEquals(true, mask_bit2.isMaskSetFromIndex(2));
//        assertEquals(true, mask_bit2.isMaskSetFromIndex(3));
//        assertEquals(true, mask_bit2.isMaskSetFromIndex(4));
//
//        mask_bit2.unSetMaskFromIndex(2);
//        assertEquals(true, mask_bit2.isMaskSetFromIndex(0));
//        assertEquals(false, mask_bit2.isMaskSetFromIndex(1));
//        assertEquals(false, mask_bit2.isMaskSetFromIndex(2));
//        assertEquals(true, mask_bit2.isMaskSetFromIndex(3));
//        assertEquals(true, mask_bit2.isMaskSetFromIndex(4));
//
//        mask_bit2.unSetMaskFromIndex(3);
//        assertEquals(true, mask_bit2.isMaskSetFromIndex(0));
//        assertEquals(false, mask_bit2.isMaskSetFromIndex(1));
//        assertEquals(false, mask_bit2.isMaskSetFromIndex(2));
//        assertEquals(false, mask_bit2.isMaskSetFromIndex(3));
//        assertEquals(true, mask_bit2.isMaskSetFromIndex(4));
//
//        mask_bit2.unSetMaskFromIndex(4);
//        assertEquals(true, mask_bit2.isMaskSetFromIndex(0));
//        assertEquals(false, mask_bit2.isMaskSetFromIndex(1));
//        assertEquals(false, mask_bit2.isMaskSetFromIndex(2));
//        assertEquals(false, mask_bit2.isMaskSetFromIndex(3));
//        assertEquals(false, mask_bit2.isMaskSetFromIndex(4));
//
//        mask_bit2.setMaskFromIndex(2);
//        assertEquals(true, mask_bit2.isMaskSetFromIndex(0));
//        assertEquals(false, mask_bit2.isMaskSetFromIndex(1));
//        assertEquals(true, mask_bit2.isMaskSetFromIndex(2));
//        assertEquals(false, mask_bit2.isMaskSetFromIndex(3));
//        assertEquals(false, mask_bit2.isMaskSetFromIndex(4));
////        assertEquals(5, mask_bit2.getMaskValue());
//
//        mask_bit2.unSetMaskFromIndex(1);
//        assertEquals(true, mask_bit2.isMaskSetFromIndex(0));
//        assertEquals(false, mask_bit2.isMaskSetFromIndex(1));
//        assertEquals(true, mask_bit2.isMaskSetFromIndex(2));
//        assertEquals(false, mask_bit2.isMaskSetFromIndex(3));
//        assertEquals(false, mask_bit2.isMaskSetFromIndex(4));
//
//        mask_bit2.unSetMaskFromIndex(3);
//        assertEquals(true, mask_bit2.isMaskSetFromIndex(0));
//        assertEquals(false, mask_bit2.isMaskSetFromIndex(1));
//        assertEquals(true, mask_bit2.isMaskSetFromIndex(2));
//        assertEquals(false, mask_bit2.isMaskSetFromIndex(3));
//        assertEquals(false, mask_bit2.isMaskSetFromIndex(4));
////        assertEquals(5, mask_bit2.getMaskValue());
//
//        mask_bit2.setMaskFromIndex(2);
//        assertEquals(true, mask_bit2.isMaskSetFromIndex(0));
//        assertEquals(false, mask_bit2.isMaskSetFromIndex(1));
//        assertEquals(true, mask_bit2.isMaskSetFromIndex(2));
//        assertEquals(false, mask_bit2.isMaskSetFromIndex(3));
//        assertEquals(false, mask_bit2.isMaskSetFromIndex(4));
////        assertEquals(5, mask_bit2.getMaskValue());
//
//        mask_bit2.setMaskValue(OpUnitMask.SET_DEFAULT_MASK);
//        assertEquals(112, mask_bit2.getMaskValue());


        //exclusive test
        assertEquals(0, mask_exl.getMaskValue());
        assertEquals(true, mask_exl.isMaskSetFromIndex(0));
        assertEquals(false, mask_exl.isMaskSetFromIndex(1));
        assertEquals(false, mask_exl.isMaskSetFromIndex(2));
        mask_exl.setMaskFromIndex(1);
        assertEquals(false, mask_exl.isMaskSetFromIndex(0));
        assertEquals(1, mask_exl.getMaskValue());
        mask_exl.unSetMaskFromIndex(1);
        assertEquals(1, mask_exl.getMaskValue());
        mask_exl.setMaskFromIndex(2);
        assertEquals(2, mask_exl.getMaskValue());
        mask_exl.setDefaultMaskValue();
        assertEquals(0, mask_exl.getMaskValue());


        //mandatory test
        assertEquals(0, mask_mand.getMaskValue());
        assertEquals(false, mask_mand.isMaskSetFromIndex(0));
        mask_mand.setMaskFromIndex(0);
        assertEquals(0, mask_mand.getMaskValue());
        mask_mand.unSetMaskFromIndex(0);
        assertEquals(0, mask_mand.getMaskValue());
        mask_mand.setMaskValue(10);
        mask_mand.setDefaultMaskValue();
        assertEquals(0, mask_mand.getMaskValue());

        //invalid test
        assertEquals(1, mask_invalid.getMaskValue());
        assertEquals(false, mask_invalid.isMaskSetFromIndex(0));
        mask_invalid.setMaskFromIndex(0);
        assertEquals(1, mask_invalid.getMaskValue());
        mask_invalid.unSetMaskFromIndex(0);
        assertEquals(1, mask_invalid.getMaskValue());
        mask_invalid.setMaskValue(0);
        mask_invalid.setDefaultMaskValue();
        assertEquals(1, mask_invalid.getMaskValue());
    }

    @Test
    public void testInfo() {
        OpEvent[] result = info.getEvents(-1);
        assertEquals(0, result.length);

        assertNull(info.findEvent("doesnt exist")); //$NON-NLS-1$

        OpEvent event = info.findEvent("CPU_CLK_UNHALTED"); //$NON-NLS-1$
        assertEquals("CPU_CLK_UNHALTED", event.getText()); //$NON-NLS-1$
    }
}
