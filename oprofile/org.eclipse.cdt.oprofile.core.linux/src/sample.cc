/* sample - A class which represents an Oprofile sample
   Written by Keith Seitz <keiths@redhat.com>
   Copyright 2004 Red Hat, Inc.

   This program is open source software licensed under the Eclipse
   Public License ver. 1.

   Alternatively, this program may be used under the terms of the GNU
   General Public License as published by the Free Software Foundation;
   either version 2 of the License, or (at your option) any later
   version.  */

#include "sample.h"
#include "xmlfmt.h"

using namespace std;

// Constructor - pass in sample's address, any associated symbol, count,
// and 
sample::sample(bfd_vma addr, symbol* sym, unsigned int count)
  : _addr (addr), _symbol (sym), _count (count)
{
}

/*
 * <sample>
 *   <addr>08059cdc</addr>
 *   <count>1</count>
 *   [SYMBOL]
 * </sample>
 */
ostream&
operator<< (ostream& os, const sample* s)
{
  char buf[65];
  sprintf_vma (buf, s->get_vma ());

  os << startt ("sample")
     << startt ("addr") << buf << endt
     << startt ("count") << s->get_count () << endt;
  
  if (s->has_symbol ())
    os << s->get_symbol ();

  return os << endt;
}
