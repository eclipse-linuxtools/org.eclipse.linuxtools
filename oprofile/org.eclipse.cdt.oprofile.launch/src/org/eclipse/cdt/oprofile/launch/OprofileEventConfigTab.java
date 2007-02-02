/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/

package org.eclipse.cdt.oprofile.launch;

import java.text.MessageFormat;

import org.eclipse.cdt.oprofile.core.OpEvent;
import org.eclipse.cdt.oprofile.core.OpEventLabelProvider;
import org.eclipse.cdt.oprofile.core.Oprofile;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Thic class represents the event configuration tab of the launcher dialog.
 * @author keiths
 */
public class OprofileEventConfigTab extends AbstractLaunchConfigurationTab
{
	private Combo _counterCombo;
	private Button _enabledButton;
	private ListViewer _eventList;
	private Button _profileKernelButton;
	private Button _profileUserspaceButton;
	private Text _countText;
	private UnitMaskViewer _unitMaskViewer;
	private Text _eventDescText;
	private OprofileCounter[] _counters = OprofileCounter.getCounters(null);
	private String _staticEventDesc = ""; //$NON-NLS-1$

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName()
	{
		return OprofileLaunchMessages.getString("tab.event.name"); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration config)
	{
		int numEnabledEvents = 0;
		boolean valid = true;

		OprofileCounter[] counters =
			new OprofileCounter[Oprofile.getNumberOfCounters()];
		for (int i = 0; i < counters.length; i++)
		{
			counters[i] = new OprofileCounter(i);
			counters[i].loadConfiguration(config);
			if (counters[i].getEnabled())
			{
				++numEnabledEvents;

				// First check min count
				int min = counters[i].getEvent().getMinCount();
				if (counters[i].getCount() < min) {
					valid = false;
					break;
				}
				
				// Next ask oprofile if it is valid
				if (!Oprofile.checkEvent(
						counters[i].getNumber(),
						counters[i].getEvent().getNumber(),
						counters[i].getEvent().getUnitMask().getMaskValue()))
				{
					valid = false;
					break;
				}
			}
		}

		boolean b = (numEnabledEvents > 0 && valid);
		//System.out.println("EventConfigTab isValid = " + b);
		return b;
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy config)
	{
		// SUCK FIXME: check if event valid?
		for (int i = 0; i < _counters.length; i++)
			_counters[i].saveConfiguration(config);
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration config)
	{
		for (int i = 0; i < _counters.length; i++)
			_counters[i].loadConfiguration(config);

		// Default to counter 0
		if (_counters.length > 0) {
			_counterCombo.setEnabled(true);
			_counterCombo.select(0); // no selection event???
			_updateDisplay(_currentCounter());
		}
	}
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy config)
	{
		// When instantiated, the OprofileCounter will set defaults.
		for (int i = 0; i < _counters.length; i++)
			_counters[i].saveConfiguration(config);
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(Composite)
	 */
	public void createControl(Composite parent)
	{
		GridData data;
		GridLayout layout;

		Composite top = new Composite(parent, SWT.NONE);
		setControl(top);
		layout = new GridLayout();
		top.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		top.setLayoutData(data);

		createVerticalSpacer(top, 1);

		// Create combo for counter configuration
		_counterCombo = new Combo(top, SWT.DROP_DOWN | SWT.READ_ONLY);
		_counters = OprofileCounter.getCounters(null);
		for (int i = 0; i < _counters.length; i++)
		{
			String s = _counters[i].getText();
			_counterCombo.add(s);
			_counterCombo.setData(s, _counters[i]);
		}
		data = new GridData();
		data.widthHint =
			_counterCombo.computeSize(SWT.DEFAULT, SWT.DEFAULT).x + 30;
		_counterCombo.setLayoutData(data);
		_counterCombo.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent se)
			{
				_updateComboFromSelection();
			}
		});
		_counterCombo.setEnabled(false);

		// Create container for counter settings
		Group g = new Group(top, SWT.SHADOW_ETCHED_IN);
		g.setText(OprofileLaunchMessages.getString("tab.event.counterSettings.label.text")); //$NON-NLS-1$
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		g.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		g.setLayoutData(data);

		_enabledButton = new Button(g, SWT.CHECK);
		_enabledButton.setText(OprofileLaunchMessages.getString("tab.event.counterSettings.enabled.button.text")); //$NON-NLS-1$
		data = new GridData();
		data.horizontalSpan = 2;
		_enabledButton.setLayoutData(data);
		_enabledButton.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent se)
			{
				_handleEnabledToggle();
			}
		});
		_enabledButton.setEnabled(false);

		Label l = new Label(top, SWT.NONE);
		l.setText(OprofileLaunchMessages.getString("tab.event.eventDescription.label.text")); //$NON-NLS-1$
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		l.setLayoutData(data);

		Composite left = new Composite(g, SWT.NONE);
		layout = new GridLayout();
		left.setLayout(layout);
		data = new GridData(GridData.FILL_BOTH);
		left.setLayoutData(data);
		_eventList =
			new ListViewer(left, SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
			// Fix layout data (height, width) after we construct right-hand composite.
//		data = new GridData(GridData.FILL_BOTH);
//		data.widthHint = 100; // SUCK FIXME! How to get font metrics?
//		_eventList.getList().setLayoutData(data);
		_eventList.setLabelProvider(new OpEventLabelProvider());
		_eventList.setContentProvider(new IStructuredContentProvider()
		{
			public void dispose()
			{
			}
			public void inputChanged(Viewer arg0, Object arg1, Object arg2)
			{
			}
			public Object[] getElements(Object inputElement)
			{
				OprofileCounter ctr = (OprofileCounter) inputElement;
				return (OpEvent[]) ctr.getValidEvents();
			}
		});
		_eventList.addSelectionChangedListener(new ISelectionChangedListener()
		{
			public void selectionChanged(SelectionChangedEvent sce)
			{
				_updateEventDisplayFromSelection();
			}
		});
		_eventList.getList().addMouseMoveListener(new MouseMoveListener()
		{
			public void mouseMove(MouseEvent event)
			{
				_handleMouseMove(event);
			}
		});
		_eventList.getList().addMouseTrackListener(new MouseTrackAdapter()
		{
			// FIXME: This doesn't work very well... Only when mouse moves
			// slowly does this really work. Must be a way to get Enter/Leave events...
			public void mouseExit(MouseEvent ev)
			{
				_resetEventDesc();
			}
		});

		Composite right = new Composite(g, SWT.NONE);
		layout = new GridLayout();
		layout.marginWidth = 0;
		right.setLayout(layout);
		data = new GridData(GridData.FILL_BOTH);
		right.setLayoutData(data);
		_profileKernelButton = new Button(right, SWT.CHECK);
		_profileKernelButton.setText(OprofileLaunchMessages.getString("tab.event.counterSettings.profileKernel.check.text")); //$NON-NLS-1$
		_profileKernelButton.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent se)
			{
				_handleProfileKernelToggle();
			}
		});
		_profileUserspaceButton = new Button(right, SWT.CHECK);
		_profileUserspaceButton.setText(OprofileLaunchMessages.getString("tab.event.counterSettings.profileUser.check.text")); //$NON-NLS-1$
		_profileUserspaceButton.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent se)
			{
				_handleProfileUserToggle();
			}
		});
		Composite p = new Composite(right, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		p.setLayout(layout);

		l = new Label(p, SWT.NONE);
		l.setText(OprofileLaunchMessages.getString("tab.event.counterSettings.count.label.text")); //$NON-NLS-1$
		_countText = new Text(p, SWT.SINGLE | SWT.BORDER);
		data = new GridData();
		data.widthHint = 60; // FIXME SUCK: Once again: font metrics?
		_countText.setLayoutData(data);
		_countText.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent me)
			{
				_handleModify();
			}
		});
		_countText.addVerifyListener(new VerifyListener()
		{
			public void verifyText(VerifyEvent ve)
			{
				// Only allow numbers and non-text events (ie. backspace and delete)
				// SUCK FIXME: i18n?
				try
				{
					//An event which doesn't contain text (backspace and delete)
					//should still be allowed.
					String text = ve.text;
					if (text.length() != 0)
					{					
						int count = Integer.parseInt(text);
						if (count < 0)
							ve.doit = false;
					}
				}
				catch (NumberFormatException e)
				{
					ve.doit = false;
				}
			}
		});

		_unitMaskViewer = new UnitMaskViewer(right);

		_eventDescText = new Text(top, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		_eventDescText.setLayoutData(data);
		
		// Now that the right-hand comosite has been constructed, compute the
		// heightHint for the event list.
		data = new GridData(GridData.FILL_BOTH);
		data.widthHint = 250; // SUCK FIXME! How to get font metrics?
		data.heightHint = right.getBounds().height;
		_eventList.getList().setLayoutData(data);
	}

	// returns the current counter
	private OprofileCounter _currentCounter()
	{
		int idx = _counterCombo.getSelectionIndex();
		if (idx != -1)
			return _counters[idx];
		return null;
	}
	
	// handles text modify events in the "count" text widget
	private void _handleModify() {
		try
		{
			if (_currentCounter() != null) {
				String errorMessage = null;
				
				// This seems counter-intuitive, but we must save the count
				// so that isValid knows this launch config is invalid
				int count = Integer.parseInt(_countText.getText());
				_currentCounter().setCount(count);
				
				// Check minimum count
				int min = _currentCounter().getEvent().getMinCount();
				if (count < min) {
					errorMessage = _getMinCountErrorMessage(min);
				}
				setErrorMessage(errorMessage);
				updateLaunchConfigurationDialog();
			}
		}
		catch (NumberFormatException e)
		{
			// Shouldn't happen: all text is validated
		}
	}
	
	// Returns a string with the minimum allowed count, suitable for
	// use with setErrorMessage().
	private String _getMinCountErrorMessage(int min) {
		String msg = OprofileLaunchMessages.getString("tab.event.counterSettings.count.too-small"); //$NON-NLS-1$
		Object[] args = new Object[] { new Integer(min) };
		return MessageFormat.format(msg, args);
	}
	
	// handles the toggling of the "profile user" button
	private void _handleProfileUserToggle()
	{
		if (_currentCounter() != null) {
			_currentCounter().setProfileUser(_profileUserspaceButton.getSelection());
			updateLaunchConfigurationDialog();
		}
	}
	
	// handles the toggling of the "profile kernel" button
	private void _handleProfileKernelToggle()
	{
		if (_currentCounter() != null) {
			_currentCounter().setProfileKernel(_profileKernelButton.getSelection());
			updateLaunchConfigurationDialog();
		}
	}
	
	// handles mouse movement in the listbox (displaying event
	// description in the textbox at the bottom)
	private void _handleMouseMove(MouseEvent me)
	{
		int lineHeight = _eventList.getList().getItemHeight();
		Rectangle area = _eventList.getList().getClientArea();
		int index = 0;
		while (index * lineHeight < me.y)
			++index;
		index += _eventList.getList().getTopIndex() - 1;
		OpEvent e = (OpEvent) _eventList.getElementAt(index);
		if (e != null) {
			_setEventDesc(e, true);
		}
	}
	
	// handles the toggling of the "enabled" combo
	private void _handleEnabledToggle()
	{
		boolean enabled = _enabledButton.getSelection();
		OprofileCounter c =
			(OprofileCounter) _counterCombo.getData(_counterCombo.getText());
		c.setEnabled(enabled);
		_updateDisplay(c);
		updateLaunchConfigurationDialog();
	}

	// resets the description text to the selected event for the current counter
	private void _resetEventDesc()
	{
		_eventDescText.setText(_staticEventDesc);
	}
	
	// when a counter is enabled/disabled, this method enables/disables
	// UI elements as appropriate
	private void _setEnabled(boolean b)
	{
		// When event is disabled, disable all input from UI objects
		_profileKernelButton.setEnabled(b);
		_profileUserspaceButton.setEnabled(b);
		_countText.setEnabled(b);
		_unitMaskViewer.setEnabled(b);
		_eventList.getList().setEnabled(b);
		_eventDescText.setEnabled(b);
	}
	
	// Sets the event description textbox to display the event description
	// of the given event. If it is TEMPORARY, then the next call to resetEventDesc
	// will wipe this out. If it is not TEMPORARY, then the next call to resetEventDesc
	// will display this event's description.
	private void _setEventDesc(OpEvent oe, boolean temporary)
	{
		if (!temporary)
			_staticEventDesc = oe.getTextDescription();

		_eventDescText.setText(oe.getTextDescription());
	}
	
	// updates the display of event options based on the selection
	private void _updateComboFromSelection()
	{
		/* Man, does the SWT Combo widget suck. If the user clicks the drop button,
		 * then releases the mouse button, then selects an item from the list, the Combo
		 * will send the selection event twice. I can live with that.
		 * 
		 * What worse is that if the user clicks the drop button and DOESN'T release
		 * the mouse button, just moving over the entries, we get selection event
		 * notifications for them all. So now we've got to rewrite the damn bindings
		 * for this lame widget. BIG SUCK: FIXME (as in, cannot release with this)
		 * 
		 * ModifyEvent suffers from the same problem. (Try this: hold mouse button down
		 * over the _same_ entry in the combo -- we get 1,000,000,000 event notifications.)
		 */
		_updateDisplay(_currentCounter());
	}
	
	// Does the real work in updating the display for the given counter
	private void _updateDisplay(OprofileCounter ctr)
	{
		if (ctr != null) {
			// Update event list, which depends on counter
			_eventList.setInput(ctr);
			
			// Show enabled
			boolean enabled = ctr.getEnabled();
			_enabledButton.setEnabled(true);
			_enabledButton.setSelection(enabled);
			_setEnabled(enabled);
			
			if (enabled)
			{
				// event
				if (ctr.getEvent() == null)
				{
					// Default to first in list
					ctr.setEvent(ctr.getValidEvents()[0]);
				}
				_eventList.setSelection(new StructuredSelection(ctr.getEvent()));
				
				// count
				_countText.setText(Integer.toString(ctr.getCount()));
				
				// profle kernel
				_profileKernelButton.setSelection(ctr.getProfileKernel());
				
				// profile user
				_profileUserspaceButton.setSelection(ctr.getProfileUser());
				
				// Update event-specific displays
				_updateEventDisplay(ctr.getEvent());
			}
		}
	}
	
	// Gets the selection from the combobox and updates the event display
	// (Primarily updates the UnitMaskViewer and the event desc textbox)
	private void _updateEventDisplayFromSelection()
	{
		if (_currentCounter() != null) {
			int index = _eventList.getList().getSelectionIndex();
			OpEvent event = (OpEvent) _eventList.getElementAt(index);
			_currentCounter().setEvent(event);
			_updateEventDisplay(event);
			updateLaunchConfigurationDialog();
			
			// Check the min count to update the error message (events can have different
			// minimum reset counts)
			int min = _currentCounter().getEvent().getMinCount();
			if (_currentCounter().getCount() < min) {
				setErrorMessage(_getMinCountErrorMessage(min));
				updateLaunchConfigurationDialog();
			}
		}
	}
	
	// Does the real work of updating the updating the event-specific displays
	private void _updateEventDisplay(OpEvent event)
	{
		_setEventDesc(event, false);
		_unitMaskViewer.displayEvent(event);
	}
}
