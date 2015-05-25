/*******************************************************************************
 * Copyright (c) 2014 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.core;

import org.eclipse.linuxtools.docker.core.IDockerProgressDetail;

public class DockerProgressDetail implements IDockerProgressDetail {

	private long current;
	private long start;
	private long total;

	public DockerProgressDetail(long current, long start, long total) {
		this.current = current;
		this.start = start;
		this.total = total;
	}

	@Override
	public long current() {
		return current;
	}

	@Override
	public long start() {
		return start;
	}

	@Override
	public long total() {
		return total;
	}

	@Override
	public String toString() {
		return "Progress Detail: current=" + Long.toString(current) + "\n"
				+ "  start=" + Long.toString(start) + "\n" + "  total="
				+ Long.toString(total) + "\n";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DockerProgressDetail other = (DockerProgressDetail) obj;
		return this.current == other.current && this.start == other.start
				&& this.total == other.total;
	}

}
