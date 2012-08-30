/*******************************************************************************
 * Copyright (c) 2011 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.linuxtools.cdt.libhover.devhelp.test;

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
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.cdt.libhover.FunctionInfo;
import org.eclipse.linuxtools.cdt.libhover.LibHoverInfo;
import org.eclipse.linuxtools.cdt.libhover.LibhoverPlugin;
import org.eclipse.linuxtools.internal.cdt.libhover.LibHover;
import org.eclipse.linuxtools.internal.cdt.libhover.LibHoverLibrary;
import org.eclipse.linuxtools.internal.cdt.libhover.devhelp.ParseDevHelp;
import org.junit.Before;
import org.junit.Test;

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
	
}
