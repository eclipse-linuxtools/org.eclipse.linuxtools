/*******************************************************************************
 * Copyright (c) 2011 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.linuxtools.cdt.libhover.devhelp.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

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
import org.osgi.framework.FrameworkUtil;

public class CheckDevhelp {
	
	public final static String CACHE_EXT_LIBHOVER = "org.eclipse.linuxtools.cdt.libhover.testCacheExtLibhover"; //$NON-NLS-1$

	@Before
	public void setUp() {
		IPath p = LibhoverPlugin.getDefault().getStateLocation().append("C"); //$NON-NLS-1$
		File f = new File(p.toOSString());
		f.delete();
	}

	@Test
	public void testParse() {
		ParseDevHelp.DevHelpParser p =  
			new ParseDevHelp.DevHelpParser("/usr/share/gtk-doc/html"); //$NON-NLS-1$
		LibHoverInfo hover = p.parse(new NullProgressMonitor());
		assertTrue(hover != null);
		Map<String, FunctionInfo> functions = hover.functions;
		assertTrue(functions != null);
		assertTrue(functions.size() > 0);
		try {
			// Now, output the LibHoverInfo for caching later
			IPath location = LibhoverPlugin.getDefault().getStateLocation().append("C"); //$NON-NLS-1$
			File ldir = new File(location.toOSString());
			ldir.mkdir();
			location = location.append("devhelp.libhover"); //$NON-NLS-1$
			FileOutputStream f = new FileOutputStream(location.toOSString());
			ObjectOutputStream out = new ObjectOutputStream(f);
			out.writeObject(hover);
			out.close();
		} catch(Exception e) {
			fail();
		}
		IPreferenceStore ps = LibhoverPlugin.getDefault().getPreferenceStore();
		ps.setValue(CACHE_EXT_LIBHOVER, true);
		LibHover.getLibHoverDocs();
		Collection<LibHoverLibrary> c = LibHover.getLibraries();
		assertTrue(c.size() > 0);
		boolean found = false;
		for (Iterator<LibHoverLibrary> i = c.iterator(); i.hasNext();) {
			LibHoverLibrary l = i.next();
			if (l.getName().equals("devhelp")) //$NON-NLS-1$
				found = true;
		}
		assertTrue(found);
	}
	@Test
	public void testTocProvider() {
		DevHelpTocProvider provider = new DevHelpTocProvider();
		ITocContribution[] contributions = provider.getTocContributions("en_US"); //$NON-NLS-1$
		assertTrue(contributions.length > 0);
		ITocContribution c = contributions[0];
		assertEquals(c.getId(), "org.eclipse.linuxtools.cdt.libhover.devhelp.toc"); //$NON-NLS-1$
		assertTrue(c.isPrimary());
		assertEquals(c.getCategoryId(), null);
		assertEquals(c.getLocale(), "en_US"); //$NON-NLS-1$
		String[] docs = c.getExtraDocuments();
		assertTrue(docs.length == 0);
		assertEquals(c.getLinkTo(), ""); //$NON-NLS-1$
		assertEquals(c.getContributorId(), "org.eclipse.linuxtools.cdt.libhover.devhelp"); //$NON-NLS-1$
	}
	
	@Test
	public void testHelpTopic() {
		String bundlePath = FrameworkUtil.getBundle(this.getClass()).getLocation();
		bundlePath = bundlePath.substring(bundlePath.indexOf('/')); // need to chop extraneious chars from front to get actual path
		// Decision to make this a fragment means we get the host's bundle, not this
		bundlePath = bundlePath.replaceFirst("libhover.devhelp", "libhover.devhelp.tests"); //$NON-NLS-1$ //$NON-NLS-2$
		IPath x = new Path(bundlePath);
		// For some reason the bundle gets the host of this fragment instead of this fragment bundle
		x = x.append("devhelp/html"); //$NON-NLS-1$
		IPreferenceStore ps = DevHelpPlugin.getDefault().getPreferenceStore();
		ps.setValue(PreferenceConstants.DEVHELP_DIRECTORY, x.toOSString());
		DevHelpToc toc = new DevHelpToc();
		ITopic[] topics = toc.getTopics();
		assertTrue(topics.length > 0);
		ITopic topic = topics[0];
		assertTrue(topic.getLabel().startsWith("test1")); //$NON-NLS-1$
		ITopic[] subtopics = topic.getSubtopics();
		assertTrue(subtopics.length > 3);
		IUAElement[] elements = topic.getChildren();
		assertTrue(elements.length > 3);
		String href = topic.getHref();
		assertEquals(href, "/org.eclipse.linuxtools.cdt.libhover.devhelp/test1/index.html"); //$NON-NLS-1$
		assertTrue(topic.isEnabled(null));
	}
	
}
