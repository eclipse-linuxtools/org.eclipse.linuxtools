/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.cdt.libhover.tests;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class BaseUITestCase extends BaseTestCase {

	public BaseUITestCase() {
		super();
	}

	public BaseUITestCase(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		final IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IViewPart view= activePage.findView("org.eclipse.cdt.ui.tests.DOMAST.DOMAST");
		if (view != null) {
			activePage.hideView(view);
		}
	}

	@Override
	protected void tearDown() throws Exception {
		runEventQueue(0);
		super.tearDown();
	}

	protected void runEventQueue(int time) {
		final long endTime= System.currentTimeMillis() + time;
		while (true) {
			while (Display.getCurrent().readAndDispatch()) {
			    //
			}
			long diff= endTime - System.currentTimeMillis();
			if (diff <= 0) {
				break;
			}
			try {
				Thread.sleep(Math.min(20, diff));
			} catch (InterruptedException e) {
				return;
			}
		}
	}

	protected void closeAllEditors() {
		IWorkbenchWindow[] windows= PlatformUI.getWorkbench().getWorkbenchWindows();
		for (IWorkbenchWindow window : windows) {
			IWorkbenchPage[] pages= window.getPages();
			for (IWorkbenchPage page : pages) {
				page.closeAllEditors(false);
			}
		}
	}

}

// Footnotes
// [0] Waiting for something to appear is very efficient; waiting for it to not
// appear is very inefficient. In the former case, regardless of how much time
// is alloted, we stop waiting as soon as the item appears, whereas in the
// latter we have to wait the entire timeout. In test suites with thousands of
// tests, efficiency is critical. Thus, in testing that a tree node doesn't have
// an Nth child, we shoot for efficiency and accept the risk of a false
// negative. More specifically, we wait only one second for the item TO NOT
// appear, whereas we give an item up to five seconds TO appear. This compromise
// is better than not having that sort of test at all, which some would argue is
// the better approach. In practice, it takes about 60-150 ms for the item to
// appear (on my machine), but we give it up to five seconds. Waiting one second
// for it to not appear should be more than adequate
