/*******************************************************************************
 * Copyright (c) 2008, 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
