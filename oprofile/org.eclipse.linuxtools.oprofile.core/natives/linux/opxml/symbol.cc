/* symbol - A class which represents symbols in executables
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
