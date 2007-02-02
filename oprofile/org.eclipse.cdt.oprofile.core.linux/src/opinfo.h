/* Generic oprofile information class for opmxl.
   Written by Keith Seitz <keiths@redhat.com>
   Copyright 2003, Red Hat, Inc.

   This program is open source software licensed under the Eclipse
   Public License ver. 1.

   Alternatively, this program may be used under the terms of the GNU
   General Public License as published by the Free Software Foundation;
   either version 2 of the License, or (at your option) any later
   version.  */
   
#ifndef _OPINFO_H
#define _OPINFO_H
#include <vector>
#include <ostream>

#include <op_cpu_type.h>
#include <op_config.h>
#include <op_events.h>

struct op_event;

// A class which knows about static oprofile information, i.e., things
// which do not depend on sample files.
class opinfo
{
  public:
  // The type of an oprofile event
  typedef struct op_event event_t;

  // The type of a list of oprofile events
  typedef std::vector<event_t*> eventlist_t;

  // A class used for reporting the validity of an event.
  // Used by opinfo::check.
  class eventcheck
  {
  public:
    // The result type returned by get_result;
    typedef enum op_event_check result_t;

    // Constructor
    eventcheck (result_t ec) : _ec (ec) {};

    // Returns the result of the check. Can be (bitmask):
    // OP_OK_EVENT, OP_INVALID_EVENT, OP_INVALID_UM, OP_INVALID_COUNTER
    inline result_t get_result (void) const { return _ec; };

  private:
    // The result from the check
    result_t _ec;
  };

  // Get the default sample directory
  inline static const char* get_default_samples_dir (void) { return OP_SAMPLES_DIR; };

  // Get the default lock filename
  inline static const char* get_default_lock_file (void) { return OP_LOCK_FILE; };

  // Get the default log filename
  inline static const char* get_default_log_file (void) { return OP_LOG_FILE; };

  // Get the default dump status filename
  inline static const char* get_default_dump_status (void) { return OP_DUMP_STATUS; };

  // Converts the given string into an enum op_cpu
  static op_cpu str_to_op_cpu (const char* const cpu_str);

  // Constructors
  opinfo (op_cpu cpu_type = op_get_cpu_type (), std::string dir = get_default_samples_dir ());

  // Returns the number of counters for this cpu type
  int get_nr_counters (void) const;

  // Returns a list of valid events for the given counter on this cpu type
  void get_events (eventlist_t& list, int ctr) const;

  // Returns the samples directory in use
  inline const std::string& get_samples_directory (void) const { return _dir; };

  // Returns the CPU frequency in MHz
  double get_cpu_frequency (void) const;

  // Returns an eventcheck object representing whether the given
  // CTR, EVENT, and MASK are valid for this cpu type
  eventcheck check (int ctr, int event, int mask) const;

  // Returns the cpu type being used
  op_cpu get_cpu_type (void) const { return _cpu_type; };

 private:
  // The cpu type
  op_cpu _cpu_type;

  // The sample directory to use
  std::string _dir;
};

// Insert operators for various classes defined in this file
std::ostream& operator<< (std::ostream& os, const opinfo::eventcheck& ec);
std::ostream& operator<< (std::ostream& os, const opinfo::event_t* event);
std::ostream& operator<< (std::ostream& os, const opinfo& info);
#endif // !_OPINFO_H
