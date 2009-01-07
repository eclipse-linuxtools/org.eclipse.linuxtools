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

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ValgrindStackFrame {
	protected String pc;
	protected String obj;
	protected String func;
	protected String dir;
	protected String file;
	protected int line;
	
	public ValgrindStackFrame(Node frameNode) {
		NodeList list = frameNode.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			Node n = list.item(i);
			if (n.getNodeName().equals("ip")) { //$NON-NLS-1$
				pc = n.getTextContent();
			}
			else if (n.getNodeName().equals("obj")) { //$NON-NLS-1$
				obj = n.getTextContent();
			}
			else if (n.getNodeName().equals("fn")) { //$NON-NLS-1$
				func = n.getTextContent();
			}
			else if (n.getNodeName().equals("dir")) { //$NON-NLS-1$
				dir = n.getTextContent();
			}
			else if (n.getNodeName().equals("file")) { //$NON-NLS-1$
				file = n.getTextContent();
			}
			else if (n.getNodeName().equals("line")) { //$NON-NLS-1$
				line = Integer.parseInt(n.getTextContent());
			}
		}
	}
	
	public String getDir() {
		return dir;
	}
	
	public String getFile() {
		return file;
	}
	
	public String getFunc() {
		return func;
	}
	
	public int getLine() {
		return line;
	}
	
	public String getObj() {
		return obj;
	}
	
	public String getPC() {
		return pc;
	}
	
}
