/* imageheader - a class which represents the "header" info for a given
   image.
   Written by Keith Seitz <keiths@redhat.com>
   Copyright 2004 Red Hat, Inc.

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

#ifndef _IMAGEHEADER_H
#define _IMAGEHEADER_H
#include <ostream>
#include <string>

#include "samplefile.h"

class imageheader
{
 public:
  // Constructor - pass in the oprofile header
  imageheader (const samplefile* sfile);

  // Returns the cpu type
  inline std::string get_cpu (void) const { return _sfile->get_cpu (); };

  // Returns the event collected
  inline std::string get_event (void) const { return _sfile->get_event (); };

  // Returns the count
  inline std::string get_count (void) const { return _sfile->get_count (); };

  // Returns the unit mask used during collection
  inline std::string get_unit_mask (void) const { return _sfile->get_unit_mask (); };

  // Returns an approx cpu speed
  // FIXME: SUCK?
  inline double get_cpu_speed (void) const { return 0.00; };

 private:
  const samplefile* _sfile;
};

std::ostream& operator<< (std::ostream& os, const imageheader* ihdr);
#endif // !_SFILEHEADER_H
