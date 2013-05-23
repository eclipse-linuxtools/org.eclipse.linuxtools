/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Camilo Bernal <cabernal@redhat.com> - Initial Implementation.
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf.model;

/**
 * Representation of a single entry in a perf stat report.
 */
public class PMStatEntry {
	// Samples of current event.
	private float samples;

	// Current event.
	private String event;

	// Metrics for current event.
	private float metrics;

	// Metrics' units.
	private String units;

	// Standard deviation of stats.
	private float deviation;

	// Scaling.
	private float scaling;

	// Types of relevant strings for a perf stat entry.
	public static enum Type {
		ENTRY_PATTERN, TIME_PATTERN, ENTRY_FORMAT, PERCENT_FORMAT, METRIC_FORMAT
	}

	// Reg-ex strings for elements in a perf stat entry.
	public static final String DECIMAL = "\\d+[\\.\\,\\d]*"; //$NON-NLS-1$
	public static final String PERCENTAGE = "(\\d+(\\.\\d+)?)\\%"; //$NON-NLS-1$
	public static final String SAMPLES = "(" + DECIMAL + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	public static final String EVENT =  "(\\w+(\\-\\w+)*(:\\w+)?)";//$NON-NLS-1$
	public static final String METRICS = "(" + DECIMAL + ")"; //$NON-NLS-1$//$NON-NLS-2$
	public static final String UNITS = "([a-zA-Z\\/\\s\\%]*)"; //$NON-NLS-1$
	public static final String DELTA = "(\\(\\s\\+\\-\\s*" + PERCENTAGE + "\\s\\))"; //$NON-NLS-1$ //$NON-NLS-2$
	public static final String SCALE = "(\\[\\s*" + PERCENTAGE + "\\])"; //$NON-NLS-1$ //$NON-NLS-2$
	public static final String TIME_UNIT = "(seconds\\stime\\selapsed)";  //$NON-NLS-1$
	public static final String TIME = "seconds time elapsed";  //$NON-NLS-1$

	public PMStatEntry(float samples, String event, float metrics,
			String units, float deviation, float scaling) {
		this.samples = samples;
		this.event = event;
		this.metrics = metrics;
		this.units = units;
		this.deviation = deviation;
		this.scaling = scaling;
	}

	public float getSamples() {
		return samples;
	}

	public String getEvent() {
		return (event == null) ? "" : event; //$NON-NLS-1$
	}


	public float getMetrics() {
		return metrics;
	}

	public String getUnits() {
		return (units == null) ? "" : units.trim(); //$NON-NLS-1$
	}

	public float getDeviation() {
		return deviation;
	}


	public float getScaling() {
		return scaling;
	}

	public String getFormattedMetrics(){
		return String.format("%.3f", metrics); //$NON-NLS-1$
	}

	public String getFormattedDeviation(){
		return String.format("%.2f%%", deviation); //$NON-NLS-1$
	}

	@Override
	public boolean equals(Object entry) {
		PMStatEntry statEntry = (PMStatEntry) entry;
		if (statEntry == null
				|| samples != statEntry.getSamples()
				|| metrics != statEntry.getMetrics()
				|| deviation != statEntry.getDeviation()
				|| scaling != statEntry.getScaling()) {
			return false;
		}

		if (!getEvent().equals(statEntry.getEvent())
				|| !getUnits().equals(statEntry.getUnits())) {
			return false;
		}

		return true;
	}

	/**
	 * Check if PMStatEntry refer to the same event.
	 * @param entry PMStatEntry to check against
	 * @return true if events are equals, false otherwise.
	 */
	public boolean equalEvents(PMStatEntry entry) {
		String event = entry.getEvent();
		if (this.event != null && event != null) {
			return this.event.equals(event);
		}
		return false;
	}

	/**
	 * Auto-generated hashCode method.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(deviation);
		result = prime * result + ((event == null) ? 0 : event.hashCode());
		result = prime * result + Float.floatToIntBits(metrics);
		result = prime * result + Float.floatToIntBits(samples);
		result = prime * result + Float.floatToIntBits(scaling);
		result = prime * result + ((units == null) ? 0 : units.hashCode());
		return result;
	}

	/**
	 * Compare this PMStatEntry with the specified one, and return an object
	 * with the result.
	 *
	 * @param entry PMStatEntry to compare against
	 * @return a PMStatEntry representing the resulting comparison.
	 */
	public PMStatEntry compare(PMStatEntry entry){
		float occurrenceDiff = entry.getSamples() - this.samples;
		float metricsDiff = entry.getMetrics() - this.metrics;
		float deviationDiff = entry.getDeviation() + this.deviation;
		float scalingDiff = entry.getScaling() + this.scaling;

		return new PMStatEntry(occurrenceDiff, event, metricsDiff, units, deviationDiff, scalingDiff);
	}

	/**
	 * Return formatted String values of fields in a string array.
	 *
	 * @return String[] String array containing string value of fields.
	 */
	public String[] toStringArray(){
		return new String[] { String.valueOf(samples),
				getEvent(),
				getFormattedMetrics(),
				getUnits(),
				getFormattedDeviation()};
	}

	/**
	 * Get string related to specified type of perf stat entry:
	 *	<p>
	 *		ENTRY_PATTERN: - Regex for a generic entry.</br>
	 *		TIME_PATTERN: - Regex for total time entry.</br>
	 *		ENTRY_FORMAT: - Format string for a generic entry.
	 *	</p>
	 *
	 * @param type
	 * @return
	 */
	public static String getString(Type type) {
		String stringRes = ""; //$NON-NLS-1$
		switch (type) {
		case ENTRY_PATTERN:
			// samples, event, metrics, units, deviation, scaling
			stringRes = "^" + SAMPLES; //$NON-NLS-1$
			stringRes += "\\s" + EVENT; //$NON-NLS-1$
			stringRes += "\\s*(\\#\\s+" + METRICS + UNITS + ")?"; //$NON-NLS-1$ //$NON-NLS-2$
			stringRes += DELTA + "?"; //$NON-NLS-1$
			stringRes += "(\\s" + SCALE + ")?$"; //$NON-NLS-1$ //$NON-NLS-2$

			return stringRes;
		case TIME_PATTERN:
			// samples, time elapsed, deviation
			stringRes += "^" + SAMPLES; //$NON-NLS-1$
			stringRes += "\\s" + TIME_UNIT; //$NON-NLS-1$
			stringRes += "\\s+" + DELTA; //$NON-NLS-1$

			return stringRes;
		case ENTRY_FORMAT:
			// Stat entry format
			stringRes += "   %%1$%1$1ds "; //$NON-NLS-1$
			stringRes += "%%2$-%2$1ds   #  "; //$NON-NLS-1$
			stringRes += "%%3$%3$1ds "; //$NON-NLS-1$
			stringRes += "%%4$-%4$1ds  "; //$NON-NLS-1$
			stringRes += "( +- %%5$%5$1ds )\n"; //$NON-NLS-1$

			return stringRes;
		default:
			return stringRes;
		}
	}

}
