#include <iostream>
#include <stdlib.h>

#define SIZE 10

using namespace std;

int main()
{
	char *p1 = (char *)malloc(sizeof(char) * SIZE);
	delete(p1);

	char* p2 = (char *)malloc(5 * sizeof(char) * SIZE);
	delete[] p2;

	char *p3 = new char;
	free(p3);

	char *p4 = new char[5];
	free(p4);

	char* p5 = new char[5];
	delete p5;

	return 0;
}
