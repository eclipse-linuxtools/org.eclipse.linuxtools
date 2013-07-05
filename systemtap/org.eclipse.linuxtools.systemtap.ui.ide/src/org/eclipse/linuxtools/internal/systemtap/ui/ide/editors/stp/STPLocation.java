/*******************************************************************************
 * Copyright (c) 2000, 2006, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *     Red Hat Inc. - modified for use in SystemTap
 *******************************************************************************/
package org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp;

/**
 * A location maintains positional information both in original source 
 * and in the output source.
 * It remembers source offsets, line/column and indentation level.
 * 
 * @since 4.0
 */
public class STPLocation {
	public int inputOffset;
	public int outputLine;
	public int outputColumn;
	public int outputIndentationLevel;
	public boolean needSpace;
	public boolean pendingSpace;
	public int numberOfIndentations;

	// chunk management
	public int lastNumberOfNewLines;
	
	// edits management
	int editsIndex;
	OptimizedReplaceEdit textEdit;
	
	public Runnable tailFormatter;

	public STPLocation(STPScribe scribe, int sourceRestart){
		update(scribe, sourceRestart);
	}
	
	public void update(STPScribe scribe, int sourceRestart){
		this.inputOffset = sourceRestart;
		this.outputLine = scribe.line;
		this.outputColumn = scribe.column;
		this.outputIndentationLevel = scribe.indentationLevel;
		this.needSpace = scribe.needSpace;
		this.pendingSpace = scribe.pendingSpace;
		this.numberOfIndentations = scribe.numberOfIndentations;
		this.lastNumberOfNewLines = scribe.lastNumberOfNewLines;
		this.editsIndex = scribe.editsIndex;
		this.textEdit = scribe.getLastEdit();
		this.tailFormatter = scribe.getTailFormatter();
	}
}
