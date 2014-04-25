/*******************************************************************************
 * Copyright (c) 2004, 2008, 2009 Red Hat, Inc.
 * (C) Copyright 2010 IBM Corp. 2010
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Thavidu Ranatunga (IBM) - derived and modified from
 *        org.eclipse.linuxtools.oprofile.launch.configuration.OprofileEventConfigTab
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf.launch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.linuxtools.internal.perf.PerfCore;
import org.eclipse.linuxtools.internal.perf.PerfPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class PerfEventsTab extends AbstractLaunchConfigurationTab {

    private static final String EMPTY_STRING = ""; //$NON-NLS-1$

    // checkbox for selecting default event
    protected Button chkDefaultEvent;

    // the event tabs within the tab folder
    protected TabItem[] eventTabItems;

    // the table within the corresponding event tab
    protected Table[] eventTable;

    protected TabFolder tabFolder;
    private int rawTabIndex = 0;
    private int bpTabIndex = 0;
    protected Text rawText;
    protected Text bpText;
    private Composite top;
    private IProject previousProject = null;


    /**
     * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
     */
    @Override
    public Image getImage() {
        return PerfPlugin.getImageDescriptor("icons/event.gif").createImage(); //$NON-NLS-1$
    }

    @Override
    public void createControl(Composite parent) {
        Composite top = new Composite(parent, SWT.NONE);
        setControl(top);
        top.setLayout(new GridLayout());
        this.top = top;
    }

    private void createEventTabs(Composite top, ILaunchConfiguration config){
        //Maybe not the best place to load the event list but we'll see.
        Map<String,List<String>> events = PerfCore.getEventList(config);

        // the special counters should be last
        ArrayList<String> tmpTabNames = new ArrayList<>(events.keySet());
        final List<String> SPECIAL_EVENTS = Arrays.asList(new String[] {
                PerfPlugin.STRINGS_HWBREAKPOINTS,
                PerfPlugin.STRINGS_RAWHWEvents });
        tmpTabNames.removeAll(SPECIAL_EVENTS);
        tmpTabNames.addAll(SPECIAL_EVENTS);

        String [] tabNames = tmpTabNames.toArray(new String [0]);
        eventTabItems = new TabItem[tabNames.length];
        eventTable = new Table[tabNames.length];

        tabFolder = new TabFolder(top, SWT.NONE);
        tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        // Initialize each tab.
        for (int i = 0; i < tabNames.length; i++) {

            eventTabItems[i] = new TabItem(tabFolder, SWT.NONE);
            eventTabItems[i].setText(tabNames[i]);

            // These are for the two special tabs for custom events.
            if (tabNames[i].equals(PerfPlugin.STRINGS_HWBREAKPOINTS)
                    || tabNames[i].equals(PerfPlugin.STRINGS_RAWHWEvents)) {

                // Composite to contain it all
                Composite c = new Composite(tabFolder, SWT.NONE);
                c.setLayout(new GridLayout(2, false));

                // A list to check off existing custom events (or show the new ones added)
                Table table = new Table(c, SWT.CHECK | SWT.MULTI);
                eventTable[i] = table;
                table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
                table.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent se) {
                        updateLaunchConfigurationDialog();
                    }
                });

                // Right side to enter new events and delete old ones
                Composite right = new Composite(c, SWT.NONE);
                right.setLayout(new GridLayout(2,false));
                right.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, true));

                // for adding
                Label l = new Label(right, SWT.NONE);
                l.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false,2,1));
                Text t = new Text(right, SWT.SINGLE | SWT.BORDER);
                t.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
                if (tabNames[i].equals(PerfPlugin.STRINGS_HWBREAKPOINTS)) {
                    bpTabIndex = i;
                    bpText = t;
                    l.setText("Please enter the hardware breakpoint in the form mem:<addr>[:access].");
                }
                if (tabNames[i].equals(PerfPlugin.STRINGS_RAWHWEvents)) {
                    rawTabIndex = i;
                    rawText = t;
                    l.setText("Please enter the raw register encoding in the form rNNN.");
                }

                Button b = new Button(right, SWT.PUSH);
                b.setText("Add");
                b.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
                b.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent se) {
                        int i = tabFolder.getSelectionIndex();
                        if (rawTabIndex == i) {
                            new TableItem(eventTable[i], SWT.NONE).setText(rawText.getText());
                        } else if(bpTabIndex == i) {
                            new TableItem(eventTable[i], SWT.NONE).setText(bpText.getText());
                        }
                        updateLaunchConfigurationDialog();
                    }
                });
                l = new Label(right, SWT.NONE);
                l.setForeground(new Color(right.getDisplay(), 100,100,100));
                if (tabNames[i].equals(PerfPlugin.STRINGS_HWBREAKPOINTS)) {
                    l.setText("For example, .........");
                }
                if (tabNames[i].equals(PerfPlugin.STRINGS_RAWHWEvents)) {
                    l.setText("For example, r1a8");
                }
                l.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false,2,1));

                // spacer label.
                l = new Label(right, SWT.NONE);
                l.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,2,1));

                // for removing
                b = new Button(right, SWT.PUSH);
                b.setText("Remove Selected Events");
                b.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false,2,1));
                b.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent se) {
                        eventTable[tabFolder.getSelectionIndex()].remove(eventTable[tabFolder.getSelectionIndex()].getSelectionIndices());
                        updateLaunchConfigurationDialog();
                    }
                });
                l = new Label(right, SWT.NONE);
                l.setForeground(new Color(right.getDisplay(), 100,100,100));
                l.setText("Note: Select by highlighting, not by checking.");
                l.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false,2,1));

                eventTabItems[i].setControl(c);

            } else {
                // This loads all the events 'perf list' gives into their respective tabs.
                Table table = new Table(tabFolder, SWT.CHECK);
                eventTable[i] = table;

                List<String> eventList = events.get(tabNames[i]);
                for (String event : eventList) {
                    TableItem item = new TableItem(table, SWT.NONE);
                    item.setText(event);
                }

                table.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent se) {
                        updateLaunchConfigurationDialog();
                    }
                });

                eventTabItems[i].setControl(table);
            }
        }
    }

    private void refreshDefaultEnabled() {
        boolean state = !chkDefaultEvent.getSelection();
        for (Table tab : eventTable) {
            tab.setEnabled(state);
        }
    }

    @Override
    public String getName() {
        return "Perf Events";
    }

    @Override
    public void initializeFrom(ILaunchConfiguration config) {
        IProject project = getProject(config);

        try {
            if(previousProject == null || (previousProject != null && !previousProject.equals(project))){
                Control[] children = top.getChildren();

                for (Control control : children) {
                    control.dispose();
                }

                createVerticalSpacer(top, 1);

                // Default event checkbox
                chkDefaultEvent = new Button(top, SWT.CHECK);
                chkDefaultEvent.setText("Default Event"); //$NON-NLS-1$
                chkDefaultEvent.setLayoutData(new GridData());
                chkDefaultEvent.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent se) {
                        refreshDefaultEnabled();
                        updateLaunchConfigurationDialog();
                    }
                });

                createEventTabs(top, config);
            }

            // restore whether things are default event/enabled or not.
            chkDefaultEvent.setSelection(config.getAttribute(PerfPlugin.ATTR_DefaultEvent, PerfPlugin.ATTR_DefaultEvent_default));
            refreshDefaultEnabled();

            // restore custom hw breakpoints
            List<?> hwbps = config.getAttribute(PerfPlugin.ATTR_HwBreakpointEvents, PerfPlugin.ATTR_HwBreakpointEvents_default);
            if (hwbps != null) {
                for (int i = 0; i < eventTable.length; i++) {
                    if (eventTabItems[i].getText().equals(PerfPlugin.STRINGS_HWBREAKPOINTS)) {
                        eventTable[i].removeAll();
                        for (Object e : hwbps) {
                            TableItem x = new TableItem(eventTable[i], SWT.NONE);
                            x.setText((String)e);
                        }
                    }
                }
            }

            // restore custom raw hw events
            List<?> rawhe = config.getAttribute(PerfPlugin.ATTR_RawHwEvents, PerfPlugin.ATTR_RawHwEvents_default);
            if (rawhe != null) {
                for (int i = 0; i < eventTable.length; i++) {
                    if (eventTabItems[i].getText().equals(PerfPlugin.STRINGS_RAWHWEvents)) {
                        eventTable[i].removeAll();
                        for (Object e : rawhe) {
                            TableItem x = new TableItem(eventTable[i], SWT.NONE);
                            x.setText((String)e);
                        }
                    }
                }
            }

            // tick all the boxes that are checked
            List<?> selectedEvents = config.getAttribute(PerfPlugin.ATTR_SelectedEvents, PerfPlugin.ATTR_SelectedEvents_default);

            if(selectedEvents != null){
                for (int i = 0; i < eventTable.length; i++) {
                    for(TableItem event : eventTable[i].getItems()) {
                        if(selectedEvents.contains(event.getText())){
                            event.setChecked(true);
                        } else {
                            event.setChecked(false);
                        }
                    }
                }
            }
            previousProject = project;
            } catch (CoreException e) {
                e.printStackTrace();
            }
    }

    @Override
    public void performApply(ILaunchConfigurationWorkingCopy wconfig) {
        //Store default event checkbox
        wconfig.setAttribute(PerfPlugin.ATTR_DefaultEvent, chkDefaultEvent.getSelection());

        //Store which events are selected
        ArrayList<String> selectedEvents = new ArrayList<>();
        for (int i = 0; i < eventTable.length; i++) {
            for(TableItem x : eventTable[i].getItems()) {
                if (x.getChecked())
                    selectedEvents.add(x.getText());
            }
        }

        if (selectedEvents.size() == 0) {
            wconfig.setAttribute(PerfPlugin.ATTR_SelectedEvents, (String) null);
        } else {
            wconfig.setAttribute(PerfPlugin.ATTR_SelectedEvents, selectedEvents);
        }

        //Flag for multiple events
        if ((chkDefaultEvent.getSelection() == false) && (selectedEvents.size() >= 1)) {
            wconfig.setAttribute(PerfPlugin.ATTR_MultipleEvents, true);
        } else {
            wconfig.setAttribute(PerfPlugin.ATTR_MultipleEvents, false);
        }
        if (selectedEvents.size() <= 0) {
            //If they unticked the default box but didn't select any events revert to default.
            wconfig.setAttribute(PerfPlugin.ATTR_DefaultEvent, true);
        }

        //Store any custom HW BreakPoints they added (even if unchecked).
        ArrayList<String> hwbps = new ArrayList<>();
        for (int i = 0; i < eventTable.length; i++) {
            if (eventTabItems[i].getText().equals(PerfPlugin.STRINGS_HWBREAKPOINTS)) {
                for(TableItem x : eventTable[i].getItems()) {
                    hwbps.add(x.getText());
                }
            }
        }
        if (hwbps.size() == 0) {
            hwbps = null; // to match with default value.
        }
        wconfig.setAttribute(PerfPlugin.ATTR_HwBreakpointEvents, hwbps);

        //Store any custom Raw HW Events they added (even if unchecked).
        ArrayList<String> rawhwe = new ArrayList<>();
        for (int i = 0; i < eventTable.length; i++) {
            if (eventTabItems[i].getText().equals(PerfPlugin.STRINGS_RAWHWEvents)) {
                for(TableItem x : eventTable[i].getItems()) {
                    rawhwe.add(x.getText());
                }
            }
        }
        if (rawhwe.size() == 0) {
            rawhwe = null; //to match with default value.
        }
        wconfig.setAttribute(PerfPlugin.ATTR_RawHwEvents, rawhwe);
    }

    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy wconfig) {
        wconfig.setAttribute(PerfPlugin.ATTR_DefaultEvent, PerfPlugin.ATTR_DefaultEvent_default);
        wconfig.setAttribute(PerfPlugin.ATTR_MultipleEvents, PerfPlugin.ATTR_MultipleEvents_default);
        wconfig.setAttribute(PerfPlugin.ATTR_SelectedEvents, PerfPlugin.ATTR_SelectedEvents_default);
        wconfig.setAttribute(PerfPlugin.ATTR_HwBreakpointEvents, PerfPlugin.ATTR_HwBreakpointEvents_default);
        wconfig.setAttribute(PerfPlugin.ATTR_RawHwEvents, PerfPlugin.ATTR_RawHwEvents_default);
    }

    private IProject getProject(ILaunchConfiguration config){
        String name = null;
        try {
            name = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, EMPTY_STRING);
        } catch (CoreException e) {
            return null;
        }
        if (name.isEmpty()){
            return null;
        }

        return ResourcesPlugin.getWorkspace().getRoot().getProject(name);
    }

}
