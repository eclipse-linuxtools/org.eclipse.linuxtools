/*******************************************************************************
 * Copyright (c) 2008, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.oprofile.core.model;

/**
 * This class represents a debugging symbol, the symbol output
 *  from opxml. If a symbol exists, it must have samples (which are
 *  OpModelSamples), although those samples may or may not have
 *  complete debug info.
 */
public class OpModelSymbol {
    private String name;
    private String file;
    private int line;
    private int count;
    private OpModelSample[] samples;
    private String printTabs = "";     //for nice output //$NON-NLS-1$

    public OpModelSymbol() {
        name = ""; //$NON-NLS-1$
        file = ""; //$NON-NLS-1$
        count = 0;
        samples = null;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFilePath(String file) {
        this.file = file;
    }

    public void setLine(int line){
        this.line = line;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setSamples(OpModelSample[] samples) {
        this.samples = samples;
    }

    public String getName() {
        return name;
    }

    public String getFilePath() {
        return file;
    }

    public int getLine(){
        return line;
    }

    public int getCount() {
        return count;
    }

    public OpModelSample[] getSamples() {
        return samples;
    }

    public String toString(String tabs) {
        printTabs = tabs;
        String s = toString();
        printTabs = ""; //$NON-NLS-1$
        return s;
    }

    @Override
    public String toString() {
        String s = name + ", File: " + file + ", Count: " + count + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        if (samples != null) {
            for (int i = 0; i < samples.length; i++) {
                s += printTabs + "Sample: "; //$NON-NLS-1$
                s += samples[i].toString();
            }
        }
        return s;
    }
}
