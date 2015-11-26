#include "Lists.h"
#include<stdlib.h>
#include<limits.h>
#include<stdio.h>
int sum(int a,int b){return a+b;}

Vector* createVector(){
	Vector* vector = malloc(sizeof(Vector));
	if(vector==NULL)
		return;
	vector->p = malloc(sizeof(Data));
	if(vector->p==NULL)
		return;
	vector->max_size=1;
	vector->current_size=0;
}

// geometric expansion version
void vectorInsert(Vector* array,int index,Data value){
	int i=0;
	int new_size=0;
	if(array==NULL || array->p ==NULL)
		return;
	new_size=2*(array->max_size);

	if(array->max_size < (index+1)){
		
		Data *p1= malloc(new_size*sizeof(Data));
		while(( index+1) >new_size){
			
			free(p1);
			new_size=2*new_size;
			p1= malloc(new_size*sizeof(Data));
			array->max_size= new_size;
		}
		array->max_size= new_size;
		for(i=0;i< new_size;i++){
			p1[i].d=-1;
		}
		for(i=0;i<array->current_size;i++){
			p1[i].d=array->p[i].d;
		}
		free(array->p);
		array->p=p1;
	array->p[index].d=value.d;
	array->current_size=index+1;
	}
	else{
	array->p[index].d=value.d;
	array->current_size=index+1;	
	}

}

//incremental expansion version
void vectorInsertincremental(Vector* array,int index,Data value){
	int i=0;
	int new_size=0;
	if(array==NULL || array->p ==NULL)
		return;
	new_size=1+(array->max_size);

	if(array->max_size < (index+1)){
	
		Data *p1= malloc(new_size*sizeof(Data));
		while(( index+1) >new_size){
		
			free(p1);
			new_size=1+new_size;
			p1= malloc(new_size*sizeof(Data));
			array->max_size= new_size;
		}
		array->max_size= new_size;
		for(i=0;i< new_size;i++){
			p1[i].d=-1;
		}
		for(i=0;i<array->current_size;i++){
			p1[i].d=array->p[i].d;
		}
		free(array->p);
		array->p=p1;
	array->p[index].d=value.d;
	array->current_size=index+1;
	}
	else{
	array->p[index].d=value.d;
	array->current_size=index+1;	
	}
}


void vectorRemove(Vector* array,int index){
	if(array==NULL) return;
	if(index>(array->max_size-1)){
	return;
	}
	array->p[index].d=0;
}

void vectorPrint(Vector* vector){
	if(vector==NULL) return;
	int i=0;
	printf("\nVector:\nMax Size: %d\nCurrent Size: %d \n",vector->max_size,vector->current_size);
	
	for(i=0;i< vector->max_size;i++){
		printf("%d\t",vector->p[i].d);
	}
	printf("\n");
}

int vectorRead(Vector* array,int index){
	if(array==NULL) return -1;
	if (array->max_size < index)
	{
		return -1;
	}
	else{
		return array->p[index].d;
	}
}
Vector* vectorDelete(Vector* v){
	if(v==NULL) 
		return NULL;
	free(v->p);
	free(v);
	return NULL;
}



//functions for linkedlist
Node* createNode(Data d){
	Node* node =  malloc(sizeof(Node));
	if(node==NULL)
		return;
	node->data=d;
	node->next=NULL;
	node->prev=NULL;
	return node;
}
List* createList(){
	List*  list = malloc(sizeof(List));
	if(list==NULL)
		return;
	list->head= NULL;
	list->tail=NULL;
	return list;
}
Node* frontNode(List* list){
	return list->head;
}
Node* insertNode(List* list,int index,Data d){

	Node* curr= list->head;
	Node* prev;
	//case when list is empty, just insert at the head
	if(list->head==NULL){
		Node* new = createNode(d);
		list->head=new;
		list-> tail= new;
		return new;
	}
	//case when insert at the head itself
	if(index==0){
	// printf("inserting at index 0\n");
		Node* new = createNode(d);
		
		list->head->prev=new;
		new->next=list->head;
		list->head=new;
		return new;
	}
	//other cases
	int counter=0;
	int length=0;
	Node* new = createNode(d);

	while(curr!=NULL){
		
		length++;
		curr=curr->next;
	}
	// printf("length: %d\n",length );
	//if in between the list
	if(length>index){
		
		curr= list->head;
		while(counter<(index-1)){
			curr=curr->next;
			counter++;
		}
		Node * next= curr->next;
		curr->next=new;
		new->prev=curr;
		new->next= next;
		next->prev=new;
	}
	//when provided index is greater than the length of the list
	else{
		
		curr= list->head;
		while(curr->next!=NULL){
			curr=curr->next;
		}
		curr->next= new;
		new->prev=curr;
		list->tail = new;
	}
}
int removeNode(List* list,int index){
	//sanity check
	if(list==NULL || list->head==NULL) return -1;
	int returnVal;
	Node* curr= list->head;
	Node* next,*prev;
	Node* freeup;
	//delete head node
	if(index==0){
		// printf("deleting at index 0\n");
		freeup= list->head;
		returnVal=freeup->data.d;
		if(list->head->next!=NULL)
			list->head->next->prev=NULL;
		else
			list->tail=NULL;
		list->head=list->head->next;
		deleteNode(freeup);
		return returnVal;
	}
	//other cases, find length first
	int length=0, counter=0;
	while(curr!=NULL){
		
		length++;
		curr=curr->next;
	}
	//return head node list length is 1, be it any input index
	if(length==1){
		returnVal = list->head->data.d;
		deleteNode(list->head);
		list->head=NULL;
		list->tail=NULL;
		return returnVal;
	}
	// index out of bounds, delete last element
	if(index>=length){
		curr= list->head;
		while(curr!=NULL){
			prev=curr;
			curr=curr->next;
		}
		freeup=prev;
		curr=prev->prev;
		curr->next=NULL;
		list->tail=curr;
		returnVal=freeup->data.d;
		deleteNode(freeup);
		return returnVal;
	}
	else{
		curr=list->head;
		while(counter<(index-1)){
			curr=curr->next;
			counter++;
		}
		next = curr->next->next;
		freeup=curr->next;
		returnVal=freeup->data.d;
		curr->next=next;
		next-> prev = curr;
		deleteNode(freeup);
		return returnVal;

	}
}
void deleteNode(Node * node){
	if(node==NULL)
		return;
	free(node);
}
void deleteList(List* list){
	if(list==NULL)
		return;
	Node *temp;
	Node* curr = list->head;
	while(curr!=NULL){
		temp = curr->next;
		free(curr);
		curr=temp;
	}
	free(list);
}
void printList(List* list){
	if(list==NULL)
		return;
	printf("\nList\t");
	Node* curr = list->head;
	while(curr!=NULL){
		printf("(%d)-> ",curr->data.d);
		curr=curr->next;
	}
	printf("end\n");
}
void printListBackwards(List* list){
	if(list==NULL)
		return;
	printf("\nList-Backwards\n\t");
	Node* curr = list->head;
	Node* last;
	while(curr!=NULL){
		last=curr;
		curr=curr->next;
	}
	// printf("\n");
	curr=last;
	while(curr!=NULL){
		printf("(%d)-> ",curr->data.d);
		curr=curr->prev;
	}
	printf("end\n");
}

int searchForward(List* list, int value){
	if(list==NULL)
		-1;
	int index=0;
	Node* curr= list->head;
	while(curr!=NULL && curr->data.d!=value){
		curr=curr->next;
		index++;
	}
	if(curr==NULL){
		printf("\nThe value was not found.\n");
		return -1;
	}
	else{
		return index;
	}
}
int searchBackward(List* list, int value){
	if(list==NULL)
		-1;
	int index=0;
	Node* curr = list->tail;
	
	while(curr!=NULL && curr->data.d!=value){
		curr=curr->prev;
		index++;
	}
	if(curr==NULL){
		printf("\nThe value was not found.\n");
		return -1;
	}
	else{
		return index;
	}
}


Stack* createStack(){
	Stack* stack = malloc(sizeof(Stack));
	if(stack==NULL)
		return NULL;
	stack->list = createList();
	if(stack->list==NULL)
		return NULL;
	return stack;
}

void deleteStack(Stack* stack){
	if(stack==NULL)
		return;
	deleteList(stack->list);
	free(stack);
}


void push(Stack* stack,int value){
	if(stack==NULL)
		return;
	Data d;
	d.d=value;
	insertNode(stack->list,0,d);
}
int  pop(Stack* stack){
	if(stack==NULL)
		return;
	 int x=removeNode(stack->list,0);
	 if(x==-1){
	 	printf("Stack Underflow\n");
	 	return -1;
	 }
	 else
	 {
	 	return x;
	 }
}

Queue* createQueue(){
	Queue* queue = malloc(sizeof(Queue));
	if(queue==NULL)
		return NULL;
	queue->list = createList();
	if(queue->list==NULL)
		return NULL;
	return queue;
}
void deleteQueue(Queue* queue){
	deleteList(queue->list);
	free(queue);
}

void enqueue(Queue* queue,int value){
	if(queue==NULL)
		return;
	Data d;
	d.d=value;
	insertNode(queue->list,INT_MAX,d);
}
int dequeue(Queue* queue){
	if(queue==NULL)
		return;
	int x= removeNode(queue->list,0);
	if(x==-1){
	 	printf("Queue Underflow\n");
	 	return -1;
	 }
	 else
	 {
	 	return x;
	 }
}
