/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.valgrind.massif.model;

import java.util.ArrayList;

import org.eclipse.swt.graphics.Image;

public abstract class MassifTreeElement {
	protected MassifTreeElement parent;
	protected ArrayList<MassifTreeElement> children;

	public MassifTreeElement getParent() {
		return parent;
	}

	public MassifTreeElement[] getChildren() {
		return children.toArray(new MassifTreeElement[children.size()]);
	}

	public abstract String getText();

	public abstract Image getImage();
}
