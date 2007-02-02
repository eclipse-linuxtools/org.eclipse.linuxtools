/* Opxml a simple program to output XML descriptions of oprofile sample
   files (and a little more). This program exists as a bridge between
   GPL'd software (oprofile and BFD) and CPL'd software (Eclipse).
   Written by Keith Seitz <keiths@redhat.com>
   Copyright 2004 Red Hat, Inc.

   This program is open source software licensed under the Eclipse
   Public License ver. 1.

   Alternatively, this program may be used under the terms of the GNU
   General Public License as published by the Free Software Foundation;
   either version 2 of the License, or (at your option) any later
   version.  */

#include <stdlib.h>
#include <iostream>
#include <string>
#include <getopt.h>
#include <errno.h>
#include <iterator>

#include "opinfo.h"
#include "oxmlstream.h"
#include "session.h"
#include "sample.h"
#include "sevent.h"

using namespace std;

// Enum describing info options
enum options
{
   INFO,
   CHECK_EVENT,
   DEBUG_INFO,
   SAMPLES,
   SESSIONS
};

// Strings of options
static const char* options[] =
{
  "info",
  "check-events",
  "debug-info",
  "samples",
  "sessions",
  0
};

// Help for options
struct help
{
  const char* arg;
  const char* desc;
};

static const struct help args_help[] =
{
  {"[CPU]\t\t\t", "information for given cpu (defualt: current cpu)" },
  {"CTR EVENT UMASK\t", "check counter/event validity"},
  {"SAMPLEFILE\t\t", "return debug information for in SAMPLEFILE"},
  {"EVENT [SESSION]\t", "get samples for given SESSION and EVENT (default: current)"},
  {"\t\t\t", "get session information"}
};

// Local functions
static void print_usage (const char* const argv0);
static int get_option_index (const char* options[], const char* arg);
static void wrong_num_arguments (int argc, char* argv[], const char* opts);
static int get_integer (const char* arg);

// Info handlers
static int info (opinfo& info, int argc, char* argv[]);
static int check_events (opinfo& info, int argc, char* argv[]);
static int debug_info (opinfo& info, int argc, char* argv[]);
static int samples (opinfo& info, int argc, char* argv[]);
static int sessions (opinfo& info, int argc, char* argv[]);

class sevent_sorter : public binary_function<sessionevent*, sessionevent*, bool>
{
public:
  bool operator() (sessionevent* lhs, sessionevent* rhs)
  {
    bool result;
    if (lhs->get_name () == "")
      result = true;
    else if (rhs->get_name () == "")
      result = false;
    else
      result = lhs->get_name () < rhs->get_name ();
    return result;
  }
};

static void
wrong_num_arguments (int argc, char* argv[], const char* opts)
{
  cerr << "wrong # args: should be \"";
  for (int i = 0; i < argc; ++i)
    cerr << argv[i] << " ";

  cerr << opts << "\"" << endl;
  exit (EXIT_FAILURE);
}

// Converts the argument into its corresponding option index
static int
get_option_index (const char* options[], const char* arg)
{
  const char* option;

  int i = 0;
  for (option = options[0]; option != NULL; option = options[++i])
    {
      if (strncmp (option, arg, strlen (arg)) == 0)
	return i;
    }

  return -1;
}

// Prints a small help message
static void
print_help (const char* const argv0)
{
  cerr << "Use '" << argv0 << " --help' for a complete list of options."
	    << endl;
}

// Prints the usage of this program
static void
print_usage (const char* const argv0)
{
  cerr << argv0 << ": usage: " << argv0 << " [OPTION] INFO [INFO_OPTIONS]" << endl;
  cerr << "Supply information about Oprofile and its sample files." << endl << endl;
  cerr << "Options:" << endl << endl;
  cerr << "--cpu|-c CPU_TYPE\t\t   cpu type (current: "
	    << op_get_cpu_type_str (op_get_cpu_type ()) << ")" << endl;
  cerr << "--dir|-d SAMPLES_DIR\t\t   set sample directory (default: "
       << opinfo::get_default_samples_dir () << ")" << endl;

  cerr << endl << "Types of INFO:" << endl << endl;

  for (unsigned int i = 0; i < (sizeof (args_help) / sizeof (args_help[0])); ++i)
    {
      cerr << options[i] << " " << args_help[i].arg << "   "
	   << args_help[i].desc << endl;
    } 
}

int
main (int argc, char* argv[])
{
  const char* argv0 = argv[0];

  if (argc < 2)
    {
      print_help (argv0);
      exit (EXIT_FAILURE);
    }

  op_cpu cpu_type = op_get_cpu_type ();
  string dir = opinfo::get_default_samples_dir ();

  static struct option long_options[] =
  {
    {"cpu-type", required_argument, 0, 'c'},
    {"dir", required_argument, 0, 'd'},
    {"help", no_argument, 0, 'h'},
    {0, 0, 0, 0}
  };

  int index;
  while (true)
    {
      int c;
      c = getopt_long_only (argc, argv, "+", long_options, &index);
      if (c == -1)
	break;

      if (c == 0 && long_options[index].flag == 0)
	c = long_options[index].val;

      switch (c)
	{
	case 'c':
	  cpu_type = opinfo::str_to_op_cpu (optarg);
	  if (cpu_type == CPU_NO_GOOD)
	    {
	      cerr << argv0 << ": cpu \"" << optarg << "\" not recognized"
			<< endl;
	      exit (EXIT_FAILURE);
	    }
	  break;

	case 'd':
	  dir = optarg;
	  break;

	case 'h':
	  print_usage (argv0);
	  exit (EXIT_SUCCESS);
	  break;

	case '?':
	  print_help (argv0);
	  exit (EXIT_FAILURE);
	}
    }

  // Check that we have a valid cpu type. It can be invalid (here) because
  // the Oprofile module not loaded/running. Oprofile library will output error.
  if (cpu_type == CPU_NO_GOOD)
    exit (EXIT_FAILURE);

  argc -= optind;
  argv += optind;

  if (argc == 0)
    {
      print_help (argv0);
      exit (EXIT_FAILURE);
    }

  int rc;
  opinfo oinfo (cpu_type, dir);
  index = get_option_index (options, argv[0]);
    {
      switch ((enum options) index)
	{
	case INFO:
	  rc = info (oinfo, argc, argv);
	  break;

	case CHECK_EVENT:
	  rc = check_events (oinfo, argc, argv);
	  break;

	case SAMPLES:
	  rc = samples(oinfo, argc, argv);
	  break;

	case DEBUG_INFO:
	  rc = debug_info (oinfo, argc, argv);
	  break;

        case SESSIONS:
          rc = sessions (oinfo, argc, argv);
	  break;

	default:
	  cerr <<  argv0 << ": unknown option \"" << argv[0] << "\""
		    << endl;
	  print_help (argv0);
	  rc = EXIT_FAILURE;
	}
    }

  return rc;
}

/* Get an integer value from the argument. Only does ints > 0.
   Returns the integer or -1. */
static int
get_integer (const char* arg)
{
  errno = 0;
  int integer = strtol (arg, NULL, 10);
  if (errno != 0)
    return -errno;
  
  return integer;
}

/* Output static information about oprofile for this cpu type. */
static int
info (opinfo& info, int argc, char* argv[])
{
  oxmlstream oxml (cout);
  oxml << info << endxml;
  return EXIT_SUCCESS;
}

/* Check whether the given counter/event/umask info is valid.
 *
 * Input: COUNTER EVENT UMASK (all integers)
 * Note: output is a BITMASK of errors. Expect multiple "result" fields on error.
 */
static int
check_events (opinfo& info, int argc, char* argv[])
{
  if (argc != 4)
    wrong_num_arguments (1, argv, "counter event umask");

  int counter = get_integer (argv[1]);
  if (counter < 0)
    {
      cerr << "invalid counter \"" << argv[1] << "\"" << endl;
      return EXIT_FAILURE;
    }

  if (counter >= info.get_nr_counters ())
    {
      cerr << "counter must not be greater than "
		<< (info.get_nr_counters () - 1) << endl;
      return EXIT_FAILURE;
    }

  int event = get_integer (argv[2]);
  if (event < 0)
    {
      cerr << "invalid event \"" << argv[2] << "\"" << endl;
      return EXIT_FAILURE;
    }

  int umask = get_integer (argv[3]);
  if (umask < 0)
    {
      cerr << "invalid unit mask \"" << argv[3] << "\"" << endl;
      return EXIT_FAILURE;
    }

  opinfo::eventcheck result = info.check (counter, event, umask);

  oxmlstream oxml (cout);
  oxml << result << endxml;
  return EXIT_SUCCESS;
}

/* Print out the debug info for the given executable and VMA in argv.
 *
 * Input: executable and vma
 */
static int
debug_info (opinfo& info, int argc, char* argv[])
{
  if (argc != 2)
    wrong_num_arguments (1, argv, "samplefile");

  samplefile sfile (argv[1]);
  samplefile::samples_t samples = sfile.get_samples ();
  
  oxmlstream oxml (cout);
  oxml << startt ("debug-info");

  // Loop through all samples (should be sorted by vma?)
  samplefile::samples_t::iterator i;
  for (i = samples.begin (); i != samples.end (); ++i)
    {
      sample* s = samplefile::SAMPLE (*i);

      unsigned int line = 0;
      const char* func  = NULL;
      const char* file  = NULL;
      sfile.get_debug_info (s->get_vma (), func, file, line);

      char buf[257];
      sprintf_vma (buf, s->get_vma ());
      oxml << startt ("address") << buf;

      if (file != NULL)
	oxml << attrt ("source-filename", file);
      if (func != NULL)
	oxml << attrt ("function", func);
      if (line != 0)
	{
	  sprintf (buf, "%d", line);
	  oxml << attrt ("line", buf);
	}
      oxml << endt;
    }

  oxml << endt << endxml;
  return EXIT_SUCCESS;
}

/* Print out the samples associated with the given session
 *
 * Input: session (none for "default" session)
 */
static int
samples (opinfo& info, int argc, char* argv[])
{
  if (argc < 2)
    wrong_num_arguments (1, argv, "event [session]");

  string event (argv[1]);

  string session_name ("current");
  if (argc == 3)
    session_name = argv[2];

  session session (session_name, &info);
  sessionevent* sevent = session.get_event (event);

  if (sevent == NULL)
    {
      cerr << "no such session or event: session=" << session_name
	   << "; event=" << event << endl;
      return EXIT_FAILURE;
    }

  sessionevent::profileimages_t* images = sevent->get_images ();

  oxmlstream oxml (cout);
  oxml << startt ("samples");

  sessionevent::profileimages_t::iterator i;
  for (i = images->begin (); i != images->end (); ++i)
    oxml << (*i);

  oxml << endt << endxml;
  
  // delete sevent; -- don't do this: it takes too much time!
  return EXIT_SUCCESS;
}


static int
sessions (opinfo& info, int argc, char* argv[])
{
  session::sessionlist_t sessions;
  sessions = session::get_sessions (info);

  /* This seems goofy, but this is best for the UI.
     Arrange the sessions by the event that they collected. */

  typedef map<string, list<sessionevent*>*, greater<string> > eventlist_t;
  eventlist_t eventlist;

  session::sessionlist_t::iterator sit = sessions.begin ();
  session::sessionlist_t::iterator const send = sessions.end ();
  for (; sit != send; ++sit)
    {
      session* s = *sit;
      session::seventlist_t events = s->get_events ();

      session::seventlist_t::iterator sit = events.begin ();
      for (; sit != events.end (); ++sit)
	{
	  sessionevent* sevent = *sit;
	  string event = sevent->get_name ();

	  if (eventlist.find (event) == eventlist.end ())
	    {
	      // New event -- add this session to the list and add
	      // the list and event to the list of known events.
	      list<sessionevent*>* lst = new list<sessionevent*> ();
	      lst->push_back (sevent);
	      eventlist.insert (pair<string, list<sessionevent*>*> (event, lst));
	    }
	  else
	    {
	      // Known event -- add this session to the list of
	      // sessions for this event
	      list<sessionevent*>* lst = eventlist[event];
	      lst->push_back (sevent);
	    }
	}
    }

  // Done compiling the list of events. Output information.
  oxmlstream oxml (cout);
  oxml << startt ("sessions");
  
  if (!eventlist.empty ())
    {
      eventlist_t::iterator elit;
      for (elit = eventlist.begin (); elit != eventlist.end (); ++elit)
	{
	  string event = (*elit).first;
	  list<sessionevent*>* lst = (*elit).second;

	  lst->sort (sevent_sorter ());
	  oxml << startt ("event") << attrt ("name", event);
	  copy (lst->begin (), lst->end (), ostream_iterator<sessionevent*> (oxml, ""));
	  oxml << endt;
	  delete lst;
	}
    }

  oxml << endt << endxml;

  return EXIT_SUCCESS;
}
