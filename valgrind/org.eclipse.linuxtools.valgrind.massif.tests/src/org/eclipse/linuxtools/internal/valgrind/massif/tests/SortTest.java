/*******************************************************************************
 * Copyright (c) 2009, 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.massif.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.linuxtools.internal.valgrind.massif.MassifLaunchConstants;
import org.eclipse.linuxtools.internal.valgrind.massif.MassifSnapshot;
import org.eclipse.linuxtools.internal.valgrind.massif.MassifViewPart;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindUIPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class SortTest extends AbstractMassifTest {

    @BeforeEach
    public void prep() throws Exception {
        proj = createProjectAndBuild("alloctest"); //$NON-NLS-1$

        ILaunchConfiguration config = createConfiguration(proj.getProject());
        ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
        wc.setAttribute(MassifLaunchConstants.ATTR_MASSIF_STACKS, true);
        wc.doSave();
        doLaunch(config, "testStacks"); //$NON-NLS-1$
    }

    @Override
    @AfterEach
    public void tearDown() throws CoreException {
        deleteProject(proj);
        super.tearDown();
    }

    @ParameterizedTest @ValueSource(ints = { 0, 1, 2, 3, 4, 5 })
    public void checkSortColumn(int column) {
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
                assertTrue(ascending ? first.getNumber() <= second.getNumber()
                        : first.getNumber() >= second.getNumber());
                break;
            case 1:
                assertTrue(ascending ? first.getTime() <= second.getTime()
                        : first.getTime() >= second.getTime());
                break;
            case 2:
                assertTrue(ascending ? first.getTotal() <= second.getTotal()
                        : first.getTotal() >= second.getTotal());
                break;
            case 3:
                assertTrue(ascending ? first.getHeapBytes() <= second
                        .getHeapBytes() : first.getHeapBytes() >= second
                        .getHeapBytes());
                break;
            case 4:
                assertTrue(ascending ? first.getHeapExtra() <= second
                        .getHeapExtra() : first.getHeapExtra() >= second
                        .getHeapExtra());
                break;
            case 5:
                assertTrue(ascending ? first.getStacks() <= second.getStacks()
                        : first.getStacks() >= second.getStacks());
                break;
            default:
                fail();
            }
        }
    }
}
