/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data;

public class PagingLinks {
	
	private String prev;
	
	private String next;
	
	private String first;
	
	private String last;
	
	private String filters;
	
	// for testing purposes only
	public PagingLinks (String prev, String next, String first, String last, String filters) {
		this.prev = prev;
		this.next = next;
		this.first = first;
		this.last = last;
		this.filters = filters;
	}
	
	public String getPrev() {
		return prev;
	}
	
	public String getNext() {
		return next;
	}
	
	public String getFirst() {
		return first;
	}
	
	public String getLast() {
		return last;
	}
	
	public String getFilters() {
		return filters;
	}

}
