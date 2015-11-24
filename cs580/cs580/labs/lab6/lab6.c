#include<stdio.h>
#include<stdlib.h>
#include <time.h>	/* time_t */
#include <sys/time.h>	/* timeval, gettimeofday() */
#include "Lists.h"
int main(){
	struct timeval start, stop;
	int i=0,n=0;;
	srand(time(0));
	time_t start_time, stop_time,final_time_geo,final_time_inc;
	int x;	
	Vector* vector = createVector();
	Data d;
	d.d=2;
	printf("\n=== PART 1 -A===\n");
	
	printf("\n Inserting 20 random elements into Vector\n");
	for(i=0;i<20;i++){
		d.d= rand()%50;
		vectorInsert(vector,i,d);
	}
	printf("\n");

	for(i=0;i<20;i++){
		x=vectorRead(vector,i);
		printf("%d\t", x);
		if((i+1)%10==0) 
			printf("\n");
	}
	vectorPrint(vector);
	for(i=0;i<20;i++){
		vectorRemove(vector,i);
	}
	vectorDelete(vector);
	
	
	printf("\n===PART 1 -B===\n");
	vector = createVector();
	//start timer	
	gettimeofday(&start, NULL); 

	//whatever you want to profile
	for(i=0;i<10000;i++){
	d.d= rand()%50;
	vectorInsert(vector,i,d);
	}
	
	//stop timer timing
	gettimeofday(&stop, NULL);
	vectorDelete(vector);
	 start_time = (start.tv_sec* 1000000) + start.tv_usec;
	 stop_time = (stop.tv_sec* 1000000) + stop.tv_usec;
	 final_time_geo = stop_time - start_time; 
	printf("\nProfiling with 10,000 insertions\nGeometric Expansion: %ld microsecond\n",final_time_geo );


	vector = createVector();
	//start timer	
	gettimeofday(&start, NULL); 
	
	//whatever you want to profile
	for(i=0;i<10000;i++){
	d.d= rand()%50;
	vectorInsertincremental(vector,i,d);
	}
	
	//stop timer timing
	gettimeofday(&stop, NULL);
	vectorDelete(vector);
	 start_time = (start.tv_sec* 1000000) + start.tv_usec;
	 stop_time = (stop.tv_sec* 1000000) + stop.tv_usec;
	 final_time_inc = stop_time - start_time; 
	printf("\nIncremental Expansion: %ld microsecond\n",final_time_inc );
	
	printf("\n===PART 2-A===\n");
	List* list  = createList();
	printf("\nInserting 10 random elements into the linked list\n");
	for (i = 0; i < 10; ++i)
	{
		d.d=rand()%50;
		insertNode(list,0,d);
		printList(list);
	}
	d.d=rand()%50;
	printf("\nInserting %d at index 20\n",d.d);
	insertNode(list,20,d);
	printList(list);
	deleteList(list);
	
	printf("\n===PART 2-B===\n");
	list  = createList();
	for (i = 0; i < 10; ++i)
	{
		d.d=rand()%50;
		insertNode(list,0,d);
	}
	printList(list);
	printf("Enter number to search\t");
	scanf("%d",&n);
	x= searchForward(list,n);
	if(x!=-1)
		printf("\nValue found(searchForward):%d\n", x);
	x= searchBackward(list,n);
	if(x!=-1)
		printf("\nValue found(searchBackward):%d\n", x);
	deleteList(list);

	printf("\n===PART 3===\n");
	Stack* stack = createStack();
	printf("Stack");
	for(i=1;i<=5;i++){
		printf("\nEnter number:\t");
		scanf("%d",&x);
		push(stack,x);
	}
	printList(stack->list);
	printf("Popping\n");
	for(i=1;i<=5;i++){
		x= pop(stack);
	printf("Popped:\t%d\n", x);
	}
	deleteStack(stack);
	

	printf("\n\nQueue");
	Queue* queue = createQueue();
	for(i=1;i<=5;i++){
		printf("\nEnter number:\t");
		scanf("%d",&x);
		enqueue(queue,x);
	}
	printList(queue->list);
	printf("Dequeuing\n");
	for(i=1;i<=5;i++){
		x= dequeue(queue);
		printf("Dequeued:\t%d\n", x);
	}
	deleteQueue(queue);

	printf("\n");
	return 0;
}
