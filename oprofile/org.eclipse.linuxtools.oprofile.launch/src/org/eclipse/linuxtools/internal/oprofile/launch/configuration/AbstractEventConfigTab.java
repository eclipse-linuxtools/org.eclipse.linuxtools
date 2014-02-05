/*******************************************************************************
 * Copyright (c) 2012 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.oprofile.launch.configuration;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.linuxtools.internal.oprofile.core.Oprofile.OprofileProject;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

public abstract class AbstractEventConfigTab extends
AbstractLaunchConfigurationTab {
	protected Button defaultEventCheck;
	protected OprofileCounter[] counters = null;
	protected CounterSubTab[] counterSubTabs;
	private Composite top;

	/**
	 * Essentially the constructor for this tab; creates the 'default event'
	 * checkbox and an appropriate number of counter tabs.
	 * @param parent the parent composite
	 */
	@Override
	public void createControl(Composite parent) {
		Composite top = new Composite(parent, SWT.NONE);
		setControl(top);
		top.setLayout(new GridLayout());
		this.top = top;
	}
	/**
	 * @since 1.1
	 * @param top
	 */
	private void createCounterTabs(Composite top){
		//tabs for each of the counters
		counters = getOprofileCounters(null);
		TabItem[] counterTabs = new TabItem[counters.length];

		// create only one counter for operf/opcontrol
		counterSubTabs = new CounterSubTab[1];

		TabFolder tabFolder = new TabFolder(top, SWT.NONE);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));


		// As per Roland suggestion if we decide to list all the selected events
		// in a separate list viewer, then it makes sense to always show just one tab.
		// This approach would make operf/opcontrol event selection more similar.
		for (int i = 0; i < counters.length; i++) {
			Composite c = new Composite(tabFolder, SWT.NONE);
			CounterSubTab currentTab = new CounterSubTab(c, counters[i]);
			counterSubTabs[i] = currentTab;

			counterTabs[i] = new TabItem(tabFolder, SWT.NONE);
			counterTabs[i].setControl(c);
			counterTabs[i].setText(OprofileLaunchMessages.getString("tab.event.counterTab.counterText")); //$NON-NLS-1$
			// just one tab for operf/opcontrol
			break;
		}

		getTabFolderComposite();
	}

	/**
	 * @since 1.1
	 */
	private Composite getTabFolderComposite(){
		// check for length and first tab being null to prevent AIOBE
		if(counterSubTabs.length == 0 ||counterSubTabs[0] == null){
			return null;
		} else {
			Composite c = counterSubTabs[0].getTabTopContainer();
			while(c != null && !(c instanceof TabFolder)){
				c = c.getParent();
			}
			return c.getParent();
		}
	}

	@Override
	public void initializeFrom(ILaunchConfiguration config) {

		IProject previousProject = getOprofileProject();
		IProject project = getProject(config);
		setOprofileProject(project);

		updateOprofileInfo();

		String previousHost = null;
		if(previousProject != null){
			if(previousProject.getLocationURI() != null){
				previousHost = previousProject.getLocationURI().getHost();
			}
		}

		String host;
		if (project != null) {
			host = project.getLocationURI().getHost();
		} else {
			host = null;
		}

		// Create the counter tabs if host has changed or if they haven't been created yet
		// Check that initialization is not done for current project.
		// Any calculation based on project doesn't work as the very first time for local project they are both null.
		if(previousProject == null || previousHost != host || host == null || counters == null){
			Control[] children = top.getChildren();

			for (Control control : children) {
				control.dispose();
			}

			if (getOprofileTimerMode()) {
				Label timerModeLabel = new Label(top, SWT.LEFT);
				timerModeLabel.setText(OprofileLaunchMessages.getString("tab.event.timermode.no.options")); //$NON-NLS-1$
			} else {
				createVerticalSpacer(top, 1);

				//default event checkbox
				defaultEventCheck = new Button(top, SWT.CHECK);
				defaultEventCheck.setText(OprofileLaunchMessages.getString("tab.event.defaultevent.button.text")); //$NON-NLS-1$
				defaultEventCheck.setLayoutData(new GridData());
				defaultEventCheck.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent se) {
						handleEnabledToggle();
					}
				});
				createVerticalSpacer(top, 1);
				createCounterTabs(top);
			}

		}

		if(!getOprofileTimerMode()){
			for (int i = 0; i < counters.length; i++) {
				counters[i].loadConfiguration(config);
			}

			for (CounterSubTab tab : counterSubTabs) {
				tab.initializeTab(config);
				tab.createEventsFilter();
			}
			try{
				boolean enabledState = config.getAttribute(OprofileLaunchPlugin.ATTR_USE_DEFAULT_EVENT, true);
				defaultEventCheck.setSelection(enabledState);
				setEnabledState(!enabledState);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean isValid(ILaunchConfiguration config) {
		IProject project = getProject(config);
		setOprofileProject(project);

		if (getOprofileTimerMode() || counterSubTabs == null) {
			return true;		//no options to check for validity
		} else {
			return validateEvents(config);
		}
	}

	/**
	 * Validate events specified in the given configuration.
	 * @param config
	 * @return
	 */
	public boolean validateEvents(ILaunchConfiguration config) {
		int numEnabledEvents = 0;
		boolean valid = true;

		try {
			if (config.getAttribute(OprofileLaunchPlugin.ATTR_USE_DEFAULT_EVENT, false)) {
				numEnabledEvents = 1;
			} else {
				//This seems like an odd way to validate, but since most of the validation
				// is done with the OprofileDaemonEvent that the counter wraps, this
				// is the easiest way.
				OprofileCounter[] counters = new OprofileCounter[getNumberOfOprofileCounters()];
				for (int i = 0; i < counters.length; i++) {
					counters[i] = getOprofileCounter(i);
					counters[i].loadConfiguration(config);

					for (CounterSubTab counterSubTab : counterSubTabs){
						int nr = counterSubTab.counter.getNumber();
						if(counterSubTab.enabledCheck.getSelection() && config.getAttribute(OprofileLaunchPlugin.ATTR_NUMBER_OF_EVENTS(nr), 0) == 0){
							valid = false;
						}
						// if target list is empty valid is false
						// target event list item count
						int count = counterSubTab.selectedEventList.getList().getItemCount();
						if(count == 0)
						{
							valid = false;
						}
						// number of event is  allowed in opcontrol binary is equal to number of h/w counter
						if(OprofileProject.OPCONTROL_BINARY.equals(OprofileProject.getProfilingBinary()) && count > counters.length)
						{
							Object[] args = new Object[] { counters.length };
							setErrorMessage(MessageFormat.format(OprofileLaunchMessages.getString("tab.event.opcontrol.validation.msg"),args)); //$NON-NLS-1$
							valid = false;
						}
					}

					if (counters[i].getEnabled()) {
						++numEnabledEvents;

						for (OpEvent event : counters[i].getEvents()) {
							if (event == null) {
								valid = false;
								break;
							}

							// First check min count
							int min = event.getMinCount();
							if (counters[i].getCount() < min) {
								Object[] args = new Object[] { min };
								setErrorMessage(MessageFormat
										.format(OprofileLaunchMessages
												.getString("tab.event.counterSettings.count.too-small"), //$NON-NLS-1$
												args));
								valid = false;
								break;
							}

							// Next ask oprofile if it is valid
							if (!checkEventSetupValidity(
									counters[i].getNumber(), event.getText(), event.getUnitMask().getMaskValue())) {
								Object[] args = new Object[] { event.getText() };
								setErrorMessage(MessageFormat.format(OprofileLaunchMessages.getString("tab.event.validation.msg"), args)); //$NON-NLS-1$
								valid = false;
								break;
							}
						}
					}
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}

		return (numEnabledEvents > 0 && valid);
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy config) {
		if (getOprofileTimerMode() || counterSubTabs == null) {
			config.setAttribute(OprofileLaunchPlugin.ATTR_USE_DEFAULT_EVENT, true);
		} else {
			config.setAttribute(OprofileLaunchPlugin.ATTR_USE_DEFAULT_EVENT, defaultEventCheck.getSelection());
			for (CounterSubTab cst : counterSubTabs) {
				cst.performApply(config);
			}
		}
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		boolean useDefault = true;
		IProject project = getProject(config);
		setOprofileProject(project);

		counters = getOprofileCounters(config);

		// When instantiated, the OprofileCounter will set defaults.
		for (int i = 0; i < counters.length; i++) {
			counters[i].saveConfiguration(config);
			if (counters[i].getEnabled()) {
				useDefault = false;
			}
		}

		config.setAttribute(OprofileLaunchPlugin.ATTR_USE_DEFAULT_EVENT, useDefault);
	}

	@Override
	public String getName() {
		return OprofileLaunchMessages.getString("tab.event.name"); //$NON-NLS-1$
	}

	@Override
	public Image getImage() {
		return OprofileLaunchPlugin.getImageDescriptor(OprofileLaunchPlugin.ICON_EVENT_TAB).createImage();
	}

	/**
	 * Handles the toggling of the default event check box. Not meant to be called
	 * directly.
	 */
	private void handleEnabledToggle() {
		setEnabledState(!defaultEventCheck.getSelection());
		updateLaunchConfigurationDialog();
	}

	/**
	 * Sets the state of the child counter tabs' widgets.
	 * @param state true for enabled, false for disabled
	 */
	private void setEnabledState(boolean state) {
		for (CounterSubTab cst : counterSubTabs) {
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
	protected abstract boolean checkEventSetupValidity(int counter, String name, int maskValue);

	/**
	 *
	 * @param config
	 * @return
	 * @since 1.1
	 */
	protected IProject getProject(ILaunchConfiguration config){
		String name = null;
		try {
			name = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, ""); //$NON-NLS-1$
		} catch (CoreException e) {
			return null;
		}
		if (name.isEmpty()) {
			return null;
		}

		return ResourcesPlugin.getWorkspace().getRoot().getProject(name);
	}

	/**
	 * Returns counter with corresponding to counter number.
	 * @param i the counter number
	 */
	public abstract OprofileCounter getOprofileCounter(int i);

	/**
	 * Returns counters in the given configuration.
	 * @param config the launch configuration
	 */
	protected abstract OprofileCounter[] getOprofileCounters(ILaunchConfiguration config);

	/**
	 * Returns the number of hardware counters the cpu has
	 * @return int number of counters
	 */
	protected abstract int getNumberOfOprofileCounters();

	/**
	 * Returns whether or not oprofile is operating in timer mode.
	 * @return true if oprofile is in timer mode, false otherwise
	 */
	protected abstract boolean getOprofileTimerMode();

	/**
	 * Returns current project to profile by Oprofile.
	 */
	protected abstract IProject getOprofileProject();

	/**
	 * Set project to profile by Oprofile.
	 * @param project the project to profile
	 */
	protected abstract void setOprofileProject(IProject project);

	/**
	 * Update generic Oprofile information.
	 */
	protected abstract void updateOprofileInfo();

	/**
	 * A sub-tab of the OprofileEventConfigTab launch configuration tab.
	 * Essentially, it is a frontend to an OprofileCounter. This is an
	 * inner class because it requires methods from the parent tab (such as
	 * updateLaunchConfigurationDialog() when a widget changes state).
	 */
	protected class CounterSubTab {

		private Button profileKernelCheck;
		private Button profileUserCheck;
		private Label countTextLabel;
		private Text countText;
		private Label eventDescLabel;
		private Text eventDescText;
		private UnitMaskViewer unitMaskViewer;
		private Text eventFilterText;
		private OprofileCounter counter;

		private ScrolledComposite scrolledTop;
		protected Composite tabTopContainer;
		protected Button enabledCheck;
		protected ListViewer eventList;

		protected Button add;
		protected Button addAll;
		protected Button remove;
		protected Button removeAll;
		protected Button customizeBtn;
		protected ListViewer selectedEventList;
		protected ScrolledComposite unitmaskScrollComposite;
		protected Composite unitMaskSubComposite ;
		private final int ADD = 1;
		private final int ADD_ALL = 2;
		private final int REMOVE = 3;
		private final int REMOVE_ALL = 4;
		private final int CUSTOMIZE = 5;
		private List<OpEvent> sourceList = new ArrayList<>(0);
		private List<OpEvent> targetList = new ArrayList<>(0);


		public Composite getTabTopContainer() {
			return tabTopContainer;
		}

		public void setTabTopContainer(Composite tabTopContainer) {
			this.tabTopContainer = tabTopContainer;
		}

		/**
		 * Constructor for a subtab. Creates the layout and widgets for its content.
		 * @param parent composite the widgets will be created in
		 * @param counter the associated OprofileCounter object
		 */
		public CounterSubTab(Composite parent, OprofileCounter counter) {
			this.counter = counter;

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
			layout.numColumns = 3;
			eventListComp.setLayout(layout);
			eventListComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			//layoutdata is set later

			createLeftCell(eventListComp);


			scrolledTop = scrolledContainer;
			this.tabTopContainer = tabTopContainer;
			resizeScrollContainer();
		}

		/**
		 * Creates the "Enabled" checkbox, and the event description text.
		 * @param parent composite these widgets will be created in
		 */
		private void createTopCell(Composite parent) {
			//checkbox
			enabledCheck = new Button(parent, SWT.CHECK);
			enabledCheck.setText(OprofileLaunchMessages.getString("tab.event.counterSettings.enabled.button.text")); //$NON-NLS-1$
			enabledCheck.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
			enabledCheck.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent se) {
					counter.setEnabled(enabledCheck.getSelection());
					internalSetEnabledState(counter.getEnabled());
					updateLaunchConfigurationDialog();
				}
			});
			enabledCheck.setEnabled(false);

			//label for textbox
			eventDescLabel = new Label(parent, SWT.NONE);
			eventDescLabel.setText(OprofileLaunchMessages.getString("tab.event.eventDescription.label.text")); //$NON-NLS-1$
			eventDescLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

			//textbox
			eventDescText = new Text(parent, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
			eventDescText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		}

		/**
		 * Creates the event list widget.
		 * @param parent composite these widgets will be created in
		 */
		private void createLeftCell(Composite parent) {
			// Text box used to filter the event list
			eventFilterText = new Text(parent, SWT.BORDER | SWT.SINGLE | SWT.ICON_CANCEL | SWT.SEARCH);
			eventFilterText.setMessage(OprofileLaunchMessages.getString("tab.event.eventfilter.message")); //$NON-NLS-1$
			GridData eventFilterLayout = new GridData();
			eventFilterLayout.horizontalAlignment = SWT.FILL;
			eventFilterLayout.grabExcessHorizontalSpace = true;
			eventFilterText.setLayoutData(eventFilterLayout);
			eventFilterText.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					eventList.refresh(false);
				}
			});

			// profile user binary and profile kernel
			createRightCell(parent);

			int options =  SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER;
			if (OprofileProject.getProfilingBinary().equals(OprofileProject.OPERF_BINARY)) {
				options |= SWT.MULTI;
			} else {
				options |= SWT.SINGLE;
			}
			eventList = new ListViewer(parent, options);
			GridData gdata = new GridData(SWT.FILL, SWT.FILL, true, true);
			gdata.verticalSpan = 5;
			eventList.getList().setLayoutData(gdata);

			eventList.setLabelProvider(new ILabelProvider(){
				@Override
				public String getText(Object element) {
					OpEvent e = (OpEvent) element;
					return e.getText();
				}
				@Override
				public Image getImage(Object element) { return null; }
				@Override
				public void addListener(ILabelProviderListener listener) { }
				@Override
				public void dispose() { }
				@Override
				public boolean isLabelProperty(Object element, String property) { return false; }
				@Override
				public void removeListener(ILabelProviderListener listener) { }
			});

			eventList.setContentProvider(new IStructuredContentProvider() {
				@Override
				public Object[] getElements(Object inputElement) {
					List<OpEvent> list = (List<OpEvent>)inputElement;
					return list.toArray();
				}
				@Override
				public void dispose() { }
				@Override
				public void inputChanged(Viewer arg0, Object arg1, Object arg2) { }
			});

			// sorter
			ListviewerSorter sorter = new ListviewerSorter();
			eventList.setSorter(sorter);

			//adds the events to the list from the counter
			sourceList.addAll(Arrays.asList(counter.getValidEvents()));
			eventList.setInput(sourceList);

			eventList.addSelectionChangedListener(new ISelectionChangedListener() {
				@Override
				public void selectionChanged(SelectionChangedEvent sce) {
					handleEventListSelectionChange();
				}
			});

			HandleButtonClick listener = new HandleButtonClick();
			add = new Button(parent, SWT.PUSH);
			add.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			add.setText(OprofileLaunchMessages.getString("tab.event.addevent.button.text")); //$NON-NLS-1$
			add.setData(ADD);
			add.addListener(SWT.Selection, listener);

			selectedEventList = new ListViewer(parent, options);
			selectedEventList.getList().setLayoutData(gdata);


			selectedEventList.setLabelProvider(new ILabelProvider(){
				@Override
				public String getText(Object element) {
					OpEvent e = (OpEvent) element;
					return e.getText();
				}
				@Override
				public Image getImage(Object element) { return null; }
				@Override
				public void addListener(ILabelProviderListener listener) { }
				@Override
				public void dispose() { }
				@Override
				public boolean isLabelProperty(Object element, String property) { return false; }
				@Override
				public void removeListener(ILabelProviderListener listener) { }
			});

			selectedEventList.setContentProvider(new IStructuredContentProvider() {
				@Override
				public Object[] getElements(Object inputElement) {
					List<OpEvent> list = (List<OpEvent>)inputElement;
					return list.toArray();
				}
				@Override
				public void dispose() { }
				@Override
				public void inputChanged(Viewer arg0, Object arg1, Object arg2) { }
			});

			// sorter
			 sorter = new ListviewerSorter();
			 selectedEventList.setSorter(sorter);

			//adds the events to the list from the counter
			if(counter.getEvents().length != 0 && null != counter.getEvents()[0])
			{
			targetList.addAll(Arrays.asList(counter.getEvents()));
			}
			selectedEventList.setInput(targetList);

			selectedEventList.addSelectionChangedListener(new ISelectionChangedListener() {
				@Override
				public void selectionChanged(SelectionChangedEvent sce) {
					handleListSelection(selectedEventList);
					eventList.getList().deselectAll();
					updateLaunchConfigurationDialog();
				}
			});





			addAll = new Button(parent, SWT.PUSH);
			addAll.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			addAll.setText(OprofileLaunchMessages.getString("tab.event.addallevent.button.text")); //$NON-NLS-1$
			addAll.setData(ADD_ALL);
			addAll.addListener(SWT.Selection, listener);


			remove = new Button(parent, SWT.PUSH);
			remove.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			remove.setText(OprofileLaunchMessages.getString("tab.event.removeevent.button.text")); //$NON-NLS-1$
			remove.setData(REMOVE);
			remove.addListener(SWT.Selection, listener);


			removeAll = new Button(parent, SWT.PUSH);
			removeAll.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			removeAll.setText(OprofileLaunchMessages.getString("tab.event.removeallevent.button.text")); //$NON-NLS-1$
			removeAll.setData(REMOVE_ALL);
			removeAll.addListener(SWT.Selection, listener);

			customizeBtn = new Button(parent, SWT.PUSH);
			customizeBtn.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			customizeBtn.setText(OprofileLaunchMessages.getString("tab.event.customizeevent.button.text")); //$NON-NLS-1$
			customizeBtn.addListener(SWT.Selection, listener);
			customizeBtn.setData(CUSTOMIZE);


		}

		/**
		 * Creates the 2 profile space checkboxes, event count and unit mask widget.
		 * @param parent composite these widgets will be created in
		 */
		private void createRightCell(Composite parent) {
			//profile kernel checkbox
			profileKernelCheck = new Button(parent, SWT.CHECK);
			profileKernelCheck.setText(OprofileLaunchMessages.getString("tab.event.counterSettings.profileKernel.check.text")); //$NON-NLS-1$
			profileKernelCheck.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent se) {
					handleProfileKernelToggle();
				}
			});

			//profile user checkbox -- should this ever be disabled?
			profileUserCheck = new Button(parent, SWT.CHECK);
			profileUserCheck.setText(OprofileLaunchMessages.getString("tab.event.counterSettings.profileUser.check.text")); //$NON-NLS-1$
			profileUserCheck.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent se) {
					handleProfileUserToggle();
				}
			});

		}

		/**
		 * Creates a text filter for the events list widget
		 */
		private void createEventsFilter(){
			// Event Filter
			ViewerFilter eventFilter = new ViewerFilter() {

				@Override
				public Object[] filter(Viewer viewer, Object parent, Object[] elements) {
					Object[] filteredElements = super.filter(viewer,parent,elements);
					handleEventListSelectionChange();
					return filteredElements;
				}

				@Override
				public boolean select(Viewer viewer, Object parentElement, Object element) {
					String[] filterTerms = eventFilterText.getText().trim().toLowerCase().split(" "); //$NON-NLS-1$
					String eventName = ((OpEvent)element).getText().toLowerCase();
					String eventDescription = ((OpEvent)element).getTextDescription().toLowerCase();

					boolean contains = true;

					for (String filterTerm : filterTerms) {
						if(contains){
							contains = eventName.contains(filterTerm) || eventDescription.contains(filterTerm);
						}
					}
					return contains;
				}
			};
			if(eventList != null){
				eventList.addFilter(eventFilter);
			}
		}

		/**
		 * Initializes the tab on first creation.
		 * @param config default configuration for the counter and the associated widgets
		 */
		public void initializeTab(ILaunchConfiguration config) {
			//make all controls inactive, since the 'default event' checkbox
			// is checked by default
			try {
				defaultEventCheck.setSelection(config.getAttribute(OprofileLaunchPlugin.ATTR_USE_DEFAULT_EVENT, true));
			} catch (CoreException e) {
				e.printStackTrace();
			}
			setEnabledState(false);

			if (config != null) {
				counter.loadConfiguration(config);
			}

			boolean enabled = counter.getEnabled();
			enabledCheck.setSelection(enabled);

			if (counter.getEvents().length == 0 || counter.getEvents()[0] == null) {
				// Default to first in list
				counter.setEvents(new OpEvent [] {counter.getValidEvents()[0]});
			}

			//load default states
			profileKernelCheck.setSelection(counter.getProfileKernel());
			profileUserCheck.setSelection(counter.getProfileUser());

			eventDescText.setText(counter.getEvents()[0].getTextDescription());


			// add opevent to target event list
			ArrayList<OpEvent> tmp = new ArrayList<>(Arrays.asList(counter.getEvents()));
			targetList.addAll(tmp);
			selectedEventList.add(tmp.toArray());
			selectedEventList.refresh();
			selectedEventList.setSelection(new StructuredSelection(tmp.toArray()));

			// remove selected opevent from source list

			sourceList.removeAll(tmp);
			eventList.remove(tmp.toArray());
			eventList.refresh();
		}

		/**
		 * Applies the tab's current state to the launch configuration.
		 * @param config launch config to apply to
		 */
		public void performApply(ILaunchConfigurationWorkingCopy config) {
			counter.saveConfiguration(config);
		}

		/**
		 * Enables/disables the widgets in this tab.
		 * @param state true to enable to the counter's state, false to disable all
		 */
		public void setEnabledState(boolean state) {
			enabledCheck.setEnabled(state);

			if (state) {
				internalSetEnabledState(counter.getEnabled());
			} else {
				internalSetEnabledState(false);
			}
		}

		/**
		 * Method split from setEnabledState to avoid code duplication.
		 * Not meant to be called directly.
		 * @param state true to enable all widgets, false to disable all widgets
		 */
		private void internalSetEnabledState(boolean state) {
			profileKernelCheck.setEnabled(state);
			profileUserCheck.setEnabled(state);
			eventDescText.setEnabled(state);
			eventList.getList().setEnabled(state);
			selectedEventList.getList().setEnabled(state);
			eventFilterText.setEnabled(state);
			add.setEnabled(state);
			addAll.setEnabled(state);
			remove.setEnabled(state);
			removeAll.setEnabled(state);
			customizeBtn.setEnabled(state);


		}

		/**
		 * Handling method for the event list. Gets the selection from the listviewer
		 * and updates the UnitMask and event description text box.
		 */
		private void handleEventListSelectionChange() {
			handleListSelection(eventList);
			int[] indices = eventList.getList().getSelectionIndices();
			if (indices.length != 0) {
				customizeBtn.setEnabled(true);
				// unselected other list element
				// to keep customize button enable
				// for both list selection
				selectedEventList.getList().deselectAll();
			}
			updateLaunchConfigurationDialog();

		}

		/**
		 * Generic method for handling source & target selection list
		 * @param eventList - list to be handled
		 * @since 3.0
		 */
		private void handleListSelection(ListViewer eventList)
		{
			setErrorMessage(null);
			int [] indices = eventList.getList().getSelectionIndices();
			if (indices.length != 0) {
				ArrayList<OpEvent> tmp = new ArrayList<> ();
				for (int index : indices) {
					OpEvent event = (OpEvent) eventList.getElementAt(index);
					tmp.add(event);
					eventDescText.setText(event.getTextDescription());

				}

				// Check the min count to update the error message (events
				// can have
				// different minimum reset counts)
				int min = Integer.MIN_VALUE;
				for (OpEvent ev : tmp) {
					// We want the largest of the min values
					if (ev.getMinCount() > min) {
						min = ev.getMinCount();
					}
				}
				if(counter.getEvents().length == 0 || counter.getEvents()[0] == null)
				{
					counter.setEvents(new OpEvent [] {counter.getValidEvents()[0]});
				}
				if ((counter.getCount() < min)
						&& (!defaultEventCheck.getSelection())) {
					setErrorMessage(getMinCountErrorMessage(min));
				}

				//counter.setEvents(tmp.toArray(new OpEvent[0]));
			} else {
				eventDescText.setText(""); //$NON-NLS-1$

			}



		}

		/**
		 * Handles the toggling of the "profile user" button.
		 */
		private void handleProfileUserToggle() {
			counter.setProfileUser(profileUserCheck.getSelection());
			updateLaunchConfigurationDialog();
		}

		/**
		 * Handles the toggling of the "profile kernel" button.
		 */
		private void handleProfileKernelToggle() {
			counter.setProfileKernel(profileKernelCheck.getSelection());
			updateLaunchConfigurationDialog();
		}

		/**
		 * Handles text modify events in the count text widget.
		 */
		private void handleCountTextModify() {
			String errorMessage = null;
			try {

				// This seems counter-intuitive, but we must save the count
				// so that isValid knows this launch config is invalid
				int count = Integer.parseInt(countText.getText());
				counter.setCount(count);

				// Check minimum count
				int min = Integer.MIN_VALUE;
				for (OpEvent event : counter.getEvents()) {
					// We want the largest of the min values
					if (event != null && event.getMinCount() > min) {
						min = event.getMinCount();
					}
				}
				if ((count < min) && (!defaultEventCheck.getSelection())) {
					errorMessage = getMinCountErrorMessage(min);
				}
			} catch (NumberFormatException e) {
				errorMessage = OprofileLaunchMessages.getString("tab.event.counterSettings.count.invalid"); //$NON-NLS-1$
				counter.setCount(OprofileDaemonEvent.COUNT_INVALID);
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
			scrolledTop.setMinSize(tabTopContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		}


		/**
		 * This class displays event unit masks via check boxes and appropriate labels.
		 */
		protected class UnitMaskViewer {
			private Label unitMaskLabel;
			private Composite top;
			private Composite maskListComp;
			private Button[] unitMaskButtons;

			/**
			 * Constructor, creates the widget.
			 * @param parent composite the widget will be created in
			 */
			public UnitMaskViewer(Composite parent) {
				//"Unit Mask:" label
				unitMaskLabel = new Label(parent, SWT.NONE);
				unitMaskLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
				unitMaskLabel.setText(OprofileLaunchMessages.getString("unitmaskViewer.label.text")); //$NON-NLS-1$
				unitMaskLabel.setVisible(true);

				//composite to contain the button widgets
				Composite top = new Composite(parent, SWT.NONE);
				top.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				GridLayout layout = new GridLayout();
				layout.marginHeight = 0;
				layout.marginWidth = 0;
				top.setLayout(layout);
				this.top = top;

				maskListComp = null;
				unitMaskButtons = null;
			}

			/**
			 * Handles button toggles; updates the counter's unit mask to the appropriate value.
			 * @param maskButton the button object
			 * @param index the button's mask index (used in OpUnitMask for a proper mask value)
			 */
			private void handleToggle(Button maskButton, int index) {
				OpUnitMask mask = counter.getUnitMask();
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
				if (maskListComp != null) {
					maskListComp.dispose();
				}

				if(oe == null){
					return;
				}


				OpUnitMask mask = oe.getUnitMask();
				int totalMasks = mask.getNumMasks();

				Composite newMaskComp = new Composite(top, SWT.NONE);
				newMaskComp.setLayout(new GridLayout());
				newMaskComp.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));
				maskListComp = newMaskComp;

				//creates these buttons with the default masks
				mask.setDefaultMaskValue();

				ArrayList<Button> maskButtons = new ArrayList<>();

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
							@Override
							public void widgetSelected(SelectionEvent se) {
								handleToggle((Button)se.getSource(), maskButtonIndex);
							}
						});

						maskButtons.add(maskButton);
					}
				}

				unitMaskButtons = new Button[maskButtons.size()];
				maskButtons.toArray(unitMaskButtons);


				resizeUnitMaskContainer();
			}

			/**
			 * Enables and disables the viewer for UI input
			 * @param enabled whether this viewer should be enabled
			 */
			public void setEnabled(boolean enabled) {
				if (unitMaskButtons != null) {
					for (Button b : unitMaskButtons) {
						if (!b.isDisposed()) {
							b.setEnabled(enabled);
						}
					}
				}
			}
		}

		/**
		 * Dialog box for unit mask field modification
		 * @since 3.0
		 *
		 */
		protected class UnitMaskDialog extends Dialog {
			private OpEvent event;
			public UnitMaskDialog(IShellProvider parentShell) {
				super(parentShell);
			}

			public UnitMaskDialog(Shell parentShell,OpEvent event) {
				super(parentShell);
				this.event = event;
			}

			@Override
			protected Control createDialogArea(Composite parent) {
				Composite child = (Composite)super.createDialogArea(parent);
				ScrolledComposite scrolledContainer = new ScrolledComposite(child, SWT.H_SCROLL|SWT.V_SCROLL);
				scrolledContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				GridLayout layout = new GridLayout();
				layout.marginHeight = 0;
				layout.marginWidth = 0;
				scrolledContainer.setLayout(layout);
				scrolledContainer.setExpandHorizontal(true);
				scrolledContainer.setExpandVertical(true);
				Composite unitMaskSubComposite  = new Composite(scrolledContainer, SWT.None);
				unitMaskSubComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				layout = new GridLayout();
				layout.numColumns=2;
				unitMaskSubComposite.setLayout(layout);
				createUnitMaskComponents(unitMaskSubComposite);
				scrolledContainer.setContent(unitMaskSubComposite);
				CounterSubTab.this.unitmaskScrollComposite = scrolledContainer;
				CounterSubTab.this.unitMaskSubComposite = unitMaskSubComposite;
				unitMaskViewer.displayEvent(event);
				return child;
			}

			@Override
			protected boolean isResizable() {
				return true;
			}

			@Override
			protected void configureShell(Shell newShell) {
				super.configureShell(newShell);
				newShell.setText(event.getText());
				newShell.setSize(400, 400);
			}
		}


		private void createUnitMaskComponents(Composite parent)
		{
			//event count label/text
			countTextLabel = new Label(parent, SWT.NONE);
			countTextLabel.setText(OprofileLaunchMessages.getString("tab.event.counterSettings.count.label.text")); //$NON-NLS-1$
			countText = new Text(parent, SWT.SINGLE | SWT.BORDER);
			countText.setText(Integer.toString(counter.getCount()));
			countText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			countText.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent me) {
					handleCountTextModify();
				}
			});

			//unit mask widget
			Composite unitMaskComp = new Composite(parent, SWT.NONE);
			GridLayout layout = new GridLayout();
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			unitMaskComp.setLayout(layout);
			unitMaskComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

			unitMaskViewer = new UnitMaskViewer(unitMaskComp);
		}
		private void resizeUnitMaskContainer() {
			unitmaskScrollComposite.setMinSize(unitMaskSubComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		}

		/**
		 * Events selection/removal button listener
		 * @since 3.0
		 *
		 */
		protected class HandleButtonClick implements Listener
 {

			@Override
			public void handleEvent(Event event) {
				int btn_value = (Integer) event.widget.getData();
				switch (btn_value) {
				case ADD:
					addButtonClicked();
					updateLaunchConfigurationDialog();
					break;
				case ADD_ALL:
					addAllButtonClicked();
					updateLaunchConfigurationDialog();
					break;
				case REMOVE:
					removeButtonClicked();
					updateLaunchConfigurationDialog();
					break;
				case REMOVE_ALL:
					removeAllButtonClicked();
					updateLaunchConfigurationDialog();
					break;
				case CUSTOMIZE:
					customizeButtonClicked();
					break;
				default:
					break;
				}

			}

			private void addButtonClicked() {
				int[] indices = eventList.getList().getSelectionIndices();
				if (indices.length != 0) {
					ArrayList<OpEvent> tmp = new ArrayList<>();
					for (int index : indices) {
						OpEvent event = (OpEvent) eventList.getElementAt(index);
						tmp.add(event);
					}
					// add to target list
					targetList.addAll(tmp);
					selectedEventList.add(tmp.toArray());

					sourceList.removeAll(tmp);
					eventList.remove(tmp.toArray());

					int count = selectedEventList.getList().getItemCount();
					tmp = new ArrayList<>();
					for (int i = 0; i < count; i++) {
						OpEvent event = (OpEvent) selectedEventList
								.getElementAt(i);
						tmp.add(event);

					}
					if (!tmp.isEmpty())
						counter.setEvents(tmp.toArray(new OpEvent[0]));

					eventList.refresh();
					selectedEventList.refresh();


				}
			}

			private void addAllButtonClicked() {
				int count = eventList.getList().getItemCount();
				ArrayList<OpEvent> tmp = new ArrayList<>();
				for (int i = 0; i < count; i++) {
					OpEvent event = (OpEvent) eventList.getElementAt(i);
					tmp.add(event);

				}

				targetList.addAll(tmp);
				selectedEventList.add(tmp.toArray());
				eventList.remove(tmp.toArray());
				sourceList.removeAll(tmp);

				count = selectedEventList.getList().getItemCount();
				tmp = new ArrayList<>();
				for (int i = 0; i < count; i++) {
					OpEvent event = (OpEvent) selectedEventList.getElementAt(i);
					tmp.add(event);

				}
				if (!tmp.isEmpty())
					counter.setEvents(tmp.toArray(new OpEvent[0]));

				eventList.refresh();
				selectedEventList.refresh();


			}

			private void removeButtonClicked() {
				int[] indices = selectedEventList.getList()
						.getSelectionIndices();
				if (indices.length != 0) {
					ArrayList<OpEvent> tmp = new ArrayList<>();
					for (int index : indices) {
						OpEvent event = (OpEvent) selectedEventList
								.getElementAt(index);
						tmp.add(event);
					}
					// add to target list
					sourceList.addAll(tmp);
					eventList.add(tmp.toArray());

					targetList.removeAll(tmp);
					selectedEventList.remove(tmp.toArray());

					int count = selectedEventList.getList().getItemCount();
					tmp = new ArrayList<>();
					for (int i = 0; i < count; i++) {
						OpEvent event = (OpEvent) selectedEventList
								.getElementAt(i);
						tmp.add(event);

					}
					if (!tmp.isEmpty())
						counter.setEvents(tmp.toArray(new OpEvent[0]));
					else
						// add first valid element to counter due to NPE
						counter.setEvents(new OpEvent[]{counter.getValidEvents()[0]});

					eventList.refresh();
					selectedEventList.refresh();

				}
			}

			private void removeAllButtonClicked() {

				int count = selectedEventList.getList().getItemCount();
				ArrayList<OpEvent> tmp = new ArrayList<>();
				for (int i = 0; i < count; i++) {
					OpEvent event = (OpEvent) selectedEventList.getElementAt(i);
					tmp.add(event);

				}

				if (!tmp.isEmpty()) {
					sourceList.addAll(tmp);
					eventList.add(tmp.toArray());

					selectedEventList.remove(tmp.toArray());
					targetList.removeAll(tmp);

				}
				counter.setEvents(new OpEvent[] { counter.getValidEvents()[0] });

				eventList.refresh();
				selectedEventList.refresh();

			}

			private void customizeButtonClicked() {
				UnitMaskDialog d = null;

				if(eventList.getList().getSelectionIndex() != -1)
				{
					d = new UnitMaskDialog(Display.getCurrent()
							.getActiveShell(),
							(OpEvent) eventList.getElementAt(eventList.getList()
									.getSelectionIndex()));
				}
				else if(selectedEventList.getList().getSelectionIndex() != -1)
				{
					d = new UnitMaskDialog(Display.getCurrent()
							.getActiveShell(),
							(OpEvent) selectedEventList.getElementAt(selectedEventList.getList()
									.getSelectionIndex()));
				}
				if(d != null)
				d.open();
			}

		}
	}

	/**
	 *
	 * Event sorting for selected as well as all available events
	 * @since 3.0
	 */
	protected class ListviewerSorter extends ViewerSorter
	{
		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			OpEvent op1 = (OpEvent) e1;
			OpEvent op2 = (OpEvent) e2;
			String op1txt = op1.getText();
			String op2txt = op2.getText();
			if(op1txt !=null && op2txt !=null && op1txt.trim().length() !=0 && op2txt.trim().length() !=0)
				return collator.compare(op1txt, op2txt);
			return super.compare(viewer, e1, e2);
		}
	}
}