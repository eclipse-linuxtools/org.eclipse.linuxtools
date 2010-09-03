/* sevent (sessionevent) - a class which represents an event collected
   within an oprofile session. There will be one sessionevent for every
   event collected within a session. No two sessionevents with the same
   session name may have the same event name.
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

#include "sevent.h"

#include "session.h"
#include "xmlfmt.h"

using namespace std;

sessionevent::sessionevent (const session* session, string event)
  : _session (session), _event_name (event)
{
  _files = new profileimages_t;
}

sessionevent::~sessionevent ()
{
  profileimages_t::iterator it;
  for (it = _files->begin (); it != _files->end (); ++it)
    delete (*it);
  delete _files;
}

void
sessionevent::add_sample_file (samplefile* sfile)
{
  if (!sfile->is_dependency ())
    {
      // top-level image
      _files->push_back (new profileimage (sfile));

    }
  else
    {
      // find file -- add dependent
      bool found = false;
      profileimages_t::iterator it;
      for (it = _files->begin (); it != _files->end (); ++it)
	{
	  if ((*it)->get_name () == sfile->get_image ())
	    {
	      (*it)->add_dependency (new profileimage (sfile));
	      found = true;
	    }
	}

      if (!found)
	{
	  // This does happen!! We got no samples in a profileimage,
	  // but we DID get samples in a dependency. Create a new
	  // "fake" profileimage and add this as a dependency.
	  profileimage* img = new profileimage (new samplefile (""));
	  img->add_dependency (new profileimage (sfile));
	  _files->push_back (img);
	}
    }
}

long
sessionevent::get_count (void) const
{
  // Get list of images
  profileimages_t::iterator it;
  long count = 0;
  for (it = _files->begin (); it != _files->end (); ++it)
    {
      // Get count of the main image
      count += (*it)->get_count ();

      // Add count for dependencies
      list<profileimage*>* deps = (*it)->get_dependencies ();
      list<profileimage*>::iterator dit;
      for (dit = deps->begin (); dit != deps->end (); ++dit)
	count += (*dit)->get_count ();
    }

  return count;
}

/*
 * <session name="foo">
 *    <count>1234</count>
 * </session>
 *
 */
ostream&
operator<< (ostream& os, const sessionevent* se)
{
  return os << startt ("session") << attrt ("name", se->get_session ()->get_name ())
//	    << startt ("count") << se->get_count () << endt
	    << endt;
}
