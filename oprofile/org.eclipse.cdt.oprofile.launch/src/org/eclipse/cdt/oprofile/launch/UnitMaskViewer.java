/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/

package org.eclipse.cdt.oprofile.launch;

import org.eclipse.cdt.oprofile.core.OpEvent;
import org.eclipse.cdt.oprofile.core.OpUnitMask;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * This class displays unit masks in the launcher's event configuration tab.
 * @author Keith Seitz <keiths@redhat.com>
 */
public class UnitMaskViewer
{
	private static final int RADIO = 0;
	private static final int CHECK = 1;
	private static final int MAX_BUTTONS = 7; // defined by oprofile's max # masks	
	private Button _maskButton[][]= new Button[2][MAX_BUTTONS];
	private Composite[] _group = new Composite[2];
	
	/**
	 * Constructor for UnitMaskViewer
	 * @param parent	the parent composite
	 */
	public UnitMaskViewer(Composite parent)
	{
		Label l = new Label(parent, SWT.NONE);
		l.setText(OprofileLaunchMessages.getString("unitmaskViewer.label.text")); //$NON-NLS-1$

		FormData fdata;
		GridData gdata;
		
		Composite top = new Composite(parent, SWT.NONE);
		gdata = new GridData(GridData.FILL_BOTH);
		top.setLayoutData(gdata);
		top.setLayout(new FormLayout());
		
		_group[RADIO] = new Composite(top, SWT.NONE);
		fdata = new FormData();
		fdata.left = new FormAttachment(0, 0);
		fdata.right = new FormAttachment(100, 0);
		_group[RADIO].setLayoutData(fdata);
		_group[RADIO].setLayout(new GridLayout());
		_group[RADIO].setVisible(false);
		_group[CHECK] = new Composite(top, SWT.NONE);
		fdata = new FormData();
		fdata.left = new FormAttachment(0, 0);
		fdata.right = new FormAttachment(100, 0);
		_group[CHECK].setLayoutData(fdata);
		_group[CHECK].setLayout(new GridLayout());
		_group[CHECK].setVisible(false);

		for (int i = 0; i < MAX_BUTTONS; i++)
		{
			final int num = i;
			_maskButton[RADIO][i] = new Button(_group[RADIO], SWT.RADIO);
			_maskButton[RADIO][i].addSelectionListener(new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent se)
				{
					_handleToggle(RADIO, num);
				}
			});
			gdata = new GridData(GridData.FILL_HORIZONTAL);
			_maskButton[RADIO][i].setLayoutData(gdata);
			
			_maskButton[CHECK][i] = new Button(_group[CHECK], SWT.CHECK);
			_maskButton[CHECK][i].addSelectionListener(new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent se)
				{
					_handleToggle(CHECK, num);
				}
			});
			gdata = new GridData(GridData.FILL_HORIZONTAL);
			_maskButton[CHECK][i].setLayoutData(gdata);
		}
	}
	
	// hanldes all button toggles (checkbuttons and radio buttons)
	private void _handleToggle(int group, int num)
	{
		boolean selected = _maskButton[group][num].getSelection();
		
		OpUnitMask mask = (OpUnitMask) _maskButton[group][num].getData();
		if (mask != null)
			mask.setMask(num);
	}
		
	/**
	 * Displays the unit mask options for the given event
	 * @param oe	the event
	 */
	public void displayEvent(OpEvent oe)
	{
			if (oe.getUnitMask().numMasks() == 0)
			{
				// Shortcut
				_group[RADIO].setVisible(false);
				_group[CHECK].setVisible(false);
				return;
			}
			
			_displayMask(oe.getUnitMask());
	}

	// does the real work of swapping out radio and check buttons and
	// updating labels
	private void _displayMask(OpUnitMask mask)
	{
		int onSet = (mask.getType() == OpUnitMask.EXCLUSIVE ? RADIO : CHECK);
		int offSet = (mask.getType() == OpUnitMask.EXCLUSIVE ? CHECK : RADIO);
		
		for (int i = 0; i < MAX_BUTTONS; i++)
		{
			if (i < mask.numMasks())
			{
				_maskButton[onSet][i].setVisible(true);
				_maskButton[onSet][i].setText(mask.getText(i));
				_maskButton[onSet][i].setData(mask);
				_maskButton[onSet][i].setSelection(mask.isSet(i));
			}
			else
				_maskButton[onSet][i].setVisible(false);
		}
		
		// Swap on and off
		_group[onSet].setVisible(true);
		_group[offSet].setVisible(false);
	}

	/**
	 * Enables and disables the viewer for UI input
	 * @param enabled	whether this viewer should be enabled
	 */
	public void setEnabled(boolean enabled)
	{
		int set = (_group[RADIO].isVisible() ? RADIO : (_group[CHECK].isVisible() ? CHECK : -1));
		if (set > -1)
		{
			for (int i = 0; i < _maskButton[set].length; i++)
				_maskButton[set][i].setEnabled(enabled);
		}
	}
}
