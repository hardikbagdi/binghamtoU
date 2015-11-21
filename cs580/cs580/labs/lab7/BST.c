#include "BST.h"
#include<stdio.h>
#include<stdlib.h>

Node* createNode(Data d){
	Node* node = malloc(sizeof(Node));
	node->data=d;
	node->left=NULL;
	node->right=NULL;
	node->parent=NULL;
	return node;
}
Node* deleteNode(Node *node){
	free(node);
	return NULL;
}
Tree* createTree(){
	Tree* tree= malloc(sizeof(Tree));
	tree->root = NULL;
	return tree;

}


void printHelper(Node* current){
	if(current==NULL){
		return;
	}
	else{
		printHelper(current->left);
		printf("%d\t", current->data.d);
		printHelper(current->right);
	}
}
void printTree(Tree* bst){
	printf("Inorder Traversal:\n");
	if(bst->root==NULL){
		printf("\n Empty Tree.\n");
	}
	else{
		printHelper(bst->root);
	}
}


void insertHelper(Node * current, Data value){
	printf("inside inserthelper:%d\n",value.d);
	Node *node;

	if(current->data.d == value.d){
		printf("\n Cannot insert duplicate value.\n");
	}
	else if(current->data.d < value.d){
			if(current->left==NULL){

				current->left= createNode(value);
			}
			else{
				insertHelper(current->left,value);
			}
	}
	else{
			if(current->right==NULL){
				current->right= createNode(value);
			}
			else{
				insertHelper(current->left,value);
			}
	}
}

void insertNode(Tree * bst, Data value){
	printf("inside insert\n");
	if(bst->root==NULL){
		Node* r = createNode(value);
		bst->root=r;
	}
	else{
		insertHelper(bst->root,value);
	}
}

Node* searchHelper(Node* current,Data value){
	if(current==NULL){
		return NULL;
	}
	else if(current->data.d == value.d){
		return current;
	}
	else if(current->data.d > value.d){
		return searchHelper(current->right,value);
	}
	else{
		return searchHelper(current->left,value);
	}
}
Node* searchTree(Tree * bst, Data value){
	if(bst->root==NULL){
		printf("Empty Tree\n");
	}
	else{
		return searchHelper(bst->root,value);
	}
}
void removeHelper(Node* current,Data value){
	if(current->data.d == value.d){
		if(current->left == NULL && current->right == NULL){
			
		}
		else if(current->left == NULL){

		}
		else if(current->right==NULL){

		}
		else{

		}
	}
	else if(current->data.d > value.d){
		removeHelper(current->right,value);
	}
	else{
		removeHelper(current->left,value);
	}
}

void removeNode(Tree * bst, Data value){
	if(bst->root==NULL){
		printf("\n Cannot delete from an empty tree.\n");
	}
	if(searchTree(bst,value)==NULL){
		printf("\n Data to delete not present in tree.\n");
	}
	else{
		removeHelper(bst->root,value);
	}
}