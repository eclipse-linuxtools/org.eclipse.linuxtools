/* imageheader - a class which represents the "header" info for a given
   image.
   Written by Keith Seitz <keiths@redhat.com>
   Copyright 2004 Red Hat, Inc.

   This program is open source software licensed under the Eclipse
   Public License ver. 1.

   Alternatively, this program may be used under the terms of the GNU
   General Public License as published by the Free Software Foundation;
   either version 2 of the License, or (at your option) any later
   version.  */

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
