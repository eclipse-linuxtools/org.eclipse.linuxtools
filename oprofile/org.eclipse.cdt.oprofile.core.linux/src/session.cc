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

#include "session.h"
#include <iostream>

#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#include <dirent.h>

#include "sevent.h"
#include "opinfo.h"
#include "xmlfmt.h"

using namespace std;

session::session(string name, const opinfo* info)
  : _name (name), _info (info)
{
}

string
session::get_base_directory (void) const
{
  return _info->get_samples_directory () + _name;
}

session::sessionlist_t
session::get_sessions (const opinfo& info)
{
  sessionlist_t sessions;

  struct stat sbuf;
  int rc = stat (info.get_samples_directory ().c_str (), &sbuf);
  if (rc < 0)
    {
      cerr << "cannot stat samples directory (" << info.get_samples_directory () << ")"
	   << endl;
      return sessions;
    }
  else if (S_ISDIR (sbuf.st_mode) == 0)
    {
      cerr << "samples directory (" << info.get_samples_directory ()
	   << ") is not a directory" << endl;
      return sessions;
    }

  DIR* dirp = opendir (info.get_samples_directory ().c_str ());
  if (dirp == NULL)
    {
      cerr << "cannot read samples directory (" << info.get_samples_directory () << ")"
	   << endl;
      return sessions;
    }

  struct dirent* dir;
  while ((dir = readdir (dirp)) != NULL)
    {
      if (strcmp (dir->d_name, ".") != 0 && strcmp (dir->d_name, "..") != 0)
	{
	  string name (dir->d_name);
	  sessions.push_back (new session (name, &info));
	}
    }

  return sessions;
}

// returns NULL if not found
sessionevent*
session::get_event (string event_name)
{
  list<string> filelist;
  samplefile::get_sample_file_list (filelist, get_base_directory ());

  // Loop through all sample files, create & populate sessionevents
  // with sample file lists
  sessionevent* the_sevent = NULL;
  list<samplefile*> deps;
  list<string>::iterator fit = filelist.begin ();
  for (; fit != filelist.end (); ++fit)
    {
      if (samplefile::event_for_filename (*fit) == event_name)
	{
	  samplefile* sfile = new samplefile (*fit);
	  if (!sfile->is_dependency ())
	    {
	      // main image
	      if (the_sevent == NULL)
		{
		  // found the desired event -- create it
		  the_sevent = new sessionevent (this, event_name); 
		}

	      // Add this sample file to the sessionevent
	      the_sevent->add_sample_file (sfile);
	    }
	  else
	    {
	      // dependency -- save it for later resolution
	      deps.push_back (sfile);
	    }
	}
    }

  // Now run through the list of dependencies
  if (the_sevent != NULL)
    {
      list<samplefile*>::iterator sfit;
      for (sfit = deps.begin (); sfit != deps.end (); ++sfit)
	{
	  samplefile* sfile = *sfit;
	  the_sevent->add_sample_file (sfile);
	}
    }

  return the_sevent;
}

session::seventlist_t
session::get_events ()
{
  list<string> filelist;
  samplefile::get_sample_file_list (filelist, get_base_directory ());

  // Loop through all sample files, create & populate sessionevents
  // with sample file lists
  seventlist_t events;
  map<string, sessionevent*> emap;
  list<samplefile*> deps;
  list<string>::iterator fit = filelist.begin ();
  for (; fit != filelist.end (); ++fit)
    {
      if (samplefile::event_for_filename (*fit) != "")
	{
	  samplefile* sfile = new samplefile (*fit);
	  if (!sfile->is_dependency ())
	    {
	      // main image
	      map<string, sessionevent*>::iterator item;
	      item = emap.find (sfile->get_event ());
	      if (item == emap.end ())
		{
		  // new event -- create sessionevent
		  sessionevent* se = new sessionevent (this, sfile->get_event ()); 

		  // Save this sessionevent in the event map
		  emap.insert (make_pair<string, sessionevent*> (sfile->get_event (), se));

		  // Add this sample file to the list
		  se->add_sample_file (sfile);

		  // Finally, add this new sessionevent to result
		  events.push_back (se);
		}
	      else
		{
		  // Add this sample file to the sessionevent
		  sessionevent* se = (*item).second;
		  se->add_sample_file (sfile);
		}
	    }
	  else
	    {
	      // dependency -- save it for later resolution
	      deps.push_back (sfile);
	    }
	}
    }

  // Now run through the list of dependencies
  list<samplefile*>::iterator sfit;
  for (sfit = deps.begin (); sfit != deps.end (); ++sfit)
    {
      samplefile* sfile = *sfit;
      map<string, sessionevent*>::iterator item;
      item = emap.find (sfile->get_event ());
      if (item != emap.end ())
	{
	  sessionevent* se = (*item).second;
	  se->add_sample_file (sfile);
	}
      else
	cerr << "WARNING! dep file with no event!" << endl;
    }

  return events;
}
