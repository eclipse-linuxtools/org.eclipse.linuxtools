/* Sample - A class which represents an Oprofile sample
   Written by Keith Seitz <keiths@redhat.com>
   Edited by Kent Sebastian     <ksebasti@redhat.com>
   Copyright 2003,2008 Red Hat, Inc.

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
