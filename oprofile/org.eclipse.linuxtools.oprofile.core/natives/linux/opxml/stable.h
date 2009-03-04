/* symboltable - A symbol table class
   Written by Keith Seitz <keiths@redhat.com>
   Copyright 2004 Red Hat, Inc.

   This program is open source software licensed under the Eclipse
   Public License ver. 1.

   Alternatively, this program may be used under the terms of the GNU
   General Public License as published by the Free Software Foundation;
   either version 2 of the License, or (at your option) any later
   version.  */

#ifndef _STABLE_H
#define _STABLE_H
#include <bfd.h>
#include <vector>

class symbol;

class symboltable
{
 public:
  symboltable (const char* file);
  ~symboltable (void);

  // Lookup a Symbol for the given sample vma. Returns the symbol found (or NULL)
  // and gives the real address of the sample. Sadly, kernel images are treated
  // differently from userspace images.
  symbol* lookup_vma (bfd_vma sample_vma, bfd_vma &real_address, bool is_kernel);
  symbol* lookup_vma (bfd_vma real_vma);

  // Read in the samples
  bool read_symbols (void);

  inline asymbol** get_bfd_symbol_table (void) const { return _symbol_table; };

  /* Gets the debug info for a given address:
     function name in debug info
     source filename
     line number

     Returns true if debug info found. False otherwise.
     NOTE: could return true but still have source_file and line be
     invalid!*/
  bool get_debug_info (bfd_vma vma, const char*& function,
		       const char*& source_file, unsigned int& line);

#if 0
  // Debugging. Dump the symbol table
  void dump_table (void);
#endif

 protected:
  // Opens the BFD associated with the executable
  bool _open_bfd (void);

  // Closes the BFD
  void _close_bfd (void);

  // Helper function: is the given asymbol "interesting"? (i.e., should
  // it go into the symbol table?)
  static bool _interesting_symbol (asymbol* sym);

  // A list of known uninteresting symbols
  static char const* _boring_symbols[];

  // All of the executable's symbols
  std::vector<symbol*> _symbols;

  // The executable's filename
  char* _filename;

  // The BFD associated with this executable
  bfd* _abfd;

  // The BFD symbol table
  asymbol** _symbol_table;

  // The physical load address of this executable (NOT THE BFD SECTION
  // START ADDRESS)
  bfd_vma _start_vma;
  bfd_vma _text_offset;

  // A performance cache
  symbol* _cache_symbol;
};
#endif // !_STABLE_H
