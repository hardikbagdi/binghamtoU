#include "Lists.h"
#include<stdlib.h>
#include<limits.h>
#include<stdio.h>
int sum(int a,int b){return a+b;}

Vector* createVector(){
	Vector* vector = malloc(sizeof(Vector));
	vector->p = malloc(sizeof(Data));
	vector->max_size=1;
	vector->current_size=0;
}

// geometric expansion version
void vectorInsert(Vector* array,int index,Data value){
	int i=0;
	int new_size=0;
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
	
	new_size=1+(array->max_size);

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
Node* createNode(){
	Node* node =  malloc(sizeof(Node));
	node->data.d=-1;
	node->next=NULL;
	node->prev=NULL;
	return node;
}
List* createList(){
	List*  list = malloc(sizeof(List));
	list->head= NULL;
	return list;
}
Node* frontNode(List* list){
	return list->head;
}
Node* insertNode(List* list,int index,Data d){

	Node* curr= list->head;
	Node* prev;
	if(list->head==NULL){
		Node* new = createNode();
		new->data=d;
		list->head=new;
		return new;
	}

	if(index==0){
	
		Node* new = createNode();
		new->data=d;
		new->next=curr;
		curr->prev=new;
		list->head=new;
		return new;
	}
	
	int counter=0;
	int length=0;
	Node* new = createNode();
	new->data=d;
	while(curr!=NULL){
		
		length++;
		curr=curr->next;
	}
	if(length>=index){
		
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
	else{
		
		curr= list->head;
		while(curr->next!=NULL){
			curr=curr->next;
		}
		curr->next= new;
		new->prev=curr;
	}
}
int removeNode(List* list,int index){
	if(list==NULL || list->head==NULL) return -1;
	int returnVal;
	Node* curr= list->head;
	Node* next,*prev;
	Node* freeup;
	if(index==0){
		freeup= list->head;
		returnVal=freeup->data.d;
		list->head=list->head->next;
		deleteNode(freeup);
		return returnVal;
	}
	
	int length=0, counter=0;
	while(curr!=NULL){
		
		length++;
		curr=curr->next;
	}
	if(length==1){
		returnVal = list->head->data.d;
		deleteNode(list->head);
		list->head=NULL;
		return returnVal;
	}
	
	if(index>length){
		curr= list->head;
		while(curr!=NULL){
			prev=curr;
			curr=curr->next;
		}
		freeup=prev;
		curr=prev->prev;
		curr->next=NULL;
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
		deleteNode(freeup);
		return returnVal;

	}
}
void deleteNode(Node * node){
	free(node);
}
void deleteList(List* list){
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
	printf("\nList\t");
	Node* curr = list->head;
	while(curr!=NULL){
		printf("(%d)-> ",curr->data.d);
		curr=curr->next;
	}
	printf("end\n");
}
void printListBackwards(List* list){
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
	int index=0;
	Node* curr = list->head;
	Node* last;
	while(curr!=NULL){
		last=curr;
		curr=curr->next;
	}
	curr=last;
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
	stack->list = createList();
	return stack;
}

void deleteStack(Stack* stack){
	deleteList(stack->list);
	free(stack);
}


void push(Stack* stack,int value){
	Data d;
	d.d=value;
	insertNode(stack->list,0,d);
}
int  pop(Stack* stack){
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
	queue->list = createList();
	return queue;
}
void deleteQueue(Queue* queue){
	deleteList(queue->list);
	free(queue);
}

void enqueue(Queue* queue,int value){
	Data d;
	d.d=value;
	insertNode(queue->list,INT_MAX,d);
}
int dequeue(Queue* queue){
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