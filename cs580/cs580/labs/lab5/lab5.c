#include<stdio.h>
#include<stdlib.h>
#include<string.h>
int heap_usage=0;

struct Name{
	char* first_name;
	char* last_name;
	char* jedi_name;
};

void * allocate(unsigned int size)
{
	heap_usage+=size;
	//printf("\n-->Allocated. Total:%d", heap_usage);
	return calloc(size,sizeof(char));
}
void * deallocate(void *p, int size)
{
	free(p);
	heap_usage-=size;
	//printf("\n-->Deallcoated. Total:%d", heap_usage);
	return NULL;
}
char* jediName_old(char* first, char* last)
{
	//printf("%s",first);
	char* saveFirst=first;
	char* saveLast=last;
	int firstlen=0,lastlen=0;
	while(*saveFirst!='\0'){firstlen++;saveFirst++;}
	while(*saveLast!='\0'){lastlen++;saveLast++;}

	//printf("first:%s\n",first);
	char *answer=NULL;
	answer = allocate(6);
	if(answer==NULL)
		{
			printf("\nMemory allocation failed\n");
			exit(0);
		}
	char* saveAnswer=NULL;
	saveAnswer=answer;
	if(firstlen>=2 && lastlen>=3)
	{
		*answer=*last;
		answer++;
		*answer=*(last+1);
		answer++;
		*answer=*(last+2);

		answer++;
		*answer=*(first);
		answer++;
		*answer=*(first+1);
		answer++;
		*answer='\0';
		return saveAnswer;
	}
	else
	{
		// code here to handle when names are shorter
		printf("Too short");
		//saveAnswer=deallocate(saveAnswer,6);	
		return saveAnswer;
	}
	//printf("\nanswer:%s\n",saveAnswer);
}


struct Name jediName(struct Name name)
{
	char* first=name.first_name;
	char* last=name.last_name;
	char* saveFirst=name.first_name;
	char* saveLast=name.last_name;
	int firstlen=0,lastlen=0;
	while(*saveFirst!='\0'&& *saveFirst!=' ')
	{
		firstlen++;saveFirst++;
	}
	while(*saveLast!='\0' &&  *saveLast!=' ')
	{
		lastlen++;saveLast++;
	}

	//printf("first:%s\n",first);
	char *answer=NULL;
	answer = allocate(6);
	if(answer==NULL)
	{
		printf("\nMemory allocation failed\n");
		exit(0);
	}
	char* saveAnswer;
	saveAnswer=answer;
	//printf(" %d : %d", firstlen,lastlen);
	//printf("\n %d : %d", strlen(first),strlen(last));
	if(firstlen>=2 && lastlen>=4)
	{
		*answer=*last;
		answer++;
		*answer=*(last+1);
		answer++;
		*answer=*(last+2);
		answer++;
		*answer=*(first);
		answer++;
		*answer=*(first+1);
		answer++;
		*answer='\0';
	}
	else
	{
		*answer='A';
		answer++;
		*answer='J';
		answer++;
		*answer='E';
		answer++;
		*answer='D';
		answer++;
		*answer='I';
		answer++;
		*answer='\0';
		//printf("too short");
	}
	//printf("\nanswer:%s\n",saveAnswer);
	name.jedi_name=saveAnswer;
	return name;
}

int main()
{
	int i=0;
	char fullname[100];
	char *firstname=NULL;
	char *saveLastname=NULL;
	char *lastname=NULL;
	char *jedi=NULL;

	struct Name name;
	FILE *file;
	file= fopen("names.txt","r");
	int count=0;
	while(fgets(fullname, 100, file))
	{
		count++;
		firstname=allocate(50);
		lastname=allocate(50);
		//name=(struct Name*)allocate(sizeof(struct Name));
		name.first_name=firstname;
		name.last_name=lastname;
		saveLastname=lastname;
		//printf("qqqqq:\n%s",fullname);
		i=0;
		while(fullname[i]!=',')
		{
		//	printf("\n%d",i);
			firstname[i]=fullname[i];
			i++;
		}
		firstname[i]='\0';
		printf("\n\nFullname: %s",fullname);
		//memcpy(firstname,fullname,i);*(firstname+i)='\0';
		//printf("=====%c",fullname[i]);
		i++;
		while(fullname[i]!='\n' )//|| fullname[i]!='\r')
		{
		//	printf("\n%c",fullname[i]);
			*(lastname++)=fullname[i++];
		}
		lastname[i]='\0';
		lastname=saveLastname;
		//printf("names obtained");
		name.jedi_name=NULL;
		name=jediName(name);
		//printf("\nfirst: %s",name.first_name);// printf("\n%d",strlen(name.first_name));
		//fflush(stdout);
		//printf("\nlast: %s",name.last_name); //printf("\n%d",strlen(name.last_name));
		//fflush(stdout);
		printf("\nJedi Name : %s\n",name.jedi_name);	
		memset(name.first_name, '\0', 50);
		memset(name.last_name, '\0', 50);
		//memset(name.jedi_name, 'A', 50);

		name.first_name=deallocate(name.first_name,50);
		name.last_name=deallocate(name.last_name,50);
		name.jedi_name=deallocate(name.jedi_name,6);
		//deallocate(name,sizeof(struct Name));
	
	}
	
	printf("\n\n Total Heap Usage:%d\n",heap_usage);
	//printf("\n no of names:%d\n",count);
	fclose(file);
	return 0;
}
