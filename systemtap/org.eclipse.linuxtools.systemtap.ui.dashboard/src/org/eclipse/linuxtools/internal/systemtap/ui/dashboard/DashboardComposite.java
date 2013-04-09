/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse, Anithra P J
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.dashboard;

import java.util.ArrayList;

import org.eclipse.linuxtools.internal.systemtap.ui.dashboard.DashboardAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * A multiplexing composite class that presents itself as one composite, while
 * allowing up to 8 DashboardAdapter composites to render on its surface. Handles
 * space allocation, drag/drop between the composites.
 * @author Henry Hughes
 */
public class DashboardComposite {

	/**
	 * The set of internal composites, these are rendered on by the associated adapters.
	 */
	private final Composite[] internal = new Composite[8];
	/**
	 * The set of external DashboardAdapter references, each one of these is associated
	 * with the internal Composite sharing its index number in the internal array.
	 */
	private final DashboardAdapter[] external = new DashboardAdapter[8];
	/**
	 * Used slot counter, keeps track of how many internal composites are presently dirty.
	 */
	private int usedSlotCount = 0;
	/**
	 * Drag listeners for all of the internal composites.
	 */
	private final InternalCompositeDragListener[] dragListeners = new InternalCompositeDragListener[8];
	/**
	 * Drop listeners for all of the internal composites.
	 */
	private final InternalCompositeDropListener[] dropListeners = new InternalCompositeDropListener[8];
	/**
	 * Queue at the end of the list. When there are 8 objects rendering and a 9th object is added,
	 * it is placed on this queue and displayed when an internal composite is released.
	 */
	private final ArrayList<DashboardAdapter> externalQueue = new ArrayList<DashboardAdapter>();
	/**
	 * The actual rendering surface.
	 */
	private final Composite surface;

	int maximized = 0;
	/**
	 * Parental hell. When objects are added to this composite incorrectly, the next run of the
	 * reparent algorithm will assign objects that are drawing on the wrong composite to this deadComposite.
	 * This composite is not rendered, it is set invisible and disabled. When an object is added to the
	 * rendering queue, it is also reparented to the dead composite.
	 */
	public final Composite deadComposite;

	/**
	 * Default constructor for the DashboardComposite. Creates all of the internal composites, sets up
	 * listeners.
	 * @param parent The parent Composite for this Composite.
	 * @param style SWT style flag to use.
	 */
	public DashboardComposite(Composite parent, int style) {
		parent.setLayout(new FormLayout());
		FormData data = new FormData();
		data.left = new FormAttachment(0,0);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(0,0);
		data.bottom = new FormAttachment(100,0);
		surface = new Composite(parent, style);
		surface.setLayoutData(data);
		surface.setLayout(new FormLayout());
		deadComposite = new Composite(parent, style);
		deadComposite.setVisible(false);
		for(int i = 0; i < internal.length; i++) {
			internal[i] = new Composite(surface, SWT.NONE);
			data = new FormData();
			internal[i].setLayoutData(data);
			internal[i].setVisible(false);
			internal[i].setLayout(new FormLayout());
			external[i] = null;
			dragListeners[i] = new InternalCompositeDragListener(i);
			dropListeners[i] = new InternalCompositeDropListener(i);
		}
	}

	/**
	 * This listener detects drag events inside the DashboardComposite and creates
	 * a string that represents the ID of the composite that is being dragged.
	 * @author Henry Hughes
	 */
	private class InternalCompositeDragListener extends DragSourceAdapter {
		final int sourceID;
		public InternalCompositeDragListener(int k)	{
			super();
			sourceID = k;
		}
		@Override
		public void dragStart(DragSourceEvent event) {
			super.dragStart(event);
			if(!external[sourceID].folder.isVisible()) {
				event.doit = false;
			}
		}

		@Override
		public void dragSetData(DragSourceEvent event) {
			super.dragSetData(event);
			event.data = Integer.toString(sourceID);
		}
	}

	/**
	 * This listener detects drop events, and invokes the move(int,int) method in DashboardComposite
	 * to swap the composites.
	 * @author Henry Hughes
	 */
	private class InternalCompositeDropListener extends DropTargetAdapter {
		final int sourceID;
		public InternalCompositeDropListener(int k)	{
			sourceID = k;
		}
		@Override
		public void dragEnter(DropTargetEvent event) {
			if(event.currentDataType != null) {
				event.detail = DND.DROP_MOVE;
			} else {
				event.detail = DND.DROP_NONE;
			}
		}

		@Override
		public void dragOperationChanged(DropTargetEvent event) {
			super.dragOperationChanged(event);
		}
		long lastDropTime = 0;
		@Override
		public void drop(DropTargetEvent event) {
			super.drop(event);
			int k = -1;

			/* For some absurdly stupid reason this method seems to be called twice under
			 * certain circumstances. This is probably a bug in eclipse's SDK, nonetheless, it's our
			 * problem now. To get around this, we check for timestamps within 100ms of each other.
			 * If the last drop was fired less than 100ms ago, we silently eat the event.
			 */
			long eventTime = event.time & 0xFFFFFFFFL;
			if(eventTime > lastDropTime - 100 && eventTime < lastDropTime + 100) {
				return;
			}

			try	{
				k = Integer.parseInt((String)event.data);
			} catch (NumberFormatException e) {
				//ignore unparseable
			}
			if(k > -1 && k < 8 && k != sourceID) {
				move(k, sourceID);
				move(k,k);  //dont ask me why, but the original graph refuses to update without this
				lastDropTime = eventTime;
			}
		}
	}

	/**
	 * Adds the input DashboardAdapter to the DashboardComposite, either assigning it an internal composite
	 * and rescaling the composite if necessary, or adding it to the queue if there are no free internal
	 * composites to use.
	 * @param c DashboardAdapter to add.
	 */
	public void add(DashboardAdapter c)	{
		int index = findLowestOpenSlot();
		if(index == -1)	{//add to the queue
			externalQueue.add(c);
			return;
		}
		external[index] = c;
		FormData data = new FormData();
		data.left= new FormAttachment(0,0);
		data.right = new FormAttachment(100,0);
		data.top = new FormAttachment(0,0);
		data.bottom = new FormAttachment(100,0);
		c.folder.setParent(internal[index]);
		c.folder.setLayoutData(data);
		bindDND(index);
		calculateUsedSlots();
		scale();
		surface.layout(true, true);
	}

	/**
	 * Removes the input DashboardAdapter, removing it from the internal composite it's associated to
	 * or from the queue, compacting the internal and external arrays, then rescaling.
	 * @param c DashboardAdapter to remove.
	 */
	public void remove(DashboardAdapter c) {
		int index = -1;
		for(int i = 0; i < external.length; i++) {
			if(external[i] == c) {
				index = i;
			}
		}
		if(index == -1)	{ //it's probably in externalQueue, pop it off of the queue and done
			externalQueue.remove(c);
		} else {	//it's on the rendering queue... deal with it
			external[index].resetDND(dragListeners[index], dropListeners[index]);
			external[index] = null;

			compact();
			reparent();
			calculateUsedSlots();
			scale();
			surface.layout(true, true);
		}
	}
	/**
	 * Swaps the objects rendering on composites from and to, reparenting them to their new homes and
	 * moving them in the actual array. Also resets drag and drop listeners on those objects.
	 */
	public void move(int from, int to) {
		DashboardAdapter fromShim = external[from], toShim = external[to];
		external[to] = fromShim;
		external[from] = toShim;

		external[to].setParent(internal[to]);
		external[from].setParent(internal[from]);

		fromShim.resetDND(dragListeners[from], dropListeners[from]);
		toShim.resetDND(dragListeners[to], dropListeners[to]);
		bindDND(from);
		bindDND(to);

		return;
	}
	/**
	 * Binds the drag and drop listeners for the given slots.
	 * @param slot Slot to bind D&D.
	 */
	private void bindDND(int slot) {
		external[slot].dragSource.addDragListener(dragListeners[slot]);
		external[slot].dropTarget.addDropListener(dropListeners[slot]);
	}
	/**
	 * Finds the lowest open slot index.
	 * @return Returns the lowest available internal slot's index.
	 */
	private int findLowestOpenSlot() {
		for(int i = 0; i < external.length; i++) {
			if(external[i] == null) {
				return i;
			}
		}
		return -1;
	}
	/**
	 * Calculates the number of used slots.
	 */
	private void calculateUsedSlots() {
		int count = 0;
		for (int i = 0; i < external.length; i++) {
			if(external[i] != null) {
				count++;
			}
		}
		usedSlotCount = count;
	}
	/**
	 * "Crushes" the external array, removing open slots that are not at the end of the list.
	 */
	private void compact() {
		int i = 0, j = 0;
		while(i < external.length) {
			while(i < external.length && external[i] != null) {
				i++;
			}
			if(i == external.length) {
				break;
			}
			//we've found the lowest null slot
			if(externalQueue.size() > 0) {	//fill it from the queue
				external[i] = externalQueue.remove(0);
				FormData data = new FormData();
				data.left= new FormAttachment(0,0);
				data.right = new FormAttachment(100,0);
				data.top = new FormAttachment(0,0);
				data.bottom = new FormAttachment(100,0);
				external[i].setLayoutData(data);
				continue;
			}
			j = i;
			while(j < external.length && external[j] == null) {
				j++;
			}
			if(j == external.length) {
				break;		//we never found a non-null, so i is the first blank slot
			}
			external[i] = external[j];
			external[j] = null;
			i = j;
		}
	}
	/**
	 * Reparents all the external DashboardAdapters to the correct internal composite.
	 */
	private void reparent()	{
		for(int i = 0; i < internal.length; i++) {
			Control[] c = internal[i].getChildren();
			for(int j = 0; j < c.length; j++) {
				if(external[i] != null && c[j] == external[i].folder) {
					c[j].setVisible(true);
				} else {
					c[j].setParent(deadComposite);
				}
			}
		}
		for(int i = 0 ; i < external.length; i++) {
			if(external[i] != null)	{
				external[i].setParent(internal[i]);
				external[i].resetDND(null, null);
				bindDND(i);
			}
		}
	}

	/**
	 * Removes the input DashboardAdapter, removing it from the internal composite it's associated to
	 * or from the queue, compacting the internal and external arrays, then rescaling.
	 * @param c DashboardAdapter to remove.
	 */
	public void maximize(DashboardAdapter c) {
		int index = -1;
		for (int i = 0; i < external.length; i++) {
			if (external[i] == c) {
				index = i;
			}
		}
		usedSlotCount = 1;
		maximized = index;
		scale();
		surface.layout(true, true);
	}
	/**
	 * Removes the input DashboardAdapter, removing it from the internal composite it's associated to
	 * or from the queue, compacting the internal and external arrays, then rescaling.
	 * @param c DashboardAdapter to remove.
	 */
	public void restore() {
			calculateUsedSlots();
			maximized = 0;
			scale();
			surface.layout(true, true);
	}

	public int getusedSlots() {
		return usedSlotCount;
	}
	/**
	 * Sets the LayoutData for all of the internal Composites, scaling the size to an appropriate amount
	 * for the number of currently used slots.
	 */
	private void scale()
	{
		switch(usedSlotCount)
		{
		case 8:
		case 7:
			//turn on all internal composites, 4x2 layout
			for(int i = 0; i < 8; i++) {
				FormData data = new FormData();
				data.left = new FormAttachment(i%2 * 50, 0);
				data.right = new FormAttachment((i%2 +1)* 50, 0);
				data.top = new FormAttachment(i / 2, 4, 0);
				data.bottom = new FormAttachment(i/2+1, 4, 0);
				internal[i].setLayoutData(data);
				internal[i].setVisible(true);
				if(external[i] != null) {
					external[i].setVisible(true);
				}
			}
			break;
		case 6:
		case 5:
			//turn on 6 internal composites, 3x2 layout
			for(int i = 0; i < 6; i++) {
				FormData data = new FormData();
				data.left = new FormAttachment(i%2 * 50, 0);
				data.right = new FormAttachment((i%2+1) * 50, 0);
				data.top = new FormAttachment(i /2,3, 0);
				data.bottom = new FormAttachment(i/2+1,3, 0);
				internal[i].setLayoutData(data);
				internal[i].setVisible(true);
				if(external[i]!= null) {
					external[i].setVisible(true);
				}
			}
			for(int i = 6; i < 8; i++) {
				internal[i].setVisible(false);
				if(external[i] != null) {
					external[i].setVisible(false);	//this should never happen
				}
			}
			break;
		case 4:
		case 3:
			//turn on 4 internal composites, 2x2 layout
			for(int i = 0; i < 4; i++) {
				FormData data = new FormData();
				data.left = new FormAttachment(i%2 * 50, 0);
				data.right = new FormAttachment((i%2+1) * 50, 0);
				data.top = new FormAttachment(i / 2,2, 0);
				data.bottom = new FormAttachment(i/2+1,2, 0);
				internal[i].setLayoutData(data);
				internal[i].setVisible(true);
				if(external[i] != null) {
					external[i].setVisible(true);
				}
			}
			for(int i = 4; i < 8; i++) {
				internal[i].setVisible(false);
				if(external[i] != null) {
					external[i].setVisible(false);	//this should never happen
				}
			}
			break;
		case 2:
			//turn on 2 internal composites, 1x2 layout
			for(int i = 0; i < 2; i++) {
				FormData data = new FormData();
				data.left = new FormAttachment(0, 3);
				data.right = new FormAttachment(100, -3);
				data.top = new FormAttachment(i ,2, 3);
				data.bottom = new FormAttachment(i+1,2, -3);
				internal[i].setLayoutData(data);
				internal[i].setVisible(true);
				external[i].setVisible(true);
			}
			for(int i = 2; i < 8; i++) {
				internal[i].setVisible(false);
				if(external[i] != null) {
					external[i].setVisible(false);	//this should never happen
				}
			}
			break;
		case 1:
		case 0:
		default:
			//turn on 1 internal composite, full layout
			FormData data = new FormData();
			data.left = new FormAttachment(0, 0);
			data.right = new FormAttachment(100, 0);
			data.top = new FormAttachment(0, 0);
			data.bottom = new FormAttachment(100, 0);
			internal[maximized].setLayoutData(data);
			internal[maximized].setVisible(true);
			if(external[maximized] != null) {
				external[maximized].setVisible(true);
			}
			for (int i = usedSlotCount; i < 8; i++) {
				if (i != maximized) {
					internal[i].setVisible(false);
					if (external[i] != null) {
						external[i].setVisible(false); // this should never
														// happen
					}
				}
			}
		}
	}
}
