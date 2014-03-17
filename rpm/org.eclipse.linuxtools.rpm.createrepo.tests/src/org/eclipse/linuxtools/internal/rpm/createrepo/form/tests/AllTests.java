/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Neil Guzman - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.createrepo.form.tests;

import org.eclipse.linuxtools.internal.rpm.createrepo.listener.tests.CreaterepoResourceListenerTest;
import org.eclipse.linuxtools.internal.rpm.createrepo.preference.tests.CreaterepoDeltaPropertyPageTest;
import org.eclipse.linuxtools.internal.rpm.createrepo.preference.tests.CreaterepoGeneralPropertyPageTest;
import org.eclipse.linuxtools.internal.rpm.createrepo.preference.tests.CreaterepoPreferenceInitializationTest;
import org.eclipse.linuxtools.internal.rpm.createrepo.preference.tests.CreaterepoPreferencePageTest;
import org.eclipse.linuxtools.internal.rpm.createrepo.tests.CreaterepoCommandCreatorTest;
import org.eclipse.linuxtools.internal.rpm.createrepo.tests.CreaterepoTest;
import org.eclipse.linuxtools.internal.rpm.createrepo.tree.tests.CreaterepoTreeCategoryTest;
import org.eclipse.linuxtools.internal.rpm.createrepo.tree.tests.CreaterepoTreeTest;
import org.eclipse.linuxtools.internal.rpm.createrepo.wizard.tests.CreaterepoWizardTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({CreaterepoProjectCreatorTest.class, CreaterepoUtilsTest.class,
	CreaterepoProjectTest.class, CreaterepoTreeCategoryTest.class, CreaterepoTreeTest.class,
	CreaterepoWizardTest.class, CreaterepoTest.class, CreaterepoPreferenceInitializationTest.class,
	CreaterepoRepoFormEditorTest.class, CreaterepoImportRPMsPageTest.class, CreaterepoMetadataPageTest.class,
	CreaterepoResourceListenerTest.class, CreaterepoCommandCreatorTest.class,
	CreaterepoPreferencePageTest.class, CreaterepoGeneralPropertyPageTest.class, CreaterepoDeltaPropertyPageTest.class})
public class AllTests {
	//Nothing here as annotation is important
}
