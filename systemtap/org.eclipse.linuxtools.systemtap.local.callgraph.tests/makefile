CC=gcc
CPLUS=g++
CFLAGS=-g -c -Wall
OFLAG=-o

all: catlan basic eag
	make clean

eag : eag.o
	$(CPLUS) $(OFLAG) eag eag.o

catlan: catlan.o 
	$(CC) $(OFLAG) catlan catlan.o

basic: basic.o
	$(CC) $(OFLAG) basic basic.o


eag.o : eag.c
	$(CPLUS) $(CFLAGS) eag.c

catlan.o: catlan.c
	$(CC) $(CFLAGS) catlan.c

basic.o: basic.c
	$(CC) $(CFLAGS) basic.c

clean:
	rm catlan.o basic.o eag.o

