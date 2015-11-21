all: 	BST.o lab7.o
	gcc -o lab7 BST.o lab7.o
	./lab7

Lists.o:	BST.c BST.h
	gcc -c BST.h BST.c

lab7.o:	lab7.c BST.h
	gcc -c BST.h lab7.c

make memtest:
		gcc lab7.c BST.c -o lab7
		valgrind  --leak-check=full ./lab7

clean:
		rm lab7 *.o *.gch