/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/
package org.eclipse.cdt.oprofile.core.linux;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.cdt.oprofile.core.Oprofile;
import org.eclipse.cdt.oprofile.core.OprofileCorePlugin;
import org.eclipse.cdt.oprofile.core.opxml.OprofileSAXHandler;
import org.eclipse.cdt.oprofile.core.opxml.XMLProcessor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * This class will run opxml.
 * 
 * opxml is a small program which acts as a textual interface between Oprofile and
 * BFD  and the oprofile plugins. This is necessary because both Oprofile and BFD are
 * under the GPL, while Eclipse is licensed under the CPL. Since these two
 * licenses are not compatible, we must jump through a hoop or two to use BFD and
 * Oprofile in Eclipse.
 *
 * @author Keith Seitz <keiths@redhat.com>
 */
public class OpxmlRunner {
	private OprofileSAXHandler _handler;
	private static final String _OPXML_PROGRAM = _findOpxml();

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
	 */
	public boolean run(String[] args, Object callData) {
		
		// Don't even bother running if the kernel module wasn't loaded successfully
		if (!Oprofile.isKernelModuleLoaded()) {
			return false;
		}
		
		XMLReader reader = null;
		_handler = OprofileSAXHandler.getInstance(callData);
		
		try {
			// Create XMLReader
		        SAXParserFactory factory = SAXParserFactory.newInstance ();
		        reader = factory.newSAXParser ().getXMLReader ();
		} catch (Exception e) {
			System.out.println ("Exception creating SAXParser: " + e.getMessage ());
		}
		
		// Set content/error handlers
		reader.setContentHandler(_handler);
		reader.setErrorHandler(_handler);
		
		// Setup args
		String[] cmdArray = new String[args.length + 1];
		cmdArray[0] = _OPXML_PROGRAM;
		System.arraycopy(args, 0, cmdArray, 1, args.length);
		
		// Run opxml
		try {
			Process p = Runtime.getRuntime().exec(cmdArray);
			BufferedReader bi = new BufferedReader(new InputStreamReader(p.getInputStream()));
			reader.parse(new InputSource(bi));			
			if (p.waitFor() != 0) {
				//System.out.println("error running opxml");
				return false;
			}
			
			return true;
		} catch (SAXException e) {
			System.out.println("SAXException: " + e.getMessage());
			e.printStackTrace();
		} catch (InterruptedException e) {
			System.out.println("InterruptedException: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("IOException: " + e.getMessage());
			e.printStackTrace();
		}
		
		return false;
	}
	
	private static String _findOpxml() {
		URL url = OprofileCorePlugin.getDefault().find(new Path("$os$/opxml")); //$NON-NLS-1$
		if (url != null) {
			try {
				return Platform.asLocalURL(url).getPath();
			} catch (IOException e) {
			}
		}
		
		// TODO: display error in unlikely event opxml not found
		// (which could only happen in case of corrupt installation)
		return null;
	}
}
