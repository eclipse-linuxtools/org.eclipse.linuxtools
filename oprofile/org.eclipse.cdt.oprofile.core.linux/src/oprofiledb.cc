/* oprofile_db - An Oprofile sample file database wrapper.
   Written by Keith Seitz <keiths@redhat.com>
   Copyright 2004 Red Hat, Inc.

   This program is open source software licensed under the Eclipse
   Public License ver. 1.

   Alternatively, this program may be used under the terms of the GNU
   General Public License as published by the Free Software Foundation;
   either version 2 of the License, or (at your option) any later
   version.  */

#include <iostream>
#include <op_sample_file.h>

#include "oprofiledb.h"
#include "stable.h"
#include "sample.h"

oprofile_db::oprofile_db (std::string filename)
  : _filename (filename), _tree (NULL), _symbol_table (NULL), _is_kernel (false)
{
}

oprofile_db::~oprofile_db ()
{
  _close_db ();
  samples_t::iterator i = _samples.begin ();
  while (i != _samples.end ())
    {
      delete SAMPLE (*i);
      ++i;
    }
  _samples.clear ();
}

void
oprofile_db::_open_db (void)
{
  if (_tree == NULL)
    {
      int rc;

      _tree = new odb_t;
      rc = odb_open (_tree, _filename.c_str (), ODB_RDONLY, sizeof (opd_header));
      if (rc != 0)
	{
	  // This shouldn't happen, but let's at least print something out.
	  std::cerr << "Error opening oprofile database: " << strerror (rc)
		    << std::endl;
	  return;
	}

      // Get the is_kernel parameter: this is needed for sample gathering later
      const opd_header* hdr = static_cast<opd_header*> (odb_get_data (_tree));
      _is_kernel = (hdr->is_kernel != 0);
    }
}

void
oprofile_db::_close_db (void)
{
  if (_tree != NULL)
    {
      odb_close (_tree);
      delete _tree;
    }

  _tree = NULL;
}

static void
samples_odb_travel (odb_t* hash, int start, int end, oprofile_db::callback_t callback, void* data)
{
  odb_node_nr_t node_nr, pos;
  odb_node_t* node = odb_get_iterator (hash, &node_nr);
  for (pos = 0; pos < node_nr; ++pos)
    {
      if (node[pos].key)
	callback (node[pos].key, node[pos].value, data);
    }
}

void
oprofile_db::walk_samples (callback_t callback, void* data)
{
  _open_db ();
  samples_odb_travel (_tree, 0, ~0, callback, data);
  _close_db ();
}

const oprofile_db::samples_t&
oprofile_db::get_samples (symboltable* stable)
{
  _symbol_table = stable;
  walk_samples (_get_samples_callback, this);
  _symbol_table = NULL;
  return _samples;
}

bool
oprofile_db::has_samples (void)
{
  walk_samples (_has_samples_callback, this);
  return _has_samples;
}

long
oprofile_db::get_count (void)
{
  long count = 0;
  walk_samples (_get_count_callback, &count);
  return count;
}

// This is a callback from oprofile when traveling the samples in the sample file.
void
oprofile_db::_get_samples_callback (odb_key_t key, odb_value_t info, void* data)
{
  oprofile_db* odb = static_cast<oprofile_db*> (data);

  symbol* symbol = NULL;
  bfd_vma real_addr;
  if (odb->_symbol_table != NULL)
    symbol = odb->_symbol_table->lookup_vma ((bfd_vma) key, real_addr, odb->_is_kernel);

  // Oprofile can have multiple samples for the same VMA, so look in the
  // our map/database and see if the given VMA exists. If it does not exist,
  // add a new Sample. If it does exist, just increment the count of the Sample
  // by INFO.
  samples_t::iterator i = odb->_samples.find ((bfd_vma) key);
  if (i == odb->_samples.end ())
    {
      // new sample
      sample* s = new sample (real_addr, symbol, info);
      odb->_samples.insert (sample_t (key, s));
    }
  else
    {
      // existing sample
      SAMPLE (*i)->incr_count (info);
    }
}

void
oprofile_db::_has_samples_callback (odb_key_t key, odb_value_t info, void* data)
{
  oprofile_db* odb = static_cast<oprofile_db*> (data);
  if (info > 0)
    odb->has_samples (true);
}

void
oprofile_db::_get_count_callback (odb_key_t key, odb_value_t info, void* data)
{
  long* count = static_cast<long*> (data);
  *count += info;
}

