/*******************************************************************************
 * Copyright (c) 2016 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
