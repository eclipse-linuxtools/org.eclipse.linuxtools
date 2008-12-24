/*******************************************************************************
 * Copyright (c) 2004,2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - much of the base of code from 
 *    		OprofileEventConfigTab, initial UnitMaskViewer code
 *    Kent Sebastian <ksebasti@redhat.com> - modified methods for new design
 *******************************************************************************/
package org.eclipse.linuxtools.oprofile.launch.configuration;

import java.text.MessageFormat;
import java.util.ArrayList;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.oprofile.core.daemon.OpEvent;
import org.eclipse.linuxtools.oprofile.core.daemon.OpUnitMask;
import org.eclipse.linuxtools.oprofile.core.daemon.OprofileDaemonEvent;
import org.eclipse.linuxtools.oprofile.launch.OprofileLaunchMessages;
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
import org.eclipse.swt.widgets.Text;

/**
 * A sub-tab of the OprofileEventConfigTab launch configuration tab.
 * Essentially, it is a frontend to an OprofileCounter.
 */
public class CounterSubTab {

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
	private OprofileEventConfigTab _parentTab;

	public CounterSubTab(Composite parent, OprofileCounter counter, OprofileEventConfigTab parentTab) {
		_parentTab = parentTab;
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
		
		_parentTab.__createVerticalSpacer(tabTopContainer, 2);
		
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
		
		//required so the scroll bars will appear after a default height 
		scrolledContainer.setMinSize(tabTopContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}
	
	//creates the "Enabled" checkbox, and the event description text
	private void createTopCell(Composite parent) {
		//checkbox
		_enabledCheck = new Button(parent, SWT.CHECK);
		_enabledCheck.setText(OprofileLaunchMessages.getString("tab.event.counterSettings.enabled.button.text")); //$NON-NLS-1$
		_enabledCheck.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		_enabledCheck.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent se) {
				_counter.setEnabled(_enabledCheck.getSelection());
				_setEnabledState(_counter.getEnabled());
				_parentTab.__updateLaunchConfigurationDialog();
			}
		});
		_enabledCheck.setEnabled(false);
		
		//label for textbox
		_eventDescLabel = new Label(parent, SWT.NONE);
		_eventDescLabel.setText(OprofileLaunchMessages.getString("tab.event.eventDescription.label.text"));
		_eventDescLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		//textbox
		_eventDescText = new Text(parent, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
		_eventDescText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	}

	//creates the event list
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
	
	//creates the 2 profile space checkboxes, event count, and unit mask widget
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
	
	//initializes the tab on first creation
	public void initializeTab(ILaunchConfiguration config) {
		//make all controls inactive
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

		_profileKernelCheck.setSelection(_counter.getProfileKernel());
		_profileUserCheck.setSelection(_counter.getProfileUser());
		_countText.setText(Integer.toString(_counter.getCount()));
		_eventDescText.setText(_counter.getEvent().getTextDescription());
		_unitMaskViewer.displayEvent(_counter.getEvent());
		_eventList.setSelection(new StructuredSelection(_counter.getEvent()));
	}
	
	//applies the tab's current state to the launch configuration
	public void performApply(ILaunchConfigurationWorkingCopy config) {
		_counter.saveConfiguration(config);
	}

	//used in OprofileEventConfigTab
	//if enabling, set to counter's state
	//if disabling, disable everything
	public void setEnabledState(boolean state) {
		_enabledCheck.setEnabled(state);
		
		if (state) {
			_setEnabledState(_counter.getEnabled());
		} else {
			_setEnabledState(false);
		}
	}
	
	//this is split from setEnabledState since the handlecheck code
	// for _enabledCheck only needs to change these elements, hence
	// reducing code duplication
	private void _setEnabledState(boolean state) {
		_profileKernelCheck.setEnabled(state);
		_profileUserCheck.setEnabled(state);
		_countText.setEnabled(state);
		_eventDescText.setEnabled(state);
		_unitMaskViewer.setEnabled(state);
		_eventList.getList().setEnabled(state);
	}

	//Gets the selection from the event listviewer and updates the 
	// other event display 
	// (Primarily updates the UnitMaskViewer and the event desc textbox)
	private void _handleEventListSelectionChange() {
		int index = _eventList.getList().getSelectionIndex();
		OpEvent event = (OpEvent) _eventList.getElementAt(index);
		_counter.setEvent(event);
		_eventDescText.setText(event.getTextDescription());
		_unitMaskViewer.displayEvent(event);

		// Check the min count to update the error message (events can have
		// different minimum reset counts)
		int min = _counter.getEvent().getMinCount();
		if (_counter.getCount() < min) {
			_parentTab.__setErrorMessage(getMinCountErrorMessage(min));
		}

		_parentTab.__updateLaunchConfigurationDialog();
	}
	
	// handles the toggling of the "profile user" button
	private void _handleProfileUserToggle() {
		_counter.setProfileUser(_profileUserCheck.getSelection());
		_parentTab.__updateLaunchConfigurationDialog();
	}

	// handles the toggling of the "profile kernel" button
	private void _handleProfileKernelToggle() {
		_counter.setProfileKernel(_profileKernelCheck.getSelection());
		_parentTab.__updateLaunchConfigurationDialog();
	}
	
	// handles text modify events in the count text widget
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
			_parentTab.__setErrorMessage(errorMessage);
			_parentTab.__updateLaunchConfigurationDialog();
		}
	}
	
	// Returns a string with the minimum allowed count, suitable for
	// use with setErrorMessage().
	private String getMinCountErrorMessage(int min) {
		String msg = OprofileLaunchMessages.getString("tab.event.counterSettings.count.too-small"); //$NON-NLS-1$
		Object[] args = new Object[] { new Integer(min) };
		return MessageFormat.format(msg, args);
	}
	
	public OprofileCounter getCounter() {
		return _counter;
	}

	
	
	/**
	 * This class displays unit masks in the launcher's event configuration tab.
	 * 
	 * Contributors:
	 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
	 *    Kent Sebastian <ksebasti@redhat.com>
	 */
	class UnitMaskViewer {
		private Label _unitMaskLabel;
		private Composite _top;
		private Composite _maskListComp;
		private Button[] _unitMaskButtons;

		/**
		 * Constructor for UnitMaskViewer
		 * 
		 * @param parent
		 *            the parent composite
		 */
		public UnitMaskViewer(Composite parent) {
			_unitMaskLabel = new Label(parent, SWT.NONE);
			_unitMaskLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			_unitMaskLabel.setText(OprofileLaunchMessages.getString("unitmaskViewer.label.text")); //$NON-NLS-1$
			_unitMaskLabel.setVisible(true);

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

		//handles button toggles; updates the counter's unit mask to
		// the appropriate value
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
			_parentTab.__updateLaunchConfigurationDialog();
		}

		/**
		 * Disposes of the old unit mask check list and creates a new one with 
		 *   the appropriate default value. 
		 * @param oe the event
		 */
		public void displayEvent(OpEvent oe) {
			OpUnitMask mask = oe.getUnitMask();
			int totalMasks = mask.numMasks();
			
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
				
				if (mask.getType() == OpUnitMask.MANDATORY) {
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
		}

		/**
		 * Enables and disables the viewer for UI input
		 * 
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
