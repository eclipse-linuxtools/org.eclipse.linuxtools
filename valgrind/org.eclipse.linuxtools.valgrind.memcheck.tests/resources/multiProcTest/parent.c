#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

#define SIZE 10
int main() {
	if (fork()) {
		char *waste = (char *)malloc(sizeof(char) * SIZE);
	}
	else {
		execv("../basicTest/Debug/basicTest", (char *[]) { "basicTest", NULL });
		perror("execv");
	}
	return 0;
}

