/* Sample - A class which represents an Oprofile sample
   Written by Keith Seitz <keiths@redhat.com>
   Edited by Kent Sebastian     <ksebasti@redhat.com>
   Copyright 2003,2008 Red Hat, Inc.

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
#include <set>


class symbol;


class sample
{
 public:
   sample (bfd_vma a, symbol* sym, unsigned int cnt);
   sample (bfd_vma a, const symbol* sym, unsigned int cnt, unsigned int line);

  // Get the name of the symbol corresponding to this sample
  // Returns NULL if no symbol.
  const char* get_name (void) const;

  // Returns the demangled name for this sample
  const char* get_demangled_name (void) const;

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

  inline unsigned int get_line() const { return _line; };

 private:
  // (real) Address of sample
  bfd_vma _addr;

  // Symbol for sample (according to minimal symbols)
  const symbol* _symbol;

  // Number of times sample appears in output
  unsigned int _count;

  //line number -- set in profileimage
  unsigned int _line;

};


struct sample_comp {
  bool operator() (const sample* lhs, const sample* rhs) { return (lhs->get_count() == rhs->get_count() ? lhs->get_line() < rhs->get_line() : lhs->get_count() > rhs->get_count() ); }
};


// Operator to output samples
std::ostream& operator<< (std::ostream& os, const sample* s);
#endif // !_SAMPLE_H
