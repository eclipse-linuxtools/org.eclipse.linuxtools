#include <math.h>

#define TIMEBASE 100

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

namespace D 
{
  
  class foocpp_dead
  {
  public:
    foocpp_dead()  {};
    
    ~foocpp_dead() {};
    
    int dead_func(int pf)
    {
      int rv=0;
      TEMPO(23, 350*TIMEBASE, pf, rv);   // 29
      return rv;
    };
  };
  
}

namespace A 
{
  class foocpp
  {
  public:
    
    foocpp()  {};
    
    ~foocpp()  {};
    
    int func_f(int pf)
    {                                     // *** 47
      int rv=0;
      TEMPO(12, 350*TIMEBASE, pf, rv);    // 49
      return rv;
    };
    
    int func_a(int pa)
    {                                     // *** 54
      int rv=0;
      TEMPO(7, 500*TIMEBASE, pa, rv);     // 56
      if (pa==1) 
	{ 
	  rv += func_f(1);
	  TEMPO(8, 500*TIMEBASE, pa, rv); // 60
	  rv += func_f(1);
	}
      TEMPO(6, 500*TIMEBASE, pa, rv);     // 63
      return rv;
    };
  };
  
}

namespace B
{
  
  class foocpp
  {
  private:
    A::foocpp *A;
    
  public:
    
    foocpp()  
    {
      A = new A::foocpp();
    };
    
    ~foocpp()  
    {
      delete A;
    };
    
    int func_b(int pb)
    {                                    // *** 91
      int rv=0;
      TEMPO(16, 200*TIMEBASE, pb, rv);   // 93
      
      rv += A->func_a(0);
      
      TEMPO(18, 150*TIMEBASE, pb, rv);   // 97
      return rv;
    };
  };
  
}

int main()
{                                   // *** 105
  int rv = 0;
  TEMPO(7, 250*TIMEBASE, rv, rv);   // 107
  
  B::foocpp  *B = new B::foocpp();
  A::foocpp *A  = new A::foocpp();

  rv += A->func_a(1);
  TEMPO(12, 150*TIMEBASE, rv, rv);  // 113

  rv += B->func_b(0);
  TEMPO(9, 150*TIMEBASE, rv, rv);   // 116 

  delete A;
  delete B;

  return rv;
}
