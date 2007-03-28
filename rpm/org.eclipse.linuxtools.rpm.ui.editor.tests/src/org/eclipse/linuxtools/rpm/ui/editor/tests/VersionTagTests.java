package org.eclipse.linuxtools.rpm.ui.editor.tests;

import java.io.ByteArrayInputStream;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.Document;
import org.eclipse.linuxtools.rpm.ui.editor.SpecfileErrorHandler;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileParser;

public class VersionTagTests extends TestCase {

	private SpecfileParser parser;
	private Specfile specfile;
	private IFile testFile;
	private Document testDocument;
	private SpecfileErrorHandler errorHandler;
	private SpecfileTestProject testProject;

	public VersionTagTests(String name) {
		super(name);
	}

	protected void newFile(String contents) throws Exception {
		testFile.setContents(new ByteArrayInputStream(contents.getBytes()), false, false, null);
		testDocument = new Document(contents);
		errorHandler = new SpecfileErrorHandler(testFile, testDocument);
		parser.setErrorHandler(errorHandler);
		specfile = parser.parse(testDocument);
	}
	
	protected void setUp() throws Exception {
		testProject = new SpecfileTestProject();
		testFile = testProject.createFile("test.spec");
		parser = new SpecfileParser();
	}

	protected void tearDown() throws Exception {
		testProject.dispose();
	}
	
	public void testResolvedSetVersion() {
		String testText = "%define blah notblah\nVersion: %{blah}";

		try {
			newFile(testText);
			assertEquals("notblah", specfile.getVersion());
		} catch (Exception e) {
			fail();
		}
	}
	
	public void testVersionTag() {
		String testText = "Version: blah";
		
		try {
			newFile(testText);
			assertEquals("blah", specfile.getVersion());
		} catch (Exception e) {
			fail();
		}
		
	}
	
	
	public void testVersionTag2() {
		String testText = "Version:		blah";
		
		try {
			newFile(testText);
			assertEquals("blah", specfile.getVersion());
		} catch (Exception e) {
			fail();
		}
	}
	
	public void testNullVersionTag() {
		String testText = "Version: ";
		
		try {
			newFile(testText);

			IMarker marker = testProject.getFailureMarkers()[0];
			assertEquals(0, marker.getAttribute(IMarker.CHAR_START, 0));
			assertEquals(testText.length(), marker.getAttribute(IMarker.CHAR_END, 0));
			assertEquals("Version declaration without value.", marker.getAttribute(IMarker.MESSAGE, ""));
		} catch (Exception e) {
			fail();
		}
	}
	
	public void testNullVersionTag2() {
		String testText = "Version:		";

		try {
			newFile(testText);

			IMarker marker = testProject.getFailureMarkers()[0];
			assertEquals(0, marker.getAttribute(IMarker.CHAR_START, 0));
			assertEquals(testText.length(), marker.getAttribute(IMarker.CHAR_END, 0));
			assertEquals("Version declaration without value.", marker.getAttribute(IMarker.MESSAGE, ""));
		} catch (Exception e) {
			fail();
		}
	}
	
	public void testMultipleVersionsTag() {
		String testText = "Version: blah bleh";

		try {
			newFile(testText);

			IMarker marker = testProject.getFailureMarkers()[0];
			assertEquals(0, marker.getAttribute(IMarker.CHAR_START, 0));
			assertEquals(testText.length(), marker.getAttribute(IMarker.CHAR_END, 0));
			assertEquals("Version cannot have multiple values.", marker.getAttribute(IMarker.MESSAGE, ""));
		} catch (Exception e) {
			fail();
		}
	}
	
	public void testMultipleVersionsTag2() {
		String testText = "Version: 	blah bleh";

		try {
			newFile(testText);

			IMarker marker = testProject.getFailureMarkers()[0];
			assertEquals(0, marker.getAttribute(IMarker.CHAR_START, 0));
			assertEquals(testText.length(), marker.getAttribute(IMarker.CHAR_END, 0));
			assertEquals("Version cannot have multiple values.", marker.getAttribute(IMarker.MESSAGE, ""));
		} catch (Exception e) {
			fail();
		}
	}

}
