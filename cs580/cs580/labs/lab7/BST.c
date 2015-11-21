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
	else if(current->data.d > value.d){
			if(current->left==NULL){
				node =createNode(value);
				current->left= node;
				node->parent = current;
			}
			else{
				 insertHelper(current->left,value);
			}
	}
	else{
			if(current->right==NULL){
				node =createNode(value);
				current->right= node;
				node->parent = current;
			}
			else{
				insertHelper(current->right,value);
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
	else if(current->data.d < value.d){
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
void removeLeaf(Node* node){
	printf("\nin remove leaf\n");
	Node* parent = node->parent;
	if(parent->left->data.d== node->data.d){
	deleteNode(node);
	parent->left=NULL;
	}
	else{
		deleteNode(node);
	parent->right=NULL;
	}
}
void shortCircuit(Node* node){
	Node* parent = node->parent;
	if(node->left != NULL){
		if(parent->left->data.d == node->data.d){
			parent->left=node->left;
			deleteNode(node);
		}
		else{
			parent->right=node->left;
			deleteNode(node);
		}
	}
	else if(node->right != NULL){
		if(parent->left->data.d == node->data.d){
			parent->left=node->right;
			deleteNode(node);
		}
		else{
			parent->right=node->right;
			deleteNode(node);
		}
	}

}
void promotion(Node* node){
	printf("\n in remove promotion.\n");
	Node* parent = node->parent;
	Node* current= node->left;
	while(current->right!=NULL){
		current=current->right;
	}
	node->data=current->data;
	if(current->left == NULL && current->right == NULL){
			removeLeaf(current);
		}
		else if(current->left == NULL || current->right==NULL){
			shortCircuit(current);
		}
}

void removeHelper(Node* current,Data value){
		printf("\n in remove helper.\n");
	if(current->data.d == value.d){
		if(current->left == NULL && current->right == NULL){
			removeLeaf(current);
		}
		else if(current->left == NULL || current->right==NULL){
			shortCircuit(current);
		}
		else{
			promotion(current);
		}
	}
	else if(current->data.d < value.d){
		removeHelper(current->right,value);
	}
	else{
		removeHelper(current->left,value);
	}
}

void removeNode(Tree * bst, Data value){
		printf("\n in remove.\n");
	if(bst->root==NULL){
		printf("\n Cannot   delete from an empty tree.\n");
	}
	if(searchTree(bst,value)==NULL){
		printf("\n Data to delete not present in tree.\n");
	}
	else{
		removeHelper(bst->root,value);
	}
}
Node* deleteTreeHelper(Node* current){
	if(current->left!=NULL){
		deleteTreeHelper(current->left);
	}
	if(current->right!=NULL){
		deleteTreeHelper(current->right);
	}
	deleteNode(current);
}

Tree* deleteTree(Tree* bst){
	if(bst->root!=NULL){
		deleteTreeHelper(bst->root);
		free(bst);
	}
	return NULL;
}