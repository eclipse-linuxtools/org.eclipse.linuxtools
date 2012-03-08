/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.cachegrind.model;

public class CachegrindDescription {
	protected String name;
	protected String size;
	protected String lineSize;
	protected String assoc;
	
	public CachegrindDescription(String name, String size, String lineSize, String assoc) {
		this.name = name;
		this.size = size;
		this.lineSize = lineSize;
		this.assoc = assoc;
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof CachegrindDescription && name.equals(((CachegrindDescription) obj).getName());
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	public String getName() {
		return name;
	}
	
	public String getSize() {
		return size;
	}
	
	public String getLineSize() {
		return lineSize;
	}
	
	public String getAssoc() {
		return assoc;
	}
}
