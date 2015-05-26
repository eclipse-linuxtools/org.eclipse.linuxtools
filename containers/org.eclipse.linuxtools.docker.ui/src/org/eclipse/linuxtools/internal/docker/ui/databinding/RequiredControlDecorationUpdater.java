/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.databinding;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationUpdater;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;

/**
 * Decorates a {@link Control} to indicate that the value is required.
 * 
 * @author xcoulon
 *
 */
public class RequiredControlDecorationUpdater extends ControlDecorationUpdater {

	/**
	 * boolean to force the decorator even when the status is IStatus#CANCEL.
	 */

	private final boolean showRequiredDecorator;

	public RequiredControlDecorationUpdater(
			final boolean showRequiredDecorator) {
		this.showRequiredDecorator = showRequiredDecorator;
	}

	@Override
	protected Image getImage(IStatus status) {
		if (status == null) {
			return null;
		}
		String fieldDecorationID = null;
		switch (status.getSeverity()) {
		case IStatus.INFO:
			fieldDecorationID = FieldDecorationRegistry.DEC_INFORMATION;
			break;
		case IStatus.WARNING:
			fieldDecorationID = FieldDecorationRegistry.DEC_WARNING;
			break;
		case IStatus.ERROR:
			fieldDecorationID = FieldDecorationRegistry.DEC_ERROR;
			break;
		case IStatus.CANCEL:
			fieldDecorationID = showRequiredDecorator
					? FieldDecorationRegistry.DEC_REQUIRED : null;
			break;
		}

		final FieldDecoration fieldDecoration = FieldDecorationRegistry
				.getDefault().getFieldDecoration(fieldDecorationID);
		return fieldDecoration == null ? null : fieldDecoration.getImage();
	}

}
