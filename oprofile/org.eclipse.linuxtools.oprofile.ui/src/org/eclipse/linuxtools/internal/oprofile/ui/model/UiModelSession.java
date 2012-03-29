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
package org.eclipse.linuxtools.internal.oprofile.ui.model;

import org.eclipse.linuxtools.internal.oprofile.core.model.OpModelImage;
import org.eclipse.linuxtools.internal.oprofile.core.model.OpModelSession;
import org.eclipse.linuxtools.internal.oprofile.ui.OprofileUiMessages;
import org.eclipse.linuxtools.internal.oprofile.ui.OprofileUiPlugin;
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

	//OProfile's default session name
	private static final String DEFAULT_SESSION_NAME = "current"; //$NON-NLS-1$
	
	public UiModelSession(IUiModelElement parent, OpModelSession session) {
		_parent = parent;
		_session = session;
		_image = null;
		_dependent = null;
		refreshModel();
	}
	
	private void refreshModel() {
		OpModelImage dataModelImage = _session.getImage();
		if (dataModelImage != null) {
			_image = new UiModelImage(this, dataModelImage, dataModelImage.getCount(), dataModelImage.getDepCount());
			
			if (dataModelImage.hasDependents()) {
				_dependent = new UiModelDependent(this, dataModelImage.getDependents(), dataModelImage.getCount(), dataModelImage.getDepCount());
			}
		}
	}

	@Override
	public String toString() {
		return _session.getName();
	}
	
	public boolean isDefaultSession() {
		return _session.getName().equalsIgnoreCase(DEFAULT_SESSION_NAME);
	}

	/** IUiModelElement functions **/
	public String getLabelText() {
		if (_session.getName().equals(DEFAULT_SESSION_NAME)){
			return OprofileUiMessages.getString("UiModelSession_current"); //$NON-NLS-1$
		}
		return toString();
	}

	public IUiModelElement[] getChildren() {
		if (_dependent != null) {
			return new IUiModelElement[] {_image, _dependent};
		} else {
			return new IUiModelElement[] {_image};
		}
	}

	public boolean hasChildren() {
		return (_image != null);
	}

	public IUiModelElement getParent() {
		return _parent;
	}

	public Image getLabelImage() {
		return OprofileUiPlugin.getImageDescriptor(OprofileUiPlugin.SESSION_ICON).createImage();
	}
}
