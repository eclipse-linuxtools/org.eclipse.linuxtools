/*******************************************************************************
 * Copyright (c) 2006, 2007, 2009 Red Hat Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.cdt.autotools.tests.editors;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IRegion;
import org.eclipse.linuxtools.cdt.autotools.tests.AutotoolsTestsPlugin;
import org.eclipse.linuxtools.cdt.autotools.tests.ProjectTools;
import org.eclipse.linuxtools.cdt.autotools.ui.editors.AutoconfEditor;
import org.eclipse.linuxtools.cdt.autotools.ui.properties.AutotoolsPropertyConstants;
import org.eclipse.linuxtools.internal.cdt.autotools.text.hover.AutoconfTextHover;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;


public class AutoconfTextHoverTest extends TestCase {

	ProjectTools tools;
	private IProject project;
	private IFile configureInFile;
	private IFile testbadFile;
	private AutoconfTextHover textHover;
	private AutoconfEditor editor;
	
	static String configureInContents =
		"AC_PREREQ(2.13)" + "\n" +
		// 16
		"AC_INIT(sample)" + "\n" +
		// 32
		"AC_CANONICAL_SYSTEM"  + "\n" +
		// 52
		"AM_INIT_AUTOMAKE(sample, 0.1, nodefine)" + "\n" +
		// 92
		"AC_PROG_CC" + "\n" + 
		// 103
		"AC_OUTPUT(Makefile sample/Makefile)" + "\n" +
		// 139
		"";
	
	static String testbadContents = 
		"<? bad setting/?>" + "\n" +
		"* a bad line << -->";
	
	private IWorkbench workbench;
	
	protected void setUp() throws Exception {
        super.setUp();
        tools = new ProjectTools();
        if (!ProjectTools.setup())
        	fail("could not perform basic project workspace setup");
        
        project = ProjectTools.createProject("testProjectATHT");
        
        if(project == null) {
        	fail("Unable to create test project");
        }
        
        project.open(new NullProgressMonitor());
        
        configureInFile = tools.createFile(project, "configure.in", configureInContents);
        testbadFile = tools.createFile(project, "test-bad.xml", testbadContents);
		workbench = AutotoolsTestsPlugin.getDefault().getWorkbench();
		
		IEditorPart openEditor = org.eclipse.ui.ide.IDE.openEditor(workbench
				.getActiveWorkbenchWindow().getActivePage(), configureInFile,
				true);
		
		editor = (AutoconfEditor) openEditor;
		textHover = new AutoconfTextHover(editor);
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
		project.delete(true, false, ProjectTools.getMonitor());
	}
	
	public void testBadHoverInfo() {
		String autoconfVersion = null;
		String automakeVersion = null;
		String autoconfDocName = textHover.AUTOCONF_MACROS_DOC_NAME;
		String automakeDocName = textHover.AUTOMAKE_MACROS_DOC_NAME;
		try {
			autoconfVersion = project.getPersistentProperty(AutotoolsPropertyConstants.AUTOCONF_VERSION);
			automakeVersion = project.getPersistentProperty(AutotoolsPropertyConstants.AUTOMAKE_VERSION);
		} catch (CoreException e1) {
			// TODO Auto-generated catch block
			fail("Can't get current autoconf and automake document versions");
		}
		try {
			project.setPersistentProperty(AutotoolsPropertyConstants.AUTOCONF_VERSION, "nonexistent");
		} catch (CoreException e1) {
			// TODO Auto-generated catch block
			fail("Can't set autoconf document version");
		}
		IRegion region = textHover.getHoverRegion(editor.getViewer(), 0);
		String s = textHover.getHoverInfo(editor.getViewer(), region);
		assertEquals(s, null);
		region = textHover.getHoverRegion(editor.getViewer(), 52);
		s = textHover.getHoverInfo(editor.getViewer(), region);
		assertNotNull(s);
		
		AutoconfTextHover.AUTOCONF_MACROS_DOC_NAME = project.getLocation().append("test").toString();
		try {
			project.setPersistentProperty(AutotoolsPropertyConstants.AUTOCONF_VERSION, "bad");
		} catch (CoreException e1) {
			// TODO Auto-generated catch block
			fail("Can't set autoconf document version");
		}
		region = textHover.getHoverRegion(editor.getViewer(), 0);
		s = textHover.getHoverInfo(editor.getViewer(), region);
		assertEquals(s, null);
		region = textHover.getHoverRegion(editor.getViewer(), 52);
		s = textHover.getHoverInfo(editor.getViewer(), region);
		assertNotNull(s);
		AutoconfTextHover.AUTOCONF_MACROS_DOC_NAME = autoconfDocName;
	
		try {
			project.setPersistentProperty(AutotoolsPropertyConstants.AUTOCONF_VERSION, autoconfVersion);
		} catch (CoreException e1) {
			// TODO Auto-generated catch block
			fail("Can't set autoconf document version");
		}
		region = textHover.getHoverRegion(editor.getViewer(), 0);
		s = textHover.getHoverInfo(editor.getViewer(), region);
		assertNotNull(s);
		region = textHover.getHoverRegion(editor.getViewer(), 52);
		s = textHover.getHoverInfo(editor.getViewer(), region);
		assertNotNull(s);

		try {
			project.setPersistentProperty(AutotoolsPropertyConstants.AUTOMAKE_VERSION, "nonexistent");
		} catch (CoreException e1) {
			// TODO Auto-generated catch block
			fail("Can't set automake document version");
		}

		region = textHover.getHoverRegion(editor.getViewer(), 0);
		s = textHover.getHoverInfo(editor.getViewer(), region);
		assertNotNull(s);
		region = textHover.getHoverRegion(editor.getViewer(), 52);
		s = textHover.getHoverInfo(editor.getViewer(), region);
		assertEquals(s, null);

		AutoconfTextHover.AUTOMAKE_MACROS_DOC_NAME = project.getLocation().append("test").toString();
		try {
			project.setPersistentProperty(AutotoolsPropertyConstants.AUTOMAKE_VERSION, "bad");
		} catch (CoreException e1) {
			// TODO Auto-generated catch block
			fail("Can't set automake document version");
		}

		region = textHover.getHoverRegion(editor.getViewer(), 0);
		s = textHover.getHoverInfo(editor.getViewer(), region);
		assertNotNull(s);
		region = textHover.getHoverRegion(editor.getViewer(), 52);
		s = textHover.getHoverInfo(editor.getViewer(), region);
		assertEquals(s, null);
		AutoconfTextHover.AUTOMAKE_MACROS_DOC_NAME = automakeDocName;

		try {
			project.setPersistentProperty(AutotoolsPropertyConstants.AUTOMAKE_VERSION, automakeVersion);
		} catch (CoreException e1) {
			// TODO Auto-generated catch block
			fail("Can't set autoconf document version");
		}
		region = textHover.getHoverRegion(editor.getViewer(), 0);
		s = textHover.getHoverInfo(editor.getViewer(), region);
		assertNotNull(s);
		region = textHover.getHoverRegion(editor.getViewer(), 52);
		s = textHover.getHoverInfo(editor.getViewer(), region);
		assertNotNull(s);
	}
}
