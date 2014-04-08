/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp;

import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;

public class STPPartitioner extends FastPartitioner {

	/**
	 * Straight forward FastPartitioner, with debug output.
	 * 
	 *  Taken directly from org.eclipse.linuxtools.rpm.ui.editor.SpecFilePartitioner.
	 *  No noteworthy alterations so Copyright header and license text untouched.
	 *
	 * @param scanner
	 * @param legalContentTypes
	 */
	public STPPartitioner(IPartitionTokenScanner scanner, String[] legalContentTypes) {
		super(scanner, legalContentTypes);
	}	
}

