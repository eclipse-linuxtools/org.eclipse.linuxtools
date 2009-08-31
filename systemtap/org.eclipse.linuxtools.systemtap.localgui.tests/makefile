CC=gcc
CFLAGS=-g -c -Wall
OFLAG=-o

all: catlan basic
	make clean

catlan: catlan.o 
	$(CC) $(OFLAG) catlan catlan.o

basic: basic.o
	$(CC) $(OFLAG) basic basic.o

catlan.o: catlan.c
	$(CC) $(CFLAGS) catlan.c

basic.o: basic.c
	$(CC) $(CFLAGS) basic.c

clean:
	rm catlan.o basic.o

