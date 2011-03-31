package org.eclipse.linuxtools.internal.cdt.libhover.devhelp.preferences;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

//  Label class for a preference page.
class LabelFieldEditor extends FieldEditor {
	private Composite parent;

	public LabelFieldEditor( Composite parent, String title ) {
		this.parent = parent;
		init("org.eclipse.linuxtools.cdt.libhover.dummy", title);
		createControl(parent);
	}

	protected void adjustForNumColumns( int numColumns ) {
        GridData gd = new GridData();
        gd.horizontalSpan = numColumns;
        // We only grab excess space if we have to
        // If another field editor has more columns then
        // we assume it is setting the width.
        gd.grabExcessHorizontalSpace = gd.horizontalSpan == 1;
        getLabelControl(parent).setLayoutData(gd);
	}

	protected void doFillIntoGrid( Composite parent, int numColumns ) {
		getLabelControl(parent);
	}

	public int getNumberOfControls() {	return 1; }
	/**
	 * The label field editor is only used to present a text label on a preference page.
	 */
	protected void doLoad() {}
	protected void doLoadDefault() {}
	protected void doStore() {}
}

