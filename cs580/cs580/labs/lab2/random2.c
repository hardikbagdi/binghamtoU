
#include<stdio.h>
#include<stdlib.h>
#include<limits.h>
int main(){


int a,b,c,d,i,f,g,e;
c=1;
srand(INT_MAX-time(0));
i=rand();
srand(i);
printf("%d",i);
for(i=0;i<10;i++){

a=rand();
b=rand();
b=(b%7);
f=(a%5);
g=b+f-1;
c=1;
//printf("a= %d, b= %d",a,b);
printf("\nG= %2d",g);
while(g>0){c=10*c;g--;}

d=a/c;
printf("\t Actual no:%13d",d);
e=INT_MAX-d;
printf("\t Difference: %13d\n",e);
}

return 0;}
