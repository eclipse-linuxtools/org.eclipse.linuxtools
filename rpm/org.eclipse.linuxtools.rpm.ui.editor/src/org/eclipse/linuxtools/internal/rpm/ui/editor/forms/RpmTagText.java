/*******************************************************************************
 * Copyright (c) 2009-2010 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui.editor.forms;

import org.eclipse.linuxtools.internal.rpm.ui.editor.parser.SpecfileTag;
import org.eclipse.linuxtools.internal.rpm.ui.editor.parser.SpecfileTag.TagType;
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
		final Text text = new Text(parent, SWT.BORDER_SOLID | flag);
		SpecfileDefine define = specfile.getDefine(rpmTag);
		if (null != define) {
			if (define.getTagType().equals(TagType.INT)) {
				text.setText(String.valueOf(define.getIntValue()));
			} else {
				text.setText(define.getStringValue());
			}
		}
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		text.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				specfile.modifyDefine(rpmTag, text.getText());
			}
		});
	}

	public RpmTagText(Composite parent, final String rpmTag,
			final Specfile specfile, final SpecfilePackage rpmPackage, int flag) {
		Label label = new Label(parent, SWT.SINGLE);
		label.setText(rpmTag);
		final Text text = new Text(parent, SWT.BORDER_SOLID | flag);
		SpecfileDefine define = specfile.getDefine(rpmTag, rpmPackage);
		if (null != define) {
			text.setText(define.getStringValue());
		}
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		text.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				specfile.modifyDefine(rpmTag, rpmPackage, text.getText());
			}
		});
	}

	public RpmTagText(Composite parent, final SpecfileTag require, final Specfile specfile) {
		Label label = new Label(parent, SWT.SINGLE);
		label.setText(Messages.RpmTagText_0);
		final Text text = new Text(parent, SWT.BORDER_SOLID);
		text.setText(require.getStringValue());
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		text.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				 specfile.modifyDefine(require, text.getText());
			}
		});
	}

}
