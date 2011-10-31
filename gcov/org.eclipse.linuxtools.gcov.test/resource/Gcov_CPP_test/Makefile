build a.out:
	g++ -fprofile-arcs -ftest-coverage *.cpp

all: a.out
	./$< 1 2 3 4 5 6
	
clean:
	rm -f *.o a.out *.gcno *.gcda
