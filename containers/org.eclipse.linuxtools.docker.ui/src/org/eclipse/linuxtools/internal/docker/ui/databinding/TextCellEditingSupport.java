package org.eclipse.linuxtools.internal.docker.ui.databinding;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ObservableValueEditingSupport;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * Support for Cell editing 
 *
 * @see http://www.vogella.com/tutorials/EclipseDataBinding/article.html#jfacedb_viewer
 */
public class TextCellEditingSupport extends ObservableValueEditingSupport {

	/** The name of the property to observe during edition. */
	private final String propertyName;
	
	public TextCellEditingSupport(final ColumnViewer viewer, final DataBindingContext dbc, final String propertyName) {
		super(viewer, dbc);
		this.propertyName = propertyName;
	}

	@Override
	protected IObservableValue doCreateCellEditorObservable(final CellEditor cellEditor) {
		return WidgetProperties.text(SWT.Modify).observe(cellEditor.getControl());
	}

	@Override
	protected IObservableValue doCreateElementObservable(final Object element, final ViewerCell cell) {
		return BeanProperties.value(this.propertyName).observe(element);
	}

	@Override
	protected CellEditor getCellEditor(final Object element) {
		return new TextCellEditor((Composite) getViewer().getControl());
	}

}