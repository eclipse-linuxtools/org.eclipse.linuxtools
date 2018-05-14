/*******************************************************************************
 * Copyright (c) 2014, 2018 Frank Becker and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Frank Becker - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.mylyn.osio.rest.core;

import java.util.concurrent.TimeUnit;

public class Duration {
	final TimeUnit unit;

	final long value;

	public Duration(long value, TimeUnit unit) {
		this.unit = unit;
		this.value = value;
	}

	public TimeUnit getUnit() {
		return unit;
	}

	public long getValue() {
		return value;
	}

}
