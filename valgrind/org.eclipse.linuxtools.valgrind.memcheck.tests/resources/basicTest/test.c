/*
 * test.c
 *
 *  Created on: Sep 12, 2008
 *      Author: ebaron
 */
#include <stdlib.h>
#include <stdio.h>
#include <fcntl.h>

#define SIZE 10
int main() {
	open("../test.c", O_RDONLY);
	char *waste = (char *)malloc(sizeof(char) * SIZE);
	int *a;
	printf("%d\n", *a);
	waste[SIZE] = 0;
	return 0;
}
