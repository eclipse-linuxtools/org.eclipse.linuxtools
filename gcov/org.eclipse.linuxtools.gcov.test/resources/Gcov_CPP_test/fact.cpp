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
#include "fact.hpp"
#include "mult.hpp"

namespace F
{

fact::fact()  {};

fact::~fact() {};

long fact::f(long val)
{
	for (int k = 100; k-->0;);
	if (val == 1) return 1;
	M::mult m;
	return m.multiply(val,f(val-1));
}
}



