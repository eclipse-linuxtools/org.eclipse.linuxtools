/*******************************************************************************
 * Copyright (c) 2004, 2008, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *    Kent Sebastian <ksebasti@redhat.com> -
 *    
 * CounterSubTab Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation (before subclassing)
 *    Kent Sebastian <ksebasti@redhat.com> - turned into a sub class,
 * 	     changed layouts, fixed up some interactivity issues, ..
 * 
 * UnitMaskViewer Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *    Kent Sebastian <ksebasti@redhat.com>
 *******************************************************************************/

package org.eclipse.linuxtools.internal.oprofile.launch.configuration;

import java.text.MessageFormat;
import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.internal.oprofile.core.Oprofile;
import org.eclipse.linuxtools.internal.oprofile.core.OprofileCorePlugin;
import org.eclipse.linuxtools.internal.oprofile.core.daemon.OpEvent;
import org.eclipse.linuxtools.internal.oprofile.core.daemon.OpUnitMask;
import org.eclipse.linuxtools.internal.oprofile.core.daemon.OprofileDaemonEvent;
import org.eclipse.linuxtools.internal.oprofile.launch.OprofileLaunchMessages;
import org.eclipse.linuxtools.internal.oprofile.launch.OprofileLaunchPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

/**
 * Thic class represents the event configuration tab of the launcher dialog.
 */
public class OprofileEventConfigTab extends AbstractLaunchConfigurationTab {
	protected Button _defaultEventCheck;
	protected OprofileCounter[] _counters = OprofileCounter.getCounters(null);
	protected CounterSubTab[] _counterSubTabs;

	/**
	 * Essentially the constructor for this tab; creates the 'default event'
	 * checkbox and an appropriate number of counter tabs.
	 * @param parent the parent composite
	 */
	public void createControl(Composite parent) {
		Composite top = new Composite(parent, SWT.NONE);
		setControl(top);
		top.setLayout(new GridLayout());

		if (getTimerMode()) {
			Label timerModeLabel = new Label(top, SWT.LEFT);
			timerModeLabel.setText(OprofileLaunchMessages.getString("tab.event.timermode.no.options")); //$NON-NLS-1$
		} else {
			createVerticalSpacer(top, 1);
	
			//default event checkbox
			_defaultEventCheck = new Button(top, SWT.CHECK);
			_defaultEventCheck.setText(OprofileLaunchMessages.getString("tab.event.defaultevent.button.text")); //$NON-NLS-1$
			_defaultEventCheck.setLayoutData(new GridData());
			_defaultEventCheck.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent se) {
					_handleEnabledToggle();
				}
			});
	
			createVerticalSpacer(top, 1);
	
			//tabs for each of the counters
			OprofileCounter[] counters = OprofileCounter.getCounters(null);
			TabItem[] counterTabs = new TabItem[counters.length];
			_counterSubTabs = new CounterSubTab[counters.length];
			
			TabFolder tabFolder = new TabFolder(top, SWT.NONE);
			tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	
	
			for (int i = 0; i < counters.length; i++) {
				Composite c = new Composite(tabFolder, SWT.NONE);
				CounterSubTab currentTab = new CounterSubTab(c, counters[i]);
				_counterSubTabs[i] = currentTab;
				
				counterTabs[i] = new TabItem(tabFolder, SWT.NONE);
				counterTabs[i].setControl(c);
				counterTabs[i].setText(OprofileLaunchMessages.getString("tab.event.counterTab.counterText") + String.valueOf(i)); //$NON-NLS-1$
			}
		}
	}

	/**
	 * @see ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration config) {
		if (!getTimerMode()) {
			try {
				for (int i = 0; i < _counters.length; i++) {
					_counters[i].loadConfiguration(config);
				}
				
				for (CounterSubTab tab : _counterSubTabs) {
					tab.initializeTab(config);
				}
	
				boolean enabledState = config.getAttribute(OprofileLaunchPlugin.ATTR_USE_DEFAULT_EVENT, true);
				_defaultEventCheck.setSelection(enabledState);
				setEnabledState(!enabledState);
	
				updateLaunchConfigurationDialog();
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @see ILaunchConfigurationTab#isValid(ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration config) {
		if (getTimerMode()) {
			return true;		//no options to check for validity
		} else {
			int numEnabledEvents = 0;
			boolean valid = true;
	
			try {
				if (config.getAttribute(OprofileLaunchPlugin.ATTR_USE_DEFAULT_EVENT, false)) {
					numEnabledEvents = 1;
				} else {
					//This seems like an odd way to validate, but since most of the validation
					// is done with the OprofileDaemonEvent that the counter wraps, this
					// is the easiest way.
					OprofileCounter[] counters = new OprofileCounter[getNumberOfCounters()];
					for (int i = 0; i < counters.length; i++) {
						counters[i] = new OprofileCounter(i);
						counters[i].loadConfiguration(config);
						if (counters[i].getEnabled()) {
							++numEnabledEvents;
	
							if (counters[i].getEvent() == null) {
								valid = false;
								break;
							}
	
							// First check min count
							int min = counters[i].getEvent().getMinCount();
							if (counters[i].getCount() < min) {
								valid = false;
								break;
							}
	
							// Next ask oprofile if it is valid
							if (!checkEventSetupValidity(counters[i].getNumber(), counters[i].getEvent().getText(), counters[i].getEvent().getUnitMask().getMaskValue())) {
								valid = false;
								break;
							}
						}
					}
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
	
			return (numEnabledEvents > 0 && valid);
		} 
	}

	/**
	 * @see ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy config) {
		if (getTimerMode()) {
			config.setAttribute(OprofileLaunchPlugin.ATTR_USE_DEFAULT_EVENT, true);
		} else {
			config.setAttribute(OprofileLaunchPlugin.ATTR_USE_DEFAULT_EVENT, _defaultEventCheck.getSelection());
			
			for (CounterSubTab cst : _counterSubTabs) {
				cst.performApply(config);
			}
		}
		try {
			config.doSave();
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		boolean useDefault = true;

		// When instantiated, the OprofileCounter will set defaults.
		for (int i = 0; i < _counters.length; i++) {
			_counters[i].saveConfiguration(config);
			if (_counters[i].getEnabled()) {
				useDefault = false;
			}
		}

		config.setAttribute(OprofileLaunchPlugin.ATTR_USE_DEFAULT_EVENT, useDefault);
		try {
			config.doSave();
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @see ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return OprofileLaunchMessages.getString("tab.event.name"); //$NON-NLS-1$
	}

	/**
	 * @see ILaunchConfigurationTab#getImage()
	 */
	@Override
	public Image getImage() {
		return OprofileLaunchPlugin.getImageDescriptor(OprofileLaunchPlugin.ICON_EVENT_TAB).createImage();
	}

	/**
	 * Handles the toggling of the default event check box. Not meant to be called
	 * directly.
	 */
	private void _handleEnabledToggle() {
		setEnabledState(!_defaultEventCheck.getSelection());
		updateLaunchConfigurationDialog();
	}
	
	/**
	 * Sets the state of the child counter tabs' widgets.
	 * @param state true for enabled, false for disabled
	 */
	private void setEnabledState(boolean state) {
		for (CounterSubTab cst : _counterSubTabs) {
			cst.setEnabledState(state);
		}
	}

	/*
	 * Extracted methods to be overridden by the test suite.
	 */

	/**
	 * Returns whether the event's unit mask is valid
	 * @param counter counter number
	 * @param name event name
	 * @param maskValue unit mask value
	 * @return true if valid config, false otherwise
	 */
	protected boolean checkEventSetupValidity(int counter, String name, int maskValue) {
		return OprofileLaunchPlugin.getCache().checkEvent(counter, name, maskValue);
	}

	/**
	 * Returns whether or not oprofile is operating in timer mode.
	 * @return true if oprofile is in timer mode, false otherwise
	 */
	protected boolean getTimerMode() {
		return Oprofile.getTimerMode();
	}
	
	/**
	 * Returns the number of hardware counters the cpu has 
	 * @return int number of counters
	 */
	protected int getNumberOfCounters() {
		return Oprofile.getNumberOfCounters();
	}
	
	/**
	 * A sub-tab of the OprofileEventConfigTab launch configuration tab. 
	 * Essentially, it is a frontend to an OprofileCounter. This is an 
	 * inner class because it requires methods from the parent tab (such as
	 * updateLaunchConfigurationDialog() when a widget changes state).
	 */ 
	protected class CounterSubTab {
		private Button _enabledCheck;
		private Button _profileKernelCheck;
		private Button _profileUserCheck;
		private Label _countTextLabel;
		private Text _countText;
		private Label _eventDescLabel;
		private Text _eventDescText;
		private UnitMaskViewer _unitMaskViewer;
		private ListViewer _eventList;
		private OprofileCounter _counter;
		
		private ScrolledComposite _scrolledTop;
		private Composite _tabTopContainer;

		/**
		 * Constructor for a subtab. Creates the layout and widgets for its content.
		 * @param parent composite the widgets will be created in 
		 * @param counter the associated OprofileCounter object
		 */
		public CounterSubTab(Composite parent, OprofileCounter counter) {
			_counter = counter;

			parent.setLayout(new GridLayout());

			//scrollable composite on top
			ScrolledComposite scrolledContainer = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
			scrolledContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			GridLayout layout = new GridLayout();
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			scrolledContainer.setLayout(layout);
			scrolledContainer.setExpandHorizontal(true);
			scrolledContainer.setExpandVertical(true);
			
			//composite to contain the rest of the tab
			Composite tabTopContainer = new Composite(scrolledContainer, SWT.NONE);
			scrolledContainer.setContent(tabTopContainer);
			layout = new GridLayout();
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			layout.numColumns = 2;
			tabTopContainer.setLayout(layout);
			tabTopContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true)); 

			//top cell
			Composite topCellComp = new Composite(tabTopContainer, SWT.NONE);
			layout = new GridLayout();
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			layout.numColumns = 2;
			topCellComp.setLayout(layout);
			topCellComp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
			
			createTopCell(topCellComp);
			
			createVerticalSpacer(tabTopContainer, 2);
			
			//left side composite group for eventList
			Composite eventListComp = new Composite(tabTopContainer, SWT.NONE);
			layout = new GridLayout();
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			eventListComp.setLayout(layout);
			//layoutdata is set later

			createLeftCell(eventListComp);
			
			
			//right side composite group for other event config and unit mask
			Composite eventConfigComp = new Composite(tabTopContainer, SWT.NONE);
			layout = new GridLayout();
			layout.numColumns = 2;
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			eventConfigComp.setLayout(layout);
			eventConfigComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			
			createRightCell(eventConfigComp);

			
			//set the list's composite layout based on the right cell's size
			GridData data = new GridData(SWT.FILL, SWT.FILL, false, true);
			data.heightHint = eventConfigComp.getSize().x;
			eventListComp.setLayoutData(data);
			
			_scrolledTop = scrolledContainer;
			_tabTopContainer = tabTopContainer;
		}
		
		/**
		 * Creates the "Enabled" checkbox, and the event description text.
		 * @param parent composite these widgets will be created in
		 */
		private void createTopCell(Composite parent) {
			//checkbox
			_enabledCheck = new Button(parent, SWT.CHECK);
			_enabledCheck.setText(OprofileLaunchMessages.getString("tab.event.counterSettings.enabled.button.text")); //$NON-NLS-1$
			_enabledCheck.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
			_enabledCheck.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent se) {
					_counter.setEnabled(_enabledCheck.getSelection());
					_setEnabledState(_counter.getEnabled());
					updateLaunchConfigurationDialog();
				}
			});
			_enabledCheck.setEnabled(false);
			
			//label for textbox
			_eventDescLabel = new Label(parent, SWT.NONE);
			_eventDescLabel.setText(OprofileLaunchMessages.getString("tab.event.eventDescription.label.text")); //$NON-NLS-1$
			_eventDescLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

			//textbox
			_eventDescText = new Text(parent, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
			_eventDescText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		}

		/**
		 * Creates the event list widget.
		 * @param parent composite these widgets will be created in
		 */
		private void createLeftCell(Composite parent) {
			_eventList = new ListViewer(parent, SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
			_eventList.getList().setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));

			_eventList.setLabelProvider(new ILabelProvider(){
				public String getText(Object element) {
					OpEvent e = (OpEvent) element;
					return e.getText();
				}
				public Image getImage(Object element) { return null; }
				public void addListener(ILabelProviderListener listener) { }
				public void dispose() { }
				public boolean isLabelProperty(Object element, String property) { return false; }
				public void removeListener(ILabelProviderListener listener) { }
			});
			
			_eventList.setContentProvider(new IStructuredContentProvider() {
				public Object[] getElements(Object inputElement) {
					OprofileCounter ctr = (OprofileCounter) inputElement;
					return (OpEvent[]) ctr.getValidEvents();
				}
				public void dispose() { }
				public void inputChanged(Viewer arg0, Object arg1, Object arg2) { }
			});

			//adds the events to the list from the counter
			_eventList.setInput(_counter);
			
			_eventList.addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent sce) {
					_handleEventListSelectionChange();
				}
			});
		}
		
		/**
		 * Creates the 2 profile space checkboxes, event count and unit mask widget.
		 * @param parent composite these widgets will be created in
		 */
		private void createRightCell(Composite parent) {
			//profile kernel checkbox
			_profileKernelCheck = new Button(parent, SWT.CHECK);
			_profileKernelCheck.setText(OprofileLaunchMessages.getString("tab.event.counterSettings.profileKernel.check.text")); //$NON-NLS-1$
			_profileKernelCheck.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent se) {
					_handleProfileKernelToggle();
				}
			});
			
			//profile user checkbox -- should this ever be disabled?
			_profileUserCheck = new Button(parent, SWT.CHECK);
			_profileUserCheck.setText(OprofileLaunchMessages.getString("tab.event.counterSettings.profileUser.check.text")); //$NON-NLS-1$
			_profileUserCheck.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent se) {
					_handleProfileUserToggle();
				}
			});
			
			//event count label/text 
			_countTextLabel = new Label(parent, SWT.NONE);
			_countTextLabel.setText(OprofileLaunchMessages.getString("tab.event.counterSettings.count.label.text")); //$NON-NLS-1$
			_countText = new Text(parent, SWT.SINGLE | SWT.BORDER);
			_countText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			_countText.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent me) {
					_handleCountTextModify();
				}
			});

			//unit mask widget
			Composite unitMaskComp = new Composite(parent, SWT.NONE);
			GridLayout layout = new GridLayout();
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			unitMaskComp.setLayout(layout);
			unitMaskComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

			_unitMaskViewer = new UnitMaskViewer(unitMaskComp);
		}
		
		/**
		 * Initializes the tab on first creation.
		 * @param config default configuration for the counter and the associated widgets
		 */
		public void initializeTab(ILaunchConfiguration config) {
			//make all controls inactive, since the 'default event' checkbox
			// is checked by default
			setEnabledState(false);
			
			if (config != null) {
				_counter.loadConfiguration(config);
			}

			boolean enabled = _counter.getEnabled();
			_enabledCheck.setSelection(enabled);

			if (_counter.getEvent() == null) {
				// Default to first in list
				_counter.setEvent(_counter.getValidEvents()[0]);
			}

			//load default states
			_profileKernelCheck.setSelection(_counter.getProfileKernel());
			_profileUserCheck.setSelection(_counter.getProfileUser());
			_countText.setText(Integer.toString(_counter.getCount()));
			_eventDescText.setText(_counter.getEvent().getTextDescription());
			_unitMaskViewer.displayEvent(_counter.getEvent());
			_eventList.setSelection(new StructuredSelection(_counter.getEvent()));
		}
		
		/**
		 * Applies the tab's current state to the launch configuration.
		 * @param config launch config to apply to
		 */
		public void performApply(ILaunchConfigurationWorkingCopy config) {
			_counter.saveConfiguration(config);
			try {
				config.doSave();
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}

		/**
		 * Enables/disables the widgets in this tab.
		 * @param state true to enable to the counter's state, false to disable all
		 */
		public void setEnabledState(boolean state) {
			_enabledCheck.setEnabled(state);
			
			if (state) {
				_setEnabledState(_counter.getEnabled());
			} else {
				_setEnabledState(false);
			}
		}
		
		/**
		 * Method split from setEnabledState to avoid code duplication.
		 * Not meant to be called directly.
		 * @param state true to enable all widgets, false to disable all widgets
		 */
		private void _setEnabledState(boolean state) {
			_profileKernelCheck.setEnabled(state);
			_profileUserCheck.setEnabled(state);
			_countText.setEnabled(state);
			_eventDescText.setEnabled(state);
			_unitMaskViewer.setEnabled(state);
			_eventList.getList().setEnabled(state);
		}

		/**
		 * Handling method for the event list. Gets the selection from the listviewer 
		 * and updates the UnitMask and event description text box. 
		 */
		private void _handleEventListSelectionChange() {
			int index = _eventList.getList().getSelectionIndex();
			if (index != -1){
				OpEvent event = (OpEvent) _eventList.getElementAt(index);
				_counter.setEvent(event);
				_eventDescText.setText(event.getTextDescription());
				_unitMaskViewer.displayEvent(event);
				
				// Check the min count to update the error message (events can have
				// different minimum reset counts)
				int min = _counter.getEvent().getMinCount();
				if (_counter.getCount() < min) {
					setErrorMessage(getMinCountErrorMessage(min));
				}
			}

			updateLaunchConfigurationDialog();
		}
		
		/**
		 * Handles the toggling of the "profile user" button.
		 */
		private void _handleProfileUserToggle() {
			_counter.setProfileUser(_profileUserCheck.getSelection());
			updateLaunchConfigurationDialog();
		}

		/**
		 * Handles the toggling of the "profile kernel" button.
		 */
		private void _handleProfileKernelToggle() {
			_counter.setProfileKernel(_profileKernelCheck.getSelection());
			updateLaunchConfigurationDialog();
		}
		
		/**
		 * Handles text modify events in the count text widget.
		 */
		private void _handleCountTextModify() {
			String errorMessage = null;
			try {

				// This seems counter-intuitive, but we must save the count
				// so that isValid knows this launch config is invalid
				int count = Integer.parseInt(_countText.getText());
				_counter.setCount(count);

				// Check minimum count
				int min = _counter.getEvent().getMinCount();
				if (count < min) {
					errorMessage = getMinCountErrorMessage(min);
				}
			} catch (NumberFormatException e) {
				errorMessage = OprofileLaunchMessages.getString("tab.event.counterSettings.count.invalid"); //$NON-NLS-1$
				_counter.setCount(OprofileDaemonEvent.COUNT_INVALID);
			} finally {
				setErrorMessage(errorMessage);
				updateLaunchConfigurationDialog();
			}
		}
		
		/**
		 * Returns a string with the minimum allowed count, suitable foruse with setErrorMessage().
		 * @param min minimum count
		 * @return a String containing the error message
		 */
		private String getMinCountErrorMessage(int min) {
			String msg = OprofileLaunchMessages.getString("tab.event.counterSettings.count.too-small"); //$NON-NLS-1$
			Object[] args = new Object[] { Integer.valueOf(min) };
			return MessageFormat.format(msg, args);
		}
		
		/**
		 * Changes parameters for the top scrolled composite which makes the scroll bars
		 * appear when content overflows the visible area. Called by the UnitMaskViewer 
		 * whenever a new set of unit mask buttons are created, since the number of them is
		 * variable and there is no guarantee as to the default size of the launch configuration
		 * dialog in general.
		 */
		private void resizeScrollContainer() {
			_scrolledTop.setMinSize(_tabTopContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		}
		
		
		/**
		 * This class displays event unit masks via check boxes and appropriate labels.
		 */
		protected class UnitMaskViewer {
			private Label _unitMaskLabel;
			private Composite _top;
			private Composite _maskListComp;
			private Button[] _unitMaskButtons;

			/**
			 * Constructor, creates the widget.
			 * @param parent composite the widget will be created in
			 */
			public UnitMaskViewer(Composite parent) {
				//"Unit Mask:" label
				_unitMaskLabel = new Label(parent, SWT.NONE);
				_unitMaskLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
				_unitMaskLabel.setText(OprofileLaunchMessages.getString("unitmaskViewer.label.text")); //$NON-NLS-1$
				_unitMaskLabel.setVisible(true);

				//composite to contain the button widgets
				Composite top = new Composite(parent, SWT.NONE);
				top.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				GridLayout layout = new GridLayout();
				layout.marginHeight = 0;
				layout.marginWidth = 0;
				top.setLayout(layout);
				_top = top;
				
				_maskListComp = null;
				_unitMaskButtons = null;
			}

			/**
			 * Handles button toggles; updates the counter's unit mask to the appropriate value.
			 * @param maskButton the button object
			 * @param index the button's mask index (used in OpUnitMask for a proper mask value) 
			 */
			private void _handleToggle(Button maskButton, int index) {
				OpUnitMask mask = _counter.getUnitMask();
				if (mask != null) {
					if (maskButton.getSelection()) {
						mask.setMaskFromIndex(index);
					} else {
						mask.unSetMaskFromIndex(index);
					}
				}

				//update the parent tab
				updateLaunchConfigurationDialog();
			}

			/**
			 * Disposes of the old unit mask check list and creates a new one with 
			 *   the appropriate default value. 
			 * @param oe the event
			 */
			public void displayEvent(OpEvent oe) {
				OpUnitMask mask = oe.getUnitMask();
				int totalMasks = mask.getNumMasks();
				
				if (_maskListComp != null) {
					_maskListComp.dispose();
				}
				
				Composite newMaskComp = new Composite(_top, SWT.NONE);
				newMaskComp.setLayout(new GridLayout());
				newMaskComp.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));
				_maskListComp = newMaskComp;

				//creates these buttons with the default masks
				mask.setDefaultMaskValue();
				
				ArrayList<Button> maskButtons = new ArrayList<Button>();
				
				for (int i = 0; i < totalMasks; i++) {
					Button maskButton;

					if (mask.getType() == OpUnitMask.INVALID) {
						//big problem, most likely parsing went awry or opxml output mangled
						OprofileCorePlugin.showErrorDialog("opxmlParse", null); //$NON-NLS-1$
						return;
					} else if (mask.getType() == OpUnitMask.MANDATORY) {
						maskButton = new Button(newMaskComp, SWT.RADIO);
						maskButton.setEnabled(false);
						maskButton.setText(mask.getText(i));
						maskButton.setSelection(true);
					} else {
						int buttonType;
						final int maskButtonIndex = i;
						boolean selected = mask.isMaskSetFromIndex(maskButtonIndex);

						if (mask.getType() == OpUnitMask.EXCLUSIVE) {
							buttonType = SWT.RADIO;
						} else {	//mask type is OpUnitMask.BITMASK
							buttonType = SWT.CHECK;
						}
						
						maskButton = new Button(newMaskComp, buttonType);
						maskButton.setEnabled(true);
						maskButton.setText(mask.getText(i));
						maskButton.setSelection(selected);
						maskButton.addSelectionListener(new SelectionAdapter() {
							public void widgetSelected(SelectionEvent se) {
								_handleToggle((Button)se.getSource(), maskButtonIndex);
							}
						});
						
						maskButtons.add(maskButton);
					}
				}
				
				_unitMaskButtons = new Button[maskButtons.size()];
				maskButtons.toArray(_unitMaskButtons);
				
				resizeScrollContainer();
			}

			/**
			 * Enables and disables the viewer for UI input
			 * @param enabled whether this viewer should be enabled
			 */
			public void setEnabled(boolean enabled) {
				if (_unitMaskButtons != null) {
					for (Button b : _unitMaskButtons) {
						b.setEnabled(enabled);
					}
				}
			}
		}
	}
}
