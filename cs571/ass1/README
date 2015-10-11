Group Members:
1)
Name: Shashi Upadhyay
Binghamton ID: B00627613
Section: CS 571-01
Email Address: supadhy2@binghamton.edu

2)
Name: Hardik Bagdi
Binghamton ID: B00576043
Section: CS 571-02
Email Address: hbagdi1@binghamton.edu

Whether your code was tested on bingsuns?
Yes, the code has been tested on bingsuns and runs perfectly.

The tar file contains below mentioned files
a) calc.l - This flex file contains the token information.
b) calc.y - This bison file contains the logic to parse the generated tree based on the defined grammer.
c) Makefile - This file contains the compilation and linking code of calc.l and calc.y files.
d) README - This is a text file and it contains the details of group member, code execution details and the algorithm summary.

Steps to execute the program:
1) Untar the p1-supadhy2andhbagdi1.tar file in a folder.
2) The tar file contains below mentioned files
	a) calc.l - This flex file contains the token information.
	b) calc.y - This bison file contains the logic to parse the generated tree based on the defined grammer.
	c) Makefile - This file contains the compilation and linking code of calc.l and calc.y files.
	d) README - This is a text file and it contains the details of group member, code execution details and the algorithm summary.
3) Within the same folder, type "make" on the command prompt and press enter.
4) The above mentioned command will compile the calc.l & calc.y files and will redirect the output to a executable file named calc. 
   An input text file can be used to pass the input required for the execution of the program.

Below is the code snippet of the Makefile.
all: compile_flex compile_bison link_flex_bison

Code to- 
Compile flex file	:flex -l calc.l
Compile bison file	:bison -dv calc.y
Link the files		:gcc -o calc calc.tab.c lex.yy.c -lfl
Above mentioned steps will be performed by the makefile. Just go to the source code folder and execute the make command.
  
Steps to execute the output:
Type below mentioned code from the source code folder
./calc < input - Here input file will contains the input required for the execution of executable file.
OR 
./calc  - With this process, input needs to be typed on the coomand prompt.
Control+D is used to exit the program.

Algorithm used for the program:
1) Lexical analyzer will be performed by the calc.l file. The  token details will be mentioned in the calc.l file. 
2) Syntactic analysis step will be permoed by the bison file.
3) Bison file will perform the parsing of the tokens based on the grammer mentioned in the same file.
4) A symbol table is being used to store the identifier details and their corresponding values.
     List of functions working in symbol table:
     a) lookup - to search for a given ID
     b) insert - whenever int x; float x; (also calls lookup to verify if not already previously defined)
     c) update - eg- x=3;x=5; so we need to update the value of x. first search if x is defined using lookup. and then update
     d) print - to print the value of the respective identifier.

5) Type checking logic is implemented while storing and displaying the values from the symbol table.
6) Values in the symbol table will be stored using the linked list. The pointers will be used for naviagating the linked list.
7) If the values of identifiers are modified then its corresponding value will also be updated in the linked list.
8) Below type checking complexities are handled in the program.
	a) Ignorance of the tab and multiple spaces.
	b) Name of the identifiers should not start with number.
	c) Compiler should throw error while performing algebric expression on the identifiers having mismatched data types.
	d) Assigning values to the identifier without declaring it.
    

Below is the list of the error:
1) Type mismatch- Trying to insert integer value in float identifier or vice versa.
2) Syntax Error - Identifier should be a sequence of one or more lower case letters or digits.It should never start with a digit.
3) Variable is used but not declared -	Assigning value to a variable without  declaring it.
4) Parsing Error - Using the undefined token.

All error messages will also contain the line number of the error.
