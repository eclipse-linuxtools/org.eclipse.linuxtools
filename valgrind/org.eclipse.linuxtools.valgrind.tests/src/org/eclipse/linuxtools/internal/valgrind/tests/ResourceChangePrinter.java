/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.tests;

import java.io.PrintStream;
import java.io.PrintWriter;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;

public class ResourceChangePrinter implements IResourceChangeListener {
	
	private PrintWriter resourceChangeWriter;

	public ResourceChangePrinter(PrintStream out) {
		resourceChangeWriter = new PrintWriter(out);
	}
	
	public void resourceChanged(IResourceChangeEvent event) {
		switch (event.getType()) {
		case IResourceChangeEvent.POST_BUILD:
			resourceChangeWriter.println("POST_BUILD " + event.getSource()); //$NON-NLS-1$
			break;
		case IResourceChangeEvent.PRE_BUILD:
			resourceChangeWriter.println("PRE_BUILD " + event.getSource()); //$NON-NLS-1$
			break;
		case IResourceChangeEvent.PRE_DELETE:
			resourceChangeWriter.println("PRE_DELETE " + event.getResource()); //$NON-NLS-1$
			break;
		case IResourceChangeEvent.PRE_REFRESH:
			resourceChangeWriter.println("PRE_REFRESH " + event.getResource()); //$NON-NLS-1$
			break;
		case IResourceChangeEvent.POST_CHANGE:
			resourceChangeWriter.println("POST_CHANGE "); //$NON-NLS-1$
			printDelta(event.getDelta(), 0);
			break;
		}
	}

	private void printDelta(IResourceDelta delta, int depth) {
		for (int i = 0; i < depth; i++) {
			resourceChangeWriter.print("\t"); //$NON-NLS-1$
		}
		switch (delta.getKind()) {
		case IResourceDelta.ADDED:
			resourceChangeWriter.print("ADDED "); //$NON-NLS-1$
			break;
		case IResourceDelta.CHANGED:
			resourceChangeWriter.print("CHANGED "); //$NON-NLS-1$
			break;
		case IResourceDelta.REMOVED:
			resourceChangeWriter.print("REMOVED "); //$NON-NLS-1$
			break;
		}
		if (delta.getResource() != null) {
			resourceChangeWriter.println(delta.getResource());
		}
		for (IResourceDelta child : delta.getAffectedChildren()) {
			printDelta(child, depth + 1);
		}
	}
}