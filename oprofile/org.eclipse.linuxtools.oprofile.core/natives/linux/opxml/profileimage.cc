/* profileimage - A class which represents a single image for
   which oprofile has samples (or for which some child dependency
   has samples).
   Written by Keith Seitz <keiths@redhat.com>
   Edited by Kent Sebastian     <ksebasti@redhat.com>
   Copyright 2004,2008 Red Hat, Inc.

   This program is open source software licensed under the Eclipse
   Public License ver. 1.

   Alternatively, this program may be used under the terms of the GNU
   General Public License as published by the Free Software Foundation;
   either version 2 of the License, or (at your option) any later
   version.  */

#include "profileimage.h"

#include <stdio.h>
#include <iostream>
#include <iterator>
#include <set>
#include <list>

#include "sample.h"
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
 * This loops through the samples and symbols for the image
 * and organizes them in a top down manner so that they can
 * be output as below:
 *
 *
 *  IMAGE name=""
 *    SYMBOL1 name="" file=""
 *      SAMPLE1  --\
 *      ..          |-- this done by sample's operator<<
 *      SAMPLEN  --/
 *    ..
 *    SYMBOLN
 *
 *
 * Note that this will only output info if there are symbols
 * for the specified image.
 *
 */
ostream&
operator<< (ostream& os, profileimage* image)
{
  samplefile* sfile;

  if (image->get_samplefile ()->has_samplefile ())
    {
      sfile = image->get_samplefile();

      //index the symbols by their bfd symbol
      map<const asymbol*, symbol*> symbols;

      //a list of samples collapsed by file & line number
      list<sample*> samples_aggregated;

      //get a list of samples from this image
      samplefile::samples_t all_samples = sfile->get_samples();

      //loop through samples, collapsing those with the same sample and line #
      for (samplefile::samples_t::iterator i = all_samples.begin (); i != all_samples.end (); ++i)
        {
          const sample* smpl = samplefile::SAMPLE (*i);

          if (smpl->has_symbol())
            {
              unsigned int line = 0;
              const char* func  = NULL;
              const char* file  = NULL;
              sfile->get_debug_info (smpl->get_vma (), func, file, line);

              //these are deleted in add_sample or at the end of this method
              sample* new_sample = new sample(smpl->get_vma(), smpl->get_symbol(), smpl->get_count(), line);

              add_sample(samples_aggregated, new_sample);
            }
        }

      //loop through the samples, find all unique symbols
      //add the sample to the symbol's list of samples
      for (list<sample*>::iterator i = samples_aggregated.begin (); i != samples_aggregated.end (); ++i)
        {
          if ((*i)->has_symbol())
            {
              unsigned int line = 0;
              const char* func  = NULL;
              const char* file  = NULL;
              sfile->get_debug_info ((*i)->get_vma (), func, file, line);

              //deleted when output later
              symbol* new_symbol = new symbol((*i)->get_symbol()->get_asymbol(), (file == NULL ? "" : file) );

              //duplicates aren't inserted because of the unique asymbol*
              symbols.insert(pair<const asymbol*, symbol*>(new_symbol->get_asymbol(),new_symbol));

              //this symbol must be in the map, either it was just added or it was already there
              symbols[(*i)->get_symbol()->get_asymbol()]->add_sample((*i));

              //add to total count of the symbol
              symbols[(*i)->get_symbol()->get_asymbol()]->add_count((*i)->get_count());
            }
        }

      char buf[11];
      long total_count = sfile->get_sample_count() + get_dependent_count(image->get_dependencies());
      sprintf(buf,"%ld", total_count);

      os << startt ("image")
         << attrt ("name", get_name(image))
         << attrt ("count", buf);

      if (symbols.size() > 0) {
        os << startt("symbols");

        set<symbol*, symbol_comp>* sorted_symbols = sort_symbols(&symbols);

        //output the symbols, and free their memory
        for (set<symbol*, symbol_comp>::iterator i = sorted_symbols->begin(); i != sorted_symbols->end(); ++i)
          {
            os << *i;
            delete *i;
          }

        os << endt;     // </symbols>
        sorted_symbols->clear();
        symbols.clear();
      }

      //free the memory from the allocated samples
      for (list<sample*>::iterator i = samples_aggregated.begin(); i != samples_aggregated.end(); ++i)
        {
//          cerr << (*i)->get_demangled_name() << " " << (*i)->get_line() << " " << (*i)->get_count() << endl;
          delete (*i);
        }
      samples_aggregated.clear();


      //output dependent images
      list<profileimage*>* deps = image->get_dependencies ();
      if (!deps->empty())
        {
          char buf[21];
          sprintf(buf,"%ld", get_dependent_count(deps));

          os << startt ("dependent")
             << attrt ("count", buf);

          set<profileimage*, depimage_comp>* ordered_deps = sort_depimages(deps);

          copy (ordered_deps->begin (), ordered_deps->end (), ostream_iterator<profileimage*> (os, ""));
          os << endt;
        }

      os << endt;       //</image>
    }

  return os;
}

//Adds the sample new_sample to the container samples.
//
//This method is required to collapse multiple samples that
// occur on the same line, since one line can correspond to
// more than one instructions and hence multiple samples
// occur for the same line of code.
void
add_sample(list<sample*> &samples, sample* new_sample)
{
  bool added = false;

  if (samples.size() == 0)
    {
      samples.push_back(new_sample);
    }
  else
    {
      for (list<sample*>::iterator i = samples.begin(); i != samples.end(); ++i)
        {
          //compare based on the symbols (bfd symbol ptr) and line number
          if ((*i)->get_symbol()->get_asymbol() == new_sample->get_symbol()->get_asymbol() &&
              (*i)->get_line() == new_sample->get_line())
            {
              //sample exists -- remove from vector, aggregate, reinsert
              sample* s = new sample( (*i)->get_vma(),
                                      (*i)->get_symbol(),
                                      (*i)->get_count() + new_sample->get_count(),
                                      (*i)->get_line());

              delete (*i);
              samples.erase(i);
              samples.push_back(s);
              added = true;
              break;
            }
        }

      if (!added)
        {
          samples.push_back(new_sample);
        }
    }
}

long
get_dependent_count(const list<profileimage*>* const deps)
{
  long dep_count = 0;

  //get total count for all the dependent images
  for (list<profileimage*>::const_iterator i = deps->begin(); i != deps->end(); ++i)
    {
      dep_count += (*i)->get_samplefile()->get_sample_count();
    }

  return dep_count;
}

//special case name for VDSO
string
get_name(const profileimage* p)
{
  string name = p->get_name();

  if (name == "")
    {
      //FIXME: any better way to do this?
      if ((p->get_samplefile()->get_sample_file_name()).find("{anon:[vdso]}",0) != string::npos)
        {
          name = VDSO_NAME_STRING;
        }
    }

  return name;
}

set<profileimage*, depimage_comp>*
sort_depimages(const std::list<profileimage*>* const deps) {
	set<profileimage*, depimage_comp>* sorted_deps = new set<profileimage*, depimage_comp>();
	for (list<profileimage*>::const_iterator i = deps->begin(); i != deps->end(); ++i) {
		sorted_deps->insert((*i));
	}

	return sorted_deps;
}

set<symbol*, symbol_comp>*
sort_symbols(const std::map<const asymbol*, symbol*>* const symbols) {
        set<symbol*, symbol_comp>* sorted_syms = new set<symbol*, symbol_comp>();

        for (map<const asymbol*, symbol*>::const_iterator i = symbols->begin(); i != symbols->end(); ++i) {
          sorted_syms->insert(i->second);
        }

        return sorted_syms;
}
