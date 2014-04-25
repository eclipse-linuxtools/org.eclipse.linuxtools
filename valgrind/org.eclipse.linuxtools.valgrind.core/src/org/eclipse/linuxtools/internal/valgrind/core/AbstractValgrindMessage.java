/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.core;

import java.util.ArrayList;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.linuxtools.valgrind.core.IValgrindMessage;

public class AbstractValgrindMessage implements IValgrindMessage {

    private IValgrindMessage parent;
    private ILaunch launch;
    private ArrayList<IValgrindMessage> children;
    private String text;

    public AbstractValgrindMessage(IValgrindMessage parent, String text, ILaunch launch) {
        children = new ArrayList<>();
        this.parent = parent;
        this.text = text;
        this.launch = launch;

        if (parent != null) {
            parent.addChild(this);
        }
    }

    @Override
    public void addChild(IValgrindMessage message) {
        children.add(message);
    }

    @Override
    public ILaunch getLaunch() {
        return launch;
    }

    @Override
    public IValgrindMessage getParent() {
        return parent;
    }

    @Override
    public IValgrindMessage[] getChildren() {
        return children.toArray(new IValgrindMessage[children.size()]);
    }

    @Override
    public String getText() {
        return text;
    }

}