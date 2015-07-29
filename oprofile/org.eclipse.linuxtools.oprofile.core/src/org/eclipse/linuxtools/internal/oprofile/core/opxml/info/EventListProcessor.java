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
    private OpEvent currentEvent;
    private int counter;
    private ArrayList<OpEvent> currentEventList;

    // An XML processor for reading the unit mask information for an event
    private UnitMaskProcessor umProcessor;

    // XML elements recognized by this processor
    private static final String EVENT_TAG = "event"; //$NON-NLS-1$
    private static final String UNIT_MASK_TAG = "unit-mask"; //$NON-NLS-1$
    private static final String NAME_TAG = "name"; //$NON-NLS-1$
    private static final String VALUE_TAG = "value"; //$NON-NLS-1$
    private static final String DESCRIPTION_TAG = "description"; //$NON-NLS-1$
    private static final String MASK_TAG = "mask"; //$NON-NLS-1$
    private static final String MINIMUM_COUNT_TAG = "minimum"; //$NON-NLS-1$
    private static final String ATTR_EVENT_LIST_COUNTER = "counter"; //$NON-NLS-1$

    // This is a special processor which is used to deal with a single mask value
    private static class MaskProcessor extends XMLProcessor {
        private OpUnitMask.MaskInfo info;

        @Override
        public void reset(Object callData) {
            info = new OpUnitMask.MaskInfo();
        }

        @Override
        public void endElement(String name, Object callData) {
            if (name.equals(VALUE_TAG)) {
                // Set mask's value
                info.value = Integer.parseInt(characters);
            } else if (name.equals(DESCRIPTION_TAG)) {
                info.description = characters;
            } else if (name.equals(MASK_TAG)) {
                // Pop and pass mask tag to previous processor (UnitMaskProcessor)
                OprofileSAXHandler.getInstance(callData).pop(MASK_TAG);
            }
        }

        /**
         * Returns the information that has been collected about a mask.
         * @return the mask information
         */
        public OpUnitMask.MaskInfo getResult() {
            return info;
        }
    }

    // This is a special processor to handle unit mask information
    private class UnitMaskProcessor extends XMLProcessor {
        // An ArrayList to hold all the valid masks for a unit mask.
        private ArrayList<OpUnitMask.MaskInfo> masks;

        // The unit mask being constructed
        private OpUnitMask unitMask;

        // An XML processor for each individual mask value.
        private MaskProcessor maskProcessor;

        // XML elements recognized by this processor
        private static final String MASK_TYPE_TAG = "type"; //$NON-NLS-1$
        private static final String MASK_DEFAULT_TAG = "default"; //$NON-NLS-1$
        private static final String MASK_TYPE_BITMASK = "bitmask"; //$NON-NLS-1$
        private static final String MASK_TYPE_MANDATORY = "mandatory"; //$NON-NLS-1$
        private static final String MASK_TYPE_EXCLUSIVE = "exclusive"; //$NON-NLS-1$

        /**
         * Constructor for UnitMaskProcessor. Initializes internal state.
         */
        public UnitMaskProcessor() {
            super();
            maskProcessor = new MaskProcessor();
            masks = new ArrayList<>();
        }

        @Override
        public void reset(Object callData) {
            unitMask = new OpUnitMask();
            masks.clear();
        }

        @Override
        public void startElement(String name, Attributes attrs, Object callData) {
            if (name.equals(MASK_TAG)) {
                // Tell SAX handler to use the mask processor
                OprofileSAXHandler.getInstance(callData).push(maskProcessor);
            } else {
                super.startElement(name, attrs, callData);
            }
        }

        @Override
        public void endElement(String name, Object callData) {
            if (name.equals(MASK_TYPE_TAG)) {
                // Set the mask type
                unitMask.setType(getTypeFromString(characters));
            } else if (name.equals(MASK_DEFAULT_TAG)) {
                // Set the default mask
                unitMask.setDefault(Integer.parseInt(characters));
            } else if (name.equals(MASK_TAG)) {
                // Add this mask description to the list of all masks
                masks.add(maskProcessor.getResult());
            } else if (name.equals(UNIT_MASK_TAG)) {
                // All done. Add the known masks to the unit mask
                OpUnitMask.MaskInfo[] descs = new OpUnitMask.MaskInfo[masks.size()];
                masks.toArray(descs);
                unitMask.setMaskDescriptions(descs);

                // Pop this processor and pass _UNIT_MASK_TAG to previoius processor
                OprofileSAXHandler.getInstance(callData).pop(UNIT_MASK_TAG);
            }
        }

        /**
         * Returns the constructed unit mask.
         * @return the unit mask
         */
        public OpUnitMask getResult() {
            return unitMask;
        }

        // Converts a string representing a mask type into an integer
        private int getTypeFromString(String string) {
            if (string.equals(MASK_TYPE_MANDATORY)) {
                return OpUnitMask.MANDATORY;
            } else if (string.equals(MASK_TYPE_BITMASK)) {
                return OpUnitMask.BITMASK;
            } else if (string.equals(MASK_TYPE_EXCLUSIVE)) {
                return OpUnitMask.EXCLUSIVE;
            }

            return -1;
        }
    }

    /**
     * Constructor for EventListProcessor. Initializes internal state.
     */
    public EventListProcessor() {
        super();
        umProcessor = new UnitMaskProcessor();
    }

    @Override
    public void reset(Object callData) {
        currentEventList = new ArrayList<>();
    }

    @Override
    public void startElement(String name, Attributes attrs, Object callData) {
        if (name.equals(EVENT_TAG)) {
            // new event
            currentEvent = new OpEvent();
        } else if (name.equals(UNIT_MASK_TAG)) {
            // Tell the SAX handler to use the unit mask processor
            OprofileSAXHandler.getInstance(callData).push(umProcessor);
        } else if (name.equals(OpInfoProcessor.EVENT_LIST_TAG)) {
            // Our start tag: grab the counter number from the attributes
            counter = Integer.parseInt(attrs.getValue(ATTR_EVENT_LIST_COUNTER));
        } else {
            super.startElement(name, attrs, callData);
        }
    }

    @Override
    public void endElement(String name, Object callData) {
        if (name.equals(EVENT_TAG)) {
            // Finished constructing an event. Add it to the list.
            currentEventList.add(currentEvent);
        } else if (name.equals(UNIT_MASK_TAG)) {
            // Set the event's unit mask
            currentEvent.setUnitMask(umProcessor.getResult());
        } else if (name.equals(NAME_TAG)) {
            // Set event's name
            currentEvent.setText(characters);
        } else if (name.equals(DESCRIPTION_TAG)) {
            // Set event's description
            currentEvent.setTextDescription(characters);
        } else if (name.equals(MINIMUM_COUNT_TAG)) {
            // Set event's minimum count
            currentEvent.setMinCount(Integer.parseInt(characters));
        } else if (name.equals(OpInfoProcessor.EVENT_LIST_TAG)) {
            OprofileSAXHandler.getInstance(callData).pop(name);
        }
    }

    public int getCounterNum() {
        return counter;
    }

    public OpEvent[] getEvents() {
        OpEvent[] events = new OpEvent[currentEventList.size()];
        currentEventList.toArray(events);
        return events;
    }
}
