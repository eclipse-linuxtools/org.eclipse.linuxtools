/*******************************************************************************
 * Copyright (c) 2004, 2006, 2007, 2008, 2011, 2012 Red Hat, Inc.
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

package org.eclipse.linuxtools.internal.cdt.libhover;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.ICHelpBook;
import org.eclipse.cdt.ui.ICHelpProvider;
import org.eclipse.cdt.ui.ICHelpResourceDescriptor;
import org.eclipse.cdt.ui.IFunctionSummary;
import org.eclipse.cdt.ui.IRequiredInclude;
import org.eclipse.cdt.ui.text.ICHelpInvocationContext;
import org.eclipse.cdt.ui.text.IHoverHelpInvocationContext;
import org.eclipse.cdt.ui.text.SharedASTJob;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.IFileSystem;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.core.runtime.Status;
import org.eclipse.help.IHelpResource;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IRegion;
import org.eclipse.linuxtools.cdt.libhover.ClassInfo;
import org.eclipse.linuxtools.cdt.libhover.FunctionInfo;
import org.eclipse.linuxtools.cdt.libhover.HelpBook;
import org.eclipse.linuxtools.cdt.libhover.LibHoverInfo;
import org.eclipse.linuxtools.cdt.libhover.LibhoverPlugin;
import org.eclipse.linuxtools.cdt.libhover.MemberInfo;
import org.eclipse.linuxtools.internal.cdt.libhover.preferences.PreferenceConstants;


public class LibHover implements ICHelpProvider {

	public final static String LIBHOVER_DOC_EXTENSION = LibhoverPlugin.PLUGIN_ID + ".library"; //$NON-NLS-1$

    // see comment in initialize()
    // private static String defaultSearchPath = null;

	private static ConcurrentHashMap<ICHelpBook, LibHoverLibrary> libraries = new ConcurrentHashMap<>();

    static final String  constructTypes[] = {
    	"dtype", //$NON-NLS-1$
    	"enum",  //$NON-NLS-1$
    	"function", //$NON-NLS-1$
    	"groupsynopsis", //$NON-NLS-1$
    	"struct", //$NON-NLS-1$
    	"type",  //$NON-NLS-1$
    	"union"  //$NON-NLS-1$
    };

    static final int dtypeIndex         = 0;
    static final int enumIndex          = 1;
    static final int functionIndex      = 2;
    static final int groupsynopsisIndex = 3;
    static final int structIndex        = 4;
    static final int typeIndex          = 5;
    static final int unionIndex         = 6;

    private static ArrayList<ICHelpBook> helpBooks = new ArrayList<>();
    private static Map<String, ICHelpBook> helpBooksMap = new HashMap<>();
    public static boolean docsFetched = false;

    public static Collection<LibHoverLibrary> getLibraries() {
    	return libraries.values();
    }

    public static void saveLibraries() {
    	// If user preference is to cache libhover data, then save any un-saved
    	// library hover data.
    	IPreferenceStore ps = LibhoverPlugin.getDefault().getPreferenceStore();
    	if (ps.getBoolean(PreferenceConstants.CACHE_EXT_LIBHOVER)) {
    		IPath locationBase = LibhoverPlugin.getDefault().getStateLocation();
    		for (Iterator<LibHoverLibrary> i = libraries.values().iterator(); i.hasNext();) {
    			LibHoverLibrary l = i.next();
    			try {
    				// Now, output the LibHoverInfo for caching later
    				IPath locationDir = locationBase;
    				if (l.isCPP())
    					locationDir = locationBase.append("CPP"); //$NON-NLS-1$
    				else
    					locationDir = locationBase.append("C"); //$NON-NLS-1$
    				File lDir = new File(locationDir.toOSString());
    				lDir.mkdir();
    				IPath location = locationDir.append(getTransformedName(l.getName()) + ".libhover"); //$NON-NLS-1$
    				File target = new File(location.toOSString());
    				if (!target.exists()) {
    					FileOutputStream f = new FileOutputStream(locationDir.append("tmpFile").toOSString()); //$NON-NLS-1$
    					ObjectOutputStream out = new ObjectOutputStream(f);
    					out.writeObject(l.getHoverInfo());
    					out.close();
    					File tmp = new File(locationDir.append("tmpFile").toOSString()); //$NON-NLS-1$
    					tmp.renameTo(target);
    				}
    			} catch(Exception e) {
    				e.printStackTrace();
    			}
    		}
    	}
    }

	public static synchronized void getLibHoverDocs() {
		if (docsFetched)
			return;
		libraries.clear();
		helpBooks.clear();
		helpBooksMap.clear();
		// Check if caching of library info is enabled and if so, get any
		// cached library hover info.
		IPreferenceStore ps = LibhoverPlugin.getDefault().getPreferenceStore();
		if (ps.getBoolean(PreferenceConstants.CACHE_EXT_LIBHOVER)) {
			// Look for cached libhover files in the plugin state location
			IPath stateLocation = LibhoverPlugin.getDefault().getStateLocation();
			IFileSystem fs = EFS.getLocalFileSystem();
			IPath CLibraryLocation = stateLocation.append("C"); //$NON-NLS-1$
			IPath CPPLibraryLocation = stateLocation.append("CPP"); //$NON-NLS-1$
			IFileStore cDir = fs.getStore(CLibraryLocation);
			if (cDir.fetchInfo().exists())
				getCachedLibraries(cDir, "C"); //$NON-NLS-1$
			IFileStore cppDir = fs.getStore(CPPLibraryLocation);
			if (cppDir.fetchInfo().exists())
				getCachedLibraries(cppDir, "C++"); //$NON-NLS-1$
		}
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
				String nameSpace = ce.getContributor().getName();
				// If library not already cached, create it
				ICHelpBook book = helpBooksMap.get(name);
				if (book == null) {
					HelpBook h = new HelpBook(name, type);
					helpBooks.add(h);
					helpBooksMap.put(name, h);
					LibHoverLibrary l = new LibHoverLibrary(name, location, helpdocs, nameSpace,
							"C++".equals(type)); //$NON-NLS-1$
					libraries.put(h, l);
				} else {
					LibHoverLibrary l = libraries.get(book);
					if (l != null)
						l.setDocs(helpdocs);
				}
				docsFetched = true;
			}
		}
	}

	private static String getTransformedName(String name) {
		return name.replaceAll("\\s", "_"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private static String getCleanName(String name) {
		return name.replaceAll("_", " "); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private static void getCachedLibraries(IFileStore dir, String type) {
		try {
			boolean isCPP = type.equals("C++"); //$NON-NLS-1$
			IFileStore[] files = dir.childStores(EFS.NONE, null);
			for (int i = 0; i < files.length; ++i) {
				IFileStore file = files[i];
				String fileName = file.fetchInfo().getName();
				if (fileName.endsWith(".libhover")) { //$NON-NLS-1$
					File f = file.toLocalFile(EFS.NONE, null);
					if (f != null) {
						String name = getCleanName(fileName.substring(0,fileName.length()-9));
						HelpBook h = new HelpBook(name, type);
						helpBooks.add(h);
						helpBooksMap.put(name, h);
						String location = file.toURI().toString();
						LibHoverLibrary l = new LibHoverLibrary(name, location, null, null, isCPP);
						libraries.put(h, l);
					}
				}
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void initialize() {
		getLibHoverDocs();
	}

	@Override
	public ICHelpBook[] getCHelpBooks () {
		ICHelpBook[] chelpbooks = new ICHelpBook[helpBooks.size()];
		return helpBooks.toArray(chelpbooks);
	}

	private static class FunctionSummary implements IFunctionSummary, Comparable<FunctionSummary> {

        private String Name;
        private String NameSpace;
        private String ReturnType;
        private String Prototype;
        private String Summary;
        private boolean prototypeHasBrackets;

        private class RequiredInclude implements IRequiredInclude {
        	private String include;

        	public RequiredInclude (String file) {
        		include = file;
        	}

        	@Override
			public String getIncludeName() {
        		return include;
        	}

        	@Override
			public boolean isStandard() {
        		return true;
        	}
        }

		@Override
		public int compareTo (FunctionSummary x) {
			FunctionSummary y = x;
			return getName().compareTo(y.getName());
		}

        private ArrayList<RequiredInclude> Includes = new ArrayList<>();

        private void setIncludeName (String iname) {
        	RequiredInclude nri = new RequiredInclude(iname);
        	Includes.add(nri);
        }

        public class FunctionPrototypeSummary implements IFunctionPrototypeSummary {
            @Override
			public String getName()             { return Name; }
            @Override
			public String getReturnType()       { return ReturnType; }
            @Override
			public String getArguments()        { return Prototype; }
            @Override
			public String getPrototypeString(boolean namefirst) {
                if (true == namefirst) {
                	if (prototypeHasBrackets())
                		return Name + " " + Prototype + " " + ReturnType; //$NON-NLS-1$ //$NON-NLS-2$
                    return Name + " (" + Prototype + ") " + ReturnType; //$NON-NLS-1$ //$NON-NLS-2$
                }
                else {
                	if (prototypeHasBrackets())
                		return ReturnType + " " + Name + " " + Prototype; //$NON-NLS-1$ //$NON-NLS-2$
                    return ReturnType + " " + Name + " (" + Prototype + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                }
            }
        }

        @Override
		public String getName()                         { return Name; }
        @Override
		public String getNamespace()                    { return NameSpace; }
        @Override
		public String getDescription()                  { return Summary; }
        public boolean prototypeHasBrackets()			{ return prototypeHasBrackets; }
        public void setPrototypeHasBrackets(boolean value)	{ prototypeHasBrackets = value; }
        @Override
		public IFunctionPrototypeSummary getPrototype() { return new FunctionPrototypeSummary(); }

        @Override
		public IRequiredInclude[] getIncludes() {
        	IRequiredInclude[] includes = new IRequiredInclude[Includes.size()];
        	for (int i = 0; i < Includes.size(); ++i) {
        		includes[i] = Includes.get(i);
        	}
        	return includes;
        }

    }

	public boolean isCPPCharacter(int ch) {
		return Character.isLetterOrDigit(ch) || ch == '_' || ch == ':';
	}

	private static class EnclosingASTNameJob extends SharedASTJob {
		private int tlength;
		private int toffset;
		private IASTName result = null;
		public EnclosingASTNameJob (ITranslationUnit t,
				int toffset, int tlength) {
			super("EnclosingASTNameJob", t); //$NON-NLS-1$
			this.toffset = toffset;
			this.tlength = tlength;
		}
		@Override
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

	public static class ASTDeclarationFinderJob extends SharedASTJob {
		private IBinding binding;
		private IASTName[] decls = null;
		public ASTDeclarationFinderJob (ITranslationUnit t, IBinding binding) {
			super("ASTDeclarationFinderJob", t); //$NON-NLS-1$
			this.binding = binding;
		}
    	@Override
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

	@Override
	public IFunctionSummary getFunctionInfo(ICHelpInvocationContext context, ICHelpBook[] helpBooks, String name) {
        IFunctionSummary f;

        f = null;
        ITranslationUnit t = context.getTranslationUnit();

        String className = null;
        ICPPFunctionType methodType = null;

        if (t.isCXXLanguage()) {
        	try {
        		if (context instanceof IHoverHelpInvocationContext) {
        			// We know the file offset of the member reference.
        			IRegion region = ((IHoverHelpInvocationContext)context).getHoverRegion();

        			// Now, let's find the declaration of the method.  We need to do this because we want the specific
        			// member prototype to go searching for.  There could be many members called "x" which have different
        			// documentation.
           			final IASTName[] result= {null};
           			EnclosingASTNameJob job = new EnclosingASTNameJob(t, region.getOffset(), region.getLength());
           			job.schedule();
           			try {
           				job.join();
           			} catch (InterruptedException e) {
           				// TODO Auto-generated catch block
           				e.printStackTrace();
           			}
           			if (job.getResult() == Status.OK_STATUS)
           				result[0] = job.getASTName();
        			if (result[0] != null) {
        				final IBinding binding = result[0].getBinding();
        				// Check to see we have a member function.
        				if (binding instanceof ICPPFunction) {
        					methodType = ((ICPPFunction)binding).getType();
        					// We have a member function, find the class name.
        					IBinding owner = ((ICPPFunction)binding).getOwner();
        					if (owner instanceof ICPPClassType) {
        						className = getClassName((ICPPClassType)owner);
        					}
        				}
        			}
        		}
        	} catch (IllegalArgumentException e) {
        		// TODO Auto-generated catch block
        		e.printStackTrace();
        	} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }

        // Loop through all the documents we have and report first match.
        for (int i = 0; i < helpBooks.length; ++i) {
        	LibHoverLibrary l = libraries.get(helpBooks[i]);
        	if (name != null) {
        		if (className != null) {
        			if (l.isCPP())
        				f = getMemberSummary(l, className, name, methodType);
        		} else {
        			f = getFunctionSummary(l, name);
        		}
        		if (f != null)
        			return f;
        	}
        }

        return null;
	}

	// Get the class name for a type, including any instance template parameters
	// e.g. std::basic_string<char>
	private String getClassName(ICPPClassType c) {
		String className = null;
		try {
			String[] qualified = c.getQualifiedName();
			className = qualified[0];
			for (int k = 1; k < qualified.length; ++k) {
				className += "::" + qualified[k]; //$NON-NLS-1$
			}

			// Check if we have an instance of a template class.
			if (c instanceof ICPPTemplateInstance) {
				ICPPTemplateInstance ti = (ICPPTemplateInstance)c;
				// Get a map which tells us the values of the template
				// arguments (e.g. _CharT maps to char in the instance).
				ICPPTemplateParameterMap tiMap = ti.getTemplateParameterMap();
				ICPPTemplateDefinition td = ti.getTemplateDefinition();
				ICPPTemplateParameter[] templateArgs = td.getTemplateParameters();
				className += "<"; //$NON-NLS-1$
				String separator = ""; //$NON-NLS-1$
				for (int x = 0; x < templateArgs.length; ++x) {
					ICPPTemplateParameter tp = templateArgs[x];
					ICPPTemplateArgument ta = tiMap.getArgument(tp);
					IType type = null;
					// The template may have a type specified or a value.
					// In the case of a value, figure out its type and use
					// that when we do a lookup.
					if (ta.isTypeValue())
						type = ta.getTypeValue();
					else
						type = ta.getTypeOfNonTypeValue();
					if (tp.getTemplateNestingLevel() == 0) {
						// if the parameter is a class type, use recursion to
						// get its class name including template parameters
						if (type instanceof ICPPClassType)
							className += separator + getClassName((ICPPClassType)type);
						else
							className += separator + type.toString();
						separator = ","; //$NON-NLS-1$
					}
				}
				className += ">"; //$NON-NLS-1$
			}
		} catch(DOMException e) {
			return null;
		}
		return className;
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

	private IFunctionSummary getMemberSummary(LibHoverLibrary l, String className,
			String memberName, ICPPFunctionType methodType) {

		ArrayList<String> templateTypes = new ArrayList<>();
		ClassInfo info = l.getClassInfo(className, templateTypes);
		String[] args = new String[0];
		@SuppressWarnings("unused")
		IType returnType = null;
		if (info == null)
			return null;
		if (methodType != null) {
			try {
				args = resolveArgs(info, methodType.getParameterTypes(), templateTypes);
				returnType = methodType.getReturnType();
			} catch (Exception e) {
				return null;
			}
		}
		MemberInfo member = info.getMember(memberName);
		if (member != null) {
			MemberInfo m = null;
			if (!isParmMatch(member, args, templateTypes, info)) {
				ArrayList<MemberInfo> members = member.getChildren();
				for (int i = 0; i < members.size(); ++i) {
					MemberInfo k = members.get(i);
					if (isParmMatch(k, args, templateTypes, info)) {
						m = k;
						break;
					}
				}
			} else {
				m = member;
			}

			if (m != null) {
				// FIXME: do some work to determine parameters and return type.
				FunctionSummary f = new FunctionSummary();
				f.ReturnType = m.getReturnType();
				f.Prototype = m.getPrototype();
				f.Summary = m.getDescription();
				f.Name = className + "::" + memberName; //$NON-NLS-1$
				String[] templateParms = info.getTemplateParms();
				for (int i = 0; i < templateTypes.size(); ++i) {
					f.ReturnType = f.ReturnType.replaceAll(templateParms[i], templateTypes.get(i));
					f.Prototype = f.Prototype.replaceAll(templateParms[i], templateTypes.get(i));
					f.Name = f.Name.replaceAll(templateParms[i], templateTypes.get(i));
				}
				if (f.ReturnType.indexOf('<') >= 0) {
					f.ReturnType = f.ReturnType.replaceAll("<", "&lt;"); //$NON-NLS-1$ //$NON-NLS-2$
					f.ReturnType = f.ReturnType.replaceAll(">", "&gt;");  //$NON-NLS-1$//$NON-NLS-2$
				}
				if (f.Prototype.indexOf('<') >= 0) {
					f.Prototype = f.Prototype.replaceAll("<", "&lt;"); //$NON-NLS-1$ //$NON-NLS-2$
					f.Prototype = f.Prototype.replaceAll(">", "&gt;");  //$NON-NLS-1$//$NON-NLS-2$
				}
				if (f.Name.indexOf('<') >= 0) {
					f.Name = f.Name.replaceAll("<", "&lt;");  //$NON-NLS-1$//$NON-NLS-2$
					f.Name = f.Name.replaceAll(">", "&gt;"); //$NON-NLS-1$ //$NON-NLS-2$
				}
				f.setPrototypeHasBrackets(true);
				f.setIncludeName(info.getInclude());
				return f;
			}
		}
		return null;
	}


	private boolean isParmMatch(MemberInfo m, String[] args, ArrayList<String> templateTypes, ClassInfo info) {
		String[] memberParms = m.getParamTypes();
		String className = info.getClassName();
		int index = className.lastIndexOf("::"); //$NON-NLS-1$
		String unqualifiedName = className.substring(index+2);
		for (int i = 0; i < memberParms.length; ++i) {
			String[] templateParms = info.getTemplateParms();
			for (int j = 0; j < templateTypes.size(); ++j) {
				memberParms[i] = memberParms[i].replaceAll(templateParms[j], templateTypes.get(j));
			}
			// Look for the class being passed by reference...the doc prototype will not fill in
			// the template parms nor the qualifier so we do it here to make sure we match what
			// is coming back from the indexer which will be fully-qualified and have template
			// parameters specified.
			if (memberParms[i].contains(unqualifiedName) && !memberParms[i].contains(className)) {
				String classTemplate = ""; //$NON-NLS-1$
				if (templateTypes.size() > 0) {
					classTemplate = "<"; //$NON-NLS-1$
					String separator = ""; //$NON-NLS-1$
					for (int j = 0; j < templateTypes.size(); ++j) {
						classTemplate += separator + templateTypes.get(j);
						separator = ","; //$NON-NLS-1$
					}
					classTemplate += ">"; //$NON-NLS-1$
				}
				memberParms[i] = memberParms[i].replaceAll(unqualifiedName, className + classTemplate);
			}
		}
		return Arrays.equals(memberParms, args);
	}

	private String[] resolveArgs(ClassInfo info, IType[] parameterTypes, ArrayList<String> templateTypes) {
		String[] templateParms = info.getTemplateParms();
		String[] result = new String[parameterTypes.length];
		for (int i = 0; i < parameterTypes.length; ++i) {
			String param = parameterTypes[i].toString();
			param = param.replaceAll("\\{.*\\}", ""); //$NON-NLS-1$ //$NON-NLS-2$
			param = param.trim();
			int index = param.indexOf("#"); //$NON-NLS-1$
			while (index >= 0) {
				// We assume no class has more than 9 template parms.
				int digit = param.charAt(index + 1) - '0';
				// where possible, replace template parms with real values
				if (digit < templateTypes.size())
					param = param.replaceFirst(param.substring(index, index + 2), templateTypes.get(digit));
				else
					param = param.replaceFirst(param.substring(index, index + 2), templateParms[digit]);
				index = param.indexOf("#"); //$NON-NLS-1$
			}
			result[i] = param;
		}
		return result;
	}

	@Override
	public IFunctionSummary[] getMatchingFunctions(ICHelpInvocationContext context, ICHelpBook[] helpBooks, String prefix) {
		ArrayList<IFunctionSummary> fList = new ArrayList<>();

		for (int di = 0; di < helpBooks.length; ++di) {
			LibHoverLibrary l = libraries.get(helpBooks[di]);
			LibHoverInfo cppInfo = l.getHoverInfo();
			SortedMap<String, FunctionInfo> map = cppInfo.functions.tailMap(prefix);
			Set<Map.Entry<String, FunctionInfo>> c = map.entrySet();
			for (Iterator<Entry<String, FunctionInfo>> i = c.iterator(); i.hasNext();) {
				Map.Entry<String, FunctionInfo> e = i.next();
				FunctionInfo x = e.getValue();
				String name = x.getName();
				// Look for names that start with prefix, but ignore names that
				// start with "0" which is used to import text data that cannot
				// be omitted from the binary version of the document (e.g. invariant
				// sections of a GFDL licensed document).  This data is given a
				// function name that starts with the character "0" which is not
				// valid for the start of a C/C++ function name.  As such, it should
				// never be offered as a choice for an empty prefix.
				if (name.startsWith(prefix) && !name.startsWith("0")) { //$NON-NLS-1$
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
		}
		IFunctionSummary[] summaries = new IFunctionSummary[fList.size()];
		for (int k = 0; k < summaries.length; k++) {
			summaries[k] = fList.get(k);
		}
		return summaries;
	}

	private static class HelpResource implements IHelpResource {
		private String href;
		private String label;
		public HelpResource(String href, String label) {
			this.href = href;
			this.label = label;
		}
		@Override
		public String getHref() {
			return href;
		}
		@Override
		public String getLabel() {
			return label;
		}
	}

	private static class HelpResourceDescriptor implements ICHelpResourceDescriptor {
		private ICHelpBook helpbook;

		public HelpResourceDescriptor(ICHelpBook helpbook) {
			this.helpbook = helpbook;
		}

		@Override
		public ICHelpBook getCHelpBook() {
			return helpbook;
		}

		@Override
		public IHelpResource[] getHelpResources() {
			LibHoverLibrary l = libraries.get(helpbook);
			if (l != null) {
				IHelpResource[] hr = new IHelpResource[1];
				hr[0] = new HelpResource(l.getDocs(), l.getName());
				return hr;
			}
			return null;
		}
	}

	@Override
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
