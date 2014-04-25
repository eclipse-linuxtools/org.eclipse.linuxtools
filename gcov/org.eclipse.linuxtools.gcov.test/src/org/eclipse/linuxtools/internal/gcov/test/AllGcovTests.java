package org.eclipse.linuxtools.internal.gcov.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    GcovTestC.class,
    GcovTestCPP.class,
    GcovTestCLibrary.class
})
public class AllGcovTests {

}
