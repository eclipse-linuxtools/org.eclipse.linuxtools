/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
#ifndef CPPTEST_H_
#define CPPTEST_H_

class Foo {
public:
	Foo() : y(4) {}
	int bar(int);
private:
	int baz(int, int);
	int y;
};

#endif /* CPPTEST_H_ */
