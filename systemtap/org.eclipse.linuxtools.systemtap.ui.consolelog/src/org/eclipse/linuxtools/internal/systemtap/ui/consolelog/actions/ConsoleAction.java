/*******************************************************************************
 * Copyright (c) 2006, 2018 IBM Corporation and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jeff Briggs, Henry Hughes, Ryan Morse, Anithra P j
 *     Roland Grunberg
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.consolelog.actions;

import java.net.URL;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.ScriptConsole;

/**
 * A basic core class that provides common methods that are needed by any
 * action that makes use of the Console.
 * @author Ryan Morse
 */
public abstract class ConsoleAction extends Action {

    protected ScriptConsole console;

    protected ConsoleAction(ScriptConsole fConsole,
            URL imagePath,
            String text,
            String toolTip) {
        this(fConsole, imagePath, text, toolTip, IAction.AS_PUSH_BUTTON);
    }

    protected ConsoleAction(ScriptConsole fConsole,
            URL imagePath,
            String text,
            String toolTip,
            int style) {
        super(null, style);
        console = fConsole;

        setImageDescriptor(ImageDescriptor.createFromURL(imagePath));
        setToolTipText(text);
        setText(toolTip);
    }
}
