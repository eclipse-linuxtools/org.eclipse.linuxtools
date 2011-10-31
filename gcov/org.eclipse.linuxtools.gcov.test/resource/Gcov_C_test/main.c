/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
