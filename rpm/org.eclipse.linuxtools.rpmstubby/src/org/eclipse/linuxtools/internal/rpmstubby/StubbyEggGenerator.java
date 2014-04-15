/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Neil Guzman - python implementation (B#350065)
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpmstubby;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.linuxtools.internal.rpmstubby.model.EggModel;

/**
 * Generator for RPM specfile from python setup.py.
 *
 */
public class StubbyEggGenerator extends AbstractGenerator {

    private EggModel model;

    /**
     * Creates the generator by parsing the setup.py file.
     *
     * @param eggFile
     *            The setup.py file to generate specfile for.
     */
    public StubbyEggGenerator(IFile eggFile) {
        parse(eggFile);
        specfileName = model.getPackageName().toLowerCase() + ".spec";
        projectName = eggFile.getProject().getName();
    }

    /**
     * Creates the model which contains the information
     *
     * @param eggFile The setup.py file
     */
    private void parse(IFile eggFile) {
        model = new EggModel(eggFile);
    }

    /**
     * Generates a RPM specfile based on the parsed data from the setup.py file.
     *
     * @return The generated specfile.
     */
    @Override
    public String generateSpecfile() {
        StringBuilder buffer = new StringBuilder();
        generateSiteLibSiteArch(buffer);
        String packageName = model.getPackageName();
        buffer.append("Name:           " + packageName.toLowerCase() + "\n");
        buffer.append("Version:        " + model.getVersion() + "\n");
        buffer.append("Release:        1%{?dist}" + "\n");
        buffer.append("Summary:        " + model.getSummary() + "\n\n");

        buffer.append("Group:          Development/Libraries\n");
        buffer.append("License:        " + model.getLicense() + "\n");
        buffer.append("URL:            " + model.getURL() + "\n");
        buffer.append("Source0:        #FIXME\n\n");

        buffer.append("BuildArch:      noarch\n");
        generateRequires(buffer);
        buffer.append("\n%description\n" + model.getDescription() + "\n\n");

        generatePython3SubPackage(buffer);
        generatePrepSection(buffer);
        generateBuildSection(buffer);
        generateInstallSection(buffer);
        generateFilesSections(buffer);
        generateChangelog(buffer);

        return buffer.toString();
    }

    /**
     * Generate python_sitelib depending on fedora release and srcname of package
     *
     * @param buffer Buffer to write content to
     */
    private void generateSiteLibSiteArch(StringBuilder buffer) {
        buffer.append("%if 0%{?fedora} > 12\n");
        buffer.append("%global with_python3 1\n");
        buffer.append("%else\n");
        buffer.append("%{!?python_sitelib: %global python_sitelib %(%{__python} -c \"from distutils.sysconfig import get_python_lib; print (get_python_lib())\")}\n");
        buffer.append("%endif\n\n");
        buffer.append("%global srcname " + model.getSimplePackageName().toLowerCase() +"\n\n");
    }

    /**
     * Generate requires
     *
     * @param buffer Buffer to write content to
     */
    private void generateRequires(StringBuilder buffer) {
        buffer.append("BuildRequires:  python2-devel\n");
        buffer.append("%if 0%{?with_python3}\n");
        buffer.append("BuildRequires:  python3-devel\n");
        buffer.append("%endif # if with_python3\n\n");

        List<String> requireList = model.getInstallRequiresList();
        for (String require : requireList) {
            buffer.append("Requires:       " + require + "\n");
        }
    }

    /**
     * Generate the python 3 subpackage
     *
     * @param buffer Buffer to write content to
     */
    private void generatePython3SubPackage(StringBuilder buffer) {
        buffer.append("%if 0%{?with_python3}\n");
        buffer.append("%package -n python3-" + model.getPackageName().toLowerCase() + "\n\n");

        buffer.append("Summary:        " + model.getSummary() + "\n");
        buffer.append("Group:          Development/Libraries\n");

        buffer.append("\n%description -n python3-" + model.getPackageName().toLowerCase() + "\n" + model.getDescription() + "\n");
        buffer.append("%endif # with_python3\n\n");
    }

    /**
     * Generate prep
     *
     * @param buffer Buffer to write content to
     */
    private static void generatePrepSection(StringBuilder buffer) {
        buffer.append("\n%prep\n");
        buffer.append("%setup -q -n %{srcname}-%{version} #You may need to update this according to your Source0\n\n");

        buffer.append("%if 0%{?with_python3}\n");
        buffer.append("rm -rf %{py3dir}\n");
        buffer.append("cp -a . %{py3dir}\n");
        buffer.append("%endif # with_python3\n\n\n");
    }

    /**
     * Generate build
     *
     * @param buffer Buffer to write content to
     */
    private static void generateBuildSection(StringBuilder buffer) {
        buffer.append("%build\n");
        buffer.append("%{__python} setup.py build\n\n");

        buffer.append("%if 0%{?with_python3}\n");
        buffer.append("pushd %{py3dir}\n");
        buffer.append("%{__python3} setup.py build\n");
        buffer.append("popd\n");
        buffer.append("%endif # with_python3\n\n\n");
    }

    /**
     * Generate install
     *
     * @param buffer Buffer to write content to
     */
    private static void generateInstallSection(StringBuilder buffer) {
        buffer.append("%install\n");

        buffer.append("%if 0%{?with_python3}\n");
        buffer.append("pushd %{py3dir}\n");
        buffer.append("%{__python3} setup.py install --skip-build --root %{buildroot}\n");
        buffer.append("popd\n");
        buffer.append("%endif # with_python3\n\n");

        buffer.append("%{__python} setup.py install --skip-build --root %{buildroot}\n\n\n");
    }

    /**
     * Generate files
     *
     * @param buffer Buffer to write content to
     */
    private void generateFilesSections(StringBuilder buffer) {
        buffer.append("%files\n");
        buffer.append("%{python_sitelib}/*\n\n");

        buffer.append("%if 0%{?with_python3}\n");
        buffer.append("%files -n python3-" + model.getPackageName().toLowerCase() +"\n");
        buffer.append("%{python3_sitelib}/*\n");
        buffer.append("%endif # with_python3\n\n\n");
    }


}
