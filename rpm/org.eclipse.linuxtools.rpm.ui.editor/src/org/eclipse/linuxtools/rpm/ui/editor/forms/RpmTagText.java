/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.ui.editor.forms;

import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileDefine;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfilePackage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class RpmTagText {

	public RpmTagText(Composite parent, final String rpmTag,
			final Specfile specfile) {
		this(parent, rpmTag, specfile, SWT.SINGLE);
	}

	public RpmTagText(Composite parent, final String rpmTag,
			final Specfile specfile, int flag) {
		Label label = new Label(parent, SWT.SINGLE);
		label.setText(rpmTag);
		final Text text = new Text(parent, SWT.BORDER_SOLID|flag);
		SpecfileDefine define = specfile.getDefine(rpmTag);
		if (null != define) {
			text.setText(define.getStringValue());
		}
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				specfile.modifyDefine(rpmTag, text.getText());
			}
		});
	}
	
	public RpmTagText(Composite parent, final String rpmTag,
			final Specfile specfile, final SpecfilePackage rpmPackage, int flag) {
		Label label = new Label(parent, SWT.SINGLE);
		label.setText(rpmTag);
		final Text text = new Text(parent, SWT.BORDER_SOLID|flag);
		SpecfileDefine define = specfile.getDefine(rpmTag, rpmPackage);
		if (null != define) {
			text.setText(define.getStringValue());
		}
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				specfile.modifyDefine(rpmTag, rpmPackage, text.getText());
			}
		});
	}

}
