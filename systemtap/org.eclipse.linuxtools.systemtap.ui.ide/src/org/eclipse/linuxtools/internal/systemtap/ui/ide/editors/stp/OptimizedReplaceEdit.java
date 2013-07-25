/*******************************************************************************
 * Copyright (c) 2000, 2011, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *     Red Hat Inc. - modified for use in SystemTap
 *******************************************************************************/
package org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp;

public class OptimizedReplaceEdit {
	int offset;
	int length;
	String replacement;
	
	public OptimizedReplaceEdit(int offset, int length, CharSequence replacement) {
		this.offset = offset;
		this.length = length;
		this.replacement = replacement.toString();
	}
	
	@Override
	public String toString() {
		return "(" + this.offset + ", length " + this.length + " :>" + this.replacement + "<"; //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$
	}
}
