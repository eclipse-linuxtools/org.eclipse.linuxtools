/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse, Anithra P J
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.dashboard.structures;

import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.IDataSet;
import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.ScriptConsole;

/**
 * This is a basic structure to contain all the important information for a
 * running Dashboard script.
 * @author Ryan Morse
 */
public class ActiveModuleData {
	public DashboardModule module;
	public ScriptConsole cmd;
	public IDataSet data;
	public boolean paused;
}
