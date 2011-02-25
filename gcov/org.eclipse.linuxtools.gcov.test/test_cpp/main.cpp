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
