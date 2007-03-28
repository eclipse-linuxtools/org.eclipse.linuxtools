package org.eclipse.cdt.rpm.editor.parser;


public class SpecfileTag extends SpecfileElement {
	
	static final int INT = 0;
	static final int STRING = 1;
	int tagType;
	
	String stringValue;
	int intValue;
	
	public SpecfileTag(String name, String value, Specfile specfile) {
		setName(name);
		this.stringValue = value;
		this.tagType = STRING;
		super.setSpecfile(specfile);
	}
	
	public String getStringValue() {
		if (tagType == INT) {
			return Integer.toString(intValue);
		}
		return resolve(stringValue);
	}
	public void setStringValue(String value) {
		this.stringValue = value;
	}
	
	public SpecfileTag(String name, int value, Specfile specfile) {
		setName(name);
		this.intValue = value;
		this.tagType = INT;
		super.setSpecfile(specfile);
	}
	
	public int getIntValue() {
		return intValue;
	}
	
	public void setIntValue(int value) {
		this.intValue = value;
	}
	
	public String toString() {
		if (tagType == INT) {
			return getName() + ": " + getIntValue();
		}
		String tagValue = getStringValue();
		if ((tagValue != null) && (tagValue.length() > 0) && (tagValue.indexOf("%") > 0)) {
			return getName() + ": " + super.resolve(tagValue);
		}
		return getName() + ": " + getStringValue();
	}

	public int getTagType() {
		return tagType;
	}

	public void setTagType(int tagType) {
		this.tagType = tagType;
	}
	
}
