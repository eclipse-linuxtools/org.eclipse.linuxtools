/*******************************************************************************
 * (C) Copyright 2010 IBM Corp. 2010
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Thavidu Ranatunga (IBM) - Initial implementation.
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf.model;

import java.util.ArrayList;
import java.util.List;

public class TreeParent {
	private String name;
	private TreeParent parent;
	private List<TreeParent> children;
	private float percent = -1;
	private double samples = -1;

	public TreeParent(String name, float percent) {
		this.name = name;
		this.percent = percent;
		children = new ArrayList<>();
	}

	public TreeParent(String name, float percent, double samples) {
		this(name, percent);
		this.samples = samples;
	}

	public String getName() {
		return name;
	}

	public void setParent(TreeParent parent) {
		this.parent = parent;
	}

	public TreeParent getParent() {
		return parent;
	}

	@Override
	public String toString() {
		return getName() + " (" + getFormattedSamples() + " samples)";  //$NON-NLS-1$//$NON-NLS-2$
	}

	public Boolean equals(String s) {
		return getName().equals(s);
	}

	public float getPercent() {
		return percent;
	}

	public void setPercent(float percent) {
		this.percent = percent;
	}

	/**
	 * Get the number of samples collected for this element.
	 *
	 * If this element is a child of PMSymbol (eg. PMLineRef) we should
	 * calculate its samples using its given percentage and the number of
	 * samples from its parent. If this element is a parent of PMSymbol we
	 * should calculate its samples by accumulating all samples from its
	 * children.
	 *
	 * @return the number of samples
	 */
	private double getSamples () {
		// Child of PMSymbol, distribute samples by percentage
		if (this instanceof PMLineRef) {
			if (samples == -1) {
				samples = (int) (getParent().getSamples() * (getPercent() / 100));
			}
		} else {
			// Parent of PMSymbol, accumulate from children elements
			if (samples == -1) {
				int sampleSum = 0;

				for (TreeParent child : getChildren()) {
					sampleSum += child.getSamples();
				}
				samples = sampleSum;
			}
		}

		return samples;
	}

	public String getFormattedSamples () {
		double samples = getSamples();
		return (samples <= Integer.MAX_VALUE)
				? String.format("%.0f", samples) //$NON-NLS-1$
				: String.format("%.4G", samples); //$NON-NLS-1$
	}

	public TreeParent(String name) {
		this.name = name;
		children = new ArrayList<>();
	}

	public void addChild(TreeParent child) {
		children.add(child);
		child.setParent(this);
		recalculatePercentage();
	}

	public TreeParent getChild(String name) {
		//check if it exists
		for(TreeParent t : children) {
			if (t.equals(name))
				return t;
		}
		return null;
	}

	public void removeChild(TreeParent child) {
		children.remove(child);
		child.setParent(null);
		recalculatePercentage();
	}

	public TreeParent [] getChildren() {
		return children.toArray(new TreeParent[children.size()]);
	}

	public boolean hasChildren() {
		return children.size() > 0;
	}

	public void clear() {
		children.clear();
	}

	private void recalculatePercentage() {
		if (getPercent() != -1 && (this instanceof PMDso || this instanceof PMFile)){
			percent = 0;
			// Re-sum its children percentages
			for (TreeParent c : getChildren()) {
				percent += c.getPercent();
			}
			// Tell its parent to re-sum too.
			if (getParent().getPercent() != -1) {
				getParent().recalculatePercentage();
			}
		}
	}

}
