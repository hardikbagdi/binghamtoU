#include<stdio.h>
#include<time.h>

struct Team{
char * name;
int handicap;
};



struct Team* game(struct Team*,struct Team*);
struct Team* tournament(struct Team*[]);

int main(){
//clrscr();
srand(time(0));
struct Team t1={"Team1",rand()%10};
struct Team t2={"Team2",rand()%10};
struct Team t3={"Team3",rand()%10};
struct Team t4={"Team4",rand()%10};

struct Team t5={"Team5",rand()%10};
struct Team t6={"Team6",rand()%10};
struct Team t7={"Team7",rand()%10};
struct Team t8={"Team8",rand()%10};

struct Team* league[8]={&t1,&t2,&t3,&t4,&t5,&t6,&t7,&t8};

tournament(league);
printf("\n");
return 0;
}


struct Team* game(struct Team* team1 ,struct Team* team2){
//srand(time(0));
int score1= rand() % 10;

int score2= rand() % 10;
while(score1==score2) score2=rand()%10;
//printf("%d",factor);
printf("%s(Score:%d) vs %s(Score:%d) ",team1->name,score1,team2->name,score2);

score1+=team1->handicap;
score2+=team2->handicap;
printf("Handicap Ratio(%d:%d)",team1->handicap,team2->handicap);
if(score1<score2)
{
printf("\nWinner: %s!!\n",team2->name);
return team2;
}
else
{
printf("\nWinner: %s!!\n",team1->name);
return team1;
}
}



struct Team* tournament(struct Team* teams[]){
int i,j=0;
struct Team* result_round1[4];
struct Team* result_round2[2];
struct Team* winner;
printf("\n===Round 1===\n");
for( i=0,j=0;i<7;i=i+2,j++){
result_round1[j]=game(teams[i],teams[i+1]);
}
//test print loop
//for(i=0;i<4;i++) printf("\n%s\n",result_round1[i]->name);
printf("\n===Round 2===\n");
for( i=0,j=0;i<4;i=i+2,j++){
result_round2[j]=game(result_round1[i],result_round1[i+1]);
}

printf("\n===Round 3===\n");
winner=game(result_round2[0],result_round2[1]);



}
