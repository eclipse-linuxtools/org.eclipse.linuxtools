/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/

package org.eclipse.cdt.oprofile.core;

/**
 * A class which represents an Oprofile event
 * @author keiths
 */
public class OpEvent
{
	// The Oprofile event name, i.e., "CPU_CLK_UNHALTED"
	private String _name;
	
	// The Oprofile event number
	private int _number;
	
	 //  A description of the event
	private String _description;

	// Unit masks for this event type
	private OpUnitMask _unitMask;
	
	// Minimum count
	private int _minCount;
	
	/**
	 * Returns the unit mask corresponding to this event.
	 * @return the unit mask
	 */
	public OpUnitMask getUnitMask()
	{		
		return _unitMask;
	}
	
	/**
	 * Sets the unit mask for this event.
	 * @param mask the new unit mask
	 */
	public void setUnitMask(OpUnitMask mask) {
		_unitMask = mask;
	}
		
	/**
	 * Returns the name of this oprofile event.
	 * @return the name
	 */
	public String getText()
	{
		return _name;
	}
	
	/**
	 * Sets the name of this event.
	 * @param text the name
	 */
	public void setText(String text) {
		_name = text;
	}
	
	/**
	 * Returns the description of this oprofile event.
	 * @return the description
	 */
	public String getTextDescription()
	{
		return _description;
	}
	
	/**
	 * Sets the description of this oprofile event.
	 * @param text the description
	 */
	public void setTextDescription(String text) {
		_description = text;
	}
	
	/**
	 * Returns the minimum count allowed for this event.
	 * @return the minimum count
	 */
	public int getMinCount()
	{
		return _minCount;
	}
	
	/**
	 * Sets the minimum count for this event.
	 * @param min the minimum count
	 */
	public void setMinCount(int min) {
		_minCount = min;
	}
		
	/**
	 * Returns oprofile's event number for this event.
	 * @return the event number
	 */
	public int getNumber()
	{
		return _number;
	}
	
	/**
	 * Sets oprofile's event number for this event.
	 * @param num the number
	 */
	public void setNumber(int num) {
		_number = num;
	}
}
