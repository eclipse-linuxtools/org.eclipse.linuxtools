/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/

package org.eclipse.cdt.oprofile.core.opxml;

import java.util.HashMap;
import java.util.Stack;

import org.eclipse.cdt.oprofile.core.ProfileImage;
import org.eclipse.cdt.oprofile.core.Sample;
import org.eclipse.cdt.oprofile.core.SampleSession;
import org.eclipse.core.runtime.IProgressMonitor;
import org.xml.sax.Attributes;


/**
 * A processor for opxml samples.
 * @author Keith Seitz  <keiths@redhat.com>
 */
public class SamplesProcessor extends XMLProcessor {
	/**
	 * The result of the SamplesProcessor. This should be passed as calldata
	 * by the caller.
	 */
	public static class CallData {
		public SampleSession session;
		public IProgressMonitor monitor;

		public CallData(IProgressMonitor mon, SampleSession ses) {
			monitor = mon;
			session = ses;
		}
	}

	// XML tags parsed by this processor
	public static final String HEADER_TAG = "header"; //$NON-NLS-1$
	public static final String SAMPLE_TAG = "sample"; //$NON-NLS-1$
	public static final String IMAGE_TAG = "image"; // $NON-NLS-1$
	public static final String SAMPLEFILE_TAG = "samplefile"; //$NON-NLS-1$
	private static final String _ATTR_FILENAME = "name"; //$NON-NLS-1$
	
	// The session into which to save results (from CallData)
	private SampleSession _session;
	
	private HeaderProcessor _headerProcessor = new HeaderProcessor();
	private SampleProcessor _sampleProcessor = new SampleProcessor();

	// Current image being constructed
	private ProfileImage _currentImage;
	
	// Stack of images being constructed
	private Stack _imageStack;
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.oprofile.core.opxml.XMLProcessor#reset(java.lang.Object)
	 */
	public void reset(Object callData) {
		_session = ((CallData) callData).session;
		_imageStack = new Stack();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.oprofile.core.opxml.XMLProcessor#startElement(java.lang.String, org.xml.sax.Attributes, java.lang.Object)
	 */
	public void startElement(String name, Attributes attrs, Object callData) {
		if (name.equals(IMAGE_TAG)) {
			String filename = attrs.getValue(_ATTR_FILENAME);
			if (_currentImage != null)
				_imageStack.push(_currentImage);
			_currentImage = new ProfileImage(filename);
		} else if (name.equals(HEADER_TAG)) {
			OprofileSAXHandler.getInstance(callData).push(_headerProcessor);
		} else if (name.equals(SAMPLE_TAG)) {
			// Yich. This would be a lot cleaner if debug info weren't such a pain...
			_sampleProcessor.setImageFile((ProfileImage) _currentImage);
			OprofileSAXHandler.getInstance(callData).push(_sampleProcessor);
		} else {
			super.startElement(name, attrs, callData);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.oprofile.core.opxml.XMLProcessor#endElement(java.lang.String, java.lang.Object)
	 */
	public void endElement(String name, Object callData) {
		if (name.equals(SAMPLE_TAG)) {
			Sample sample = _sampleProcessor.getSample();
			_currentImage.addSample(sample);
			CallData cdata = (CallData) callData;
			if (cdata.monitor != null) {
				cdata.monitor.worked(sample.getSampleCount());
			}
		} else if (name.equals(IMAGE_TAG)) {
			if (_imageStack.isEmpty()) {
				// Finished image -- add to session
				_session.addSampleContainer(_currentImage);
				_currentImage = null;
			} else {
				// Dependency -- add to image on stack
				ProfileImage dep = _currentImage;
				_currentImage = (ProfileImage) _imageStack.pop();
				_currentImage.addSampleContainer(dep);
			}
		} else if (name.equals(SAMPLEFILE_TAG)) {
			_currentImage.setSampleFile(_characters);
		} else if (name.equalsIgnoreCase(HEADER_TAG)) {
			// Save header map into samplefile
			HashMap map = _headerProcessor.getMap();
			_currentImage.setHeader(map);
		} else {
			super.endElement(name, callData);
		}
	}
}
