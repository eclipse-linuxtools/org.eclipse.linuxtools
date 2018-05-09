/*******************************************************************************
 * Copyright (c) 2012, 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
