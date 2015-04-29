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

import static org.junit.Assert.assertTrue;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNameBase;
import org.eclipse.cdt.internal.core.pdom.CModelListener;
import org.eclipse.cdt.internal.core.pdom.PDOMManager;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.Before;

@SuppressWarnings("restriction")
public class BaseUITestCase {

    private static final String DEFAULT_INDEXER_TIMEOUT_SEC = "10";
    private static final String INDEXER_TIMEOUT_PROPERTY = "indexer.timeout";
    /**
     * Indexer timeout used by tests. To avoid this timeout expiring during debugging add
     * -Dindexer.timeout=some_large_number to VM arguments of the test launch configuration.
     */
    protected static final int INDEXER_TIMEOUT_SEC =
            Integer.parseInt(System.getProperty(INDEXER_TIMEOUT_PROPERTY, DEFAULT_INDEXER_TIMEOUT_SEC));

    @Before
    public void setUp() {
        CPPASTNameBase.sAllowRecursionBindings= false;
        CPPASTNameBase.sAllowNameComputation= false;
        CModelListener.sSuppressUpdateOfLastRecentlyUsed= true;
        final IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IViewPart view= activePage.findView("org.eclipse.cdt.ui.tests.DOMAST.DOMAST");
        if (view != null) {
            activePage.hideView(view);
        }
    }

    @After
    public void tearDown() throws Exception {
        runEventQueue(0);
        ResourceHelper.cleanUp();
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

    public static void waitForIndexer(ICProject project) throws InterruptedException {
        Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_REFRESH, null);

        final PDOMManager indexManager = CCoreInternals.getPDOMManager();
        assertTrue(indexManager.joinIndexer(INDEXER_TIMEOUT_SEC * 1000, new NullProgressMonitor()));
        long waitms= 1;
        while (waitms < 2000 && !indexManager.isProjectRegistered(project)) {
            Thread.sleep(waitms);
            waitms *= 2;
        }
        assertTrue(indexManager.isProjectRegistered(project));
        assertTrue(indexManager.joinIndexer(INDEXER_TIMEOUT_SEC * 1000, new NullProgressMonitor()));
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
