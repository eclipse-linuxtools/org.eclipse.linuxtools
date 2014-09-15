/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brajesh K Rathore <brrathor@linux.vnet.ibm.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.oprofile.ui.model;

import java.text.Collator;
import java.util.Comparator;

/**
 * Comparator for sorting tree elements.
 *
 * @since 3.0
 */
public class UiModelSorting implements Comparator<IUiModelElement> {
    private static UiModelSorting instance = new UiModelSorting();

    private Collator collator;

    private UiModelSorting() {
        collator = Collator.getInstance();
    }

    public static UiModelSorting getInstance() {
        return instance;
    }

    @Override
    public int compare(IUiModelElement o1, IUiModelElement o2) {

        // compare line no.
        if (o1 instanceof UiModelSample && o2 instanceof UiModelSample) {
            return ((UiModelSample) o1).getLine()
                    - ((UiModelSample) o2).getLine();
        } else if (o1 instanceof UiModelSymbol && o2 instanceof UiModelSymbol) {
            // compare function name
            return collator.compare(((UiModelSymbol) o1).getFunctionName(),
                    ((UiModelSymbol) o2).getFunctionName());
        } else if (o1 instanceof UiModelImage && o2 instanceof UiModelImage) {
            // comapre lib name
            return collator.compare(getLibraryName(o1.getLabelText()),
                    getLibraryName(o2.getLabelText()));
        }
        // default comparison based on display label
        return collator.compare(o1.getLabelText(), o2.getLabelText());
    }

    private String getLibraryName(String lib) {
        // /lib64/libc-2.12.so - libc-2.12.s0
        String libName = ""; //$NON-NLS-1$
        int index = 0;
        if (null != lib && lib.trim().length() != 0) {
            index = lib.lastIndexOf('/');
            if (index != -1) {
                libName = lib.substring(index + 1, lib.length());
            }

        }
        return libName;
    }
}
