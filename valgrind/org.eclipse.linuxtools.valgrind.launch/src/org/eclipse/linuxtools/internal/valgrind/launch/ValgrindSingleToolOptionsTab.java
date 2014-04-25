/*******************************************************************************
 * Copyright (c) 2012 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.launch;

/**
 * @since 1.2
 *
 * Special version of ValgrindOptionsTab to force the choice of Valgrind tool to
 * just one.  This is used by the Profiling Framework to offer more
 * task-oriented choices to the end-user and not overwhelm them with all the
 * tool choices.
 */
public class ValgrindSingleToolOptionsTab extends ValgrindOptionsTab {

    public ValgrindSingleToolOptionsTab(String toolId) {
        super();
        noToolCombo = true;
        tool = toolId;
        tools = new String[] {tool};
    }

}
