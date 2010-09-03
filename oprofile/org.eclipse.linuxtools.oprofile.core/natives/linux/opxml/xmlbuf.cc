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

#include <stdio.h>

#include "xmlbuf.h"
#include "xmltag.h"

#define XML_ENCODING "UTF-8"
#define XML_VERSION  "1.0"

using namespace std;

xmlbuf::xmlbuf (ostream& outstream)
  : _os (outstream)
{
  _top = xmltag::dtd_header (XML_VERSION, XML_ENCODING);
  _current = _top;
  _tag_stack = new stack<xmltag*> ();
}

xmlbuf::~xmlbuf ()
{
  // delete tree
  delete _top;

  // delete tag stack and any items left on it
  while (!_tag_stack->empty ())
    {
      xmltag* t = _tag_stack->top ();
      _tag_stack->pop ();
      delete t;
    }

  delete _tag_stack;
}

// Only tags get text -- escape reserved characters
int
xmlbuf::overflow (int ch)
{
  switch (ch)
    {
    case EOF:
      /* nothing */                    break;
    case '&':
      _current->add_text ("&amp;");    break;
    case '<':
      _current->add_text ("&lt;");     break;
    case '>':
      _current->add_text ("&gt;");     break;
    case '\'':
      _current->add_text ("&apos;");   break;
    case '\"':
      _current->add_text ("&quot;");   break;
    default:
      _current->add_char (ch);         break;
    }

  return ch;
}

void
xmlbuf::add_tag (const string& tag_name)
{
  xmltag* tag = _current->add_child (tag_name);
  _tag_stack->push (_current);
  _current = tag;
}

void
xmlbuf::end_tag (void)
{
  _current = _tag_stack->top ();
  _tag_stack->pop ();
}

void
xmlbuf::add_attr (const string& name, const string& value)
{
  _current->add_attr (name, value);
}

void
xmlbuf::dump (void)
{
  // dump output to stream
  _top->output (_os);

  // reset top of tree
  _top->delete_children ();
}
