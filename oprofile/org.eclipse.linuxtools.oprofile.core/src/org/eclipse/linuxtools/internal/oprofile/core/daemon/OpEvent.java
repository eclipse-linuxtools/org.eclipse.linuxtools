/*******************************************************************************
 * Copyright (c) 2004, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *    Kent Sebastian <ksebasti@redhat.com>
 *******************************************************************************/

package org.eclipse.linuxtools.internal.oprofile.core.daemon;

/**
 * A class which represents an Oprofile event
 */
public class OpEvent {
    /**
     *  The Oprofile event name, i.e., "CPU_CLK_UNHALTED"
     */
    private String name;

     /**
      *   A description of the event
      */
    private String description;

    /**
     * Unit masks for this event type
     */
    private OpUnitMask unitMask;

    /**
     *  Minimum count
     */
    private int minCount;

    /**
     * Sets the unit mask for this event.
     * Only called from XML parsers.
     * @param mask the new unit mask
     */
    public void setUnitMask(OpUnitMask mask) {
        unitMask = mask;
    }

    /**
     * Sets the name of this event.
     * Only called from XML parsers.
     * @param text the name
     */
    public void setText(String text) {
        name = text;
    }

    /**
     * Sets the description of this oprofile event.
     * Only called from XML parsers.
     * @param text the description
     */
    public void setTextDescription(String text) {
        description = text;
    }

    /**
     * Sets the minimum count for this event.
     * Only called from XML parsers.
     * @param min the minimum count
     */
    public void setMinCount(int min) {
        minCount = min;
    }

    /**
     * Returns the unit mask corresponding to this event.
     * @return the unit mask
     */
    public OpUnitMask getUnitMask() {
        return unitMask;
    }

    /**
     * Returns the name of this oprofile event.
     * @return the name
     */
    public String getText() {
        return name;
    }

    /**
     * Returns the description of this oprofile event.
     * @return the description
     */
    public String getTextDescription() {
        return description;
    }

    /**
     * Returns the minimum count allowed for this event.
     * @return the minimum count
     */
    public int getMinCount() {
        return minCount;
    }
}
