/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
