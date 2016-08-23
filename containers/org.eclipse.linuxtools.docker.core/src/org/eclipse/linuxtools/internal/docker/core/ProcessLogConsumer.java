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

package org.eclipse.linuxtools.internal.docker.core;

import java.util.function.Consumer;

/**
 * A Log consumer that has a {@code ready} flag to delay processing.
 */
public class ProcessLogConsumer {

	private final Consumer<String> consumer;

	private boolean ready = false;

	public ProcessLogConsumer(final Consumer<String> consumer) {
		this.consumer = consumer;
	}

	public boolean isReady() {
		return this.ready;
	}

	public void setReady(boolean ready) {
		this.ready = ready;
	}

	public void process(final String log) {
		consumer.accept(log);
	}
}
