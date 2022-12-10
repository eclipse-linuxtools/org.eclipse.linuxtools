/*******************************************************************************
 * Copyright (c) 2011, 2018 Red Hat Inc. and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.linuxtools.cdt.libhover.devhelp.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Map;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.help.ITocContribution;
import org.eclipse.help.ITopic;
import org.eclipse.help.IUAElement;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.cdt.libhover.FunctionInfo;
import org.eclipse.linuxtools.cdt.libhover.LibHoverInfo;
import org.eclipse.linuxtools.cdt.libhover.LibhoverPlugin;
import org.eclipse.linuxtools.internal.cdt.libhover.LibHover;
import org.eclipse.linuxtools.internal.cdt.libhover.LibHoverLibrary;
import org.eclipse.linuxtools.internal.cdt.libhover.devhelp.DevHelpPlugin;
import org.eclipse.linuxtools.internal.cdt.libhover.devhelp.DevHelpToc;
import org.eclipse.linuxtools.internal.cdt.libhover.devhelp.DevHelpTocProvider;
import org.eclipse.linuxtools.internal.cdt.libhover.devhelp.ParseDevHelp;
import org.eclipse.linuxtools.internal.cdt.libhover.devhelp.preferences.PreferenceConstants;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleReference;

public class CheckDevhelpTest {

    public final static String CACHE_EXT_LIBHOVER = "org.eclipse.linuxtools.cdt.libhover.testCacheExtLibhover"; //$NON-NLS-1$

    @Before
    public void setUp() {
        IPath p = LibhoverPlugin.getDefault().getStateLocation().append("C"); //$NON-NLS-1$
        File f = new File(p.toOSString());
        f.delete();
    }

    @Test
    public void testParse() throws IOException {
        ParseDevHelp.DevHelpParser p = new ParseDevHelp.DevHelpParser(
                "/usr/share/gtk-doc/html"); //$NON-NLS-1$
        LibHoverInfo hover = p.parse(new NullProgressMonitor());
        assertNotNull(hover);
        Map<String, FunctionInfo> functions = hover.functions;
        assertNotNull(functions);
        assertFalse(functions.isEmpty());
        // Now, output the LibHoverInfo for caching later
        IPath location = LibhoverPlugin.getDefault().getStateLocation()
                .append("C"); //$NON-NLS-1$
        File ldir = new File(location.toOSString());
        ldir.mkdir();
        location = location.append("devhelp.libhover"); //$NON-NLS-1$
        try (FileOutputStream f = new FileOutputStream(location.toOSString());
                ObjectOutputStream out = new ObjectOutputStream(f)) {
            out.writeObject(hover);
        }
        IPreferenceStore ps = LibhoverPlugin.getDefault().getPreferenceStore();
        ps.setValue(CACHE_EXT_LIBHOVER, true);
        LibHover.getLibHoverDocs();
        Collection<LibHoverLibrary> c = LibHover.getLibraries();
        assertFalse(c.isEmpty());
        boolean found = false;
        for (LibHoverLibrary l : c) {
            if (l.getName().equals("devhelp")) { //$NON-NLS-1$
                found = true;
            }
        }
        assertTrue(found);
    }

    @Test
    public void testParseMyApp() throws IOException {
        // We need to have a devhelp directory with contents to test.
        // Copy over the needed devhelp/html2 contents in this test plug-in
        // to the workspace.  This test verifies: bug 433507 and also
        // tests non-breaking spaces used in latest gtk-doc devhelp generation.
        ClassLoader cl = getClass().getClassLoader();
        Bundle bundle = null;
        if (cl instanceof BundleReference) {
            bundle = ((BundleReference) cl).getBundle();
        }
        IWorkspace ws = ResourcesPlugin.getWorkspace();
        IPath wslocpath = ws.getRoot().getLocation();

        IPath outfilepath = wslocpath.append("devhelp/html2/myapp"); //$NON-NLS-1$
        File outfiledir = outfilepath.toFile();
        assertTrue(outfiledir.mkdirs());
        outfilepath = outfilepath.append("myapp.devhelp2"); //$NON-NLS-1$
        File outfile = outfilepath.toFile();
        IPath outfilepath2 = wslocpath.append("devhelp/html2/myapp/myapp.html"); //$NON-NLS-1$
        File outfile2 = outfilepath2.toFile();
        IPath outfilepath3 = wslocpath.append("devhelp/html2/myapp/myapp2.html"); //$NON-NLS-1$
        File outfile3 = outfilepath3.toFile();
        IPath outfilepath4 = wslocpath.append("devhelp/html2/myapp/style.css"); //$NON-NLS-1$
        File outfile4 = outfilepath4.toFile();
        outfile.createNewFile();
        outfile2.createNewFile();
        outfile3.createNewFile();
        outfile4.createNewFile();
        try (InputStream in = FileLocator.openStream(bundle, new Path(
                "devhelp/html2/myapp/myapp.devhelp2"), false)) { //$NON-NLS-1$
            Files.copy(in, Paths.get(outfile.toURI()), StandardCopyOption.REPLACE_EXISTING);
        }
        try (InputStream in2 = FileLocator.openStream(bundle, new Path(
                "devhelp/html2/myapp/myapp.html"), false)) { //$NON-NLS-1$
            Files.copy(in2, Paths.get(outfile2.toURI()), StandardCopyOption.REPLACE_EXISTING);
        }
        try (InputStream in3 = FileLocator.openStream(bundle, new Path(
                "devhelp/html2/myapp/myapp2.html"), false)) { //$NON-NLS-1$
            Files.copy(in3, Paths.get(outfile3.toURI()), StandardCopyOption.REPLACE_EXISTING);
        }
        try (InputStream in4 = FileLocator.openStream(bundle, new Path(
                "devhelp/html2/myapp/myapp2.html"), false)) { //$NON-NLS-1$
            Files.copy(in4, Paths.get(outfile4.toURI()), StandardCopyOption.REPLACE_EXISTING);
        }

        IPath x = wslocpath.append("devhelp/html2"); //$NON-NLS-1$
        ParseDevHelp.DevHelpParser p = new ParseDevHelp.DevHelpParser(x.toOSString());
        LibHoverInfo hover = p.parse(new NullProgressMonitor());
        assertNotNull(hover);
        Map<String, FunctionInfo> functions = hover.functions;
        assertNotNull(functions);
        assertFalse(functions.isEmpty());
        FunctionInfo f = functions.get("myapp_init"); //$NON-NLS-1$
        assertNotNull(f);
        String desc = f.getDescription();
        assertFalse(desc.isEmpty());
        String endOfDesc = desc.substring(desc.length() - 50);
        assertEquals("pointer to application's argv. </p></dd>\n\n\n</dl>\n\n", endOfDesc); //$NON-NLS-1$
        FunctionInfo g = functions.get("myapp_init_check"); //$NON-NLS-1$
        assertNotNull(g);
    }

    @Test
    public void testTocProvider() {
        DevHelpTocProvider provider = new DevHelpTocProvider();
        ITocContribution[] contributions = provider
                .getTocContributions("en_US"); //$NON-NLS-1$
        assertTrue(contributions.length > 0);
        ITocContribution c = contributions[0];
        assertEquals(c.getId(),
                "org.eclipse.linuxtools.cdt.libhover.devhelp.toc"); //$NON-NLS-1$
        assertFalse(c.isPrimary());
        assertEquals(c.getCategoryId(), null);
        assertEquals(c.getLocale(), "en_US"); //$NON-NLS-1$
        String[] docs = c.getExtraDocuments();
        assertEquals(docs.length, 0);
        assertFalse(c.getLinkTo().isEmpty());
        assertEquals(c.getContributorId(),
                "org.eclipse.linuxtools.cdt.libhover.devhelp"); //$NON-NLS-1$
    }

    @Test
    public void testHelpTopic() throws IOException {
        // We need to have a devhelp directory with contents to test.
        // Copy over the needed devhelp/html contents in this test plug-in,
        // test1.devhelp2 and index.html, to the workspace.
        ClassLoader cl = getClass().getClassLoader();
        Bundle bundle = null;
        if (cl instanceof BundleReference) {
            bundle = ((BundleReference) cl).getBundle();
        }
        IWorkspace ws = ResourcesPlugin.getWorkspace();
        IPath wslocpath = ws.getRoot().getLocation();

        IPath outfilepath = wslocpath.append("devhelp/html/test1"); //$NON-NLS-1$
        File outfiledir = outfilepath.toFile();
        assertTrue(outfiledir.mkdirs());
        outfilepath = outfilepath.append("test1.devhelp2"); //$NON-NLS-1$
        File outfile = outfilepath.toFile();
        IPath outfilepath2 = wslocpath.append("devhelp/html/test1/index.html"); //$NON-NLS-1$
        File outfile2 = outfilepath2.toFile();
        outfile.createNewFile();
        outfile2.createNewFile();
        try (InputStream in = FileLocator.openStream(bundle, new Path(
                "devhelp/html/test1/test1.devhelp2"), false)) { //$NON-NLS-1$
            Files.copy(in, Paths.get(outfile.toURI()), StandardCopyOption.REPLACE_EXISTING);
        }
        try (InputStream in2 = FileLocator.openStream(bundle, new Path(
                "devhelp/html/test1/index.html"), false)) { //$NON-NLS-1$
            Files.copy(in2, Paths.get(outfile2.toURI()), StandardCopyOption.REPLACE_EXISTING);
        }

        IPath x = wslocpath.append("devhelp/html"); //$NON-NLS-1$
        IPreferenceStore ps = DevHelpPlugin.getDefault().getPreferenceStore();
        ps.setValue(PreferenceConstants.DEVHELP_DIRECTORY, x.toOSString());
        DevHelpToc toc = new DevHelpToc();
        ITopic[] topics = toc.getTopics();
        // Verify we have the test1 topic
        assertTrue(topics.length > 0);
        ITopic topic = topics[0];
        assertTrue(topic.getLabel().startsWith("test1")); //$NON-NLS-1$
        ITopic[] subtopics = topic.getSubtopics();
        // Verify it has 4 or more sub-topics
        assertTrue(subtopics.length > 3);
        IUAElement[] elements = topic.getChildren();
        assertTrue(elements.length > 3);
        String href = topic.getHref();
        // Verify the topic href is the index.html file and that the topic is
        // enabled
        assertEquals(href,
                "/org.eclipse.linuxtools.cdt.libhover.devhelp/test1/index.html"); //$NON-NLS-1$
        assertTrue(topic.isEnabled(null));
    }
}
