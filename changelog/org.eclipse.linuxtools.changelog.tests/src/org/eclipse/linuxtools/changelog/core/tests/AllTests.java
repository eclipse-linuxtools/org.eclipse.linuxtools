package org.eclipse.linuxtools.changelog.core.tests;
import org.eclipse.linuxtools.changelog.core.formatters.tests.GNUFormatTest;
import org.junit.runners.Suite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	ChangeLogWriterTest.class,
	GNUFormatTest.class
	}
)

public class AllTests {
	// empty
}
