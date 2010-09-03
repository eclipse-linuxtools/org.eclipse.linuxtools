/* xmlfmt - defines several operators and classes for formatting
   an XML stream (see oxmlstream.h)
   Written by Keith Seitz <keiths@redhat.com>
              Kent Sebastian <ksebasti@redhat.com>
   Copyright 2003,2009 Red Hat, Inc.

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
