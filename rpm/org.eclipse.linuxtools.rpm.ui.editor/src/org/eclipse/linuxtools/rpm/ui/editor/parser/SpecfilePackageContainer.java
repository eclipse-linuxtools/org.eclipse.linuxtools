/*******************************************************************************
 * Copyright (c) 2007, 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.rpm.ui.editor.parser;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.linuxtools.internal.rpm.ui.editor.SpecfileLog;

public class SpecfilePackageContainer extends SpecfileElement {
	List<SpecfilePackage> packages;

	public SpecfilePackageContainer() {
		packages = new ArrayList<>();
	}

	public SpecfilePackage[] getPackages() {
		try {
			Object[] objs = packages.toArray();
			SpecfilePackage[] packs = new SpecfilePackage[objs.length];
			for (int i = 0; i < objs.length; i++) {
				SpecfilePackage pack = (SpecfilePackage) objs[i];
				packs[i] = pack;
			}
			return packs;
		} catch (Exception e) {
			SpecfileLog.logError(e);
		}
		return new SpecfilePackage[0];
	}

	void addPackage(SpecfilePackage subPackage) {
		packages.add(subPackage);
	}

	@Override
	public int getLineStartPosition() {
		if (packages == null || packages.isEmpty()) {
			return 0;
		}

		int lowestStartLine = Integer.MAX_VALUE;

		for (SpecfilePackage subPackage : packages) {
			if (subPackage.getLineStartPosition() < lowestStartLine) {
				lowestStartLine = subPackage.getLineStartPosition();
			}
		}

		if (lowestStartLine == Integer.MAX_VALUE) {
			return 0;
		}
		return lowestStartLine;
	}

	@Override
	public int getLineEndPosition() {
		if ((packages == null) || packages.isEmpty()) {
			return 0;
		}

		int highestEndLine = 0;

		for (SpecfilePackage subPackage : packages) {
			if (subPackage.getLineStartPosition() > highestEndLine)
				highestEndLine = subPackage.getLineEndPosition();
		}

		if (highestEndLine < 0) {
			return 0;
		}
		return highestEndLine;
	}

	public SpecfilePackage getPackage(String packageName) {
		for (SpecfilePackage thisPackage : packages) {
			if (thisPackage.getPackageName().equals(thisPackage.resolve(packageName))) {
				return thisPackage;
			}
		}
		return null;
	}

	public boolean contains(SpecfilePackage subPackage) {
		return packages.contains(subPackage);
	}

	public boolean hasChildren() {
		if (packages != null && !packages.isEmpty()) {
			return true;
		}
		return false;
	}
}
