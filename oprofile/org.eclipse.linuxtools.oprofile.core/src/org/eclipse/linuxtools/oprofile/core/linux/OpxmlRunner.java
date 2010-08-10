/*******************************************************************************
 * Copyright (c) 2004, 2008, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *    Kent Sebastian <ksebasti@redhat.com> - 
 *******************************************************************************/ 
package org.eclipse.linuxtools.oprofile.core.linux;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.xml.parsers.SAXParserFactory;

import org.eclipse.linuxtools.oprofile.core.OprofileCorePlugin;
import org.eclipse.linuxtools.oprofile.core.OpxmlException;
import org.eclipse.linuxtools.oprofile.core.opxml.OprofileSAXHandler;
import org.eclipse.linuxtools.oprofile.core.opxml.XMLProcessor;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * This class will run opxml.
 * 
 * opxml is a small program which acts as a textual interface between Oprofile and
 * BFD and the oprofile plugins. 
 */
public class OpxmlRunner {
	private OprofileSAXHandler _handler;
	private String _pathToOpxml;

	public OpxmlRunner(String pathToOpxml) {
		//assume that the path given is valid
		_pathToOpxml = pathToOpxml;
	}
	
	/**
	 * Returns the current XMLProcessor handling parsing of opxml output.
	 * @return the processor
	 */
	public XMLProcessor getProcessor() {
		return _handler.getProcessor();
	}
	
	/**
	 * Runs opxml with the given arguments.
	 * @param args the arguments to pass to opxml
	 * @param callData any callData to pass to the processor
	 * @return boolean indicating the success/failure of opxml
	 * @throws OpxmlException 
	 */
	public boolean run(String[] args, Object callData) {
		XMLReader reader = null;
		_handler = OprofileSAXHandler.getInstance(callData);
		
		// Create XMLReader
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
			reader = factory.newSAXParser().getXMLReader();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		// Set content/error handlers
		reader.setContentHandler(_handler);
		reader.setErrorHandler(_handler);
		
		// Setup args
		String[] cmdArray = new String[args.length + 1];
		cmdArray[0] = _pathToOpxml;
		System.arraycopy(args, 0, cmdArray, 1, args.length);
		
		// Run opxml
		try {
			Process p = Runtime.getRuntime().exec(cmdArray);
			BufferedReader bi = new BufferedReader(new InputStreamReader(p.getInputStream()));
			reader.parse(new InputSource(bi));	
			int ret = p.waitFor();
			if (ret != 0) {
				//System.out.println("error running opxml");
				return false;
			}
			
			return true;
		} catch (SAXException e) {
			e.printStackTrace();
			OprofileCorePlugin.showErrorDialog("opxmlSAXParseException", null); //$NON-NLS-1$
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
//			System.out.println("IOException: " + e.getMessage());
			e.printStackTrace();
			OprofileCorePlugin.showErrorDialog("opxmlIOException", null); //$NON-NLS-1$
		}
		
		return false;
	}
}
