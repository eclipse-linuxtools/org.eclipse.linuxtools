/*******************************************************************************
 * Copyright (c) 2006 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kyu Lee <klee@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.changelog.core.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.IAction;

public class PrepareCommitHandler  extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
	
		IAction exampleAction = new PrepareCommitAction() {
			@Override
			public void run() {
				//setSelection(new StructuredSelection(rm));
				doRun();
			}
		};

		exampleAction.run();
		
		return null;
	}

}
