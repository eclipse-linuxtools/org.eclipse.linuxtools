#include <stdio.h>
#include <stdlib.h>
#include <iostream>

using namespace std;

long leftfib(unsigned long n);
long rightfib(unsigned long n);

long leftfib(unsigned long n) {
    if (n <= 1) {
        return n;
    } else {
        return leftfib(n-1)+rightfib(n-2);
    }
}
long rightfib(unsigned long n) {
    if (n <= 1) {
        return n;
    } else {
    	return leftfib(n-1)+rightfib(n-2);
    }
}

int main(int argc, char *argv[]) {
	if (argc != 2) {
		cout << "Not right args" << endl;
		return 1;
	}
	cout << atoi(argv[1]) << endl;
	cout << leftfib(atoi(argv[1])) << endl;
	return 0;
}
