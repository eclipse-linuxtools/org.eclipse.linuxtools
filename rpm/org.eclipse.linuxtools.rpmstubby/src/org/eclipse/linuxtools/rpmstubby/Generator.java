/*******************************************************************************
 * Copyright (c) 2011, 2013 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexander Kurtakov - initial API and implementation
 *     Neil Guzman        - python, ruby, perl implementation (B#350065,B#350066)
 *******************************************************************************/

package org.eclipse.linuxtools.rpmstubby;

import org.eclipse.core.resources.IFile;
import org.eclipse.linuxtools.internal.rpmstubby.StubbyEggGenerator;
import org.eclipse.linuxtools.internal.rpmstubby.StubbyGemGenerator;
import org.eclipse.linuxtools.internal.rpmstubby.StubbyGenerator;
import org.eclipse.linuxtools.internal.rpmstubby.StubbyMakefilePLGenerator;
import org.eclipse.linuxtools.internal.rpmstubby.StubbyPomGenerator;

/**
 * Utility API for stubifying spec files for different input types.
 *
 */
public class Generator {

    private InputType type;

    /**
     * Creates the generator for the given input type.
     *
     * @param type
     *            The input type for this generator.
     */
    public Generator(InputType type) {
        this.type = type;
    }

    /**
     * Generate the spec file for the given input file.
     *
     * @param file
     *            The input file.
     */
    public void generate(IFile file) {
        switch (type) {
        case ECLIPSE_FEATURE:
            new StubbyGenerator(file).writeContent();
            break;
        case MAVEN_POM:
            new StubbyPomGenerator(file).writeContent();
            break;
        case PYTHON_EGG:
            new StubbyEggGenerator(file).writeContent();
            break;
        case RUBY_GEM:
            new StubbyGemGenerator(file).writeContent();
            break;
        case PERL_MAKEFILE:
            new StubbyMakefilePLGenerator(file).writeContent();
            break;
        default:
            break;
        }
    }

}
