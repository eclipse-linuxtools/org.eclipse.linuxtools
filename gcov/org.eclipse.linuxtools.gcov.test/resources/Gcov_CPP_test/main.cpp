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
#include <iostream>
#include <stdlib.h>
#include "fact.hpp"


int main(int argc, char** argv)
{
	int i = 1;
	for (; i<argc; i++)
	{
		for (int k = 1000; k-->0;);
		unsigned long val = strtol(argv[i],NULL,10);
		F::fact f;
		unsigned long res = f.f(val);
		std::cout << val
		          << "! = "
		          << res
		          << std::endl;
	}
}
