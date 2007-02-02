/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/

package org.eclipse.cdt.oprofile.ui.internal;

import org.eclipse.swt.graphics.Image;

/**
 * This is the main interface between the UI displays and the underlying
 * model elements.
 * @author keiths
 */
public interface IProfileElement
{
	// The root element type
	public final int ROOT = 0;
	
	// Element is a session
	public final int SESSION = 1;

	// Element is a root object (object file, executable, shlib)
	public final int OBJECT = 2;
	
	// Element is a sample file
	public final int SAMPLEFILE = 3;

	// Element is a symbol
	public final int SYMBOL = 4;

	// Element is a sample
	public final int SAMPLE = 5;
	
	/**
	 * Returns the "childern" of the current object or null if none.
	 * @return a list of all children
	 */
	IProfileElement[] getChildren();
	
	/**
	 * Does this element have children to be displayed in the tree?
	 * @return whether there are any children to display
	 */
	boolean hasChildren();
	
	/**
	 * Method getParent.
	 * @return the parent element of this element
	 */
	IProfileElement getParent();

	/**
	 * Returns the type of this element
	 * @return the element's type
	 */
	int getType();

	/**
	 * Returns the image to be used when this element is displayed
	 * @return the Image to display
	 */
	Image getLabelImage();

	/**
	 * Returns the text to be displayed when this element is displayed
	 * @return the text to display
	 */
	String getLabelText();

	/*
	 * FIXME: Most of the below should probably go into a separate interface
	 * specifically for the SampleView.
	 */
	 	
	/**
	 * Gets the total sample count for this element and all of its children
	 * @return the sample count
	 */
	int getSampleCount();
	
	/**
	 * Returns the line number that the sample occurs on, if known; zero, otherwise
	 * @return the line number
	 */
	int getLineNumber();
	
	/**
	 * Returns the filename to display in the "File" column. For Samples, this
	 * is the containing source file. For SampleFiles, it is the executable name. For
	 * sessions, it is the name of the session.
	 * @return String
	 */
	String getFileName();
	
	/**
	 * Returns the address of the element. For Samples, it is the sample's VMA.
	 * For Symbols, it is the start address of the symbol. For all others, it is whatever
	 * is desired to be displayed in the SampleView "VMA" column (usually blank).
	 * @return the string to be displayed in the "VMA" column
	 */
	String getAddress();
	
	/**
	 * Returns a unique ID for this element that may be used to search for it
	 * @return the ID
	 */
	String getId();
	
	/**
	 * Get the IProfileElement with the given ID.
	 * @param id	the ID of the desired element	
	 * @return the element with the given ID or null if not found
	 */
	IProfileElement getElementFromId(String id);
}
