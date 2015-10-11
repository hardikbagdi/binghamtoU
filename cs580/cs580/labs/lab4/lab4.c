#include<stdio.h>
#include<stdlib.h>


int numSeconds(int ,int ,int );
enum Months{January,February,March,April,May,June,July,August,September,October,November,December};
char* monthstrings[]={"January","February","March","April","May","June","July","August","September","October","November","December"};
struct Time{
int hours;int minutes;int seconds;
};



struct DateTime{
struct Time time;
int day;
enum Months month;
int year;
};

struct Time differnce(struct Time t1,struct Time t2){
int diff=0;
int t1sec = numSeconds(t1.hours,t1.minutes,t1.seconds);

int t2sec = numSeconds(t2.hours,t2.minutes,t2.seconds);

if(t1sec>=t2sec)
diff= t1sec-t2sec;
else
diff=t2sec-t1sec;

struct Time* answer = (struct Time*)malloc(sizeof(struct Time));
answer->hours= diff/3600;
answer->minutes = (diff%3600)/60;
answer->seconds= diff%60;
//printTime(*answer);
return *answer;
}

void printTime(struct Time t){
printf("\nTime: %d hours, %d minutes, %d seconds\n",t.hours,t.minutes,t.seconds);
}


void printDateTime(struct DateTime dt){
printf("\n%9s %d %d %02d:%02d\n",monthstrings[dt.month],dt.day,dt.year,dt.time.hours,dt.time.minutes);
}
int main(){
int i;


struct Time t1[]=  {{.hours=1, .minutes=30, .seconds=45},{.hours=1, .minutes=23, .seconds=1},{.hours=0, .minutes=1, .seconds=1},{.hours=12, .minutes=0, .seconds=0}};

struct Time t2[]=  {{.hours=16, .minutes=30, .seconds=45},{.hours=12, .minutes=11, .seconds=12},{.hours=23, .minutes=59, .seconds=59},{.hours=12, .minutes=0, .seconds=0}};
//printTime(t1);
//printTime(t2);
for(i=0;i<4;i++){
//printTime(t1[i]);
//printTime(t2[i]);
printTime(differnce(t1[i],t2[i]));
}


//DateTime
struct Time time4dt[]={{12,1,0},{6,0,0},{8,22,0}};
struct DateTime dt1={.time=time4dt[0], .day=19, .month=January, .year=1809};
printDateTime(dt1);
struct DateTime dt2= {.time=time4dt[1], .day=11, .month=November, .year=1922};
printDateTime(dt2);
struct DateTime dt3= {.time=time4dt[2], .day=6, .month=January, .year=2000};
printDateTime(dt3);
//test numseconds
//int s= numSeconds(2,4,3);
//printf("%d",s);
return 0;
}


int numSeconds(int hours,int minutes,int seconds){

return hours*3600+minutes*60+seconds;
}

