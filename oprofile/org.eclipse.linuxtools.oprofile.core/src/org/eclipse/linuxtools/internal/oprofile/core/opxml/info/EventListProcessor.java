/*******************************************************************************
 * Copyright (c) 2004,2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *    Kent Sebastian <ksebasti@redhat.com>
 *******************************************************************************/ 
package org.eclipse.linuxtools.internal.oprofile.core.opxml.info;

import java.util.ArrayList;

import org.eclipse.linuxtools.internal.oprofile.core.daemon.OpEvent;
import org.eclipse.linuxtools.internal.oprofile.core.daemon.OpUnitMask;
import org.eclipse.linuxtools.internal.oprofile.core.opxml.OprofileSAXHandler;
import org.eclipse.linuxtools.internal.oprofile.core.opxml.XMLProcessor;
import org.xml.sax.Attributes;


/**
 * XML handler class for opxml's "event-list".
 * @see org.eclipse.linuxtools.internal.oprofile.core.opxml.OpxmlRunner
 */
public class EventListProcessor extends XMLProcessor {
	// The current event being constructed
	private OpEvent _currentEvent;
	private int _counter;
	private ArrayList<OpEvent> _currentEventList;
	
	// An XML processor for reading the unit mask information for an event
	private UnitMaskProcessor _umProcessor;
	
	// XML elements recognized by this processor
	private static final String _EVENT_TAG = "event"; //$NON-NLS-1$
	private static final String _UNIT_MASK_TAG = "unit-mask"; //$NON-NLS-1$
	private static final String _NAME_TAG = "name"; //$NON-NLS-1$
	private static final String _VALUE_TAG = "value"; //$NON-NLS-1$
	private static final String _DESCRIPTION_TAG = "description"; //$NON-NLS-1$
	private static final String _MASK_TAG = "mask"; //$NON-NLS-1$
	private static final String _MINIMUM_COUNT_TAG = "minimum"; //$NON-NLS-1$
	private static final String _ATTR_EVENT_LIST_COUNTER = "counter"; //$NON-NLS-1$
	
	// This is a special processor which is used to deal with a single mask value
	private class MaskProcessor extends XMLProcessor {
		private OpUnitMask.MaskInfo _info;
		
		/**
		 * @see org.eclipse.linuxtools.internal.oprofile.core.XMLProcessor#reset()
		 */
		public void reset(Object callData) {
			_info = new OpUnitMask.MaskInfo();
		}
		
		/**
		 * @see org.eclipse.linuxtools.internal.oprofile.core.XMLProcessor#endElement(String)
		 */
		public void endElement(String name, Object callData) {
			if (name.equals(_VALUE_TAG)) {
				// Set mask's value
				_info.value = Integer.parseInt(_characters);
			} else if (name.equals(_DESCRIPTION_TAG)) {
				_info.description = _characters;
			} else if (name.equals(_MASK_TAG)) {
				// Pop and pass mask tag to previous processor (UnitMaskProcessor)
				OprofileSAXHandler.getInstance(callData).pop(_MASK_TAG);
			}
		}

		/**
		 * Returns the information that has been collected about a mask.
		 * @return the mask information
		 */
		public OpUnitMask.MaskInfo getResult() {
			return _info;
		}
	}
	
	// This is a special processor to handle unit mask information
	private class UnitMaskProcessor extends XMLProcessor {
		// An ArrayList to hold all the valid masks for a unit mask.
		private ArrayList<OpUnitMask.MaskInfo> _masks;
		
		// The unit mask being constructed
		private OpUnitMask _unitMask;
		
		// An XML processor for each individual mask value.
		private MaskProcessor _maskProcessor;
		
		// XML elements recognized by this processor
		private static final String _MASK_TYPE_TAG = "type"; //$NON-NLS-1$
		private static final String _MASK_DEFAULT_TAG = "default"; //$NON-NLS-1$
		private static final String _MASK_TYPE_BITMASK = "bitmask"; //$NON-NLS-1$
		private static final String _MASK_TYPE_MANDATORY = "mandatory"; //$NON-NLS-1$
		private static final String _MASK_TYPE_EXCLUSIVE = "exclusive"; //$NON-NLS-1$
		
		/**
		 * Constructor for UnitMaskProcessor. Initializes internal state.
		 */
		public UnitMaskProcessor() {
			super();
			_maskProcessor = new MaskProcessor();
			_masks = new ArrayList<OpUnitMask.MaskInfo>();
		}
		
		/**
		 * @see org.eclipse.linuxtools.internal.oprofile.core.XMLProcessor#reset()
		 */
		public void reset(Object callData) {
			_unitMask = new OpUnitMask();
			_masks.clear();
		}
		
		/**
		 * @see org.eclipse.linuxtools.internal.oprofile.core.XMLProcessor#startElement(String, Attributes)
		 */
		public void startElement(String name, Attributes attrs, Object callData) {
			if (name.equals(_MASK_TAG)) {
				// Tell SAX handler to use the mask processor
				OprofileSAXHandler.getInstance(callData).push(_maskProcessor);
			} else {
				super.startElement(name, attrs, callData);
			}
		}
		
		/**
		 * @see org.eclipse.linuxtools.internal.oprofile.core.XMLProcessor#endElement(String)
		 */
		public void endElement(String name, Object callData) {
			if (name.equals(_MASK_TYPE_TAG)) {
				// Set the mask type
				_unitMask._setType(_getTypeFromString(_characters));
			} else if (name.equals(_MASK_DEFAULT_TAG)) {
				// Set the default mask
				_unitMask._setDefault(Integer.parseInt(_characters));
			} else if (name.equals(_MASK_TAG)) {
				// Add this mask description to the list of all masks
				_masks.add(_maskProcessor.getResult());
			} else if (name.equals(_UNIT_MASK_TAG)) {
				// All done. Add the known masks to the unit mask
				OpUnitMask.MaskInfo[] descs = new OpUnitMask.MaskInfo[_masks.size()];
				_masks.toArray(descs);
				_unitMask._setMaskDescriptions(descs);
				
				// Pop this processor and pass _UNIT_MASK_TAG to previoius processor
				OprofileSAXHandler.getInstance(callData).pop(_UNIT_MASK_TAG);
			}
		}
				
		/**
		 * Returns the constructed unit mask.
		 * @return the unit mask
		 */
		public OpUnitMask getResult() {
			return _unitMask;
		}
		
		// Converts a string representing a mask type into an integer
		private int _getTypeFromString(String string) {
			if (string.equals(_MASK_TYPE_MANDATORY)) {
				return OpUnitMask.MANDATORY;
			} else if (string.equals(_MASK_TYPE_BITMASK)) {
				return OpUnitMask.BITMASK;
			} else if (string.equals(_MASK_TYPE_EXCLUSIVE)) {
				return OpUnitMask.EXCLUSIVE;
			}

			return -1;
		}
	};
	
	/**
	 * Constructor for EventListProcessor. Initializes internal state.
	 */
	public EventListProcessor() {
		super();
		_umProcessor = new UnitMaskProcessor();
	}
	
	@Override
	public void reset(Object callData) {
		_currentEventList = new ArrayList<OpEvent>();
	}
	
	/**
	 * @see org.eclipse.linuxtools.internal.oprofile.core.XMLProcessor#startElement(String, Attributes)
	 */
	public void startElement(String name, Attributes attrs, Object callData) {
		if (name.equals(_EVENT_TAG)) {
			// new event
			_currentEvent = new OpEvent();
		} else if (name.equals(_UNIT_MASK_TAG)) {
			// Tell the SAX handler to use the unit mask processor
			OprofileSAXHandler.getInstance(callData).push(_umProcessor);
		} else if (name.equals(OpInfoProcessor.EVENT_LIST_TAG)) {
			// Our start tag: grab the counter number from the attributes
			_counter = Integer.parseInt(attrs.getValue(_ATTR_EVENT_LIST_COUNTER));
		} else {
			super.startElement(name, attrs, callData);
		}
	}
	
	/**
	 * @see org.eclipse.linuxtools.internal.oprofile.core.XMLProcessor#endElement(String)
	 */
	public void endElement(String name, Object callData) {
		if (name.equals(_EVENT_TAG)) {
			// Finished constructing an event. Add it to the list.
			_currentEventList.add(_currentEvent);
		} else if (name.equals(_UNIT_MASK_TAG)) {
			// Set the event's unit mask
			_currentEvent._setUnitMask(_umProcessor.getResult());
		} else if (name.equals(_NAME_TAG)) {
			// Set event's name
			_currentEvent._setText(_characters);
		} else if (name.equals(_DESCRIPTION_TAG)) {
			// Set event's description
			_currentEvent._setTextDescription(_characters);
		} else if (name.equals(_MINIMUM_COUNT_TAG)) {
			// Set event's minimum count
			_currentEvent._setMinCount(Integer.parseInt(_characters));
		} else if (name.equals(OpInfoProcessor.EVENT_LIST_TAG)) {
			OprofileSAXHandler.getInstance(callData).pop(name);
		}
	}
	
	public int getCounterNum() {
		return _counter;
	}
	
	public OpEvent[] getEvents() {
		OpEvent[] events = new OpEvent[_currentEventList.size()];
		_currentEventList.toArray(events);
		return events;
	}
}
