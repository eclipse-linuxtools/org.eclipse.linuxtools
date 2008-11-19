/*******************************************************************************
 * Copyright (c) 2004,2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *    Kent Sebastian <ksebasti@redhat.com> - 
 *******************************************************************************/ 

package org.eclipse.linuxtools.oprofile.ui.sample;

import java.text.NumberFormat;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.linuxtools.oprofile.ui.internal.IProfileElement;
import org.eclipse.swt.graphics.Image;


/*
 * Columns
 * 
 * SystemRoot:
 * percentage, samples, image name, "", ""
 * 
 * Session:
 * percentage, samples, image name, "", ""
 * 
 * OpModelImage:
 * percentage, samples, symbol name, "", ""
 * 
 * SampleSymbol:
 * percentage, samples, line, "", file
 * 
 * OpModelSample: (disallow?)
 * percentage, samples, line, vma, file
 * 
 */
public class SampleViewLabelProvider
	extends LabelProvider
	implements ITableLabelProvider
{	
	
	/**
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(Object, int)
	 */
	public Image getColumnImage(Object element, int columnIndex)
	{
		return null;
	}

	/**
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(Object, int)
	 */
	public String getColumnText(Object element, int columnIndex)
	{
		String result = ""; //$NON-NLS-1$
		IProfileElement obj = (IProfileElement) element;
		
		switch (columnIndex)
		{
			case SampleView.COLUMN_PERCENT:
				float total = (float) obj.getParent().getSampleCount();
				float count = obj.getSampleCount();
				float percent = count / total * (float) 100.0;
				NumberFormat nf = NumberFormat.getInstance();
				nf.setMaximumFractionDigits(2);
				nf.setMinimumFractionDigits(2);
				result = nf.format(percent);
				break;
				
			case SampleView.COLUMN_SAMPLES:
				result = Integer.toString(obj.getSampleCount());
				break;

			case SampleView.COLUMN_LINE:
				if (obj.getLineNumber() != 0)
					result = Integer.toString(obj.getLineNumber());
				break;
				
			case SampleView.COLUMN_VMA:
				if (obj.getAddress() != null)
					result = obj.getAddress();
				break;

			case SampleView.COLUMN_NAME:
				if (obj.getFileName() != null)
					result = obj.getFileName();
				break;
		}
		
		return result;
	}
}
