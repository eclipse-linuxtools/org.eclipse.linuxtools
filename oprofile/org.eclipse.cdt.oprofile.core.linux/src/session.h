/* session - a class which represents an oprofile session.
   All sessions occur as directories of the samples directory.
   Written by Keith Seitz <keiths@redhat.com>
   Copyright 2003, 2004 Red Hat, Inc.

   This program is open source software licensed under the Eclipse
   Public License ver. 1.

   Alternatively, this program may be used under the terms of the GNU
   General Public License as published by the Free Software Foundation;
   either version 2 of the License, or (at your option) any later
   version.  */

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
