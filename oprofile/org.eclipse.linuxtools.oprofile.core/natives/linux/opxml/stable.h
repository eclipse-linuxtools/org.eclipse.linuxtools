/* symboltable - A symbol table class
   Written by Keith Seitz <keiths@redhat.com>
   Copyright 2004 Red Hat, Inc.

   Redistribution and use in source and binary forms, with or without
   modification, are permitted provided that the following conditions
   are met:

    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.

    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the following
      disclaimer in the documentation and/or other materials provided
      with the distribution.

    * Neither the name of Red Hat, Inc. nor the names of its
      contributors may be used to endorse or promote products derived
      from this software without specific prior written permission.

   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
   "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
   LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
   FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
   COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
   INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
   (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
   SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
   HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
   STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
   ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
   OF THE POSSIBILITY OF SUCH DAMAGE.  */

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
