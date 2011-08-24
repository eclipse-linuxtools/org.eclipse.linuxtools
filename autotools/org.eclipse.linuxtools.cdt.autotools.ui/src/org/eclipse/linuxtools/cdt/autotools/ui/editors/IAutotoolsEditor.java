package org.eclipse.linuxtools.cdt.autotools.ui.editors;

import org.eclipse.linuxtools.internal.cdt.autotools.ui.editors.automake.IReconcilingParticipant;
import org.eclipse.ui.texteditor.ITextEditor;


public interface IAutotoolsEditor extends ITextEditor {
	
	/**
	 * Adds the given listener.
	 * Has no effect if an identical listener was not already registered.
	 * 
	 * @param listener	The reconcile listener to be added
	 */
	public void addReconcilingParticipant(IReconcilingParticipant listener);
}
