/* xmlbuf - A class for XML output on an ostream.
   Written by Keith Seitz <keiths@redhat.com>
   Copyright 2003, Red Hat, Inc.

   This program is open source software licensed under the Eclipse
   Public License ver. 1.

   Alternatively, this program may be used under the terms of the GNU
   General Public License as published by the Free Software Foundation;
   either version 2 of the License, or (at your option) any later
   version.  */

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
