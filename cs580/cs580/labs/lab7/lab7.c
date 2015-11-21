#include "BST.h"
#include<stdio.h>
#include<stdlib.h>


int main(int argc, char const *argv[])
{
	Data value;
	Data value1;
	value.d = 42;
	Tree* bst = createTree();
	insertNode(bst,value);
	value1.d = 43;
	insertNode(bst,value1);
	value.d = 41;
	insertNode(bst,value);
	printTree(bst);
	value.d=4;
	Node* node = searchTree(bst,value);
	if(node==NULL){

		printf("\n Value not found\n");
	}
	else{

		printf("\n Value found\n");
	}
	removeNode(bst,value);
	return 0;
}