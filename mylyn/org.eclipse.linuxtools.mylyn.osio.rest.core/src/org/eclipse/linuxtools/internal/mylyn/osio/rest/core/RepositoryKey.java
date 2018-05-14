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
 *     Red Hat Inc. - modified for use with OpenShift.io
 *******************************************************************************/
package org.eclipse.linuxtools.internal.mylyn.osio.rest.core;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.mylyn.tasks.core.TaskRepository;

public class RepositoryKey {
	private final TaskRepository repository;

	public RepositoryKey(@NonNull TaskRepository repository) {
		super();
		this.repository = repository;
	}

	public TaskRepository getRepository() {
		return repository;
	}

	@Override
	public int hashCode() {
		return repository.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		return this.repository.equals(((RepositoryKey) obj).getRepository());
	}
}
