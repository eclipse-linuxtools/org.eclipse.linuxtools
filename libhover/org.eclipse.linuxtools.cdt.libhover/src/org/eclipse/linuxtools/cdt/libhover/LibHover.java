/*******************************************************************************
 * Copyright (c) 2004, 2006, 2007, 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
/*
 * Initially created on Jul 8, 2004
 */

/**
 * @author Chris Moller, Red Hat, Inc.
 * @author Jeff Johnston, Red Hat, Inc.  (rewrite to use ICHelpProvider)
 * Modified to be org.eclipse.linuxtools.cdt.libhover package.
 */

package org.eclipse.linuxtools.cdt.libhover;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.Map.Entry;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.ICHelpBook;
import org.eclipse.cdt.ui.ICHelpProvider;
import org.eclipse.cdt.ui.ICHelpResourceDescriptor;
import org.eclipse.cdt.ui.IFunctionSummary;
import org.eclipse.cdt.ui.IRequiredInclude;
import org.eclipse.cdt.ui.text.ICHelpInvocationContext;
import org.eclipse.cdt.ui.text.SharedASTJob;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.core.runtime.Status;
import org.eclipse.help.IHelpResource;
import org.eclipse.jface.text.IRegion;


public class LibHover implements ICHelpProvider {
	
	public static String LIBHOVER_DOC_EXTENSION = LibhoverPlugin.PLUGIN_ID + ".library"; //$NON-NLS-1$

    // see comment in initialize()
    // private static String defaultSearchPath = null;
    
	private static HashMap<ICHelpBook, LibHoverLibrary> libraries = new HashMap<ICHelpBook, LibHoverLibrary>();
	
    static final String  constructTypes[] ={
    	"dtype", // $NON-NLS-1$
    	"enum",  // $NON-NLS-1$
    	"function", // $NON-NLS-1$
    	"groupsynopsis", // $NON-NLS-1$
    	"struct", // $NON-NLS-1$
    	"type",  // $NON-NLS-1$
    	"union"  // $NON-NLS-1$
    };
    
    static final int dtypeIndex         = 0;
    static final int enumIndex          = 1;
    static final int functionIndex      = 2;
    static final int groupsynopsisIndex = 3;
    static final int structIndex        = 4;
    static final int typeIndex          = 5;
    static final int unionIndex         = 6;

    private static ArrayList<ICHelpBook> helpBooks = new ArrayList<ICHelpBook>();
    public static boolean docsFetched = false;

	public static synchronized void getLibHoverDocs() {
		if (docsFetched)
			return;
//		System.out.println("getlibhoverdocs");
		libraries.clear();
		helpBooks.clear();
		IExtensionRegistry x = RegistryFactory.getRegistry();
		IConfigurationElement[] ces = x.getConfigurationElementsFor(LIBHOVER_DOC_EXTENSION);
		for (int i = 0; i < ces.length; ++i) {
			IConfigurationElement ce = ces[i];
			if (ce.getName().equals("library")) { //$NON-NLS-1$
				// see comment in initialize()
				// Use the FileLocator class to open the magic hover doc file
				// in the plugin's jar.
				// Either open the html file or file system file depending
				// on what has been specified.
				String location = ce.getAttribute("location"); //$NON-NLS-1$
				String name = ce.getAttribute("name"); //$NON-NLS-1$
				String helpdocs = ce.getAttribute("docs"); //$NON-NLS-1$
				String type = ce.getAttribute("type"); //$NON-NLS-1$
				HelpBook h = new HelpBook(name, type);
				helpBooks.add(h);
				LibHoverLibrary l = new LibHoverLibrary(name, location, helpdocs, 
						true);
				libraries.put(h, l);
				docsFetched = true;
			}
		}
	}
	
	public void initialize() {
		getLibHoverDocs();
	}
		
	public ICHelpBook[] getCHelpBooks () {
		ICHelpBook[] chelpbooks = new ICHelpBook[helpBooks.size()];
		return helpBooks.toArray(chelpbooks);
	}
	
	private class FunctionSummary implements IFunctionSummary, Comparable<FunctionSummary> {

        private String Name;
        private String NameSpace;
        private String ReturnType;
        private String Prototype;
        private String Summary;
        private boolean prototypeHasBrackets;
        
//        private String Synopsis;
        private class RequiredInclude implements IRequiredInclude {
        	private String include;
        	
        	public RequiredInclude (String file) {
        		include = file;
        	}
        	
        	public String getIncludeName() {
        		return include;
        	}
        	
        	public boolean isStandard() {
        		return true;
        	}
        }
        
		public int compareTo (FunctionSummary x) {
			FunctionSummary y = (FunctionSummary)x;
			return getName().compareTo(y.getName());
		}

//        private RequiredInclude Includes[];
        private ArrayList<RequiredInclude> Includes = new ArrayList<RequiredInclude>();

        private void setIncludeName (String iname) {
        	RequiredInclude nri = new RequiredInclude(iname);
        	Includes.add(nri);
        }

        public class FunctionPrototypeSummary implements IFunctionPrototypeSummary {
            public String getName()             { return Name; }
            public String getReturnType()       { return ReturnType; }
            public String getArguments()        { return Prototype; }
            public String getPrototypeString(boolean namefirst) {
                if (true == namefirst) {
                	if (prototypeHasBrackets())
                		return Name + " " + Prototype + " " + ReturnType; // $NON-NLS-1$ // $NON-NLS-2$
                    return Name + " (" + Prototype + ") " + ReturnType; // $NON-NLS-1$ // $NON-NLS-2$
                }
                else {
                	if (prototypeHasBrackets())
                		return ReturnType + " " + Name + " " + Prototype; // $NON-NLS-1$ // $NON-NLS-2$
                    return ReturnType + " " + Name + " (" + Prototype + ")"; // $NON-NLS-1$ // $NON-NLS-2$ // $NON-NLS-3$
                }
            }
        }

        public String getName()                         { return Name; }
        public String getNamespace()                    { return NameSpace; }
        public String getDescription()                  { return Summary; }
        public boolean prototypeHasBrackets()			{ return prototypeHasBrackets; }
        public void setPrototypeHasBrackets(boolean value)	{ prototypeHasBrackets = value; }
        public IFunctionPrototypeSummary getPrototype() { return new FunctionPrototypeSummary(); }
        
        public IRequiredInclude[] getIncludes() {
        	IRequiredInclude[] includes = new IRequiredInclude[Includes.size()];
        	for (int i = 0; i < Includes.size(); ++i) {
        		includes[i] = (IRequiredInclude)Includes.get(i);
        	}
        	return includes;
        }
        
    }
	
	public boolean isCPPCharacter(int ch) {
		return Character.isLetterOrDigit(ch) || ch == '_' || ch == ':'; 
	}

	private class EnclosingASTNameJob extends SharedASTJob {
		private int tlength;
		private int toffset;
		private IASTName result = null;
		public EnclosingASTNameJob (ITranslationUnit t, 
				int toffset, int tlength) {
			super("EnclosingASTNameJob", t); // $NON-NLS-1$
			this.toffset = toffset;
			this.tlength = tlength;
		}
		public IStatus runOnAST(ILanguage lang, IASTTranslationUnit ast) {
			if (ast != null) {
				result = ast.getNodeSelector(null).findEnclosingName(toffset, tlength);
			}
			return Status.OK_STATUS;
		}
		public IASTName getASTName() {
			return result;
		}
	}
	
	public class ASTDeclarationFinderJob extends SharedASTJob {
		private IBinding binding;
		private IASTName[] decls = null;
		public ASTDeclarationFinderJob (ITranslationUnit t, IBinding binding) {
			super("ASTDeclarationFinderJob", t); // $NON-NLS-1$
			this.binding = binding;
		}
    	public IStatus runOnAST(ILanguage lang, IASTTranslationUnit ast) {
    		if (ast != null) {
    			decls = ast.getDeclarationsInAST(binding);
    		}
    		return Status.OK_STATUS;
    	}
    	public IASTName[] getDeclarations() {
    		return decls;
    	}
	}

	@SuppressWarnings("unchecked")
	public IFunctionSummary getFunctionInfo(ICHelpInvocationContext context, ICHelpBook[] helpBooks, String name) {
        IFunctionSummary f;

        f = null;
        boolean isPTR = false;
        boolean isREF = false;
        boolean isCPP = false;
        int offset = -1;
        int length = 0;
        
        ITranslationUnit t = context.getTranslationUnit();
        
        String className = null;
        
        // FIXME: Uncomment following code out for CDT 6.0 to support C++ hover.
        
//        if (t.isCXXLanguage()) {
//        	isCPP = true;
//        	try {
//        		// We use reflection to cast the context to ICHelpInvocationContextExtended as this is
//        		// not guaranteed to be in CDT except in Fedora.  We need ICHelpInvocationExtended to get
//        		// the document region.  Otherwise, we have no way of doing C++ hover help/completion because
//        		// we need to figure out the class name which is not given to us.
//        		Class<? extends ICHelpInvocationContext> contextClass = context.getClass();
//        		Class[] interfaces = contextClass.getInterfaces();
//        		for (int i = 0; i < interfaces.length; ++i) {
//        			if (interfaces[i].getName().contains("ICHelpInvocationContextExtended")) // $NON-NLS-1$ 
//        				contextClass = interfaces[i];
//        		}
//        		Method getRegion = contextClass.getMethod("getHoverRegion", (Class<?>[])null);  // $NON-NLS-1$
//        		IRegion region = (IRegion)getRegion.invoke(context, (Object[])null);
//        		char[] contents = t.getCodeReader().buffer;
//				int i = region.getOffset();
//				if (i > 2 && contents[i-1] == '>' && contents[i-2] == '-') {
//					// Pointer reference
//					int j = i - 3;
//					int pointer = 0;
//					while (j > 0 && isCPPCharacter(contents[j])) {
//						pointer = j;
//						--j;
//					}
//					if (pointer != 0) {
//						offset = pointer;
//						length = region.getOffset() - pointer - 2;
//						isPTR = true;
////						String pointerName = new String(contents, pointer, region.getOffset() - pointer - 2);
////						System.out.println("pointer reference to " + pointerName);
//					}
//				} else if (i > 1 && contents[i-1] == '.') {
//					int j = i - 2;
//					int ref = 0;
//					while (j > 0 && isCPPCharacter(contents[j])) {
//						ref = j;
//						--j;
//					}
//					if (ref != 0) {
//						offset = ref;
//						length = region.getOffset() - ref - 1;
//						isREF = true;
////						String refName = new String(contents, ref, region.getOffset() - ref - 1);
////						System.out.println("regular reference to " + refName);
//					}
//				}
//				final IASTName[] result= {null};
//				final int toffset = offset;
//				final int tlength = length;
//
//				if (isPTR || isREF) {
//					EnclosingASTNameJob job = new EnclosingASTNameJob(t, toffset, tlength);
//					job.schedule();
//					try {
//						job.join();
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					if (job.getResult() == Status.OK_STATUS)
//						result[0] = job.getASTName();
//				}
//
//				final IASTName[][] decl = {null};
//				if (result[0] != null) {
//					final IBinding binding = result[0].getBinding();
//					ASTDeclarationFinderJob job = new ASTDeclarationFinderJob(t, binding);
//					job.schedule();
//					try {
//						job.join();
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					if (job.getResult() == Status.OK_STATUS) {
//						decl[0] = job.getDeclarations();
//					}
//				}
//				
//				IASTNode n = null;
//				if (decl[0] != null && decl[0].length > 0) {
//					n = decl[0][0];
//					while (n != null && !(n instanceof IASTSimpleDeclaration)) {
//						n = n.getParent();
//					}
//				}
//
//				if (n != null) {
//					IASTSimpleDeclaration d = (IASTSimpleDeclaration)n;
//					IASTDeclSpecifier s = d.getDeclSpecifier();
//					if (s instanceof IASTNamedTypeSpecifier) {
//						IASTName astName = ((IASTNamedTypeSpecifier)s).getName();
//						if (astName != null)
//							className = astName.toString();
//					}
//				}
//			    
////				System.out.println("classname is " + className);
//        	} catch (NoSuchMethodException e) {
//        		// do nothing...we don't have enough info to do C++ members
//        	} catch (IllegalArgumentException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IllegalAccessException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (InvocationTargetException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//        }
        	
        // Loop through all the documents we have and report first match.
        for (int i = 0; i < helpBooks.length; ++i) {
        	LibHoverLibrary l = libraries.get(helpBooks[i]);
        	// FIXME: remove following 2 lines when CDT 6.0 to enable C++ hover.
        	if (l.isCPP())
        		continue;
        	if (name != null) {
        		if (className != null) {
        			if (l.isCPP())
        				f = getMemberSummary(l, className, name);
        		} else {
        			f = getFunctionSummary(l, name);
        		}
        		if (f != null)
        			return f;
        	}
        }
        
        return null;
	}
	
	private IFunctionSummary getFunctionSummary(LibHoverLibrary l, String name) {
		FunctionInfo x = l.getFunctionInfo(name);
		if (x != null) {
			FunctionSummary f = new FunctionSummary();
			f.ReturnType = x.getReturnType();
			f.Prototype = x.getPrototype();
			f.Summary = x.getDescription();
			f.Name = x.getName();
			ArrayList<String> headers = x.getHeaders();
			for (int i = 0; i < headers.size(); ++i)
				f.setIncludeName(headers.get(i));
			return f;
		}
		return null;
	}
	
	private IFunctionSummary getMemberSummary(LibHoverLibrary l, String className, String memberName) {

		ClassInfo info = l.getClassInfo(className);
		if (info == null)
			return null;
		MemberInfo m = info.getMember(memberName);
		if (m != null) {
			// FIXME: do some work to determine parameters and return type.
			FunctionSummary f = new FunctionSummary();
			f.ReturnType = m.getReturnType();
			f.Prototype = m.getPrototype();
			f.Summary = m.getDescription();
			f.setPrototypeHasBrackets(true);
			f.setIncludeName(info.getInclude());
			String actualClassName = className.substring(className.indexOf("::")+2); // $NON-NLS-1$
			actualClassName = actualClassName.replaceAll("<", "&lt;"); // $NON-NLS-1$ // $NON-NLS-2$
			actualClassName = actualClassName.replaceAll(">", "&gt;"); // $NON-NLS-1$ // $NON-NLS-2$
			f.Name = actualClassName + "::" + memberName; // $NON-NLS-1$
			return f;
		}
		return null;
	}
     
 	
	public IFunctionSummary[] getMatchingFunctions(ICHelpInvocationContext context, ICHelpBook[] helpBooks, String prefix) {
		ArrayList<IFunctionSummary> fList = new ArrayList<IFunctionSummary>();

		for (int di = 0; di < helpBooks.length; ++di) {
			LibHoverLibrary l = libraries.get(helpBooks[di]);
			LibHoverInfo cppInfo = l.getHoverInfo();
			SortedMap<String, FunctionInfo> map = cppInfo.functions.tailMap(prefix);
			Set<Map.Entry<String, FunctionInfo>> c = map.entrySet();
			for (Iterator<Entry<String, FunctionInfo>> i = c.iterator(); i.hasNext();) {
				Map.Entry<String, FunctionInfo> e = (Map.Entry<String, FunctionInfo>)i.next();
				FunctionInfo x = e.getValue();
				if (x.getName().startsWith(prefix)) {
					FunctionSummary f = new FunctionSummary();
					f.ReturnType = x.getReturnType();
					f.Prototype = x.getPrototype();
					f.Summary = x.getDescription();
					f.Name = x.getName();
					ArrayList<String> headers = x.getHeaders();
					for (int i1 = 0; i1 < headers.size(); ++i1)
						f.setIncludeName(headers.get(i1));
					fList.add(f);
				}
			}
			
//			Document document = l != null ? l.getDocument() : null;
//			if ((null != document) && (null != prefix)) {
//				NodeList elems = document.getElementsByTagName("construct"); // $NON-NLS-1$
//				for (int i = 0; i < elems.getLength(); ++i) {
//					Element elem = (Element)elems.item(i);
//					NamedNodeMap attrs = elem.getAttributes();
//					Node id_node = attrs.item(0);
//					String elemName = id_node.getNodeValue();
//					if (elemName != null && elemName.startsWith("function-")) { // $NON-NLS-1$
//						String funcName = elemName.substring(9);
//						if (funcName != null && funcName.startsWith(prefix)) {
//							NodeList functionNodes = elem.getElementsByTagName("function"); // $NON-NLS-1$
//							for (int j = 0; j < functionNodes.getLength(); ++j) {
//								Node function_node = functionNodes.item(j);
//								FunctionSummary f = getFunctionSummaryFromNode(funcName, function_node, document);
//								fList.add(f);
//							}
//						}
//					}
//				}
//			}
		}
		IFunctionSummary[] summaries = new IFunctionSummary[fList.size()];
		for (int k = 0; k < summaries.length; k++) {
			summaries[k] = (IFunctionSummary)fList.get(k);
		}
		return summaries;
	}
	
	private class HelpResource implements IHelpResource {
		private String href;
		private String label;
		public HelpResource(String href, String label) {
			this.href = href;
			this.label = label;
		}
		public String getHref() {
			return href;
		}
		public String getLabel() {
			return label;
		}
	}
	
	private class HelpResourceDescriptor implements ICHelpResourceDescriptor {
		private ICHelpBook helpbook;
		
		public HelpResourceDescriptor(ICHelpBook helpbook) {
			this.helpbook = helpbook;
		}
		
		public ICHelpBook getCHelpBook() {
			return helpbook;
		}
		
		public IHelpResource[] getHelpResources() {
			LibHoverLibrary l = libraries.get(helpbook);
			if (l != null) {
				IHelpResource[] hr = new IHelpResource[1];
				hr[0] = new HelpResource(l.getLocation(), l.getName());
				return hr;
			}
			return null;
		}
	}
	
	public ICHelpResourceDescriptor[] getHelpResources(ICHelpInvocationContext context, ICHelpBook[] helpBooks, String name) {
		for (int i = 0; i < helpBooks.length; ++i) {
			IFunctionSummary fs = getFunctionInfo(context, new ICHelpBook[]{helpBooks[i]}, name);
			if (fs != null) {
				return new HelpResourceDescriptor[]{new HelpResourceDescriptor(helpBooks[i])};
			}
		}
		return null;
	}
}
