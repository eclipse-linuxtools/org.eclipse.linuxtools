#include <math.h>

int func_f(int pf);
int func_b(int pb);
int func_a(int pa);
int dead_func(int pf);

#define TIMEBASE 300

#define TEMPO(TJ,TI, IN, OUT) { \
  volatile double d1 = 0;\
  volatile double d2 = 10.2334445;\
  int jx, ix;\
  for (jx = 0; jx < TJ; jx++) for (ix = 0; ix < TI; ix++) {\
      d1 += d2 + (double)(IN); \
      d2 += 0.121; \
  }\
  OUT+=(int)(d1+0.5);\
}

int dead_func(int pf)
{
  int rv=0;
  TEMPO(10, 350*TIMEBASE, pf, rv);
  return rv;
}

int func_f(int pf)
{                                     // *** 29
  int rv=0;
  TEMPO(12, 350*TIMEBASE, pf, rv);    // 31
  return rv;
}

int func_a(int pa)
{                                     // *** 36
  int rv=0;
  TEMPO(7, 500*TIMEBASE, pa, rv);     // 38
  if (pa==1) 
    { 
      rv += func_f(1);
      TEMPO(8, 500*TIMEBASE, pa, rv); // 42
      rv += func_f(1);
    }
  TEMPO(6, 500*TIMEBASE, pa, rv);     // 45
  return rv;
}

int func_b(int pb)
{                                    // *** 50
  int rv=0;
  TEMPO(16, 200*TIMEBASE, pb, rv);   // 52
  rv += func_a(0);
  TEMPO(18, 150*TIMEBASE, pb, rv);   // 54
  return rv;
}

int main()
{
  int rv = 0;
  TEMPO(7, 250*TIMEBASE, rv, rv);   // 61
  rv += func_a(1);
  TEMPO(12, 150*TIMEBASE, rv, rv);  // 63
  rv += func_b(0);
  TEMPO(9, 150*TIMEBASE, rv, rv);   // 65 
  return rv;
}
