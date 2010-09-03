/* sample - A class which represents an Oprofile sample
   Written by Keith Seitz <keiths@redhat.com>
   Edited by Kent Sebastian     <ksebasti@redhat.com>
   Copyright 2004,2008 Red Hat, Inc.

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

#include "sample.h"
#include "xmlfmt.h"
#include "symbol.h"

using namespace std;

// Constructor - pass in sample's address, any associated symbol, count,
sample::sample(bfd_vma addr, symbol* sym, unsigned int count)
  : _addr (addr), _symbol (sym), _count (count)
{
}

sample::sample(bfd_vma addr, const symbol* sym, unsigned int count, unsigned int line)
  : _addr (addr), _symbol (sym), _count (count), _line (line)
{
}

const char*
sample::get_name (void) const
{
  return (has_symbol () ? _symbol->name () : NULL);
}

const char*
sample::get_demangled_name (void) const
{
  return (has_symbol () ? _symbol->demangled_name () : NULL);
}

/*
 * <sample>
 *   <count>4312</count>
 *   <line>41</line>
 * </sample>
 */
ostream&
operator<< (ostream& os, const sample* s)
{
  os << startt ("sample")
     << startt ("count") << s->get_count () << endt
     << startt ("line") << s->get_line() << endt;

  return os << endt;
}
