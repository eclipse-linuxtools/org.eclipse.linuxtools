/*******************************************************************************
 * Copyright (c) 2010 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui.editor.forms;

import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileSection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

public class RpmSectionPage extends FormPage {
    private String rpmSection;
    private SpecfileSection section;

    public RpmSectionPage(SpecfileFormEditor editor, Specfile specfile,
            String rpmSection) {
        super(editor, rpmSection, rpmSection);
        this.rpmSection = rpmSection;
        this.section = specfile.getSection(rpmSection.substring(1));
    }

    @Override
    protected void createFormContent(IManagedForm managedForm) {
        super.createFormContent(managedForm);
        FormToolkit toolkit = managedForm.getToolkit();
        ScrolledForm form = managedForm.getForm();
        form.setText(rpmSection);
        GridLayout layout = new GridLayout();

        form.getBody().setLayout(layout);
        layout.numColumns = 2;
        toolkit.createLabel(form.getBody(), rpmSection);
        final Text text = toolkit.createText(form.getBody(), section.getContents(),
                SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
        GridData gd = new GridData();
        gd.grabExcessHorizontalSpace = true;
        gd.grabExcessVerticalSpace = true;
        gd.horizontalAlignment = SWT.FILL;
        gd.verticalAlignment = SWT.FILL;
        text.setLayoutData(gd);
    }
}
