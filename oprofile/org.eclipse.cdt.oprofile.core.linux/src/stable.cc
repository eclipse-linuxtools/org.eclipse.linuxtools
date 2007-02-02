/* symboltable - A symbol table class
   Written by Keith Seitz <keiths@redhat.com>
   Copyright 2004 Red Hat, Inc.

   This program is open source software licensed under the Eclipse
   Public License ver. 1.

   Alternatively, this program may be used under the terms of the GNU
   General Public License as published by the Free Software Foundation;
   either version 2 of the License, or (at your option) any later
   version.  */

#include <stdlib.h>
#include <string.h>
#include <iostream>
#include <fcntl.h>

#include "stable.h"
#include "symbol.h"

static bool ltvma (const symbol* a, const symbol* b);

// Stolen from oprofile
char const* symboltable::_boring_symbols[] = {
  "gcc2_compiled.",
  "_init"
};

// Helper function for sorting symbols by VMA
static bool
compare_symbol_with_vma (bfd_vma vma, const symbol* a)
{
  return (a->end () > vma);
}

symboltable::symboltable(const char* file)
  : _filename (strdup (file)), _abfd (NULL), _symbol_table (NULL), _cache_symbol (NULL)
{
}

symboltable::~symboltable ()
{
  if (_symbol_table != NULL)
    free (_symbol_table);

  std::vector<symbol*>::iterator i = _symbols.begin ();
  while (i != _symbols.end ())
    {
      delete (*i);
      //_symbols.erase (i);
      ++i;
    }

  _symbols.clear ();
  _close_bfd ();
  free (_filename);
}

// If this returns NULL, then the VMA is not in any range of
// msymbols. This is can apparently happen. op_time and friends
// ignore these samples.
symbol*
symboltable::lookup_vma (bfd_vma vma, bfd_vma& real_vma, bool is_kernel)
{
  if (is_kernel)
    real_vma = vma;
  else
    real_vma = vma + _start_vma - _text_offset;
  return (lookup_vma (real_vma));
}

symbol*
symboltable::lookup_vma (bfd_vma real_vma)
{
  if (_cache_symbol != NULL && _cache_symbol->contains (real_vma))
    return _cache_symbol;

  std::vector<symbol*>::iterator i;
  i = upper_bound (_symbols.begin (), _symbols.end (),
		   real_vma, compare_symbol_with_vma);
  if (i != _symbols.end () && (*i)->contains (real_vma))
    {
      _cache_symbol = *i;
      return *i;
    }

  return NULL;
}

bool
symboltable::read_symbols ()
{
  if (_open_bfd ())
    {
      long storage_needed = bfd_get_symtab_upper_bound (_abfd);
      if (storage_needed > 0)
	{
	  _symbol_table = (asymbol**) malloc (storage_needed);
	  long num_symbols = bfd_canonicalize_symtab (_abfd, _symbol_table);
	  for (int i = 0; i < num_symbols; ++i)
	    {
	      if (_interesting_symbol (_symbol_table[i]))
		_symbols.push_back (new symbol (_symbol_table[i]));
	    }

	  if (_symbols.size() > 0)
	    {
	      // Sort in order of increasing vma and eliminate duplicates
	      stable_sort (_symbols.begin (), _symbols.end (), ltvma);

	      // Eliminate duplicates
	      for (size_t i = 0; i < _symbols.size () - 1; ++i)
		{
		  if (_symbols[i]->start () == _symbols[i+1]->start ())
		    {
		      int erase;

		      // Opt to keep FUNCTIONs, first come, first kept
		      if (_symbols[i]->flags () & BSF_FUNCTION)
			erase = i + 1;
		      else if (_symbols[i+1]->flags () & BSF_FUNCTION)
			erase = i;
		      else // Don't know. Keep first.
			erase = i + 1;

		      delete *(_symbols.begin () + erase);
		      _symbols.erase (_symbols.begin () + erase);
		      --i;
		    }
		}
	      
	      // Fill in end addresses
	      for (size_t i = 0; i <= _symbols.size () - 1; ++i)
		{
		  asymbol* this_sym;
		  asymbol* next_sym = NULL;
		  asection* this_sect;
		  asection* next_sect;

		  this_sym = _symbols[i]->get_asymbol ();
		  this_sect = bfd_get_section (this_sym);
		  if (i < _symbols.size () - 1)
		    {
		      next_sym = _symbols[i + 1]->get_asymbol ();
		      next_sect = bfd_get_section (next_sym);
		    }
		  else
		    next_sect = NULL;

		  if (next_sect != NULL
		      && bfd_get_section (this_sym) == bfd_get_section (next_sym))
		    _symbols[i]->end (_symbols[i + 1]->start ());
		  else
		    {
		      asection* asect = bfd_get_section (this_sym);
		      bfd_vma end = bfd_get_section_vma (_abfd, asect);
		      end += bfd_section_size (_abfd, asect);
		      _symbols[i]->end (end);
		    }
		}

	      return true;
	    }
	}
      /* This may not seem correct, since we're pasing pointers of asymbols
	 and the like to other pieces of opxml (class symbol, in particular),
	 but remember that we've allocated memory for the symbol table earlier,
	 and class symbol references that. So as long as the object "class symboltable"
	 is not deleted, all symbols will be valid, and we can close the bfd. */
      _close_bfd ();
    }

  return false;
}

bool
symboltable::_interesting_symbol (asymbol* sym)
{
  if (!(bfd_get_section_flags (bfd_asymbol_bfd (sym), bfd_get_section (sym)) & SEC_CODE))
    return false;

  const char* name = bfd_asymbol_name (sym);
  if (name == NULL || name[0] == '\0')
    return false;

  // C++ exception stuff
  if (name[0] == '.' && name[1] == 'L')
    return false;

  for (size_t i = 0; i < sizeof (_boring_symbols) / sizeof (_boring_symbols[0]); i++)
    {
      if (strcmp (name, _boring_symbols[i]) == 0)
	return false;
    }

  return true;
}

bool
symboltable::_open_bfd (void)
{
  if (_abfd == NULL)
    {
      bfd_init ();
      int fd = open (_filename, O_RDONLY); // bfd_close will close fd
      _abfd = bfd_fdopenr (_filename, NULL, fd);
      if (_abfd != NULL)
	{
	  char** matches;
	  if (bfd_check_format_matches (_abfd, bfd_object, &matches))
	    {
	      asection const* sect;
	      for (sect = _abfd->sections; sect != NULL; sect = sect->next)
		{
		  if (sect->flags & SEC_CODE)
		    {
		      _text_offset = sect->filepos;
		      break;
		    }
		}
	    }

	  _start_vma = bfd_get_start_address (_abfd);
	}
    }
  return (_abfd != NULL);
}

void
symboltable::_close_bfd (void)
{
  if (_abfd != NULL)
    bfd_close (_abfd);

  _abfd = NULL;
}

bool
symboltable::get_debug_info (bfd_vma vma, const char*& function,
			     const char*& source_file, unsigned int& line)
{
  function = NULL;
  source_file = NULL;
  line = 0;

  symbol* symbol = lookup_vma (vma);
  if (symbol != NULL)
    {
      asection* asection = symbol->section ();

      bfd_vma pc = vma - bfd_get_section_vma (_abfd, asection);
      return bfd_find_nearest_line (_abfd, asection, _symbol_table, pc, 
				    &source_file, &function, &line);
    }

  return false;
}

#if 0
void
symboltable::dump_table (void)
{
  printf ("%8s\t%8s\t%s\n", "start", "end", "name");

  std::vector<symbol*>::iterator i;
  for (i = _symbols.begin (); i != _symbols.end (); ++i)
    {
      symbol* sym = *i;;
      printf("%8x\t%8x\t%s\n", sym->start (), sym->end (),
	     sym->name ());
    }
}
#endif

// Helper function to sort two symbols based on VMA
static bool
ltvma (const symbol* a, const symbol* b)
{
  return (a->start () < b->start ());
}

