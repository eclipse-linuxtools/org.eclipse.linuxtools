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
package org.eclipse.linuxtools.valgrind.tests;

import org.eclipse.linuxtools.valgrind.launch.ExportWizardConstants;
import org.eclipse.linuxtools.valgrind.launch.Messages;
import org.eclipse.linuxtools.valgrind.launch.ValgrindExportWizard;
import org.eclipse.linuxtools.valgrind.launch.ValgrindExportWizardPage;

public class ValgrindTestExportWizard extends ValgrindExportWizard {
	
	@Override
	protected ValgrindExportWizardPage getWizardPage() {
		return new ValgrindTestExportWizardPage(Messages.getString("ValgrindExportWizard.Page_name"), ExportWizardConstants.WIZARD_TITLE, null); //$NON-NLS-1$
	}

}
