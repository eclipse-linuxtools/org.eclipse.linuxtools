/*
 * alloctest.c
 *
 *  Created on: Oct 27, 2008
 *      Author: ebaron
 */
#include <stdlib.h>
#include <unistd.h>

#define SIZE sizeof(int) * 10

int main(int argc, char **argv) {
	int *foo();
	void bar(int *);
	int *ptr1 = (int *)malloc(SIZE);
	int *ptr2 = foo();
	int *ptr3 = (int *)malloc(SIZE);
	int *ptr4 = foo();
	int *ptr5 = (int *)malloc(SIZE);
	int *ptr6 = foo();

	free(ptr1);
	bar(ptr2);
	free(ptr3);
	bar(ptr4);
	free(ptr5);
	bar(ptr6);
	return 0;
}

int *foo() {
	return (int *)malloc(SIZE);
}

void bar(int *ptr) {
	//free(ptr);
}
