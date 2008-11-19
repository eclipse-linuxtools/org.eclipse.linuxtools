/*******************************************************************************
 * Copyright (c) 2004,2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *    Kent Sebastian <ksebasti@redhat.com> - 
 *******************************************************************************/ 

package org.eclipse.linuxtools.oprofile.core.model;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.swt.widgets.Shell;

/**
 * OpModelContainer is an abstract base class that is used to provide common
 * functionality to classes which contain samples (SampleFiles, which contain samples, and
 * SampleSessions, which contain SampleFiles).
 */
public abstract class OpModelContainer implements IOpModelContainer
{
	// The samples in this container
	protected ArrayList<OpModelSample> _samples;
	
	// The disk image of this container
	protected File _file;
	
	// The list of sample containers associated with this container (if any)
	protected ArrayList<IOpModelContainer> _containers;
	
	/**
	 * Constructor OpModelContainer.
	 * @param file the disk image of this container
	 */
	public OpModelContainer(File file) {
		_file = file;
		_containers = new ArrayList<IOpModelContainer>();
		_samples = new ArrayList<OpModelSample>();
	}
	
	/**
	 * @see org.eclipse.linuxtools.oprofile.core.model.IOpModelContainer#getExecutableName()
	 */
	public String getExecutableName() {
		return getFile().getPath();
	}
		
	/**
	 * @see org.eclipse.linuxtools.oprofile.core.model.IOpModelContainer#getFile()
	 */
	public File getFile() {
		return _file;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.oprofile.core.ISampleContainer#getSampleContainers()
	 */
	public IOpModelContainer[] getSampleContainers(Shell shell) {
		IOpModelContainer[] containers = new IOpModelContainer[_containers.size()];
		_containers.toArray(containers);
		return containers;
	}
	
	/**
	 * Add a sample container to this container (i.e., "--separate-libs")
	 * @param container the container
	 */
	public void addSampleContainer(IOpModelContainer container) {
		_containers.add(container);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.oprofile.core.ISampleContainer#getSamples()
	 */
	public OpModelSample[] getSamples(Shell shell) {
		OpModelSample[] samples = new OpModelSample[_samples.size()];
		_samples.toArray(samples);
		return samples;
	}
	
	/**
	 * Add a sample to this container.
	 * @param smpl the sample
	 */
	public void addSample(OpModelSample smpl) {
		_samples.add(smpl);
	}
	
	
}
