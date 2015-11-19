all: 	Lists.o lab6.o
	gcc -o lab6 Lists.o lab6.o
	./lab6

Lists.o:	Lists.c Lists.h
	gcc -c Lists.h Lists.c

lab6.o:	lab6.c Lists.h
	gcc -c Lists.h lab6.c

make memtest:
		gcc lab6.c Lists.c -o lab6
		valgrind  --leak-check=full ./lab6

clean:
		rm lab6 *.o *.gch