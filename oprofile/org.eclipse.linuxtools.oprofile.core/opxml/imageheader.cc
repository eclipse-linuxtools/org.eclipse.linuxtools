/* imageheader - a class which represents the "header" information for a given
   image.
   Written by Keith Seitz <keiths@redhat.com>
   Copyright 2004 Red Hat, Inc.

   This program is open source software licensed under the Eclipse
   Public License ver. 1.

   Alternatively, this program may be used under the terms of the GNU
   General Public License as published by the Free Software Foundation;
   either version 2 of the License, or (at your option) any later
   version.  */

#include "imageheader.h"
#include "xmlfmt.h"

using namespace std;

imageheader::imageheader (const samplefile* sfile)
  : _sfile (sfile)
{
}

ostream&
operator<< (ostream& os, const imageheader* ihdr)
{
  return os << startt ("header")
	    << startt ("cpu_type") << ihdr->get_cpu () << endt
	    << startt ("count") << ihdr->get_count () << endt
            << startt ("event") << ihdr->get_event () << endt
	    << startt ("unit-mask") << ihdr->get_unit_mask () << endt
	    << startt ("cpu-speed") << ihdr->get_cpu_speed () << endt
	    << endt;
}

