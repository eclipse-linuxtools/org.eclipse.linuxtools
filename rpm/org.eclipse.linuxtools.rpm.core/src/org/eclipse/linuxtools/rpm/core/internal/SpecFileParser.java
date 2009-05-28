/*
 * (c) 2005 Red Hat, Inc.
 *
 * This program is open source software licensed under the 
 * Eclipse Public License ver. 1
 */
package org.eclipse.linuxtools.rpm.core.internal;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.rpm.core.IRPMConstants;

/**
 * A spec file parser.
 *
 */
public class SpecFileParser {

	private int lastSourceLine = 0;
	private int lastPatchLine = 0;
	private int setupLine = 0;
	private int lastPatchMacroLine = 0;
    private int numPatches = 0;
    
    private IFile specFile;
    private String configureArgs;
    private String name;
    private String version;
    private String release;
    
	/**
	 * Constructs a new parser.
	 * @param specFile a handle to the workspace spec file
	 */
    public SpecFileParser(IFile specFile) {
        this.specFile = specFile;
    }
    
	/**
	 * Parses the spec file.
	 * @throws CoreException if parsing fails
	 */
    public void parse() throws CoreException {
        /* The following logic determines where in the spec file the "Patchx:" and
         * %patchx -p1" lines will need to be added to accomodate the patch we
         * are fixing to generate.  If this is the first patch to ever be added to this
         * source RPM then the "Patchx: statement will have to be added after the
         * last "Sourcex:" statement and the "%patch -p1" statement will need to be
         * added after the "%setup" statement.  If this is not the first patch for this
         * source rpm, the "Patchx:" statement will be added after the last "Patchx:"
         * statement and the "%patchx -p1" will be added after the last "%patch -p1"
         * statement.  So, we keep track of where the line numbers for all of these
         * eventualities are so when we mod the file we will know where to insert
         * the necessary new lines.
         */
        ArrayList<String> patchlist = new ArrayList<String>();
        boolean found_patch = false;
        boolean found_define = false;
        boolean found_define_name = false;
        int define_ctr = 0;
        int lines = 1;

        try {
            FileReader sp_file = new FileReader(specFile.getLocation().toOSString());
            StreamTokenizer st = new StreamTokenizer(sp_file);

            // Make sure numbers, colons and percent signs are considered valid
            st.wordChars('a','z');
            st.wordChars('A','Z');
            st.wordChars(':', ':');
            st.wordChars('0', '9');
            st.wordChars('%', '%');
            st.wordChars('{', '}');
            st.wordChars('-', '-');
            st.wordChars('/', '/');
            st.wordChars('=','=');
            st.wordChars('.','.');
            st.wordChars('_','_');
            st.eolIsSignificant(true);
            
            String new_word;
            boolean check_ifs = false;
            int if_ctr = 0;
            int token = st.nextToken();
            while (token != StreamTokenizer.TT_EOF) {
                token = st.nextToken();

                switch (token) {
                case StreamTokenizer.TT_EOL:
                  lines++;
                  break;
                case StreamTokenizer.TT_WORD:
                    new_word = st.sval;
                    
/* The following commented out logic addresses bugzilla 110452 where the version and
 * release numbers for spec files are stored in "%define" variables at the top of the file.  It
 * has been decided to put this change on hold until it can be determined how pervasive
 * the use of this practice is.  The code is incomplete for the time being and may be deleted
 * entirely in future releases.
 */                   
/*                  if (found_version) {
                        found_version = false;
                        if (new_word.startsWith("%{")) {  //$NON-NLS-1$
                            version_param = true;
                            define_info.add(0,new_word.substring(2,new_word.length()-1));
                        }
                        break;
                    }
                    
                    if (found_release) {
                        found_release = false;
                        if (new_word.startsWith("%{")) {  //$NON-NLS-1$
//                          release_param = true;
                            define_info.add(1,new_word.substring(2,new_word.length()-1));
                        }
                        break;
                    }  */
                    
                    // See if we have found the Version: line
                    if (new_word.equals("Version:")) {  //$NON-NLS-1$
                        break;
                    }
                    
                    // See if we have found the Release: line
                    if (new_word.equals("Release:")) {  //$NON-NLS-1$
                        break;
                    }

                        // Record where the last line of the form "Sourcex:" is
                        if (new_word.startsWith("Source") &  //$NON-NLS-1$
                             new_word.endsWith(":")) { //$NON-NLS-1$
                            lastSourceLine = lines;
                            break;
                        }

                        /* Record where the last line of the form "Patchx:" is and count how many there were.
                         * Also, record the statement so when we generate our new "Patchx:" statement
                         * we don't duplicate a "Patch" statement.  This has to be done because a lot of
                         * spec files have "Patchx:" statements that are non-sequential
                         */
                        if (new_word.startsWith("Patch") &  //$NON-NLS-1$
                               new_word.endsWith(":")) { //$NON-NLS-1$
                            lastPatchLine = lines;
                            numPatches++;
                            patchlist.add(new_word);

                            break;
                        }

                        // Record where the "%setup line is
                        if (new_word.equals("%setup")) { //$NON-NLS-1$

                            // set the "check for if" constructs switch
                            check_ifs = true;
                            setupLine = lines;

                            break;
                        }

                        if (new_word.equals("%build")) { //$NON-NLS-1$
                            check_ifs = false;
                            
                            break;
                        }

                        // Record where the last (if any) "%patchx" line is
                        if (new_word.startsWith("%patch")) { //$NON-NLS-1$
                            lastPatchMacroLine = lines;
                            found_patch = true;

                            break;
                        }
                        
                        // See if we have found a %define statement, if so save it as some
                        // source RPMs use %define statements to "define" version/release #'s
/* See the comment several lines above regarding bugzilla 110452 as it also pertains to this code */
/*                      if (new_word.equals("%define")) {  //$NON-NLS-1$
                            found_define = true;
                            define_line_ptr[define_line_ctr] = lines;
                            define_line_ctr++;
                            
                            break;
                        }  */
                        
                    if (found_define) {
                        found_define = false;
//                      define_info.add(define_ctr,new_word);
                        define_ctr++;
                        found_define_name = true;
                        break;
                    }
                    
                    if (found_define_name) {
                        found_define_name = false;
//                      define_info.add(define_ctr,new_word);
                        define_ctr++;
                        break;
                    }

                        // Set the found %if/%ifarch/%ifnarch/%ifos/%ifnos switch
                        if (check_ifs) {
                            if (new_word.startsWith("%if")) { //$NON-NLS-1$
                                if_ctr++;

                                break;
                            }

                            // Reset the found %if/%ifarch switch
                            if (new_word.equals("%endif")) { //$NON-NLS-1$

                                if ((if_ctr > 0) & found_patch) {
                                    if_ctr--;
                                    lastPatchMacroLine = lines;
                                    found_patch = false;

                                    break;
                                }
                            }

                            break;
                        }
                        
                        break;

                default:
                    break;
                }
            }

            sp_file.close();
        } catch (IOException e) {
            e.printStackTrace();
            String throw_message = Messages.getString(
                    "RPMCore.Error_parsing_the_spec_file_in_the_project_--_157") + //$NON-NLS-1$
                    specFile.getLocation().toOSString();
            IStatus error = new Status(IStatus.ERROR, IRPMConstants.ERROR, 1, throw_message,
                    null);
            throw new CoreException(error);
        }

        if (numPatches > 1) {
            int patch_num = getUniquePatchId(patchlist, numPatches);
            numPatches = patch_num;
        }
        setConfigureArgs(parseConfigureArgs());
        parseNameVerRel();
    }
    
    /**
     * Method parseConfigureArgs.
     * This method takes a spec file path and parses it to see if there are any options
     * that need to be passed to the "configure" script when conmfiguring an RPM.
     * @param path_to_specfile - contains a string with a path to the spec file to be
     * searched to see if the "configure" command has any options to be applied
     * @return a string containing the options to pass to configure if any were found
     */
    private String parseConfigureArgs() throws CoreException {
        String path_to_specfile = specFile.getLocation().toOSString();
    
        boolean found_config = false;
        int lines = 0;
        int config_line = 0;
        String config_opts = ""; //$NON-NLS-1$
        
        try {
            FileReader sp_file = new FileReader(path_to_specfile);
            StreamTokenizer st = new StreamTokenizer(sp_file);
//            st.resetSyntax();

            // Make sure numbers, colons and percent signs are considered valid
            st.wordChars('a','z');
            st.wordChars('A','Z');
            st.wordChars(':', ':');
            st.wordChars('0', '9');
            st.wordChars('%', '%');
            st.wordChars('{', '}');
            st.wordChars('-', '-');
            st.wordChars('/','/');
            st.wordChars('=','=');
            st.wordChars('.','.');
            st.wordChars('_','_');
            st.eolIsSignificant(true);
            
            String new_word;
            int token = st.nextToken();
            while (token != StreamTokenizer.TT_EOF) {
                token = st.nextToken();

                switch (token) {
                case StreamTokenizer.TT_EOL:
                  lines++;  
                  break;
                case StreamTokenizer.TT_WORD:
                    new_word = st.sval;
                    // System.out.println("---- " + new_word + line_sep + "   line no = " + st.lineno());
                    
                    // If '%configure' was found, gather the options if there were any
                    if (found_config & config_line == lines) {
                        config_opts = config_opts + " --" + new_word; //$NON-NLS-1$
                        break;
                    }
                    if (found_config & !(config_line == lines)) {
                        found_config = false;
                        break;
                    }

                        // See if there is a %configure section
                        if (new_word.equals("%configure")) { //$NON-NLS-1$
                            found_config = true;
                            config_line = lines;
                            
                            break;
                        }
                }
            }

            sp_file.close();
        } catch (IOException e) {
            String throw_message = Messages.getString(
                    "RPMCore.Error_parsing_the_spec_file_in_the_project_--_157") + //$NON-NLS-1$
                    path_to_specfile;
            IStatus error = new Status(IStatus.ERROR, IRPMConstants.ERROR, 1, throw_message,
                    null);
            throw new CoreException(error);
        }

        if(!found_config) {
            return null;
        }
        return config_opts;
    }
    
    /**
     * Method parseNameVerRel interrogates a spec file for the name, version and release
     * of the RPM
     * @param path_to_specfile contains a string pointing to the specfile to interrogate
     * @return if successful, throw Exception if not
     */

    private void parseNameVerRel() throws CoreException {
        String path_to_specfile = specFile.getLocation().toOSString();
        ArrayList<String> rpm_info = new ArrayList<String>();
        ArrayList<String> define_info = new ArrayList<String>();

        // initialize version/release numbers to 0 in case none are found in the spec file
        rpm_info.add(0, "0"); //$NON-NLS-1$
        rpm_info.add(1, "0"); //$NON-NLS-1$
        rpm_info.add(2, " "); //$NON-NLS-1$

        boolean found_version = false;
        boolean found_release = false;
        boolean found_name = false;
        boolean found_ver_token = false;
        boolean found_rel_token = false;
        boolean found_name_token = false;
        boolean found_define = false;
        boolean found_define_name = false;
        int define_ctr = 0;
        
        File f = new File(path_to_specfile);

        if (!f.exists()) {
            String throw_message = "" + //$NON-NLS-1$
                path_to_specfile;
            IStatus error = new Status(IStatus.ERROR, Messages.getString("RPMCore.Error_1"), 1, //$NON-NLS-1$
                    throw_message, null);
            throw new CoreException(error);
        }

        try {
            FileReader sp_file = new FileReader(path_to_specfile);
            StreamTokenizer st = new StreamTokenizer(sp_file);

            // Make sure numbers, colons and periods are considered valid characters
            st.resetSyntax();
            st.wordChars(':', ':');
            st.wordChars('0', '9');
            st.wordChars('.', '.');
            st.wordChars('A', 'z');
            st.wordChars('%','%');
            st.wordChars('{','{');
            st.wordChars('}','}');

            int token = 0;
            String new_word;
outer: 
            while (token != StreamTokenizer.TT_EOF) {
                token = st.nextToken();

                switch (token) {
                case StreamTokenizer.TT_WORD:
                    new_word = st.sval;
                    
                    if (found_define) {
                        found_define = false;
                        define_info.add(define_ctr,new_word);
                        define_ctr++;
                        found_define_name = true;
                        break;
                    }
                    
                    if (found_define_name) {
                        found_define_name = false;
                        define_info.add(define_ctr,new_word);
                        define_ctr++;
                        break;
                    }
                    
                    if (found_version & !found_ver_token) {
                        found_ver_token = true;
                        if (new_word.startsWith("%")) { //$NON-NLS-1$
                            try {
                                rpm_info.set(0,parseDefine(new_word, define_info));
                            } catch (Exception e) {
                                String throw_message = Messages.getString("RPMCore.Error_using_parseDefine_to_get_the_version_no._41") + //$NON-NLS-1$
                                  Messages.getString("RPMCore._from_the_spec_file_at___42") + path_to_specfile; //$NON-NLS-1$
                                IStatus error = new Status(IStatus.ERROR, Messages.getString("RPMCore.Error_1"), 1, //$NON-NLS-1$
                                                    throw_message, null);
                                throw new CoreException(error);
                            }
                        } else {
                             rpm_info.set(0, new_word);
                        }

                        // System.out.println("Found version = " + new_word);
                        if (found_name_token & found_ver_token &
                                found_rel_token) {
                            break outer;
                        }

                        break;
                    }

                    if (found_release & !found_rel_token) {
                        found_rel_token = true;
                        if (new_word.startsWith("%")) {  //$NON-NLS-1$
                            try {
                                rpm_info.set(1,parseDefine(new_word, define_info));
                            } catch (Exception e) {
                            String throw_message = Messages.getString("RPMCore.Error_using_parseDefine_to_get_the_release_no._44") + //$NON-NLS-1$
                              Messages.getString("RPMCore._from_the_spec_file_at___45") + path_to_specfile; //$NON-NLS-1$
                            IStatus error = new Status(IStatus.ERROR, Messages.getString("RPMCore.Error_1"), 1, //$NON-NLS-1$
                                                throw_message, null);
                            throw new CoreException(error);
                        }
                            break;
                        } else {
                             rpm_info.set(1, new_word);
                          }

                        // System.out.println("Found release = " + new_word);
                        if (found_name_token & found_ver_token &
                                found_rel_token) {
                            break outer;
                        }

                        break;
                    }

                    if (found_name & !found_name_token) {
                        found_name_token = true;
                        rpm_info.set(2, new_word);

                        // System.out.println("Found name = " + new_word);
                        if (found_name_token & found_ver_token &
                                found_rel_token) {
                            break outer;
                        }

                        break;
                    }

                    // See if this is a "Version:" tag
                    if (new_word.equals("Version:")) { //$NON-NLS-1$
                        found_version = true;
                        break;
                    }

                    // See if this is a "Release:" tag
                    if (new_word.equals("Release:")) { //$NON-NLS-1$
                        found_release = true;
                        break;
                    }

                    // See if this is a "Name:" tag
                    if (new_word.equals("Name:")) { //$NON-NLS-1$
                        found_name = true;
                        break;
                    }
                    
                    // See if this a "%define" statement
                    // the version and release can sometimes be in a define stmt
                    if (new_word.equals("%define")) {  //$NON-NLS-1$
                        found_define = true;
                        break;
                    }

                default:
                    break;
                }
            }
        } catch (IOException e) {
            String throw_message = Messages.getString(
                    "RPMCore.Error_parsing_the_spec_file_at") + //$NON-NLS-1$
                path_to_specfile;
            IStatus error = new Status(IStatus.ERROR, Messages.getString("RPMCore.Error_1"), 1, //$NON-NLS-1$
                    throw_message, null);
            throw new CoreException(error);
        }

        /* Ugly: In rpm_info ArrayList:
         * [0] = Version
         * [1] = Release
         * [2] = Name
         */
        setVersion(rpm_info.get(0));
        setRelease(rpm_info.get(1));
        setName(rpm_info.get(2));
    }
    
    /**
      * Method parseDefine accepts a token from the parser and
      * searches the ArrayList passed to it for the value of the
      * token name.  This is crude at this point since this does not
      * happen very often.
      * @param token is a string containing the name found after the
      *               "Version:" or "Release:" fields of a spec file and the
      *               begining character is a "%"
      * @param token_value ia an ArrayList containing the names and
      *               values found in the "%define" statements usually found
      *              at the top of the spec file
      * @return a string with the correct version or release number
      *               else throw a CoreException
      */
    private String parseDefine(String token, ArrayList<String> token_value) 
        throws CoreException {
          // See if there in anything in the ArrayList
          if (token_value.isEmpty()) {
              String throw_message = Messages.getString("RPMCore.No___%defines___were_found_in_the_spec_file_38"); //$NON-NLS-1$
              IStatus error = new Status(IStatus.ERROR, Messages.getString("RPMCore.Error_1"), 1, //$NON-NLS-1$
                                  throw_message, null);
              throw new CoreException(error);
          }
          // A token usually looks this: %{name}
          String token_name = token.substring(2,token.length()-1);
          int i = token_value.indexOf(token_name);
          return token_value.get(i+1);
    }
    
    private int getUniquePatchId(ArrayList<String> patchlist, int patch_ctr) {
        int patch_array_size = patchlist.size();
        int patch_num;
        String last_patch = patchlist.get(patch_array_size - 1);
        int indx = 5;

        while (last_patch.charAt(indx) != ':') {
            indx++;
        }

        // Allow for the fact that there could only be one patch statement of the
        // form "Patch:", that is, there is no number
        if (indx == 5) {
            return 0;
        }

        String num = last_patch.substring(5, indx);

        try {
            patch_num = Integer.parseInt(num, 10);
        } catch (NumberFormatException e) {
            return -1;
        }

        return patch_num + 1;
    }
    public String getConfigureArgs() {
        return configureArgs;
    }
    public String getName() {
        return name;
    }
    public String getRelease() {
        return release;
    }
    public String getVersion() {
        return version;
    }
    private void setConfigureArgs(String configureArgs) {
        this.configureArgs = configureArgs;
    }
    private void setName(String name) {
        this.name = name;
    }
    private void setRelease(String release) {
        this.release = release;
    }
    private void setVersion(String version) {
        this.version = version;
    }

	public int getLastPatchLine() {
		return lastPatchLine;
	}

	public int getLastPatchMacroLine() {
		return lastPatchMacroLine;
	}

	public int getLastSourceLine() {
		return lastSourceLine;
	}

	public int getNumPatches() {
		return numPatches;
	}

	public int getSetupLine() {
		return setupLine;
	}
}
