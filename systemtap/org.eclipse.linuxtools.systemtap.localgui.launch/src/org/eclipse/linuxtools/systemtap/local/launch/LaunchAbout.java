/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.systemtap.local.launch;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.systemtap.local.core.PluginConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;


public class LaunchAbout extends SystemTapLaunchShortcut {
		
	/**
	 * The About function is special insofar that it doesn't actually use SystemTap.
	 * It just opens a dialogue containing some information about the pre-installed
	 * scripts.
	 * 
	 * We're having it extend SystemTapLaunchShortcut for simplicity's sake.
	 */
	
	public void launch(IBinary bin, String mode) {
		
		try {
			for (ICElement b : bin.getCProject().getChildrenOfType(ICElement.C_CCONTAINER)) {
				ICContainer c = (ICContainer) b;
				for (ITranslationUnit ast : c.getTranslationUnits()) {
					TranslationUnitVisitor v = new TranslationUnitVisitor();
					ast.accept(v);
			}
			}
		} catch (CModelException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		
//		try {
//		for (ICElement b : bin.getChildren() ) {
//			if (!b.getElementName().contains(":")) {
//				System.out.println(b.getResource().getFullPath());
//				CDOM d = CDOM.getInstance();
//				
//				IASTTranslationUnit ast = d.getTranslationUnit( (IFile)b.getResource());
//				TranslationUnitVisitor v = new TranslationUnitVisitor();
//				ast.accept(v);
//			}
//		}
//		} catch (UnsupportedDialectException e) {
//			e.printStackTrace();
//		} catch (CModelException e) {
//			e.printStackTrace();
//		}

	}
	
	public void launch(IEditorPart ed, String mode) {
		
		super.launch(ed, mode);
//		launchMessage();
	}
	
	public void launchMessage() {
		
		Display disp = Display.getCurrent();
		if (disp == null){
			disp = Display.getDefault();
		}

		
		Shell sh = new Shell(disp, SWT.MIN | SWT.MAX);
		sh.setSize(425, 540);
		GridLayout gl = new GridLayout(1, true);
		sh.setLayout(gl);

		sh.setText(Messages.getString("LaunchAbout.0")); //$NON-NLS-1$
		
		Image img = new Image(disp, PluginConstants.PLUGIN_LOCATION+"systemtap.png"); //$NON-NLS-1$
		Composite cmp = new Composite(sh, sh.getStyle());
		cmp.setLayout(gl);
		GridData data = new GridData(415,100);
		cmp.setLayoutData(data);
		cmp.setBackgroundImage(img);

		Composite c = new Composite(sh, sh.getStyle());
		c.setLayout(gl);
		GridData gd = new GridData(415,400);
		c.setLayoutData(gd);
		c.setLocation(0,300);
		StyledText viewer = new StyledText(c, SWT.READ_ONLY | SWT.MULTI
				| SWT.V_SCROLL | SWT.WRAP | SWT.BORDER);		
		
		GridData viewerGD = new GridData(SWT.FILL, SWT.FILL, true, true);
		viewer.setLayoutData(viewerGD);
		Font font = new Font(sh.getDisplay(), "Monospace", 11, SWT.NORMAL); //$NON-NLS-1$
		viewer.setFont(font);
		viewer.setText(
				 Messages.getString("LaunchAbout.2") + //$NON-NLS-1$
				 Messages.getString("LaunchAbout.3") + //$NON-NLS-1$
				 Messages.getString("LaunchAbout.4") + //$NON-NLS-1$
				 Messages.getString("LaunchAbout.5") +  //$NON-NLS-1$
				 Messages.getString("LaunchAbout.6") + //$NON-NLS-1$
				 Messages.getString("LaunchAbout.7") + //$NON-NLS-1$
				 
				 Messages.getString("LaunchAbout.8") + //$NON-NLS-1$
//				 
//				 Messages.getString("LaunchAbout.9") + //$NON-NLS-1$
//				 Messages.getString("LaunchAbout.10") + //$NON-NLS-1$
				 
				 Messages.getString("LaunchAbout.11") + //$NON-NLS-1$
				 Messages.getString("LaunchAbout.12") + //$NON-NLS-1$
				 Messages.getString("LaunchAbout.13") + //$NON-NLS-1$
				 
//				 Messages.getString("LaunchAbout.14") + //$NON-NLS-1$
//				 Messages.getString("LaunchAbout.15") + //$NON-NLS-1$
//				 Messages.getString("LaunchAbout.16") + //$NON-NLS-1$
				 
				 Messages.getString("LaunchAbout.17") + //$NON-NLS-1$
				 
//				 Messages.getString("LaunchAbout.18") + //$NON-NLS-1$
//				 Messages.getString("LaunchAbout.19") + //$NON-NLS-1$
				 
				 Messages.getString("LaunchAbout.20") + //$NON-NLS-1$
				 Messages.getString("LaunchAbout.21") //$NON-NLS-1$
				);


		
		sh.open();		
	}
}