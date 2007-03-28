package org.eclipse.linuxtools.rpm.ui.editor.tests;

import java.io.ByteArrayInputStream;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.Document;
import org.eclipse.linuxtools.rpm.ui.editor.SpecfileErrorHandler;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileParser;

public class NameTagTests extends TestCase {

	private SpecfileParser parser;
	private Specfile specfile;
	private IFile testFile;
	private Document testDocument;
	private SpecfileErrorHandler errorHandler;
	private SpecfileTestProject testProject;

	public NameTagTests(String name) {
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
	
	public void testResolvedNameTag() {
		
		String testText = "%define blah notblah\nName: %{blah}";
		
		try {
			newFile(testText);	
			
			assertEquals("notblah", specfile.getName());
		} catch (Exception e) {
			fail();
		}
	}
	
	public void testResolvedSetName() {
		String testText = "%define blah notblah\nName: %{blah}";
		try {
			newFile(testText);
			assertEquals("notblah", specfile.getName());
		} catch (Exception e) {
			fail();
		}
	}
	
	public void testNameTag() {
		String testText = "Name: blah";
		
		try {
			newFile(testText);
			assertEquals("blah", specfile.getName());
		} catch (Exception e) {
			fail();
		}
		
	}
	
	public void testNameTag2() {
		String testText = "Name:	blah";
	
		try {
			newFile(testText);
			assertEquals("blah", specfile.getName());
		} catch (Exception e) {
			fail();
		}
	}
	
	public void testNullNameTag() {
		String testText = "Name:	";
		try {
			newFile(testText);
			IMarker marker = testProject.getFailureMarkers()[0];
			assertEquals(0, marker.getAttribute(IMarker.CHAR_START, 0));
			assertEquals(testText.length(), marker.getAttribute(IMarker.CHAR_END, 0));
			assertEquals("Name declaration without value.", marker.getAttribute(IMarker.MESSAGE, ""));
		} catch (Exception e) {
			fail();
		}
	}
	
	public void testNullNameTag2() {
		String testText = "Name: ";
		
		try {
			newFile(testText);
			IMarker marker = testProject.getFailureMarkers()[0];
			assertEquals(0, marker.getAttribute(IMarker.CHAR_START, 0));
			assertEquals(testText.length(), marker.getAttribute(IMarker.CHAR_END, 0));
			assertEquals("Name declaration without value.", marker.getAttribute(IMarker.MESSAGE, ""));
		} catch (Exception e) {
			fail();
		}
	}
	
	public void testMultipleNamesTag() {
		String testText = "Name: blah bleh";
		
		try {
			newFile(testText);
			IMarker marker = testProject.getFailureMarkers()[0];
			assertEquals(0, marker.getAttribute(IMarker.CHAR_START, 0));
			assertEquals(testText.length(), marker.getAttribute(IMarker.CHAR_END, 0));
			assertEquals("Name cannot have multiple values.", marker.getAttribute(IMarker.MESSAGE, ""));
		} catch (Exception e) {
			fail();
		}
	}
	
	public void testMultipleNamesTag2() {
		String testText = "Name: 	blah bleh";

		try {
			newFile(testText);
			IMarker marker = testProject.getFailureMarkers()[0];
			assertEquals(0, marker.getAttribute(IMarker.CHAR_START, 0));
			assertEquals(testText.length(), marker.getAttribute(IMarker.CHAR_END, 0));
			assertEquals("Name cannot have multiple values.", marker.getAttribute(IMarker.MESSAGE, ""));
		} catch (Exception e) {
			fail();
		}
	}

}
