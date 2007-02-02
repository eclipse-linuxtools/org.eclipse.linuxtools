/* profileimage - A class which represents a single image for
   which oprofile has samples (or for which some child dependency
   has samples).
   Written by Keith Seitz <keiths@redhat.com>
   Copyright 2004 Red Hat, Inc.

   This program is open source software licensed under the Eclipse
   Public License ver. 1.

   Alternatively, this program may be used under the terms of the GNU
   General Public License as published by the Free Software Foundation;
   either version 2 of the License, or (at your option) any later
   version.  */

#include "profileimage.h"

#include <iostream>
#include <iterator>

#include "imageheader.h"
#include "xmlfmt.h"

using namespace std;

profileimage::profileimage (samplefile* sfile)
  : _samplefile (sfile), _header (NULL)
{
  _dependencies = new list<profileimage*>;
}

profileimage::~profileimage ()
{
  delete _samplefile;
  delete _dependencies;
  if (_header != NULL)
    delete _header;
}

// returns parsed_filename.image if top-level image
// or parsed_filename.lib_image if dependency
string
profileimage::get_name (void) const
{
  if (!_samplefile->has_samplefile ())
    {
      // We have no sample file for this object -- look for
      // the image name in the first dependency
      list<profileimage*>::iterator i = _dependencies->begin ();
      if (i != _dependencies->end ())
	return (*i)->get_samplefile ()->get_image ();
      else
	{
	  // Can this happen? I don't think so, but...
	  cerr << "WARNING: empty profileimage at " << __FILE__
	       << ":" << __LINE__ << endl;
	  return "";
	}
    }

  return _samplefile->get_name ();
}

long
profileimage::get_count (void) const
{
  return _samplefile->get_sample_count ();
}

void
profileimage::add_dependency (profileimage* image)
{
  _dependencies->push_back (image);
}

const imageheader*
profileimage::get_header (void)
{
  if (_header == NULL)
    {
      samplefile* sfile;
      if (_samplefile->has_samplefile ())
	sfile =_samplefile;
      else
	{
	  // No samplefile -- use first dependency
	  list<profileimage*>::iterator i = _dependencies->begin ();
	  if (i != _dependencies->end ())
	    sfile = (*i)->get_samplefile ();
	  else
	    {
	      // Can this happen? I don't think so, but...
	      cerr << "WARNING: empty profileimage at " << __FILE__
		   << ":" << __LINE__ << endl;
	      return NULL;
	    }
	}

      _header = new imageheader (sfile);
    }

  return _header;
}

/*
 * <image name="/bin/bash">
 *   HEADER INFO
 *   SAMPLE_0
 *   SAMPLE_1
 *   ..
 *   SAMPLE_N
 *   SUB-IMAGE_0
 *   SUB-IMAGE_1
 *   ...
 *   SUB-IMAGE_N
 * </image>
 */
/*
  So this operator<< should output:
  o header
  o loop through outputting samples
  o loop through outputting dependent images
 */
ostream&
operator<< (ostream& os, profileimage* image)
{
  os << startt ("image") << attrt ("name", image->get_name ());

  if (image->get_samplefile ()->has_samplefile ())
    {
      os << image->get_header ()
	 << image->get_samplefile ();
    }
  else
    {
      // If this image does not have a samplefile (i.e., it is a "fake"
      // one generated because it has no samples but has dependencies which do)
      // then output the header for a dependency with no samples.
      list<profileimage*>* deps = image->get_dependencies ();
      os << deps->front ()->get_header ();
    }

  // Output dependent images
  list<profileimage*>* deps = image->get_dependencies ();
  copy (deps->begin (), deps->end (), ostream_iterator<profileimage*> (os, ""));
  return os << endt;
}
