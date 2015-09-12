#include<stdio.h>
#include<math.h>
int main(){

//double x=3.31*pow((double)10,(double)-8)*2.01*pow((double)10,(double)-7);
float cel,faht;
double ans3;
int i,j,k;
k=0;
int ans2;
double num,denom,ans1;
double x =2.55;
double ans0 = 3*pow(x,(double)3)-5*pow(x,(double)2)+6;
long int large_num = 9838263505978427528;

double no1= (double)large_num;
char c = (char)large_num;

printf("\n \nHello World!\n");
printf("\n \nEvaluating  3x^3-5x^2+6 for x=3:");
printf("\n \nAnswer: \t %f",ans0);
//	
printf("\n \nEvaluating the expression\n(3.31 × 10^-8 × 2.01 × 10^-7) / (7.16 × 10^-6 + 2.01 × 10^-8)");

num= (3.31E-8)*(2.01E-7);
denom= (7.16E-6)+(2.01E-8);
ans1= num/denom;
//printf("%lf",x);
//printf("\n %e",x);
//printf("\n \n %e",denom);
printf("\nAnswer :\t %e",ans1);
printf("\n \n Rounding up one int to even multiple of another:");
//while(k<3){
printf("\nEnter the number to be rounded up:\t");
scanf("%d",&i);
printf("Enter the number whose even multiple we need");
scanf("%d",&j);
ans2=(i+j)-i%j;
if((ans2/j)%2!=0){ans2 = ans2+j;}
printf("\n \nRounding up %d to the next even multiple of %d,\nanswer:\t %d",i,j,ans2);
//k++;
//}
printf("\n \nConversion of temperature from Fahrenheit to Celcisus");

printf("\nEnter temperature in Fahrenheit: \t");
scanf("%f",&faht);
cel=(double)((faht-32)/1.8);
printf("\nCorresponding temperature in celsiusis is:\t %f \n",cel);


printf("\n\nType conversion:");
printf("\n \t Integer:  \t  %d",large_num);
printf("\n \t Float : \t %f",no1);
printf("\n \t Float : \t %e",no1);
printf("\n \t Character: \t %c\n\n",c);
return 0;}
