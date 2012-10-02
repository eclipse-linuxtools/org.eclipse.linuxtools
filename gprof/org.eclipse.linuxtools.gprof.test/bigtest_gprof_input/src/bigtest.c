#include <stdio.h> 

#define LATENCY_0to9(X)     {volatile int zx; for (zx=0; zx<argc*500;zx++);} 
#define LATENCY_10to99(X)   {volatile int zx; for (zx=0; zx<argc*50;zx++);}  
#define LATENCY_100to999(X) {volatile int zx; for (zx=0; zx<argc*5;zx++);}   

int func_100 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_100 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_101 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_101 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_102 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_102 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_103 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_103 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_104 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_104 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_105 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_105 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_106 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_106 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_107 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_107 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_108 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_108 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_109 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_109 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_110 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_110 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_111 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_111 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_112 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_112 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_113 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_113 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_114 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_114 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_115 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_115 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_116 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_116 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_117 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_117 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_118 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_118 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_119 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_119 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_120 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_120 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_121 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_121 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_122 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_122 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_123 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_123 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_124 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_124 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_125 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_125 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_126 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_126 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_127 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_127 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_128 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_128 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_129 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_129 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_130 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_130 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_131 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_131 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_132 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_132 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_133 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_133 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_134 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_134 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_135 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_135 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_136 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_136 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_137 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_137 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_138 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_138 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_139 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_139 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_140 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_140 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_141 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_141 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_142 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_142 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_143 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_143 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_144 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_144 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_145 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_145 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_146 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_146 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_147 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_147 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_148 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_148 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_149 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_149 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_150 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_150 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_151 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_151 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_152 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_152 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_153 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_153 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_154 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_154 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_155 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_155 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_156 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_156 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_157 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_157 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_158 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_158 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_159 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_159 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_160 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_160 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_161 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_161 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_162 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_162 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_163 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_163 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_164 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_164 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_165 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_165 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_166 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_166 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_167 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_167 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_168 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_168 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_169 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_169 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_170 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_170 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_171 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_171 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_172 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_172 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_173 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_173 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_174 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_174 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_175 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_175 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_176 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_176 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_177 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_177 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_178 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_178 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_179 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_179 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_180 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_180 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_181 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_181 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_182 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_182 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_183 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_183 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_184 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_184 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_185 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_185 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_186 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_186 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_187 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_187 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_188 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_188 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_189 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_189 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_190 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_190 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_191 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_191 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_192 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_192 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_193 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_193 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_194 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_194 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_195 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_195 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_196 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_196 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_197 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_197 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_198 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_198 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_199 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_199 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_200 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_200 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_201 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_201 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_202 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_202 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_203 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_203 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_204 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_204 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_205 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_205 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_206 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_206 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_207 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_207 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_208 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_208 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_209 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_209 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_210 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_210 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_211 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_211 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_212 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_212 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_213 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_213 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_214 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_214 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_215 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_215 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_216 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_216 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_217 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_217 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_218 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_218 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_219 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_219 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_220 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_220 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_221 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_221 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_222 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_222 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_223 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_223 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_224 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_224 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_225 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_225 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_226 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_226 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_227 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_227 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_228 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_228 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_229 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_229 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_230 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_230 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_231 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_231 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_232 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_232 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_233 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_233 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_234 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_234 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_235 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_235 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_236 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_236 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_237 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_237 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_238 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_238 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_239 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_239 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_240 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_240 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_241 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_241 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_242 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_242 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_243 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_243 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_244 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_244 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_245 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_245 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_246 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_246 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_247 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_247 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_248 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_248 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_249 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_249 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_250 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_250 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_251 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_251 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_252 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_252 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_253 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_253 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_254 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_254 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_255 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_255 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_256 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_256 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_257 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_257 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_258 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_258 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_259 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_259 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_260 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_260 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_261 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_261 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_262 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_262 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_263 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_263 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_264 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_264 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_265 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_265 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_266 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_266 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_267 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_267 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_268 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_268 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_269 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_269 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_270 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_270 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_271 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_271 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_272 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_272 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_273 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_273 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_274 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_274 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_275 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_275 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_276 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_276 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_277 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_277 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_278 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_278 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_279 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_279 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_280 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_280 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_281 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_281 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_282 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_282 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_283 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_283 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_284 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_284 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_285 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_285 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_286 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_286 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_287 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_287 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_288 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_288 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_289 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_289 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_290 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_290 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_291 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_291 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_292 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_292 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_293 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_293 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_294 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_294 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_295 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_295 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_296 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_296 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_297 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_297 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_298 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_298 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_299 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_299 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_300 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_300 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_301 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_301 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_302 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_302 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_303 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_303 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_304 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_304 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_305 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_305 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_306 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_306 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_307 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_307 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_308 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_308 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_309 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_309 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_310 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_310 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_311 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_311 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_312 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_312 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_313 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_313 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_314 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_314 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_315 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_315 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_316 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_316 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_317 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_317 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_318 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_318 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_319 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_319 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_320 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_320 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_321 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_321 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_322 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_322 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_323 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_323 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_324 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_324 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_325 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_325 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_326 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_326 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_327 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_327 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_328 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_328 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_329 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_329 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_330 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_330 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_331 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_331 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_332 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_332 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_333 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_333 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_334 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_334 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_335 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_335 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_336 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_336 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_337 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_337 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_338 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_338 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_339 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_339 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_340 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_340 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_341 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_341 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_342 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_342 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_343 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_343 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_344 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_344 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_345 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_345 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_346 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_346 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_347 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_347 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_348 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_348 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_349 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_349 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_350 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_350 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_351 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_351 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_352 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_352 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_353 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_353 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_354 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_354 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_355 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_355 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_356 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_356 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_357 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_357 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_358 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_358 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_359 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_359 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_360 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_360 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_361 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_361 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_362 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_362 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_363 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_363 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_364 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_364 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_365 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_365 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_366 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_366 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_367 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_367 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_368 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_368 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_369 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_369 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_370 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_370 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_371 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_371 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_372 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_372 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_373 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_373 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_374 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_374 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_375 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_375 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_376 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_376 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_377 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_377 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_378 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_378 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_379 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_379 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_380 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_380 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_381 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_381 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_382 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_382 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_383 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_383 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_384 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_384 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_385 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_385 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_386 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_386 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_387 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_387 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_388 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_388 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_389 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_389 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_390 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_390 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_391 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_391 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_392 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_392 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_393 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_393 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_394 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_394 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_395 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_395 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_396 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_396 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_397 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_397 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_398 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_398 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_399 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_399 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_400 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_400 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_401 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_401 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_402 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_402 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_403 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_403 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_404 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_404 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_405 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_405 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_406 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_406 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_407 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_407 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_408 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_408 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_409 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_409 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_410 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_410 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_411 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_411 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_412 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_412 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_413 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_413 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_414 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_414 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_415 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_415 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_416 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_416 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_417 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_417 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_418 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_418 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_419 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_419 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_420 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_420 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_421 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_421 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_422 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_422 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_423 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_423 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_424 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_424 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_425 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_425 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_426 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_426 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_427 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_427 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_428 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_428 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_429 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_429 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_430 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_430 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_431 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_431 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_432 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_432 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_433 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_433 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_434 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_434 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_435 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_435 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_436 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_436 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_437 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_437 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_438 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_438 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_439 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_439 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_440 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_440 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_441 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_441 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_442 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_442 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_443 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_443 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_444 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_444 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_445 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_445 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_446 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_446 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_447 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_447 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_448 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_448 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_449 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_449 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_450 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_450 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_451 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_451 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_452 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_452 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_453 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_453 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_454 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_454 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_455 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_455 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_456 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_456 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_457 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_457 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_458 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_458 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_459 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_459 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_460 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_460 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_461 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_461 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_462 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_462 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_463 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_463 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_464 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_464 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_465 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_465 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_466 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_466 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_467 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_467 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_468 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_468 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_469 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_469 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_470 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_470 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_471 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_471 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_472 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_472 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_473 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_473 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_474 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_474 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_475 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_475 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_476 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_476 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_477 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_477 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_478 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_478 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_479 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_479 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_480 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_480 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_481 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_481 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_482 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_482 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_483 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_483 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_484 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_484 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_485 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_485 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_486 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_486 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_487 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_487 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_488 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_488 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_489 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_489 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_490 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_490 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_491 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_491 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_492 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_492 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_493 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_493 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_494 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_494 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_495 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_495 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_496 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_496 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_497 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_497 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_498 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_498 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_499 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_499 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_500 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_500 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_501 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_501 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_502 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_502 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_503 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_503 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_504 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_504 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_505 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_505 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_506 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_506 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_507 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_507 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_508 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_508 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_509 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_509 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_510 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_510 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_511 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_511 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_512 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_512 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_513 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_513 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_514 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_514 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_515 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_515 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_516 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_516 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_517 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_517 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_518 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_518 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_519 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_519 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_520 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_520 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_521 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_521 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_522 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_522 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_523 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_523 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_524 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_524 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_525 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_525 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_526 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_526 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_527 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_527 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_528 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_528 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_529 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_529 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_530 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_530 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_531 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_531 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_532 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_532 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_533 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_533 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_534 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_534 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_535 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_535 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_536 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_536 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_537 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_537 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_538 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_538 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_539 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_539 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_540 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_540 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_541 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_541 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_542 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_542 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_543 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_543 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_544 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_544 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_545 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_545 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_546 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_546 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_547 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_547 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_548 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_548 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_549 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_549 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_550 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_550 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_551 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_551 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_552 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_552 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_553 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_553 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_554 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_554 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_555 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_555 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_556 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_556 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_557 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_557 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_558 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_558 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_559 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_559 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_560 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_560 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_561 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_561 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_562 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_562 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_563 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_563 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_564 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_564 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_565 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_565 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_566 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_566 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_567 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_567 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_568 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_568 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_569 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_569 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_570 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_570 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_571 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_571 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_572 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_572 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_573 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_573 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_574 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_574 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_575 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_575 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_576 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_576 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_577 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_577 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_578 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_578 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_579 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_579 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_580 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_580 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_581 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_581 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_582 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_582 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_583 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_583 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_584 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_584 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_585 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_585 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_586 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_586 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_587 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_587 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_588 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_588 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_589 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_589 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_590 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_590 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_591 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_591 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_592 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_592 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_593 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_593 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_594 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_594 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_595 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_595 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_596 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_596 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_597 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_597 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_598 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_598 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_599 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_599 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_600 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_600 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_601 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_601 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_602 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_602 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_603 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_603 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_604 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_604 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_605 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_605 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_606 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_606 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_607 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_607 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_608 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_608 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_609 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_609 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_610 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_610 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_611 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_611 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_612 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_612 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_613 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_613 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_614 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_614 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_615 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_615 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_616 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_616 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_617 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_617 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_618 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_618 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_619 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_619 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_620 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_620 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_621 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_621 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_622 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_622 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_623 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_623 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_624 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_624 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_625 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_625 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_626 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_626 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_627 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_627 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_628 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_628 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_629 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_629 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_630 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_630 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_631 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_631 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_632 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_632 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_633 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_633 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_634 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_634 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_635 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_635 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_636 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_636 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_637 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_637 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_638 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_638 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_639 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_639 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_640 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_640 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_641 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_641 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_642 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_642 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_643 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_643 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_644 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_644 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_645 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_645 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_646 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_646 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_647 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_647 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_648 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_648 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_649 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_649 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_650 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_650 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_651 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_651 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_652 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_652 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_653 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_653 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_654 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_654 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_655 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_655 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_656 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_656 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_657 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_657 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_658 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_658 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_659 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_659 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_660 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_660 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_661 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_661 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_662 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_662 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_663 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_663 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_664 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_664 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_665 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_665 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_666 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_666 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_667 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_667 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_668 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_668 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_669 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_669 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_670 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_670 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_671 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_671 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_672 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_672 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_673 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_673 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_674 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_674 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_675 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_675 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_676 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_676 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_677 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_677 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_678 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_678 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_679 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_679 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_680 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_680 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_681 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_681 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_682 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_682 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_683 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_683 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_684 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_684 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_685 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_685 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_686 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_686 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_687 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_687 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_688 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_688 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_689 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_689 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_690 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_690 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_691 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_691 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_692 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_692 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_693 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_693 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_694 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_694 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_695 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_695 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_696 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_696 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_697 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_697 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_698 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_698 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_699 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_699 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_700 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_700 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_701 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_701 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_702 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_702 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_703 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_703 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_704 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_704 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_705 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_705 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_706 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_706 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_707 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_707 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_708 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_708 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_709 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_709 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_710 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_710 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_711 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_711 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_712 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_712 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_713 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_713 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_714 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_714 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_715 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_715 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_716 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_716 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_717 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_717 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_718 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_718 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_719 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_719 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_720 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_720 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_721 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_721 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_722 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_722 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_723 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_723 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_724 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_724 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_725 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_725 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_726 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_726 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_727 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_727 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_728 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_728 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_729 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_729 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_730 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_730 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_731 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_731 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_732 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_732 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_733 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_733 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_734 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_734 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_735 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_735 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_736 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_736 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_737 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_737 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_738 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_738 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_739 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_739 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_740 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_740 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_741 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_741 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_742 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_742 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_743 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_743 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_744 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_744 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_745 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_745 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_746 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_746 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_747 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_747 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_748 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_748 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_749 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_749 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_750 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_750 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_751 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_751 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_752 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_752 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_753 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_753 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_754 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_754 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_755 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_755 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_756 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_756 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_757 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_757 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_758 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_758 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_759 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_759 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_760 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_760 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_761 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_761 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_762 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_762 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_763 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_763 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_764 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_764 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_765 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_765 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_766 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_766 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_767 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_767 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_768 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_768 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_769 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_769 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_770 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_770 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_771 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_771 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_772 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_772 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_773 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_773 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_774 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_774 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_775 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_775 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_776 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_776 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_777 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_777 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_778 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_778 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_779 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_779 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_780 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_780 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_781 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_781 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_782 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_782 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_783 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_783 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_784 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_784 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_785 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_785 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_786 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_786 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_787 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_787 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_788 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_788 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_789 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_789 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_790 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_790 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_791 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_791 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_792 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_792 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_793 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_793 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_794 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_794 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_795 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_795 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_796 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_796 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_797 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_797 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_798 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_798 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_799 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_799 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_800 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_800 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_801 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_801 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_802 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_802 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_803 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_803 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_804 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_804 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_805 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_805 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_806 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_806 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_807 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_807 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_808 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_808 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_809 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_809 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_810 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_810 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_811 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_811 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_812 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_812 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_813 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_813 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_814 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_814 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_815 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_815 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_816 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_816 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_817 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_817 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_818 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_818 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_819 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_819 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_820 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_820 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_821 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_821 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_822 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_822 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_823 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_823 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_824 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_824 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_825 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_825 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_826 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_826 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_827 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_827 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_828 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_828 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_829 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_829 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_830 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_830 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_831 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_831 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_832 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_832 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_833 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_833 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_834 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_834 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_835 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_835 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_836 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_836 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_837 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_837 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_838 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_838 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_839 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_839 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_840 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_840 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_841 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_841 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_842 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_842 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_843 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_843 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_844 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_844 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_845 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_845 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_846 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_846 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_847 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_847 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_848 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_848 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_849 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_849 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_850 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_850 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_851 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_851 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_852 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_852 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_853 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_853 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_854 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_854 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_855 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_855 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_856 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_856 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_857 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_857 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_858 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_858 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_859 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_859 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_860 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_860 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_861 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_861 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_862 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_862 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_863 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_863 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_864 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_864 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_865 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_865 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_866 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_866 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_867 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_867 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_868 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_868 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_869 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_869 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_870 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_870 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_871 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_871 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_872 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_872 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_873 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_873 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_874 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_874 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_875 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_875 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_876 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_876 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_877 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_877 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_878 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_878 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_879 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_879 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_880 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_880 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_881 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_881 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_882 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_882 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_883 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_883 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_884 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_884 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_885 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_885 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_886 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_886 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_887 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_887 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_888 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_888 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_889 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_889 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_890 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_890 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_891 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_891 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_892 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_892 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_893 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_893 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_894 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_894 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_895 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_895 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_896 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_896 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_897 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_897 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_898 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_898 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_899 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_899 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_900 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_900 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_901 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_901 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_902 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_902 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_903 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_903 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_904 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_904 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_905 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_905 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_906 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_906 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_907 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_907 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_908 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_908 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_909 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_909 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_910 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_910 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_911 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_911 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_912 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_912 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_913 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_913 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_914 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_914 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_915 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_915 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_916 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_916 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_917 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_917 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_918 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_918 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_919 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_919 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_920 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_920 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_921 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_921 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_922 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_922 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_923 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_923 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_924 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_924 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_925 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_925 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_926 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_926 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_927 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_927 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_928 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_928 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_929 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_929 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_930 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_930 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_931 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_931 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_932 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_932 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_933 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_933 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_934 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_934 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_935 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_935 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_936 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_936 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_937 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_937 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_938 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_938 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_939 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_939 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_940 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_940 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_941 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_941 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_942 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_942 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_943 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_943 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_944 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_944 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_945 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_945 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_946 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_946 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_947 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_947 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_948 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_948 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_949 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_949 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_950 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_950 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_951 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_951 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_952 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_952 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_953 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_953 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_954 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_954 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_955 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_955 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_956 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_956 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_957 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_957 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_958 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_958 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_959 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_959 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_960 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_960 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_961 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_961 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_962 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_962 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_963 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_963 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_964 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_964 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_965 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_965 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_966 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_966 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_967 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_967 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_968 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_968 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_969 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_969 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_970 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_970 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_971 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_971 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_972 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_972 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_973 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_973 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_974 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_974 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_975 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_975 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_976 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_976 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_977 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_977 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_978 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_978 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_979 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_979 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_980 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_980 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_981 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_981 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_982 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_982 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_983 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_983 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_984 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_984 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_985 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_985 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_986 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_986 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_987 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_987 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_988 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_988 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_989 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_989 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_990 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_990 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_991 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_991 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_992 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_992 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_993 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_993 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_994 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_994 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_995 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_995 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_996 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_996 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_997 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_997 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_998 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_998 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_999 (int argc, char** argv) 
{ 
  int rv = 0; 
  volatile int ix2; 
  volatile int lx2 = 10*argc; 
  volatile int rx2 = 0; 
  printf ("    func_999 \n");
  LATENCY_100to999(argc)
  for (ix2=0; ix2<lx2; ix2++) 
  { 
    rx2 += ix2; 
  } 
  return rv; 
} 


int func_010 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_010 \n");
  LATENCY_10to99(argc)
  func_100(100, NULL); 
  LATENCY_10to99(argc)
  func_101(101, NULL); 
  LATENCY_10to99(argc)
  func_102(102, NULL); 
  LATENCY_10to99(argc)
  func_103(103, NULL); 
  LATENCY_10to99(argc)
  func_104(104, NULL); 
  LATENCY_10to99(argc)
  func_105(105, NULL); 
  LATENCY_10to99(argc)
  func_106(106, NULL); 
  LATENCY_10to99(argc)
  func_107(107, NULL); 
  LATENCY_10to99(argc)
  func_108(108, NULL); 
  LATENCY_10to99(argc)
  func_109(109, NULL); 
  return rv; 
} 


int func_011 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_011 \n");
  LATENCY_10to99(argc)
  func_110(110, NULL); 
  LATENCY_10to99(argc)
  func_111(111, NULL); 
  LATENCY_10to99(argc)
  func_112(112, NULL); 
  LATENCY_10to99(argc)
  func_113(113, NULL); 
  LATENCY_10to99(argc)
  func_114(114, NULL); 
  LATENCY_10to99(argc)
  func_115(115, NULL); 
  LATENCY_10to99(argc)
  func_116(116, NULL); 
  LATENCY_10to99(argc)
  func_117(117, NULL); 
  LATENCY_10to99(argc)
  func_118(118, NULL); 
  LATENCY_10to99(argc)
  func_119(119, NULL); 
  return rv; 
} 


int func_012 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_012 \n");
  LATENCY_10to99(argc)
  func_120(120, NULL); 
  LATENCY_10to99(argc)
  func_121(121, NULL); 
  LATENCY_10to99(argc)
  func_122(122, NULL); 
  LATENCY_10to99(argc)
  func_123(123, NULL); 
  LATENCY_10to99(argc)
  func_124(124, NULL); 
  LATENCY_10to99(argc)
  func_125(125, NULL); 
  LATENCY_10to99(argc)
  func_126(126, NULL); 
  LATENCY_10to99(argc)
  func_127(127, NULL); 
  LATENCY_10to99(argc)
  func_128(128, NULL); 
  LATENCY_10to99(argc)
  func_129(129, NULL); 
  return rv; 
} 


int func_013 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_013 \n");
  LATENCY_10to99(argc)
  func_130(130, NULL); 
  LATENCY_10to99(argc)
  func_131(131, NULL); 
  LATENCY_10to99(argc)
  func_132(132, NULL); 
  LATENCY_10to99(argc)
  func_133(133, NULL); 
  LATENCY_10to99(argc)
  func_134(134, NULL); 
  LATENCY_10to99(argc)
  func_135(135, NULL); 
  LATENCY_10to99(argc)
  func_136(136, NULL); 
  LATENCY_10to99(argc)
  func_137(137, NULL); 
  LATENCY_10to99(argc)
  func_138(138, NULL); 
  LATENCY_10to99(argc)
  func_139(139, NULL); 
  return rv; 
} 


int func_014 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_014 \n");
  LATENCY_10to99(argc)
  func_140(140, NULL); 
  LATENCY_10to99(argc)
  func_141(141, NULL); 
  LATENCY_10to99(argc)
  func_142(142, NULL); 
  LATENCY_10to99(argc)
  func_143(143, NULL); 
  LATENCY_10to99(argc)
  func_144(144, NULL); 
  LATENCY_10to99(argc)
  func_145(145, NULL); 
  LATENCY_10to99(argc)
  func_146(146, NULL); 
  LATENCY_10to99(argc)
  func_147(147, NULL); 
  LATENCY_10to99(argc)
  func_148(148, NULL); 
  LATENCY_10to99(argc)
  func_149(149, NULL); 
  return rv; 
} 


int func_015 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_015 \n");
  LATENCY_10to99(argc)
  func_150(150, NULL); 
  LATENCY_10to99(argc)
  func_151(151, NULL); 
  LATENCY_10to99(argc)
  func_152(152, NULL); 
  LATENCY_10to99(argc)
  func_153(153, NULL); 
  LATENCY_10to99(argc)
  func_154(154, NULL); 
  LATENCY_10to99(argc)
  func_155(155, NULL); 
  LATENCY_10to99(argc)
  func_156(156, NULL); 
  LATENCY_10to99(argc)
  func_157(157, NULL); 
  LATENCY_10to99(argc)
  func_158(158, NULL); 
  LATENCY_10to99(argc)
  func_159(159, NULL); 
  return rv; 
} 


int func_016 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_016 \n");
  LATENCY_10to99(argc)
  func_160(160, NULL); 
  LATENCY_10to99(argc)
  func_161(161, NULL); 
  LATENCY_10to99(argc)
  func_162(162, NULL); 
  LATENCY_10to99(argc)
  func_163(163, NULL); 
  LATENCY_10to99(argc)
  func_164(164, NULL); 
  LATENCY_10to99(argc)
  func_165(165, NULL); 
  LATENCY_10to99(argc)
  func_166(166, NULL); 
  LATENCY_10to99(argc)
  func_167(167, NULL); 
  LATENCY_10to99(argc)
  func_168(168, NULL); 
  LATENCY_10to99(argc)
  func_169(169, NULL); 
  return rv; 
} 


int func_017 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_017 \n");
  LATENCY_10to99(argc)
  func_170(170, NULL); 
  LATENCY_10to99(argc)
  func_171(171, NULL); 
  LATENCY_10to99(argc)
  func_172(172, NULL); 
  LATENCY_10to99(argc)
  func_173(173, NULL); 
  LATENCY_10to99(argc)
  func_174(174, NULL); 
  LATENCY_10to99(argc)
  func_175(175, NULL); 
  LATENCY_10to99(argc)
  func_176(176, NULL); 
  LATENCY_10to99(argc)
  func_177(177, NULL); 
  LATENCY_10to99(argc)
  func_178(178, NULL); 
  LATENCY_10to99(argc)
  func_179(179, NULL); 
  return rv; 
} 


int func_018 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_018 \n");
  LATENCY_10to99(argc)
  func_180(180, NULL); 
  LATENCY_10to99(argc)
  func_181(181, NULL); 
  LATENCY_10to99(argc)
  func_182(182, NULL); 
  LATENCY_10to99(argc)
  func_183(183, NULL); 
  LATENCY_10to99(argc)
  func_184(184, NULL); 
  LATENCY_10to99(argc)
  func_185(185, NULL); 
  LATENCY_10to99(argc)
  func_186(186, NULL); 
  LATENCY_10to99(argc)
  func_187(187, NULL); 
  LATENCY_10to99(argc)
  func_188(188, NULL); 
  LATENCY_10to99(argc)
  func_189(189, NULL); 
  return rv; 
} 


int func_019 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_019 \n");
  LATENCY_10to99(argc)
  func_190(190, NULL); 
  LATENCY_10to99(argc)
  func_191(191, NULL); 
  LATENCY_10to99(argc)
  func_192(192, NULL); 
  LATENCY_10to99(argc)
  func_193(193, NULL); 
  LATENCY_10to99(argc)
  func_194(194, NULL); 
  LATENCY_10to99(argc)
  func_195(195, NULL); 
  LATENCY_10to99(argc)
  func_196(196, NULL); 
  LATENCY_10to99(argc)
  func_197(197, NULL); 
  LATENCY_10to99(argc)
  func_198(198, NULL); 
  LATENCY_10to99(argc)
  func_199(199, NULL); 
  return rv; 
} 


int func_020 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_020 \n");
  LATENCY_10to99(argc)
  func_200(200, NULL); 
  LATENCY_10to99(argc)
  func_201(201, NULL); 
  LATENCY_10to99(argc)
  func_202(202, NULL); 
  LATENCY_10to99(argc)
  func_203(203, NULL); 
  LATENCY_10to99(argc)
  func_204(204, NULL); 
  LATENCY_10to99(argc)
  func_205(205, NULL); 
  LATENCY_10to99(argc)
  func_206(206, NULL); 
  LATENCY_10to99(argc)
  func_207(207, NULL); 
  LATENCY_10to99(argc)
  func_208(208, NULL); 
  LATENCY_10to99(argc)
  func_209(209, NULL); 
  return rv; 
} 


int func_021 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_021 \n");
  LATENCY_10to99(argc)
  func_210(210, NULL); 
  LATENCY_10to99(argc)
  func_211(211, NULL); 
  LATENCY_10to99(argc)
  func_212(212, NULL); 
  LATENCY_10to99(argc)
  func_213(213, NULL); 
  LATENCY_10to99(argc)
  func_214(214, NULL); 
  LATENCY_10to99(argc)
  func_215(215, NULL); 
  LATENCY_10to99(argc)
  func_216(216, NULL); 
  LATENCY_10to99(argc)
  func_217(217, NULL); 
  LATENCY_10to99(argc)
  func_218(218, NULL); 
  LATENCY_10to99(argc)
  func_219(219, NULL); 
  return rv; 
} 


int func_022 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_022 \n");
  LATENCY_10to99(argc)
  func_220(220, NULL); 
  LATENCY_10to99(argc)
  func_221(221, NULL); 
  LATENCY_10to99(argc)
  func_222(222, NULL); 
  LATENCY_10to99(argc)
  func_223(223, NULL); 
  LATENCY_10to99(argc)
  func_224(224, NULL); 
  LATENCY_10to99(argc)
  func_225(225, NULL); 
  LATENCY_10to99(argc)
  func_226(226, NULL); 
  LATENCY_10to99(argc)
  func_227(227, NULL); 
  LATENCY_10to99(argc)
  func_228(228, NULL); 
  LATENCY_10to99(argc)
  func_229(229, NULL); 
  return rv; 
} 


int func_023 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_023 \n");
  LATENCY_10to99(argc)
  func_230(230, NULL); 
  LATENCY_10to99(argc)
  func_231(231, NULL); 
  LATENCY_10to99(argc)
  func_232(232, NULL); 
  LATENCY_10to99(argc)
  func_233(233, NULL); 
  LATENCY_10to99(argc)
  func_234(234, NULL); 
  LATENCY_10to99(argc)
  func_235(235, NULL); 
  LATENCY_10to99(argc)
  func_236(236, NULL); 
  LATENCY_10to99(argc)
  func_237(237, NULL); 
  LATENCY_10to99(argc)
  func_238(238, NULL); 
  LATENCY_10to99(argc)
  func_239(239, NULL); 
  return rv; 
} 


int func_024 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_024 \n");
  LATENCY_10to99(argc)
  func_240(240, NULL); 
  LATENCY_10to99(argc)
  func_241(241, NULL); 
  LATENCY_10to99(argc)
  func_242(242, NULL); 
  LATENCY_10to99(argc)
  func_243(243, NULL); 
  LATENCY_10to99(argc)
  func_244(244, NULL); 
  LATENCY_10to99(argc)
  func_245(245, NULL); 
  LATENCY_10to99(argc)
  func_246(246, NULL); 
  LATENCY_10to99(argc)
  func_247(247, NULL); 
  LATENCY_10to99(argc)
  func_248(248, NULL); 
  LATENCY_10to99(argc)
  func_249(249, NULL); 
  return rv; 
} 


int func_025 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_025 \n");
  LATENCY_10to99(argc)
  func_250(250, NULL); 
  LATENCY_10to99(argc)
  func_251(251, NULL); 
  LATENCY_10to99(argc)
  func_252(252, NULL); 
  LATENCY_10to99(argc)
  func_253(253, NULL); 
  LATENCY_10to99(argc)
  func_254(254, NULL); 
  LATENCY_10to99(argc)
  func_255(255, NULL); 
  LATENCY_10to99(argc)
  func_256(256, NULL); 
  LATENCY_10to99(argc)
  func_257(257, NULL); 
  LATENCY_10to99(argc)
  func_258(258, NULL); 
  LATENCY_10to99(argc)
  func_259(259, NULL); 
  return rv; 
} 


int func_026 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_026 \n");
  LATENCY_10to99(argc)
  func_260(260, NULL); 
  LATENCY_10to99(argc)
  func_261(261, NULL); 
  LATENCY_10to99(argc)
  func_262(262, NULL); 
  LATENCY_10to99(argc)
  func_263(263, NULL); 
  LATENCY_10to99(argc)
  func_264(264, NULL); 
  LATENCY_10to99(argc)
  func_265(265, NULL); 
  LATENCY_10to99(argc)
  func_266(266, NULL); 
  LATENCY_10to99(argc)
  func_267(267, NULL); 
  LATENCY_10to99(argc)
  func_268(268, NULL); 
  LATENCY_10to99(argc)
  func_269(269, NULL); 
  return rv; 
} 


int func_027 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_027 \n");
  LATENCY_10to99(argc)
  func_270(270, NULL); 
  LATENCY_10to99(argc)
  func_271(271, NULL); 
  LATENCY_10to99(argc)
  func_272(272, NULL); 
  LATENCY_10to99(argc)
  func_273(273, NULL); 
  LATENCY_10to99(argc)
  func_274(274, NULL); 
  LATENCY_10to99(argc)
  func_275(275, NULL); 
  LATENCY_10to99(argc)
  func_276(276, NULL); 
  LATENCY_10to99(argc)
  func_277(277, NULL); 
  LATENCY_10to99(argc)
  func_278(278, NULL); 
  LATENCY_10to99(argc)
  func_279(279, NULL); 
  return rv; 
} 


int func_028 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_028 \n");
  LATENCY_10to99(argc)
  func_280(280, NULL); 
  LATENCY_10to99(argc)
  func_281(281, NULL); 
  LATENCY_10to99(argc)
  func_282(282, NULL); 
  LATENCY_10to99(argc)
  func_283(283, NULL); 
  LATENCY_10to99(argc)
  func_284(284, NULL); 
  LATENCY_10to99(argc)
  func_285(285, NULL); 
  LATENCY_10to99(argc)
  func_286(286, NULL); 
  LATENCY_10to99(argc)
  func_287(287, NULL); 
  LATENCY_10to99(argc)
  func_288(288, NULL); 
  LATENCY_10to99(argc)
  func_289(289, NULL); 
  return rv; 
} 


int func_029 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_029 \n");
  LATENCY_10to99(argc)
  func_290(290, NULL); 
  LATENCY_10to99(argc)
  func_291(291, NULL); 
  LATENCY_10to99(argc)
  func_292(292, NULL); 
  LATENCY_10to99(argc)
  func_293(293, NULL); 
  LATENCY_10to99(argc)
  func_294(294, NULL); 
  LATENCY_10to99(argc)
  func_295(295, NULL); 
  LATENCY_10to99(argc)
  func_296(296, NULL); 
  LATENCY_10to99(argc)
  func_297(297, NULL); 
  LATENCY_10to99(argc)
  func_298(298, NULL); 
  LATENCY_10to99(argc)
  func_299(299, NULL); 
  return rv; 
} 


int func_030 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_030 \n");
  LATENCY_10to99(argc)
  func_300(300, NULL); 
  LATENCY_10to99(argc)
  func_301(301, NULL); 
  LATENCY_10to99(argc)
  func_302(302, NULL); 
  LATENCY_10to99(argc)
  func_303(303, NULL); 
  LATENCY_10to99(argc)
  func_304(304, NULL); 
  LATENCY_10to99(argc)
  func_305(305, NULL); 
  LATENCY_10to99(argc)
  func_306(306, NULL); 
  LATENCY_10to99(argc)
  func_307(307, NULL); 
  LATENCY_10to99(argc)
  func_308(308, NULL); 
  LATENCY_10to99(argc)
  func_309(309, NULL); 
  return rv; 
} 


int func_031 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_031 \n");
  LATENCY_10to99(argc)
  func_310(310, NULL); 
  LATENCY_10to99(argc)
  func_311(311, NULL); 
  LATENCY_10to99(argc)
  func_312(312, NULL); 
  LATENCY_10to99(argc)
  func_313(313, NULL); 
  LATENCY_10to99(argc)
  func_314(314, NULL); 
  LATENCY_10to99(argc)
  func_315(315, NULL); 
  LATENCY_10to99(argc)
  func_316(316, NULL); 
  LATENCY_10to99(argc)
  func_317(317, NULL); 
  LATENCY_10to99(argc)
  func_318(318, NULL); 
  LATENCY_10to99(argc)
  func_319(319, NULL); 
  return rv; 
} 


int func_032 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_032 \n");
  LATENCY_10to99(argc)
  func_320(320, NULL); 
  LATENCY_10to99(argc)
  func_321(321, NULL); 
  LATENCY_10to99(argc)
  func_322(322, NULL); 
  LATENCY_10to99(argc)
  func_323(323, NULL); 
  LATENCY_10to99(argc)
  func_324(324, NULL); 
  LATENCY_10to99(argc)
  func_325(325, NULL); 
  LATENCY_10to99(argc)
  func_326(326, NULL); 
  LATENCY_10to99(argc)
  func_327(327, NULL); 
  LATENCY_10to99(argc)
  func_328(328, NULL); 
  LATENCY_10to99(argc)
  func_329(329, NULL); 
  return rv; 
} 


int func_033 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_033 \n");
  LATENCY_10to99(argc)
  func_330(330, NULL); 
  LATENCY_10to99(argc)
  func_331(331, NULL); 
  LATENCY_10to99(argc)
  func_332(332, NULL); 
  LATENCY_10to99(argc)
  func_333(333, NULL); 
  LATENCY_10to99(argc)
  func_334(334, NULL); 
  LATENCY_10to99(argc)
  func_335(335, NULL); 
  LATENCY_10to99(argc)
  func_336(336, NULL); 
  LATENCY_10to99(argc)
  func_337(337, NULL); 
  LATENCY_10to99(argc)
  func_338(338, NULL); 
  LATENCY_10to99(argc)
  func_339(339, NULL); 
  return rv; 
} 


int func_034 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_034 \n");
  LATENCY_10to99(argc)
  func_340(340, NULL); 
  LATENCY_10to99(argc)
  func_341(341, NULL); 
  LATENCY_10to99(argc)
  func_342(342, NULL); 
  LATENCY_10to99(argc)
  func_343(343, NULL); 
  LATENCY_10to99(argc)
  func_344(344, NULL); 
  LATENCY_10to99(argc)
  func_345(345, NULL); 
  LATENCY_10to99(argc)
  func_346(346, NULL); 
  LATENCY_10to99(argc)
  func_347(347, NULL); 
  LATENCY_10to99(argc)
  func_348(348, NULL); 
  LATENCY_10to99(argc)
  func_349(349, NULL); 
  return rv; 
} 


int func_035 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_035 \n");
  LATENCY_10to99(argc)
  func_350(350, NULL); 
  LATENCY_10to99(argc)
  func_351(351, NULL); 
  LATENCY_10to99(argc)
  func_352(352, NULL); 
  LATENCY_10to99(argc)
  func_353(353, NULL); 
  LATENCY_10to99(argc)
  func_354(354, NULL); 
  LATENCY_10to99(argc)
  func_355(355, NULL); 
  LATENCY_10to99(argc)
  func_356(356, NULL); 
  LATENCY_10to99(argc)
  func_357(357, NULL); 
  LATENCY_10to99(argc)
  func_358(358, NULL); 
  LATENCY_10to99(argc)
  func_359(359, NULL); 
  return rv; 
} 


int func_036 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_036 \n");
  LATENCY_10to99(argc)
  func_360(360, NULL); 
  LATENCY_10to99(argc)
  func_361(361, NULL); 
  LATENCY_10to99(argc)
  func_362(362, NULL); 
  LATENCY_10to99(argc)
  func_363(363, NULL); 
  LATENCY_10to99(argc)
  func_364(364, NULL); 
  LATENCY_10to99(argc)
  func_365(365, NULL); 
  LATENCY_10to99(argc)
  func_366(366, NULL); 
  LATENCY_10to99(argc)
  func_367(367, NULL); 
  LATENCY_10to99(argc)
  func_368(368, NULL); 
  LATENCY_10to99(argc)
  func_369(369, NULL); 
  return rv; 
} 


int func_037 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_037 \n");
  LATENCY_10to99(argc)
  func_370(370, NULL); 
  LATENCY_10to99(argc)
  func_371(371, NULL); 
  LATENCY_10to99(argc)
  func_372(372, NULL); 
  LATENCY_10to99(argc)
  func_373(373, NULL); 
  LATENCY_10to99(argc)
  func_374(374, NULL); 
  LATENCY_10to99(argc)
  func_375(375, NULL); 
  LATENCY_10to99(argc)
  func_376(376, NULL); 
  LATENCY_10to99(argc)
  func_377(377, NULL); 
  LATENCY_10to99(argc)
  func_378(378, NULL); 
  LATENCY_10to99(argc)
  func_379(379, NULL); 
  return rv; 
} 


int func_038 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_038 \n");
  LATENCY_10to99(argc)
  func_380(380, NULL); 
  LATENCY_10to99(argc)
  func_381(381, NULL); 
  LATENCY_10to99(argc)
  func_382(382, NULL); 
  LATENCY_10to99(argc)
  func_383(383, NULL); 
  LATENCY_10to99(argc)
  func_384(384, NULL); 
  LATENCY_10to99(argc)
  func_385(385, NULL); 
  LATENCY_10to99(argc)
  func_386(386, NULL); 
  LATENCY_10to99(argc)
  func_387(387, NULL); 
  LATENCY_10to99(argc)
  func_388(388, NULL); 
  LATENCY_10to99(argc)
  func_389(389, NULL); 
  return rv; 
} 


int func_039 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_039 \n");
  LATENCY_10to99(argc)
  func_390(390, NULL); 
  LATENCY_10to99(argc)
  func_391(391, NULL); 
  LATENCY_10to99(argc)
  func_392(392, NULL); 
  LATENCY_10to99(argc)
  func_393(393, NULL); 
  LATENCY_10to99(argc)
  func_394(394, NULL); 
  LATENCY_10to99(argc)
  func_395(395, NULL); 
  LATENCY_10to99(argc)
  func_396(396, NULL); 
  LATENCY_10to99(argc)
  func_397(397, NULL); 
  LATENCY_10to99(argc)
  func_398(398, NULL); 
  LATENCY_10to99(argc)
  func_399(399, NULL); 
  return rv; 
} 


int func_040 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_040 \n");
  LATENCY_10to99(argc)
  func_400(400, NULL); 
  LATENCY_10to99(argc)
  func_401(401, NULL); 
  LATENCY_10to99(argc)
  func_402(402, NULL); 
  LATENCY_10to99(argc)
  func_403(403, NULL); 
  LATENCY_10to99(argc)
  func_404(404, NULL); 
  LATENCY_10to99(argc)
  func_405(405, NULL); 
  LATENCY_10to99(argc)
  func_406(406, NULL); 
  LATENCY_10to99(argc)
  func_407(407, NULL); 
  LATENCY_10to99(argc)
  func_408(408, NULL); 
  LATENCY_10to99(argc)
  func_409(409, NULL); 
  return rv; 
} 


int func_041 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_041 \n");
  LATENCY_10to99(argc)
  func_410(410, NULL); 
  LATENCY_10to99(argc)
  func_411(411, NULL); 
  LATENCY_10to99(argc)
  func_412(412, NULL); 
  LATENCY_10to99(argc)
  func_413(413, NULL); 
  LATENCY_10to99(argc)
  func_414(414, NULL); 
  LATENCY_10to99(argc)
  func_415(415, NULL); 
  LATENCY_10to99(argc)
  func_416(416, NULL); 
  LATENCY_10to99(argc)
  func_417(417, NULL); 
  LATENCY_10to99(argc)
  func_418(418, NULL); 
  LATENCY_10to99(argc)
  func_419(419, NULL); 
  return rv; 
} 


int func_042 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_042 \n");
  LATENCY_10to99(argc)
  func_420(420, NULL); 
  LATENCY_10to99(argc)
  func_421(421, NULL); 
  LATENCY_10to99(argc)
  func_422(422, NULL); 
  LATENCY_10to99(argc)
  func_423(423, NULL); 
  LATENCY_10to99(argc)
  func_424(424, NULL); 
  LATENCY_10to99(argc)
  func_425(425, NULL); 
  LATENCY_10to99(argc)
  func_426(426, NULL); 
  LATENCY_10to99(argc)
  func_427(427, NULL); 
  LATENCY_10to99(argc)
  func_428(428, NULL); 
  LATENCY_10to99(argc)
  func_429(429, NULL); 
  return rv; 
} 


int func_043 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_043 \n");
  LATENCY_10to99(argc)
  func_430(430, NULL); 
  LATENCY_10to99(argc)
  func_431(431, NULL); 
  LATENCY_10to99(argc)
  func_432(432, NULL); 
  LATENCY_10to99(argc)
  func_433(433, NULL); 
  LATENCY_10to99(argc)
  func_434(434, NULL); 
  LATENCY_10to99(argc)
  func_435(435, NULL); 
  LATENCY_10to99(argc)
  func_436(436, NULL); 
  LATENCY_10to99(argc)
  func_437(437, NULL); 
  LATENCY_10to99(argc)
  func_438(438, NULL); 
  LATENCY_10to99(argc)
  func_439(439, NULL); 
  return rv; 
} 


int func_044 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_044 \n");
  LATENCY_10to99(argc)
  func_440(440, NULL); 
  LATENCY_10to99(argc)
  func_441(441, NULL); 
  LATENCY_10to99(argc)
  func_442(442, NULL); 
  LATENCY_10to99(argc)
  func_443(443, NULL); 
  LATENCY_10to99(argc)
  func_444(444, NULL); 
  LATENCY_10to99(argc)
  func_445(445, NULL); 
  LATENCY_10to99(argc)
  func_446(446, NULL); 
  LATENCY_10to99(argc)
  func_447(447, NULL); 
  LATENCY_10to99(argc)
  func_448(448, NULL); 
  LATENCY_10to99(argc)
  func_449(449, NULL); 
  return rv; 
} 


int func_045 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_045 \n");
  LATENCY_10to99(argc)
  func_450(450, NULL); 
  LATENCY_10to99(argc)
  func_451(451, NULL); 
  LATENCY_10to99(argc)
  func_452(452, NULL); 
  LATENCY_10to99(argc)
  func_453(453, NULL); 
  LATENCY_10to99(argc)
  func_454(454, NULL); 
  LATENCY_10to99(argc)
  func_455(455, NULL); 
  LATENCY_10to99(argc)
  func_456(456, NULL); 
  LATENCY_10to99(argc)
  func_457(457, NULL); 
  LATENCY_10to99(argc)
  func_458(458, NULL); 
  LATENCY_10to99(argc)
  func_459(459, NULL); 
  return rv; 
} 


int func_046 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_046 \n");
  LATENCY_10to99(argc)
  func_460(460, NULL); 
  LATENCY_10to99(argc)
  func_461(461, NULL); 
  LATENCY_10to99(argc)
  func_462(462, NULL); 
  LATENCY_10to99(argc)
  func_463(463, NULL); 
  LATENCY_10to99(argc)
  func_464(464, NULL); 
  LATENCY_10to99(argc)
  func_465(465, NULL); 
  LATENCY_10to99(argc)
  func_466(466, NULL); 
  LATENCY_10to99(argc)
  func_467(467, NULL); 
  LATENCY_10to99(argc)
  func_468(468, NULL); 
  LATENCY_10to99(argc)
  func_469(469, NULL); 
  return rv; 
} 


int func_047 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_047 \n");
  LATENCY_10to99(argc)
  func_470(470, NULL); 
  LATENCY_10to99(argc)
  func_471(471, NULL); 
  LATENCY_10to99(argc)
  func_472(472, NULL); 
  LATENCY_10to99(argc)
  func_473(473, NULL); 
  LATENCY_10to99(argc)
  func_474(474, NULL); 
  LATENCY_10to99(argc)
  func_475(475, NULL); 
  LATENCY_10to99(argc)
  func_476(476, NULL); 
  LATENCY_10to99(argc)
  func_477(477, NULL); 
  LATENCY_10to99(argc)
  func_478(478, NULL); 
  LATENCY_10to99(argc)
  func_479(479, NULL); 
  return rv; 
} 


int func_048 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_048 \n");
  LATENCY_10to99(argc)
  func_480(480, NULL); 
  LATENCY_10to99(argc)
  func_481(481, NULL); 
  LATENCY_10to99(argc)
  func_482(482, NULL); 
  LATENCY_10to99(argc)
  func_483(483, NULL); 
  LATENCY_10to99(argc)
  func_484(484, NULL); 
  LATENCY_10to99(argc)
  func_485(485, NULL); 
  LATENCY_10to99(argc)
  func_486(486, NULL); 
  LATENCY_10to99(argc)
  func_487(487, NULL); 
  LATENCY_10to99(argc)
  func_488(488, NULL); 
  LATENCY_10to99(argc)
  func_489(489, NULL); 
  return rv; 
} 


int func_049 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_049 \n");
  LATENCY_10to99(argc)
  func_490(490, NULL); 
  LATENCY_10to99(argc)
  func_491(491, NULL); 
  LATENCY_10to99(argc)
  func_492(492, NULL); 
  LATENCY_10to99(argc)
  func_493(493, NULL); 
  LATENCY_10to99(argc)
  func_494(494, NULL); 
  LATENCY_10to99(argc)
  func_495(495, NULL); 
  LATENCY_10to99(argc)
  func_496(496, NULL); 
  LATENCY_10to99(argc)
  func_497(497, NULL); 
  LATENCY_10to99(argc)
  func_498(498, NULL); 
  LATENCY_10to99(argc)
  func_499(499, NULL); 
  return rv; 
} 


int func_050 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_050 \n");
  LATENCY_10to99(argc)
  func_500(500, NULL); 
  LATENCY_10to99(argc)
  func_501(501, NULL); 
  LATENCY_10to99(argc)
  func_502(502, NULL); 
  LATENCY_10to99(argc)
  func_503(503, NULL); 
  LATENCY_10to99(argc)
  func_504(504, NULL); 
  LATENCY_10to99(argc)
  func_505(505, NULL); 
  LATENCY_10to99(argc)
  func_506(506, NULL); 
  LATENCY_10to99(argc)
  func_507(507, NULL); 
  LATENCY_10to99(argc)
  func_508(508, NULL); 
  LATENCY_10to99(argc)
  func_509(509, NULL); 
  return rv; 
} 


int func_051 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_051 \n");
  LATENCY_10to99(argc)
  func_510(510, NULL); 
  LATENCY_10to99(argc)
  func_511(511, NULL); 
  LATENCY_10to99(argc)
  func_512(512, NULL); 
  LATENCY_10to99(argc)
  func_513(513, NULL); 
  LATENCY_10to99(argc)
  func_514(514, NULL); 
  LATENCY_10to99(argc)
  func_515(515, NULL); 
  LATENCY_10to99(argc)
  func_516(516, NULL); 
  LATENCY_10to99(argc)
  func_517(517, NULL); 
  LATENCY_10to99(argc)
  func_518(518, NULL); 
  LATENCY_10to99(argc)
  func_519(519, NULL); 
  return rv; 
} 


int func_052 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_052 \n");
  LATENCY_10to99(argc)
  func_520(520, NULL); 
  LATENCY_10to99(argc)
  func_521(521, NULL); 
  LATENCY_10to99(argc)
  func_522(522, NULL); 
  LATENCY_10to99(argc)
  func_523(523, NULL); 
  LATENCY_10to99(argc)
  func_524(524, NULL); 
  LATENCY_10to99(argc)
  func_525(525, NULL); 
  LATENCY_10to99(argc)
  func_526(526, NULL); 
  LATENCY_10to99(argc)
  func_527(527, NULL); 
  LATENCY_10to99(argc)
  func_528(528, NULL); 
  LATENCY_10to99(argc)
  func_529(529, NULL); 
  return rv; 
} 


int func_053 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_053 \n");
  LATENCY_10to99(argc)
  func_530(530, NULL); 
  LATENCY_10to99(argc)
  func_531(531, NULL); 
  LATENCY_10to99(argc)
  func_532(532, NULL); 
  LATENCY_10to99(argc)
  func_533(533, NULL); 
  LATENCY_10to99(argc)
  func_534(534, NULL); 
  LATENCY_10to99(argc)
  func_535(535, NULL); 
  LATENCY_10to99(argc)
  func_536(536, NULL); 
  LATENCY_10to99(argc)
  func_537(537, NULL); 
  LATENCY_10to99(argc)
  func_538(538, NULL); 
  LATENCY_10to99(argc)
  func_539(539, NULL); 
  return rv; 
} 


int func_054 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_054 \n");
  LATENCY_10to99(argc)
  func_540(540, NULL); 
  LATENCY_10to99(argc)
  func_541(541, NULL); 
  LATENCY_10to99(argc)
  func_542(542, NULL); 
  LATENCY_10to99(argc)
  func_543(543, NULL); 
  LATENCY_10to99(argc)
  func_544(544, NULL); 
  LATENCY_10to99(argc)
  func_545(545, NULL); 
  LATENCY_10to99(argc)
  func_546(546, NULL); 
  LATENCY_10to99(argc)
  func_547(547, NULL); 
  LATENCY_10to99(argc)
  func_548(548, NULL); 
  LATENCY_10to99(argc)
  func_549(549, NULL); 
  return rv; 
} 


int func_055 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_055 \n");
  LATENCY_10to99(argc)
  func_550(550, NULL); 
  LATENCY_10to99(argc)
  func_551(551, NULL); 
  LATENCY_10to99(argc)
  func_552(552, NULL); 
  LATENCY_10to99(argc)
  func_553(553, NULL); 
  LATENCY_10to99(argc)
  func_554(554, NULL); 
  LATENCY_10to99(argc)
  func_555(555, NULL); 
  LATENCY_10to99(argc)
  func_556(556, NULL); 
  LATENCY_10to99(argc)
  func_557(557, NULL); 
  LATENCY_10to99(argc)
  func_558(558, NULL); 
  LATENCY_10to99(argc)
  func_559(559, NULL); 
  return rv; 
} 


int func_056 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_056 \n");
  LATENCY_10to99(argc)
  func_560(560, NULL); 
  LATENCY_10to99(argc)
  func_561(561, NULL); 
  LATENCY_10to99(argc)
  func_562(562, NULL); 
  LATENCY_10to99(argc)
  func_563(563, NULL); 
  LATENCY_10to99(argc)
  func_564(564, NULL); 
  LATENCY_10to99(argc)
  func_565(565, NULL); 
  LATENCY_10to99(argc)
  func_566(566, NULL); 
  LATENCY_10to99(argc)
  func_567(567, NULL); 
  LATENCY_10to99(argc)
  func_568(568, NULL); 
  LATENCY_10to99(argc)
  func_569(569, NULL); 
  return rv; 
} 


int func_057 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_057 \n");
  LATENCY_10to99(argc)
  func_570(570, NULL); 
  LATENCY_10to99(argc)
  func_571(571, NULL); 
  LATENCY_10to99(argc)
  func_572(572, NULL); 
  LATENCY_10to99(argc)
  func_573(573, NULL); 
  LATENCY_10to99(argc)
  func_574(574, NULL); 
  LATENCY_10to99(argc)
  func_575(575, NULL); 
  LATENCY_10to99(argc)
  func_576(576, NULL); 
  LATENCY_10to99(argc)
  func_577(577, NULL); 
  LATENCY_10to99(argc)
  func_578(578, NULL); 
  LATENCY_10to99(argc)
  func_579(579, NULL); 
  return rv; 
} 


int func_058 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_058 \n");
  LATENCY_10to99(argc)
  func_580(580, NULL); 
  LATENCY_10to99(argc)
  func_581(581, NULL); 
  LATENCY_10to99(argc)
  func_582(582, NULL); 
  LATENCY_10to99(argc)
  func_583(583, NULL); 
  LATENCY_10to99(argc)
  func_584(584, NULL); 
  LATENCY_10to99(argc)
  func_585(585, NULL); 
  LATENCY_10to99(argc)
  func_586(586, NULL); 
  LATENCY_10to99(argc)
  func_587(587, NULL); 
  LATENCY_10to99(argc)
  func_588(588, NULL); 
  LATENCY_10to99(argc)
  func_589(589, NULL); 
  return rv; 
} 


int func_059 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_059 \n");
  LATENCY_10to99(argc)
  func_590(590, NULL); 
  LATENCY_10to99(argc)
  func_591(591, NULL); 
  LATENCY_10to99(argc)
  func_592(592, NULL); 
  LATENCY_10to99(argc)
  func_593(593, NULL); 
  LATENCY_10to99(argc)
  func_594(594, NULL); 
  LATENCY_10to99(argc)
  func_595(595, NULL); 
  LATENCY_10to99(argc)
  func_596(596, NULL); 
  LATENCY_10to99(argc)
  func_597(597, NULL); 
  LATENCY_10to99(argc)
  func_598(598, NULL); 
  LATENCY_10to99(argc)
  func_599(599, NULL); 
  return rv; 
} 


int func_060 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_060 \n");
  LATENCY_10to99(argc)
  func_600(600, NULL); 
  LATENCY_10to99(argc)
  func_601(601, NULL); 
  LATENCY_10to99(argc)
  func_602(602, NULL); 
  LATENCY_10to99(argc)
  func_603(603, NULL); 
  LATENCY_10to99(argc)
  func_604(604, NULL); 
  LATENCY_10to99(argc)
  func_605(605, NULL); 
  LATENCY_10to99(argc)
  func_606(606, NULL); 
  LATENCY_10to99(argc)
  func_607(607, NULL); 
  LATENCY_10to99(argc)
  func_608(608, NULL); 
  LATENCY_10to99(argc)
  func_609(609, NULL); 
  return rv; 
} 


int func_061 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_061 \n");
  LATENCY_10to99(argc)
  func_610(610, NULL); 
  LATENCY_10to99(argc)
  func_611(611, NULL); 
  LATENCY_10to99(argc)
  func_612(612, NULL); 
  LATENCY_10to99(argc)
  func_613(613, NULL); 
  LATENCY_10to99(argc)
  func_614(614, NULL); 
  LATENCY_10to99(argc)
  func_615(615, NULL); 
  LATENCY_10to99(argc)
  func_616(616, NULL); 
  LATENCY_10to99(argc)
  func_617(617, NULL); 
  LATENCY_10to99(argc)
  func_618(618, NULL); 
  LATENCY_10to99(argc)
  func_619(619, NULL); 
  return rv; 
} 


int func_062 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_062 \n");
  LATENCY_10to99(argc)
  func_620(620, NULL); 
  LATENCY_10to99(argc)
  func_621(621, NULL); 
  LATENCY_10to99(argc)
  func_622(622, NULL); 
  LATENCY_10to99(argc)
  func_623(623, NULL); 
  LATENCY_10to99(argc)
  func_624(624, NULL); 
  LATENCY_10to99(argc)
  func_625(625, NULL); 
  LATENCY_10to99(argc)
  func_626(626, NULL); 
  LATENCY_10to99(argc)
  func_627(627, NULL); 
  LATENCY_10to99(argc)
  func_628(628, NULL); 
  LATENCY_10to99(argc)
  func_629(629, NULL); 
  return rv; 
} 


int func_063 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_063 \n");
  LATENCY_10to99(argc)
  func_630(630, NULL); 
  LATENCY_10to99(argc)
  func_631(631, NULL); 
  LATENCY_10to99(argc)
  func_632(632, NULL); 
  LATENCY_10to99(argc)
  func_633(633, NULL); 
  LATENCY_10to99(argc)
  func_634(634, NULL); 
  LATENCY_10to99(argc)
  func_635(635, NULL); 
  LATENCY_10to99(argc)
  func_636(636, NULL); 
  LATENCY_10to99(argc)
  func_637(637, NULL); 
  LATENCY_10to99(argc)
  func_638(638, NULL); 
  LATENCY_10to99(argc)
  func_639(639, NULL); 
  return rv; 
} 


int func_064 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_064 \n");
  LATENCY_10to99(argc)
  func_640(640, NULL); 
  LATENCY_10to99(argc)
  func_641(641, NULL); 
  LATENCY_10to99(argc)
  func_642(642, NULL); 
  LATENCY_10to99(argc)
  func_643(643, NULL); 
  LATENCY_10to99(argc)
  func_644(644, NULL); 
  LATENCY_10to99(argc)
  func_645(645, NULL); 
  LATENCY_10to99(argc)
  func_646(646, NULL); 
  LATENCY_10to99(argc)
  func_647(647, NULL); 
  LATENCY_10to99(argc)
  func_648(648, NULL); 
  LATENCY_10to99(argc)
  func_649(649, NULL); 
  return rv; 
} 


int func_065 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_065 \n");
  LATENCY_10to99(argc)
  func_650(650, NULL); 
  LATENCY_10to99(argc)
  func_651(651, NULL); 
  LATENCY_10to99(argc)
  func_652(652, NULL); 
  LATENCY_10to99(argc)
  func_653(653, NULL); 
  LATENCY_10to99(argc)
  func_654(654, NULL); 
  LATENCY_10to99(argc)
  func_655(655, NULL); 
  LATENCY_10to99(argc)
  func_656(656, NULL); 
  LATENCY_10to99(argc)
  func_657(657, NULL); 
  LATENCY_10to99(argc)
  func_658(658, NULL); 
  LATENCY_10to99(argc)
  func_659(659, NULL); 
  return rv; 
} 


int func_066 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_066 \n");
  LATENCY_10to99(argc)
  func_660(660, NULL); 
  LATENCY_10to99(argc)
  func_661(661, NULL); 
  LATENCY_10to99(argc)
  func_662(662, NULL); 
  LATENCY_10to99(argc)
  func_663(663, NULL); 
  LATENCY_10to99(argc)
  func_664(664, NULL); 
  LATENCY_10to99(argc)
  func_665(665, NULL); 
  LATENCY_10to99(argc)
  func_666(666, NULL); 
  LATENCY_10to99(argc)
  func_667(667, NULL); 
  LATENCY_10to99(argc)
  func_668(668, NULL); 
  LATENCY_10to99(argc)
  func_669(669, NULL); 
  return rv; 
} 


int func_067 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_067 \n");
  LATENCY_10to99(argc)
  func_670(670, NULL); 
  LATENCY_10to99(argc)
  func_671(671, NULL); 
  LATENCY_10to99(argc)
  func_672(672, NULL); 
  LATENCY_10to99(argc)
  func_673(673, NULL); 
  LATENCY_10to99(argc)
  func_674(674, NULL); 
  LATENCY_10to99(argc)
  func_675(675, NULL); 
  LATENCY_10to99(argc)
  func_676(676, NULL); 
  LATENCY_10to99(argc)
  func_677(677, NULL); 
  LATENCY_10to99(argc)
  func_678(678, NULL); 
  LATENCY_10to99(argc)
  func_679(679, NULL); 
  return rv; 
} 


int func_068 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_068 \n");
  LATENCY_10to99(argc)
  func_680(680, NULL); 
  LATENCY_10to99(argc)
  func_681(681, NULL); 
  LATENCY_10to99(argc)
  func_682(682, NULL); 
  LATENCY_10to99(argc)
  func_683(683, NULL); 
  LATENCY_10to99(argc)
  func_684(684, NULL); 
  LATENCY_10to99(argc)
  func_685(685, NULL); 
  LATENCY_10to99(argc)
  func_686(686, NULL); 
  LATENCY_10to99(argc)
  func_687(687, NULL); 
  LATENCY_10to99(argc)
  func_688(688, NULL); 
  LATENCY_10to99(argc)
  func_689(689, NULL); 
  return rv; 
} 


int func_069 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_069 \n");
  LATENCY_10to99(argc)
  func_690(690, NULL); 
  LATENCY_10to99(argc)
  func_691(691, NULL); 
  LATENCY_10to99(argc)
  func_692(692, NULL); 
  LATENCY_10to99(argc)
  func_693(693, NULL); 
  LATENCY_10to99(argc)
  func_694(694, NULL); 
  LATENCY_10to99(argc)
  func_695(695, NULL); 
  LATENCY_10to99(argc)
  func_696(696, NULL); 
  LATENCY_10to99(argc)
  func_697(697, NULL); 
  LATENCY_10to99(argc)
  func_698(698, NULL); 
  LATENCY_10to99(argc)
  func_699(699, NULL); 
  return rv; 
} 


int func_070 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_070 \n");
  LATENCY_10to99(argc)
  func_700(700, NULL); 
  LATENCY_10to99(argc)
  func_701(701, NULL); 
  LATENCY_10to99(argc)
  func_702(702, NULL); 
  LATENCY_10to99(argc)
  func_703(703, NULL); 
  LATENCY_10to99(argc)
  func_704(704, NULL); 
  LATENCY_10to99(argc)
  func_705(705, NULL); 
  LATENCY_10to99(argc)
  func_706(706, NULL); 
  LATENCY_10to99(argc)
  func_707(707, NULL); 
  LATENCY_10to99(argc)
  func_708(708, NULL); 
  LATENCY_10to99(argc)
  func_709(709, NULL); 
  return rv; 
} 


int func_071 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_071 \n");
  LATENCY_10to99(argc)
  func_710(710, NULL); 
  LATENCY_10to99(argc)
  func_711(711, NULL); 
  LATENCY_10to99(argc)
  func_712(712, NULL); 
  LATENCY_10to99(argc)
  func_713(713, NULL); 
  LATENCY_10to99(argc)
  func_714(714, NULL); 
  LATENCY_10to99(argc)
  func_715(715, NULL); 
  LATENCY_10to99(argc)
  func_716(716, NULL); 
  LATENCY_10to99(argc)
  func_717(717, NULL); 
  LATENCY_10to99(argc)
  func_718(718, NULL); 
  LATENCY_10to99(argc)
  func_719(719, NULL); 
  return rv; 
} 


int func_072 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_072 \n");
  LATENCY_10to99(argc)
  func_720(720, NULL); 
  LATENCY_10to99(argc)
  func_721(721, NULL); 
  LATENCY_10to99(argc)
  func_722(722, NULL); 
  LATENCY_10to99(argc)
  func_723(723, NULL); 
  LATENCY_10to99(argc)
  func_724(724, NULL); 
  LATENCY_10to99(argc)
  func_725(725, NULL); 
  LATENCY_10to99(argc)
  func_726(726, NULL); 
  LATENCY_10to99(argc)
  func_727(727, NULL); 
  LATENCY_10to99(argc)
  func_728(728, NULL); 
  LATENCY_10to99(argc)
  func_729(729, NULL); 
  return rv; 
} 


int func_073 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_073 \n");
  LATENCY_10to99(argc)
  func_730(730, NULL); 
  LATENCY_10to99(argc)
  func_731(731, NULL); 
  LATENCY_10to99(argc)
  func_732(732, NULL); 
  LATENCY_10to99(argc)
  func_733(733, NULL); 
  LATENCY_10to99(argc)
  func_734(734, NULL); 
  LATENCY_10to99(argc)
  func_735(735, NULL); 
  LATENCY_10to99(argc)
  func_736(736, NULL); 
  LATENCY_10to99(argc)
  func_737(737, NULL); 
  LATENCY_10to99(argc)
  func_738(738, NULL); 
  LATENCY_10to99(argc)
  func_739(739, NULL); 
  return rv; 
} 


int func_074 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_074 \n");
  LATENCY_10to99(argc)
  func_740(740, NULL); 
  LATENCY_10to99(argc)
  func_741(741, NULL); 
  LATENCY_10to99(argc)
  func_742(742, NULL); 
  LATENCY_10to99(argc)
  func_743(743, NULL); 
  LATENCY_10to99(argc)
  func_744(744, NULL); 
  LATENCY_10to99(argc)
  func_745(745, NULL); 
  LATENCY_10to99(argc)
  func_746(746, NULL); 
  LATENCY_10to99(argc)
  func_747(747, NULL); 
  LATENCY_10to99(argc)
  func_748(748, NULL); 
  LATENCY_10to99(argc)
  func_749(749, NULL); 
  return rv; 
} 


int func_075 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_075 \n");
  LATENCY_10to99(argc)
  func_750(750, NULL); 
  LATENCY_10to99(argc)
  func_751(751, NULL); 
  LATENCY_10to99(argc)
  func_752(752, NULL); 
  LATENCY_10to99(argc)
  func_753(753, NULL); 
  LATENCY_10to99(argc)
  func_754(754, NULL); 
  LATENCY_10to99(argc)
  func_755(755, NULL); 
  LATENCY_10to99(argc)
  func_756(756, NULL); 
  LATENCY_10to99(argc)
  func_757(757, NULL); 
  LATENCY_10to99(argc)
  func_758(758, NULL); 
  LATENCY_10to99(argc)
  func_759(759, NULL); 
  return rv; 
} 


int func_076 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_076 \n");
  LATENCY_10to99(argc)
  func_760(760, NULL); 
  LATENCY_10to99(argc)
  func_761(761, NULL); 
  LATENCY_10to99(argc)
  func_762(762, NULL); 
  LATENCY_10to99(argc)
  func_763(763, NULL); 
  LATENCY_10to99(argc)
  func_764(764, NULL); 
  LATENCY_10to99(argc)
  func_765(765, NULL); 
  LATENCY_10to99(argc)
  func_766(766, NULL); 
  LATENCY_10to99(argc)
  func_767(767, NULL); 
  LATENCY_10to99(argc)
  func_768(768, NULL); 
  LATENCY_10to99(argc)
  func_769(769, NULL); 
  return rv; 
} 


int func_077 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_077 \n");
  LATENCY_10to99(argc)
  func_770(770, NULL); 
  LATENCY_10to99(argc)
  func_771(771, NULL); 
  LATENCY_10to99(argc)
  func_772(772, NULL); 
  LATENCY_10to99(argc)
  func_773(773, NULL); 
  LATENCY_10to99(argc)
  func_774(774, NULL); 
  LATENCY_10to99(argc)
  func_775(775, NULL); 
  LATENCY_10to99(argc)
  func_776(776, NULL); 
  LATENCY_10to99(argc)
  func_777(777, NULL); 
  LATENCY_10to99(argc)
  func_778(778, NULL); 
  LATENCY_10to99(argc)
  func_779(779, NULL); 
  return rv; 
} 


int func_078 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_078 \n");
  LATENCY_10to99(argc)
  func_780(780, NULL); 
  LATENCY_10to99(argc)
  func_781(781, NULL); 
  LATENCY_10to99(argc)
  func_782(782, NULL); 
  LATENCY_10to99(argc)
  func_783(783, NULL); 
  LATENCY_10to99(argc)
  func_784(784, NULL); 
  LATENCY_10to99(argc)
  func_785(785, NULL); 
  LATENCY_10to99(argc)
  func_786(786, NULL); 
  LATENCY_10to99(argc)
  func_787(787, NULL); 
  LATENCY_10to99(argc)
  func_788(788, NULL); 
  LATENCY_10to99(argc)
  func_789(789, NULL); 
  return rv; 
} 


int func_079 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_079 \n");
  LATENCY_10to99(argc)
  func_790(790, NULL); 
  LATENCY_10to99(argc)
  func_791(791, NULL); 
  LATENCY_10to99(argc)
  func_792(792, NULL); 
  LATENCY_10to99(argc)
  func_793(793, NULL); 
  LATENCY_10to99(argc)
  func_794(794, NULL); 
  LATENCY_10to99(argc)
  func_795(795, NULL); 
  LATENCY_10to99(argc)
  func_796(796, NULL); 
  LATENCY_10to99(argc)
  func_797(797, NULL); 
  LATENCY_10to99(argc)
  func_798(798, NULL); 
  LATENCY_10to99(argc)
  func_799(799, NULL); 
  return rv; 
} 


int func_080 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_080 \n");
  LATENCY_10to99(argc)
  func_800(800, NULL); 
  LATENCY_10to99(argc)
  func_801(801, NULL); 
  LATENCY_10to99(argc)
  func_802(802, NULL); 
  LATENCY_10to99(argc)
  func_803(803, NULL); 
  LATENCY_10to99(argc)
  func_804(804, NULL); 
  LATENCY_10to99(argc)
  func_805(805, NULL); 
  LATENCY_10to99(argc)
  func_806(806, NULL); 
  LATENCY_10to99(argc)
  func_807(807, NULL); 
  LATENCY_10to99(argc)
  func_808(808, NULL); 
  LATENCY_10to99(argc)
  func_809(809, NULL); 
  return rv; 
} 


int func_081 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_081 \n");
  LATENCY_10to99(argc)
  func_810(810, NULL); 
  LATENCY_10to99(argc)
  func_811(811, NULL); 
  LATENCY_10to99(argc)
  func_812(812, NULL); 
  LATENCY_10to99(argc)
  func_813(813, NULL); 
  LATENCY_10to99(argc)
  func_814(814, NULL); 
  LATENCY_10to99(argc)
  func_815(815, NULL); 
  LATENCY_10to99(argc)
  func_816(816, NULL); 
  LATENCY_10to99(argc)
  func_817(817, NULL); 
  LATENCY_10to99(argc)
  func_818(818, NULL); 
  LATENCY_10to99(argc)
  func_819(819, NULL); 
  return rv; 
} 


int func_082 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_082 \n");
  LATENCY_10to99(argc)
  func_820(820, NULL); 
  LATENCY_10to99(argc)
  func_821(821, NULL); 
  LATENCY_10to99(argc)
  func_822(822, NULL); 
  LATENCY_10to99(argc)
  func_823(823, NULL); 
  LATENCY_10to99(argc)
  func_824(824, NULL); 
  LATENCY_10to99(argc)
  func_825(825, NULL); 
  LATENCY_10to99(argc)
  func_826(826, NULL); 
  LATENCY_10to99(argc)
  func_827(827, NULL); 
  LATENCY_10to99(argc)
  func_828(828, NULL); 
  LATENCY_10to99(argc)
  func_829(829, NULL); 
  return rv; 
} 


int func_083 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_083 \n");
  LATENCY_10to99(argc)
  func_830(830, NULL); 
  LATENCY_10to99(argc)
  func_831(831, NULL); 
  LATENCY_10to99(argc)
  func_832(832, NULL); 
  LATENCY_10to99(argc)
  func_833(833, NULL); 
  LATENCY_10to99(argc)
  func_834(834, NULL); 
  LATENCY_10to99(argc)
  func_835(835, NULL); 
  LATENCY_10to99(argc)
  func_836(836, NULL); 
  LATENCY_10to99(argc)
  func_837(837, NULL); 
  LATENCY_10to99(argc)
  func_838(838, NULL); 
  LATENCY_10to99(argc)
  func_839(839, NULL); 
  return rv; 
} 


int func_084 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_084 \n");
  LATENCY_10to99(argc)
  func_840(840, NULL); 
  LATENCY_10to99(argc)
  func_841(841, NULL); 
  LATENCY_10to99(argc)
  func_842(842, NULL); 
  LATENCY_10to99(argc)
  func_843(843, NULL); 
  LATENCY_10to99(argc)
  func_844(844, NULL); 
  LATENCY_10to99(argc)
  func_845(845, NULL); 
  LATENCY_10to99(argc)
  func_846(846, NULL); 
  LATENCY_10to99(argc)
  func_847(847, NULL); 
  LATENCY_10to99(argc)
  func_848(848, NULL); 
  LATENCY_10to99(argc)
  func_849(849, NULL); 
  return rv; 
} 


int func_085 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_085 \n");
  LATENCY_10to99(argc)
  func_850(850, NULL); 
  LATENCY_10to99(argc)
  func_851(851, NULL); 
  LATENCY_10to99(argc)
  func_852(852, NULL); 
  LATENCY_10to99(argc)
  func_853(853, NULL); 
  LATENCY_10to99(argc)
  func_854(854, NULL); 
  LATENCY_10to99(argc)
  func_855(855, NULL); 
  LATENCY_10to99(argc)
  func_856(856, NULL); 
  LATENCY_10to99(argc)
  func_857(857, NULL); 
  LATENCY_10to99(argc)
  func_858(858, NULL); 
  LATENCY_10to99(argc)
  func_859(859, NULL); 
  return rv; 
} 


int func_086 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_086 \n");
  LATENCY_10to99(argc)
  func_860(860, NULL); 
  LATENCY_10to99(argc)
  func_861(861, NULL); 
  LATENCY_10to99(argc)
  func_862(862, NULL); 
  LATENCY_10to99(argc)
  func_863(863, NULL); 
  LATENCY_10to99(argc)
  func_864(864, NULL); 
  LATENCY_10to99(argc)
  func_865(865, NULL); 
  LATENCY_10to99(argc)
  func_866(866, NULL); 
  LATENCY_10to99(argc)
  func_867(867, NULL); 
  LATENCY_10to99(argc)
  func_868(868, NULL); 
  LATENCY_10to99(argc)
  func_869(869, NULL); 
  return rv; 
} 


int func_087 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_087 \n");
  LATENCY_10to99(argc)
  func_870(870, NULL); 
  LATENCY_10to99(argc)
  func_871(871, NULL); 
  LATENCY_10to99(argc)
  func_872(872, NULL); 
  LATENCY_10to99(argc)
  func_873(873, NULL); 
  LATENCY_10to99(argc)
  func_874(874, NULL); 
  LATENCY_10to99(argc)
  func_875(875, NULL); 
  LATENCY_10to99(argc)
  func_876(876, NULL); 
  LATENCY_10to99(argc)
  func_877(877, NULL); 
  LATENCY_10to99(argc)
  func_878(878, NULL); 
  LATENCY_10to99(argc)
  func_879(879, NULL); 
  return rv; 
} 


int func_088 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_088 \n");
  LATENCY_10to99(argc)
  func_880(880, NULL); 
  LATENCY_10to99(argc)
  func_881(881, NULL); 
  LATENCY_10to99(argc)
  func_882(882, NULL); 
  LATENCY_10to99(argc)
  func_883(883, NULL); 
  LATENCY_10to99(argc)
  func_884(884, NULL); 
  LATENCY_10to99(argc)
  func_885(885, NULL); 
  LATENCY_10to99(argc)
  func_886(886, NULL); 
  LATENCY_10to99(argc)
  func_887(887, NULL); 
  LATENCY_10to99(argc)
  func_888(888, NULL); 
  LATENCY_10to99(argc)
  func_889(889, NULL); 
  return rv; 
} 


int func_089 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_089 \n");
  LATENCY_10to99(argc)
  func_890(890, NULL); 
  LATENCY_10to99(argc)
  func_891(891, NULL); 
  LATENCY_10to99(argc)
  func_892(892, NULL); 
  LATENCY_10to99(argc)
  func_893(893, NULL); 
  LATENCY_10to99(argc)
  func_894(894, NULL); 
  LATENCY_10to99(argc)
  func_895(895, NULL); 
  LATENCY_10to99(argc)
  func_896(896, NULL); 
  LATENCY_10to99(argc)
  func_897(897, NULL); 
  LATENCY_10to99(argc)
  func_898(898, NULL); 
  LATENCY_10to99(argc)
  func_899(899, NULL); 
  return rv; 
} 


int func_090 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_090 \n");
  LATENCY_10to99(argc)
  func_900(900, NULL); 
  LATENCY_10to99(argc)
  func_901(901, NULL); 
  LATENCY_10to99(argc)
  func_902(902, NULL); 
  LATENCY_10to99(argc)
  func_903(903, NULL); 
  LATENCY_10to99(argc)
  func_904(904, NULL); 
  LATENCY_10to99(argc)
  func_905(905, NULL); 
  LATENCY_10to99(argc)
  func_906(906, NULL); 
  LATENCY_10to99(argc)
  func_907(907, NULL); 
  LATENCY_10to99(argc)
  func_908(908, NULL); 
  LATENCY_10to99(argc)
  func_909(909, NULL); 
  return rv; 
} 


int func_091 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_091 \n");
  LATENCY_10to99(argc)
  func_910(910, NULL); 
  LATENCY_10to99(argc)
  func_911(911, NULL); 
  LATENCY_10to99(argc)
  func_912(912, NULL); 
  LATENCY_10to99(argc)
  func_913(913, NULL); 
  LATENCY_10to99(argc)
  func_914(914, NULL); 
  LATENCY_10to99(argc)
  func_915(915, NULL); 
  LATENCY_10to99(argc)
  func_916(916, NULL); 
  LATENCY_10to99(argc)
  func_917(917, NULL); 
  LATENCY_10to99(argc)
  func_918(918, NULL); 
  LATENCY_10to99(argc)
  func_919(919, NULL); 
  return rv; 
} 


int func_092 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_092 \n");
  LATENCY_10to99(argc)
  func_920(920, NULL); 
  LATENCY_10to99(argc)
  func_921(921, NULL); 
  LATENCY_10to99(argc)
  func_922(922, NULL); 
  LATENCY_10to99(argc)
  func_923(923, NULL); 
  LATENCY_10to99(argc)
  func_924(924, NULL); 
  LATENCY_10to99(argc)
  func_925(925, NULL); 
  LATENCY_10to99(argc)
  func_926(926, NULL); 
  LATENCY_10to99(argc)
  func_927(927, NULL); 
  LATENCY_10to99(argc)
  func_928(928, NULL); 
  LATENCY_10to99(argc)
  func_929(929, NULL); 
  return rv; 
} 


int func_093 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_093 \n");
  LATENCY_10to99(argc)
  func_930(930, NULL); 
  LATENCY_10to99(argc)
  func_931(931, NULL); 
  LATENCY_10to99(argc)
  func_932(932, NULL); 
  LATENCY_10to99(argc)
  func_933(933, NULL); 
  LATENCY_10to99(argc)
  func_934(934, NULL); 
  LATENCY_10to99(argc)
  func_935(935, NULL); 
  LATENCY_10to99(argc)
  func_936(936, NULL); 
  LATENCY_10to99(argc)
  func_937(937, NULL); 
  LATENCY_10to99(argc)
  func_938(938, NULL); 
  LATENCY_10to99(argc)
  func_939(939, NULL); 
  return rv; 
} 


int func_094 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_094 \n");
  LATENCY_10to99(argc)
  func_940(940, NULL); 
  LATENCY_10to99(argc)
  func_941(941, NULL); 
  LATENCY_10to99(argc)
  func_942(942, NULL); 
  LATENCY_10to99(argc)
  func_943(943, NULL); 
  LATENCY_10to99(argc)
  func_944(944, NULL); 
  LATENCY_10to99(argc)
  func_945(945, NULL); 
  LATENCY_10to99(argc)
  func_946(946, NULL); 
  LATENCY_10to99(argc)
  func_947(947, NULL); 
  LATENCY_10to99(argc)
  func_948(948, NULL); 
  LATENCY_10to99(argc)
  func_949(949, NULL); 
  return rv; 
} 


int func_095 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_095 \n");
  LATENCY_10to99(argc)
  func_950(950, NULL); 
  LATENCY_10to99(argc)
  func_951(951, NULL); 
  LATENCY_10to99(argc)
  func_952(952, NULL); 
  LATENCY_10to99(argc)
  func_953(953, NULL); 
  LATENCY_10to99(argc)
  func_954(954, NULL); 
  LATENCY_10to99(argc)
  func_955(955, NULL); 
  LATENCY_10to99(argc)
  func_956(956, NULL); 
  LATENCY_10to99(argc)
  func_957(957, NULL); 
  LATENCY_10to99(argc)
  func_958(958, NULL); 
  LATENCY_10to99(argc)
  func_959(959, NULL); 
  return rv; 
} 


int func_096 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_096 \n");
  LATENCY_10to99(argc)
  func_960(960, NULL); 
  LATENCY_10to99(argc)
  func_961(961, NULL); 
  LATENCY_10to99(argc)
  func_962(962, NULL); 
  LATENCY_10to99(argc)
  func_963(963, NULL); 
  LATENCY_10to99(argc)
  func_964(964, NULL); 
  LATENCY_10to99(argc)
  func_965(965, NULL); 
  LATENCY_10to99(argc)
  func_966(966, NULL); 
  LATENCY_10to99(argc)
  func_967(967, NULL); 
  LATENCY_10to99(argc)
  func_968(968, NULL); 
  LATENCY_10to99(argc)
  func_969(969, NULL); 
  return rv; 
} 


int func_097 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_097 \n");
  LATENCY_10to99(argc)
  func_970(970, NULL); 
  LATENCY_10to99(argc)
  func_971(971, NULL); 
  LATENCY_10to99(argc)
  func_972(972, NULL); 
  LATENCY_10to99(argc)
  func_973(973, NULL); 
  LATENCY_10to99(argc)
  func_974(974, NULL); 
  LATENCY_10to99(argc)
  func_975(975, NULL); 
  LATENCY_10to99(argc)
  func_976(976, NULL); 
  LATENCY_10to99(argc)
  func_977(977, NULL); 
  LATENCY_10to99(argc)
  func_978(978, NULL); 
  LATENCY_10to99(argc)
  func_979(979, NULL); 
  return rv; 
} 


int func_098 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_098 \n");
  LATENCY_10to99(argc)
  func_980(980, NULL); 
  LATENCY_10to99(argc)
  func_981(981, NULL); 
  LATENCY_10to99(argc)
  func_982(982, NULL); 
  LATENCY_10to99(argc)
  func_983(983, NULL); 
  LATENCY_10to99(argc)
  func_984(984, NULL); 
  LATENCY_10to99(argc)
  func_985(985, NULL); 
  LATENCY_10to99(argc)
  func_986(986, NULL); 
  LATENCY_10to99(argc)
  func_987(987, NULL); 
  LATENCY_10to99(argc)
  func_988(988, NULL); 
  LATENCY_10to99(argc)
  func_989(989, NULL); 
  return rv; 
} 


int func_099 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("  func_099 \n");
  LATENCY_10to99(argc)
  func_990(990, NULL); 
  LATENCY_10to99(argc)
  func_991(991, NULL); 
  LATENCY_10to99(argc)
  func_992(992, NULL); 
  LATENCY_10to99(argc)
  func_993(993, NULL); 
  LATENCY_10to99(argc)
  func_994(994, NULL); 
  LATENCY_10to99(argc)
  func_995(995, NULL); 
  LATENCY_10to99(argc)
  func_996(996, NULL); 
  LATENCY_10to99(argc)
  func_997(997, NULL); 
  LATENCY_10to99(argc)
  func_998(998, NULL); 
  LATENCY_10to99(argc)
  func_999(999, NULL); 
  return rv; 
} 

int func_001 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("func_001 \n");
  LATENCY_0to9(argc)
  func_010(10, NULL); 
  LATENCY_0to9(argc)
  func_011(11, NULL); 
  LATENCY_0to9(argc)
  func_012(12, NULL); 
  LATENCY_0to9(argc)
  func_013(13, NULL); 
  LATENCY_0to9(argc)
  func_014(14, NULL); 
  LATENCY_0to9(argc)
  func_015(15, NULL); 
  LATENCY_0to9(argc)
  func_016(16, NULL); 
  LATENCY_0to9(argc)
  func_017(17, NULL); 
  LATENCY_0to9(argc)
  func_018(18, NULL); 
  LATENCY_0to9(argc)
  func_019(19, NULL); 
  return rv; 
} 

int func_002 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("func_002 \n");
  LATENCY_0to9(argc)
  func_020(20, NULL); 
  LATENCY_0to9(argc)
  func_021(21, NULL); 
  LATENCY_0to9(argc)
  func_022(22, NULL); 
  LATENCY_0to9(argc)
  func_023(23, NULL); 
  LATENCY_0to9(argc)
  func_024(24, NULL); 
  LATENCY_0to9(argc)
  func_025(25, NULL); 
  LATENCY_0to9(argc)
  func_026(26, NULL); 
  LATENCY_0to9(argc)
  func_027(27, NULL); 
  LATENCY_0to9(argc)
  func_028(28, NULL); 
  LATENCY_0to9(argc)
  func_029(29, NULL); 
  return rv; 
} 

int func_003 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("func_003 \n");
  LATENCY_0to9(argc)
  func_030(30, NULL); 
  LATENCY_0to9(argc)
  func_031(31, NULL); 
  LATENCY_0to9(argc)
  func_032(32, NULL); 
  LATENCY_0to9(argc)
  func_033(33, NULL); 
  LATENCY_0to9(argc)
  func_034(34, NULL); 
  LATENCY_0to9(argc)
  func_035(35, NULL); 
  LATENCY_0to9(argc)
  func_036(36, NULL); 
  LATENCY_0to9(argc)
  func_037(37, NULL); 
  LATENCY_0to9(argc)
  func_038(38, NULL); 
  LATENCY_0to9(argc)
  func_039(39, NULL); 
  return rv; 
} 

int func_004 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("func_004 \n");
  LATENCY_0to9(argc)
  func_040(40, NULL); 
  LATENCY_0to9(argc)
  func_041(41, NULL); 
  LATENCY_0to9(argc)
  func_042(42, NULL); 
  LATENCY_0to9(argc)
  func_043(43, NULL); 
  LATENCY_0to9(argc)
  func_044(44, NULL); 
  LATENCY_0to9(argc)
  func_045(45, NULL); 
  LATENCY_0to9(argc)
  func_046(46, NULL); 
  LATENCY_0to9(argc)
  func_047(47, NULL); 
  LATENCY_0to9(argc)
  func_048(48, NULL); 
  LATENCY_0to9(argc)
  func_049(49, NULL); 
  return rv; 
} 

int func_005 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("func_005 \n");
  LATENCY_0to9(argc)
  func_050(50, NULL); 
  LATENCY_0to9(argc)
  func_051(51, NULL); 
  LATENCY_0to9(argc)
  func_052(52, NULL); 
  LATENCY_0to9(argc)
  func_053(53, NULL); 
  LATENCY_0to9(argc)
  func_054(54, NULL); 
  LATENCY_0to9(argc)
  func_055(55, NULL); 
  LATENCY_0to9(argc)
  func_056(56, NULL); 
  LATENCY_0to9(argc)
  func_057(57, NULL); 
  LATENCY_0to9(argc)
  func_058(58, NULL); 
  LATENCY_0to9(argc)
  func_059(59, NULL); 
  return rv; 
} 

int func_006 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("func_006 \n");
  LATENCY_0to9(argc)
  func_060(60, NULL); 
  LATENCY_0to9(argc)
  func_061(61, NULL); 
  LATENCY_0to9(argc)
  func_062(62, NULL); 
  LATENCY_0to9(argc)
  func_063(63, NULL); 
  LATENCY_0to9(argc)
  func_064(64, NULL); 
  LATENCY_0to9(argc)
  func_065(65, NULL); 
  LATENCY_0to9(argc)
  func_066(66, NULL); 
  LATENCY_0to9(argc)
  func_067(67, NULL); 
  LATENCY_0to9(argc)
  func_068(68, NULL); 
  LATENCY_0to9(argc)
  func_069(69, NULL); 
  return rv; 
} 

int func_007 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("func_007 \n");
  LATENCY_0to9(argc)
  func_070(70, NULL); 
  LATENCY_0to9(argc)
  func_071(71, NULL); 
  LATENCY_0to9(argc)
  func_072(72, NULL); 
  LATENCY_0to9(argc)
  func_073(73, NULL); 
  LATENCY_0to9(argc)
  func_074(74, NULL); 
  LATENCY_0to9(argc)
  func_075(75, NULL); 
  LATENCY_0to9(argc)
  func_076(76, NULL); 
  LATENCY_0to9(argc)
  func_077(77, NULL); 
  LATENCY_0to9(argc)
  func_078(78, NULL); 
  LATENCY_0to9(argc)
  func_079(79, NULL); 
  return rv; 
} 

int func_008 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("func_008 \n");
  LATENCY_0to9(argc)
  func_080(80, NULL); 
  LATENCY_0to9(argc)
  func_081(81, NULL); 
  LATENCY_0to9(argc)
  func_082(82, NULL); 
  LATENCY_0to9(argc)
  func_083(83, NULL); 
  LATENCY_0to9(argc)
  func_084(84, NULL); 
  LATENCY_0to9(argc)
  func_085(85, NULL); 
  LATENCY_0to9(argc)
  func_086(86, NULL); 
  LATENCY_0to9(argc)
  func_087(87, NULL); 
  LATENCY_0to9(argc)
  func_088(88, NULL); 
  LATENCY_0to9(argc)
  func_089(89, NULL); 
  return rv; 
} 

int func_009 (int argc, char** argv) 
{ 
  int rv = 0; 
  printf ("func_009 \n");
  LATENCY_0to9(argc)
  func_090(90, NULL); 
  LATENCY_0to9(argc)
  func_091(91, NULL); 
  LATENCY_0to9(argc)
  func_092(92, NULL); 
  LATENCY_0to9(argc)
  func_093(93, NULL); 
  LATENCY_0to9(argc)
  func_094(94, NULL); 
  LATENCY_0to9(argc)
  func_095(95, NULL); 
  LATENCY_0to9(argc)
  func_096(96, NULL); 
  LATENCY_0to9(argc)
  func_097(97, NULL); 
  LATENCY_0to9(argc)
  func_098(98, NULL); 
  LATENCY_0to9(argc)
  func_099(99, NULL); 
  return rv; 
} 

int main (int argc, char** argv) 
{ 
  int rv = 0; 
  func_001(1, NULL); 
  func_002(2, NULL); 
  func_003(3, NULL); 
  func_004(4, NULL); 
  func_005(5, NULL); 
  func_006(6, NULL); 
  func_007(7, NULL); 
  func_008(8, NULL); 
  func_009(9, NULL); 
  return rv; 
} 
