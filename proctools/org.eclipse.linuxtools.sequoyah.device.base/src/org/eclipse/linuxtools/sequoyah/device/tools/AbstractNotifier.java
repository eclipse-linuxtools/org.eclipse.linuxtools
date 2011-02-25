/********************************************************************************
 * Copyright (c) 2008-2010 Motorola Inc. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributor:
 * Otavio Ferranti (Motorola)
 *
 * Contributors:
 * Daniel Pastore (Eldorado) - [289870] Moving and renaming Tml to Sequoyah
 ********************************************************************************/

package org.eclipse.linuxtools.sequoyah.device.tools;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.linuxtools.sequoyah.device.network.IConstants.EventCode;

/**
 * @author Otavio Ferranti
 */
public class AbstractNotifier implements INotifier{

	private ArrayList<IListener> listeners = new ArrayList<IListener>();
	
	/* (non-Javadoc)
	 * @see org.eclipse.sequoyah.linuxmemorymapviewer.network.IConnectionProvider#addListener(org.eclipse.sequoyah.linuxmemorymapviewer.network.IListener)
	 */
	public void addListener(IListener listener) {
		if (null != listener) {
			listeners.add(listener);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.sequoyah.device.tools.INotifier#listeners()
	 */
	public List<IListener> listeners() {
		return listeners;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.sequoyah.linuxmemorymapviewer.network.IConnectionProvider#notifyListeners(org.eclipse.sequoyah.linuxmemorymapviewer.network.IConnectionConstants.EventCode, java.lang.Object)
	 */
	public void notifyListeners(EventCode event, Object result) {
		for (IListener listener:listeners){
			listener.notify(this, event, result);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.sequoyah.device.tools.INotifier#removeAllListeners()
	 */
	public void removeAllListeners() {
		listeners.clear();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.sequoyah.device.tools.INotifier#removeListener(org.eclipse.linuxtools.sequoyah.device.tools.IListener)
	 */
	public void removeListener(IListener listener) {
		listeners.remove(listener);
	}
}
