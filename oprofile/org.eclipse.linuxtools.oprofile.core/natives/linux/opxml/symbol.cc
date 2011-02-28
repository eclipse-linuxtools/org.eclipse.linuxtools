/* symbol - A class which represents symbols in executables
   Written by Keith Seitz <keiths@redhat.com>
   Edited by Kent Sebastian     <ksebasti@redhat.com>
   Copyright 2003,2008 Red Hat, Inc.

   This program is open source software licensed under the Eclipse
   Public License ver. 1.

   Alternatively, this program may be used under the terms of the GNU
   General Public License as published by the Free Software Foundation;
   either version 2 of the License, or (at your option) any later
   version.  */

#include <stdlib.h>
#include <stdio.h>

#include "symbol.h"
#include "xmlfmt.h"

// From libiberty
#ifndef DMGL_PARAMS
#define DMGL_PARAMS (1 << 0)	// Include function arguments
#endif

#ifndef DMGL_ANSI
#define DMGL_ANSI (1 << 1)	// Include const, volatile, etc
#endif

using namespace std;

extern "C" char* cplus_demangle (char const* mangled_name, int options);

const bfd_vma symbol::UNSET = (bfd_vma) -1;


symbol::symbol (asymbol* sym)
  : _end (UNSET), _asymbol (sym)
{
  _count = 0;
}

symbol::symbol (asymbol* sym, string src_filename)
  : _end (UNSET), _asymbol (sym), _src_filename(src_filename)
{
  _count = 0;
}

bool
symbol::contains (bfd_vma addr) const
{
  if (addr >= start() && addr < _end)
    return true;
  return false;
}

const char*
symbol::demangled_name (void) const
{
  char* demangled = cplus_demangle (name (), DMGL_PARAMS | DMGL_ANSI);
  if (demangled == NULL)
    return name ();

  return demangled;
}

ostream&
operator<< (ostream& os, const symbol* s)
{
  //convert symbol's total count to a string for attrt
  char buf[11];
  sprintf(buf,"%u",s->get_count());

  //output this symbol's info
  os << startt ("symbol")
      << attrt ("name", s->demangled_name ())
      << attrt ("file", s->get_srcfilename())
      << attrt ("count", buf);

  //ouput all samples under this symbol
  const std::set<sample*, sample_comp>* samples = s->get_sample_list();
  for (std::set<sample*, sample_comp>::iterator i = samples->begin(); i != samples->end(); ++i)
    os << (*i);

  return os << endt;
}
