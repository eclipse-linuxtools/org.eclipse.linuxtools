/*******************************************************************************
 * Copyright (c) 2013, 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Neil Guzman - python, ruby, perl implementation (B#350065,B#350066)
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpmstubby.parser;

/**
 * Meta-Data tags common within the files
 *
 */
@SuppressWarnings("javadoc")
public interface CommonMetaData {
    String NAME = "name";
    String DESCRIPTION = "description";
    String VERSION = "version";
    String LICENSE = "license";
    String URL = "url";
}
