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

package org.eclipse.tml.linuxtools.ui;

import org.eclipse.tml.linuxtools.tools.ITool;
import org.eclipse.ui.IViewPart;

/**
 * @author Otavio Ferranti
 */
public interface IToolViewPart extends IViewPart{

	public ITool getTool();
}
