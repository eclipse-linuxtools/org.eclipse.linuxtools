/*******************************************************************************
 * Copyright (c) 2004,2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *    Kent Sebastian <ksebasti@redhat.com> - 
 *******************************************************************************/ 
package org.eclipse.linuxtools.oprofile.ui.system;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.linuxtools.oprofile.ui.OprofileUiPlugin;
import org.eclipse.linuxtools.oprofile.ui.ProfileContentProvider;
import org.eclipse.linuxtools.oprofile.ui.ProfileLabelProvider;
import org.eclipse.linuxtools.oprofile.ui.internal.IProfileElement;
import org.eclipse.linuxtools.oprofile.ui.sample.SampleView;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.ViewPart;


/**
 * This is the SystemProfileView. It is similar to the Navigation view,
 * but in a profiling-centric way. This class is responsible for creating and
 * manipulating the view's UI, saving and restoring state, etc.
 */
public class SystemProfileView extends ViewPart implements IMenuListener
{
	protected TreeViewer _viewer;
	protected IMemento _memento = null;

	// The event currently being viewed
	private String _current = null;
	
	// All the menu Actions (one for each event)
	private HashMap _actions;
	
	// Maps event name (String) to SystemProfileEvents
	private HashMap _eventList;
	
	// Persistence - global view parameters
	private static final String TAG_LAST_EVENT = "lastEvent"; //$NON-NLS-1$
	
	// Persistence tags
	private static final String TAG_EVENT = "event"; //$NON-NLS-1$
	private static final String TAG_EVENT_NAME = "name"; //$NON-NLS-1$
	private static final String TAG_SELECTION = "selection"; //$NON-NLS-1$
	private static final String TAG_ELEMENT = "element"; //$NON-NLS-1$
	private static final String TAG_ELEMENT_ID = "id"; //$NON-NLS-1$
	private static final String TAG_VERTICAL_POSITION = "verticalPosition"; //$NON-NLS-1$
	private static final String TAG_EXPANDED = "expanded"; //$NON-NLS-1$
	
	// This saves the TreeViewer state so that it can be saved/restored
	private class DisplayInfo
	{
		int verticalPosition;
		Object[] expandedElements;
		Object[] selection;
	};
	
	// A map of all event display settings (key is event name (String))
	private HashMap _displays;
	
	/**
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(Composite)
	 */
	public void createPartControl(Composite parent)
	{
		_viewer = new TreeViewer(parent);
		_viewer.setContentProvider(new ProfileContentProvider());
		_viewer.setLabelProvider(new ProfileLabelProvider());
		_viewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			public void selectionChanged(SelectionChangedEvent sce)
			{
				TreeItem[] items = _viewer.getTree().getSelection();
				if (items.length > 0)
				{
					IProfileElement element = (IProfileElement) items[0].getData();
					_showInSampleView(element);
				}
			}
		});		
		
		
		// Create profile roots for events
		_refreshEvents();
		
		// Create Actions
		_createActions();
		
		// Update menus
		_updateMenus();
		
		// Restore state
		boolean haveState = false;
		if (_memento != null)
		{
			haveState = _restoreState(_memento);
			_memento = null;
		}
		
		if (!haveState)
		{
			// No previous state
			if (!_eventList.isEmpty()) {
				String first = (String) _eventList.keySet().iterator().next();
				_showEvent(first);
			}
		}
		
		OprofileUiPlugin.getDefault().setSystemProfileView(this);
	}
	
	public void dispose() {
		OprofileUiPlugin.getDefault().setSystemProfileView(null);
	}
	
	// Sync events for current oprofile state on disk
	private void _refreshEvents() {
		// Scan through the sessions and find all events that were collected.
		_eventList = new HashMap();
		_displays = new HashMap();
		
//		OpModelEvent[] events = Oprofile.getSessionEvents();
//		for (int i = 0; i < events.length; ++i) {
//			SystemProfileEvent spEvent = new SystemProfileEvent();
//			for (int j = 0; j < events[i].sessions.length; ++j) {
//				OpModelSession session = events[i].sessions[j];
//				spEvent.add(new SystemProfileSession(session));
//			}
//			_eventList.put(events[i].eventName, spEvent);
//		}
	}
	
	// Creates Actions for menus, toolbars, etc
	private void _createActions()
	{
		// Create actions for oprofile events shown in the viewer
		_actions = new HashMap();
		// _updateMenus will actually populate this
		
		// Toolbar contributions
		IActionBars actionBars = getViewSite().getActionBars();
		
		Action refreshAction = new Action() {
			public void run() {
				refreshView();
			}
		};
		actionBars.setGlobalActionHandler(ActionFactory.REFRESH.getId(), refreshAction);
	}
	
	/**
	 * Refresh the view
	 */
	public void refreshView() {
//really not needed here		
//		/* Give the user a chance to initialize the kernel
//		   module, if he hasn't already done so. */
//		if (!Oprofile.isKernelModuleLoaded()) {
//			Oprofile.initializeOprofileModule();
//		}

		// Update the list of known events
		_refreshEvents();
		_updateMenus();
		
		// Get the current layout of the viewer
	    IMemento memento = XMLMemento.createWriteRoot("foo"); //$NON-NLS-1$
		saveState(memento);
		
		// Clear the viewer and repopulate it
		_viewer.setInput(null);
		
		// Try to restore the previous layout. Reset _current so that
		// we don't attempt to save the layout again in _showEvent (called by _restoreState).
		_current = null;
		boolean hasState = _restoreState(memento);
		
		//no previous event to display, choose the first one from the list
		if (!hasState && !_eventList.isEmpty())
		{
			_showEvent((String)_eventList.keySet().toArray()[0]);
		}
	}
	
	// Responsible for showing the event with the given name
	// This means resetting viewer's input, restoring any previously
	// saved state, updating menus, etc.
	private void _showEvent(String event)
	{
		for (Iterator i = _actions.keySet().iterator(); i.hasNext(); )
		{
			Action action = (Action) _actions.get(i.next());
			action.setChecked(false);
		}

		// Save current event info
		if (_current != null)
			_saveDisplayInfo(_current);
		_current = event;
		
		// Check new event menu action
		Action action = (Action) _actions.get(event);
		action.setChecked(true);
		
		_displayEvent(event);
	}
	
	// Does the real work of displaying the given event
	private void _displayEvent(String event)
	{
		// Set the viewer
		_viewer.setInput(_eventList.get(event));
		
		// Restore display info for this event
		DisplayInfo info = (DisplayInfo) _displays.get(event);
		if (info != null)
		{
			// Expand items
			if (info.expandedElements != null)
				_viewer.setExpandedElements(info.expandedElements);
			
			// Set vertical position
			ScrollBar bar = _viewer.getTree().getVerticalBar();
			if (bar != null)
				bar.setSelection(info.verticalPosition);
			
			// Restore selection
			if (info.selection != null)
			{
				_viewer.setSelection(new StructuredSelection(info.selection));
				
				// Display first element in OpModelSample viewer
				_showInSampleView((IProfileElement) info.selection[0]);
			}
		}
	}
	
	// Restore the state of the display based on the last exit state
	private boolean _restoreState(IMemento memento)
	{
		// Restore DisplayInfo
		IMemento[] events = memento.getChildren(TAG_EVENT);
		for (int i = 0; i < events.length; ++i)
		{
			String event = events[i].getString(TAG_EVENT_NAME);
			if (event != null && _actions.containsKey(event))
			{
				DisplayInfo info = new DisplayInfo();
				
				// Get expanded elements
				info.expandedElements = null;
				ArrayList elements = new ArrayList();
				IMemento expanded = events[i].getChild(TAG_EXPANDED);
				if (expanded != null)
				{
					IMemento[] mem = expanded.getChildren(TAG_ELEMENT);
					for (int j = 0; j < mem.length; ++j)
					{
						String id = mem[j].getString(TAG_ELEMENT_ID);
						IProfileElement element = _getEventObject(event, id);
						if (element != null)
							elements.add(element);
					}
				}
				if (elements.size() > 0)
				{
					info.expandedElements = new Object[elements.size()];
					elements.toArray(info.expandedElements);
				}

				// Get veritcal position
				info.verticalPosition = 0;
				Integer pos = events[i].getInteger(TAG_VERTICAL_POSITION);
				if (pos != null)
					info.verticalPosition = pos.intValue();
				
				// Get selection
				info.selection = null;
				String selection = events[i].getString(TAG_SELECTION);
				if (selection != null)
				{
					IProfileElement obj = _getEventObject(event, selection);
					if (obj != null)
						info.selection = new Object[] {obj};
				}
				
				// Save DisplayInfo into list
				_displays.put(event, info);
			}
		}
			
		// Restore last viewed event
		String last = memento.getString(TAG_LAST_EVENT);
		if (last != null && _actions.containsKey(last)) {
			_showEvent(last);
			return true;
		}
		
		return false;
	}
	
	// Helper function for _restoreState. Given an event and an ID of an
	// object for the event, find the object. Returns null if not found
	private IProfileElement _getEventObject(String eventString, String id)
	{
		IProfileElement obj = null;
		SystemProfileEvent event = (SystemProfileEvent) _eventList.get(eventString);
		if (event != null)
			obj = event.getElementFromId(id);
		
		return obj;
	}

	
	// Responsible for saving the display info for the given event
	private void _saveDisplayInfo(String event)
	{
		// Save event display info
		DisplayInfo info = new DisplayInfo();
		
		ScrollBar bar = _viewer.getTree().getVerticalBar();
		info.verticalPosition = (bar == null ? 0 : bar.getSelection());
		info.expandedElements = _viewer.getExpandedElements();
		IStructuredSelection sel = (IStructuredSelection) _viewer.getSelection();
		if (sel.isEmpty())
			info.selection = null;
		else
			info.selection = sel.toArray();
		
		_displays.put(event, info);
	}
	
	// Updates the menus with current event configuration
	private void _updateMenus()
	{
		// out with the old
		_actions.clear();
		
		// in with the new
		for (Iterator i = _eventList.keySet().iterator(); i.hasNext(); )
		{
			final String event = (String) i.next();
			Action action = new Action(event)
			{
				public void run()
				{
					_showEvent(event);
				};
			};
			_actions.put(event, action);
		}
		
		// update menus
		IMenuManager mgr = getViewSite().getActionBars().getMenuManager();
		mgr.removeAll();
		for (Iterator i = _eventList.keySet().iterator(); i.hasNext(); )
		{
			String eventStr = (String) i.next();
			Action action = (Action) _actions.get(eventStr);
			action.setChecked(false);
			mgr.add(action);
		}

		//getViewSite().getActionBars().getToolBarManager();
		
		//MenuManager menuMgr = new MenuManager("#PopupMenu");
		//menuMgr.setRemoveAllWhenShown(true);
		//menuMgr.addMenuListener(this);
	}
	
	// Shows the given element in the sample viewer
	protected void _showInSampleView(IProfileElement element)
	{
		SampleView view = OprofileUiPlugin.getDefault().getSampleView();
		if (view != null)
			view.setInput(element);
	}	
	
	/**
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	public void setFocus()
	{
		_viewer.getTree().setFocus();
	}
	
	public void init(IViewSite site, IMemento memento) throws PartInitException
	{
		super.init(site, memento);
		_memento = memento;
	}	

	public void menuAboutToShow(IMenuManager manager) {
		//do nothing?
	}

	// Saves the state of this widget into the given memento
	public void saveState(IMemento memento)
	{
		if (_viewer == null)
		{
			if (_memento != null)
				memento.putMemento(_memento);
		}
		else
		{
			// Save all events
			for (Iterator i = _eventList.keySet().iterator(); i.hasNext(); )
			{
				// Save last viewed event
				String event = (String) i.next();
				if (_current != null && _current.equals(event))
				{
					_saveDisplayInfo(event);
					memento.putString(TAG_LAST_EVENT, event);
				}
				
				DisplayInfo info = (DisplayInfo) _displays.get(event);
				if (info != null)
				{
					// Save event-specific view data
					IMemento eventMemento = memento.createChild(TAG_EVENT);
					eventMemento.putString(TAG_EVENT_NAME, event);
				
					// Expanded elements
					if (info.expandedElements != null && info.expandedElements.length > 0)
					{
						IMemento expMemento = eventMemento.createChild(TAG_EXPANDED);
						for (int j = 0; j < info.expandedElements.length; ++j)
						{
							IProfileElement element = (IProfileElement) info.expandedElements[j];
							IMemento child = expMemento.createChild(TAG_ELEMENT);
							child.putString(TAG_ELEMENT_ID, element.getId());
						}
					}
					
					// vertical position
					eventMemento.putInteger(TAG_VERTICAL_POSITION, info.verticalPosition);

					// Selection
					if (info.selection != null)
					{
						for (int j = 0; j < info.selection.length; ++j)
						{
							IProfileElement element = (IProfileElement) info.selection[j];
							eventMemento.putString(TAG_SELECTION, element.getId());
						}
					}
				}
			}
		}
	}
	
	public void changeSelection(IProfileElement element) 
	{
		//set new selection in the tree and show it in the sample view
		_viewer.setSelection(new StructuredSelection(element));
		_showInSampleView(element);
	}
}
