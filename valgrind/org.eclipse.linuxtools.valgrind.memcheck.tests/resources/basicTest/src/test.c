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
#include <stdio.h>
#include <fcntl.h>

#define SIZE 10
int main() {
	char *waste = (char *)malloc(sizeof(char) * SIZE);
	int *a;
	printf("%d\n", *a);
	waste[SIZE] = 0;
	return 0;
}
