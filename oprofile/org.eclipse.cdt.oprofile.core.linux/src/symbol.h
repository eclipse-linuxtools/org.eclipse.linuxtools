/* symbol - A class which represents symbols in executables
   Written by Keith Seitz <keiths@redhat.com>
   Copyright 2003, Red Hat, Inc.

   This program is open source software licensed under the Eclipse
   Public License ver. 1.

   Alternatively, this program may be used under the terms of the GNU
   General Public License as published by the Free Software Foundation;
   either version 2 of the License, or (at your option) any later
   version.  */

#ifndef _SYMBOL_H
#define _SYMBOL_H

#include <ostream>
#include <bfd.h>

class symbol
{
 public:
  symbol (asymbol* sym);

  // Get the start address of this symbol
  inline bfd_vma start (void) const { return bfd_asymbol_value (_asymbol); };

  // Get the end address of this symbol
  inline bfd_vma end (void) const { return _end; };

  // Set the end address of this symbol
  inline void end (bfd_vma addr) { _end = addr; };

  // Get BFD's symbol info
  inline asymbol* get_asymbol (void) const { return _asymbol; };

  // Get BFD's section info
  inline asection* section (void) const { return bfd_get_section (_asymbol); };

  // Get the name of this symbol
  inline const char* name (void) const { return bfd_asymbol_name (_asymbol); };

  // Get the demangled name of this symbol (could be the same as name())
  const char* demangled_name (void) const;

  // Get the BFD flags for this symbol (i.e., BSF_FUNCTION, BSF_GLOBAL)
  inline flagword flags (void) const { return _asymbol->flags; };

  // Does this symbol contain the address ADDR?
  bool contains (bfd_vma addr) const;

  // Constant that all unset address are set to
  static const bfd_vma UNSET;

 protected:
  // The end address of this symbol (can only be set once all symbols read)
  bfd_vma _end;

  // The BFD symbol
  asymbol* _asymbol;
};

// ostream inserter for this class
std::ostream& operator<< (std::ostream& os, const symbol* s);
#endif // !_SYMBOL_H
