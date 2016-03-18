package org.eclipse.linuxtools.rdt.proxy.tests;

import org.eclipse.linuxtools.rdt.proxy.tests.CommandLauncherProxyTest;
import org.eclipse.linuxtools.rdt.proxy.tests.FileProxyTest;
import org.eclipse.linuxtools.rdt.proxy.tests.RemoteProxyManagerTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({CommandLauncherProxyTest.class, FileProxyTest.class,
	RemoteProxyManagerTest.class
})
public class AllTests {

}
