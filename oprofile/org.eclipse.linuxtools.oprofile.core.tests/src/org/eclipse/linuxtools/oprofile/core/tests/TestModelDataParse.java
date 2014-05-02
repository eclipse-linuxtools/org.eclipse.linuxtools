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

import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.internal.oprofile.core.model.OpModelImage;
import org.eclipse.linuxtools.internal.oprofile.core.model.OpModelSample;
import org.eclipse.linuxtools.internal.oprofile.core.model.OpModelSymbol;
import org.eclipse.linuxtools.internal.oprofile.core.opxml.OprofileSAXHandler;
import org.eclipse.linuxtools.internal.oprofile.core.opxml.modeldata.ModelDataProcessor;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.FrameworkUtil;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

public class TestModelDataParse {
    private static final String REL_PATH_TO_TEST_XML = "resources/test_model-data.xml"; //$NON-NLS-1$
    private static final String REL_PATH_TO_TEST_XML_MULTI_IMAGE = "resources/test_model-data_multiple_image.xml"; //$NON-NLS-1$
    private static final String IMAGE_OUTPUT = "/test/path/for/image, Count: 205000, Dependent Count: 5000\nSymbols: TestFunction1(int), File: /test/path/for/src/image.cpp, Count: 180000\n\tSample: Line #: 42, Count: 130000\n\tSample: Line #: 36, Count: 40000\n\tSample: Line #: 31, Count: 9999\n\tSample: Line #: 39, Count: 1\nSymbols: TestFunction2(int, int), File: /test/path/for/src/image2.cpp, Count: 20000\n\tSample: Line #: 94, Count: 19998\n\tSample: Line #: 12, Count: 1\n\tSample: Line #: 55, Count: 1\nDependent Image: /no-vmlinux, Count: 4400\nDependent Image: /lib64/ld-2.9.so, Count: 300\n\tSymbols: do_lookup_x, File: dl-lookup.c, Count: 299\n\t\tSample: Line #: 0, Count: 299\n\tSymbols: _dl_unload_cache, File: rawmemchr.c, Count: 1\n\t\tSample: Line #: 0, Count: 1\nDependent Image: /usr/lib64/libstdc++.so.6.0.10, Count: 160\nDependent Image: /lib64/libc-2.9.so, Count: 140\n\tSymbols: _IO_new_file_seekoff, File: , Count: 100\n\t\tSample: Line #: 0, Count: 100\n\tSymbols: bool std::operator!=<char, std::char_traits<char>, std::allocator<char> >(std::basic_string<char, std::char_traits<char>, std::allocator<char> > const&, char const*), File: , Count: 40\n\t\tSample: Line #: 0, Count: 40\n"; //$NON-NLS-1$
    private static final String IMAGE_OUTPUT_WITHTAB = "/test/path/for/image, Count: 205000, Dependent Count: 5000\n\tSymbols: TestFunction1(int), File: /test/path/for/src/image.cpp, Count: 180000\n\t\tSample: Line #: 42, Count: 130000\n\t\tSample: Line #: 36, Count: 40000\n\t\tSample: Line #: 31, Count: 9999\n\t\tSample: Line #: 39, Count: 1\n\tSymbols: TestFunction2(int, int), File: /test/path/for/src/image2.cpp, Count: 20000\n\t\tSample: Line #: 94, Count: 19998\n\t\tSample: Line #: 12, Count: 1\n\t\tSample: Line #: 55, Count: 1\n\tDependent Image: /no-vmlinux, Count: 4400\n\tDependent Image: /lib64/ld-2.9.so, Count: 300\n\t\tSymbols: do_lookup_x, File: dl-lookup.c, Count: 299\n\t\t\tSample: Line #: 0, Count: 299\n\t\tSymbols: _dl_unload_cache, File: rawmemchr.c, Count: 1\n\t\t\tSample: Line #: 0, Count: 1\n\tDependent Image: /usr/lib64/libstdc++.so.6.0.10, Count: 160\n\tDependent Image: /lib64/libc-2.9.so, Count: 140\n\t\tSymbols: _IO_new_file_seekoff, File: , Count: 100\n\t\t\tSample: Line #: 0, Count: 100\n\t\tSymbols: bool std::operator!=<char, std::char_traits<char>, std::allocator<char> >(std::basic_string<char, std::char_traits<char>, std::allocator<char> > const&, char const*), File: , Count: 40\n\t\t\tSample: Line #: 0, Count: 40\n"; //$NON-NLS-1$

    private OpModelImage parsedImage;
    private OpModelImage parsedErrorImage;

    @Before
    public void setUp() throws Exception {
        /* this code mostly taken from OpxmlRunner */
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

        String filePath = FileLocator.toFileURL(FileLocator.find(FrameworkUtil.getBundle(this.getClass()), new Path(REL_PATH_TO_TEST_XML), null)).getFile();
        reader.parse(new InputSource(new FileReader(filePath)));

        //2nd test image
        parsedErrorImage = new OpModelImage();
        ModelDataProcessor.CallData errorImage = new ModelDataProcessor.CallData(parsedErrorImage);
        handler = OprofileSAXHandler.getInstance(errorImage);

        // Set content/error handlers
        reader.setContentHandler(handler);
        reader.setErrorHandler(handler);

        filePath = FileLocator.toFileURL(FileLocator.find(FrameworkUtil.getBundle(this.getClass()), new Path(REL_PATH_TO_TEST_XML_MULTI_IMAGE), null)).getFile();
        reader.parse(new InputSource(new FileReader(filePath)));
    }

    @Test
    public void testParse() {
        //test attributes
        assertEquals("/test/path/for/image", parsedImage.getName()); //$NON-NLS-1$
        assertEquals(205000, parsedImage.getCount());

        //test symbols
        OpModelSymbol[] symbols = parsedImage.getSymbols();
        assertEquals(2, symbols.length);
        OpModelSymbol sym1 = symbols[0], sym2 = symbols[1];
        assertEquals("TestFunction1(int)", sym1.getName()); //$NON-NLS-1$
        assertEquals("/test/path/for/src/image.cpp", sym1.getFilePath()); //$NON-NLS-1$
        assertEquals(180000, sym1.getCount());
        assertEquals("TestFunction2(int, int)", sym2.getName()); //$NON-NLS-1$
        assertEquals("/test/path/for/src/image2.cpp", sym2.getFilePath()); //$NON-NLS-1$
        assertEquals(20000, sym2.getCount());

        //test samples
        OpModelSample[] sym1_spls = sym1.getSamples(), sym2_spls = sym2.getSamples();
        assertEquals(4, sym1_spls.length);
        assertEquals(3, sym2_spls.length);
        OpModelSample sym1_spl1 = sym1_spls[0], sym1_spl2 = sym1_spls[1], sym1_spl3 = sym1_spls[2], sym1_spl4 = sym1_spls[3];
        OpModelSample sym2_spl1 = sym2_spls[0], sym2_spl2 = sym2_spls[1], sym2_spl3 = sym2_spls[2];
        assertEquals(130000, sym1_spl1.getCount());
        assertEquals(42, sym1_spl1.getLine());
        assertEquals(40000, sym1_spl2.getCount());
        assertEquals(36, sym1_spl2.getLine());
        assertEquals(9999, sym1_spl3.getCount());
        assertEquals(31, sym1_spl3.getLine());
        assertEquals(1, sym1_spl4.getCount());
        assertEquals(39, sym1_spl4.getLine());
        assertEquals(19998, sym2_spl1.getCount());
        assertEquals(94, sym2_spl1.getLine());
        assertEquals(1, sym2_spl2.getCount());
        assertEquals(12, sym2_spl2.getLine());
        assertEquals(1, sym2_spl3.getCount());
        assertEquals(55, sym2_spl3.getLine());

        //test dependent images
        assertEquals(true, parsedImage.hasDependents());
        assertEquals(5000, parsedImage.getDepCount());
        OpModelImage[] deps = parsedImage.getDependents();
        assertEquals(4, deps.length);
        OpModelImage dep1 = deps[0], dep2 = deps[1], dep3 = deps[2], dep4 = deps[3];

        assertEquals(false, dep1.hasDependents());
        assertEquals("/no-vmlinux", dep1.getName()); //$NON-NLS-1$
        assertEquals(4400, dep1.getCount());

        assertEquals(false, dep2.hasDependents());
        OpModelSymbol[] dep2_syms = dep2.getSymbols();
        assertEquals(2, dep2_syms.length);
        OpModelSymbol dep2_sym1 = dep2_syms[0], dep2_sym2 = dep2_syms[1];
        OpModelSample[] dep2_sym1_spls = dep2_sym1.getSamples(), dep2_sym2_spls = dep2_sym2.getSamples();
        assertEquals(1, dep2_sym1_spls.length);
        assertEquals(1, dep2_sym2_spls.length);
        OpModelSample dep2_sym1_spl1 = dep2_sym1_spls[0], dep2_sym2_spl1 = dep2_sym2_spls[0];
        assertEquals("/lib64/ld-2.9.so", dep2.getName()); //$NON-NLS-1$
        assertEquals(300, dep2.getCount());
        assertEquals("do_lookup_x", dep2_sym1.getName()); //$NON-NLS-1$
        assertEquals("dl-lookup.c", dep2_sym1.getFilePath()); //$NON-NLS-1$
        assertEquals(299, dep2_sym1.getCount());
        assertEquals(299, dep2_sym1_spl1.getCount());
        assertEquals(0, dep2_sym1_spl1.getLine());
        assertEquals("_dl_unload_cache", dep2_sym2.getName()); //$NON-NLS-1$
        assertEquals("rawmemchr.c", dep2_sym2.getFilePath()); //$NON-NLS-1$
        assertEquals(1, dep2_sym2.getCount());
        assertEquals(1, dep2_sym2_spl1.getCount());
        assertEquals(0, dep2_sym2_spl1.getLine());

        assertEquals(false, dep3.hasDependents());
        assertEquals("/usr/lib64/libstdc++.so.6.0.10", dep3.getName()); //$NON-NLS-1$
        assertEquals(160, dep3.getCount());

        assertEquals(false, dep4.hasDependents());
        OpModelSymbol[] dep4_syms = dep4.getSymbols();
        assertEquals(2, dep4_syms.length);
        OpModelSymbol dep4_sym1 = dep4_syms[0], dep4_sym2 = dep4_syms[1];
        OpModelSample[] dep4_sym1_spls = dep4_sym1.getSamples(), dep4_sym2_spls = dep4_sym2.getSamples();
        assertEquals(1, dep4_sym1_spls.length);
        assertEquals(1, dep4_sym2_spls.length);
        OpModelSample dep4_sym1_spl1 = dep4_sym1_spls[0], dep4_sym2_spl1 = dep4_sym2_spls[0];
        assertEquals("/lib64/libc-2.9.so", dep4.getName()); //$NON-NLS-1$
        assertEquals(140, dep4.getCount());
        assertEquals("_IO_new_file_seekoff", dep4_sym1.getName()); //$NON-NLS-1$
        assertEquals("", dep4_sym1.getFilePath()); //$NON-NLS-1$
        assertEquals(100, dep4_sym1.getCount());
        assertEquals(100, dep4_sym1_spl1.getCount());
        assertEquals(0, dep4_sym1_spl1.getLine());
        assertEquals("bool std::operator!=<char, std::char_traits<char>, std::allocator<char> >(std::basic_string<char, std::char_traits<char>, std::allocator<char> > const&, char const*)", dep4_sym2.getName()); //$NON-NLS-1$
        assertEquals("", dep4_sym2.getFilePath()); //$NON-NLS-1$
        assertEquals(40, dep4_sym2.getCount());
        assertEquals(40, dep4_sym2_spl1.getCount());
        assertEquals(0, dep4_sym2_spl1.getLine());


        assertEquals(OpModelImage.IMAGE_PARSE_ERROR, parsedErrorImage.getCount());
        assertEquals(0, parsedErrorImage.getDepCount());
        assertNull(parsedErrorImage.getDependents());
        assertNull(parsedErrorImage.getSymbols());
        assertEquals("", parsedErrorImage.getName()); //$NON-NLS-1$
    }

    @Test
    public void testStringOutput() {
        assertEquals(IMAGE_OUTPUT, parsedImage.toString());
        assertEquals(IMAGE_OUTPUT_WITHTAB, parsedImage.toString("\t")); //$NON-NLS-1$
    }
}
