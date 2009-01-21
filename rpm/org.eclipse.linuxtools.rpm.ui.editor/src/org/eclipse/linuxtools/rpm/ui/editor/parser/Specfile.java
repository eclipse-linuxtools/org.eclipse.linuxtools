/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *    Alphonse Van Assche
 *******************************************************************************/

package org.eclipse.linuxtools.rpm.ui.editor.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

public class Specfile {
	String name = ""; //$NON-NLS-1$

	int epoch = -1;

	String version;

	String release;
	
	String license;
	
	SpecfilePreamble preamble;
	
	SpecfilePackageContainer packages;

	List<SpecfileSection> sections;
	List<SpecfileSection> complexSections;

	Map<String, SpecfileDefine> defines;

	Map<Integer, SpecfileSource> sources;

	Map<Integer, SpecfileSource> patches;

	private IDocument document;

	public Specfile() {
		packages = new SpecfilePackageContainer();
		preamble = new SpecfilePreamble();
		sections = new ArrayList<SpecfileSection>();
		complexSections = new ArrayList<SpecfileSection>();
		defines = new HashMap<String, SpecfileDefine>();
		sources = new HashMap<Integer, SpecfileSource>();
		patches = new HashMap<Integer, SpecfileSource>();
	}

	public Specfile(String name) {
		this();
		this.name = name;
	}

	public List<SpecfileSection> getSectionsAsList() {
		return sections;
	}
	
	public SpecfileElement[] getSections() {
		SpecfileElement[] elements = new SpecfileElement[sections.size()]; 
		return sections.toArray(elements);
	}
	
	public List<SpecfileSection> getComplexSectionsAsList() {
		return complexSections;
	}
	
	public SpecfileSource getPatch(int number) {
		return patches.get(number);
	}

	public SpecfileSource getSource(int number) {
		return sources.get(number);
	}

	public String getName() {
		return name;
	}

	/**
	 * Sets the name of this specfile. This method also
	 * adds/updates the SpecfileDefine name.
	 * 
	 * @param name The name of the spec file.
	 */
	public void setName(String name) {
		this.name = name;
		addDefine("name", name); //$NON-NLS-1$
	}

	public void addSection(SpecfileSection section) {
		sections.add(section);
	}

	public void addComplexSection(SpecfileSection section) {
		complexSections.add(section);
	}
	
	public void addSource(SpecfileSource source) {
		sources.put(Integer.valueOf(source.getNumber()), source);
	}

	public void addPatch(SpecfileSource patch) {
		patches.put(Integer.valueOf(patch.getNumber()), patch);
	}

    /**
     * Adds the given define to the map of defines for this specfile.
     * 
     * @param define The define to add.
     */
    public void addDefine(SpecfileDefine define) {
		defines.put(define.getName(), define);
	}
	
	/**
	 * Creates a SpecfileDefine with the given name and value for this specfile and 
	 * adds it to the map of defines.
	 * 
	 * @param name The name of the define.
	 * @param value the value of the define.
	 */
	public void addDefine(String name, String value) {
		defines.put(name, new SpecfileDefine(name, value, this));
	}
	
	/**
	 * Creates a SpecfileDefine with the given name and value for this specfile and 
	 * adds it to the map of defines.
	 * 
	 * @param name The name of the define.
	 * @param value the value of the define.
	 */
	public void addDefine(String name, int value) {
		defines.put(name, new SpecfileDefine(name, value, this));
	}

	public SpecfileDefine getDefine(String defineName) {
		return defines.get(defineName);
	}

	public int getEpoch() {
		return epoch;
	}

	/**
	 * Sets the epoch of this specfile. This method also
	 * adds/updates the SpecfileDefine epoch.
	 * 
	 * @param epoch The epoch.
	 */
	public void setEpoch(int epoch) {
		this.epoch = epoch;
		addDefine("epoch", epoch); //$NON-NLS-1$
	}

	public String getRelease() {
		return release;
	}

	/**
	 * Sets the release of this specfile. This method also
	 * adds/updates the SpecfileDefine release.
	 * 
	 * @param release The release.
	 */
	public void setRelease(String release) {
		this.release = release;
		addDefine("release", release); //$NON-NLS-1$
	}

	public String getVersion() {
		return version;
	}

	/**
	 * Sets the version of this specfile. This method also
	 * adds/updates the SpecfileDefine version.
	 * 
	 * @param version The version.
	 */
	public void setVersion(String version) {
		this.version = version;
		addDefine("version", version); //$NON-NLS-1$
	}

	public Map<Integer, SpecfileSource> getPatches() {
		return patches;
	}

	public Map<Integer, SpecfileSource> getSources() {
		return sources;
	}

	public List<SpecfileSource> getPatchesAsList() {
		List<SpecfileSource> patchesList = new ArrayList<SpecfileSource>(patches.values());
		Collections.sort(patchesList, new SourceComparator());
		return patchesList;
	}

	public Collection<SpecfileSource> getSourcesAsList() {
		List<SpecfileSource> sourcesList = new ArrayList<SpecfileSource>(sources.values());
		Collections.sort(sourcesList, new SourceComparator());
		return sourcesList;
	}

	public List<SpecfileDefine> getDefinesAsList() {
		List<SpecfileDefine> definesList = new ArrayList<SpecfileDefine>(defines.values());
		return definesList;
	}
	
	public void organizePatches() {
		List<SpecfileSource> patches = getPatchesAsList();
		int newPatchNumber = 0;
		int oldPatchNumber = -1;
		Map<Integer, SpecfileSource> newPatches = new HashMap<Integer, SpecfileSource>();
		for (SpecfileSource thisPatch: patches) {
			if (thisPatch.getSpecfile() == null)
				thisPatch.setSpecfile(this);
			oldPatchNumber = thisPatch.getNumber();
			thisPatch.setNumber(newPatchNumber);
			thisPatch.changeDeclaration(oldPatchNumber);
			thisPatch.changeReferences(oldPatchNumber);
			newPatches.put(Integer.valueOf(newPatchNumber), thisPatch);
			newPatchNumber++;
		}
		setPatches(newPatches);
	}

	public void setDocument(IDocument specfileDocument) {
		document = specfileDocument;
	}

	public String getLine(int lineNumber) throws BadLocationException {
		int offset = document.getLineOffset(lineNumber);
		int length = getLineLength(lineNumber);
		String lineContents = document.get(offset, length);
		return lineContents;
	}

	public IDocument getDocument() {
		return document;
	}

	public int getLineLength(int lineNumber) throws BadLocationException {
		int length = document.getLineLength(lineNumber);
		String lineDelimiter = document.getLineDelimiter(lineNumber);
		if (lineDelimiter != null)
			length = length - lineDelimiter.length();
		return length;
	}

	public void changeLine(int lineNumber, String string)
			throws BadLocationException {
		document.replace(document.getLineOffset(lineNumber),
				getLineLength(lineNumber), string);
	}

	public void setPatches(Map<Integer, SpecfileSource> patches) {
		this.patches = patches;
	}

	public void setSources(Map<Integer, SpecfileSource> sources) {
		this.sources = sources;
	}
	
	@Override
	public String toString() {
		return getName();
	}

	public String getLicense() {
		return license;
	}

	public void setLicense(String license) {
		this.license = license;
	}

	public SpecfilePackageContainer getPackages() {
		return packages;
	}

	public SpecfileElement getPreamble() {
		return preamble;
	}

	public SpecfilePackage getPackage(String packageName) {
		return getPackages().getPackage(packageName);
	}

	public void addPackage(SpecfilePackage subPackage) {
		if (! packages.contains(subPackage))
			packages.addPackage(subPackage);
	}
}
