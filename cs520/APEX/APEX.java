import java.util.*;
import java.io.*;
// enum InstructionType {ADD,SUB,MUL,MOVC,AND,OR,XOR,LOAD,STORE,BZ,BNZ,JUMP,BAL,HALT}
public class APEX{
	public int register[];
	public boolean registerValid[];
	boolean flagEND=false; // used to stop incrementing PC once end of instructions is reached
	Instruction[] instructions;
	public int memory[];
	public int GlobalPC;
	public int userInputCycles;
	public StageData fetch,decode,execution,memoryRead,writeback;
	public Instruction inFetch=null,inDecode=null,inEX=null, inMEM=null,inWB=null;
	public Instruction inFetchtoNext=null,inDecodetoNext=null,inEXtoNext=null, inMEMtoNext=null,inWBtoNext=null;
	//public boolean stallFe
	public APEX(){
		GlobalPC=20000;
		instructions= new Instruction[100];
		for (int i=0;i<100 ;i++ ) {
			instructions[i]=new Instruction();
		}
		register = new int[8];
		registerValid= new boolean[8];
		//hardcoding
		register[2]=100;
		register[3]=200;
		registerValid[2]=true;
		registerValid[3]=true;

		memory = new int[10000];
		inFetch= new Instruction();
		inDecode= new Instruction();
		inEX= new Instruction();
		inMEM= new Instruction();
		inWB= new Instruction();
	}

	public void processCycles(int noOfCycles){

		for(int i=0;i<noOfCycles;i++){
			System.out.print("Cycle: "+(i+1)+"\n");
			//swap memory
			doFetch();
			doDecode();
			doEX();
			doMEM();
			doWB();
			//clock tick
			
			}
		}
		public void doFetch(){
			inFetchtoNext=inFetch;
			inFetch=this.instructions[GlobalPC-20000];
			System.out.print(" Fetch ");inFetch.printRaw();
			if(instructions[GlobalPC-20000+1].contains)GlobalPC++;
			else{
				//System.out.println("Next is null");
				if(!flagEND)GlobalPC++;
					flagEND=true; // once set, never increment PC. means EOF reached and PC will point to null
				}
		}
		public void AddFU(Instruction instruction){
			System.out.println("in add fu");
			instruction.destination_data= instruction.src1_data+instruction.src2_data;
		}
		public void doDecode(){
			inDecodetoNext=inDecode;
			inDecode=inFetchtoNext;

			System.out.print(" Decode ");inDecode.printRaw();
			if(inDecode.src1!=-1 && registerValid[inDecode.src1] ){
				
				inDecode.src1_data= register[inDecode.src1];
			}
			if(inDecode.src2!=-1  && registerValid[inDecode.src2] ){

				inDecode.src2_data= register[inDecode.src2];
			}
			if(inDecode.destination!=-1 ){

				
			}
		}
		public void doEX(){
			inEXtoNext=inEX;
			inEX=inDecodetoNext;
		//if(inEX.contains){
			System.out.print(" EX ");inEX.printRaw();
			if(inEX!=null && inEX.instr_id!=null){
				switch (inEX.instr_id) {
					case MOV : 
								break;
					case ADD: AddFU(inEX);
								break;
				}
			}
		//}
		}
		public void doMEM(){
			inMEMtoNext=inMEM;
			inMEM=inEXtoNext;
		//if(inMEM.contains){
			System.out.print(" MEM ");inMEM.printRaw();

		//}
		}
		public void doWB(){
			inWB=inMEMtoNext;
		//if(inWB.contains){
			//code to writeback
			System.out.print(" WB ");inWB.printRaw();
			//move last mem instrucion here
			if(inWB!=null && inWB.destination>-1){
			register[inWB.destination]=inWB.destination_data;
			registerValid[inWB.destination]=true;
			}
		//}


		}
		public void PrintMenuWithInit(){
			System.out.println("\nOptions:\n1:\tInitialize- initializes the processor to boot up state");
			System.out.println("2:\tSimulate: Performs initialization and simulate n cycles.\n");
			System.out.println("3.\tDisplay: Content of all memory and other relevant info.");
			System.out.print("4.\tShutDown.\nInput:\t");

		}
		public void PrintMenu(){
			System.out.println("\nOptions:\n");
			System.out.println("1:\tSimulate: Performs initialization and simulate n cycles. \n");
			System.out.println("2.\tDisplay: Content of all memory and other relevant info.");
			System.out.print("3.\tShutDown.\nInput:\t");

		}
		public  void printStages(){
			System.out.println();
	// System.out.print(String.format("%10s","In fetch\t"));inFetch.print();
	// System.out.print(String.format("%10s","In decode\t"));inDecode.print();
	// System.out.print(String.format("%10s","In EX\t"));inEX.print();
	// System.out.print(String.format("%10s","In MEM\t"));inMEM.print();
	// System.out.print(String.format("%10s","In WB\t"));inWB.print();
			System.out.print(String.format("%10s","In fetch\t"));inFetch.printRaw();
			System.out.print(String.format("%10s","In decode\t"));inDecode.printRaw();
			System.out.print(String.format("%10s","In EX\t"));inEX.printRaw();
			System.out.print(String.format("%10s","In MEM\t"));inMEM.printRaw();
			System.out.print(String.format("%10s","In WB\t"));inWB.printRaw();
		}
		public void processorInit(){
			System.out.println("===Processor initialized===");
			GlobalPC=20000;	
		}
		public void displayAll(){
			System.out.println("\n\n\nProgram Counter:"+GlobalPC);
			System.out.println("\nRegisters:");
			System.out.print("R0:"+register[0]);
			System.out.print("\tR1:"+register[1]);
			System.out.print("\tR2:"+register[2]);
			System.out.print("\tR3:"+register[3]);
			System.out.print("\tR4:"+register[4]);
			System.out.print("\tR5:"+register[5]);
			System.out.print("\tR6:"+register[6]);
			System.out.print("\tR7:"+register[7]);
			System.out.println("\n\nMemory(0 to 99):");
			for(int i=0;i<100;i++){
				System.out.print(memory[i]+"\t");
				if((i+1)%10==0)System.out.println();
			}
			this.printStages();


		}
		public int userInputInit(Scanner s){
			int input = s.nextInt();
			switch (input) {
				case 1: processorInit();
				break;
				case 2: processorInit();
				System.out.println("How many cycles?");
				this.userInputCycles=s.nextInt();
				break;
				case 3:	this.displayAll();
				break;
				case 4: System.exit(0);
				break;
				default: 
				this.PrintMenuWithInit();
				input=this.userInputInit(s);
			}
			return input;
		}
		public int userInput(Scanner s){
			int input = s.nextInt();
			switch (input) {

				case 1: System.out.print("How many cycles?\t");
				this.userInputCycles=s.nextInt();
				break;
				case 2:	this.displayAll();
				break;
				case 3: System.exit(0);
				break;
				default: 
				this.PrintMenu();
				input=this.userInputInit(s);
			}
			return input;
		}
		public void complieInstructions(Instruction instruction) throws Exception{
			int literal=0;
			int noOfOperands=0;
			String string = instruction.rawString;
			StringTokenizer tokenizer = new StringTokenizer(string);
			instruction.inst_name=tokenizer.nextToken(" ").trim();
			instruction.instr_id= InstructionType.valueOf(instruction.inst_name);
			
			// System.out.println("w"+instruction.instr_id+"w");
			// System.out.println("w"+instruction.inst_name+"w");
			instruction.destination=Integer.parseInt(tokenizer.nextToken(",").replace(" ","").substring(1) );
			noOfOperands++;
			//System.out.println(instruction.destination);
			// System.out.println(tokenizer);
			//string.indexOf(",")

			if(tokenizer.hasMoreTokens()){
				String token = tokenizer.nextToken();
			//	System.out.println("Src1:");
			//	System.out.println(token);
				if(!token.contains("#")){
					int src1= Integer.parseInt(token.replace(" ","").substring(1) );
					// System.out.println(src1);
					instruction.src1=src1;
					noOfOperands++;
				}


			}
			if(tokenizer.hasMoreTokens()){

				String token = tokenizer.nextToken();
				// System.out.println("Src2:");
				//System.out.println(tokenizer.nextToken());
				if(!token.contains("#")){
					int src2= Integer.parseInt(token.replace(" ","").substring(1) );
					// System.out.println(src2);
					instruction.src2=src2;
					noOfOperands++;
				}

			}
			// System.out.println("Searching for literal");
			//System.out.println("# index"+string.indexOf("#"));
			if(string.indexOf("#")>0){
				literal = Integer.parseInt(string.substring(string.indexOf("#")+1).trim());
				instruction.literal=literal;
				noOfOperands++;
				// System.out.println("# value"+literal);
			}
			instruction.noOfOperands=noOfOperands;
			if(noOfOperands>3){
				throw new Exception("No of operands more than possible.");
			}
			//System.out.println("Count of operands"+noOfOperands);
		}
		public void processInput(){
			BufferedReader br;
			String line;
			int i=0;
			try{ 
				br = new BufferedReader(new FileReader("asm"));
				while((line = br.readLine()) != null) {
					System.out.println(i+"  "+line);
					this.instructions[i].rawString=line;
					this.instructions[i].contains=true;
					this.instructions[i].address=i+20000;
					this.complieInstructions(this.instructions[i]);
					i++;
				}
			}
			catch (FileNotFoundException e) {
				System.out.println("Input file not found.Please create a file with name \"asm\".");
				System.exit(0);
			}
			catch (IOException e) {
				System.out.println("IO error occured\n"+e);
				System.exit(0);
			}
			catch (Exception e) {
				System.out.println("Problem with instructions in the  file. Please check file and retry.\n"+e);
				System.exit(0);
			}

		}
		public static void main(String[] args){
			APEX a= new APEX();
			Scanner s= new Scanner(System.in);
			a.processInput();
			a.PrintMenuWithInit();
			int inputIndex= a.userInputInit(s);
			if(inputIndex==2);
			a.processCycles(a.userInputCycles);

			while(true){

				a.PrintMenu();
				if(a.userInput(s)==1)
					a.processCycles(a.userInputCycles);
			}

		}




	}