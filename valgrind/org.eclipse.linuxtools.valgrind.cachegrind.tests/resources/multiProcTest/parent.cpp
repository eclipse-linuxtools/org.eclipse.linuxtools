#include <cstdio>
#include <unistd.h>

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

int main() {
	if (fork()) {
		A a = A();
		a.c();
	}
	else {
		execv("../cpptest/Debug/cpptest", NULL);
		perror("execv");
	}
	return 0;
}

