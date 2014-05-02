/*******************************************************************************
 * Copyright (c) 2004, 2008, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *    Kent Sebastian <ksebasti@redhat.com>
 *******************************************************************************/
package org.eclipse.linuxtools.internal.oprofile.core.model;


/**
 * A class which represents an image (executables,
 * libraries, modules) profile by OProfile.
 */
public class OpModelImage {
    public static final int IMAGE_PARSE_ERROR = -1;

    //The count of all samples from this image
    private int count;

    //the count for all dependent images -- needed?
    private int depcount;

    //The name of this image (the full path, where applicable)
    private String name;

    //The symbols profiled in this image
    private OpModelSymbol[] symbols;

    //Any dependent images on this image (usually shared libs, kernel modules)
    private OpModelImage[] dependents;

    private String printTabs = "";        //for nice output //$NON-NLS-1$

    public OpModelImage() {
        name = ""; //$NON-NLS-1$
        count = 0;
        depcount = 0;
        symbols = null;
        dependents = null;
    }

    public int getCount() {
        return count;
    }

    public int getDepCount() {
        return depcount;
    }

    public String getName() {
        return name;
    }

    public OpModelSymbol[] getSymbols() {
        return symbols;
    }

    public OpModelImage[] getDependents() {
        return dependents;
    }

    public boolean hasDependents() {
        return (dependents == null || dependents.length == 0 ? false : true);
    }

    /**
     * This method is not meant to be called publicly, used only
     * from the XML processors
     * @param count
     */
    public void setCount(int count) {
        this.count = count;
    }

    /**
     * This method is not meant to be called publicly, used only
     * from the XML processors
     * @param depcount
     */
    public void setDepCount(int depcount) {
        this.depcount = depcount;
    }

    /**
     * This method is not meant to be called publicly, used only
     * from the XML processors
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * This method is not meant to be called publicly, used only
     * from the XML processors
     * @param symbols
     */
    public void setSymbols(OpModelSymbol[] symbols) {
        this.symbols = symbols;
    }

    /**
     * This method is not meant to be called publicly, used only
     * from the XML processors
     * @param dependents
     */
    public void setDependents(OpModelImage[] dependents) {
        this.dependents = dependents;
    }

    public String toString(String tabs) {
        printTabs = tabs;
        String s = toString();
        printTabs = ""; //$NON-NLS-1$
        return s;
    }

    @Override
    public String toString() {
        String s = name + ", Count: " + count + (depcount !=0 ? ", Dependent Count: " + depcount + "\n" : "\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        if (symbols != null) {
            for (int i = 0; i < symbols.length; i++) {
                s += printTabs + "Symbols: "; //$NON-NLS-1$
                s += symbols[i].toString(printTabs + "\t"); //$NON-NLS-1$
            }
        }
        if (dependents != null) {
            for (int i = 0; i < dependents.length; i++) {
                s += printTabs + "Dependent Image: "; //$NON-NLS-1$
                s += dependents[i].toString(printTabs + "\t"); //$NON-NLS-1$
            }
        }
        return s;
    }
}
