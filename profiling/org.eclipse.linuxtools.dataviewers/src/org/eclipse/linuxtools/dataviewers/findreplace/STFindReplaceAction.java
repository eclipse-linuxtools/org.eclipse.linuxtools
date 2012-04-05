/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marzia Maugeri <marzia.maugeri@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.dataviewers.findreplace;


import java.util.ResourceBundle;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.texteditor.IAbstractTextEditorHelpContextIds;
import org.eclipse.ui.texteditor.ITextEditorExtension2;
import org.eclipse.ui.texteditor.IUpdate;
import org.eclipse.ui.texteditor.ResourceAction;

/**
 * An action which opens a Find/Replace dialog.
 * The dialog while open, tracks the active workbench part
 * and retargets itself to the active find/replace target.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 *
 * @see ISTFindReplaceTarget
 */
public class STFindReplaceAction extends ResourceAction implements IUpdate {
	private static final String BUNDLE_FOR_CONSTRUCTED_KEYS= "org.eclipse.ui.texteditor.ConstructedEditorMessages";//$NON-NLS-1$
	private static ResourceBundle fgBundleForConstructedKeys= ResourceBundle.getBundle(BUNDLE_FOR_CONSTRUCTED_KEYS);
	private static String prefix = "Editor.FindReplace.";
	
	/**
	 * Represents the "global" find/replace dialog. It tracks the active
	 * part and retargets the find/replace dialog accordingly. The find/replace
	 * target is retrieved from the active part using
	 * <code>getAdapter(ISTFindReplaceTarget.class)</code>.
	 */
	static class FindReplaceDialogStub implements IPartListener, DisposeListener {

		/** The workbench part */
		private IWorkbenchPart fPart;
		/** The previous workbench part */
		private IWorkbenchPart fPreviousPart;
		/** The previous find/replace target */
		private ISTFindReplaceTarget fPreviousTarget;

		/** The workbench window */
		private IWorkbenchWindow fWindow;
		/** The find/replace dialog */
		private STFindReplaceDialog fDialog;

		/**
		 * Creates a new find/replace dialog accessor anchored at the given part site.
		 * 
		 * @param site the part site
		 */
		public FindReplaceDialogStub(IWorkbenchPartSite site) {
			this(site.getShell());
			fWindow= site.getWorkbenchWindow();
			IPartService service= fWindow.getPartService();
			service.addPartListener(this);
			partActivated(service.getActivePart());
		}

		/**
		 * Creates a new find/replace dialog accessor anchored at the given shell.
		 * 
		 * @param shell the shell if no site is used
		 */
		public FindReplaceDialogStub(Shell shell) {
			fDialog= new STFindReplaceDialog(shell);				
			fDialog.create();
			fDialog.getShell().addDisposeListener(this);
		}

		/**
		 * Returns the find/replace dialog.
		 * @return the find/replace dialog
		 */
		public STFindReplaceDialog getDialog() {
			return fDialog;
		}

		/*
		 * @see IPartListener#partActivated(IWorkbenchPart)
		 */
		public void partActivated(IWorkbenchPart part) {
			ISTFindReplaceTarget target= part == null ? null : (ISTFindReplaceTarget) part.getAdapter(ISTFindReplaceTarget.class);
			fPreviousPart= fPart;
			fPart= target == null ? null : part;

			if (fPreviousTarget != target) {
				fPreviousTarget= target;
				if (fDialog != null) {
					boolean isEditable= false;
					if (fPart instanceof ITextEditorExtension2) {
						ITextEditorExtension2 extension= (ITextEditorExtension2) fPart;
						isEditable= extension.isEditorInputModifiable();
					} else if (target != null) {
						isEditable= target.isEditable();
					}
					fDialog.updateTarget(target, isEditable, false);
				}
			}
		}

		/*
		 * @see IPartListener#partClosed(IWorkbenchPart)
		 */
		public void partClosed(IWorkbenchPart part) {

			if (part == fPreviousPart) {
				fPreviousPart= null;
				fPreviousTarget= null;
			}

			if (part == fPart)
				partActivated(null);
		}

		/*
		 * @see DisposeListener#widgetDisposed(DisposeEvent)
		 */
		public void widgetDisposed(DisposeEvent event) {

			if (fgFindReplaceDialogStub == this)
				fgFindReplaceDialogStub= null;

			if(fgFindReplaceDialogStubShell == this)
				fgFindReplaceDialogStubShell= null;
			
			if (fWindow != null) {
				fWindow.getPartService().removePartListener(this);
				fWindow= null;
			}
			fDialog= null;
			fPart= null;
			fPreviousPart= null;
			fPreviousTarget= null;
		}

		/*
		 * @see IPartListener#partOpened(IWorkbenchPart)
		 */
		public void partOpened(IWorkbenchPart part) {}

		/*
		 * @see IPartListener#partDeactivated(IWorkbenchPart)
		 */
		public void partDeactivated(IWorkbenchPart part) {}

		/*
		 * @see IPartListener#partBroughtToTop(IWorkbenchPart)
		 */
		public void partBroughtToTop(IWorkbenchPart part) {}
		
		/**
		 * Checks if the dialogs shell is the same as the
		 * given <code>shell</code> and if not clears the stub
		 * and closes the dialog.
		 *
		 * @param shell the shell check
		 */
		public void checkShell(Shell shell) {
			if (fDialog != null && shell != fDialog.getParentShell()) {
				if (fgFindReplaceDialogStub == this)
					fgFindReplaceDialogStub= null;

				if(fgFindReplaceDialogStubShell == this)
					fgFindReplaceDialogStubShell= null;
				
				fDialog.close();
			}			
		}
	}

	/**
	 * Listener for disabling the dialog on shell close.
	 * <p>
	 * This stub is shared amongst <code>IWorkbenchPart</code>s.</p>
	 */
	private static FindReplaceDialogStub fgFindReplaceDialogStub;
	
	/** Listener for disabling the dialog on shell close.
	 * <p>
	 * This stub is shared amongst <code>Shell</code>s.</p>
	 */
	private static FindReplaceDialogStub fgFindReplaceDialogStubShell;

	/** The action's target */
	private ISTFindReplaceTarget fTarget;
	/** The part to use if the action is created with a part. */
	private IWorkbenchPart fWorkbenchPart;
	/**
	 * The shell to use if the action is created with a shell.
	 */
	private Shell fShell;

	/**
	 * Creates a new find/replace action for the given workbench part.
	 * <p>
	 * The action configures its visual representation from the given
	 * resource bundle.</p>
	 *
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 *   (described in <code>ResourceAction</code> constructor), or
	 *   <code>null</code> if none
	 * @param workbenchPart	 the workbench part
	 * @see ResourceAction#ResourceAction(ResourceBundle, String)
	 */
	public STFindReplaceAction(IWorkbenchPart workbenchPart) {
		super(fgBundleForConstructedKeys, prefix);
		Assert.isLegal(workbenchPart != null);
		setHelpContextId(IAbstractTextEditorHelpContextIds.FIND_ACTION);
		setActionDefinitionId(IWorkbenchCommandConstants.EDIT_FIND_AND_REPLACE);
		fWorkbenchPart= workbenchPart;
		update();
	}

	/**
	 * Creates a new find/replace action for the given target and shell.
	 * <p>
	 * This can be used without having an IWorkbenchPart e.g. for
	 * dialogs or wizards.</p>
	 * <p>
	 * The action configures its visual representation from the given
	 * resource bundle.</p>
	 *
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 *   (described in <code>ResourceAction</code> constructor), or
	 *   <code>null</code> if none
	 * @param target the ISTFindReplaceTarget to use
	 * @param shell the shell
	 * @see ResourceAction#ResourceAction(ResourceBundle, String)
	 * 
	 */
	public STFindReplaceAction(Shell shell, ISTFindReplaceTarget target) {
		super(fgBundleForConstructedKeys, prefix);
		Assert.isLegal(target != null && shell != null);
		setHelpContextId(IAbstractTextEditorHelpContextIds.FIND_ACTION);
		setActionDefinitionId(IWorkbenchCommandConstants.EDIT_FIND_AND_REPLACE);
		fTarget= target;
		fShell= shell;
 		update();
	}

	/*
	 *	@see IAction#run()
	 */
	public void run() {
		if (fTarget == null){
			return;
		}	
		
		
		final STFindReplaceDialog dialog;
		final boolean isEditable;
		
		if(fShell == null) {
			if (fgFindReplaceDialogStub != null) {
				Shell shell= fWorkbenchPart.getSite().getShell();
				fgFindReplaceDialogStub.checkShell(shell);
			}
			if (fgFindReplaceDialogStub == null)
				fgFindReplaceDialogStub= new FindReplaceDialogStub(fWorkbenchPart.getSite());

			if (fWorkbenchPart instanceof ITextEditorExtension2)
				isEditable= ((ITextEditorExtension2) fWorkbenchPart).isEditorInputModifiable();
			else
				isEditable= fTarget.isEditable();
				
			dialog= fgFindReplaceDialogStub.getDialog();

		} else {
			if (fgFindReplaceDialogStubShell != null) {
				fgFindReplaceDialogStubShell.checkShell(fShell);
			}
			if (fgFindReplaceDialogStubShell == null)
				fgFindReplaceDialogStubShell= new FindReplaceDialogStub(fShell);

			isEditable= fTarget.isEditable();
			dialog= fgFindReplaceDialogStubShell.getDialog();
		}
		
		dialog.updateTarget(fTarget, isEditable, true);
		dialog.open();					
	}

	/*
	 * @see IUpdate#update()
	 */
	public void update() {
		if(fShell == null){
			if (fWorkbenchPart != null)
				fTarget= (ISTFindReplaceTarget) fWorkbenchPart.getAdapter(ISTFindReplaceTarget.class);
			else
				fTarget= null;
		}
		
		if (fTarget != null){
			fTarget.setFindAction(this);
		}
		
		setEnabled(fTarget != null && fTarget.canPerformFind());
	}

}
