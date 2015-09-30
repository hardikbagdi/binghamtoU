#include<stdio.h>
#include<stdlib.h>
#define EQUAL ==
#define TRUE 1
#define FALSE 0

#if TRUE
#define DEBUG(s) TRACE;fprintf(stderr,"\t %s",s);
#define TRACE fprintf(stderr,"%d", __LINE__)
#else
#define DEBUG(s) 
#define TRACE 
#endif


int myStrStr (char  haystack[], char needle[], char buffer[]){

int i=0,j=0;
int k;
int r;
while(haystack[i]!='\0'){

	if(haystack[i]==needle[j]){
		buffer[j]=needle[j];
		//printf("\nouter %c",needle[j]);
		j++;k=i;r=i;k++;
			while(needle[j]!='\0'){
				if(haystack[k]!=needle[j]) break;
				//printf("\n inner %c",needle[j]);
				buffer[j]=needle[j];
				j++;k++;
				
			}
			if(needle[j]=='\0'){
				buffer[j]=needle[j];
			//	printf("\nstring found at %d",r);
				
				return 0;
			}
	j=0;
	}

i++;
}
printf("String not found");
return 1;

}


int main(){
int result;
DEBUG("HELLO");

//char haystack[20],needle[20],buffer[20];
//fprintf(stderr,"%d", __LINE__);
printf("\n");


char haystack[20];
char needle[20];
char buffer[10]="";
printf("\nEnter the haystack string:");
scanf("%s",haystack);
printf("\nEnter the needle string:");
scanf("%s",needle);
result =myStrStr(haystack,needle,buffer);
printf("\nHaystack:%s \nNeedle:%s \nBuffer:%s\n",haystack,needle,buffer);

return 0;
}
