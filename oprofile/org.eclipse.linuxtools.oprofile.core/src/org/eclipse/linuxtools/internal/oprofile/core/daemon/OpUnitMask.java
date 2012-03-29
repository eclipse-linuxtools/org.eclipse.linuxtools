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
	};
	
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


	// The current value of this unitmask
	private int _mask;

	// The default mask provided by the oprofile library
	private int _defaultMask;

	// The type of this unitmask
	private int _maskType;

	// Descriptions of the bits of this mask
	private String[] _maskOptionDescriptions = new String[0];

	// mask values -- now bit masks have distinct values (eg: an all of the
	// above)
	private int[] _maskOptionValues;

	/**
	 * Set the descriptions and values for this unitmask's mask options.
	 * Only used from the XML parsers.
	 * @param masks a list of all the mask options
	 */
	public void _setMaskDescriptions(MaskInfo[] masks) {
		_maskOptionDescriptions = new String[masks.length];
		_maskOptionValues = new int[masks.length];

		for (int i = 0; i < masks.length; ++i) {
			_maskOptionDescriptions[i] = masks[i].description;
			_maskOptionValues[i] = masks[i].value;
		}
	}

	/**
	 * Sets the default value for this unitmask, and initializes
	 *   the current unitmask value to this default.
	 * Only used from the XML parsers.
	 * @param theDefault the default value
	 */
	public void _setDefault(int theDefault) {
		_defaultMask = theDefault;
		setDefaultMaskValue();	
	}
	
	/**
	 * Sets the unitmask type.
	 * Only used from the XML parsers.
	 * @param type the type
	 */
	public void _setType(int type) {
		_maskType = type;
	}
	
	/**
	 * Returns the integer value of this unitmask, suitable for passing to oprofile.
	 * @return the integer value
	 */
	public int getMaskValue() {
		return _mask;
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

		if (index >= 0 && index < _maskOptionValues.length) {
			switch (_maskType) {
			case EXCLUSIVE:
				result = (_mask == _maskOptionValues[index]);
				break;

			case BITMASK:
				result = ((_mask & _maskOptionValues[index]) != 0);
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
			_mask = _defaultMask;
		} else {
			_mask = newValue;
		}
	}
	
	/**
	 * Sets the bits of the given mask option's value in the unitmask value.
	 * @param index the index of the mask option to set
	 */
	public void setMaskFromIndex(int index) {
		//mandatory masks only use the default value
		if (index >= 0 && index < _maskOptionValues.length) {
			if (_maskType == BITMASK)
				_mask |= _maskOptionValues[index];
			else if (_maskType == EXCLUSIVE) {
				_mask = _maskOptionValues[index];
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
		if (_maskType == BITMASK) {
			if (index >= 0 && index < _maskOptionValues.length) {
				return _maskOptionValues[index];
			}
		} else if (_maskType == EXCLUSIVE) {
			if (index >= 0 && index < _maskOptionValues.length) {
				return _maskOptionValues[index];
			}
		} else if (_maskType == MANDATORY) {
			return _defaultMask;
		}

		//type invalid or unknown, or out of bounds
		return -1;
	}
	
	/**
	 * Unset the bits of the given mask option's value in the unitmask value.
	 * @param index the index of the mask option to set
	 */
	public void unSetMaskFromIndex(int index) {
		if (index >= 0 && index < _maskOptionValues.length && _maskType == BITMASK) {
			_mask = _mask & ~_maskOptionValues[index];
		}
	}

	/**
	 * Sets the current unitmask value to the default mask value.
	 */
	public void setDefaultMaskValue() {
		_mask = _defaultMask;
	}

	/**
	 * Returns a description of the requested mask option.
	 * @param num the mask option index
	 * @return the description
	 */
	public String getText(int num) {
		if (num >= 0 && num < _maskOptionDescriptions.length)
			return _maskOptionDescriptions[num];

		return null;
	}
	
	/**
	 * Returns the number of mask options in this unitmask.
	 * @return the number of mask options
	 */
	public int getNumMasks() {
		return _maskOptionDescriptions.length;
	}

	/**
	 * Returns the mask type for this unit mask.
	 * @return <code>BITMASK</code>, <code>EXCLUSIVE</code>, or
	 *         <code>MANDATORY</code>
	 */
	public int getType() {
		return _maskType;
	}
}
