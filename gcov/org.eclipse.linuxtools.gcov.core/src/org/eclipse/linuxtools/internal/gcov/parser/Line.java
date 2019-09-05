/*******************************************************************************
 * Copyright (c) 2009, 2019 STMicroelectronics and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gcov.parser;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;


public class Line implements Serializable{

    private static final long serialVersionUID = 8804878976767948267L;
    private boolean exists = false;
    private long count = 0;
	private Set<Block> blocks = new HashSet<>();

    /*Getters & setters*/

    public boolean exists() {
        return exists;
    }

    public void setExists(boolean exists) {
        this.exists = exists;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public long getCount() {
        return count;
    }

	public void addBlock(Block b) {
		blocks.add(b);
	}

	public boolean hasBlock(Block b) {
		return blocks.contains(b);
	}

	public Set<Block> getBlocks() {
		return blocks;
	}

	@Override
	public int hashCode() {
		return blocks.hashCode();
	}

}
