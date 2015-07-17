/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.massif;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.linuxtools.profiling.ui.ProfileUIUtils;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class MassifPlugin extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.eclipse.linuxtools.valgrind.massif"; //$NON-NLS-1$
    public static final String EDITOR_ID = PLUGIN_ID + ".chartEditor"; //$NON-NLS-1$

    // The shared instance
    private static MassifPlugin plugin;

    // Needed for source lookup on massif output, since massif only supplies filenames
    // and not full paths
    private ISourceLocator locator;

    public static final String TOOL_ID = "org.eclipse.linuxtools.valgrind.launch.massif"; //$NON-NLS-1$

    /**
     * The constructor
     */
    public MassifPlugin() {
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    public static FontMetrics getFontMetrics(Control control) {
         GC gc = new GC(control);
         gc.setFont(control.getFont());
         FontMetrics fontMetrics = gc.getFontMetrics();
         gc.dispose();
         return fontMetrics;
    }

    public void openEditorForNode(MassifHeapTreeNode element) {
        // do source lookup
        if (locator instanceof ISourceLookupDirector) {
            Object obj = ((ISourceLookupDirector) locator).getSourceElement(element.getFilename());
            if (obj instanceof IStorage){
                try {
                    // Most likely a remote project
                    if (obj instanceof IFile) {
                        ProfileUIUtils.openEditorAndSelect(((IFile)obj), element.getLine());
                    // Local projects
                    } else {
                        String fullFilePath = ((IStorage) obj).getFullPath().toOSString();
                        ProfileUIUtils.openEditorAndSelect(fullFilePath, element.getLine());
                    }
                } catch (PartInitException|BadLocationException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected void setSourceLocator(ISourceLocator locator) {
        this.locator = locator;
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static MassifPlugin getDefault() {
        return plugin;
    }

}
