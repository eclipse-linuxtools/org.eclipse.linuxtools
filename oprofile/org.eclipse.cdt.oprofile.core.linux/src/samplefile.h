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

#ifndef _SAMPLEFILE_H
#define _SAMPLEFILE_H
#include <string>
#include <list>
#include <vector>

#include "oprofiledb.h"

class symboltable;

class samplefile
{
 public:
  // The type of a list of samples. STL container with iterators.
  typedef oprofile_db::samples_t samples_t;
  
  // The type of a sample. Also an STL container. Use SAMPLE to get at
  // actual sample.
  typedef oprofile_db::sample_t sample_t;

  // The type of a list of samplefiles
  typedef std::list<samplefile*> samplefilelist_t;

  // Convenience function to return the sample associated with
  // a sample_t
  static inline sample* SAMPLE (sample_t sample)
    { return oprofile_db::SAMPLE (sample); };

  // Constructor -- pass in the filename (may be "" when there
  // were no samples collected for the profileimage, i.e., "fake").
  samplefile (std::string filename);

  // Destructor
  ~samplefile (void);

  // Does this sample have a samplefile? This happens when Oprofile has
  // collected samples for an image, but all those samples were collected
  // in libraries and other dependencies.
  bool has_samplefile (void) const
    { return _filename != ""; };

  // Is this samplefile a dependency?
  bool is_dependency (void) const
    { return (!has_samplefile () || (_image != _lib_image)); }

  // Get count of all samples in this file
  long get_sample_count (void)
    { return (has_samplefile () ? _db->get_count () : 0); };

  // Returns the filename of this samplefile (or "" if it is "fake")
  std::string get_sample_file_name (void) const
    { return _filename; };

  // Returns the image name
  std::string get_image (void) const
    { return _image; };

  // Returns the library image name
  std::string get_lib_image (void) const
    {return _lib_image; };

  // Returns the logical name of the image in this samplefile, i.e.,
  // the lib_image if this is a dependency or image_name if not
  std::string get_name (void) const
    { return (is_dependency () ? get_lib_image () : get_image ()); };

  // Returns the event name that was collected in this samplefile
  std::string get_event (void) const
    { return _event; };

  std::string get_count (void) const
    { return _count; };

  std::string get_unit_mask (void) const
    { return _unit_mask; };

  std::string get_tgid (void) const
    { return _tgid; };
 
  std::string get_tid (void) const
    { return _tid; };

  std::string get_cpu (void) const
    { return _cpu; };

  std::string get_callgraph (void) const
    { return _callgraph; };

  // Returns a list of all the samples in this samplefile. Do NOT free the result!
  const samples_t get_samples (void);

  // Returns the debug info for the given VMA.
  bool get_debug_info (bfd_vma vma, const char*& func, const char*& file, unsigned int& line);

  // Get list of files from base_dir
  static void get_sample_file_list (std::list<std::string>& file_list, 
				    const std::string& base_dir);

  // This may seem like a bad practice, but this is done for speed reasons
  static std::string event_for_filename (std::string filename);

 private:
  static std::vector<std::string> _tokenize (const std::string& str, char delim);
  void _get_info_from_filename (void);

  // The oprofile_db associated with this samplefile
  oprofile_db* _db;

  // The symbol table opened for the executable represented by this samplefile
  symboltable* _st;

  // Information about the collection configuration
  std::string _filename;  // Disk filename of samplefile
  std::string _image;     // Name of the image recorded
  std::string _lib_image; // Name of the library (== _image if not a sub-image)
  std::string _event;
  std::string _count;
  std::string _unit_mask;
  std::string _tgid;
  std::string _tid;
  std::string _cpu;
  std::string _callgraph;
};

std::ostream& operator<< (std::ostream& os, samplefile* sf);
#endif // !_SAMPLEFILE_H
