/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/

package org.eclipse.cdt.oprofile.core;

/**
 * A class representing the unit mask that may be associated with oprofile
 * events. Note that mandatory masks are hidden from the user, so they are
 * special-cased throughout the code.
 * @author Keith Seitz <keiths@redhat.com>
 */
public class OpUnitMask
{
	/**
	 * A class which describes an individual unit mask value.
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

	/*
	 * The types of masks
	 */
	 
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
	
	// The current value of this unit mask
	private int _mask;
	
	// The default mask provided by the oprofile library
	private int _defaultMask;
	
	// The type of this mask
	private int _maskType;

	// Descriptions of the bits of this mask
	private String[] _descriptions = new String[0];

	// EXCLUSIVE mask values
	private int[] _exclusiveValues;
	
	/**
	 * Returns the integer value of this unit mask, suitable for passing
	 * to oprofile.
	 * @return the integer value
	 */
	public int getMaskValue()
	{		
		return _mask;
	}

	/**
	 * Tests whether a particular mask is set in the unit mask.
	 * @param num	which mask value to check
	 * @return whether the given mask is set
	 */
	public boolean isSet(int num)
	{
		boolean result = false;
		
		if (num <= _descriptions.length)
		{
			switch (_maskType)
			{
				case EXCLUSIVE:
					result = (_mask == _exclusiveValues[num]);
					break;
					
				case BITMASK:
					result = ((_mask & (1 << num)) != 0);
					break;
					
				default:
					result = false;
			}
		}
		
		return result;
	}

	/**
	 * Set the given mask.
	 * @param newMask	BITMASK: the bit number to set; EXCLUSIVE: index mask to set
	 */
	public void setMask(int newMask)
	{
		// Not permitted for mandatory masks
		if (_maskType != MANDATORY)
		{
			if (_maskType == BITMASK)
				_mask |= (1 << newMask);
			else
			{
				// With EXCLUSIVE masks, each mask has a different value.
				// newMask will be an index (i.e., the newMask'th mask is to be set)
				_mask = _exclusiveValues[newMask];
			}
		}
	}
	
	/**
	 * Set the mask value descriptions for this unit mask.
	 * @param masks a list of all the mask values
	 */
	public void setMaskDescriptions(MaskInfo[] masks) {
		_descriptions = new String[masks.length];
		_exclusiveValues = new int[masks.length];
		
		for (int i = 0; i < masks.length; ++i) {
			_descriptions[i] = masks[i].description;
			_exclusiveValues[i] = masks[i].value;
		}
	}
	
	/**
	 * Sets the absolute mask value.
	 * @param newValue	the new value of this mask
	 */
	public void setMaskValue(int newValue)
	{
		_mask = newValue;
	}
	
	/**
	 * Returns a description of the request mask value.
	 * @param num	the bit number/index
	 * @return the description
	 */
	public String getText(int num)
	{
		if (num <= _descriptions.length)
			return _descriptions[num];

		return null;
	}

	/**
	 * Returns the number of mask values in this unit mask.
	 * Note that <code>MANDATORY</code> masks are exluded from this count.
	 * @return the number of mask values
	 */
	public int numMasks()
	{
		int nMasks = 0;
		
		if (_maskType == MANDATORY)
				nMasks = 0;
		else
				nMasks = _descriptions.length;
				
		return nMasks;
	}
	
	/**
	 * Sets the default value for this unit mask.
	 * @param theDefault the default value
	 */
	public void setDefault(int theDefault) {
		_defaultMask = theDefault;
	}
	
	/**
	 * Returns the mask type for this unit mask.
	 * @return <code>BITMASK</code>, <code>EXCLUSIVE</code>, or <code>MANDATORY</code>
	 */
	public int getType() {
		return _maskType;
	}
	
	/**
	 * Sets the unit mask type.
	 * @param type the type
	 */
	public void setType(int type) {
		_maskType = type;
	}
}
