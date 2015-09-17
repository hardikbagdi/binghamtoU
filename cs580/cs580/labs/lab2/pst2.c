#include<stdio.h>
#include<limits.h>
#include<stdlib.h>
int random_between(int min, int max) {
min=INT_MIN;max=INT_MAX;
 
  double r=(double) rand();
double d = (double)max;
double s= (int)r % (int)d ;
printf("\n \n %f",r);
printf("\n \n %f",s);
}

int main(){
int f0=0;
int f1=1;
int number=INT_MIN;
int i=0;
int random_number=0;
int random_number2=0;
int count=0;
unsigned int mask=1<<31; // make the number as 1 followed by 31 zeros
//int bit_mask=255;
int fnum=0;
printf("\n %d", INT_MIN);
printf("\n %d", INT_MAX);
printf("Fibonacci Series numbers:");
printf("\n%5d",f0);
printf("\n%5d",f1);
for(i=0;i<18;i++)
	{
	fnum=f0+f1;
	printf("\n%5d",fnum);
	f0=f1;
	f1=fnum;
	
	}
printf("\n");

printf("Binary representation of Number:");
srand(time(NULL)); 
//printf("\n value of i= %d",i2);
for(;mask>0;mask=mask>>1){
if(random_number & mask){
printf("1");
}
else{
printf("0");
}
}
i=0;srand(INT_MAX);

while(i<10){
random_between(0,0);
i++;
}
printf("random number thing:");



return 0;
}
