#include <stdio.h>
#include <pthread.h>

pthread_t thread;

void* thread3 (void* d)
{
  int count3 = 0;

  while(count3 < 100){
    printf("Thread 3: %d\n", count3++);
  }
  return NULL;
}

void* thread2 (void* d)
{
  int count2 = 0;

  while(count2 < 1000){
	  //
    printf("Thread 2: %d\n", count2++);
  }
  return NULL;
}

int main (){

  pthread_create (&thread, NULL, thread2, NULL);
  pthread_create (&thread, NULL, thread3, NULL);

  //Thread 1
  int count1 = 0;

  while(count1 < 10){
    printf("Thread 1: %d\n", count1++);
  }

  pthread_join(thread,NULL);
  return 0;
}
