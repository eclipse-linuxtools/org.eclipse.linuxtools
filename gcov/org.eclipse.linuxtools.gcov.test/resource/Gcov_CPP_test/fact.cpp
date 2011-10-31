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



