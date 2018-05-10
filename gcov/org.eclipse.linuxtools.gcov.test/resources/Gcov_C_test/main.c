/*******************************************************************************
 * Copyright (c) 2009, 2018 STMicroelectronics and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *******************************************************************************/
#include <stdio.h>
#include <stdlib.h>

long fact(long val);

int main(int argc, char** argv)
{
	int i = 1;
	for (; i<argc; i++)
	{
		unsigned long val = strtol(argv[i],NULL,10);
		unsigned long res = fact(val);
		int k = 1000;
		for (; k-->0;);
		printf("%li! = %li\n", val, res);
	}
	return 0;
}
