/* xmlbuf - A class for XML output on an ostream.
   Written by Keith Seitz <keiths@redhat.com>
   Copyright 2003, Red Hat, Inc.

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

#ifndef _XMLBUF_H
#define _XMLBUF_H
#include <streambuf>
#include <ostream>
#include <string>
#include <stack>

class xmltag;

class xmlbuf : public std::streambuf
{
 public:
  // Constructor - pass in the ostream to which to dump the whole
  // XML tree.
  xmlbuf (std::ostream& outstream);
  ~xmlbuf ();

  // Adds an XML tag of the given name
  // Invoked via "addt" operator (defined in xmlfmt.h)
  void add_tag (const std::string& tag_name);

  // Ends the current tag
  // Invoked via "endt" operator (defined in xmlfmt.h)
  void end_tag (void);

  // Adds the given attribute NAME with VALUE to the current tag
  // Invoked via "attrt" operator (defined in xmlfmt.h)
  void add_attr (const std::string& name, const std::string& value);

  // Dumps the whole tree to the (real) output stream
  // Invoked via "endxml" operator (defined in xmlfmt.h)
  void dump (void);

 protected:
  int overflow (int ch);

 private:
  // The ostream to dump the tree
  std::ostream& _os;

  // The top of the XML document tree
  xmltag* _top;

  // The current node in the tree being constructed
  xmltag* _current;

  // A stack of tags being constructed
  std::stack<xmltag*>* _tag_stack;
};
#endif // !_XMLBUF_H
