/*******************************************************************************
 * Copyright (c) 2011, 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexander Kurtakov - initial API and implementation
 *     Neil Guzman        - python, ruby, perl implementation (B#350065,B#350066)
 *******************************************************************************/

package org.eclipse.linuxtools.rpmstubby;

/**
 * Supported input types and file name patterns for the corresponding files.
 *
 */
public enum InputType {

    /** Eclipse feature.xml file. */
    ECLIPSE_FEATURE("feature.xml"),
    /** Maven pom.xml file. */
    MAVEN_POM("pom.xml"),
    /** Python Egg setup.py file */
    PYTHON_EGG("setup.py"),
    /** Ruby *.gemspec file */
    RUBY_GEM("*.gemspec"),
    /** Perl Makefile.PL file */
    PERL_MAKEFILE("Makefile.PL");

    private String fileNamePattern;

    InputType(String fileName) {
        this.fileNamePattern = fileName;
    }

    /**
     * Returns the file name pattern for the input type.
     *
     * @return The pattern for the file name e.g. feature.xml or pom.xml.
     */
    public String getFileNamePattern() {
        return fileNamePattern;
    }

}
