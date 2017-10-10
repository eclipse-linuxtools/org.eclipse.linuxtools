/*******************************************************************************
 * Copyright (c) 2014, 2017 Sam Davis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sam Davis - initial API and implementation
 *     Red Hat Inc. - modified to use with OpenShift.io
 *******************************************************************************/

package org.eclipse.linuxtools.internal.mylyn.osio.rest.core;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;

public abstract class ListenableFutureJob<V> extends Job implements ListenableFuture<V> {

	private class Listener {

		Runnable runnable;

		Executor executor;

		public Listener(Runnable runnable, Executor executor) {
			this.runnable = runnable;
			this.executor = executor;
		}

		public void run() {
			executor.execute(runnable);
		}
	}

	List<Listener> listeners = Collections.synchronizedList(Lists.<Listener> newArrayList());

	private volatile boolean done;

	private V resultObject;

	public ListenableFutureJob(String name) {
		super(name);
		resultObject = null;
		this.addJobChangeListener(new JobChangeAdapter() {

			@Override
			public void done(IJobChangeEvent event) {
				done = true;
				synchronized (listeners) {
					for (Listener listener : listeners) {
						listener.run();
					}
				}
			}
		});
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return this.cancel();
	}

	@Override
	public V get() throws InterruptedException, ExecutionException {
		this.join();
		return resultObject;
	}

	@Override
	public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		long start = System.currentTimeMillis();
		while (!done && System.currentTimeMillis() - start < unit.toMillis(timeout)) {
			Thread.sleep(250);
		}
		if (!done) {
			throw new TimeoutException("ListenableFutureJob.get() could not get the result!");
		}
		return resultObject;
	}

	protected void set(V future) {
		this.resultObject = future;
	}

	@Override
	public boolean isCancelled() {
		return this.getResult().getSeverity() == IStatus.CANCEL;
	}

	@Override
	public boolean isDone() {
		return done;
	}

	@Override
	public void addListener(Runnable listener, Executor executor) {
		listeners.add(new Listener(listener, executor));
	}
}
