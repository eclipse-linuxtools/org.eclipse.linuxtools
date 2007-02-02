/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/

package org.eclipse.cdt.oprofile.ui.system;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.cdt.oprofile.core.ISampleContainer;
import org.eclipse.cdt.oprofile.core.Sample;
import org.eclipse.cdt.oprofile.ui.OprofilePlugin;
import org.eclipse.cdt.oprofile.ui.internal.IProfileElement;
import org.eclipse.cdt.oprofile.ui.internal.ProfileElement;
import org.eclipse.swt.graphics.Image;

/**
 * Convenience class for all SystemProfile root elements.
 * @author keiths
 */
public abstract class SystemProfileRootElement extends ProfileElement
{
	protected IProfileElement[] _children = null;
	protected ISampleContainer _container;
	protected int _count = -1;
	
	SystemProfileRootElement(IProfileElement parent, int type, ISampleContainer sfile)
	{
		super(parent, type);
		_container = sfile;
	}
		
	/**
	 * @see org.eclipse.cdt.oprofile.ui.internal.IProfileElement#hasChildren()
	 */
	public boolean hasChildren()
	{
		// Sample containers always have children; other samplefiles, symbols or samples
	 	return true;
	}

	/**
	 * @see org.eclipse.cdt.oprofile.ui.internal.IProfileElement#getChildren()
	 * This is only executed by root elements, so there will be only one container: the
	 * sample file.
	 */
	public IProfileElement[] getChildren()
	{
		if (_children == null)
		{
			ArrayList children = new ArrayList();
			
			// Add children of sub-sample files
			ISampleContainer[] files = _container.getSampleContainers(OprofilePlugin.getActiveWorkbenchShell());
			if (files != null) {
				for (int i = 0; i < files.length; ++i) {
					// FIXME: Stolen from SystemProfileSession. Should probably be made
					// public somewhere.
					String exeName = files[i].getExecutableName();
					if (exeName.endsWith(".o"))  { // $NON-NLS-1$
						children.add(new SystemProfileObject(this, files[i]));
					} else if (exeName.endsWith(".so") || exeName.indexOf(".so") != -1) { // $NON-NLS-1$  // $NON-NLS-2$
						children.add(new SystemProfileShLib(this, files[i]));
					} else {
						children.add(new SystemProfileExecutable(this, files[i]));
					}
				}
			}

			// Now add children of this object's sample file
			HashMap symbols = new HashMap();
			// getSampleContainers collects the samples, too, so we don't need a
			// progress monitor here.
			Sample[] samples = _container.getSamples(null);

			// Go through all of the samples, collecting all the samples for a
			// given symbol name into one SystemProfileSymbol. If the sample
			// has no symbol info, make a SystemProfileSample of it.
			
			for (int i = 0; i < samples.length; i++)
			{
				if (samples[i].hasSymbol())
				{
					if (symbols.containsKey(samples[i].getSymbol().name))
					{
						// add this sample to the list of all symbols in the SampleSymbol
						SystemProfileSymbol sym = (SystemProfileSymbol) symbols.get(samples[i].getSymbol().name);
						sym.addSample(samples[i]);
					}
					else
					{
						// Add this sample to a new object in the map
						SystemProfileSymbol sym = new SystemProfileSymbol(this);
						sym.addSample(samples[i]);
						symbols.put(samples[i].getSymbol().name, sym);
					}
				}
				else
				{
					// No symbol info. Just add a new SystemProfileSample
					children.add(new SystemProfileSample(this, samples[i]));
				}
			}
		
			// Now add all the symbols to the list of children (since they are now unique)
			for (Iterator i = symbols.keySet().iterator(); i.hasNext(); )
				children.add((IProfileElement) symbols.get(i.next()));
		
			IProfileElement[] kids = new IProfileElement[children.size()];
			children.toArray(kids);
			_children = kids;
		}
		
		return _children;
	}

	/**
	 * @see org.eclipse.cdt.oprofile.ui.internal.IProfileElement#getLabelText()
	 */
	public String getLabelText()
	{
		return _container.getExecutableName();
	}

	public abstract Image getLabelImage();
	
	public int getSampleCount()
	{
		if (_children == null)
			_children = getChildren();
		
		if (_count < 0)
		{
			_count = 0;
			for (int i = 0; i < _children.length; ++i)
				_count += _children[i].getSampleCount();
		}
		
		return _count;
	}
}
