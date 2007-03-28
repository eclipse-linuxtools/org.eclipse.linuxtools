package org.eclipse.linuxtools.rpm.ui.editor.parser;

public class SpecfilePatchMacro extends SpecfileMacro {
	private int patchNumber;
	private int patchLevel;
	// TODO:  add patchLevel functionality
	public SpecfilePatchMacro(int patchNumber) {
		super();
		this.patchNumber = patchNumber;
		this.patchLevel = patchLevel;
	}
	public int getPatchLevel() {
		return patchLevel;
	}
	public void setPatchLevel(int patchLevel) {
		this.patchLevel = patchLevel;
	}
	public int getPatchNumber() {
		return patchNumber;
	}
	public void setPatchNumber(int patchNumber) {
		this.patchNumber = patchNumber;
	}
	public String toString() {
		return "patch #" + patchNumber + " at level " + patchLevel;
	}
}
