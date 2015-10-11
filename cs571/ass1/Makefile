all: compile_flex compile_bison link_flex_bison

compile_flex:
	flex -l calc.l

compile_bison:
	bison -dv calc.y

link_flex_bison:
	gcc -o calc calc.tab.c lex.yy.c -lfl
