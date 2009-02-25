/*******************************************************************************
 * Copyright (c) 2008, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
#include <stdlib.h>
#include <unistd.h>

int main(int argc, char **argv) {
	long long bytes = 40;
	long long times = 1;
	if (argc > 1) {
		bytes = atoll(argv[1]);
		if (argc > 2) {
			times = atoll(argv[2]);
		}
	}
	else {
		bytes = 40;
	}

	while (times-- > 0) {
		int *foo();
		void bar(int *);
		int *ptr1 = (int *)malloc(bytes);
		int *ptr2 = foo();
		int *ptr3 = (int *)malloc(bytes);
		int *ptr4 = foo();
		int *ptr5 = (int *)malloc(bytes);
		int *ptr6 = foo();

		free(ptr1);
		bar(ptr2);
		free(ptr3);
		bar(ptr4);
		free(ptr5);
		bar(ptr6);
	}
	return 0;
}

int *foo(long long bytes) {
	return (int *)malloc(bytes);
}

void bar(int *ptr) {
	free(ptr);
}
