/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/

package org.eclipse.cdt.oprofile.core;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.swt.widgets.Shell;

/**
 * SampleContainer is an abstract base class that is used to provide common
 * functionality to classes which contain samples (SampleFiles, which contain samples, and
 * SampleSessions, which contain SampleFiles).
 * @author Keith Seitz  <keiths@redhat.com>
 */
public abstract class SampleContainer implements ISampleContainer
{
	// The samples in this container
	protected ArrayList _samples;
	
	// The disk image of this container
	protected File _file;
	
	// The list of sample containers associated with this container (if any)
	protected ArrayList _containers;
	
	/**
	 * Constructor SampleContainer.
	 * @param file the disk image of this container
	 */
	public SampleContainer(File file) {
		_file = file;
		_containers = new ArrayList();
		_samples = new ArrayList();
	}
	
	/**
	 * @see org.eclipse.cdt.oprofile.core.ISampleContainer#getExecutableName()
	 */
	public String getExecutableName() {
		return getFile().getPath();
	}
		
	/**
	 * @see org.eclipse.cdt.oprofile.core.ISampleContainer#getFile()
	 */
	public File getFile() {
		return _file;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.oprofile.core.ISampleContainer#getSampleContainers()
	 */
	public ISampleContainer[] getSampleContainers(Shell shell) {
		ISampleContainer[] containers = new ISampleContainer[_containers.size()];
		_containers.toArray(containers);
		return containers;
	}
	
	/**
	 * Add a sample container to this container (i.e., "--separate-libs")
	 * @param container the container
	 */
	public void addSampleContainer(ISampleContainer container) {
		_containers.add(container);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.oprofile.core.ISampleContainer#getSamples()
	 */
	public Sample[] getSamples(Shell shell) {
		Sample[] samples = new Sample[_samples.size()];
		_samples.toArray(samples);
		return samples;
	}
	
	/**
	 * Add a sample to this container.
	 * @param smpl the sample
	 */
	public void addSample(Sample smpl) {
		_samples.add(smpl);
	}
}
