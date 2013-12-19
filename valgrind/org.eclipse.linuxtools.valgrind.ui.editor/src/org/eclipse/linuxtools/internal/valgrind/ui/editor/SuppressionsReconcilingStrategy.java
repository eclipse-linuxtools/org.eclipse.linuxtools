/*******************************************************************************
 * Copyright (c) 2008, 2009 Phil Muldoon <pkmuldoon@picobot.org>.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Phil Muldoon <pkmuldoon@picobot.org> - initial API.
 *    Red Hat - modifications for use with Valgrind plugins. 
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.ui.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.swt.widgets.Display;

public class SuppressionsReconcilingStrategy implements IReconcilingStrategy,
		IReconcilingStrategyExtension {
	private SuppressionsEditor editor;
	private IDocument document;
	private List<Position> positions;
	private IProgressMonitor monitor;
	
	public SuppressionsReconcilingStrategy(SuppressionsEditor editor) {
		this.editor = editor;
		positions = new ArrayList<>();
	}
	
	@Override
	public void reconcile(IRegion partition) {
		initialReconcile();		
	}

	@Override
	public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
		initialReconcile();
	}

	@Override
	public void setDocument(IDocument document) {
		this.document = document;		
	}

	@Override
	public void initialReconcile() {
		int start = -1;
		int end = document.getLength();
		int worked = 0;
		monitor.beginTask(Messages.getString("SuppressionsReconcilingStrategy.Monitor_title"), 10); //$NON-NLS-1$
		for (int pos = 0; pos < end; pos++) {
			try {
				char ch = document.getChar(pos);
				if (ch == '{') {
					start = pos;
				}
				else if (ch == '}' && start > 0) {
					positions.add(new Position(start, pos - start + 1));
					start = -1; // reset
				}
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
			
			if (pos * 10 / end > worked) {
				monitor.worked(1);
				worked++;
			}
		}		
		monitor.done();
		
		Display.getDefault().syncExec(new Runnable() {		
			@Override
			public void run() {
				editor.updateFoldingStructure(positions.toArray(new Position[positions.size()]));
			}
		});
	}

	@Override
	public void setProgressMonitor(IProgressMonitor monitor) {
		this.monitor = monitor;
	}

}
