/* profileimage - A class which represents a single image for
   which oprofile has samples (or for which some child dependency
   has samples).
   Written by Keith Seitz <keiths@redhat.com>
   Edited by Kent Sebastian     <ksebasti@redhat.com>
   Copyright 2004,2008 Red Hat, Inc.

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

#ifndef _PROFILEIMAGE_H
#define _PROFILEIMAGE_H

#include <list>
#include <string>
#include <set>

#include "samplefile.h"
#include "symbol.h"

#define VDSO_NAME_STRING "[vdso]"

class sample;
class imageheader;

class profileimage
{
 public:
  // Constructor - pass in the samplefile; CANNOT BE NULL.
  profileimage (samplefile* sfile);

  // Destructor
  ~profileimage ();

  // Returns the name of this image; it is the name of the actual binary
  // in which samples were collected.
  std::string get_name (void) const;

  // Returns the image header for this image
  const imageheader* get_header (void);

  // Add the given profileimage as a dependency of this image
  void add_dependency (profileimage* image);

  // Returns a list of all the dependencies of this image
  std::list<profileimage*>* get_dependencies (void) const { return _dependencies; };

  // Returns the Oprofile samplefile for this image
  samplefile* get_samplefile (void) const { return _samplefile; };

  // Returns the count of all the samples collected in this image, excluding dependencies
  long get_count (void) const;

 private:
  // The samplefile (non-NULL)
  samplefile* _samplefile;

  // List of dependencies
  std::list<profileimage*>* _dependencies;

  // Image header
  imageheader* _header;
};

struct depimage_comp {
  bool operator() (const profileimage* lhs, const profileimage* rhs)
    {
      if (lhs->get_count() == rhs->get_count())
        if (lhs->get_name() == rhs->get_name())
          return true;
        else
          return lhs->get_name() < rhs->get_name();
      else
        return lhs->get_count() > rhs->get_count();
    }
};

struct symbol_comp {
  bool operator() (const symbol* lhs, const symbol* rhs)
    {
      if (lhs->get_count() == rhs->get_count())
        {
          std::string ln(lhs->name()), rn(rhs->name());
          if (ln == rn)
            return true;
          else
            return ln < rn;
        }
      else
        return lhs->get_count() > rhs->get_count();
    }
};


std::ostream& operator<< (std::ostream& os, profileimage* image);
void add_sample(std::list<sample*> &samples, sample* new_sample);
long get_dependent_count(const std::list<profileimage*>* const deps);
std::set<profileimage*, depimage_comp>* sort_depimages(const std::list<profileimage*>* const deps);
std::set<symbol*, symbol_comp>* sort_symbols(const std::map<const asymbol*, symbol*>* const symbols);
std::string get_name(const profileimage* p);
#endif // !_PROFILEIMAGE_H
