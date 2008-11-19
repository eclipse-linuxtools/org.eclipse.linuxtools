/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.valgrind.memcheck;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.valgrind.core.ValgrindPlugin;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ValgrindError {
	protected String unique;
	protected String tid;
	protected String kind;
	protected String what;
	protected String pid; 
	protected ArrayList<ValgrindStackFrame> frames;
	protected String auxwhat;
	protected ArrayList<ValgrindStackFrame> auxframes;

	public ValgrindError(Node errorNode) throws CoreException, IOException {
		pid = findPid(errorNode);

		NodeList list = errorNode.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			Node n = list.item(i);
			if (n.getNodeName().equals("unique")) { //$NON-NLS-1$
				unique = n.getTextContent();
			}
			else if (n.getNodeName().equals("tid")) { //$NON-NLS-1$
				tid = n.getTextContent();
			}
			else if (n.getNodeName().equals("kind")) { //$NON-NLS-1$
				kind = n.getTextContent();
			}
			else if (n.getNodeName().equals("what")) { //$NON-NLS-1$
				what = n.getTextContent();
			}
			else if (n.getNodeName().equals("stack")) { //$NON-NLS-1$
				NodeList frameNodes = n.getChildNodes();
				// main stack or aux stack?
				ArrayList<ValgrindStackFrame> stack;
				if (auxwhat == null) {
					frames = new ArrayList<ValgrindStackFrame>();
					stack = frames;
				}
				else {
					auxframes = new ArrayList<ValgrindStackFrame>();
					stack = auxframes;
				}
				for (int j = 0; j < frameNodes.getLength(); j++) {
					Node m = frameNodes.item(j);
					if (m.getNodeName().equals("frame")) { //$NON-NLS-1$
						stack.add(new ValgrindStackFrame(m));	
					}
				}
			}
			else if (n.getNodeName().equals("auxwhat")) { //$NON-NLS-1$
				auxwhat = n.getTextContent();
			}
		}
		
		createMarker();
	}

	protected void createMarker() throws CoreException, IOException {
		IMarker marker = null;		
		// find the topmost stack frame within the workspace to annotate with marker
		for (int i = 0; i < frames.size() && marker == null; i++) {
			ValgrindStackFrame frame = frames.get(i);
			if (frame.getDir() != null && frame.getFile() != null && frame.getLine() > 0) {
				String strpath = frame.getDir() + Path.SEPARATOR + frame.getFile();
				File file = new File(strpath);
				Path path = new Path(file.getCanonicalPath());
				
				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				IFile resource = root.getFileForLocation(path);
				if (resource != null && resource.exists()) {
					marker = resource.createMarker(ValgrindPlugin.MARKER_TYPE);
					marker.setAttribute(IMarker.MESSAGE, what);
					marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
					marker.setAttribute(IMarker.LINE_NUMBER, frame.getLine());
				}
			}
		}
	}

	protected String findPid(Node errorNode) {
		String pid = null;
		NodeList pidList = errorNode.getOwnerDocument().getElementsByTagName("pid"); //$NON-NLS-1$
		if (pidList.getLength() > 0) {
			pid = pidList.item(0).getTextContent();
		}
		return pid;
	}

	public ArrayList<ValgrindStackFrame> getFrames() {
		return frames;
	}

	public String getKind() {
		return kind;
	}

	public String getTid() {
		return tid;
	}

	public String getUnique() {
		return unique;
	}

	public String getWhat() {
		return what;
	}

	public String getPid() {
		return pid;
	}
	
	public String getAuxWhat() {
		return auxwhat;
	}
	
	public ArrayList<ValgrindStackFrame> getAuxFrames() {
		return auxframes;
	}
}
