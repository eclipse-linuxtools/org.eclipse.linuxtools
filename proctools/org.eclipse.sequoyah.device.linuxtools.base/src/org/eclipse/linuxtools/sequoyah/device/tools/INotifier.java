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

import java.util.List;

import org.eclipse.linuxtools.sequoyah.device.network.IConstants.EventCode;

/**
 * @author Otavio Ferranti
 */
public interface INotifier {

	/**
	 * @param listener
	 */
	public void addListener(IListener listener);
	
	/**
	 * @return
	 */
	public List<IListener> listeners();

	/**
	 * @param event
	 * @param result
	 */
	public void notifyListeners (EventCode event, Object result);

	/**
	 * 
	 */
	public void removeAllListeners ();
	
	/**
	 * @param listener
	 */
	public void removeListener (IListener listener);
	
}
