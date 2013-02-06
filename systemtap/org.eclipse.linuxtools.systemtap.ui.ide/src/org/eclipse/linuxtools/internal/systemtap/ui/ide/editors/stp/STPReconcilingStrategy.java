/*******************************************************************************
 * Copyright (c) 2008 Phil Muldoon <pkmuldoon@picobot.org>.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Phil Muldoon <pkmuldoon@picobot.org> - initial API and implementation. 
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp;

import java.util.ArrayList;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.swt.widgets.Display;

/**
 * 
 * Reconciling strategy for Systemtap editor code folding positions. The positional aspects
 * of document tag discovery should really be placed with a position builder
 *
 */
public class STPReconcilingStrategy  implements IReconcilingStrategy,
               IReconcilingStrategyExtension {

	// Constants
    protected static final int STP_NO_TAG = 0;
    protected static final int STP_MULTILINE_COMMENT_TAG = 1;
    protected static final int STP_PROBE = 2;
    protected static final int STP_FUNCTION = 3;

    // Next Character Position
    protected int nextCharPosition = 0;

    // Current tag start
    protected int currentTagStart = 0;

    // Current tag end
    protected int currentTagEnd = 0;

	// List of positions
	protected final ArrayList<Position> documentPositionList = new ArrayList<Position>();
	
    // The end offset of the range to be scanned *//*
    protected int endOfDocumentPostion;

    private IDocument currentDocument;
    private STPEditor currentEditor;    
    
    /**
     * Sets the current editor.
     */
    public void setEditor(STPEditor editor) {
    	this.currentEditor = editor;
    }

    /**
     * Sets the current (ie working) document.
     */
    @Override
	public void setDocument(IDocument document) {
    	this.currentDocument = document;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategy#reconcile(org.eclipse.jface.text.IRegion)
	 */
	@Override
	public void reconcile(IRegion partition) {		
		// Just rebuild the whole document
        initialReconcile();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategy#reconcile(org.eclipse.jface.text.reconciler.DirtyRegion, org.eclipse.jface.text.IRegion)
	 */
	@Override
	public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
		//Just rebuild the whole document
        initialReconcile();		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension#initialReconcile()
	 */
	@Override
	public void initialReconcile() {
        endOfDocumentPostion = currentDocument.getLength();
        try {
			calculatePositions();
		} catch (BadLocationException e) {
			// Cannot reconcile, return
			return;
		}		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension#setProgressMonitor(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void setProgressMonitor(IProgressMonitor monitor) {
		
	}
	
	/**
	 * 
	 * From currentDocument, calculate beginning of document 
	 * to endOfDocumentPostion to build positions for code folding.
	 *  
	 * @throws BadLocationException
	 */
	private void calculatePositions() throws BadLocationException {
		// Clear old positions and reset to beginning of document
		documentPositionList.clear();
        nextCharPosition = 0;
        
        // Build the actual document positions
        buildPositions();
        
        // Paint the folding annotations in the background.
        Display.getDefault().asyncExec(new Runnable() {
            @Override
			public void run() {
            	currentEditor.updateFoldingStructure(documentPositionList);
            }
        });
	}
	 
	/**
	 * 
	 * Start trying to guess if given char z, what - if any - tag this
	 * begins. 
	 * 
	 * @param location - location of current position
	 * @return - tag type, if any
	 * 
	 * @throws BadLocationException
	 */
	private int classifyComponent(int location) throws BadLocationException {
        int deltaLocation = location;
		char ch = currentDocument.getChar(deltaLocation);
        switch (ch) {
        	// The 'comment' case.
        	case '/':
        		deltaLocation++;
        		ch = currentDocument.getChar(deltaLocation);
        		if (ch == '*') {
        			currentTagStart = location;
        			deltaLocation++;
        			nextCharPosition = deltaLocation;
        			return STP_MULTILINE_COMMENT_TAG;
        		}
        		break;
        	// The 'probe' case.
        	case 'p':
        		if (isProbe()) {
        			currentTagStart = location;
        			return STP_PROBE;        			
        		}
        		
        	// The 'function' case.
        	case 'f':
        		if (isFunction()) {
        			currentTagStart = location;
        			return STP_FUNCTION;        			
        		}
        	// No tag, don't fold region.
        	default:
        		break;
        }
        return STP_NO_TAG;
	}
	
	/**
	 * 
	 * Build a list of locations to mark beginning and end of folding regions.
	 * 
	 * @throws BadLocationException
	 */
	private void buildPositions() throws BadLocationException {
        while (nextCharPosition < endOfDocumentPostion) {
        	switch (classifyComponent(nextCharPosition)) 
        	{
        		// All of these cases have found the beginning of a tag
        	    // to start folding. Each element must now be find
        		// the end of the region it represents.
            	case STP_MULTILINE_COMMENT_TAG:
            		currentTagEnd = findEndOfComment();
            		writePosition(currentTagStart,currentTagEnd);
            		nextCharPosition = currentTagStart + currentTagEnd;
            		break;
            	case STP_PROBE:
            	case STP_FUNCTION:
            		currentTagEnd = findEndOfProbeOrFunction();
            		writePosition(currentTagStart,currentTagEnd);
            		nextCharPosition = currentTagStart + currentTagEnd;
            		break;
            	default:
            		nextCharPosition++;
            		break;
            }
        }
	}
	
    /**
     * 
     * Write a Position to the position list.
     * 
     * @param startOffset - start of position in the document.
     * @param length - length of position.
     * 
     */
    protected void writePosition(int startOffset, int length) {
    	if (length > 0)
    		documentPositionList.add(new Position(startOffset, length));
    }

    private boolean isProbe() throws BadLocationException {
		return matchKeyWord("probe"); //$NON-NLS-1$
    }
    
    private boolean isFunction() throws BadLocationException {
		return matchKeyWord("function"); //$NON-NLS-1$
    }

    private boolean matchKeyWord(String word) throws BadLocationException {
    	StringBuffer keyWord = new StringBuffer();
    	int location = nextCharPosition;
    	while (location < endOfDocumentPostion) {
    		char ch = currentDocument.getChar(location);
    		if ((ch == ' ') || (!Character.isLetter(ch)))
    			break;
    		else 
    			keyWord.append(ch);
    		location++;
    	}
    	if (keyWord.toString().compareTo(word) == 0)
    		return true;
    	return false;    	
    }
    
    private int findEndOfProbeOrFunction() throws BadLocationException {
    	int bracketCount = 0;
    	boolean firstBracket = false;
    	char ch;
    	
    	while (nextCharPosition < endOfDocumentPostion) {
    		ch = currentDocument.getChar(nextCharPosition);
    		if (ch == '{') {
    			firstBracket = true;
    			bracketCount++;
    		}
    		if (ch == '}') 
    			bracketCount--;    		
    		if ((bracketCount == 0) && (firstBracket))
    			return (nextCharPosition-currentTagStart)+2;
    		nextCharPosition++;
    	}
    	return -1;
    }

    private int findEndOfComment() throws BadLocationException {
    	while (nextCharPosition < endOfDocumentPostion) {
    		char ch = currentDocument.getChar(nextCharPosition);
    		if (ch == '*') {
    			nextCharPosition++;
        		ch = currentDocument.getChar(nextCharPosition);
    			if (ch == '/') 
    				return (nextCharPosition-currentTagStart)+2;
    		}
    		nextCharPosition++;
    	}
    	return -1;
    }
}