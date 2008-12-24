/* oprofile_db - An Oprofile sample file database wrapper.
   Written by Keith Seitz <keiths@redhat.com>
   Copyright 2004 Red Hat, Inc.

   This program is open source software licensed under the Eclipse
   Public License ver. 1.

   Alternatively, this program may be used under the terms of the GNU
   General Public License as published by the Free Software Foundation;
   either version 2 of the License, or (at your option) any later
   version.  */

#ifndef _OPROFILEDB_H
#define _OPROFILEDB_H

#include <map>
#include <string>
#include <bfd.h>
#include <odb.h>

class sample;
class symboltable;

// A class which represents an oprofile sample database. This is much
// lower-level stuff than class samplefile.
class oprofile_db
{
 public:
  typedef void (*callback_t)(odb_key_t, odb_value_t, void*);

  // Creates an oprofile_db from the given sample file
  oprofile_db (std::string sample_file);
  ~oprofile_db ();

  // Function object used to compare VMA for sorting
  struct ltvma
  {
    bool operator() (const bfd_vma a, const bfd_vma b) const
    { return (a < b); }
  };

  // The type of the sample database returned be get_samples
  typedef std::map<const bfd_vma, sample*, ltvma> samples_t;

  // The type of one sample in the database
  typedef std::pair<const bfd_vma, sample*> sample_t;

  // Macro to fetch the sample from the sample_t.
  static inline sample* SAMPLE (sample_t sample) { return sample.second; }

  // Retrieves the sample database using STABLE as a symbol table (may be NULL)
  const samples_t& get_samples (symboltable* stable);

  // Set/query whether the db has any samples in it 
  bool has_samples (void);
  void has_samples (bool yesno) { _has_samples = yesno; };

  // Get the total number of samples in this samplefile
  long get_count (void);

  // Walks the samples with the given callback
  void walk_samples (callback_t callback, void* data);

 protected:
  // Callbacks for walking oprofile sample database
  static void _get_samples_callback (odb_key_t key, odb_value_t info, void* data);
  static void _has_samples_callback (odb_key_t key, odb_value_t info, void* data);
  static void _get_count_callback (odb_key_t key, odb_value_t info, void* data);

  // Makes sure the oprofile sample file is open
  void _open_db (void);

  // Closes the oprofile sample file
  void _close_db (void);

  // The sample file
  std::string _filename;

  // The oprofile sample database for the file
  odb_t* _tree;

  // A map of all the samples
  samples_t _samples;

  // The symbol table used to resolve VMA into symbols
  symboltable* _symbol_table;

  bool _has_samples;
  callback_t _callback;
  bool _is_kernel;
};
#endif // !_OPROFILEDB_H
