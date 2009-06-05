package org.eclipse.linuxtools.cdt.autotools.ui.editors;

import org.eclipse.linuxtools.cdt.autotools.internal.editors.automake.IReconcilingParticipant;
import org.eclipse.ui.texteditor.ITextEditor;


public interface IAutotoolsEditor extends ITextEditor {
	
	public Object getAdapter(Class<?> key);
	
	/**
	 * Adds the given listener.
	 * Has no effect if an identical listener was not already registered.
	 * 
	 * @param listener	The reconcile listener to be added
	 */
	public void addReconcilingParticipant(IReconcilingParticipant listener);
}
