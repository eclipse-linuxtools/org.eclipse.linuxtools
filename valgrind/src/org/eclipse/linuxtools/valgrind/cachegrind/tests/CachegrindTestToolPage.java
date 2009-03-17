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
package org.eclipse.linuxtools.valgrind.cachegrind.tests;

import org.eclipse.linuxtools.valgrind.cachegrind.CachegrindToolPage;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Spinner;

public class CachegrindTestToolPage extends CachegrindToolPage {
	
	public Button getCacheButton() {
		return cacheButton;
	}

	public Button getBranchButton() {
		return branchButton;
	}

	public Spinner getI1SizeSpinner() {
		return i1SizeSpinner;
	}

	public Spinner getI1AssocSpinner() {
		return i1AssocSpinner;
	}

	public Spinner getI1LineSizeSpinner() {
		return i1LineSizeSpinner;
	}

	public Button getI1Button() {
		return i1Button;
	}

	public Spinner getD1SizeSpinner() {
		return d1SizeSpinner;
	}

	public Spinner getD1AssocSpinner() {
		return d1AssocSpinner;
	}

	public Spinner getD1LineSizeSpinner() {
		return d1LineSizeSpinner;
	}

	public Button getD1Button() {
		return d1Button;
	}

	public Spinner getL2SizeSpinner() {
		return l2SizeSpinner;
	}

	public Spinner getL2AssocSpinner() {
		return l2AssocSpinner;
	}

	public Spinner getL2LineSizeSpinner() {
		return l2LineSizeSpinner;
	}

	public Button getL2Button() {
		return l2Button;
	}
	
	@Override
	public void checkI1Enablement() {
		super.checkI1Enablement();
	}
	
	@Override
	public void checkD1Enablement() {
		super.checkD1Enablement();
	}
	
	@Override
	public void checkL2Enablement() {
		super.checkL2Enablement();
	}
}
