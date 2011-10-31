libtestgcovlib.so:
	gcc -shared -fPIC -fprofile-arcs -ftest-coverage fact.c mult.c -o libtestgcovlib.so

build a.out: libtestgcovlib.so
	gcc -L . -ltestgcovlib main.c

all: a.out
	LD_LIBRARY_PATH=. ./$< 1 2 3 4 5 6
	
clean:
	rm -f *.o a.out *.gcno *.gcda
