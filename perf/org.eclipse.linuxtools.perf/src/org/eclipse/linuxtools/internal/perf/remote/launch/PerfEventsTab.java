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
package org.eclipse.linuxtools.internal.perf.remote.launch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.internal.perf.PerfCore;
import org.eclipse.linuxtools.internal.perf.PerfPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class PerfEventsTab extends org.eclipse.linuxtools.internal.perf.launch.PerfEventsTab {
	private int rawTabIndex = 0;
	private int bpTabIndex = 0;
	private String[] tabNames = null;
	private Composite top;

	//Function adapted from org.eclipse.linuxtools.oprofile.launch.configuration.OprofileSetupTab.java
	@Override
	public void createControl(Composite parent) {
		top = new Composite(parent, SWT.NONE);
		setControl(top);
		top.setLayout(new GridLayout());

		createVerticalSpacer(top, 1);

		//Default event checkbox
		_chkDefaultEvent = new Button(top, SWT.CHECK);
		_chkDefaultEvent.setText("Default Event"); //$NON-NLS-1$
		_chkDefaultEvent.setLayoutData(new GridData());
		_chkDefaultEvent.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent se) {
				refreshDefaultEnabled();
				updateLaunchConfigurationDialog();
			}
		});

		createVerticalSpacer(top, 1);

	}

	@Override
	public void initializeFrom(ILaunchConfiguration config) {
		//Maybe not the best place to load the event list but we'll see.
		HashMap<String,ArrayList<String>> events = PerfCore.getEventList(config);
		if (tabNames==null) {
			//tabs for each of the counters
			//String[] tabNames = new String[]{"Hardware Event","Software Event","Hardware Cache Event","Tracepoint Event",  "Raw hardware event descriptor","Hardware breakpoint"};
			tabNames = events.keySet().toArray(new String[events.keySet().size()]);
			_eventTabItems = new TabItem[tabNames.length];
			_eventTabLists = new Table[tabNames.length];

			_tabFolder = new TabFolder(top, SWT.NONE);
			_tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

			//Initialize each tab.		 
			for (int i = 0; i < tabNames.length; i++) {

				_eventTabItems[i] = new TabItem(_tabFolder, SWT.NONE);
				_eventTabItems[i].setText(tabNames[i]);

				if (tabNames[i].equals(PerfPlugin.STRINGS_HWBREAKPOINTS) || tabNames[i].equals(PerfPlugin.STRINGS_RAWHWEvents)) {
					//These are for the two special tabs for custom events.

					//Composite to contain it all
					Composite c = new Composite(_tabFolder, SWT.NONE);
					c.setLayout(new GridLayout(2, false));

					//A list to check off existing custom events (or show the new ones added)
					Table eventList = new Table(c, SWT.CHECK | SWT.MULTI);
					_eventTabLists[i] = eventList;				
					eventList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
					eventList.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent se) {
							updateLaunchConfigurationDialog();
						}
					});
					/* Snippet to insert static items
				TableItem x = new TableItem(eventList, SWT.NONE);
				x.setText("hello1");
				x = new TableItem(eventList, SWT.NONE);
				x.setText("hello2");*/

					//Right side to enter new events and delete old ones 
					Composite right = new Composite(c, SWT.NONE);
					right.setLayout(new GridLayout(2,false));
					right.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, true));

					//for adding
					Label l = new Label(right, SWT.NONE);				
					l.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false,2,1));
					Text t = new Text(right, SWT.SINGLE | SWT.BORDER);
					t.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
					if (tabNames[i].equals(PerfPlugin.STRINGS_HWBREAKPOINTS)) {
						bpTabIndex = i;
						_bpText = t;
						l.setText("Please enter the hardware breakpoint in the form mem:<addr>[:access].");
					}
					if (tabNames[i].equals(PerfPlugin.STRINGS_RAWHWEvents)) {
						rawTabIndex = i;
						_rawText = t;
						l.setText("Please enter the raw register encoding in the form rNNN.");
					}

					Button b = new Button(right, SWT.PUSH);
					b.setText("      Add       ");
					b.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
					b.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent se) {
							int i = _tabFolder.getSelectionIndex();
							if (rawTabIndex == i) {
								new TableItem(_eventTabLists[i], SWT.NONE).setText(_rawText.getText());							
							} else if(bpTabIndex == i) {
								new TableItem(_eventTabLists[i], SWT.NONE).setText(_bpText.getText());
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

					//spacer label.
					l = new Label(right, SWT.NONE); 
					l.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,2,1));

					//for removing
					b = new Button(right, SWT.PUSH);
					b.setText("Remove Selected Events");
					b.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false,2,1));
					b.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent se) {
							_eventTabLists[_tabFolder.getSelectionIndex()].remove(_eventTabLists[_tabFolder.getSelectionIndex()].getSelectionIndices());
							updateLaunchConfigurationDialog();
						}
					});
					l = new Label(right, SWT.NONE);
					l.setForeground(new Color(right.getDisplay(), 100,100,100));
					l.setText("Note: Select by highlighting, not by checking.");
					l.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false,2,1));

					_eventTabItems[i].setControl(c);
				} else {
					//This loads all the events 'perf list' gives into their respective tabs.
					Table eventList = new Table(_tabFolder, SWT.CHECK);
					_eventTabLists[i] = eventList;

					ArrayList<String> evlist = events.get(tabNames[i]);
					for (String e : evlist) {
						TableItem x = new TableItem(eventList, SWT.NONE);
						x.setText(e);
					}

					eventList.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent se) {
							updateLaunchConfigurationDialog();
						}
					});

					_eventTabItems[i].setControl(eventList);
				}
			}
		}
		//if (PerfPlugin.DEBUG_ON) System.out.println("Initializing eventsTab from previous config.");
		try {
			//restore whether things are default event/enabled or not.
			_chkDefaultEvent.setSelection(config.getAttribute(PerfPlugin.ATTR_DefaultEvent, PerfPlugin.ATTR_DefaultEvent_default));
			refreshDefaultEnabled();

			//restore custom hw breakpoints
			List<?> hwbps = config.getAttribute(PerfPlugin.ATTR_HwBreakpointEvents, PerfPlugin.ATTR_HwBreakpointEvents_default);
			if (hwbps != null) {
				for (int i = 0; i < _eventTabLists.length; i++) {
					if (_eventTabItems[i].getText().equals(PerfPlugin.STRINGS_HWBREAKPOINTS)) {
						_eventTabLists[i].removeAll();
						for (Object e : hwbps) {
							TableItem x = new TableItem(_eventTabLists[i], SWT.NONE);
							x.setText((String)e);
						}
					}
				}
			}

			//restore custom raw hw events
			List<?> rawhe = config.getAttribute(PerfPlugin.ATTR_RawHwEvents, PerfPlugin.ATTR_RawHwEvents_default);
			if (rawhe != null) {
				for (int i = 0; i < _eventTabLists.length; i++) {
					if (_eventTabItems[i].getText().equals(PerfPlugin.STRINGS_RAWHWEvents)) {
						_eventTabLists[i].removeAll();
						for (Object e : rawhe) {
							TableItem x = new TableItem(_eventTabLists[i], SWT.NONE);
							x.setText((String)e);
						}
					}
				}
			}

			//tick all the boxes that are checked (the events i mean)			
			//This is a little inefficient, I guess. TODO Check more efficiently?
			List<?> selectedEvents = config.getAttribute(PerfPlugin.ATTR_SelectedEvents, PerfPlugin.ATTR_SelectedEvents_default);
			if (selectedEvents != null) {
				for(Object s : selectedEvents) {
					for (int i = 0; i < _eventTabLists.length; i++) {
						for(TableItem x : _eventTabLists[i].getItems()) {
							if (x.getText().equals(s))
								x.setChecked(true);
						}
					}
				}
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


}
