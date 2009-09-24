/*
 * main.c
 *
 *  Created on: 2009-09-21
 *      Author: chwang
 */
#include <stdio.h>


void calledOnce() {
	printf("Called\n");
}

void calledTwice() {
	printf("Double\n");
}

int main() {
	printf("Done\n");
	calledOnce();
	calledTwice();
	calledTwice();
	return 0;
}

