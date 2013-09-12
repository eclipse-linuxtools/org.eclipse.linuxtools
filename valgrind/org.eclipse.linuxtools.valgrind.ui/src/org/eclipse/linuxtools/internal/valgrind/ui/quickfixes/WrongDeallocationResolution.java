/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rafael Medeiros Teixeira <rafaelmt@linux.vnet.ibm.com> - initial API and implementation
*******************************************************************************/

package org.eclipse.linuxtools.internal.valgrind.ui.quickfixes;

import java.util.ArrayList;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.linuxtools.internal.valgrind.core.ValgrindError;
import org.eclipse.linuxtools.internal.valgrind.core.ValgrindStackFrame;
import org.eclipse.linuxtools.internal.valgrind.ui.Messages;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindUIPlugin;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindViewPart;
import org.eclipse.linuxtools.valgrind.core.IValgrindMessage;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * Quick-fix for the Wrong deallocation function issue reported by memcheck.
 * Switches the deallocation function ("free" or "delete") accordingly.
 *
 * @author rafaelmt
 */
public class WrongDeallocationResolution extends AbstractValgrindMarkerResolution {
	private static final String DELETE = "delete"; //$NON-NLS-1$
	private static final String FREE = "free"; //$NON-NLS-1$
	private static final String NEW = "new"; //$NON-NLS-1$
	private int allocLine;
	private int allocOffset;
	private int allocLength;

	@Override
	public void apply(IMarker marker, IDocument document) {
		try {
			IASTNode astNode = getIASTNode(marker, document);
			if(astNode != null) {
				int nodeLength = astNode.getFileLocation().getNodeLength();
				int nodeOffset = astNode.getFileLocation().getNodeOffset();
				String content = document.get(nodeOffset, nodeLength);
				if(content.contains(DELETE)){
					String allocFunction = getAllocFunction(marker, document);
					if(allocFunction.contains(NEW)){
						content = document.get(nodeOffset, nodeLength).replace(DELETE, DELETE + "[]"); //$NON-NLS-1$
						document.replace(nodeOffset, nodeLength, content);
					} else {
						addParentheses(astNode, document);
						if(content.contains("[")){ //$NON-NLS-1$
							removeBrackets(astNode, document);
						}
						content = document.get(nodeOffset, nodeLength).replace(DELETE, FREE);
						document.replace(nodeOffset, nodeLength, content);
					}
				} else if(content.contains(FREE)){
					if(getAllocFunction(marker, document).contains("[")){ //$NON-NLS-1$
						content = content.concat("[]"); //$NON-NLS-1$
					}
					content = content.replace(FREE, DELETE);
					document.replace(nodeOffset, nodeLength, content);
				}

				IValgrindMessage message = getMessage(marker);
				removeMessage(message.getParent());
				ValgrindStackFrame nestedStackFrame = getStackBottom(getNestedStack(message.getParent()));
				int nestedLine = nestedStackFrame.getLine();
				String nestedFile = nestedStackFrame.getFile();
				removeMarker(nestedFile, nestedLine, marker.getType());
				marker.delete();
			}
		} catch (BadLocationException e ){
			Status status = new Status(IStatus.ERROR, ValgrindUIPlugin.PLUGIN_ID, null, e);
			String title = Messages.getString("ValgrindMemcheckQuickFixes.Valgrind_error_title"); //$NON-NLS-1$
			String message = Messages.getString("ValgrindMemcheckQuickFixes.Error_applying_quickfix"); //$NON-NLS-1$
			showErrorMessage(title, message, status);
		} catch (CoreException e ){
			Status status = new Status(IStatus.ERROR, ValgrindUIPlugin.PLUGIN_ID, null, e);
			String title = Messages.getString("ValgrindMemcheckQuickFixes.Valgrind_error_title"); //$NON-NLS-1$
			String message = Messages.getString("ValgrindMemcheckQuickFixes.Error_applying_quickfix"); //$NON-NLS-1$
			showErrorMessage(title, message, status);
		} catch (ValgrindMessagesException e){
			Status status = new Status(IStatus.ERROR, ValgrindUIPlugin.PLUGIN_ID, Messages.getString("ValgrindMemcheckQuickFixes.Error_finding_messages"), null); //$NON-NLS-1$
			String title = Messages.getString("ValgrindMemcheckQuickFixes.Valgrind_error_title"); //$NON-NLS-1$
			String message = Messages.getString("ValgrindMemcheckQuickFixes.Error_applying_quickfix"); //$NON-NLS-1$
			showErrorMessage(title, message, status);
		}
	}

	@Override
	public String getLabel() {
		return Messages.getString("ValgrindMemcheckQuickFixes.Wrong_dealloc_label"); //$NON-NLS-1$
	}

	/**
	 * Displays a pop-up indicating that an error occurred
	 * @param title The title of the pop-up window
	 * @param message The message of the pop-up window
	 * @param status {@link IStatus} containing information about the error
	 */
	private void showErrorMessage(String title, String message, IStatus status){
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		ErrorDialog.openError(shell, title, message, status);
	}

	/**
	 * Adds parentheses to a function call (if necessary)
	 * @param node {@link IASTNode} containing the function call
	 * @throws BadLocationException
	 */
	private void addParentheses(IASTNode node, IDocument document) throws BadLocationException{
		IASTNode[] children = node.getChildren();
		if(children.length > 0 && !children[0].getRawSignature().contains("(")) { //$NON-NLS-1$
			IASTNode childNode = children[0];
			int childNodeLength = childNode.getFileLocation().getNodeLength();
			int childNodeOffset = childNode.getFileLocation().getNodeOffset();
			String childContent = document.get(childNodeOffset, childNodeLength);
			String newChild = "(".concat(childContent).concat(")");   //$NON-NLS-1$//$NON-NLS-2$
			// Skewed 1 char to left to remove space before parentheses
			document.replace(childNodeOffset - 1, childNodeLength + 1, newChild);
		}
	}

	/**
	 * Returns the allocation function that relates to the given marker
	 * @param marker {@link IMarker} object that points to where the wrong de-allocation function is
	 * @return {@link String} object containing the allocation function
	 * @throws BadLocationException
	 * @throws ValgrindMessagesException
	 */
	private String getAllocFunction(IMarker marker, IDocument document) throws BadLocationException, ValgrindMessagesException {
		IValgrindMessage allocMessage = null;
		String file = marker.getResource().getName();
		int line = marker.getAttribute(IMarker.LINE_NUMBER, 0);

		IValgrindMessage[] wrongDeallocMessages = getMessagesByText(Messages.getString("ValgrindMemcheckQuickFixes.Wrong_dealloc_message")); //$NON-NLS-1$
		for (IValgrindMessage wrongDeallocMessage : wrongDeallocMessages) {
			ValgrindStackFrame stackBottom = getStackBottom(wrongDeallocMessage);
			int stackBottomLine = stackBottom.getLine();
			String stackBottomFile = stackBottom.getFile();
			if(stackBottomLine == line && file != null && file.equals(stackBottomFile)){
				allocMessage = getStackBottom(getNestedStack(wrongDeallocMessage));
			}
		}
		if(allocMessage instanceof ValgrindStackFrame){
			allocLine = ((ValgrindStackFrame)allocMessage).getLine() - 1;
			allocOffset = document.getLineOffset(allocLine);
			allocLength = document.getLineLength(allocLine);
			return document.get(allocOffset, allocLength);
		}
		return null;
	}

	/**
	 * Remove array brackets (if present)
	 * @param node {@link IASTNode} from which the brackets will be removed
	 * @throws BadLocationException
	 */
	private void removeBrackets(IASTNode node, IDocument document) throws BadLocationException{
		int nodeLength = node.getFileLocation().getNodeLength();
		int nodeOffset = node.getFileLocation().getNodeOffset();
		String content = document.get(nodeOffset, nodeLength);
		String newContent = content.replace("[","").replace("]","");  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$//$NON-NLS-4$
		document.replace(nodeOffset, nodeLength, newContent);
	}

	/**
	 * Returns all of the messages from the currently active Valgrind view that
	 * contains a given {@link String} in their description.
	 * @param text the {@link String} to match the Valgrind messages' descriptions
	 * @return
	 */
	private IValgrindMessage[] getMessagesByText(String text) throws ValgrindMessagesException{
		ValgrindViewPart valgrindView = ValgrindUIPlugin.getDefault().getView();
		ArrayList<IValgrindMessage> foundMessages = new ArrayList<IValgrindMessage>();

		if(valgrindView == null){
			throw new ValgrindMessagesException();
		}
		IValgrindMessage[] messages = valgrindView.getMessages();

		if(messages == null || messages.length == 0){
			throw new ValgrindMessagesException();
		}

		for (IValgrindMessage message : messages) {
			if(message.getText().contains(text)){
				foundMessages.add(message);
			}
		}
		IValgrindMessage[] foundMessagesArray = new IValgrindMessage[foundMessages.size()];
		foundMessages.toArray(foundMessagesArray);
		return foundMessagesArray;
	}

	/**
	 * Return the last nested element from a given {@link ValgrindMessage}, or null if there are
	 * no nested messages.
	 * @param message
	 * @return The {@link ValgrindStackFrame} in the bottom of the nested stack
	 */
	private ValgrindStackFrame getStackBottom(IValgrindMessage message){
		ValgrindStackFrame stackBottom = null;
		IValgrindMessage[] children = message.getChildren();
		for (IValgrindMessage child : children) {
			if(child instanceof ValgrindStackFrame){
				stackBottom = (ValgrindStackFrame) child;
			}
		}
		return stackBottom;
	}

	/**
	 * Returns the {@link ValgrindMessage} element from the Valgrind View that represents
	 * a given Marker
	 * @param marker the marker to which the ValgrindMessage relates
	 * @return {@link ValgrindMessage} that represents the {@link IMarker}
	 */
	private IValgrindMessage getMessage(IMarker marker) throws ValgrindMessagesException{
		IValgrindMessage message = null;
		String file = marker.getResource().getName();
		int line = marker.getAttribute(IMarker.LINE_NUMBER, 0);
		IValgrindMessage[] wrongDeallocMessages = getMessagesByText(Messages.getString("ValgrindMemcheckQuickFixes.Wrong_dealloc_message")); //$NON-NLS-1$
		for (IValgrindMessage wrongDeallocMessage : wrongDeallocMessages) {
			ValgrindStackFrame stackBottom = getStackBottom(wrongDeallocMessage);
			int stackBottomLine = stackBottom.getLine();
			String stackBottomFile = stackBottom.getFile();
			if(stackBottomLine == line && file != null && file.equals(stackBottomFile)){
				message = stackBottom;
			}
		}
		return message;
	}

	/**
	 * Returns the nested stack from a given ValgrindMessage in the Valgrind View
	 * @param message The message from which the stack will be acquired
	 * @return {@link ValgrindError} object containing the nested stack
	 */
	private ValgrindError getNestedStack(IValgrindMessage message){
		ValgrindError nestedError = null;
		IValgrindMessage[] children = message.getChildren();
		for (IValgrindMessage child : children) {
			if(child instanceof ValgrindError){
				nestedError = (ValgrindError)child;
			}
		}
		return nestedError;
	}

	/**
	 * Removes marker from file
	 *
	 * @param file The file containing the marker
	 * @param line The line in which the marker that will be removed is
	 * @param markerType The type of marker to be removed
	 * @throws CoreException
	 */
	private void removeMarker(String file, int line, String markerType) throws CoreException {
		IMarker[] markers = ResourcesPlugin.getWorkspace().getRoot().findMarkers(markerType, false, IResource.DEPTH_INFINITE);
		for (IMarker marker : markers) {
			if(marker.getAttribute(IMarker.LINE_NUMBER, 0) == line && marker.getResource().getName().equals(file)){
				marker.delete();
			}
		}
	}

	/**
	 * Removes message from Valgrind view.
	 * @param message The message to be removed
	 */
	private void removeMessage(IValgrindMessage message){
		ValgrindViewPart valgrindView = ValgrindUIPlugin.getDefault().getView();
		valgrindView.getMessagesViewer().getTreeViewer().remove(message);
	}
}
