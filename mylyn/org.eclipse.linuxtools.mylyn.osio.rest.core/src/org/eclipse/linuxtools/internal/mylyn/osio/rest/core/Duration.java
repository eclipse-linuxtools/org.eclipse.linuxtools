/*******************************************************************************
 * Copyright (c) 2014 Frank Becker and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
