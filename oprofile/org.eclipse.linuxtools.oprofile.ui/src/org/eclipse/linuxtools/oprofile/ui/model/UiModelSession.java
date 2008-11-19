/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation 
 *******************************************************************************/ 
package org.eclipse.linuxtools.oprofile.ui.model;

import org.eclipse.linuxtools.oprofile.core.model.OpModelSession;
import org.eclipse.swt.graphics.Image;

public class UiModelSession implements IUiModelElement {
	private UiModelEvent _parent;
	private OpModelSession _session;
	private UiModelImage _image;
	private UiModelDependent _dependent;
	
	public UiModelSession(UiModelEvent parent, OpModelSession session) {
		_parent = parent;
		_session = session;
		_image = null;
		_dependent = null;
		refreshModel();
	}
	
	private void refreshModel() {
		
//		_image = new UiModelImage();
		
	}

	@Override
	public IUiModelElement[] getChildren() {
		return new IUiModelElement[] {_image, _dependent};
	}

	@Override
	public Image getLabelImage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IUiModelElement getParent() {
		return _parent;
	}

	@Override
	public boolean hasChildren() {
		return (_image != null);
	}

	@Override
	public String getLabelText() {
		return toString();
	}

	@Override
	public String toString() {
		return _session.getName();
	}
}
