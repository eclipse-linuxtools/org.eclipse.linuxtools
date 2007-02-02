/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/

package org.eclipse.cdt.oprofile.core;

/**
 * A sample from Oprofile.
 * @author Keith Seitz <keiths@redhat.com>
 */
public class Sample
{
	// A class describing a symbol that may be associated with a sample
	public static class Symbol
	{
		// The name of the symbol
		public String name;
		
		// The start address of the symbol
		public String startAddress;
	};
	
	// A class describing the debug info that may exist for a symbol
	public static class DebugInfo
	{
		// The address for this info
		public String address;
		
		// The line number for the given VMA of the sample
		public int lineNumber;
		
		// The function containing the sample
		public String function;
		
		// The source file containing the sample
		public String sourceFilename;
		
		public DebugInfo() {
			address = new String();
			lineNumber = 0;
			function = new String();
			sourceFilename = new String();
		}
	};
	
	// The address of this sample
	private String _address;
	
	// The symbol information for this sample
	private Symbol _symbol = null;
	
	// The debug info associated with the symbol
	private DebugInfo _info = null;

	// The total number of samples at this address
	private int _count;
	
	// The containing image file
	private ProfileImage _image;
	
	/**
	 * Constructor.
	 * @param file the ProfileImage from which this sample came
	 */
	public Sample(ProfileImage image) {
		_count = 0;
		_symbol = null;
		_image = image;
	}
	
	/**
	 * Returns whether this sample has any symbol associated with it.
	 * @return whether this sample has any symbol associated with it
	 */
	public boolean hasSymbol()
	{
		return (_symbol != null);
	}
	
	/**
	 * Returns the number of samples taken at this address
	 * @return the number of samples
	 */
	public int getSampleCount ()
	{
		return _count;
	}
	
	/**
	 * Set the count for this sample.
	 * @param count the count
	 */
	public void setCount(int count) {
		_count = count;
	}
	
	/**
	 * Returns the symbol associated with this samples (or null).
	 * @return the symbol
	 */
	public Symbol getSymbol()
	{
		return _symbol;
	}
	
	/**
	 * Set the symbol for this sample.
	 * @param sym the symbol
	 */
	public void setSymbol(Symbol sym) {
		_symbol = sym;
	}
	
	/**
	 * Returns the address of this sample as a string.
	 * @return the address
	 */
	public String getAddress()
	{
		return _address;
	}
	
	/**
	 * Set the address of this sample.
	 * @param addr the address
	 */
	public void setAddress(String addr) {
		_address = addr;
	}
	
	/**
	 * Returns the source filename which contains this address
	 * @return the source filename
	 */
	public String getFilename()
	{
		if (_info == null)
			_getDebugInfo();

		return _info.sourceFilename;
	}
	
	/**
	 * Returns the line number of this sample in its source file.
	 * @return the line number
	 */
	public int getLineNumber()
	{
		if (_info == null)
			_getDebugInfo();
			
		return _info.lineNumber;
	}
	
	/**
	 * Sets the debug information for this sample.
	 * @param info the debug information
	 */
	public void setDebugInfo(DebugInfo info) {
		_info = info;
	}
	
	// Fetches the debug info for the sample
	private void _getDebugInfo ()
	{
		// Set default debug info for this sample. When the debug info is fetched,
		// it will overwrite this default with the real thing via setDebugInfo.
		_info = new DebugInfo();
		_image.getDebugInfo();
	}
}
