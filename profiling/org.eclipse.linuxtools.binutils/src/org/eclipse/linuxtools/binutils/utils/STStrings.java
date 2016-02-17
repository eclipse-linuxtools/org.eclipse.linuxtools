/*******************************************************************************
 * Copyright (c) 2016 Ingenico.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ingenico  - Vincent Guignot <vincent.guignot@ingenico.com> - Add binutils strings
 *******************************************************************************/

package org.eclipse.linuxtools.binutils.utils;

/**
 * @since 6.0
 */
public class STStrings {

    private String strings;
    private String[] args;

    public STStrings(String strings, String[] args) {
        this.strings = strings;
        this.args = args;
    }

    public String getName() {
        return this.strings;
    }

    public String[] getArgs() {
        return this.args;
    }
}
