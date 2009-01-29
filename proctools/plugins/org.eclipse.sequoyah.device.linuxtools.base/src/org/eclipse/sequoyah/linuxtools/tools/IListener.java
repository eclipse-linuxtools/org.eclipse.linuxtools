/********************************************************************************
 * Copyright (c) 2008 Motorola Inc. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributor:
 * Otávio Ferranti (Motorola)
 *
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.tml.linuxtools.tools;

import org.eclipse.tml.linuxtools.network.IConstants.EventCode;

/**
 * @author Otávio Ferranti
 */
public interface IListener {

	/**
	 * @param notifier
	 * @param event
	 * @param result
	 */
	public void notify (INotifier notifier,
						EventCode event,
						Object result);
}
