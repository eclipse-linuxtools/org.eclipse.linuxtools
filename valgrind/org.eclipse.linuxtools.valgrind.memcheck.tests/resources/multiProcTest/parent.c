#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

#define SIZE 10
int main() {
	if (fork()) {
		int *a;
		printf("%d\n", *a);
	}
	else {
		execv("../basicTest/Debug/test", NULL);
		perror("execv");
	}
	return 0;
}

