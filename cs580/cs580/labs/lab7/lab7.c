#include "BST.h"
#include<stdio.h>
#include<stdlib.h>

int main(int argc, char const *argv[])
{	
	FILE* file = fopen ("data.txt", "r");
	int i = 0;
	int a[100];
	int count =0,userinput=0;
	Data dx;
	Tree *bst;
	Node* node;
	bst = createTree();
	//inserting via file
	while (!feof (file))
	{  
		fscanf (file, "%d", &i);      
		a[count] =i;
		count++;
		dx.d = i;
		insertNode(bst,dx);
	}
	fclose (file);        	
	printTree(bst);
	//search the tree
	printf("\nEnter value to search(0 to exit)\n");
	scanf("%d",&userinput);
	dx.d = userinput;
	while(userinput!=0){
		node = searchTree(bst,dx);
		if(node==NULL){
			printf("\n Value not found\n");
		}
		else{

			printf("\n Value found: %d\n",node->data.d);
			if(node->parent!=NULL)
				printf("\n Parent of the node: %d\n",node->parent->data.d);
			else
				printf("\n Root node\n");
			if(node->left!=NULL)
				printf("\n Left Child of the node: %d\n",node->left->data.d);
			else
				printf("\n No left child");
			if(node->right!=NULL)
				printf("\n Right Child of the node: %d\n",node->right->data.d);
			else
				printf("\n No right child");
		}
		printf("\nEnter value to search(0 to exit)\n");
		scanf("%d",&userinput);
		dx.d = userinput;
	}

	//removing node
	printf("\nEnter value to delete(0 to exit)\n");
	scanf("%d",&userinput);
	dx.d = userinput;
	while(userinput!=0){
		removeNode(bst,dx);
		printTree(bst);
		printf("\nEnter value to delete(0 to exit)\n");
		scanf("%d",&userinput);
		dx.d = userinput;
	}
	deleteTree(bst);
	return 0;
}