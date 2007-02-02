/* Sample - A class which represents an Oprofile sample
   Written by Keith Seitz <keiths@redhat.com>
   Copyright 2003, Red Hat, Inc.

   This program is open source software licensed under the Eclipse
   Public License ver. 1.

   Alternatively, this program may be used under the terms of the GNU
   General Public License as published by the Free Software Foundation;
   either version 2 of the License, or (at your option) any later
   version.  */

#ifndef _SAMPLE_H
#define _SAMPLE_H
#include <stdlib.h>
#include <bfd.h>
#include <ostream>

#include "symbol.h"

class sample
{
 public:
  sample (bfd_vma a, symbol* sym, unsigned int cnt);

  // Get the name of the symbol corresponding to this sample
  // Returns NULL if no symbol.
  inline const char* get_name (void) const
    { return (has_symbol () ? _symbol->name () : NULL); };

  // Returns the demangled name for this sample
  inline const char* get_demangled_name (void) const
    { return (has_symbol () ? _symbol->demangled_name () : NULL); };

  // Returns the total sample count for this sample
  inline unsigned int get_count(void) const { return _count; };

  // Increments the total sample count for this sample
  inline void incr_count (int n) { _count += n; };

  // Does this sample have a symbol?
  inline bool has_symbol (void) const { return _symbol != NULL; };

  // Returns the symbol for this sample (could be NULL)
  inline const symbol* get_symbol (void) const { return _symbol; };

  // Gets the real vma for this sample
  inline bfd_vma get_vma (void) const { return _addr; };

 private:
  // (real) Address of sample
  bfd_vma _addr;

  // Symbol for sample (according to minimal symbols)
  symbol* _symbol;

  // Number of times sample appears in output
  unsigned int _count;
};
#endif // !_SAMPLE_H

// Operator to output samples
std::ostream& operator<< (std::ostream& os, const sample* s);
