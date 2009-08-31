#include <stdio.h>

int catlan(int n){
	if (n%2==0){
		return 0;
	}else if (n==1){
		return 1;
	}else if (n==2){
		return 1;
	}else{
		int i=0;
		int total = 0;
		for (i=1; i<=(n-1)/2; i++){
			total += catlan(n-(2*i))*catlan((2*i)-1);
		}
		return total;
	}

}

main(int argc, char *argv[]){
	extern int catlan(int n);
	printf("%d\n",catlan(9));
}
