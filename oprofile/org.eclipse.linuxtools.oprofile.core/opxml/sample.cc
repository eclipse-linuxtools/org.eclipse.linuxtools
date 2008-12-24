/* sample - A class which represents an Oprofile sample
   Written by Keith Seitz <keiths@redhat.com>
   Edited by Kent Sebastian     <ksebasti@redhat.com>
   Copyright 2004,2008 Red Hat, Inc.

   This program is open source software licensed under the Eclipse
   Public License ver. 1.

   Alternatively, this program may be used under the terms of the GNU
   General Public License as published by the Free Software Foundation;
   either version 2 of the License, or (at your option) any later
   version.  */

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
