/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/

package org.eclipse.cdt.oprofile.ui.internal;

import java.util.StringTokenizer;

/**
 * This is a least-common denominator implementation of IProfileElement. It
 * takes care of some of the more mundane tasks (IDs, parent relationships).
 * @author keiths
 */
public abstract class ProfileElement implements IProfileElement
{
	protected static final String SEPARATOR_CHAR = ";"; //$NON-NLS-1$

	// parent object
	protected IProfileElement _parent;

	// The type of this element
	private final int _type;
	
	/**
	 * Constructor for ProfileElement.
	 */
	public ProfileElement(IProfileElement parent, int type)
	{
		_parent = parent;
		_type = type;
	}
	
	/**
	 * @see org.eclipse.cdt.oprofile.ui.internal.IProfileElement#getParent()
	 */
	public IProfileElement getParent()
	{
		return _parent;
	}

	/**
	 * @see org.eclipse.cdt.oprofile.ui.internal.IProfileElement#getType()
	 */
	public int getType()
	{
		return _type;
	}
		
	/**
	 * @see org.eclipse.cdt.oprofile.ui.internal.IProfileElement#getSampleCount()
	 */
	public abstract int getSampleCount();
	
	/**
	 * @see org.eclipse.cdt.oprofile.ui.internal.IProfileElement#getLineNumber()
	 */
	public int getLineNumber()
	{
		return 0;
	}
	
	// This is the id of this object; it does not include parents. Classes
	// which subclass this class can override this when the getLabelText
	// displays something too complex for identification.
	protected String _myId()
	{
		return getLabelText();
	}
	
	/**
	 * @see org.eclipse.cdt.oprofile.ui.internal.IProfileElement#getFileName()
	 */
	public String getFileName()
	{
		return getLabelText();
	}

	/**
	 * @see org.eclipse.cdt.oprofile.ui.internal.IProfileElement#getAddress()
	 */
	public String getAddress()
	{
		return null;
	}
	
	
	/**
	 * @see org.eclipse.cdt.oprofile.ui.internal.IProfileElement#getId()
	 */
	public String getId()
	{
		if (getParent() == null)
 			return _myId() + SEPARATOR_CHAR;
		
		return getParent().getId() + _myId() + SEPARATOR_CHAR;
	}
	
	/**
	 * @see org.eclipse.cdt.oprofile.ui.internal.IProfileElement#getElementFromId(String)
	 */
	public IProfileElement getElementFromId(String id)
	{
		IProfileElement object;
		StringTokenizer st = new StringTokenizer(id, SEPARATOR_CHAR);
		String objId = new String();

		object = this;
		while (st.hasMoreTokens())
		{
			IProfileElement[] children = object.getChildren();
			objId += st.nextToken() + SEPARATOR_CHAR;
			for (int i = 0; i < children.length; ++i)
			{
				if (objId.equals(children[i].getId()))
				{
					object = children[i];
					break;
				}
			}
		}
		
		return (id.equals(object.getId()) ? object : null);
	}
}
