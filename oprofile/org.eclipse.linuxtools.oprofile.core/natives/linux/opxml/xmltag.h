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

#include <utility>
#include <string>
#include <list>
#include <ostream>

// An XML tag attribute handler class
class xmlattr
{
 public:
  ~xmlattr();

  // The type of all attributes
  typedef std::pair<std::string, std::string> attr_t;

  // Convenience accessors
  static inline const std::string& attr_name (const attr_t* a) { return a->first; };
  static inline const std::string& attr_value (const attr_t* a) { return a->second; };

  // Add an attribute of the given NAME and VALUE
  void add (const std::string& name, const std::string& value);

  // Output all attributes to the given ostream.
  void output (std::ostream& os);

 private:
  // A list of all attributes
  std::list<attr_t*> _attributes;
};

// An XML tag
class xmltag
{
 public:
  // Convenience function to construct a "tag" which contains
  // document type definition
  static xmltag* dtd_header (std::string version, std::string encoding);

  // Constructors - give NAME of tag and (optionally) an indent level
  xmltag (const std::string& name);
  xmltag (const std::string& name, int level);
  ~xmltag ();

  // Add a under this tag of the given NAME and return a pointer to it
  xmltag* add_child (const std::string& name);

  // Delete all children
  void delete_children (void);

  // Add an attribute of the given NAME and VALUE to this tag
  void add_attr (const std::string& name, const std::string& value);

  // Add the character C to the text contained in this tag
  void add_char (const char c);
  
  // Add the given TXT to the text contained in this tag
  void add_text (const std::string& txt);

  // Output the tag and it's text to the given stream
  void output (std::ostream& os);

 private:
  // convenience type of the list of child tags
  typedef std::list<xmltag*> _childlist_t;

  // A function to return indentation
  std::string _indent (void);

  // Convenience function to output the children of this tag to the given stream
  void _output_children (std::ostream& os);

  // Have we output the DTD?
  static bool _header_output;

  // The name of this tag
  std::string _name;

  // The text of this tag
  std::string _text;

  // The attributes associated with this tag
  xmlattr _attributes;

  // The "children" of this tag (i.e., tags this tag encloses)
  _childlist_t* _children;

  // The indent level for this tag
  int _level;
};
