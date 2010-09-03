/* session - a class which represents an oprofile session.
   All sessions occur as directories of the samples directory.
   Written by Keith Seitz <keiths@redhat.com>
   Copyright 2003, 2004 Red Hat, Inc.

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

#ifndef _SESSION_H
#define _SESSION_H
#include <string>
#include <list>

#include "samplefile.h"

class opinfo;
class sessionevent;

class session
{
 public:
  // Constructor - pass in the name of the session (or "" for the default)
  // and cpu/config information
  session (std::string name, const opinfo* info);

  // Returns a list of all sessions
  typedef std::list<session*> sessionlist_t;
  static sessionlist_t get_sessions (const opinfo& info);

  // Returns the name of this session
  const std::string& get_name (void) const { return _name; };

  // Returns a list of events collected in this session.
  typedef std::list<sessionevent*> seventlist_t;
  seventlist_t get_events ();

  // Searches for and returns the sessionevent which collected the
  // given event_name. Returns NULL if not found. Return value must be
  // freed by caller.
  sessionevent* get_event (std::string event_name);

  // Returns the directory for this session, i.e., SAMPLES_DIR+session_name
  std::string get_base_directory (void) const;

 private:
  // The name of this session
  std::string _name;

  // The cpu info
  const opinfo* _info;
};


#endif // !_SESSION_H
