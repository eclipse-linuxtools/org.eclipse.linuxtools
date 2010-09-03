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

#ifndef _SEVENT_H
#define _SEVENT_H
#include <string>
#include <list>

#include "profileimage.h"

class samplefile;
class session;

class sessionevent
{
 public:
  // Constructor -- pass in session and event name
  sessionevent (const session* session, std::string event);

  // Desctructor
  ~sessionevent ();

  // Returns the event name
  const std::string get_name (void) const { return _event_name; };

  // Returns the session
  const session* get_session (void) const { return _session; };

  // Adds the samplefile to this sessionevent
  void add_sample_file (samplefile* sfile);

  // Returns the count of all samples in this sessionevent
  long get_count (void) const;

  // Returns a list of the images in the sessionevent
  typedef std::list<profileimage*> profileimages_t;
  profileimages_t* get_images (void) const { return _files; };

 private:
  // The session
  const session* _session;

  // The name of the event
  const std::string _event_name;

  // A list of images in this session
  profileimages_t* _files;
};

std::ostream& operator<< (std::ostream& os, const sessionevent* se);
#endif // _SEVENT_H
