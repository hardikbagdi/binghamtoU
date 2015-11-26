import java.util.*;
import java.io.*;

public class ApexOOO {
	// CPU representations
	public int PSW_Z = -1;
	// arch registers
	public int ar_register[];
	// physical registers
	public int register[];
	// status array for the ROB physical register
	public boolean registerValid[];
	// renamed array
	public boolean renamed[];
	// to flush & undo during branching
	public int invaliditySetBy[];
	// memory
	public int memory[];
	// PC counter - next fetch
	public int GlobalPC;
	// hold the instructions of the input file
	Instruction[] instructions;
	// load store queue. Size 4
	Instruction[] loadStoreQueue;
	// issue queue. Size 8.
	Instruction[] issuequeue;
	int currentSizeIQ;
	// reorder buffer of size 16
	Queue<Instruction> reorderBuffer;
	// free list of physical registers
	Queue<Integer> freelist;
	// Register Alias Table
	HashMap<Integer, Integer> RAT;
	// bit to decide if pointing to ARF or PRF
	int[] RATdecision; // 0 means ARF , 1 means PRF
	// hold pipeline details in real time
	public Instruction inFetch = null, inDecode = null, toIntFu = null, toMulFU = null, inExIntFU = null,
			inExMulFU = null, inMEM = null, inWB = null;
	// passing on. work as pipeline registers.
	public Instruction inFetchtoNext = null, inDecodetoNext = null, toMEM = null, inMEMtoNext = null;
	// various flags to handle message passing
	public boolean stalled = false;
	// free up register method in Decode(case 2) TODO verify if free up possible
	// in this case
	public boolean freeUpRegisterInDecode = false;
	public int RegisterToFree = -1;
	// following not used currently
	boolean stallFetch = false, stallDecode = false, stallEXAdd = false, stallEXMul = false, stallMEM = false,
			stallWB = false;
	// take input for no of cycles to simulate.
	public int userInputCycles;
	// used to stop incrementing PC once end of instructions is reached
	boolean flagEND = false;

	// keeps track of Mul FU cycles - non-pipelined
	public int multimer = 0;
	// signals that MUL completed in the current cycle(and hence has to be
	// forwarded to MEM
	public boolean mulcompletes;

	// constructor
	public ApexOOO() {
		GlobalPC = 20000;
		instructions = new Instruction[1000];
		ar_register = new int[8];
		register = new int[16];
		registerValid = new boolean[16];
		renamed = new boolean[16];
		invaliditySetBy = new int[16];
		loadStoreQueue = new Instruction[4];
		issuequeue = new Instruction[8];
		currentSizeIQ = 0;
		reorderBuffer = new LinkedList<Instruction>();
		freelist = new LinkedList<Integer>();
		RATdecision = new int[8];
		// memory
		memory = new int[10000];
		// _-Cache
		for (int i = 0; i < 1000; i++) {
			instructions[i] = new Instruction();
		}
		//
		RAT = new HashMap<Integer, Integer>();
		for (int i = 0; i < 8; i++) {
			RAT.put(i, i);
			RATdecision[i] = 0; // initially all committed values. So,everything
			// points to ARF
		}
		// TODO confirm if initially all entries in RAT point to ARF as there
		// will be no physical stand in for any register and hence no register
		// will be valid(in the next for loop)
		for (int i = 0; i < 16; i++) {
			freelist.add(i);
		}

		for (int i = 0; i < 16; i++)
			registerValid[i] = false;

		for (int i = 0; i < 9; i++)
			invaliditySetBy[i] = -1;
		stalled = false;

	}

	public void processCycles(int noOfCycles) {

		for (int i = 0; i < noOfCycles; i++) {
			System.out.print("Cycle: " + (i + 1) + "\n");
			// swap memory
			if (GlobalPC < 20000) {
				System.out.println("Invalid PC Value:" + GlobalPC);
				System.exit(-1);

			}
			doFetch();
			doDecode();
			issueQueueCheckIssueAndForward();
			doEX();
			doMEM();
			doWB();
			retirement();
			// clock tick

		}
	}

	// fetch stage
	public void doFetch() {
		System.out.println("===Stalled from fetch:" + stalled);
		if (flagEND && stalled)
			return;

		if (stalled)
			return;

		inFetchtoNext = inFetch;
		if (flagEND)
			inFetch = null;
		inFetchtoNext = inFetch;
		inFetch = this.instructions[GlobalPC - 20000];
		// check if there is a next instruction. if yes, then increment the
		// counter. else increment the counter
		// and set end of file flag. we will never increment the counter beyond
		// this now.
		if (instructions[GlobalPC - 20000 + 1].contains) {
			GlobalPC++;
		} else {
			if (!flagEND) {
				GlobalPC++;
				flagEND = true;
			} // once set, never increment PC. means EOF reached and PC will
				// point to null
		}
	}

	// decode stage followed by helper functions for it
	public void doDecode() {

		// these check SHOULD NOT BE PERFORMED FOR JUMP, BZ and may be a few
		// others
		// check if free slot in IQ and a free register is available,
		// other wise stall
		System.out.println("===Stalled from decode:" + stalled);

		// stalled = checkDecodeStall(inDecode);
		if (!stalled) {
			inDecodetoNext = inDecode;
			inDecode = inFetchtoNext;
			if (inDecode != null) {

				if (currentSizeIQ == 8 || reorderBuffer.size() == 16) {
					System.err.println("no IQ or ROB, current IQ size:" + currentSizeIQ + " reorder buffer size: "
							+ reorderBuffer.size());
					stalled = true;
					return;

				} else if (inDecode.destination != -1 && freelist.size() == 0) {
					System.err.println("no free register");
					stalled = true;
					return;
				}
				renameAndRead(inDecode);
			}
		} else {
			return;
		}
	}

	// TODO verify the rename logic. definitely a  bug.
	// executes only if Decode not stalling. renames and attempts to read
	// sources. sets flags. renames destination and allocate a PR. update RAT.
	// allocate IQ & ROB
	public void renameAndRead(Instruction instruction) {
		// Building the renamed instructions
		StringBuilder sb = new StringBuilder();
		sb.append(instruction.inst_name + " ");
		// rename and try to read sources
		if (instruction.src1 != -1) {
			instruction.renamedSrc1 = RAT.get(instruction.src1);
			if (RATdecision[instruction.src1] == 0) {
				instruction.src1_data = ar_register[instruction.renamedSrc1];
				instruction.src1valid = true;
			} else {
				if (registerValid[instruction.renamedSrc1] == true) {
					instruction.src1_data = register[instruction.renamedSrc1];
					instruction.src1valid = true;
				} else {
					instruction.src1valid = false;
				}
			}
		}
		if (instruction.src2 != -1) {
			instruction.renamedSrc2 = RAT.get(instruction.src2);
			if (RATdecision[instruction.src2] == 0) {
				instruction.src2_data = ar_register[instruction.renamedSrc2];
				instruction.src2valid = true;
			} else {
				if (registerValid[instruction.renamedSrc2] == true) {
					instruction.src2_data = register[instruction.renamedSrc2];
					instruction.src2valid = true;
				} else {
					instruction.src2valid = false;
				}
			}
		}
		// TODO biggest confirmations, read data in decode when RAT points to
		// ARF for a source, so no register as such
		// rename destination and update RAT other flags
		if (instruction.destination != -1) {
			// TODO verify allocation of various resources
			int x = freelist.remove();
			renamed[x] = false;
			registerValid[x] = false;
			int currentStandin = RAT.get(instruction.destination);
			if (RATdecision[instruction.destination] == 1)
				renamed[currentStandin] = true;
			RAT.put(instruction.destination, x);
			RATdecision[instruction.destination] = 1;
			instruction.renamedDestination = x;

			sb.append("P" + instruction.renamedDestination);
			
			// below 'if' will be removed as we will have to commit everything

			// if (registerValid[currentStandin]) {
			// freeUpRegisterInDecode = true; // opportunity to free up a
			// // register if the the old stand
			// // in has been renamed (Case 1)
			// RegisterToFree = currentStandin;
			// } else {
			// freeUpRegisterInDecode = false;
			// }
		}
		if (instruction.src1 != -1) {
			if (RATdecision[instruction.src1] != 0)
				sb.append(" P" + instruction.renamedSrc1);
			else {
				sb.append("(" + instruction.src1_data + ")");
			}
		}
		if (instruction.src2 != -1) {
			if (RATdecision[instruction.src2] != 0)
				sb.append(" P" + instruction.renamedSrc2);
			else {
				sb.append("(" + instruction.src2_data + ")");
			}
		}
		if (instruction.literal != -1) {
			sb.append(" " + instruction.literal);
		}
		instruction.renamedString = sb.toString();
		// TODO renaming the instruction string. not handled for X right now.
		// set renamed string
		// set Issuable condition if all the required sources have been read in
		instruction.isReadyForIssue = true;
		// set isIssuable if both the sources are ready.
		if (instruction.src1 != -1)
			instruction.isReadyForIssue = instruction.src1valid;
		if (instruction.src2 != -1)
			instruction.isReadyForIssue &= instruction.src2valid;

		// TODO confirm assumption: instruction once decoded is put into the
		// issue queue entry and ROB entry by the end of the cycle in which it
		// got decoded
		// put into the empty slot in the IQ
		for (int i = 0; i < 8; i++) {
			if (issuequeue[i] == null) {
				issuequeue[i] = instruction;
				break;
			}

		}
		++currentSizeIQ;
		// put into the reorder buffer
		reorderBuffer.add(instruction);

	}

	// put items in issue queue and check for forwarded data in each cycle and
	// issue instructions
	public void issueQueueCheckIssueAndForward() {
		// TODO handle stalls in EX due to resource contention. when EX stalls,
		// nothing goes from IQ to EX
		Instruction instruction = null;
		toIntFu = null;
		// toMulFU = null;
		// check forwarded data and read in wherever required.
		for (int i = 0; i < 8; i++) {

		}
		for (int i = 0; i < 8; i++) {
			// set to null, just a pipeline register

			instruction = issuequeue[i];
			if (instruction != null) {
				++instruction.ageInIQ;
			}
		}
		// check instructions ready for issue
		for (int i = 0; i < 8; i++) {

			instruction = issuequeue[i];
			if (instruction != null) {

				instruction.isReadyForIssue = true;
				// set isIssuable if both the sources are ready.
				if (instruction.src1 != -1)
					instruction.isReadyForIssue = instruction.src1valid;
				if (instruction.src2 != -1)
					instruction.isReadyForIssue &= instruction.src2valid; // TODO
				// check
				// if
				// logic
				// works.
				// TODO possible intFUstall boolean condition here to check if
				// int fu stalled when resource contention takes place
				if (instruction.isReadyForIssue && instruction.FUtype == 0 && toIntFu == null && instruction.ageInIQ > 1
						&& multimer + 1 != 4) {
					toIntFu = issuequeue[i];
					System.err.println("sending out to int EX stage" + toIntFu);
					issuequeue[i] = null;
					currentSizeIQ--;
					break;
				}
				// TODO verify, have put breaks, serializedstart ups.
				if (instruction.isReadyForIssue && instruction.FUtype == 1 && toIntFu == null
						&& instruction.ageInIQ > 1) {
					toMulFU = issuequeue[i];
					issuequeue[i] = null;
					currentSizeIQ--;
					break;
				}
			}
		}
	}

	public void doEX() {
		// timer for MUL non-pipelined delay(4)
		if (inExMulFU != null)
			multimer++;
		if (multimer == 4 && inExMulFU!=null) {
			mulcompletes = true;
		}

		// TODO handle resource conflicts
		// toMEM = inExIntFU;
		// TODO experimental mul completes. mul given higher priority. if mul
		// completes, then it is forwarded to MEM and int FU stalls
		// TODO confirm if serialized ex to mem is ok
		if (mulcompletes && !stallMEM && !stallWB) {
			System.err.println("MUL COMPLETED and sending to MEM" + inExMulFU);
			// TODO forward either INT or MUL to MEM ie serialized flow from ex to mem. obvious.
			toMEM = inExMulFU;
			inExMulFU = null;
			mulcompletes=false;
			return;
			// above return stalls int fu whenever mul FU outputs to MEM
		}
		// if (inExIntFU != null) {
		toMEM = inExIntFU;
		inExIntFU = null;
		// }
		if (toMulFU != null && toMulFU.instr_id != null && inExMulFU == null) {
			inExMulFU = toMulFU;
			toMulFU = null;
			multimer = 0;
			MulFU(inExMulFU);
			System.err.println(" Starting mul exec " + inExMulFU);
		}

		// for all instructions other than MUL
		if (!mulcompletes && toIntFu != null) {
			inExIntFU = toIntFu;
			toIntFu = null;
		}
		if (inExIntFU != null && inExIntFU.instr_id != null) {
			System.err.println(" EX in int FU " + inExIntFU);// inEX.printRaw();
			// main execution
			switch (inExIntFU.instr_id) {
			// do nothing
			case MOVC:
			case MOV:
				Mov(inExIntFU);
				break;
			case LOAD:
			case STORE:
			case HALT:
				break;
			case ADD:
				AddFU(inExIntFU);
				break;
			// TODO move to mul FU separate
			case MUL:
				MulFU(inExMulFU);
				break;
			case SUB:
				SubFU(inExIntFU);
				break;
			case AND:
				AndFU(inExIntFU);
				break;
			case XOR:
				XorFU(inExIntFU);
				break;
			case OR:
				OrFU(inExIntFU);
				break;
			case BNZ:
				if (PSW_Z != 0) {
					branchFlush();
					GlobalPC = inExIntFU.address + inExIntFU.literal; // relative
					// Addressing
				}
				break;
			case BZ:
				if (PSW_Z == 0) {
					branchFlush();

					GlobalPC = inExIntFU.address + inExIntFU.literal;

				}
				break;
			case BAL:
				branchFlush();
				stalled = false; // hack. can go wrong!!!
				register[8] = inExIntFU.address + 1;
				GlobalPC = inExIntFU.src1_data + inExIntFU.literal; // absolute
				// addressing
				break;
			case JUMP:
				branchFlush();
				stalled = false; // hack. can go wrong!!!
				GlobalPC = inExIntFU.src1_data + inExIntFU.literal;// absolute
				// addressing
				break;

			}
			// TODO add forwarding logic here
		}
	}

	public void Mov(Instruction instruction) {
		if (instruction.instr_id == InstructionType.MOVC) {
			instruction.destination_data = instruction.literal;
		} else if (instruction.instr_id == InstructionType.MOV) {
			instruction.destination_data = register[instruction.renamedSrc1];
		}
	}

	// for squashing instruction upon branch resolutions
	public void branchFlush() {
		for (int i = 0; i < 8; i++) {
			if (invaliditySetBy[i] == inDecode.address || invaliditySetBy[i] == inExIntFU.address) {
				invaliditySetBy[i] = -1;
				registerValid[i] = true;
			}
		}
		inDecode = null;
		inFetch = null;
		inFetchtoNext = null;
		inDecodetoNext = null;
	}

	public void AddFU(Instruction instruction) {
		System.err.println("in add fu");
		int operand2 = 0;
		operand2 = instruction.src2 != -1 ? instruction.src2_data : instruction.literal;
		instruction.destination_data = instruction.src1_data + operand2;
		if (instruction.destination_data == 0)
			PSW_Z = 0;
		else
			PSW_Z = -1;
	}

	// needs non pipelined 4 cycle latency
	public void MulFU(Instruction instruction) {
		if (multimer == 4) {
			System.err.println("\n ==========>>>MUL COMPLETES");
			mulcompletes = true;
		}
		System.err.println("in Mul fu");
		int operand2 = 0;
		operand2 = instruction.src2 != -1 ? instruction.src2_data : instruction.literal;
		instruction.destination_data = instruction.src1_data * operand2;
	}

	public void SubFU(Instruction instruction) {
		System.err.println("in sub fu");
		int operand2 = 0;
		operand2 = instruction.src2 != -1 ? instruction.src2_data : instruction.literal;
		instruction.destination_data = instruction.src1_data - operand2;
		if (instruction.destination_data == 0)
			PSW_Z = 0;
		else
			PSW_Z = -1;
	}

	public void AndFU(Instruction instruction) {
		System.out.println("in OR fu");
		int operand2 = 0;
		operand2 = instruction.src2 != -1 ? instruction.src2_data : instruction.literal;
		instruction.destination_data = instruction.src1_data & operand2;
		if (instruction.destination_data == 0)
			PSW_Z = 0;
		else
			PSW_Z = -1;
	}

	public void OrFU(Instruction instruction) {
		System.out.println("in OR fu");
		int operand2 = 0;
		operand2 = instruction.src2 != -1 ? instruction.src2_data : instruction.literal;
		instruction.destination_data = instruction.src1_data | operand2;
		if (instruction.destination_data == 0)
			PSW_Z = 0;
		else
			PSW_Z = -1;
	}

	public void XorFU(Instruction instruction) {
		System.out.println("in OR fu");
		int operand2 = 0;
		operand2 = instruction.src2 != -1 ? instruction.src2_data : instruction.literal;
		instruction.destination_data = instruction.src1_data ^ operand2;
		if (instruction.destination_data == 0)
			PSW_Z = 0;
		else
			PSW_Z = -1;
	}

	// MEM stage & its helper functions
	public void doMEM() {
		inMEMtoNext = inMEM;
		inMEM = toMEM;
		// TODO add forwarding logic here
		System.err.print("===>>> MEM " + inMEM + "\n");// inMEM.printRaw();
		if (inMEM != null && inMEM.instr_id != null) {
			System.err.println("Debugging NPE" + inMEM);
			switch (inMEM.instr_id) {
			// do nothing
			case MOVC:
				inMEM.destination_data = inMEM.literal;
				break;
			case MOV:
				inMEM.destination_data = register[inMEM.src1];
				break;
			case LOAD:
			case STORE:
				LoadStoreFU(inMEM);
				break;
			default:
			}
		}

	}

	void LoadStoreFU(Instruction instruction) {
		if (instruction.instr_id == InstructionType.LOAD) {
			if (instruction.literal == -1) {

				instruction.destination_data = memory[instruction.src1_data + instruction.src2_data];
			} else {

				instruction.destination_data = memory[instruction.src1_data + instruction.literal];
			}
		}

		else if (instruction.instr_id == InstructionType.STORE) {
			if (instruction.literal == -1) {

				memory[instruction.src1_data + instruction.src2_data] = register[instruction.destination];

			} else {

				memory[instruction.src1_data + instruction.literal] = register[instruction.destination];
			}

		}
	}

	// WB stage
	public void doWB() {
		inWB = inMEMtoNext;
		if (inWB != null && inWB.instr_id != null) {
			// TODO add forwarding logic here
			inWB.writtenBack = true;
			// code to writeback
			System.err.print(" WB " + inWB + "\n");// inWB.printRaw();
			// move last memory instruction here
			// perform check if not a store too :remaining

			switch (inWB.instr_id) {
			case HALT:
				// TODO Halt will go through write back. execution stops when
				// HALT in commited.

				// this.displayAll();
				// System.out.println("HALT INSTRUCTION ENCOUNTERED(in WB)");
				// System.exit(0);
				break;
			case MOVC:
			case MOV:
			case LOAD:
			case ADD:
			case MUL:
			case SUB:
			case AND:
			case XOR:
			case OR:
				register[inWB.renamedDestination] = inWB.destination_data;

				registerValid[inWB.renamedDestination] = true;
				break;

			case STORE:
			case BNZ:
			case BZ:
			case BAL:
			case JUMP:
				break;
			}

			if (inWB.src1 != -1) {

				registerValid[inWB.src1] = true;
			}
			if (inWB.src2 != -1) {

				registerValid[inWB.src2] = true;
			}
			if (inWB.destination != -1) {
				registerValid[inWB.destination] = true;
			}

		}
	}

	// retirement logic for ROB, takes place during WB
	public void retirement() {
		// TODO only case to free up register when we retire an instruction &
		// register has been renamed.
		// confirm if an instruction has to spend minimum one instruction in ROB
		// or if it is possible then it can be directly committed to the ARF,
		// right now it spends minimum 1 cycle in ROB

		if (reorderBuffer.size() > 0 && reorderBuffer.peek().isReadyForCommit) {
			Instruction retiring = reorderBuffer.remove();
			//setting renmaed string back to null as the instrution has completed. renaming from scratch if the same instruction is executed again in future
			retiring.renamedString=null;
			if (retiring.destination != -1) {
				register[retiring.destination] = -1;
				ar_register[retiring.destination] = retiring.destination_data;
				freelist.add(retiring.renamedDestination);
				RAT.put(retiring.destination, retiring.destination);
				RATdecision[retiring.destination] = 0;
				registerValid[retiring.renamedDestination] = false;
			}
		}
		for (Instruction i : reorderBuffer) {
			// hack, could have directly set the isReadyforCommit but then
			// instruction would've directly committed to and not spent min 1
			// cycle in ROB
			i.isReadyForCommit = i.writtenBack;
		}
	}

	// freeing up a register on commit of an instruction, during WB only
	public void freeUpRegister(int r) {
		register[r] = -1;
		registerValid[r] = false;
		renamed[r] = false;
		freelist.add(r);
	}

	// TODO add forwarded tag and data value
	// display function and helpers
	public void displayAll() {
		System.out.println(
				"-----------------------------------------------------------------------------------------------------------------------------");
		System.out.println("\n\n\nProgram Counter:" + GlobalPC);
		System.out.println("\nPSW-Zero(only 0 is 0):" + PSW_Z);
		printRenameTable();
		printRegisters();
		printIssueQueue();
		this.printStages();
		// this.printFUs();
		System.out.println("\n\nMemory(0 to 10):");
		for (int i = 0; i < 10; i++) {
			System.out.print(memory[i] + "\t");
			if ((i + 1) % 10 == 0)
				System.out.println();
		}
		printreorderbuffer();

	}

	private void printreorderbuffer() {
		// TODO Auto-generated method stub
		System.out.println("\n\nReorder Buffer:");
		// System.out.println(reorderBuffer);
		// printing ready for commit bool val
		System.out.print("\n[");
		for (Instruction i : reorderBuffer) {
			System.out.print(i + ":" + i.isReadyForCommit + ",");
		}
		System.out.print("]");

	}

	public void printRegisters() {
		System.out.println("\nArchtectural Registers:");
		System.out.print("R0:" + ar_register[0]);
		System.out.print("\tR1:" + ar_register[1]);
		System.out.print("\tR2:" + ar_register[2]);
		System.out.print("\tR3:" + ar_register[3]);
		System.out.print("\tR4:" + ar_register[4]);
		System.out.print("\tR5:" + ar_register[5]);
		System.out.print("\tR6:" + ar_register[6]);
		System.out.print("\tR7:" + ar_register[7]);
		System.out.format("\n%15s\t", "Phy Register");
		System.out.print("R0:" + register[0]);
		System.out.print("\tR1:" + register[1]);
		System.out.print("\tR2:" + register[2]);
		System.out.print("\tR3:" + register[3]);
		System.out.print("\tR4:" + register[4]);
		System.out.print("\tR5:" + register[5]);
		System.out.print("\tR6:" + register[6]);
		System.out.print("\tR7:" + register[7]);
		System.out.print("\tR8:" + register[8]);
		System.out.print("\tR9:" + register[9]);
		System.out.print("\tR10:" + register[10]);
		System.out.print("\tR11:" + register[11]);
		System.out.print("\tR12:" + register[12]);
		System.out.print("\tR13:" + register[13]);
		System.out.print("\tR14:" + register[14]);
		System.out.print("\tR15:" + register[15]);
		System.out.format("\n\n%15s\t", "Allocated bit");
		for (int i = 0; i < 16; i++) {
			System.out.print(!freelist.contains(i) + "\t");
		}
		System.out.format("\n\n%15s\t", "Status bit");
		for (int i = 0; i < 16; i++) {
			System.out.print(registerValid[i] + "\t");
		}
		System.out.format("\n%15s\t", "Renamed bit");
		for (int i = 0; i < 16; i++) {
			System.out.print(renamed[i] + "\t");
		}

	}

	// Various print functions
	public void printIssueQueue() {
		System.out.println("\n\nIssue Queue:\n");
		for (int i = 0; i < 8; i++) {

			System.out.print(issuequeue[i]);
			if (issuequeue[i] != null) {
				System.out.print("\tIssuable:" + issuequeue[i].isReadyForIssue);
			}
			System.out.print("\n");
		}

	}

	public void printFUs() {
		System.out.print(String.format("%10s", "In Int FU\t"));
		System.out.println(inExIntFU);
		System.out.print(String.format("%10s", "In Mul FU\t"));
		System.out.print(inExMulFU);
		if (inExMulFU != null)
			System.out.print("\tCycle: " + multimer);
		System.out.println();
	}

	public void printRenameTable() {
		System.out.println();
		System.out.println("Registe Alias Table:");
		char decide1, decide2;
		decide1 = RATdecision[0] == 0 ? 'R' : 'P';
		decide2 = RATdecision[4] == 0 ? 'R' : 'P';
		System.out.println("R0->" + decide1 + RAT.get(0) + "\t\tR4->" + decide2 + RAT.get(4));
		decide1 = RATdecision[1] == 0 ? 'R' : 'P';
		decide2 = RATdecision[5] == 0 ? 'R' : 'P';
		System.out.println("R1->" + decide1 + RAT.get(1) + "\t\tR5->" + decide2 + RAT.get(5));
		decide1 = RATdecision[2] == 0 ? 'R' : 'P';
		decide2 = RATdecision[6] == 0 ? 'R' : 'P';
		System.out.println("R2->" + decide1 + RAT.get(2) + "\t\tR6->" + decide2 + RAT.get(6));
		decide1 = RATdecision[3] == 0 ? 'R' : 'P';
		decide2 = RATdecision[7] == 0 ? 'R' : 'P';
		System.out.println("R3->" + decide1 + RAT.get(3) + "\t\tR7->" + decide2 + RAT.get(7));
	}

	public void printStages() {
		System.out.println();
		System.out.print(String.format("%10s", "In fetch\t"));
		System.out.print(inFetch + "\n");
		System.out.print(String.format("%10s", "In decode\t"));
		System.out.print(inDecode + "\n");
		System.out.print(String.format("%10s", "In EX\n"));
		this.printFUs();
		System.out.print(String.format("%10s", "In MEM\t"));
		System.out.print(inMEM + "\n");
		System.out.print(String.format("%10s", "In WB\t"));
		System.out.print(inWB + "\n");
	}

	// initializing processor. TODO copy over from constructor
	public void processorInit() {
		System.out.println("===Processor initialized===");
		GlobalPC = 20000;
	}

	// command line user interfacing- 4 functions
	public void PrintMenuWithInit() {
		System.out.println("\nOptions:\n1:\tInitialize- initializes the processor to boot up state");
		System.out.println("2:\tSimulate: Performs initialization and simulate n cycles.\n");
		System.out.println("3.\tDisplay: Content of all memory and other relevant info.");
		System.out.print("4.\tShutDown.\nInput:\t");

	}

	public void PrintMenu() {
		System.out.println("\nOptions:\n");
		System.out.println("1:\tSimulate: Simulate n cycles. \n");
		System.out.println("2.\tDisplay: Content of all memory and other relevant info.");
		System.out.print("3.\tShutDown.\nInput:\t");

	}

	public int userInputInit(Scanner s) {
		int input;
		input = s.nextInt();

		switch (input) {
		case 1:
			processorInit();
			break;
		case 2:
			processorInit();
			System.out.println("How many cycles?");
			this.userInputCycles = s.nextInt();
			break;
		case 3:
			this.displayAll();
			break;
		case 4:
			System.exit(0);
			break;
		default:
			this.PrintMenuWithInit();
			input = this.userInputInit(s);
		}
		return input;
	}

	public int userInput(Scanner s) {
		int input = s.nextInt();
		switch (input) {

		case 1:
			System.out.print("How many cycles?\t");
			this.userInputCycles = s.nextInt();
			break;
		case 2:
			this.displayAll();
			break;
		case 3:
			System.exit(0);
			break;
		default:
			this.PrintMenu();
			input = this.userInputInit(s);
		}
		return input;
	}

	// converting input file into an Instruction[]
	public void complieInstructions(Instruction instruction) throws Exception {
		String d;
		String string = instruction.rawString;
		instruction.FUtype = 0;
		StringTokenizer tokenizer = new StringTokenizer(string, " ");
		instruction.inst_name = tokenizer.nextToken();
		// hack for EX-OR to be added
		// using try catch
		if (instruction.inst_name.equals("EX-OR")) {
			instruction.instr_id = InstructionType.XOR;
		} else {
			instruction.instr_id = InstructionType.valueOf(instruction.inst_name);
		}

		if (instruction.instr_id == InstructionType.HALT)
			return;

		if (instruction.instr_id == InstructionType.MUL)
			instruction.FUtype = 1;
		if (tokenizer.hasMoreTokens()) {
			if (instruction.instr_id == InstructionType.JUMP) {
				d = tokenizer.nextToken();
				if (d.equals("X")) {
					instruction.src1 = 16;
				} else if (d.charAt(0) != 'R') {
					instruction.literal = Integer.parseInt(d);
					// System.out.println("JUMP/BZ/BNZ" + d);
					return;
				} else {
					instruction.src1 = Integer.parseInt(d.substring(1));
				}
			} else {

				d = tokenizer.nextToken();
				if (d.equals("X")) {
					instruction.destination = 16;
				} else if (d.charAt(0) != 'R') {
					instruction.literal = Integer.parseInt(d);
					// System.out.println("JUMP/BZ/BNZ" + d);
					return;
				} else {
					instruction.destination = Integer.parseInt(d.substring(1));
				}
			}
		}

		if (tokenizer.hasMoreTokens()) {
			d = tokenizer.nextToken();
			if (d.charAt(0) != 'R') {
				instruction.literal = Integer.parseInt(d);
				return;
			} else if (d.equals("X")) {
				instruction.src1 = 8;
			} else {
				instruction.src1 = Integer.parseInt(d.substring(1));
			}
		}
		if (tokenizer.hasMoreTokens()) {

			d = tokenizer.nextToken();
			if (d.charAt(0) != 'R') {
				instruction.literal = Integer.parseInt(d);
				return;
			} else {
				instruction.src2 = Integer.parseInt(d.substring(1));
			}
		}
	}

	// process the input file.
	public void processInput() {
		BufferedReader br;
		String line;
		int i = 0;
		try {
			br = new BufferedReader(new FileReader("asm.txt"));
			while ((line = br.readLine()) != null) {
				System.out.println(i + " " + line);
				this.instructions[i].rawString = line;
				this.instructions[i].contains = true;
				this.instructions[i].address = i + 20000;
				this.complieInstructions(this.instructions[i]);
				instructions[i].print();
				i++;
			}
		} catch (FileNotFoundException e) {
			System.out.println("Input file not found.Please create a file with name \"asm.txt\".");
			System.exit(0);
		} catch (IOException e) {
			System.out.println("IO error occured\n" + e);
			System.exit(0);
		} catch (Exception e) {
			System.out.println("Problem with instructions in the  file. Please check file and retry.\n" + e);
			System.exit(0);
		}

	}

	public static void main(String[] args) {
		// err log file, add redirection when complete TODO
		// try{
		// PrintStream ps = new PrintStream("log");
		// System.setErr(ps);
		// }
		// catch (Exception e) {
		// System.out.println("Log file doesn't exist");
		// }

		ApexOOO a = new ApexOOO();
		// System.err.println("QW");
		Scanner s = new Scanner(System.in);
		a.processInput();
		a.PrintMenuWithInit();
		int inputIndex = a.userInputInit(s);
		if (inputIndex == 2)
			;
		a.processCycles(a.userInputCycles);

		while (true) {

			a.PrintMenu();
			if (a.userInput(s) == 1)
				a.processCycles(a.userInputCycles);
			a.displayAll();
		}
	}
}