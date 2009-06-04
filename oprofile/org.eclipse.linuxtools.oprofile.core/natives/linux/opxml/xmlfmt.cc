/* xmlfmt - defines several operators and classes for formatting
   an XML stream (see oxmlstream.h)
   Written by Keith Seitz <keiths@redhat.com>
              Kent Sebastian <ksebasti@redhat.com>
   Copyright 2003,2009 Red Hat, Inc.

   This program is open source software licensed under the Eclipse
   Public License ver. 1.

   Alternatively, this program may be used under the terms of the GNU
   General Public License as published by the Free Software Foundation;
   either version 2 of the License, or (at your option) any later
   version.  */

#include "xmlfmt.h"
#include "xmlbuf.h"

using namespace std;

ostream&
operator<< (ostream& os, const startt& s)
{
  xmlbuf* xbuf = dynamic_cast<xmlbuf*> (os.rdbuf ());
  if (xbuf != NULL)
    xbuf->add_tag (s._name);
  return os;
}

ostream&
operator<< (ostream& os, const attrt& a)
{
  xmlbuf* xbuf = dynamic_cast<xmlbuf*> (os.rdbuf ());
  if (xbuf != NULL) {
    valid_string(const_cast<string&>(a._value));
    xbuf->add_attr (a._name, a._value);
  }
  return os;
}

ostream&
endt (ostream& os)
{
  xmlbuf* xbuf = dynamic_cast<xmlbuf*> (os.rdbuf ());
  if (xbuf != NULL)
    xbuf->end_tag ();
  return os;
}

ostream&
endxml (ostream& os)
{
  xmlbuf *xbuf = dynamic_cast<xmlbuf*> (os.rdbuf ());
  if (xbuf != NULL)
    xbuf->dump ();
  return os;
}

//cant have characters "'&<> in an attribute, will cause xml parsing exceptions
void
valid_string (string &s) {
  string chars = "&\"'<>";
  string char_replacements[] = {"&amp;", "&quot;", "&apos;", "&lt;", "&gt;"};
  char search_char;

  for (int char_index = 0; char_index < 5; char_index++) {
    search_char = chars[char_index];

    string::size_type search_index = 0;
    while(1) {
      search_index = s.find(search_char, search_index);

      if (search_index == string::npos)
        break;

      s.replace(search_index, 1, char_replacements[char_index]);
      search_index += char_replacements[char_index].length();
    }
  }
}
