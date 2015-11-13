/*
ToDo's
-EOF loop problem
-handle memory dependecy.
-BAL R1,R2, #literal moves Return Address(next address) to R1 and goes to R2+literal.
-One stage in Decode to read after WB.
-redirect all debug to err
*/
import java.util.*;
import java.io.*;

public class APEX{
	//CPU representations
	public int PSW_Z=-1;
	public int register[];
	public boolean registerValid[];
	public int invaliditySetBy[];
	public int memory[];
	public int GlobalPC;
	//hold the instructions of the input file
	Instruction[] instructions;

	//hold pipeline details in real time
	public Instruction inFetch=null,inDecode=null,inEX=null, inMEM=null,inWB=null;
	//passing on. work as pipeline registers.
	public Instruction inFetchtoNext=null,inDecodetoNext=null,inEXtoNext=null, inMEMtoNext=null;

	//various flags to handle message passing 
	public boolean stalled=false;
	boolean stallFetch=false,stallDecode=false,stallEX=false,stallMEM=false,stallWB=false;
	
	public int userInputCycles; // take input for no of cycles to simulate.
	boolean flagEND=false; // used to stop incrementing PC once end of instructions is reached
	
	//constructor
	public APEX(){
		GlobalPC=20000;
		instructions= new Instruction[100];
		for (int i=0;i<100 ;i++ ) {
			instructions[i]=new Instruction();
		}
		register = new int[8];
		registerValid= new boolean[8];
		invaliditySetBy= new int[8];
		//hardcoding
		//register[2]=100;
		//register[3]=200;
		for(int i=0;i<8;i++) 
			registerValid[i]=true;
		memory = new int[10000];
		for(int i=0;i<8;i++) 
			invaliditySetBy[i]=-1;
		//inFetch= new Instruction();
		//	inDecode= new Instruction();
		//	inEX= new Instruction();
		//	inMEM= new Instruction();
		//	inWB= new Instruction();
	}

	public void processCycles(int noOfCycles){

		for(int i=0;i<noOfCycles;i++){
			System.out.print("Cycle: "+(i+1)+"\n");
			//swap memory
			if(GlobalPC<20000) {
				System.out.println("\n Invalid PC valuee");
				System.exit(0);
			}
			doFetch();
			System.err.println("=====decode starts");
			doDecode();
			System.err.println("=====decode ends");
			doEX();
			doMEM();
			doWB();
			//clock tick
			
		}
	}
	
	public boolean checkDecodeStall(Instruction inDecode){
		if(inDecode==null )
			return false;
		if(inDecode.src1!=-1 && !registerValid[inDecode.src1]){
			return true;
		}
		if(inDecode.src2!=-1 && !registerValid[inDecode.src2]){
			return true;
		}
		if(inDecode.destination!=-1 && !registerValid[inDecode.destination] ){
			return true;
		}
		return false;
	}

	public void doFetch(){
			// System.out.println("-->(from fetch)value of decode stall"+checkDecodeStall());
			System.out.println("-->(stalled"+stalled);
			System.out.println("-->(flagEND"+flagEND);
		
		if(stalled ) return;
			inFetchtoNext=inFetch;
		if(flagEND) inFetch=null;
		//	if(checkDecodeStall(inFetchtoNext)) return;
		inFetch=this.instructions[GlobalPC-20000];
		System.out.print(GlobalPC+" Fetch "+inFetch);//inFetch.printRaw();
		//check if there is a next instruction. if yes, then incremenet the counter. else increment the counter 
		// and set end of file flag. we will never increment the counter beyond this now.
		if(instructions[GlobalPC-20000+1].contains){
				GlobalPC++;
		}
		else{
			if(!flagEND){GlobalPC++;
				flagEND=true;} // once set, never increment PC. means EOF reached and PC will point to null
		}
	}

	public void doDecode(){
		System.err.println(inDecode);
		if(inDecode!=null){
		//System.err.print(" Decode "+inDecode+"\t\t\t"+inDecode.address);
			if(stalled){
				inDecodetoNext=null;
				//System.err.println("\n\n\n\n recheckingforstall\n\n\n\n"+inDecode);
				if(checkDecodeStall(inDecode))
					 return;							
				//System.err.println("\n\n\n\n executing\n\n\n\n");
				stalled=false;
		
				if(inDecode.src1!=-1){
					inDecode.src1_data= register[inDecode.src1];
					registerValid[inDecode.src1]=false;
					invaliditySetBy[inDecode.src1]=inDecode.address;
				}
				if(inDecode.src2!=-1 ){
					inDecode.src2_data= register[inDecode.src2];
					registerValid[inDecode.src2]=false;
					invaliditySetBy[inDecode.src2]=inDecode.address;
				}
				if(inDecode.destination!=-1 ){
					registerValid[inDecode.destination]=false;
					invaliditySetBy[inDecode.destination]=inDecode.address;
				}
						
				}
		else{
				System.err.println("\n\n\n\n no stall in last\n\n\n\n");
				inDecodetoNext=inDecode;
				inDecode=inFetchtoNext;
				if(checkDecodeStall(inDecode)){
					stalled=true;
				}
				else{
					stalled=false;
					if(inDecode.src1!=-1){
						inDecode.src1_data= register[inDecode.src1];
						registerValid[inDecode.src1]=false;
						invaliditySetBy[inDecode.src1]=inDecode.address;
					}
					
					if(inDecode.src2!=-1 ){
						inDecode.src2_data= register[inDecode.src2];
						registerValid[inDecode.src2]=false;
						invaliditySetBy[inDecode.src2]=inDecode.address;
					}

					if(inDecode.destination!=-1 ){
						registerValid[inDecode.destination]=false;
						invaliditySetBy[inDecode.destination]=inDecode.address;
					}
				}

			}
		}
	 else{
			System.err.println("\n\n\n\nw\n\n\n\n");
			inDecodetoNext=inDecode;
			inDecode=inFetchtoNext;
			if(inDecode!=null){
				//System.err.println("\n\n\n\nfirst\n\n\n\n");
				if(checkDecodeStall(inDecode)){
					
					//System.err.println("\n\n\n\nfirst stalling\n\n\n\n");
					stalled=true;
				}
				else{
					stalled=false;
					if(inDecode.src1!=-1){
						inDecode.src1_data= register[inDecode.src1];
						registerValid[inDecode.src1]=false;
						invaliditySetBy[inDecode.src1]=inDecode.address;
					}
					
					if(inDecode.src2!=-1 ){
						inDecode.src2_data= register[inDecode.src2];
						registerValid[inDecode.src2]=false;
						invaliditySetBy[inDecode.src2]=inDecode.address;
					}
					
					if(inDecode.destination!=-1 ){
						registerValid[inDecode.destination]=false;
						invaliditySetBy[inDecode.destination]=inDecode.address;
					}
				}
			}	
		}

	}

	public void doEX(){
		if(!stallEX && !stallMEM && !stallWB){
			inEXtoNext=inEX;		
			inEX=inDecodetoNext;
				if(inEX!=null && inEX.instr_id!=null){
			System.err.print(" EX "+inEX);//inEX.printRaw();
					switch (inEX.instr_id) {
						//do nothing
						case MOVC: 
						case LOAD:
						case STORE:
						case HALT:
									break;
						case ADD: AddFU(inEX);
						break;
						case MUL: MulFU(inEX);
						break;
						case SUB: SubFU(inEX);
						break;
						case AND: AndFU(inEX);
						break;
						case XOR: XorFU(inEX);
						break;
						case OR: OrFU(inEX);
						break;
						case BNZ: 
									if(PSW_Z!=0){ 
								 		branchFlush();
										GlobalPC=register[inEX.destination];
									}
									break;
						case BZ: 
								 	if(PSW_Z==0){
								 		branchFlush();
								 		GlobalPC=register[inEX.destination];

								 	}
									break;
						case BAL: 	branchFlush();
									stalled=false;
									register[inEX.destination]=inEX.address+1;
									GlobalPC=register[inEX.src1]+inEX.literal;
									break;
						case JUMP:	branchFlush();
									GlobalPC= register[inEX.destination];
									break;
						
					}
				}
		}
		else{
			inEXtoNext=null;
		}
	}

	public void branchFlush(){
		for(int i=0;i<8;i++){
			if(invaliditySetBy[i]==inDecode.address || invaliditySetBy[i]==inEX.address){
				invaliditySetBy[i]=-1;
				registerValid[i]=true;
			}
		}
		inDecode=null;inFetch=null;inFetchtoNext=null;inDecodetoNext=null;
	}
	
	public void AddFU(Instruction instruction){
		System.err.println("in add fu");
		int operand2=0;
		operand2=  instruction.src2!= -1 ? instruction.src2_data : instruction.literal;
		instruction.destination_data= instruction.src1_data+operand2;
	}
	
	public void MulFU(Instruction instruction){
		System.err.println("in Mul fu");
		int operand2=0;
		operand2=  instruction.src2!= -1 ? instruction.src2_data : instruction.literal;
		instruction.destination_data= instruction.src1_data*operand2;
	}
	
	public void SubFU(Instruction instruction){
		System.err.println("in sub fu");
		int operand2=0;
		operand2=  instruction.src2!= -1 ? instruction.src2_data : instruction.literal;
		instruction.destination_data= instruction.src1_data-operand2;
		if(instruction.destination_data==0)PSW_Z=0;
	}

	public void AndFU(Instruction instruction){
		System.err.println("in AND fu");
		int operand2=0;
		operand2=  instruction.src2!= -1 ? instruction.src2_data : instruction.literal;
		instruction.destination_data= instruction.src1_data & operand2;
		if(instruction.destination_data==0)PSW_Z=0;
	}
	
	public void OrFU(Instruction instruction){
		System.err.println("in OR fu");
		int operand2=0;
		operand2=  instruction.src2!= -1 ? instruction.src2_data : instruction.literal;
		instruction.destination_data= instruction.src1_data | operand2;
		if(instruction.destination_data==0)PSW_Z=0;
	}
	
	public void XorFU(Instruction instruction){
		System.err.println("in XOR fu");
		int operand2=0;
		operand2=  instruction.src2!= -1 ? instruction.src2_data : instruction.literal;
		instruction.destination_data= instruction.src1_data ^ operand2;
		if(instruction.destination_data==0)PSW_Z=0;
	}
	
	public void doMEM(){
		inMEMtoNext=inMEM;
		inMEM=inEXtoNext;
		System.err.print(" MEM "+inMEM+"\n");//inMEM.printRaw();
		if(inMEM!=null && inMEM.instr_id!=null){
			System.err.println("Debugging NPE"+inMEM);
			switch (inMEM.instr_id) {
				//do nothing
				case MOVC: 	inMEM.destination_data=inMEM.literal;
							break;
				case LOAD: 
				case STORE: LoadStoreFU(inMEM);
							break;
				default:
			}
		}
		
		
	}

	void LoadStoreFU(Instruction instrucion){
		if(instrucion.instr_id==InstructionType.LOAD){
			instrucion.destination_data=memory[instrucion.src1_data+instrucion.literal];
		}	
		else if(instrucion.instr_id==InstructionType.STORE){
			memory[instrucion.src1_data+instrucion.literal]=register[instrucion.destination];

		}
	}

	public void doWB(){
		inWB=inMEMtoNext;
		if(inWB!=null){
	
			//code to writeback
				System.err.print(" WB "+inWB+"\n");//inWB.printRaw();
			//move last mem instrucion here
				// perform check if not a store too :remainng
			
			switch (inWB.instr_id) {
						case HALT:	this.displayAll();
									System.out.println("HALT INSTRUCTION ENCOUNTERED(in WB)");
									System.exit(0);
									break;
						case MOVC: 
						case LOAD:
						case ADD: 
						case MUL:				
						case SUB: 
						case AND: 
						case XOR:
						case OR: 
								register[inWB.destination]=inWB.destination_data;
								break;
						
						case STORE:
						case BNZ:
						case BZ:
						case BAL:
						case JUMP: 
								break;
				}

				if(inWB.src1!=-1){
						
						registerValid[inWB.src1]=true;
					}
					if(inWB.src2!=-1 ){
						
						registerValid[inWB.src2]=true;
					}
					if(inWB.destination!=-1 ){
						registerValid[inWB.destination]=true;
					}
				
				
		}
	}


	public void PrintMenuWithInit(){
		System.out.println("\nOptions:\n1:\tInitialize- initializes the processor to boot up state");
		System.out.println("2:\tSimulate: Performs initialization and simulate n cycles.\n");
		System.out.println("3.\tDisplay: Content of all memory and other relevant info.");
		System.out.print("4.\tShutDown.\nInput:\t");

	}
	public void PrintMenu(){
		System.out.println("\nOptions:\n");
		System.out.println("1:\tSimulate: Simulate n cycles. \n");
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
		System.out.print(String.format("%10s","In fetch\t"));System.out.print(inFetch+"\n");
		System.out.print(String.format("%10s","In decode\t"));System.out.print(inDecode+"\n");
		System.out.print(String.format("%10s","In EX\t"));System.out.print(inEX+"\n");
		System.out.print(String.format("%10s","In MEM\t"));System.out.print(inMEM+"\n");
		System.out.print(String.format("%10s","In WB\t"));System.out.print(inWB+"\n");
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
		System.out.print("\n");
		for(int i=0;i<8;i++){
			System.out.print(registerValid[i]+"\t");
		}
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
		if(instruction.instr_id==InstructionType.HALT) return;
	// System.err.println("w"+instruction.instr_id+"w");
	// System.err.println("w"+instruction.inst_name+"w");
		instruction.destination=Integer.parseInt(tokenizer.nextToken(",").replace(" ","").substring(1) );
		noOfOperands++;
	//System.err.println(instruction.destination);
	// System.err.println(tokenizer);
	//string.indexOf(",")

		if(tokenizer.hasMoreTokens()){
			String token = tokenizer.nextToken();
	//	System.err.println("Src1:");
	//	System.err.println(token);
			if(!token.contains("#")){
				int src1= Integer.parseInt(token.replace(" ","").substring(1) );
			// System.err.println(src1);
				instruction.src1=src1;
				noOfOperands++;
			}


		}
		if(tokenizer.hasMoreTokens()){

			String token = tokenizer.nextToken();
		// System.err.println("Src2:");
		//System.err.println(tokenizer.nextToken());
			if(!token.contains("#")){
				int src2= Integer.parseInt(token.replace(" ","").substring(1) );
			// System.err.println(src2);
				instruction.src2=src2;
				noOfOperands++;
			}

		}
	// System.err.println("Searching for literal");
	//System.err.println("# index"+string.indexOf("#"));
		if(string.indexOf("#")>0){
			literal = Integer.parseInt(string.substring(string.indexOf("#")+1).trim());
			instruction.literal=literal;
			noOfOperands++;
		// System.err.println("# value"+literal);
		}
		instruction.noOfOperands=noOfOperands;
		if(noOfOperands>3){
			throw new Exception("No of operands more than possible.");
		}
	//System.err.println("Count of operands"+noOfOperands);
	}
	public void processInput(){
		BufferedReader br;
		String line;
		int i=0;
		try{ 
			br = new BufferedReader(new FileReader("asm.txt"));
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
			System.out.println("Input file not found.Please create a file with name \"asm.txt\".");
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
		try{
			PrintStream ps = new PrintStream("log");
			System.setErr(ps);
		}
		catch (Exception e) {
			System.out.println("Log file doesn't exist");
		}

		APEX a= new APEX();
		//System.err.println("QW");
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
			// a.displayAll();
		}
	}
}