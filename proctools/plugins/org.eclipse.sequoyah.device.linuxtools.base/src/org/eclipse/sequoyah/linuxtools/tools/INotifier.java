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

import java.util.List;

import org.eclipse.tml.linuxtools.network.IConstants.EventCode;

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
