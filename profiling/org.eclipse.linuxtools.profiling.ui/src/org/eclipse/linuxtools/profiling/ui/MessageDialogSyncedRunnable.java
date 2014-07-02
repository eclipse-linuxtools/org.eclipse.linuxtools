/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    lufimtse :  Leo Ufimtsev lufimtse@redhat.com
 *******************************************************************************/

package org.eclipse.linuxtools.profiling.ui;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * <h1>UI sycned Message Dialogue </h1>
 * <p> 
 * This class is for launching common messages <b>from background threads</b> and <b>getting a return value.
 * </p>
 * <p> 
 * When you're not in a U.I thread, getting the 'shell' causes a runtime exception. <br>
 * Never the less, sometimes you need user feedback when executing a background job. <br>
 * (e.g missing flags when launching a profiling tools, ask the user if he wants the flag to be added). <br>
 * This class aims to implement wrappers for the standard openTYPE message dialogues so that <br>
 * they could be launched from any background thread. 
 * </p>
 *
 * <p> Note, methods in this subclass have a postfix of 'SyncedRunnable'.
 *  @since 3.1
 */
public class MessageDialogSyncedRunnable extends MessageDialog {

    /**
     * Calls parent, identical to parent implementation in all respects. <br>
     * <p> Neccessary to supress compiler warnings. Use static methods in this class instead </p>
     * <p> For details and paramater description, please see: <br> </p>
     * {@link org.eclipse.jface.dialogs.MessageDialog#MessageDialog(Shell, String, Image, String, int, String[], int) }
     */
    protected MessageDialogSyncedRunnable(Shell parentShell, String dialogTitle,
            Image dialogTitleImage, String dialogMessage, int dialogImageType,
            String[] dialogButtonLabels, int defaultIndex) {
        super(parentShell, dialogTitle, dialogTitleImage, dialogMessage, dialogImageType,
                dialogButtonLabels, defaultIndex);
    }

    /**
     * <h1>Open Question Dialogue.</h1>
     * <p> 
     * Identical to parent implementation {@link MessageDialog#openQuestion(Shell, String, String) openQuestion} <br>
     *  except that you do not need to provide a shell. 
     * </p>
     * <p> 
     * Convenience method to open a simple Yes/No question dialog. 
     * </p>
     *
     * @param title      the dialog's title, or <code>null</code> if none
     * @param message    the message
     * @return <code>true </code> if the user presses the Yes button,
     *         <code>false</code> otherwise
     */
    public static boolean openQuestionSyncedRunnable(final String title, final String message) {
       return openInSyncedRunnable(DialogueType.QUESTION, title, message);
    }

    /**
     * <h1>Open Confirmation Dialogue. </h1>
     * <p> 
     * Identical to parent implementation {@link MessageDialog#openConfirm(Shell, String, String) openConfirm}<br>
     * except that you do not need to provide a shell.
     * </p>
     *
     * <p> Convenience method to open a simple confirm (OK/Cancel) dialog. </p>
     *
     * @param title the dialog's title, or <code>null</code> if none
     * @param message the message
     * @return <code>true</code> if the user presses the OK button,
     *         <code>false</code> otherwise
     */
    public static boolean openConfirmSyncedRunnable(final String title, final String message) {
        return openInSyncedRunnable(DialogueType.CONFIRMATION, title, message);
    }

    /**
     * <h1>Open Information Dialogue.</h1>
     * <p>
     * Identical to parent implementation {@link MessageDialog#openInformation(Shell, String, String) openInformation} <br>
     * except that you do not need to provide a shell.
     * </p>
     * <p> 
     * Convenience method to open a simple confirm (OK/Cancel) dialog.
     * </p>
     *
     * @param title     the dialog's title, or <code>null</code> if none
     * @param message   the message
     */
    public static void openInformationSyncedRunnable(final String title, final String message) {
        //We discard boolean and don't return anything.
        openInSyncedRunnable(DialogueType.INFO, title, message);
     }

    /**
     * <h1>Open Error Dialogue.</h1>
     * <p>
     * Identical to parent implementation  {@link MessageDialog#openError(Shell, String, String) openError}<br>
     * except that you do not need to provide a shell:
     * </p>
     *<p> 
     *Convenience method to open a standard error dialog.
     *</p>
     *
     * @param title     the dialog's title, or <code>null</code> if none
     * @param message   the message
     */
    public static void openErrorSyncedRunnable(final String title, final String message) {
        //We discard boolean and don't return anything.
        openInSyncedRunnable(DialogueType.ERROR, title, message);
    }

    private static enum DialogueType {INFO, ERROR, CONFIRMATION, QUESTION}

    /* To prevent code duplication... */
    private static boolean openInSyncedRunnable(final DialogueType type, final String title, final String message) {

        //We define a 'final' variable that will be accessible in the runnable object.
        final BooleanWithGetSet userChoiceBool = new BooleanWithGetSet(false);

        //To generate U.I, we make a syncronised call the U.I thread,
        //otherwise we get an U.I exception.
        PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
            @Override
            public void run() {
                Shell parent = PlatformUI.getWorkbench().getDisplay().getActiveShell();
                switch (type) {
                case INFO:
                    MessageDialog.openInformation(parent, title, message);
                    break;
                case ERROR:
                    MessageDialog.openError(parent, title, message);
                    break;
                case CONFIRMATION:
                    boolean okPressed = MessageDialog.openConfirm(parent, title, message);
                    userChoiceBool.setVal(okPressed);
                    break;
                case QUESTION:
                    boolean okPressedQ = MessageDialog.openQuestion(parent, title, message);
                    userChoiceBool.setVal(okPressedQ);
                    break;
                default:
                    break;
                }
                return;
        }});
        return userChoiceBool.getVal();
    }
}
