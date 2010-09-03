/* xmltag/xmlattr - Classes which are nodes in the XML document tree
   constructed by oxmlstream, xmlbuf, xmlfmt.
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

#include "xmltag.h"

using namespace std;

bool xmltag::_header_output = false;

xmltag*
xmltag::dtd_header (string version, string encoding)
{
  string txt ("<?xml version=\"" + version + "\" encoding=\"" + encoding + "\"?>");
  xmltag* header = new xmltag ("");
  header->add_text (txt);
  return header;
}

xmltag::xmltag (const string& name)
  : _name (name), _level (-1)
{
  _children = new _childlist_t;
}

xmltag::xmltag (const string& name, int level)
  : _name (name), _level (level)
{
  _children = new _childlist_t;
}

xmltag::~xmltag ()
{
  // delete all children
  delete_children ();
  delete _children;
}

void
xmltag::delete_children (void)
{
  _childlist_t::iterator i;
  for (i = _children->begin (); i != _children->end (); ++i)
    delete (*i);

  _children->clear ();
}

xmltag*
xmltag::add_child (const string& name)
{
  xmltag* child = new xmltag (name, _level + 1);
  _children->push_back (child);
  return child;
}

void
xmltag::add_text (const string& txt)
{
  _text += txt;
}

void
xmltag::add_char (const char c)
{
  _text += c;
}

void
xmltag::add_attr (const string& name, const string& value)
{
  _attributes.add (name, value);
}

// Tags output a start tag with attributes, then children, then a closing tag.
void
xmltag::output (ostream& os)
{
  bool is_hdr = (_level == -1);
  bool no_txt = (_text.length () == 0);

  if (is_hdr)
    {
      if (!xmltag::_header_output)
	{
	  os << _text << endl;
	  xmltag::_header_output = true;
	}
    }
  else
    {
      os << _indent () << "<" << _name;
      _attributes.output (os);
      os << ">" << _text;
      if (no_txt)
	os << endl;
    }

  // output children
  _output_children (os);

  if (!is_hdr)
    {
      if (no_txt)
	os << _indent ();
      os << "</" << _name << ">" << endl;
    }
}

// Walk through children, having them output themselves
void
xmltag::_output_children (ostream& os)
{
  _childlist_t::iterator i;
  for (i = _children->begin (); i != _children->end (); ++i)
    {
      xmltag* child = (*i);
      child->output (os);
    }
}

string
xmltag::_indent (void)
{
  return (_level > 0 ? std::string (_level, '\t') : "");
}

xmlattr::~xmlattr ()
{
  list<attr_t*>::iterator i;
  for (i = _attributes.begin (); i != _attributes.end (); ++i)
    delete (*i);
}

void
xmlattr::add (const string& name, const string& value)
{
  _attributes.push_back (new attr_t (name, value));
}

// Attributes output 'name="value"' for each attribute
void
xmlattr::output (ostream& os)
{
  list<attr_t*>::iterator i;
  for (i = _attributes.begin (); i != _attributes.end (); ++i)
    os << " " << attr_name (*i) << "=\"" << attr_value (*i) << "\"";
}
