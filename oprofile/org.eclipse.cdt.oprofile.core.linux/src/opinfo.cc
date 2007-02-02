/* Generic oprofile information class for opmxl.
   Written by Keith Seitz <keiths@redhat.com>
   Copyright 2003, Red Hat, Inc.

   This program is open source software licensed under the Eclipse
   Public License ver. 1.

   Alternatively, this program may be used under the terms of the GNU
   General Public License as published by the Free Software Foundation;
   either version 2 of the License, or (at your option) any later
   version.  */

#include "opinfo.h"

#include <sstream>

#include "xmlfmt.h"

using namespace std;

// From liboputil.a. Sadly this won't work if we want to enable
// remote-system profiling.
extern "C" double op_cpu_frequency (void);

// Forward declaration
static void __output_unit_mask_info (ostream& os, const opinfo::event_t* e);

// Constructor
opinfo::opinfo (op_cpu cpu_type, string dir)
  : _cpu_type (cpu_type), _dir (dir)
{
}

// Returns the number of counters for this cpu type
int
opinfo::get_nr_counters (void) const
{
  return op_get_nr_counters (_cpu_type);
}

// Returns (in LIST) a list of valid events for the given counter
void
opinfo::get_events (eventlist_t& list, int ctr) const
{
  struct list_head* events, *p;
  events = op_events (_cpu_type);

  list_for_each (p, events)
    {
      struct op_event* event = list_entry (p, struct op_event, event_next);
      if (/*event->counter_mask == CTR_ALL || */ event->counter_mask & (1 << ctr))
	list.push_back (event);
    }
}

// Returns cpu frequency
double
opinfo::get_cpu_frequency (void) const
{
  return op_cpu_frequency ();
}

// Checks whether the given CTR, EVENT, and MASK are valid
opinfo::eventcheck
opinfo::check (int ctr, int event, int mask) const
{
  eventcheck::result_t result =
    static_cast<eventcheck::result_t> (op_check_events (ctr, event, mask, _cpu_type));
  return eventcheck (result);
}

// Converts the given string into a corresponding op_cpu (CPU_NO_GOOD if invalid)
op_cpu
opinfo::str_to_op_cpu (const char* const cpu_str)
{
  int i;
  for (i = 0; i < MAX_CPU_TYPE; ++i)
    {
      if (strcmp (op_get_cpu_type_str ((op_cpu) i), cpu_str) == 0)
	return (op_cpu) i;
    }

  return CPU_NO_GOOD;
}

// This actually outputs a bunch of information
ostream&
operator<< (ostream& os, const opinfo& info)
{
  os << startt ("info");

  // Output out number of counters and defaults
  os << startt ("num-counters") << info.get_nr_counters () << endt
     << startt ("cpu-frequency") << info.get_cpu_frequency () << endt
     << startt ("defaults")
     << startt ("sample-dir") << opinfo::get_default_samples_dir () << endt
     << startt ("lock-file") << opinfo::get_default_lock_file () << endt
     << startt ("log-file") << opinfo::get_default_log_file () << endt
     << startt ("dump-status") << opinfo::get_default_dump_status () << endt
     << endt;

  // Output event list
  for (int ctr = 0; ctr < info.get_nr_counters (); ++ctr)
    {
      opinfo::eventlist_t events;

      ostringstream ctr_s;
      ctr_s << ctr;
      os << startt ("event-list") << attrt ("counter", ctr_s.str ());
      info.get_events (events, ctr);
      opinfo::eventlist_t::iterator i;
      for (i = events.begin (); i != events.end (); ++i)
	os << (*i);
      os << endt;
    }

  return os << endt;
}

// Prints the given EVENT on the given stream
ostream&
operator<< (ostream& os, const opinfo::event_t* event)
{
  os << startt ("event")
     << startt ("name") << event->name << endt
     << startt ("description") << event->desc << endt
     << startt ("value") << static_cast<int> (event->val) << endt
     << startt ("minimum") << static_cast<int> (event->min_count) << endt;

  // ouput unit mask info
  __output_unit_mask_info (os, event);

  return os << endt;
}

// Prints the given eventcheck on the given stream
ostream&
operator<< (ostream& os, const opinfo::eventcheck& check)
{
  static const char* errors[3] = {"invalid-event", "invalid-um", "invalid-counter"};

  os << startt ("check-events");

  if (check.get_result () == OP_OK_EVENT)
    os << startt ("result") << "ok" << endt;
  else
    {
      for (unsigned int i = 0; i < sizeof (errors) / sizeof (errors[0]); ++i)
	{
	  if ((check.get_result () & (1 << i)) > 0)
	    os << startt ("result") << errors[i] << endt;
	}
    }

  return os << endt;
}

// Convenience function to output unit mask information
static void
__output_unit_mask_info (ostream& os, const opinfo::event_t* event)
{
  const struct op_unit_mask* umask = event->unit;
  const char* type;

  switch (umask->unit_type_mask)
    {
    case utm_exclusive:
      type = "exclusive";   break;
    case utm_bitmask:
      type = "bitmask";     break;
    case utm_mandatory:
    default:
      type = "mandatory";   break;
    }

  os << startt ("unit-mask")
     << startt ("type") << type << endt
     << startt ("default") << umask->default_mask << endt;
  
  for (u32 i = 0; i < umask->num; ++i)
    {
      os << startt ("mask")
	 << startt ("value") << umask->um[i].value << endt
	 << startt ("description") << umask->um[i].desc << endt
	 << endt;
    }

  os << endt;
}
