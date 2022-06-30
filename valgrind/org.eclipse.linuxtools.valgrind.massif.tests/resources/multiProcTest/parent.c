#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

#define SIZE 100
int main() {
	if (fork()) {
		int *a = (int *)malloc(sizeof(int) * SIZE);
		int *b = (int *)malloc(sizeof(int) * SIZE);
		int *c = (int *)malloc(sizeof(int) * SIZE);
		free(a);
		free(b);
		free(c);
	}
	else {
                char *params[2] = {(char *)"alloctest", NULL};
		execv("../alloctest/Debug/alloctest", params);
		perror("execv");
	}
	return 0;
}

