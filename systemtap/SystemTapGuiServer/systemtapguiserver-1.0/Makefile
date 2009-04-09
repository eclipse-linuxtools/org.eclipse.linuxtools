# Makefile for datamanager daemon
OBJECTS = subscriptionMgr.o datamanager.o MailBox.o confReader.o dmerror.o \
common.o logger.o
TESTOBJS = test/testdriver.o dmerror.o common.o
LD_FLAGS = -lpthread 

all: makefile.dep stapgui-server 

makefile.dep : *.cpp include/*.hpp
	for i in *.cpp; do gcc -MM "$${i}"; done > $@
include makefile.dep

stapgui-server: $(OBJECTS)
	$(CXX) -o $@ $^ $(LD_FLAGS)

.PHONY: clean  	

clean:
	rm -f stapgui-server $(OBJECTS) $(TESTOBJS) stapgui-server 
