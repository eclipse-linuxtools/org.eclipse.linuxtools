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

package org.eclipse.linuxtools.systemtap.ui.dashboard;

import java.util.ArrayList;

/**
 * This is a simple class used to store information specific to the dashboard that
 * needs to be accessesed at random.
 * @author Ryan Morse
 */
public class DashboardSessionSettings {
	public static String password = null;
	public static ArrayList<Object> allmodules = null;
}
