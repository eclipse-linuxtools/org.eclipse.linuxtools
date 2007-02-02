/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/

package org.eclipse.cdt.oprofile.ui;

import org.eclipse.cdt.oprofile.ui.internal.IProfileElement;
import org.eclipse.cdt.oprofile.ui.sample.SampleView;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;


/**
 * This is base class for the different profile-centric views. This is very bare-bones
 * right now, since much of the logic is in the SystemProfileView. Whenever
 * ProjectProfileView is completed, this will probably be a lot more useful.
 * @author keiths
 */
public class BaseProfileView extends ViewPart implements IMenuListener
{
	protected TreeViewer _viewer;
	protected IMemento _memento = null;

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
	}
	
	// Shows the given element in the sample viewer
	protected void _showInSampleView(IProfileElement element)
	{
		SampleView view = OprofilePlugin.getDefault().getSampleView();
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
	
	/**
	 * @see org.eclipse.ui.IViewPart#saveState(IMemento)
	 */
	public void saveState(IMemento memento)
	{
	}
	
	/**
	 * @see org.eclipse.jface.action.IMenuListener#menuAboutToShow(IMenuManager)
	 */
	public void menuAboutToShow(IMenuManager menuMgr)
	{
	}
}
