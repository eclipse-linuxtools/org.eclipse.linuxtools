/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Neil Guzman - ruby implementation (B#350066)
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpmstubby;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.linuxtools.internal.rpmstubby.model.GemModel;

/**
 * Generator for RPM specfile from ruby gemspec.
 *
 */
public class StubbyGemGenerator extends AbstractGenerator {

    private GemModel model;

    private static final String FIX_ME = "#FIXME";

    /**
     * Creates the generator by parsing the gemspec file.
     *
     * @param gemFile
     *            The gemspec file to generate specfile for.
     */
    public StubbyGemGenerator(IFile gemFile) {
        parse(gemFile);
        specfileName = model.getPackageName().toLowerCase() + ".spec";
        projectName = gemFile.getProject().getName();
    }

    /**
     * Creates the model which contains the information
     *
     * @param gemFile The gemspec file
     */
    private void parse(IFile gemFile) {
        model = new GemModel(gemFile);
    }

    /**
     * Generates a RPM specfile based on the parsed data from the gemspec file.
     *
     * @return The generated specfile.
     */
    @Override
    public String generateSpecfile() {
        StringBuilder buffer = new StringBuilder();
        generateDefines(buffer);
        String packageName = model.getPackageName();
        buffer.append("Name:           " + packageName.toLowerCase() + "\n");
        buffer.append("Version:        " + model.getVersion() + "\n");
        buffer.append("Release:        1%{?dist}" + "\n");
        buffer.append("Summary:        " + model.getSummary() + "\n\n");

        buffer.append("Group:          Development/Libraries\n");
        buffer.append("License:        " + model.getLicense() + "\n");
        buffer.append("URL:            " + model.getURL() + "\n");
        buffer.append("Source0:        #FIXME\n\n");

        generateRequires(buffer);

        buffer.append("BuildArch:      noarch\n");
        buffer.append("Provides:       rubygem(%{gem_name}) = %{version}\n\n\n");

        buffer.append("%description\n" + model.getDescription() + "\n\n\n");
        generatePrepSection(buffer);
        generateBuildSection(buffer);
        generateInstallSection(buffer);
        generateFilesSections(buffer);
        generateChangelog(buffer);

        return buffer.toString();
    }

    private void generateDefines(StringBuilder buffer) {
        buffer.append("%global gem_name " + model.getSimplePackageName().toLowerCase() +"\n\n");
    }

    /**
     * Generate requires
     *
     * @param buffer Buffer to write content to
     */
    private void generateRequires(StringBuilder buffer) {
        buffer.append("BuildRequires:  rubygems-devel\n");
        List<String> requireList = model.getBuildRequiresList();
        for (String require : requireList) {
            buffer.append("BuildRequires:  " + require + "\n");
        }
        buffer.append("Requires:       ruby(release)\n");

        String rubyGemsVersion = model.getGemVersion();
        if (rubyGemsVersion.equals(FIX_ME)) {
            buffer.append("Requires:       rubygems\n");
        } else {
            buffer.append("Requires:       rubygems == " + rubyGemsVersion +"\n");
        }

        requireList = model.getInstallRequiresList();
        for (String require : requireList) {
            buffer.append("Requires:       " + require + "\n");
        }
        buffer.append("\n");
    }

    /**
     * Generate prep
     *
     * @param buffer Buffer to write content to
     */
    private static void generatePrepSection(StringBuilder buffer) {
        buffer.append("%prep\n");
        buffer.append("gem unpack %{SOURCE0}\n");
        buffer.append("%setup -q -D -T -n %{gem_name}-%{version} #You may need to update this according to your Source0\n\n");

        buffer.append("gem spec %{SOURCE0} -l --ruby > %{gem_name}.gemspec\n\n\n");
    }

    /**
     * Generate build
     *
     * @param buffer Buffer to write content to
     */
    private static void generateBuildSection(StringBuilder buffer) {
        buffer.append("%build\n");
        buffer.append("gem build %{gem_name}.gemspec\n\n");

        buffer.append("%gem_install\n\n\n");
    }

    /**
     * Generate install
     *
     * @param buffer Buffer to write content to
     */
    private void generateInstallSection(StringBuilder buffer) {
        buffer.append("%install\n");
        buffer.append("mkdir -p %{buildroot}%{gem_dir}\n");
        buffer.append("cp -a ./%{gem_dir}/* %{buildroot}%{gem_dir}/\n\n");

        buffer.append("mkdir -p %{buildroot}%{_bindir}\n");
        buffer.append("cp -a ./%{_bindir}/* %{buildroot}%{_bindir}\n\n");

        List<String> requirePaths = model.getRequirePaths();
        if (!requirePaths.isEmpty()) {
            buffer.append("mkdir -p %{buildroot}%{gem_extdir_mri}/"+ requirePaths.get(0) +"\n");
            buffer.append("mv %{buildroot}%{gem_instdir}/"+requirePaths.get(0)+"/shared_object.so %{buildroot}%{gem_extdir_mri}/"+ requirePaths.get(0)+"/\n\n");
        }

        buffer.append("\n");
    }

    /**
     * Generate files
     *
     * @param buffer Buffer to write content to
     */
    private static void generateFilesSections(StringBuilder buffer) {
        buffer.append("%files\n");
        buffer.append("%dir %{gem_instdir}\n");
        buffer.append("%{gem_libdir}\n");
        buffer.append("%{gem_spec}\n\n");

        buffer.append("%files doc\n");
        buffer.append("%doc %{gem_docdir}\n\n\n");
    }

}
