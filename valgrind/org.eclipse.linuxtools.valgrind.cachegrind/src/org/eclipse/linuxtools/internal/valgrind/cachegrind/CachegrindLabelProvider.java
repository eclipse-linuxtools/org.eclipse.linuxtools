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
package org.eclipse.linuxtools.internal.valgrind.cachegrind;

import java.text.DecimalFormat;

import org.eclipse.cdt.core.model.util.CElementBaseLabels;
import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.linuxtools.internal.valgrind.cachegrind.model.CachegrindFile;
import org.eclipse.linuxtools.internal.valgrind.cachegrind.model.CachegrindFunction;
import org.eclipse.linuxtools.internal.valgrind.cachegrind.model.CachegrindLine;
import org.eclipse.linuxtools.internal.valgrind.cachegrind.model.CachegrindOutput;
import org.eclipse.linuxtools.internal.valgrind.cachegrind.model.ICachegrindElement;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class CachegrindLabelProvider extends CellLabelProvider {

	private static final String NAME_NOT_FOUND_SYMBOL = "???"; //$NON-NLS-1$

	protected CElementLabelProvider cLabelProvider = new CElementLabelProvider(CElementLabelProvider.SHOW_SMALL_ICONS | CElementLabelProvider.SHOW_PARAMETERS | CElementLabelProvider.SHOW_RETURN_TYPE) {
		@Override
		public int getTextFlags() {
			return super.getTextFlags() |CElementBaseLabels.M_FULLY_QUALIFIED;
		}
	};

	protected DecimalFormat df = new DecimalFormat("#,##0"); //$NON-NLS-1$

	private static final Image FUNC_IMG = AbstractUIPlugin.imageDescriptorFromPlugin(CachegrindPlugin.PLUGIN_ID, "icons/function_obj.gif").createImage(); //$NON-NLS-1$

	@Override
	public void update(ViewerCell cell) {
		ICachegrindElement element = ((ICachegrindElement) cell.getElement());
		int index = cell.getColumnIndex();

		if (index == 0) {
			if (element instanceof CachegrindFile) {
				// Try to use the CElementLabelProvider
				IAdaptable model = ((CachegrindFile) element).getModel();
				if (model != null) {
					cell.setText(cLabelProvider.getText(model));
					cell.setImage(cLabelProvider.getImage(model));
				} else { // Fall back
					String name = ((CachegrindFile) element).getName();
					cell.setText(name);
					cell.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE));
				}
			} else if (element instanceof CachegrindFunction) {
				// Try to use the CElementLabelProvider
				IAdaptable model = ((CachegrindFunction) element).getModel();
				if (model != null) {
					cell.setText(cLabelProvider.getText(model));
					cell.setImage(cLabelProvider.getImage(model));
				} else { // Fall back
					String name = ((CachegrindFunction) element).getName();
					cell.setText(name);
					cell.setImage(FUNC_IMG);
				}
			} else if (element instanceof CachegrindLine) {
				cell.setText(NLS.bind(Messages.getString("CachegrindViewPart.line"), ((CachegrindLine) element).getLine())); //$NON-NLS-1$
				cell.setImage(DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_INSTRUCTION_POINTER_TOP));
			} else if (element instanceof CachegrindOutput) {
				cell.setText(NLS.bind(Messages.getString("CachegrindViewPart.Total_PID"), ((CachegrindOutput) element).getPid())); //$NON-NLS-1$
				cell.setImage(DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_REGISTER));
			}
		} else if (element instanceof CachegrindFunction) {
			cell.setText(df.format(((CachegrindFunction) element).getTotals()[index - 1]));
		} else if (element instanceof CachegrindLine) {
			cell.setText(df.format(((CachegrindLine) element).getValues()[index - 1]));
		} else if (element instanceof CachegrindOutput) {
			cell.setText(df.format(((CachegrindOutput) element).getSummary()[index - 1]));
		}
	}

	public CElementLabelProvider getCLabelProvider() {
		return cLabelProvider;
	}

	@Override
	public String getToolTipText(Object element) {
		String tooltip = null;
		if(element instanceof CachegrindFile){
			if(((CachegrindFile)element).getName().contains(NAME_NOT_FOUND_SYMBOL)){
				tooltip = Messages.getString("CachegrindViewPart.Tooltip_no_debug_info"); //$NON-NLS-1$
			}
		}

		if(element instanceof CachegrindFunction){
			if(((CachegrindFunction)element).getName().contains(NAME_NOT_FOUND_SYMBOL)){
				tooltip = Messages.getString("CachegrindViewPart.Tooltip_no_debug_info"); //$NON-NLS-1$
			}
		}
		return tooltip;
	}
}