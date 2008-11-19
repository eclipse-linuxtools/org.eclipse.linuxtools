/* oxmlstream.h - A convenience class for outputting XML.
   Written by Keith Seitz <keiths@redhat.com>
   Copyright 2003, Red Hat, Inc.

   This program is open source software licensed under the Eclipse
   Public License ver. 1.

   Alternatively, this program may be used under the terms of the GNU
   General Public License as published by the Free Software Foundation;
   either version 2 of the License, or (at your option) any later
   version.  */

#ifndef _OXMLSTREAM_H
#define _OXMLSTREAM_H
#include <ostream>
#include "xmlbuf.h"
#include "xmlfmt.h"

// An ostream which outputs in XML. See xmlfmt.h for XML operators.
class oxmlstream : public std::ostream
{
 public:
  // Constructor - pass ostream onto which XML should be output.
  oxmlstream (std::ostream& os) : std::ostream (new xmlbuf (os)) {}
  ~oxmlstream () { delete rdbuf (); }
};
#endif // ! _OXMLSTREAM_H
