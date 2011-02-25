package org.eclipse.linuxtools.rpm.ui.editor.parser;

public class SpecfileDefine extends SpecfileTag {

	public SpecfileDefine(String name, int value, Specfile specfile) {
		super(name, value, specfile);
	}

	public SpecfileDefine(String name, String value, Specfile specfile) {
		super(name, value, specfile);
	}
	
}
