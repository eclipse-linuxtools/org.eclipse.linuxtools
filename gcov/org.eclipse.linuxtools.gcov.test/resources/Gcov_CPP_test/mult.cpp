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
