/*******************************************************************************
 * Copyright (c) 2004, 2018 IBM Corporation and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Red Hat Inc. - modified to test Linux Tools libhover
 *******************************************************************************/
package org.eclipse.linuxtools.cdt.libhover.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;

import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.CHelpProviderManager;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.text.CHelpBookDescriptor;
import org.eclipse.cdt.internal.ui.text.contentassist.CContentAssistProcessor;
import org.eclipse.cdt.ui.text.ICHelpInvocationContext;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@SuppressWarnings("restriction")
public class ContentAssistTests extends BaseUITestCase {
    private final NullProgressMonitor monitor= new NullProgressMonitor();
    static IProject project;
    static ICProject cproject;

    @BeforeEach
    public void setUpD() throws InterruptedException {

        if (project == null) {
            try {
                cproject = CProjectHelper.createCCProject("ContentAssistTestProject", "bin", IPDOMManager.ID_FAST_INDEXER); //$NON-NLS-1$ //$NON-NLS-2$
                project = cproject.getProject();
                waitForIndexer(cproject);
            } catch ( CoreException e ) {
                /*boo*/
            }
            if (project == null)
                fail("Unable to create project"); //$NON-NLS-1$
        }
    }

    private static void disableContributions (){
        //disable the help books so we don't get proposals we weren't expecting
        CHelpBookDescriptor helpBooks[];
        helpBooks = CHelpProviderManager.getDefault().getCHelpBookDescriptors(new ICHelpInvocationContext(){
            @Override
            public IProject getProject(){return project;}
            @Override
            public ITranslationUnit getTranslationUnit(){return null;}
            }
        );
        for (CHelpBookDescriptor helpBook : helpBooks) {
            if( helpBook != null && !helpBook.getCHelpBook().getTitle().contains("glibc") ) //$NON-NLS-1$
                helpBook.enable( false );
        }
    }

    public void cleanupProject() {
        closeAllEditors();
        try{
            project.delete( true, false, monitor );
            project = null;
        } catch( Throwable e ){
            /*boo*/
        }
    }

    @AfterEach
    public void tearDownD() throws Exception {
        if (project == null || !project.exists()) {
            return;
        }

        closeAllEditors();

        // wait for indexer before deleting project to avoid errors in the log
        waitForIndexer(cproject);

        IResource [] members = project.members();
        for (IResource member : members) {
            if( member.getName().equals( ".project" ) || member.getName().equals( ".cproject" ) ) //$NON-NLS-1$ //$NON-NLS-2$
                continue;
            if (member.getName().equals(".settings"))
                continue;
            try{
                member.delete( false, monitor );
            } catch( Throwable e ){
                /*boo*/
            }
        }
        cleanupProject();

    }

    protected IFile importFile(String fileName, String contents ) throws Exception{
        //Obtain file handle
        IFile file = project.getProject().getFile(fileName);

        InputStream stream = new ByteArrayInputStream( contents.getBytes() );
        //Create file input stream
        if( file.exists() )
            file.setContents( stream, false, false, monitor );
        else
            file.create( stream, false, monitor );

        return file;
    }

    protected ICompletionProposal[] getResults( IFile file, int offset ) throws Exception {
        disableContributions();
        // call the ContentAssistProcessor
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        FileEditorInput editorInput = new FileEditorInput(file);
        IEditorPart editorPart = page.openEditor(editorInput, "org.eclipse.cdt.ui.editor.CEditor");
        CEditor editor = (CEditor) editorPart ;
        @SuppressWarnings("unused")
        IAction completionAction = editor.getAction("ContentAssistProposal");

        String contentType = editor.getViewer().getDocument().getContentType(offset);
        ContentAssistant assistant = new ContentAssistant();
        CContentAssistProcessor processor = new CContentAssistProcessor(editor, assistant, contentType);
        return processor.computeCompletionProposals(editor.getViewer(), offset);
    }

    @Test
    public void testBug69334a() throws Exception {
        importFile( "test.h", "class Test{ public : Test( int ); }; \n" );  //$NON-NLS-1$//$NON-NLS-2$
        StringWriter writer = new StringWriter();
        writer.write( "#include \"test.h\"                \n"); //$NON-NLS-1$
        writer.write( "Test::Test( int i ) { return; }    \n"); //$NON-NLS-1$
        writer.write( "int main() {                       \n"); //$NON-NLS-1$
        writer.write( "   int veryLongName = 1;           \n"); //$NON-NLS-1$
        writer.write( "   Test * ptest = new Test( very   \n"); //$NON-NLS-1$

        String code = writer.toString();
        IFile cu = importFile( "test.cpp", code ); //$NON-NLS-1$

        ICompletionProposal [] results = getResults( cu, code.indexOf( "very " ) + 4 ); //$NON-NLS-1$

        assertEquals( 1, results.length );
        assertEquals( "veryLongName : int", results[0].getDisplayString() ); //$NON-NLS-1$
    }

    @Test
    public void testBug69334b() throws Exception {
        importFile( "test.h", "class Test{ public : Test( int ); }; \n" );  //$NON-NLS-1$//$NON-NLS-2$
        StringWriter writer = new StringWriter();
        writer.write( "#include \"test.h\"                \n"); //$NON-NLS-1$
        writer.write( "Test::Test( int i ) { return; }    \n"); //$NON-NLS-1$
        writer.write( "int main() {                       \n"); //$NON-NLS-1$
        writer.write( "   int veryLongName = 1;           \n"); //$NON-NLS-1$
        writer.write( "   Test test( very   \n"); //$NON-NLS-1$

        String code = writer.toString();
        IFile cu = importFile( "test.cpp", code ); //$NON-NLS-1$

        ICompletionProposal [] results = getResults( cu, code.indexOf( "very " ) + 4 ); //$NON-NLS-1$

        assertEquals( 1, results.length );
        // We should not match anything in glibc and only see something from the source code
        assertEquals( "veryLongName : int", results[0].getDisplayString() ); //$NON-NLS-1$
    }

    @Test @Disabled
    public void testBug428037() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write( "class Strategy {                             \n"); //$NON-NLS-1$
        writer.write( "public :                                     \n"); //$NON-NLS-1$
        writer.write( "   enum _Ability { IDIOT, NORMAL, CHEAT } ;  \n"); //$NON-NLS-1$
        writer.write( "   Strategy( _Ability a ) { }                \n"); //$NON-NLS-1$
        writer.write( "   _Ability getAbility();                    \n"); //$NON-NLS-1$
        writer.write( "};                                           \n"); //$NON-NLS-1$
        writer.write( "int main(){                                  \n"); //$NON-NLS-1$

        String code = writer.toString();
        String c2 = code + "   Strategy *p[3] = { new Strategy( Str \n"; //$NON-NLS-1$

        IFile cu = importFile( "strategy.cpp", c2 ); //$NON-NLS-1$

        ICompletionProposal [] results = getResults( cu, c2.indexOf( "Str " ) + 3 ); //$NON-NLS-1$
        assertEquals( 1, results.length );
        assertEquals( "Strategy", results[0].getDisplayString() ); //$NON-NLS-1$

        c2 = code + "   Strategy *p[3] = { new Strategy( Strategy:: \n"; //$NON-NLS-1$

        cu = importFile( "strategy.cpp", c2 ); //$NON-NLS-1$

        results = getResults( cu, c2.indexOf( "::" ) + 2 ); //$NON-NLS-1$

        // Verify we only get back completions from the source code and not glibc function completions
        assertEquals( 4, results.length );
        assertEquals( "CHEAT", results[0].getDisplayString()  ); //$NON-NLS-1$
        assertEquals( "IDIOT", results[1].getDisplayString()  ); //$NON-NLS-1$
        assertEquals( "NORMAL", results[2].getDisplayString()  ); //$NON-NLS-1$
        // "_Ability" is here due to fix for bug 199598
        // Difficult to differentiate between declaration and expression context
        assertEquals( "_Ability", results[3].getDisplayString()  ); //$NON-NLS-1$

        // in a method definition context, constructors and methods should be proposed

        c2 = code + "return 0;}\nStrategy::\n"; //$NON-NLS-1$

        cu = importFile( "strategy.cpp", c2 ); //$NON-NLS-1$

        results = getResults( cu, c2.indexOf( "::" ) + 2 ); //$NON-NLS-1$
        assertEquals( 3, results.length );
        assertEquals( "getAbility(void) : enum _Ability", results[1].getDisplayString()  ); //$NON-NLS-1$
        assertEquals( "Strategy(enum _Ability a)", results[0].getDisplayString()  ); //$NON-NLS-1$
        assertEquals( "_Ability", results[2].getDisplayString()  ); //$NON-NLS-1$
}
    @Test
    public void testBug72559() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("void foo(){               \n"); //$NON-NLS-1$
        writer.write("   int var;               \n"); //$NON-NLS-1$
        writer.write("   {                      \n"); //$NON-NLS-1$
        writer.write("      float var;          \n"); //$NON-NLS-1$
        writer.write("      v                   \n"); //$NON-NLS-1$
        writer.write("   }                      \n"); //$NON-NLS-1$
        writer.write("}                         \n"); //$NON-NLS-1$

        String code = writer.toString();
        IFile cu = importFile( "t.cpp", code ); //$NON-NLS-1$
        ICompletionProposal [] results = getResults( cu, code.indexOf( "v " ) + 1 ); //$NON-NLS-1$

        assertTrue( results.length >= 5 );

        // Verify first 2 suggestions come from glibc
        assertEquals( results[0].getDisplayString(), "valloc (size_t size) void *" ); //$NON-NLS-1$
        assertEquals( results[1].getDisplayString(), "vasprintf (char **ptr, const char *template, va_list ap) int" ); //$NON-NLS-1$

        // Verify end of list contains completions from indexer
        assertEquals( results[results.length - 3].getDisplayString(), "var : float" ); //$NON-NLS-1$
        assertEquals( results[results.length - 2].getDisplayString(), "virtual" ); //$NON-NLS-1$
        assertEquals( results[results.length - 1].getDisplayString(), "volatile" ); //$NON-NLS-1$
    }

    @Test
    public void testCfunc() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("void foo(){               \n"); //$NON-NLS-1$
        writer.write("   int var;               \n"); //$NON-NLS-1$
        writer.write("   var = strle            \n"); //$NON-NLS-1$
        writer.write("}                         \n"); //$NON-NLS-1$

        String code = writer.toString();
        IFile cu = importFile( "t.c", code ); //$NON-NLS-1$
        ICompletionProposal [] results = getResults( cu, code.indexOf( "strle " ) + 5 ); //$NON-NLS-1$

        assertTrue( results.length >= 1 );

        // Verify we find the glibc strlen function
        assertEquals( results[0].getDisplayString(), "strlen (const char *s) size_t" ); //$NON-NLS-1$

        if (results.length > 1) // if newlib book also enabled, verify it is correct too
            assertEquals( results[1].getDisplayString(), "strlen (const char *str) size_t " ); //$NON-NLS-1$
    }
}
