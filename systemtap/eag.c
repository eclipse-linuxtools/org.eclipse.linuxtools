#include <iostream>
#include <stdlib.h>


int callThisOnce(int);
int callThisTwice(int);
int callThisThirtyTimes(int);
int hasOneChild(int);
int hasTwoDifferentChildren(int);
int chainOfFifty(int);
void finalCall(void);
void neverCalled(void);
void abcdefghijklmnopqrstuvwxyz(void);


int main() {
	int i = 0;
	callThisOnce(i);
	callThisTwice(i);
	callThisTwice(i);
	for (i = 0; i < 30; i ++)
		callThisThirtyTimes(i);
	hasOneChild(i);
	hasTwoDifferentChildren(i);
	chainOfFifty(0);
	abcdefghijklmnopqrstuvwxyz();
	finalCall();
	return i;
}

void finalCall() {
	return;
}

void abcdefghijklmnopqrstuvwxyz() {
	return;
}


int callThisOnce(int i) {
	return i +1;
}

int callThisTwice(int i) {
	return i + 1;
}

int callThisThirtyTimes(int i) {
	return i + 1;

}

int hasOneChild(int i) {
	for (int j = 0; j < 13000; j++)
		j=j;
	callThisOnce(i);
	return i;
}

int hasTwoDifferentChildren(int i) {
	for (int j = 0; j < 13000; j++)
		j=j;
	hasOneChild(i);
	callThisOnce(i);




	return i;
}

int chainOfFifty(int i) {
	if (i < 50)
		chainOfFifty(i+1);


	for (int j = 0; j < 100; j++)
		j=j;

	return i;
}
