/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
#include <stdlib.h>

#define SIZE 10
int main() {
	char *waste = (char *)malloc(sizeof(char) * SIZE);
	waste[0] = waste[SIZE];
	waste[SIZE] = 0;
	return 0;
}
