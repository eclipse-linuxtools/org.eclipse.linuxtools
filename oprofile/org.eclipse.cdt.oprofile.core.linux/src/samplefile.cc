/* samplefile - A class which represents a samplefile. This class either
   represents a real disk file or a "fake" one (needed in cases where
   Oprofile only collected samples in a dependency, like a library).
   Written by Keith Seitz <keiths@redhat.com>
   Copyright 2004 Red Hat, Inc.

   This program is open source software licensed under the Eclipse
   Public License ver. 1.

   Alternatively, this program may be used under the terms of the GNU
   General Public License as published by the Free Software Foundation;
   either version 2 of the License, or (at your option) any later
   version.  */

#include "samplefile.h"
#include "sample.h"
#include "stable.h"
#include "xmlfmt.h"

using namespace std;

// From libutil++
extern bool create_file_list (list<string>& file_list, const string& base_dir,
			      const string& filter = "*", bool recursive = true);

samplefile::samplefile (string filename)
{
  _st = NULL;
  _db = new oprofile_db (filename);
  _filename = filename;
  _get_info_from_filename ();
}

// This seems like a giant waste of time, but when it comes down to processing many
// hundreds of samplefiles, we really need to be able to do this quickly.
string
samplefile::event_for_filename (string filename)
{
  string event;

  string::size_type pos = filename.find_last_of ('/');
  if (pos != string::npos)
    {
      string basename = filename.substr (pos + 1, string::npos);

      // Tokenize the basename between the '.'. This seems like a contradiction
      // to the speed mantra, but consider it a minimal sanity check.
      vector<string> parts = _tokenize (basename, '.');
      if (parts.size () == 6)
	event = parts[0];
    }

  return event;
}

void
samplefile::_get_info_from_filename (void)
{
  // First the easy stuff: event specifications
  // Filenames look like: EVENT.COUNT.UMASK.TGID.TID.CPU  
  string basename;
  string dir_name;
  string::size_type pos = _filename.find_last_of ('/');
  if (pos != string::npos)
    {
      dir_name = _filename.substr (0, pos);
      basename = _filename.substr (pos + 1, string::npos);

      // Tokenize the basename between the '.'
      vector<string> parts = _tokenize (basename, '.');
      if (parts.size () == 6)
	{
	  // Right number of specifications!
	  int i = 0;
	  _event = parts[i++];
	  _count = parts[i++];
	  _unit_mask = parts[i++];
	  _tgid = parts[i++];
	  _tid = parts[i++];
	  _cpu = parts[i++];
	}
      else
	return ;

      /* Now the hard part: the lib and image names */
      
      // Like the event spec, the easiest way to do this is to tokenize the pathname
      parts = _tokenize (dir_name, '/');

      // Strip off everything up to either "{root}" or "{kern}"
      vector<string>::size_type i = 0;
      
      /* Basically, we have
	 "/path/to/samples/<session>/{root} or {kern}"
	 + "/path/to/executable/{dep}" + "{root}" || "{kern}"
	 + "/path/to/library[/{cg}]
	 [+ "/path/to/callgraph"] */

      // First "token" to look for is "{root}" or "{kern}"
      for ( ; i < parts.size (); ++i)
	{
	  if (parts[i] == "{root}" || parts[i] == "{kern}")
	    break;
	}

      // Skip past "{root}" or "{kern}"
      ++i;

      // Next "token" is "{dep}". Everything else is image name
      for ( ; i < parts.size () && parts[i] != "{dep}"; ++i)
	_image += "/" + parts[i];
      
      // Skip past "{dep}"
      ++i;

      // "{dep}" must be followed by "{kern}" or "{root}"
      if (parts[i] != "{kern}" && parts[i] != "{root}")
	{
	  // Error. Filename truncated.
	  return;
	}

      // Skip past "{kern}" or "{root}"
      ++i;

      // Next "token" will be "{cg}" or string::npos
      for ( ; i < parts.size () && parts[i] != "{cg}"; ++i)
	_lib_image += "/" + parts[i];

      // Skip past "{cg}" (or end)
      ++i;

      // Last bits will be callgraph
      for ( ; i < parts.size (); ++i)
	_callgraph += "/" + parts[i];
    }
}

vector<string>
samplefile::_tokenize (const string& str, char delim)
{
  vector<string> tokens;
  string::size_type start, end;

  start = end = 0;
  while ((end = str.find (delim, start)) != string::npos)
    {
      if (start != end ) // ignore zero-length, i.e, str[0] == delim
	tokens.push_back (str.substr (start, end - start));

      // skip the delimiter character
      start = end + 1;
    }

  // add any trailing stuff
  if (start != str.length ())
    tokens.push_back (str.substr (start, string::npos));

  return tokens;
}

samplefile::~samplefile (void)
{
  if (_db != NULL)
    {
      delete _db;
      _db = NULL;
    }

  if (_st != NULL)
    {
      delete _st;
      _st = NULL;
    }
}

// DO NOT FREE THE RESULT. ~oprofile_db will do it.
const samplefile::samples_t
samplefile::get_samples (void)
{
  samplefile::samples_t samples;

  if (has_samplefile ())
    {
      if (_st == NULL)
	{
	  _st = new symboltable (get_name ().c_str ());
	  _st->read_symbols ();
	}

      samples = _db->get_samples (_st);
    }

  return samples;
}

bool
samplefile::get_debug_info (bfd_vma vma, const char*& func, const char*& file, unsigned int& line)
{
  return (_st == NULL ? false : _st->get_debug_info (vma, func, file, line));
}

void
samplefile::get_sample_file_list (list<string>& file_list, const string& base_dir)
{
  file_list.clear ();

  list<string> files;
  if (create_file_list (files, base_dir))
    {
      list<string>::iterator i;
      for (i = files.begin (); i != files.end (); ++i)
	{
	  // Only allow unique filenames into the final list.
	  // (This can happen because we can have multiple counters
	  // for any given sample file.)
	  if (find (file_list.begin (), file_list.end (), *i)
	      == file_list.end ())
	    file_list.push_back (*i);
	}
    }
}

// Output header & list of samples
/*
 * <samplefile>/var/lib/oprofile/samples/current/blah/blah/blah</samplefile>
 * SAMPLE (handled by class sample)
 */
ostream&
operator<< (ostream& os, samplefile* sf)
{
  // output the sfile's full pathname (used for fetching debug info)
  os << startt ("samplefile") << sf->get_sample_file_name () << endt;

  // output list of samples
  samplefile::samples_t samples = sf->get_samples ();
  samplefile::samples_t::iterator s;
  for (s = samples.begin (); s != samples.end (); ++s)
    {
      const sample* smpl = samplefile::SAMPLE (*s);
      os << smpl;
    }

  return os;
}
