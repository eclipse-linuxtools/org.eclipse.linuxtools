/********************************************************************************
 * Copyright (c) 2008 Motorola Inc. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributor:
 * Otavio Ferranti (Motorola)
 *
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.tml.linuxtools.tools;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.tml.linuxtools.network.IConstants.EventCode;

/**
 * @author Otavio Ferranti
 */
public class AbstractNotifier implements INotifier{

	private ArrayList<IListener> listeners = new ArrayList<IListener>();
	
	/* (non-Javadoc)
	 * @see org.eclipse.tml.linuxmemorymapviewer.network.IConnectionProvider#addListener(org.eclipse.tml.linuxmemorymapviewer.network.IListener)
	 */
	public void addListener(IListener listener) {
		if (null != listener) {
			listeners.add(listener);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tml.linuxtools.tools.INotifier#listeners()
	 */
	public List<IListener> listeners() {
		return listeners;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.tml.linuxmemorymapviewer.network.IConnectionProvider#notifyListeners(org.eclipse.tml.linuxmemorymapviewer.network.IConnectionConstants.EventCode, java.lang.Object)
	 */
	public void notifyListeners(EventCode event, Object result) {
		for (IListener listener:listeners){
			listener.notify(this, event, result);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tml.linuxtools.tools.INotifier#removeAllListeners()
	 */
	public void removeAllListeners() {
		listeners.clear();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tml.linuxtools.tools.INotifier#removeListener(org.eclipse.tml.linuxtools.tools.IListener)
	 */
	public void removeListener(IListener listener) {
		listeners.remove(listener);
	}
}
