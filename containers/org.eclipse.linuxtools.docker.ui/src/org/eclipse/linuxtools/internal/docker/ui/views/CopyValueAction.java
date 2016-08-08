package org.eclipse.linuxtools.internal.docker.ui.views;

import java.util.Iterator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.IWorkbenchCommandConstants;

public class CopyValueAction extends Action {

	private Clipboard clipboard;
	private TreeViewer treeViewer;

	public CopyValueAction(TreeViewer tableViewer, Clipboard clipboard) {
		super(DVMessages.getString("CopyAction.text")); //$NON-NLS-1$
		setToolTipText(DVMessages.getString("CopyAction.tooltip")); //$NON-NLS-1$
		setActionDefinitionId(IWorkbenchCommandConstants.EDIT_COPY);
		this.treeViewer = tableViewer;
		this.clipboard = clipboard;
	}

	@Override
	public void run() {
		IStructuredSelection selection = (IStructuredSelection)treeViewer.getSelection();
		if (!selection.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			boolean needEOL = false;
			for (Iterator<?> it = selection.iterator(); it.hasNext();) {
				Object item = it.next();
				if (needEOL) {
					sb.append(System.getProperty("line.separator")); //$NON-NLS-1$
				} else {
					needEOL = true;
				}
				// Copy just the value of the property - TreeSelection item is
				// array of 2 strings
				Object[] text = (Object[])item;
				sb.append((String) text[1]);

			}
			clipboard.setContents(new String[] { sb.toString() },
					new Transfer[] { TextTransfer.getInstance() });
		}
	}

}
