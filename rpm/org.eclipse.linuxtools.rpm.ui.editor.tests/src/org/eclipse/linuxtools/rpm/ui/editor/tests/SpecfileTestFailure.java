/*******************************************************************************
 * Copyright (c) 2010, 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.ui.editor.tests;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;

public class SpecfileTestFailure {

	private Position position;
	private Annotation annotation;

	public SpecfileTestFailure(Annotation annotation, Position position) {
		this.annotation = annotation;
		this.position = position;
	}

	public Annotation getAnnotation() {
		return annotation;
	}

	public Position getPosition() {
		return position;
	}
}
