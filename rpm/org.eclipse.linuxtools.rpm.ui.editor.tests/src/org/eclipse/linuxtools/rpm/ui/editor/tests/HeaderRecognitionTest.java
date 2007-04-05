/**
 * 
 */
package org.eclipse.linuxtools.rpm.ui.editor.tests;

import java.io.ByteArrayInputStream;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.Document;
import org.eclipse.linuxtools.rpm.ui.editor.SpecfileErrorHandler;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileElement;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileParser;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileSection;

/**
 * @author overholt
 * 
 */
public class HeaderRecognitionTest extends TestCase {

	String testText;
	private SpecfileParser parser;
	private Specfile specfile;
	private IFile testFile;
	private Document testDocument;
	private SpecfileErrorHandler errorHandler;
	private SpecfileTestProject testProject;

	/**
	 * @param name
	 */
	public HeaderRecognitionTest(String name) {
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



	public void testGetSimpleSectionName() {
		testText = "%prep";
		SpecfileElement element;
		
		try {
			newFile(testText);
			element = parser.parseLine(testText, specfile, 0);
			assertEquals(SpecfileSection.class, element.getClass());
			assertEquals(testText.substring(1), ((SpecfileSection) element)
					.getName());
		} catch (Exception e) {
			fail();
		}
	}

	public void testGetComplexSectionName1() {
		testText = "%post";
		SpecfileElement element;

		try {
			newFile(testText);
			element = parser.parseLine(testText, specfile, 0);
			assertEquals(SpecfileSection.class, element.getClass());
			SpecfileSection section = (SpecfileSection) element;
			assertEquals(testText.substring(1), section.getName());
			assertNull(section.getPackage());
		} catch (Exception e) {
			fail();
		}
	}

	public void testGetComplexSectionName2() {
		testText = "%post -n";

		try {
			newFile(testText);
			specfile = parser.parse(testDocument);
			
			IMarker marker = testProject.getFailureMarkers()[0];
			assertEquals(0, marker.getAttribute(IMarker.CHAR_START, 0));
			assertEquals("No package name after -n in post section.", marker.getAttribute(IMarker.MESSAGE, ""));
			assertEquals(testText.length(), marker.getAttribute(IMarker.CHAR_END, 0));
		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail();
		}
	}

	public void testGetComplexSectionName3() {
		testText = "%post -n name";
		String[] tokens = testText.split("\\s+");
		SpecfileElement element;

		try {
			newFile(testText);
			element = parser.parseLine(testText, specfile, 0);
			assertEquals(SpecfileSection.class, element.getClass());
			SpecfileSection section = (SpecfileSection) element;
			assertEquals(tokens[0].substring(1), section.getName());
			assertEquals(tokens[2], section.getPackage().getName());
		} catch (Exception e) {
			fail();
		}
	}

	public void testGetComplexSectionName4() {
		// FIXME: check for rest of line when -p is implemented
		// this should be an error case
		testText = "%post -n name -p";
		String[] tokens = testText.split("\\s+");
		SpecfileElement element;

		try {
			newFile(testText);
			element = parser.parseLine(testText, specfile, 0);
			assertEquals(SpecfileSection.class, element.getClass());
			SpecfileSection section = (SpecfileSection) element;
			assertEquals(tokens[0].substring(1), section.getName());
			assertEquals(tokens[2], section.getPackage().getName());
		} catch (Exception e) {
			fail();
		}
	}

	public void testGetComplexSectionName5() {
		// FIXME: check for rest of line when -p is implemented
		// "blah bleh" should become the actual text of the section
		testText = "%post -n name -p blah bleh";
		String[] tokens = testText.split("\\s+");
		SpecfileElement element;

		try {
			newFile(testText);
			element = parser.parseLine(testText, specfile, 0);
			assertEquals(SpecfileSection.class, element.getClass());
			SpecfileSection section = (SpecfileSection) element;
			assertEquals(tokens[0].substring(1), section.getName());
			assertEquals(tokens[2], section.getPackage().getName());
		} catch (Exception e) {
			fail();
		}
	}

	public void testGetComplexSectionName6() {
		testText = "%post -p blah bleh";
		// FIXME: check for rest of line when -p is implemented
		// "blah bleh" should become the actual text of the section
		
		String[] tokens = testText.split("\\s+");
		SpecfileElement element;

		try {
			newFile(testText);
			element = parser.parseLine(testText, specfile, 0);
			assertEquals(SpecfileSection.class, element.getClass());
			SpecfileSection section = (SpecfileSection) element;
			assertEquals(tokens[0].substring(1), section.getName());
			assertNull(section.getPackage());
		} catch (Exception e) {
			fail();
		}
	}

	 public void testGetComplexSectionName7() {
		 testText = "%post -n -p blah";

		try {
			newFile(testText);
			specfile = parser.parse(testDocument);
			
			IMarker marker = testProject.getFailureMarkers()[0];
			assertEquals(0, marker.getAttribute(IMarker.CHAR_START, 0));
			assertEquals("Package name must not start with '-': -p.", marker.getAttribute(IMarker.MESSAGE, ""));
			assertEquals(testText.length(), marker.getAttribute(IMarker.CHAR_END, 0));
			} catch (Exception e) {
				fail();
			}
	 }
	 
//	 public void testGetComplexSectionName8() {
//		 testText = "%files blah -f blah.list";
//
//		 SpecfileElement element;
//		 specfile = new Specfile("testspec");
//		 try {
//			 element = parser.parseLine(testText, specfile);
//			 fail();
//		 } catch (Exception e) {
//			 assertTrue(e.getMessage().startsWith(""));
//		 }
//	 }
//	 
//	 public void testGetComplexSectionName9() {
//		 testText = "%files blah blah -f blah.list";
//
//		 SpecfileElement element;
//		 specfile = new Specfile("testspec");
//		 try {
//			 element = parser.parseLine(testText, specfile);
//			 fail();
//		 } catch (Exception e) {
//			 assertTrue(e.getMessage().startsWith(""));
//		 }
//	 }
//	 
//	 public void testGetComplexSectionName10() {
//		 // FIXME:  can you have multiple files lists?
//		 testText = "%files blah -f blah.list blah2.list";
//
//		 SpecfileElement element;
//		 specfile = new Specfile("testspec");
//		 try {
//			 element = parser.parseLine(testText, specfile);
//			 fail();
//		 } catch (Exception e) {
//			 assertTrue(e.getMessage().startsWith(""));
//		 }
//	 }

}
