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
int random_number;
int number=INT_MIN;
int i=0;
int decimal_number=0;
int random_number2=0;
int count=0;
unsigned int mask=1<<31; // make the number as 1 followed by 31 zeros
//int bit_mask=255;
int fnum=0;
//printf("\n %d", INT_MIN);
//printf("\n %d", INT_MAX);


// FIBONACCI SERIES: 


printf("Fibonacci Series first 20 numbers:");
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



//CONVERSION BINARY NUMBERS:

printf("\n\nBinary representation of Number:"); 
printf("\n");
decimal_number=2;

printf("Decimal: %d\nBinray:\t",decimal_number);
for(;mask>0;mask=mask>>1){
if(decimal_number & mask){
printf("1");
}
else{
printf("0");
}
}
printf("\n");
mask=1<<31;
decimal_number=255;

printf("Decimal: %d\nBinary:\t",decimal_number);
for(;mask>0;mask=mask>>1){
if(decimal_number & mask){
printf("1");
}
else{
printf("0");
}
}

printf("\n");
mask=1<<31;
decimal_number=32;

printf("Decimal: %d\nBinary:\t",decimal_number);
for(;mask>0;mask=mask>>1){
if(decimal_number & mask){
printf("1");
}
else{
printf("0");
}
}

printf("\n");
mask=1<<31;
decimal_number=INT_MIN;

printf("Decimal: %d\nBinary:\t",decimal_number);
for(;mask>0;mask=mask>>1){
if(decimal_number & mask){
printf("1");
}
else{
printf("0");
}
}
printf("\n");
mask=1<<31;
decimal_number=INT_MAX;

printf("Decimal: %d\nBinary:\t",decimal_number);
for(;mask>0;mask=mask>>1){
if(decimal_number & mask){
printf("1");
}
else{
printf("0");
}
}

mask=1<<31;
decimal_number=INT_MIN;
printf("\n");
printf("Decimal: %d\nBinary:\t",decimal_number);
for(;mask>0;mask=mask>>1){
if(decimal_number & mask){
printf("1");
}
else{
printf("0");
}
}

//RANDOM BINARY NUMBERS:

printf("\n\n\nRandom integer to Binary:");
srand(time(0));
random_number=rand();


mask=1<<31;
printf("\n");
printf("Decimal: %d\nBinary:\t",random_number);
for(;mask>0;mask=mask>>1){
if(random_number & mask){
printf("1");
}
else{
printf("0");
}
}
printf("\n");
return 0;
}
