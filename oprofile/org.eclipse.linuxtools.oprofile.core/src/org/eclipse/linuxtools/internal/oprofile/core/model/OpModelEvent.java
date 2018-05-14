/*******************************************************************************
 * Copyright (c) 2004, 2018 Red Hat, Inc.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *    Kent Sebastian <ksebasti@redhat.com> -
 *******************************************************************************/

package org.eclipse.linuxtools.internal.oprofile.core.model;

import org.eclipse.linuxtools.internal.oprofile.core.Oprofile;

/**
 * A class which represents the event collected in a given session.
 */
public class OpModelEvent {
	private String eventName;
	private String printTabs = ""; // for nice output //$NON-NLS-1$
	private OpModelImage image;
	private OpModelSession parentSession;

	public OpModelEvent(OpModelSession parentSession, String name) {
		this.parentSession = parentSession;
		this.eventName = name;
	}

	public String getName() {
		return eventName;
	}

	public OpModelSession getSession() {
		return parentSession;
	}

	// populate all images & dependent images
	public void refreshModel() {
		image = getNewImage();
	}

	public OpModelImage getImage() {
		return image;
	}

	protected OpModelImage getNewImage() {
		return Oprofile.getModelData(this.eventName, parentSession.getName());
	}

	public int getCount() {
		if (image == null) {
			return 0;
		} else {
			return image.getCount();
		}
	}

	public String toString(String tabs) {
		printTabs = tabs;
		String s = toString();
		printTabs = ""; //$NON-NLS-1$
		return s;
	}

	@Override
	public String toString() {
		String s = eventName + "\n"; //$NON-NLS-1$
		if (image != null) {
			s += printTabs + "Image: "; //$NON-NLS-1$
			s += image.toString(printTabs + "\t"); //$NON-NLS-1$
		}
		return s;
	}
}
