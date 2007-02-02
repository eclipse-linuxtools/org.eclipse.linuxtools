/* xmltag/xmlattr - Classes which are nodes in the XML document tree
   constructed by oxmlstream, xmlbuf, xmlfmt.
   Written by Keith Seitz <keiths@redhat.com>
   Copyright 2003, Red Hat, Inc.

   This program is open source software licensed under the Eclipse
   Public License ver. 1.

   Alternatively, this program may be used under the terms of the GNU
   General Public License as published by the Free Software Foundation;
   either version 2 of the License, or (at your option) any later
   version.  */

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
