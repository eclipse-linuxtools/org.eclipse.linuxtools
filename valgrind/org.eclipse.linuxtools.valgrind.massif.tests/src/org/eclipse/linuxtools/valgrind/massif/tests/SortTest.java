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
package org.eclipse.linuxtools.valgrind.massif.tests;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.linuxtools.valgrind.massif.MassifLaunchConstants;
import org.eclipse.linuxtools.valgrind.massif.MassifSnapshot;
import org.eclipse.linuxtools.valgrind.massif.MassifViewPart;
import org.eclipse.linuxtools.valgrind.ui.ValgrindUIPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class SortTest extends AbstractMassifTest {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		proj = createProjectAndBuild("alloctest"); //$NON-NLS-1$

		ILaunchConfiguration config = createConfiguration(proj.getProject());
		ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
		wc.setAttribute(MassifLaunchConstants.ATTR_MASSIF_STACKS, true);
		wc.doSave();
		doLaunch(config, "testStacks"); //$NON-NLS-1$
	}

	@Override
	protected void tearDown() throws Exception {
		deleteProject(proj);
		super.tearDown();
	}

	public void testSortSnapshots() throws Exception {
		checkSortColumn(0);
	}

	public void testSortTime() throws Exception {
		checkSortColumn(1);
	}

	public void testSortTotal() throws Exception {
		checkSortColumn(2);
	}

	public void testSortUseful() throws Exception {
		checkSortColumn(3);
	}

	public void testSortExtra() throws Exception {
		checkSortColumn(4);
	}

	public void testSortStacks() throws Exception {
		checkSortColumn(5);
	}

	private void checkSortColumn(int column) throws CoreException, Exception {
		MassifViewPart view = (MassifViewPart) ValgrindUIPlugin.getDefault()
				.getView().getDynamicView();
		TableViewer viewer = view.getTableViewer();
		TableColumn control = viewer.getTable().getColumn(column);

		// Test ascending
		control.notifyListeners(SWT.Selection, null);
		assertEquals(SWT.UP, viewer.getTable().getSortDirection());
		assertEquals(control, viewer.getTable().getSortColumn());
		checkOrder(viewer, column, true);

		// Test descending
		control.notifyListeners(SWT.Selection, null);
		assertEquals(SWT.DOWN, viewer.getTable().getSortDirection());
		assertEquals(control, viewer.getTable().getSortColumn());
		checkOrder(viewer, column, false);
	}

	private void checkOrder(TableViewer viewer, int column, boolean ascending) {
		TableItem[] items = viewer.getTable().getItems();
		for (int i = 0; i < items.length - 1; i++) {
			MassifSnapshot first = (MassifSnapshot) items[i].getData();
			MassifSnapshot second = (MassifSnapshot) items[i + 1].getData();

			switch (column) {
			case 0:
				assertTrue(ascending ? first.getNumber() <= second.getNumber() : first.getNumber() >= second.getNumber());
				break;
			case 1:
				assertTrue(ascending ? first.getTime() <= second.getTime() : first.getTime() >= second.getTime());
				break;
			case 2:
				assertTrue(ascending ? first.getTotal() <= second.getTotal() : first.getTotal() >= second.getTotal());
				break;
			case 3:
				assertTrue(ascending ? first.getHeapBytes() <= second.getHeapBytes() : first.getHeapBytes() >= second.getHeapBytes());
				break;
			case 4:
				assertTrue(ascending ? first.getHeapExtra() <= second.getHeapExtra() : first.getHeapExtra() >= second.getHeapExtra());
				break;
			case 5:
				assertTrue(ascending ? first.getStacks() <= second.getStacks() : first.getStacks() >= second.getStacks());
				break;
			default:
				fail();
			}
		}
	}
}
