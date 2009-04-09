#ifndef STAPDEBUG_HPP_
#define STAPDEBUG_HPP_

#ifdef DEBUG_ON
#define coutd cout << __FILE__ << ": " << __func__ << "()" << " [" << __LINE__ << "] "
#else
#define coutd if(0) cout
#endif

#endif /*STAPDEBUG_HPP_*/
