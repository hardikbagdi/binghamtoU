#ifndef LISTSFORLAB6
#define LISTSFORLAB6

typedef struct _Data{
	int d;
} Data;

typedef struct _Vector
{
	Data *p;
	int current_size;
	int max_size;
}Vector;

typedef struct _Node
{
	Data data;
	struct _Node* next;
	struct _Node* prev;
}Node;

typedef struct _List{
	Node* head;
}List;

typedef struct _Stack
{
	List* list;
	
}Stack;

typedef struct _Queue
{
	List *list;
}Queue;
//functions for vectors
Vector* createVector();
void vectorInsert(Vector* array,int index,Data value);
void vectorRemove(Vector* array,int index);
int vectorRead(Vector* array,int index);
Vector* vectorDelete(Vector* v);
void vectorPrint(Vector* vector);
void vectorInsertincremental(Vector* array,int index,Data value);


//functions for linkedlist
Node* createNode();
List* createList();
Node* frontNode(List*);
Node* insertNode(List*,int,Data);
int removeNode(List*,int);
void deleteNode(Node * node);
void deleteList(List* list);
int searchForward(List*, int);
int searchBackward(List*, int);

//functions for Stack
Stack* createStack();
void deleteStack(Stack*);
void push(Stack*,int);
int pop(Stack*);


//functions for Queue
Queue* createQueue();
void deleteQueue(Queue*);
void enqueue(Queue*,int);
int dequeue(Queue*);
#endif
