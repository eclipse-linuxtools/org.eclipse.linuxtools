package org.eclipse.linuxtools.rpm.ui.editor.forms;

import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class RpmTagText extends Text {

	private String tag;
	private Specfile specfile;
	
	public RpmTagText(Composite parent, int style, String rpmTag, Specfile specfile) {
		super(parent, style);
		this.tag = rpmTag;
		this.specfile = specfile;
		setText(specfile.getDefine(rpmTag).getStringValue());
	}

}
