#include<stdio.h>
#define EQUAL ==
#define TRUE 1
#define FALSE 0
#define DEBUG(s) TRACE;fprintf(stderr,"%s",s);
#define TRACE fprintf(stderr,"%d", __LINE__)

int myStrStr (char  haystack[], char needle[], char buffer[]){

int i=0,j=0;
int k;
int r;
while(haystack[i]!='\0'){

	if(haystack[i]==needle[j]){
		printf("\nouter %c",needle[j]);
		j++;k=i;r=i;k++;
			while(needle[j]!='\0'){
				if(haystack[k]!=needle[j]) break;
				printf("\n inner %c",needle[j]);
				j++;k++;
			}
			if(needle[j]=='\0'){
				printf("\nstring found at %d",r);
				return 1;
			}
	j=0;
	}

i++;
}
printf("String not found");
return 0;

}


int main(){
int result;
DEBUG("HELLO");
//fprintf(stderr,"%d", __LINE__);
printf("\n\n\n\n\n");
result =myStrStr("orange","ge","");

return 0;
}
