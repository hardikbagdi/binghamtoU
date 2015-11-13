all: Lists.h Lists.c lab6.c
		gcc lab6.c Lists.c -o lab6
		./lab6
make memtest:
		gcc lab6.c Lists.c -o lab6
		valgrind  --leak-check=full ./lab6

clean:
		rm lab6