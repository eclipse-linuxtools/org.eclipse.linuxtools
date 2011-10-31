build a.out:
	gcc -fprofile-arcs -ftest-coverage *.c

all: a.out
	./$< 1 2 3 4 5 6
	
clean:
	rm -f *.o a.out *.gcno *.gcda
