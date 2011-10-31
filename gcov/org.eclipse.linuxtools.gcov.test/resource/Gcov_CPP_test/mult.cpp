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
#include "mult.hpp"

namespace M 
{
mult::mult()  {};

mult::~mult() {};

long mult::multiply(long val1, long val2)
{
	for (int k = 50; k-->0;);
	long res = 0;
	while(val2-->0)
	{
		res += val1;
	}
	return res;
};
}
