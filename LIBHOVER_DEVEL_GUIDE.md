Introduction
------------

The Libhover plug-in from the Linux Tools project provides a common interface for supplying C and C++ hover help for libraries. The plug-in uses a CDT (C/C++ Developer Tools) Help extension to register itself with the CDT. When a C or C++ file is presented in the editor and a hover event occurs, the CDT will call the Libhover plug-in to get information. In turn, the Libhover plug-in supplies its own extension which allows the end-user to specify a set of valid hovers to use. Each hover library can be enabled or disabled for a C/C++ project via the Project-\>Properties-\>C/C++ General-\>Documentation page. There a list of the valid hovers are shown and the user can check or un-check them as desired. Note that Libhover help suppliers set the language of the hover help and so a C project will ignore any C++ hover libraries. For a C++ project, both C and C++ library hovers are valid so they will all appear on the Documentation page.

Libhover Extension
------------------

The Libhover plug-in adds a new org.eclipse.linuxtools.cdt.libhover.library extension to be used in a plug-in. Let's examine an example which specifies libhover help for the glibc C Library:

    <extension
        id="library"
        name="Glibc C Library"
        point="org.eclipse.linuxtools.cdt.libhover.library">
        <library
              docs="http://www.gnu.org/software/libc/manual/html_node/index.html"
              location="./data/glibc-2.7-2.libhover"
              name="glibc library"
              type="C">
        </library>;
    </extension>;

Fields are as follows:

-   id - unique id for this extension (required)
-   name - name of the extension (required)
-   library - details of the library (1 or more)
    -   docs - URL location of external help documentation (optional)
    -   location - location of libhover binary data (either URL or relative location to plug-in) (required)
    -   name - name that will appear in the C/C++ Documentation page for this hover help (required)
    -   type - one of (C, C++, or ASM) (required)

Note that the location can be specified local to the plug-in that declares the extension. This obviously saves time when accessing the data before a hover event.

Libhover Data
-------------

So what is Libhover data? Libhover data is merely a Java serialized class that is stored in binary format. Java serialization allows one to save and restore a class to/from a file. The Libhover class is really org.eclipse.linuxtools.cdt.libhover.LibhoverInfo:

    public class LibHoverInfo implements Serializable { 
     
    private static final long serialVersionUID = 1L; 
     
      public HashMap<String, ClassInfo> classes = new HashMap<String, ClassInfo>(); 
      public HashMap<String, TypedefInfo> typedefs = new HashMap<String, TypedefInfo>(); 
      public TreeMap<String, FunctionInfo> functions = new TreeMap<String, FunctionInfo>(); 
     
    } 

The class is just a collection of Maps from name to C++ class, name to C++ typedef, and name to C function. A C library hover info will only fill in the last map whereas a C++ library hover info will typically only fill in the first two.

C Library Data
--------------

The simplest form of Libhover data is for C functions. Looking at org.eclipse.linuxtools.cdt.libhover.FunctionInfo:

    public class FunctionInfo implements Serializable {
       
      private static final long serialVersionUID = 1L;
      private String name;
      private String prototype;
      private String desc;
      private String returnType;
      private ArrayList<String> headers;
      private ArrayList<FunctionInfo> children; 
      
    }

we see the class is made up of String fields containing the function data that will be pieced together in the hover window. The prototype does not include the outer parentheses. The desc field is the description of the function and can is treated as html format. The children field is for future support of C++ overloaded functions. This is due to the fact that we look-up a function by name in the HashMap to make it quickly referenced. When there is overloading of function names (C++ only), then we register the first function found in the map and use the children field to store all others in no particular order. Currently, overloaded functions are not supported by the Libhover look-up mechanism, but this functionality could be added if required. All the fields are accessed via get and set methods (e.g. getName(), setDesc()).

### C Library Hover Utility

To aid in building C library hover data, a utility has been created that will take xml and create the libhover binary data in the form of a file with suffix ".libhover". The utility is found in the org.eclipse.linuxtools.cdt.libhover plug-in as org.eclipse.linuxtools.cdt.libhover.utils.BuildFunctionInfos.java. Run the file as a Java application (it has a static main method) and pass to it two parameters:

1.  the URL or file location of the xml file to parse
2.  the location where the output should be placed

Once finished you can place the .libhover file in your plug-in and use the Libhover Library extension to specify a local location.

XML files referenced must adhere to the following xml structure:


    <!DOCTYPE descriptions [

      <!ELEMENT descriptions (construct)*>

      <!ELEMENT construct (structure|function)*>
      <!ATTLIST construct
        id ID #REQUIRED
        type CDATA #REQUIRED
      >

      <!ELEMENT structure       (synopsis?, elements?)?>

      <!ELEMENT elements     (element*)>

      <!ELEMENT element (synopsis*)>
      <!ATTLIST element
        content CDATA #REQUIRED
      >

      <!ELEMENT synopsis     (#PCDATA)*>

      <!ELEMENT function     (prototype,headers?,synopsis)>
      <!ATTLIST function
        returntype CDATA #REQUIRED
      >

      <!ELEMENT prototype    (parameter+)?>

      <!ELEMENT parameter (#PCDATA)*>
      <!ATTLIST parameter
        content CDATA #REQUIRED
      >

      <!ELEMENT headers      (header+)?>

      <!ELEMENT header (#PCDATA)*>
      <!ATTLIST header
        filename CDATA #REQUIRED
      >

    ]>

Note that function ids need to be prefixed by "function-". For example, for the C atexit function:

    <descriptions>
      <construct id="function-atexit" type="function">
        <function returntype="int">
          <prototype>
            <parameter content="void (*function) (void)"/>
          </prototype>
          <headers>
            <header filename = "stdlib.h"/>
          </headers>
            <synopsis>
            The &lt;CODE&gt;atexit&lt;/CODE&gt; function registers the function &lt;VAR&gt;function&lt;/VAR&gt; to be
             called at normal program termination.  The &lt;VAR&gt;function&lt;/VAR&gt; is called with
             no arguments.
             &lt;br&gt;&lt;br&gt; The return value from &lt;CODE&gt;atexit&lt;/CODE&gt; is zero on success and nonzero if
             the function cannot be registered.
            </synopsis>
        </function>
      </construct>
    </descriptions>

Also note that the synopsis is output as html. To specify html tags, one needs to use &lt; and &gt; as delimeters in place of "&lt" and "&gt". In the previous example, VAR tags are used for variable references, CODE tags for the function name, and br tags for forcing paragraph breaks. All of these make the hover look more interesting when displayed.

For glibc, a parser was written to parse the glibc/manual directory and process the texinfo files to form the xml file format above.

C++ Library Hover
-----------------

C++ library hover data is more complex because a member cannot be accessed just by name. One needs to first know from which class the member is being accessed and the signature of the call since member names can be overloaded. Additional complexities arise because the member might actually belong to a base class of the given class used in the call or the class may be a typedef of another class or a template instance. Template instances are tricky because there is substitution that occurs for parameterized types.

A utility org.eclipse.linuxtools.cdt.libhover.libstdcxx.DoxygenCPPInfo was created to parse the Doxygen documentation output for the libstdc++ library. If you can get your library documentation into the same format, then all you need to do is to use the utility, passing two parameters:

1.  location of the Doxygen xml input
2.  location to place the output libhover data file

Failing that, you will need to create your own library hover info. Let's look at the fields of interest in org.eclipse.linuxtools.cdt.libhover.ClassInfo

    public class ClassInfo implements Serializable {

        private static final long serialVersionUID = 1L;
        private String templateParms[];
        private String className;
        private String include;
        private ArrayList<ClassInfo> baseClasses;
        private HashMap<String, MemberInfo> members;
        private ArrayList<ClassInfo> children;
    }

The following describes each field:

-   templateParms - this is used to store the template parameters of this class (e.g. A\<\_T, \_U, Integer\> would store "\_T" and "\_U". Real types are not part of this list. These are needed to perform replacement in the description text (e.g. the return value of a member function may be specified as a template parameter).
-   className - this is the name of the class including the template specification. Any template parameters from templateParms are replaced with a generic regex "[a-zA-Z0-9\_: \*]+" which allows us to do a quick regex match on a template (e.g. A<Integer, Double> would match A\<\_T, \_U\>.
-   include - this is the name of the header file that contains this class
-   baseClasses - the ClassInfo data of any base classes of this class
-   members - maps member names to MemberInfo (only 1 per name with MemberInfo chaining when overloading exists).
-   children - this is the set of template classes with the same name as this class

Note that the name used to hash the ClassInfo in the LibhoverInfo class map is the class name minus any template specification.

The TypedefInfo is merely a way to find the actual class we are seeking:

    public class TypedefInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        private String[] templates;
        private String typedefName;
        private String transformedType;
        private ArrayList<TypedefInfo> children = null;
    };

-   typedefName - name of the typedef with any template parameters replaced with a generic regex string "[a-zA-Z0-9\_: \*]+"
-   templates - this the set of template parameters from the transformed class name
-   transformedType - what the typedef transforms into
-   children - used when there are multiple typedefs of the same name (e.g. partial templates)

It is assumed that the typedef will use the same template parameters as the class it represents. For example, if we have class A\<\_T, \_U\> and we could have a typedef B\<\_T\> which transforms to A\<\_T, Integer\>.

The MemberInfo class is much like the FunctionInfo class:

    public class MemberInfo implements Serializable {

        private static final long serialVersionUID = 1L;
        private String name;
        private String prototype;
        private String desc;
        private String returnType;
        private String[] paramTypes;
        private ArrayList<MemberInfo> children;
        
    };

and contains the actual hover data of interest. The following are the fields of interest:

-   name - member name
-   prototype - prototype minus outer parentheses
-   desc - member description in html format
-   returnType - the return type of the member function
-   paramTypes - an array of just the parameter types of this function without template replacement. The array is used in conjunction with template types to verify we have the correct member being used (e.g. a(\_T, \_U) of A\<\_T, \_U\> is a match for a(Integer k, Double l) of A<Integer, Double> class).
-   children - members with the same name as this (i.e. overloaded method signatures)

Devhelp Library Hover
---------------------

The org.eclipse.linuxtools.cdt.libhover.devhelp plug-in adds support for dynamically processing installed documentation formatted for use by the Devhelp API browser.

Documentation is generated by GtkDoc either from specially formatted comments in the C code or via adding to template files created after GtkDoc parses the header files. From these files, GtkDoc creates a Docbook xml file or sgml file which can be used to create html. Various packages use this form of documentation which is installed in a common area for the Devhelp API browser to locate.

The Devhelp libhover plug-in provides a new preferences page under Libhover-\>Devhelp for configuration.

A text entry is provided to specify where the Devhelp documentation is installed on the current system (default /usr/share/gtk-doc). An additional button is provided to start the generation (or regeneration) of the Devhelp libhover documentation. Pressing the button starts an Eclipse Job that can be put into the background or cancelled.

The results of the job replace the current Devhelp libhover binary data currently loaded.

To create documentation in a format that can be used, see the [GtkDoc manual](https://wiki.gnome.org/DocumentationProject/GtkDoc).

Libhover Logic
--------------

For C hover, Libhover is given the name of the C function to find and a list of C HelpBooks that are enabled. These HelpBooks correspond to the Project-\>Properties-\>C/C++ General-\>Documentation items that are enabled and that were registered by the Libhover plug-in (these correspond to "C" type library hover infos included by the Libhover Library extension). For each C Library info in the list, Libhover does a find of the name in the FunctionInfo map. If any FunctionInfo is found, it is transformed into the CDT format required. Otherwise, null is returned.

For C++, it is more complicated. The CDT provides the location in the editor that the hover is for. From this, Libhover consults the CDT indexer for the context of the hover which includes the class name and the member signature. Once this is acquired, Libhover first looks for the class name in the TypdefInfo map. If it is found and this isn't a templated typedef, the transformed name is then used as the class name. In the case of a template, the TypedefInfo and all its children are checked one by one for a regex match of the typedef name with the given typedef. Remember that for template parameters we substituted a generic regex string in the typedef name.

Now we have a class name. We use that class name to access the ClassInfo map. If we don't match, we return null. Otherwise, we may have to resolve templates so we perform a regex match of the class name with the class name in question, again we have substituted a generic regex string for template parameters. If no match, we return null.

Now we have a ClassInfo and only need to find the member in question. We start by searching the immediate members of the ClassInfo and if needed, we then start looking in base classes. We start by accessing the MemberInfo map by name. If we have a match, we need to check if the MemberInfo has children, indicating overloading. If overloading has occurred, we need to check the parameter types and return type of each member to find a match. The same check applies if we are forced to look in the base classes. It is assumed that base classes are not typedefs themselves. If this needs to be modified in the future, then the baseClasses list would be of type String and then a new transform would have to be performed.

Once the correct MemberInfo is located, the details are used to supply the CDT hover information. Any template parameters are substituted in the prototype, return type, and name of the member function. Currently, this substitution is not performed on the member description though it would be simple to add if needed.
