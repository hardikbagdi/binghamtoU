#ifndef TREEFORLAB7
#define TREEFORLAB7


typedef struct _Data{
	int d;
} Data;

typedef struct _Node
{
	Data data;
	struct _Node* left;
	struct _Node* right;
	struct _Node* parent;
}Node;

typedef struct _Tree {
Node* root;
}Tree;

Tree* createTree();
void printTree(Tree * bst);
Node* createNode(Data);
void insertNode(Tree * bst, Data value);
Node * searchTree(Tree * bst, Data value);
void removeNode(Tree * bst, Data value);
#endif

