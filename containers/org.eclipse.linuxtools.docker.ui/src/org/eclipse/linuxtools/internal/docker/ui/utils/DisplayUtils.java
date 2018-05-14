/*******************************************************************************
 * Copyright (c) 2016,2018 Red Hat.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.utils;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Supplier;

import org.eclipse.swt.widgets.Display;

/**
 * Utility class for the {@link Display}
 */
public class DisplayUtils {

	/**
	 * Calls <strong>synchronously</strong> the given {@link Supplier} in the
	 * default Display and returns the result
	 *
	 * @param supplier
	 *            the Supplier to call
	 * @return the supplier's result
	 */
	public static <V> V get(final Supplier<V> supplier) {
		final Queue<V> result = new ArrayBlockingQueue<>(1);
		Display.getDefault().syncExec(() -> result.add(supplier.get()));
		return result.poll();
	}

}
