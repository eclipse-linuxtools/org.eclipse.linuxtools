/*******************************************************************************
 * Copyright (c) 2009, 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
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
