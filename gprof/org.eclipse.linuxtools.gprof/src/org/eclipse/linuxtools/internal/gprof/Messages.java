/*******************************************************************************
 * Copyright (c) 2013 Kalray.eu.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Xavier Raynaud <xavier.raynaud@kalray.eu> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gprof;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.gprof.messages"; //$NON-NLS-1$
    public static String GmonDecoder_BAD_TAG_ERROR;
    public static String HistogramDecoder_INCOMPATIBLE_HIST_HEADER_ERROR_MSG;
    public static String HistRoot_Summary;
    public static String OpenGmonDialog_BINARY_FILE;
    public static String OpenGmonDialog_DOES_NOT_EXIST;
    public static String OpenGmonDialog_FILE_SYSTEM;
    public static String OpenGmonDialog_GMON_BINARY_FILE;
    public static String OpenGmonDialog_OPEN_BINARY_FILE;
    public static String OpenGmonDialog_PLEASE_ENTER_BINARY_FILE;
    public static String OpenGmonDialog_PLEASE_ENTER_BINARY_FILE_FULL_MSG;
    public static String OpenGmonDialog_WORKSPACE;
    public static String SamplePerCallField_TIME_CALL;
    public static String SamplePerCallField_TIME_CALL_TOOLTIP;
    public static String SampleProfField_SAMPLE_HDR;
    public static String SampleProfField_TIME_HDR;
    public static String SampleProfField_TIME_SPENT_AT_LOCATION;
    public static String SampleProfField_TIME_SPENT_IN_FILE;
    public static String SampleProfField_TIME_SPENT_IN_FUNCTION;
    public static String SampleProfField_TOTAL_TIME_SPENT;
    public static String SwitchSampleTimeAction_GMON_PROF_RATE_IS_NULL;
    public static String SwitchSampleTimeAction_GMON_PROF_RATE_IS_NULL_LONG_MSG;
    public static String SwitchSampleTimeAction_SWITCH_SAMPLE_TIME;
    public static String Aggregator_ERROR_COMMON_PREFIX;
    public static String Aggregator_GPROF_ERROR;
    public static String Aggregator_NOT_FOUND;
    public static String CallsProfField_CALLS;
    public static String CallsProfField_FUNCTION_CALL_NUMBER_BY_FUNCTION;
    public static String CallsProfField_INVOCATION_NUMBER;
    public static String CallsProfField_TOTAL_CALL_NUMBER_BY_FUNCTION;
    public static String CallsProfField_TOTAL_NUMBER_OF_FUNCTION_CALLS;
    public static String GmonView_filter_by_name;
    public static String GmonView_type_filter_text;
    public static String NameProfField_NAME_AND_LOCATION;
    public static String RatioProfField_TIME_PERCENTAGE;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
