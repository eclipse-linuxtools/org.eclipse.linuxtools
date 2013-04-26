/*******************************************************************************
 * Copyright (c) 2004,2008 Red Hat, Inc.
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
 * A class representing the unit mask that may be associated with oprofile
 * events. Note that since this class was originally written, oprofile unit
 * masks have changed -- a single unit mask may affect several bits at once.
 * Hence, instead of a certain bit being flipped, the specific bits to be changed 
 * are determined by the particular mask's index 
 */
public class OpUnitMask {
	/**
	 * A class which describes an individual unit mask value. Used in XML parsing.
	 */
	public static class MaskInfo {
		/**
		 * The integer value of the mask.
		 */
		public int value;

		/**
		 * A description of the mask.
		 */
		public String description;
	}
	
	public static final int SET_DEFAULT_MASK = -1;

	/**
	 * Invalid mask type.
	 */
	public static final int INVALID = -1;

	/**
	 * The mask is mandatory. It must be used.
	 */
	public static final int MANDATORY = 1;

	/**
	 * The mask is exclusive. Only one of its mask values may be used.
	 */
	public static final int EXCLUSIVE = 2;

	/**
	 * The mask is a bitmask. Any combination of its values may be used.
	 */
	public static final int BITMASK = 3;


	/**
	 *  The current value of this unitmask
	 */
	private int mask;

	/**
	 *  The default mask provided by the oprofile library
	 */
	private int defaultMask;

	/**
	 *  The type of this unitmask
	 */
	private int maskType;

	/**
	 *  Descriptions of the bits of this mask
	 */
	private String[] maskOptionDescriptions = new String[0];

	/**
	 *  mask values -- now bit masks have distinct values (eg: an all of the above)
	 */

	private int[] maskOptionValues;

	/**
	 * Set the descriptions and values for this unitmask's mask options.
	 * Only used from the XML parsers.
	 * @param masks a list of all the mask options
	 */
	public void setMaskDescriptions(MaskInfo[] masks) {
		maskOptionDescriptions = new String[masks.length];
		maskOptionValues = new int[masks.length];

		for (int i = 0; i < masks.length; ++i) {
			maskOptionDescriptions[i] = masks[i].description;
			maskOptionValues[i] = masks[i].value;
		}
	}

	/**
	 * Sets the default value for this unitmask, and initializes
	 *   the current unitmask value to this default.
	 * Only used from the XML parsers.
	 * @param theDefault the default value
	 */
	public void setDefault(int theDefault) {
		defaultMask = theDefault;
		setDefaultMaskValue();	
	}
	
	/**
	 * Sets the unitmask type.
	 * Only used from the XML parsers.
	 * @param type the type
	 */
	public void setType(int type) {
		maskType = type;
	}
	
	/**
	 * Returns the integer value of this unitmask, suitable for passing to oprofile.
	 * @return the integer value
	 */
	public int getMaskValue() {
		return mask;
	}

	/**
	 * Tests whether a particular mask is set in the unitmask value, based on the
	 * value of the mask option at the given index.
	 * 
	 * @param index the index of the mask option to check
	 * @return whether the given mask option's value is set
	 */
	public boolean isMaskSetFromIndex(int index) {
		boolean result = false;

		if (index >= 0 && index < maskOptionValues.length) {
			switch (maskType) {
			case EXCLUSIVE:
				result = (mask == maskOptionValues[index]);
				break;

			case BITMASK:
				result = ((mask & maskOptionValues[index]) != 0);
				break;

			default:
				result = false;
			}
		}

		return result;
	}

	/**
	 * Sets the absolute unitmask value. 
	 * 
	 * @param newValue the new value of this unitmask
	 */
	public void setMaskValue(int newValue) {
		if (newValue == SET_DEFAULT_MASK) {
			mask = defaultMask;
		} else {
			mask = newValue;
		}
	}
	
	/**
	 * Sets the bits of the given mask option's value in the unitmask value.
	 * @param index the index of the mask option to set
	 */
	public void setMaskFromIndex(int index) {
		//mandatory masks only use the default value
		if (index >= 0 && index < maskOptionValues.length) {
			if (maskType == BITMASK)
				mask |= maskOptionValues[index];
			else if (maskType == EXCLUSIVE) {
				mask = maskOptionValues[index];
			}
		}
	}

	/**
	 * Returns the value of the mask based on the unitmask index.
	 * @param index the index of the mask option
	 * @return the mask option's value
	 */
	public int getMaskFromIndex(int index) {
		//mandatory masks only use the default value
		if (maskType == BITMASK) {
			if (index >= 0 && index < maskOptionValues.length) {
				return maskOptionValues[index];
			}
		} else if (maskType == EXCLUSIVE) {
			if (index >= 0 && index < maskOptionValues.length) {
				return maskOptionValues[index];
			}
		} else if (maskType == MANDATORY) {
			return defaultMask;
		}

		//type invalid or unknown, or out of bounds
		return -1;
	}
	
	/**
	 * Unset the bits of the given mask option's value in the unitmask value.
	 * @param index the index of the mask option to set
	 */
	public void unSetMaskFromIndex(int index) {
		if (index >= 0 && index < maskOptionValues.length && maskType == BITMASK) {
			mask = mask & ~maskOptionValues[index];
		}
	}

	/**
	 * Sets the current unitmask value to the default mask value.
	 */
	public void setDefaultMaskValue() {
		mask = defaultMask;
	}

	/**
	 * Returns a description of the requested mask option.
	 * @param num the mask option index
	 * @return the description
	 */
	public String getText(int num) {
		if (num >= 0 && num < maskOptionDescriptions.length)
			return maskOptionDescriptions[num];

		return null;
	}
	
	/**
	 * Returns the number of mask options in this unitmask.
	 * @return the number of mask options
	 */
	public int getNumMasks() {
		return maskOptionDescriptions.length;
	}

	/**
	 * Returns the mask type for this unit mask.
	 * @return <code>BITMASK</code>, <code>EXCLUSIVE</code>, or
	 *         <code>MANDATORY</code>
	 */
	public int getType() {
		return maskType;
	}
}
