#include "BST.h"
#include<stdio.h>
#include<stdlib.h>


int main(int argc, char const *argv[])
{
	Data value;
	Data value1;
	value.d = 5;
	Tree* bst = createTree();
	insertNode(bst,value);
	value1.d = 3;
	insertNode(bst,value1);
	value.d = 4;
	insertNode(bst,value);
	value.d = 2;
	insertNode(bst,value);
	value.d = 1;
	insertNode(bst,value);
	value.d = 7;
	insertNode(bst,value);
	value.d = 6;
	insertNode(bst,value);
	value.d = 8;
	insertNode(bst,value);
	value.d = 9;
	insertNode(bst,value);
	printTree(bst);
	value.d=1;
	Node* node = searchTree(bst,value);
	if(node==NULL){

		printf("\n Value not found\n");
	}
	else{

		printf("\n Value found: %d\n",node->data.d);
		printf("\n Parent of the node: %d\n",node->parent->data.d);
		if(node->left!=NULL)
			printf("\n Left Child of the node: %d\n",node->left->data.d);
		else
			printf("\n No left child");
		if(node->right!=NULL)
			printf("\n Right Child of the node: %d\n",node->right->data.d);
		else
			printf("\n No right child");
	}
	removeNode(bst,value);
	printTree(bst);
	value.d=5;
	removeNode(bst,value);
	value.d=4;
	node = searchTree(bst,value);
	if(node==NULL){

		printf("\n Value not found\n");
	}
	else{

		printf("\n Value found: %d\n",node->data.d);
		if(node->parent!=NULL)
		printf("\n Parent of the node: %d\n",node->parent->data.d);
		else
			printf("\n Node has no root.\n");
		if(node->left!=NULL)
			printf("\n Left Child of the node: %d\n",node->left->data.d);
		else
			printf("\n No left child");
		if(node->right!=NULL)
			printf("\n Right Child of the node: %d\n",node->right->data.d);
		else
			printf("\n No right child");
	}
	printTree(bst);
	deleteTree(bst);
	return 0;
}