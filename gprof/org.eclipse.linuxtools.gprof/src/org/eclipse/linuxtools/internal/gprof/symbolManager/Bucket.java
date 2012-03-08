/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gprof.symbolManager;


/**
 * Bucket structure.
 * used to display bucket info relative to each symbol.
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public class Bucket {
	
	/** Start address of this bucket */
	public final long start_addr;
	/** End address of this bucket */
	public final long end_addr;
	/** time spent in this bucket */
	public final int  time;
	
	/**
	 * Constructor
	 * @param start_addr
	 * @param end_addr
	 * @param time
	 */
	public Bucket(long start_addr, long end_addr, int time) {
		this.start_addr = start_addr;
		this.end_addr   = end_addr;
		this.time       = time;
	}

}
