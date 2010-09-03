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

#ifndef _XMLFMT_H
#define _XMLFMT_H
#include <ostream>

// Start a new tag with the given NAME
class startt
{
 public:
  startt (const std::string &name) : _name (name) {};
  friend std::ostream& operator<< (std::ostream& os, const startt& s);

 private:
  std::string _name;
};

// Add an attribute with the given NAME and VALUE to the current tag
class attrt
{
 public:
  attrt (const std::string& name, const std::string& value)
    : _name (name), _value (value) {};
  friend std::ostream& operator<< (std::ostream&, const attrt& a);

 private:
  std::string _name;
  std::string _value;

};

// End the current tag
std::ostream& endt (std::ostream& os);

// End the XML document and output it
std::ostream& endxml (std::ostream& os);

//function to ensure strings dont have invalid characters
void valid_string(std::string &s);
#endif // ! _XMLFMT_H
