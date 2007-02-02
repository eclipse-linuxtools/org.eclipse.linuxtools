# Makefile for Eclipse Oprofile wrapper library
# Written by Keith Seitz  <keiths@redhat.com>
#
# Copyright (c) 2004, Red Hat, Inc.

CFLAGS += -g -Wall
CXXFLAGS = $(CFLAGS)

OPROFILE_LIBS = -lop -lodb -loputil++ -loputil
BFD_LIBRARY = -lbfd
LIBIBERTY = -liberty
OTHER_LIBS = $(BFD_LIBRARY) 
EXTRA_LIBS = $(OPROFILE_LIBS) $(OTHER_LIBS) $(LIBIBERTY)

OPXML = 		opxml
OPXML_OBJS =		opxml.o
OPROFILE_LIB_OBJS =	stable.o oprofiledb.o symbol.o sample.o \
			session.o samplefile.o imageheader.o opinfo.o \
			xmlfmt.o xmlbuf.o xmltag.o \
			sevent.o profileimage.o


all: $(OPXML)

$(OPXML): $(OPROFILE_LIB_OBJS) $(OPXML_OBJS)
		$(CXX) -o $(OPXML) $(OPXML_OBJS) $(OPROFILE_LIB_OBJS) \
		$(EXTRA_LIBS)

clean:
	$(RM) $(OPROFILE_LIB_OBJS) $(OPXML) $(OPXML_OBJS) *~



# Dependencies
opxml.o: oxmlstream.h opinfo.h session.h sample.h sevent.h opxml.cc

imageheader.o: xmlfmt.h imageheader.h imageheader.cc
imageheader.h: samplefile.h

opinfo.o: opinfo.h opinfo.cc
opinfo.h: 

oprofiledb.o: oprofiledb.h sample.h stable.h oprofiledb.cc
oprofiledb.h: xmlbuf.h xmlfmt.h

oxmlstream.h: xmlbuf.h xmlfmt.h

profileimage.o: xmlfmt.h profileimage.h profileimage.cc
profileimage.h: samplefile.h

sample.o: xmlfmt.h sample.h sample.cc
sample.h: symbol.h

samplefile.o: sample.h stable.h xmlfmt.h samplefile.h samplefile.cc
samplefile.h: oprofiledb.h

session.o: sevent.h opinfo.h xmlfmt.h session.h session.cc
session.h: samplefile.h

sevent.o: session.h xmlfmt.h sevent.h sevent.cc
sevent.h: profileimage.h

stable.o: stable.h symbol.h stable.cc
stable.h: 

symbol.o: xmlfmt.h symbol.h symbol.cc
symbol.h: 

xmlbuf.o: xmltag.h xmlbuf.h xmlbuf.cc
xmlbuf.h: 

xmlfmt.o: xmlbuf.h xmlfmt.h xmlfmt.cc
xmlfmt.h: 

xmltag.o: xmltag.h xmltag.cc
xmltag.h:

