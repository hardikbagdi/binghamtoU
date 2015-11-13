#include<stdio.h>
#include<stdlib.h>
#include <time.h>	/* time_t */
#include <sys/time.h>	/* timeval, gettimeofday() */
#include "Lists.h"
//struct Data;
int main(){
	struct timeval start, stop;
	int i=0,n=0;;
	srand(0);
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
	printf("\nProfiling with 10,000 insertions\nGeometric Expansion: %ld\n",final_time_geo );


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
	printf("\nIncremental Expansion: %ld\n",final_time_inc );
	printf("\nDifference: %ld\n",final_time_inc- final_time_geo );



	//misc tests
	// vector = createVector();	
	// vectorInsert(vector,0,d);
	// vectorPrint(vector);
	
	// d.d=22;	
	// vectorInsert(vector,1,d);
	// vectorPrint(vector);
	
	// d.d=222;
	// vectorInsert(vector,2,d);
	// vectorPrint(vector);
	// vectorRemove(vector,1);
	//  x= vectorRead(vector,0);
	// printf("%d\n", x);
	//  x= vectorRead(vector,1);
	// printf("%d\n", x);
	//  x= vectorRead(vector,2);
	// printf("%d\n", x);
	// vectorPrint(vector);

	// //for( i=0;i<vector->max_size;i++){
	// vector= vectorDelete(vector);
	//}

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
		printf("\nValue found(0-indexed):%d\n", x);
	x= searchBackward(list,n);
	if(x!=-1)
		printf("\nValue found(0-indexed):%d\n", x);
	deleteList(list);

	printf("\n===PART 3===\n");
	Stack* stack = createStack();
	printf("Stack");
	for(i=1;i<=5;i++){
		printf("\nEnter number:\t");
		scanf("%d",&x);
		push(stack,x);
	}
	printf("Popping");
	for(i=1;i<=5;i++){
		x= pop(stack);
	printf("\t%d", x);
	}
	deleteStack(stack);
	

	printf("\n\nQueue");
	Queue* queue = createQueue();
	for(i=1;i<=5;i++){
		printf("\nEnter number:\t");
		scanf("%d",&x);
		enqueue(queue,x);
	}
	printf("Dequeuing");
	for(i=1;i<=5;i++){
		x= dequeue(queue);
		printf("\t%d", x);
	}
	deleteQueue(queue);

	printf("\n");
	return 0;
}
