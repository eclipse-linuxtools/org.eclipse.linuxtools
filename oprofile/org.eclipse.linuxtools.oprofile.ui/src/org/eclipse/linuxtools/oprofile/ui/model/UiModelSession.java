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

import org.eclipse.linuxtools.oprofile.core.model.OpModelImage;
import org.eclipse.linuxtools.oprofile.core.model.OpModelSession;
import org.eclipse.swt.graphics.Image;

/**
 * Children of events in the view -- sessions containing images/symbols
 *  for its parent event. Must have a child image. May also have dependent
 *  images, which are children of the Image in the data model, but are 
 *  displayed as children of the session in the view.
 */
public class UiModelSession implements IUiModelElement {
	private IUiModelElement _parent;		//parent element
	private OpModelSession _session;		//the node in the data model
	private UiModelImage _image;			//this node's child
	private UiModelDependent _dependent;	//dependent images of the OpModelImage
	
	public UiModelSession(IUiModelElement parent, OpModelSession session) {
		_parent = parent;
		_session = session;
		_image = null;
		_dependent = null;
		refreshModel();
	}
	
	private void refreshModel() {
		OpModelImage dataModelImage = _session.getImage();
		_image = new UiModelImage(this, dataModelImage, dataModelImage.getCount(), dataModelImage.getDepCount());
		
		if (dataModelImage.hasDependents()) {
			_dependent = new UiModelDependent(this, dataModelImage.getDependents(), dataModelImage.getCount(), dataModelImage.getDepCount());
		}
	}

	@Override
	public String toString() {
		return _session.getName();
	}

	/** IUiModelElement functions **/
	@Override
	public String getLabelText() {
		return toString();
	}

	@Override
	public IUiModelElement[] getChildren() {
		if (_dependent != null) {
			return new IUiModelElement[] {_image, _dependent};
		} else {
			return new IUiModelElement[] {_image};
		}
	}

	@Override
	public boolean hasChildren() {
		return (_image != null);
	}

	@Override
	public IUiModelElement getParent() {
		return _parent;
	}

	@Override
	public Image getLabelImage() {
		// TODO Auto-generated method stub
		return null;
	}
}
