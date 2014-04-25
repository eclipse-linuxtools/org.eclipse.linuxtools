/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.graphing.ui.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.linuxtools.internal.systemtap.graphing.ui.views.Messages;
import org.eclipse.linuxtools.systemtap.graphing.core.datasets.IFilteredDataSet;
import org.eclipse.linuxtools.systemtap.graphing.ui.GraphDisplaySet;
import org.eclipse.linuxtools.systemtap.structures.listeners.ITabListener;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.part.EditorPart;



/**
 * This class is designed to hold a number of different running script sets.
 * Each running script is given its own tab that contains all the details for
 * that specific script.  Script sets can be created or disposed of at the will
 * of the user.
 * @author Ryan Morse
 * @since 3.0 Migrated from .ui.graphing package.
 */
public class GraphSelectorEditor extends EditorPart {

    private CTabFolder scriptFolder;
    private List<GraphDisplaySet> displaySets;
    private List<ITabListener> tabListeners;
    public static final String ID = "org.eclipse.linuxtools.systemtap.graphing.ui.views.GraphSelectorEditor"; //$NON-NLS-1$

    public GraphSelectorEditor() {
        super();
        displaySets = new ArrayList<>();
        tabListeners = new ArrayList<>();
    }

    /**
     * This method will create a new script set for each of the provided dataSets.
     * Each new script set will be given a new tab item at the end of the list.
     * @param scriptName The full name of the script that is being monitored.
     * @param titles The names to be shown on each new tab
     * @param dataSets The <code>IFilteredDataSet</code>s for each new script set
     * @since 2.2
     */
    public void createScriptSets(String scriptName, List<String> titles, List<IFilteredDataSet> dataSets) {
        CTabItem item = null;

        for (int i = 0, n = titles.size(); i < n; i++) {
            item = new CTabItem(scriptFolder, SWT.CLOSE);
            item.setText(titles.get(i));
            Composite parent = new Composite(scriptFolder, SWT.NONE);
            final GraphDisplaySet gds = new GraphDisplaySet(parent, dataSets.get(i));
            displaySets.add(gds);
            item.setControl(parent);
            item.addDisposeListener(new DisposeListener() {

                @Override
                public void widgetDisposed(DisposeEvent e) {
                    gds.dispose();
                }
            });
        }

        scriptFolder.setSelection(item); // Choose the last created item.
        fireTabOpenEvent();
        this.setPartName(NLS.bind(Messages.GraphSelectorEditor_graphsEditorTitle, scriptName.substring(scriptName.lastIndexOf('/')+1)));
    }

    /**
     * This method creates the framework for what will be displayed by this dialog box.
     * @param parent The composite that will contain all the elements from this dialog
     */
    @Override
    public void createPartControl(Composite parent) {
        FormData data2 = new FormData();
        data2.left = new FormAttachment(0, 0);
        data2.top = new FormAttachment(0, 0);
        data2.right = new FormAttachment(100, 0);
        data2.bottom = new FormAttachment(100, 0);
        Composite cmpGraph = new Composite(parent, SWT.NONE);
        cmpGraph.setLayoutData(data2);

        //This is for the tab view
        cmpGraph.setLayout(new FormLayout());

        //Create the folder for all of the script sets, so it takes up all of the parent composite
        scriptFolder = new CTabFolder(cmpGraph, SWT.NONE);
        FormData data = new FormData();
        data.top = new FormAttachment(0, 0);
        data.left = new FormAttachment(0, 0);
        data.right = new FormAttachment(100, 0);
        data.bottom = new FormAttachment(100, 0);
        scriptFolder.setLayoutData(data);
        scriptFolder.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                fireTabChangedEvent();
            }
        });


        scriptFolder.addCTabFolder2Listener(new CTabFolder2Adapter() {
            @Override
            public void close(CTabFolderEvent e) {
                displaySets.remove(scriptFolder.indexOf((CTabItem)e.item));
                fireTabCloseEvent();
            }
        });

    }

    /**
     * Finds and returns the active component inside of the active script set
     * @return The <code>GraphDisplaySet</code> that is currently active
     * @since 2.0
     */
    public GraphDisplaySet getActiveDisplaySet() {
        return getDisplaySet(scriptFolder.getSelectionIndex());
    }

    /**
     * @return The title of the tab containing the active graph.
     * @since 3.0
     */
    public String getActiveTitle() {
        return scriptFolder.getSelection().getText();
    }

    /**
     * Finds and returns the component of the provided index.
     * @param index The index of the GraphDisplaySet to return
     * @return The <code>GraphDisplaySet</code> of the provided
     * index, or null if the index is out of range.
     * @since 2.2
     */
    public GraphDisplaySet getDisplaySet(int index) {
        if(index >= 0 && index < displaySets.size()) {
            return displaySets.get(index);
        } else {
            return null;
        }
    }

    @Override
    public void setFocus() {
        // Empty
    }

    /**
     * @since 2.0
     */
    public void addTabListener(ITabListener listener) {
        tabListeners.add(listener);
    }

    /**
     * @since 2.0
     */
    public void removeTabListener(ITabListener listener) {
        tabListeners.remove(listener);
    }

    private void fireTabCloseEvent() {
        for(ITabListener tabListener:tabListeners) {
            tabListener.tabClosed();
        }
    }

    private void fireTabOpenEvent() {
        for(ITabListener tabListener:tabListeners) {
            tabListener.tabOpened();
        }
    }

    private void fireTabChangedEvent() {
        for(ITabListener tabListener:tabListeners) {
            tabListener.tabChanged();
        }
    }

    /**
     * Removes all internal references in this class.  Nothing should make any references
     * to anything in this class after calling the dispose method.
     */
    @Override
    public void dispose() {
        super.dispose();

        if(null != scriptFolder && !scriptFolder.isDisposed()) {
            scriptFolder.dispose();
        }
        scriptFolder = null;
        if(null != tabListeners) {
            tabListeners.clear();
        }
        tabListeners = null;
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        // TODO
    }

    @Override
    public void doSaveAs() {
        // TODO
    }

    @Override
    public void init(IEditorSite site, IEditorInput input) {
        setInput(input);
        setSite(site);
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }
}
