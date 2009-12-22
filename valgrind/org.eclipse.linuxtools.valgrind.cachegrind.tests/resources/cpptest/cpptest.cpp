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
#include "cpptest.h"

int x = 3;

class A {
public:
	A() : y(6) {}
	int c() {
		B b = B();
		b.d();
		return b.x + y;
	}
private:
	class B {
	public:
		B() : x(5) {}
		void d() {
			x++;
			e();
		}
		int x;
	private:
		void e() {
			x--;
		}
	};
	int y;
};

int Foo::bar(int z) {
	return x + baz(y, z);
}

int Foo::baz(int a, int b) {
	return a + b;
}

int main(int argc, char **argv) {
	Foo f = Foo();
	A a = A();
	f.bar(5);
	a.c();
	return 0;
}
