/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/

package org.eclipse.cdt.oprofile.core;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.cdt.oprofile.core.opxml.HeaderProcessor;


/**
 * A class which represents an image (executables,
 * libraries, modules) profile by OProfile.
 * @author Keith Seitz  <keiths@redhat.com>
 */
public class ProfileImage extends SampleContainer {
	// ProfileImage Header
	public class Header {
		// The cpu type ("PIII")
		public String cpuType;
		
		// The counter reset value
		public int count;
		
		// The event collected
		public String event;
		
		// The unit mask used during collection
		public int unitMask;
		
		// The approximate cpu speed in MHz
		public float cpuSpeed;
	}
	
	// The header of this image
	protected Header _header = null;
		
	// The count of all samples in this container
	protected int _count = -1;
	
	// The actual OProfile samplefile
	protected String _samplefile;
	
	/**
	 * Constructor ProfileImage.
	 * @param filename the disk filename of this ProfileImage
	 */
	public ProfileImage(String filename) {
		super(new File(filename));
	}
	
	/**
	 * @see org.eclipse.cdt.oprofile.core.ISampleContainer#getSampleCount()
	 */
	public int getSampleCount() {
		if (_count < 0) {
			_count = 0;
			for (Iterator i = _samples.iterator(); i.hasNext(); ) {
				Sample s = (Sample) i.next();
				_count += s.getSampleCount();
			}
		}
		
		return _count;
	}
		
	/**
	 * Set the header for this ProfileImage.
	 * @param map the map of values from the HeaderProcessor
	 */
	public void setHeader(HashMap map) {
		_header = new Header();
		_header.event = (String) map.get(HeaderProcessor.EVENT);
		_header.count = Integer.parseInt((String) map.get(HeaderProcessor.COUNT));
		_header.cpuSpeed = Float.parseFloat((String) map.get(HeaderProcessor.CPU_SPEED));
		_header.cpuType = (String) map.get(HeaderProcessor.CPU_TYPE);
		_header.unitMask = Integer.parseInt((String) map.get(HeaderProcessor.UNIT_MASK));
	}

	/**
	 * Fetches the debug info for this samplefile's samples
	 */
	public void getDebugInfo() {
		Oprofile.getDebugInfo(this);
	}
	
	/**
	 * Sets the Oprofile samplefile for this image.
	 * @param sfile the OProfile samplefile
	 */
	public void setSampleFile(String sfile) {
		_samplefile = sfile;
	}
	
	/**
	 * Returns the actual disk image of the OProfile samplefile
	 * for this ProfileImage.
	 * @return the OProfile samplefile
	 */
	public String getSampleFile() {
		return _samplefile;
	}
}
