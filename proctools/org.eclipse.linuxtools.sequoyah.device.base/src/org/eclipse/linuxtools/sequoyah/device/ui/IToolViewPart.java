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

package org.eclipse.linuxtools.sequoyah.device.ui;

import org.eclipse.linuxtools.sequoyah.device.tools.ITool;
import org.eclipse.ui.IViewPart;

/**
 * @author Otavio Ferranti
 */
public interface IToolViewPart extends IViewPart{

	public ITool getTool();
}
