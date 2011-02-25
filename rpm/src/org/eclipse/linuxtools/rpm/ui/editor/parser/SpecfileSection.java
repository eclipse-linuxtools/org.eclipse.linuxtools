package org.eclipse.linuxtools.rpm.ui.editor.parser;

public class SpecfileSection extends SpecfileElement {

	private SpecfilePackage parentPackage;

	
	public SpecfileSection(String name, Specfile specfile) {
		super(name);
		parentPackage = null;
		super.setSpecfile(specfile);
	}

	public SpecfilePackage getPackage() {
		return parentPackage;
	}

	public void setPackage(SpecfilePackage thePackage) {
		this.parentPackage = thePackage;
	}

	public String toString() {
		if (parentPackage == null) {
			return getName();
		} else {
			return getName() + " " + parentPackage;
		}
	}
        
        public String getPackageName(){
            return parentPackage.getPackageName();
        }
	
}
