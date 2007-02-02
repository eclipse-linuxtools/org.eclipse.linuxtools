/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/

package org.eclipse.cdt.oprofile.ui.sample;

import java.text.NumberFormat;

import org.eclipse.cdt.oprofile.ui.internal.IProfileElement;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;


/**
 * @author keiths
 */

/*
 * Columns
 * 
 * SystemRoot:
 * "", samples, percentage, image name, ""
 * 
 * Session:
 * "", samples, percentage, image name, ""
 * 
 * ProfileImage:
 * "", samples, percentage, symbol name, ""
 * 
 * SampleSymbol:
 * "", samples, percentage, file, line
 * 
 * Sample: (disallow?)
 * vma, samples, percentage, file, line
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
			case SampleView.COLUMN_VMA:
				if (obj.getAddress() != null)
					result = obj.getAddress();
				break;
				
			case SampleView.COLUMN_SAMPLES:
				result = Integer.toString(obj.getSampleCount());
				break;
				
			case SampleView.COLUMN_PERCENT:
				float total = (float) obj.getParent().getSampleCount();
				float count = obj.getSampleCount();
				float percent = count / total * (float) 100.0;
				NumberFormat nf = NumberFormat.getInstance();
				nf.setMaximumFractionDigits(2);
				nf.setMinimumFractionDigits(2);
				result = nf.format(percent);
				break;
				
			case SampleView.COLUMN_NAME:
				if (obj.getFileName() != null)
					result = obj.getFileName();
				break;
				
			case SampleView.COLUMN_LINE:
				if (obj.getLineNumber() != 0)
					result = Integer.toString(obj.getLineNumber());
				break;
		}
		
		return result;
	}
}
