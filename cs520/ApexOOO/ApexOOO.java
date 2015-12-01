import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.Stack;
import java.util.StringTokenizer;

public class ApexOOO {
	// CPU representations
	public int PSW_Z = -1;
	// arch registers
	public int ar_register[];
	// physical registers
	public int register[];
	// status array for the ROB physical register
	public boolean registerValid[];
	// renamed array -- switched off. no use
	// public boolean renamed[];
	// memory
	public int memory[];
	// PC counter - next fetch
	public int GlobalPC;
	// hold the instructions of the input file
	Instruction[] instructions;
	// load store queue. Size 4
	Queue<Instruction> loadStoreQueue;
	public int currentSizeLSQ;
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
	// BIS
	Stack<Instruction> BIS;
	// counter for branch tags
	public int globalBranchCounter;
	int topBIS = -1;
	// hold pipeline details in real time
	public Instruction inFetch = null, inDecode = null, toIntFu = null, toMulFU = null, inExIntFU = null,
			inExMulFU = null, inEXLSFU = null, inEXLSFU1 = null, inEXLSFU2 = null, inWB4Int = null, inWB4Mul = null,
			inWB4LS = null, toLSFU = null;
	// passing on. work as pipeline registers.
	public Instruction inFetchtoNext = null;
	// various flags to handle message passing
	public boolean stalled = false;
	// take input for no of cycles to simulate.
	public int userInputCycles;
	// used to stop incrementing PC once end of instructions is reached
	boolean flagEND = false;
	// hack for BAL and jump
	public boolean masterStall;
	// keeps track of Mul FU cycles - non-pipelined
	// also signals that MUL completed in the current cycle(and hence has to be
	// forwarding data
	public int multimer = 0;
	// tag is the register(physical) number being forwarded
	// value is the data being

	// forward path from EX stage
	public int forwardedFromIntEXtag = -1;
	public int forwardedFromIntEXValue = -1;
	public int forwardedFromMulEXtag = -1;
	public int forwardedFromMulEXValue = -1;
	public int forwardedfromLSFUtag = -1;
	public int forwardedFromLSFUValue = -1;

	// forward path from WB stage
	public int forwardedFromIntWBtag = -1;
	public int forwardedFromIntWBValue = -1;
	public int forwardedFromMulWBtag = -1;
	public int forwardedFromMulWBValue = -1;
	public int forwardedFromLSWBtag = -1;
	public int forwardedFromLSWBValue = -1;

	// read value
	public int forwardResult = -1;

	// constructor
	public ApexOOO() {
		GlobalPC = 20000;
		instructions = new Instruction[1000];
		ar_register = new int[9];
		register = new int[16];
		registerValid = new boolean[16];
		// renamed = new boolean[16];
		loadStoreQueue = new LinkedList<Instruction>();
		issuequeue = new Instruction[8];
		currentSizeIQ = 0;
		reorderBuffer = new LinkedList<Instruction>();
		freelist = new LinkedList<Integer>();
		RATdecision = new int[9];
		// memory
		memory = new int[10000];
		// _-Cache
		for (int i = 0; i < 1000; i++) {
			instructions[i] = new Instruction();
		}
		//
		BIS = new Stack<Instruction>();
		RAT = new HashMap<Integer, Integer>();
		for (int i = 0; i < 8; i++) {
			RAT.put(i, i);
			RATdecision[i] = 0; // initially all committed values. So,everything
			// points to ARF
		}
		RAT.put(8, 8);
		RATdecision[8] = 0;
		// initially all entries in RAT point to ARF as there
		// will be no physical stand in for any register and hence no register
		// will be valid(in the next for loop)
		for (int i = 0; i < 16; i++) {
			freelist.add(i);
		}

		for (int i = 0; i < 16; i++)
			registerValid[i] = false;

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
			// hack for stalling fetch and decode whenever there is a jump or
			// BAL instruction. stall till target address is generated in the
			// IntFU
			if (!masterStall) {
				doFetch();
				doDecode();
			} else {
				// hack to null decode stage after a JUMP/BAL instruction in
				// next cycle.(they will be moved to IQ).
				inDecode = null;
			}
			updateIssueQueue();
			doEX();
			// No memory stage now.
			// doMEM();
			doWB();
			retirement();
			// clock tick

		}
	}

	// fetch stage
	public void doFetch() {
		System.out.println("===Stalled from fetch:" + stalled);
		System.out.println("===flagendfrom fetch:" + flagEND);

		if (flagEND && stalled)
			return;

		if (stalled)
			return;

		inFetchtoNext = inFetch;
		if (flagEND)
			inFetch = null;

		// the test is- create a new object of instruction every time. this
		// solves the problem of rename and branch thing
		// inFetch = this.instructions[GlobalPC - 20000];
		inFetch = createInstruction(this.instructions[GlobalPC - 20000]);
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

	private Instruction createInstruction(Instruction instruction) {
		if (instruction == null || instruction.instr_id == null) {
			return null;
		}
		System.err.println("create instruction:" + instruction);
		Instruction newinst = new Instruction();
		newinst.rawString = new String(instruction.rawString);
		newinst.FUtype = instruction.FUtype;
		newinst.address = instruction.address;
		newinst.inst_name = instruction.inst_name;
		newinst.instr_id = instruction.instr_id;
		newinst.src1 = instruction.src1;
		newinst.src2 = instruction.src2;
		newinst.destination = instruction.destination;
		newinst.literal = instruction.literal;
		return newinst;
	}

	// decode stage followed by helper functions for it
	public void doDecode() {

		// these check SHOULD NOT BE PERFORMED FOR JUMP, BZ and may be a few
		// others
		// check if free slot in IQ and a free register is available,
		// other wise stall
		System.out.println("===Stalled from decode:" + stalled);

		stalled = checkDecodeStall(inDecode);
		if (!stalled) {
			inDecode = inFetchtoNext;
			if (inDecode != null) {
				stalled = checkDecodeStall(inDecode);
				if (!stalled) {
					renameReadPutintoIQnROB(inDecode);
					// masterStall true when these are are issued. Start fetch
					// and decode once target address is recomputed.
					// TODO verify the stall logic for JUMP and BAL
					if (inDecode.instr_id == InstructionType.JUMP || inDecode.instr_id == InstructionType.BAL) {
						inFetch = null;
						inFetchtoNext = null;
						masterStall = true;
					}
					// static branch prediction
					if (inDecode.instr_id == InstructionType.BZ || inDecode.instr_id == InstructionType.BNZ) {
						// positive offset means branch is not taken, let the
						// flow continue as is
						if (inDecode.literal > 0) {
							inDecode.prediction = false;
						}
						// negative means prediction is branch is taken.
						// update PC, flush fetch
						else {
							inDecode.prediction = true;
							GlobalPC = inDecode.address + inDecode.literal;
							inFetch = null;
							inFetchtoNext = null;
						}
					}
				}
			}
		} else {
			// TODO verify this method
			if (inDecode != null && inDecode.instr_id != null)
				forwardCheckDuringDecodeStall(inDecode);
			return;
		}
	}

	public void forwardCheckDuringDecodeStall(Instruction instruction) {
		if (instruction.src1 != -1 && checkForwardedPaths(instruction.src1)) {
			instruction.src1_data = forwardResult;
			instruction.src1valid = true;
		}
		if (instruction.src2 != -1 && checkForwardedPaths(instruction.src2)) {
			instruction.src2_data = forwardResult;
			instruction.src2valid = true;
		}
	}

	public boolean checkDecodeStall(Instruction instruction) {
		if (instruction != null) {
			if (currentSizeIQ == 8 || reorderBuffer.size() == 16) {
				System.err.println("no IQ or ROB, current IQ size:" + currentSizeIQ + " reorder buffer size: "
						+ reorderBuffer.size());
				// stalled = true;
				return true;

			} else if (instruction.destination != -1 && freelist.size() == 0) {
				System.err.println("no free register");
				// stalled = true;
				return true;
			}
		}
		return false;
	}

	// TODO change if any required for LSQ. Can't see anything right now

	// executes only if Decode not stalling. renames and attempts to read
	// sources. sets flags. renames destination and allocate a PR. update RAT.
	// allocate IQ & ROB
	public void renameReadPutintoIQnROB(Instruction instruction) {
		// Building the renamed instructions
		StringBuilder sb = new StringBuilder();
		sb.append(instruction.inst_name + " ");
		// rename and try to read sources
		// TODO experimental forward check while renaming. forwarding
		// also checked when an instruction stalls in decode via
		// forwardCheckDuringDecodeStall()
		System.err.println("in renameAndRead" + instruction);
		if (instruction.src1 != -1) {
			System.err.println("has AR src1  " + instruction.src1);
			instruction.renamedSrc1 = RAT.get(instruction.src1);
			System.err.println("renamed src1  is " + instruction.renamedSrc1);
			if (RATdecision[instruction.src1] == 0) {
				instruction.src1_data = ar_register[instruction.renamedSrc1];
				instruction.src1valid = true;
			} else {
				if (registerValid[instruction.renamedSrc1] == true) {
					System.err.println(" src1 PR  valid");
					instruction.src1_data = register[instruction.renamedSrc1];
					instruction.src1valid = true;
				}

				else if (checkForwardedPaths(instruction.renamedSrc1)) {
					System.err.println(" src1 PR from forwardeda");
					instruction.src1_data = forwardResult;
					instruction.src1valid = true;
				} else {
					System.err.println(" src1 PR Not valid");
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
				} else if (checkForwardedPaths(instruction.renamedSrc2)) {
					instruction.src2_data = forwardResult;
					instruction.src2valid = true;
				} else {
					instruction.src2valid = false;
				}
			}
		}
		// read data in decode when RAT points to
		// ARF for a source, so no register as such
		// rename destination and update RAT other flags
		if (instruction.destination != -1) {
			int x = freelist.remove();

			registerValid[x] = false;
			int currentStandin = RAT.get(instruction.destination);
			// if (RATdecision[instruction.destination] == 1)
			// putting info for rollback in case of branch taken
			instruction.previousStandIn = currentStandin;
			instruction.previousRATdeciderbit = RATdecision[instruction.destination] == 0 ? 0 : 1;
			RAT.put(instruction.destination, x);
			RATdecision[instruction.destination] = 1;
			instruction.renamedDestination = x;

			sb.append("P" + instruction.renamedDestination);
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
		// TODO test the instruction handled for X right now.
		// set renamed string
		// set Issuable condition if all the required sources have been read in
		instruction.isReadyForIssue = true;
		// set isIssuable if both the sources are ready.
		if (instruction.src1 != -1)
			instruction.isReadyForIssue = instruction.src1valid;
		if (instruction.src2 != -1)
			instruction.isReadyForIssue &= instruction.src2valid;

		/*
		 * TODO setting branch tags and updating counter if the instruction
		 * itself is a branch. Also, this is done only for a conditional branch
		 * as unconditional branch will make decode and fetch stall till target
		 * address is generated. BZ and BNZ have relative addressing
		 */
		if (instruction.instr_id == InstructionType.BZ || instruction.instr_id == InstructionType.BNZ) {
			insertintoBIS(instruction);
			++topBIS;
		}
		instruction.branchTag = topBIS;
		// assumption: instruction once decoded is put into the
		// issue queue entry and ROB entry by the end of the cycle in which it
		// got decoded
		// put into the empty slot in the IQ

		for (int i = 0; i < 8; i++) {
			if (issuequeue[i] == null && instruction.instr_id != null) {
				issuequeue[i] = instruction;
				break;
			}

		}
		++currentSizeIQ;
		if (instruction.instr_id != null)
			reorderBuffer.add(instruction);

	}

	// 1. check for forwarded data in each cycle
	// 2. increment age and set issuable condition
	// 3.issue instructions - not serialized. i.e. multiple instructions can be
	// -----broken down into 3 seperate methods
	// issued simultaneously

	// POINT 3 is abstracted into a method - this method called from EX
	// directly. reduces complexity

	// TODO verify method stub
	private void insertintoBIS(Instruction instruction) {
		BIS.push(instruction);
		System.err.println("branch instruction inserted into BIS");
		return;

	}

	public void updateIssueQueue() {
		System.err.println("updating issue queue ");
		Instruction instruction = null;
		toIntFu = null;
		toLSFU = null;
		toMulFU = null;
		// toMulFU = null;
		for (int i = 0; i < 8; i++) {
			instruction = issuequeue[i];
			if (instruction != null) {
				System.err.println("for instrutin" + instruction);
				if (instruction.src1 != -1 && !instruction.src1valid && checkForwardedPaths(instruction.renamedSrc1)) {
					instruction.src1_data = forwardResult;
					instruction.src1valid = true;
					System.err.println("Received forward data for" + instruction);
				}
				if (instruction.src2 != -1 && !instruction.src2valid && checkForwardedPaths(instruction.renamedSrc2)) {
					instruction.src2_data = forwardResult;
					instruction.src2valid = true;
					System.err.println("Received forward data for" + instruction);
				}
			}
		}
		// increment age. why?
		// 1.instruction spends at least 1 cycle in IQ
		// 2. Later on introduce age based issue
		instruction = null;
		for (int i = 0; i < 8; i++) {
			instruction = issuequeue[i];
			if (instruction != null) {
				++instruction.ageInIQ;
				// verify if the below logic is correct.
				instruction.isReadyForIssue = true;
				// set isIssuable if both the sources are ready.
				if (instruction.src1 != -1)
					instruction.isReadyForIssue &= instruction.src1valid;
				if (instruction.src2 != -1)
					instruction.isReadyForIssue &= instruction.src2valid;
			}
		}
	}

	// TODO age 2 makes an instruction stay in IQ for at least one cycle. to be
	// changed when forwarding logic is updated
	// probe IQ for a Int Issuable instruction
	public boolean checkIssueQueueForInt() {
		toIntFu = null;
		Instruction instruction;
		for (int i = 0; i < 8; i++) {
			instruction = issuequeue[i];
			if (instruction != null) {
				if (instruction.isReadyForIssue && instruction.FUtype == 0 && instruction.ageInIQ > 2) {
					toIntFu = issuequeue[i];
					System.err.println("sending out to intFU EX stage" + toIntFu);
					issuequeue[i] = null;
					currentSizeIQ--;
					return true;
				}
			}
		}
		return false;
	}

	// probe IQ for a Mul Issuable instruction
	public boolean checkIssueQueueForMul() {
		toMulFU = null;
		Instruction instruction;
		for (int i = 0; i < 8; i++) {
			instruction = issuequeue[i];
			if (instruction != null) {
				if (instruction.isReadyForIssue && instruction.FUtype == 1 && instruction.ageInIQ > 2) {
					toMulFU = issuequeue[i];
					System.err.println("sending out to MulFU EX stage" + toMulFU);
					issuequeue[i] = null;
					currentSizeIQ--;
					return true;
				}
			}
		}
		return false;
	}
	
	// LSQ code /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	// TODO redo because of LSQ
	// probe IQ for a LOAD STORE Issuable instruction
	public boolean checkIssueQueueForLoadStore() {
		toLSFU = null;
		Instruction instruction;
		for (int i = 0; i < 8; i++) {
			instruction = issuequeue[i];
			if (instruction != null) {
				if (instruction.isReadyForIssue && instruction.FUtype == 2 && instruction.ageInIQ > 2) {
					toLSFU = issuequeue[i];
					System.err.println("sending out to LSFU EX stage" + toLSFU);
					issuequeue[i] = null;
					currentSizeIQ--;
					return true;
				}
			}
		}
		return false;
	}
	
	// LSQ code /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void doEX() {

		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// LSFU
		// new revamped implementation for LSFU TODO update requried if any for
		// LSQ
		// this is pipelined hence can be checked for every cycle
		inWB4LS = inEXLSFU2;
		inEXLSFU2 = inEXLSFU1;
		inEXLSFU1 = inEXLSFU;
		if (checkIssueQueueForLoadStore()) {
			inEXLSFU = toLSFU;
			System.out.println("Starting load/store:" + inEXLSFU);
			LoadStoreFU(inEXLSFU);
		} else {
			inEXLSFU = null;
		}
		// TODO put into LSQ
		// forwarding logic starts
		if (inEXLSFU2 != null && inEXLSFU2.instr_id != null) {
			forwardedfromLSFUtag = inEXLSFU2.renamedDestination;
			forwardedFromLSFUValue = inEXLSFU2.destination_data;
		} else {
			forwardedfromLSFUtag = -1;
			forwardedFromLSFUValue = -1;
		}
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// MULFU
		// new revamped implementation for Mul FU ( NON PIPELINED 4 cycles)
		forwardedFromMulEXtag = -1;
		forwardedFromMulEXValue = -1;
		if (multimer > 0) {
			++multimer;
		}
		if (multimer == 5) {
			multimer = 0;
			// pass to WB Stage
			inWB4Mul = inExMulFU;
			forwardedFromMulEXtag = inExMulFU.renamedDestination;
			forwardedFromMulEXValue = inExMulFU.destination_data;
			inExMulFU = null;
		} else {
			inWB4Mul = null;
			forwardedFromMulEXtag = -1;
			forwardedFromMulEXValue = -1;
		}

		if (multimer == 0) {
			if (checkIssueQueueForMul()) {

				inExMulFU = toMulFU;
				MulFU(inExMulFU);
				multimer = 1;
			} else {
				inExMulFU = null;
			}
		}
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// INT FU
		forwardedFromIntEXtag = -1;
		forwardedFromIntEXValue = -1;
		inWB4Int = inExIntFU;
		if (checkIssueQueueForInt()) {
			inExIntFU = toIntFu;
			System.err.println(" EX in int FU " + inExIntFU);
			switch (inExIntFU.instr_id) {
			case MOVC:
			case MOV:
				Mov(inExIntFU);
				break;
			case ADD:
				AddFU(inExIntFU);
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
			case HALT:
				break;
			case BNZ:
			case BZ:
				// Relative addressing
				processConditionalBranches(inExIntFU);
				break;
			case BAL:
				// absolute addressing
				// TODO verify, no branch flush is needed. as processor fetch
				// stalls when BAL or JUMP instruction is encountered in Decode.
				// hence no squashing is needed at all
				// branchFlush(inExIntFU);
				// start execution again
				masterStall = false;
				// System.err.println("bal not implemented yet.");
				// System.exit(0);
				ar_register[8] = inExIntFU.address + 1;
				GlobalPC = inExIntFU.src1_data + inExIntFU.literal;
				break;
			case JUMP:
				// absolute addressing
				// branchFlush(inExIntFU);
				// start execution again
				masterStall = false;
				GlobalPC = inExIntFU.src1_data + inExIntFU.literal;
				break;
			default:
				System.err.println("?????????????????????In default case of INT FU. Never possibe!!!!!!!!!!!!!!! ");
				break;

			}
			// forwarding logic starts
			if (inExIntFU.destination != -1) {
				forwardedFromIntEXtag = inExIntFU.renamedDestination;
				forwardedFromIntEXValue = inExIntFU.destination_data;
			}
			// forwarding logic ends
		} else {
			inExIntFU = null;
		}
	}

	public void processConditionalBranches(Instruction instruction) {
		boolean taken = false;
		if (instruction.instr_id == InstructionType.BZ) {
			taken = (instruction.src1_data == 0) ? true : false;
		}
		if (instruction.instr_id == InstructionType.BNZ) {
			taken = (instruction.src1_data != 0) ? true : false;
		}
		if (taken == instruction.prediction) {
			// correct prediction, so continue as is.
			System.err.println("$$$$$$$$prediction was correct");
		} else {
			// incorrect prediction,
			System.err.println(
					"**********************************************************************************************************");
			System.err.println(
					"***************************************$Branch Misprediction**********************************************");
			System.err.println(
					"**********************************************************************************************************");
			flushProcessorAfterMisprediction(instruction);
		}
	}

	// TODO no use of this function. can be used with some modifications
	// TODO for squashing instruction upon branch resolutions
	public void flushProcessorAfterMisprediction(Instruction conditionalInstruction) {
		inFetch = null;
		inFetchtoNext = null;
		inDecode = null;
		Instruction instruction;
		// TODO (verify) flush issue queue for instructions with address equal
		// and
		// greater
		// than the
		// branch address
		while (conditionalInstruction.branchTag <= topBIS) {
			for (int i = 0; i < 8; i++) {
				if (issuequeue[i] != null && issuequeue[i].branchTag == topBIS) {

					System.err.println("!!!!!found: at tag" + topBIS + "ins:" + issuequeue[i] + "now rollingback");
					issuequeue[i] = null;
					--topBIS;
					break;
				}
			}
		}
		topBIS = conditionalInstruction.branchTag;
		System.err.println("Issue queue now is-");
		printIssueQueue();
		// TODO(verify) flush ROB for instructions with address greater than the
		// branch
		// address

		// TODO remove instructions from LSU,IntFU and MUL if they have
		// instructions which have branchTags >= the mispredicted branch.

		// TODO verify this logic, might introduce a bug
		// cannot do the following , because the Branch instruction itself is in
		// intFU
		// if (inExIntFU.branchTag >= conditionalInstruction.branchTag)
		// inExIntFU = null;

		// TODO verify, pop BIS till you find the mis predicted branch
		while (!BIS.isEmpty() && BIS.peek().branchTag > conditionalInstruction.branchTag) {
			System.err.println(BIS.pop());
		}
		reInItRATandAllocatedList();// copy committed ARF to RAT. now we will
		// update. walk
		// backwards(from head to tail)
		// alternative3 in the notes.

		Queue<Instruction> newReorderBuffer = new LinkedList<Instruction>();
		// last condition to stop
		// TODO (verify) walking back (towards) from head of the queue till the
		// branch mispredicted
		while (!reorderBuffer.isEmpty() && reorderBuffer.peek().branchTag < conditionalInstruction.branchTag) {
			instruction = reorderBuffer.remove();
			if (instruction.destination != -1) {
				RAT.put(instruction.destination, instruction.renamedDestination);
			}
			newReorderBuffer.add(instruction);

		}
		newReorderBuffer.add(conditionalInstruction);
		reorderBuffer = newReorderBuffer;
		System.err.println("reorderbuffer after flushing");
		System.err.println(reorderBuffer);
	}

	// TODO(verify)
	private void reInItRATandAllocatedList() {
		// TODO Auto-generated method stub
		HashMap<Integer, Integer> newRAT = new HashMap<Integer, Integer>();
		for (int i = 0; i < 8; i++) {
			newRAT.put(i, i);
		}
		freelist.clear();
		for (int i = 0; i < 16; i++) {
			freelist.add(i);
		}
		RAT = newRAT;
		for (int i = 0; i < 8; i++) {
			RATdecision[i] = 0;
		}
		printRenameTable();
	}

	// TODO not used
	public void rollbackInstruction(Instruction instruction) {
		if (instruction.destination != -1) {
			freelist.add(instruction.renamedDestination);
			RATdecision[instruction.destination] = instruction.previousRATdeciderbit;
			RAT.put(instruction.destination, instruction.previousStandIn);
		}
	}

	public void Mov(Instruction instruction) {
		if (instruction.instr_id == InstructionType.MOVC) {
			instruction.destination_data = instruction.literal;
		} else if (instruction.instr_id == InstructionType.MOV) {
			instruction.destination_data = register[instruction.renamedSrc1];
		}
		return;
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

	void LoadStoreFU(Instruction instruction) {
		if (instruction.instr_id == InstructionType.LOAD) {
			if (instruction.literal == -1) {

				instruction.destination_data = memory[instruction.src1_data + instruction.src2_data];
			} else {

				instruction.destination_data = memory[instruction.src1_data + instruction.literal];
			}
		}
		// TODO whatever a store will do, the operation to memory will take
		// place when STORE retires
		else if (instruction.instr_id == InstructionType.STORE) {
			if (instruction.literal == -1) {

				// memory[instruction.src1_data + instruction.src2_data] =
				// register[instruction.destination];

			} else {

				// memory[instruction.src1_data + instruction.literal] =
				// register[instruction.destination];
			}

		}
	}

	// WB stage
	public void doWB() {
		forwardedFromIntWBtag = -1;
		forwardedFromIntWBValue = -1;
		forwardedFromMulWBtag = -1;
		forwardedFromMulWBValue = -1;
		forwardedFromLSWBtag = -1;
		forwardedFromLSWBValue = -1;

		// LS instructions
		if (inWB4LS != null && inWB4LS.instr_id != null) {
			// forward result
			forwardedFromLSWBValue = inWB4LS.destination_data;
			// update WB done flag
			inWB4LS.writtenBack = true;
			if (inWB4LS.instr_id == InstructionType.LOAD) {
				registerValid[inWB4LS.renamedDestination] = true;
				register[inWB4LS.renamedDestination] = inWB4LS.destination_data;
			}
		}
		// Mul instructions
		if (inWB4Mul != null && inWB4Mul.instr_id != null) {
			// forward result
			forwardedFromMulWBtag = inWB4Mul.renamedDestination;
			forwardedFromMulWBValue = inWB4Mul.destination_data;
			// update WB done flag
			inWB4Mul.writtenBack = true;
			registerValid[inWB4Mul.renamedDestination] = true;
			register[inWB4Mul.renamedDestination] = inWB4Mul.destination_data;
		}
		// Int instructions
		if (inWB4Int != null && inWB4Int.instr_id != null) {
			// set forwarding values
			forwardedFromIntWBtag = inWB4Int.renamedDestination;
			forwardedFromIntWBValue = inWB4Int.destination_data;
			// set write back flag to true
			inWB4Int.writtenBack = true;

			switch (inWB4Int.instr_id) {
			case HALT:
				break;
			case MOVC:
			case MOV:
			case ADD:
			case SUB:
			case AND:
			case XOR:
			case OR:
				register[inWB4Int.renamedDestination] = inWB4Int.destination_data;

				registerValid[inWB4Int.renamedDestination] = true;
				break;
			case BNZ:
			case BZ:
			case BAL:
			case JUMP:
				break;
			default:
				System.err.println("?????????????????????In default case of INT WB. Never possibe!!!!!!!!!!!!!!! ");
				break;
			}
		}
		System.err.print(" WB - LS" + inWB4LS + "\n");
		System.err.print(" WB - MUL" + inWB4Mul + "\n");
		System.err.print(" WB - INT" + inWB4Int + "\n");

	}

	// retirement logic for ROB, takes place during WB
	public void retirement() {
		// confirm if an instruction has to spend minimum one instruction in ROB
		// or if it is possible then it can be directly committed to the ARF,
		// right now it spends minimum 1 cycle in ROB
		if (reorderBuffer.size() > 0 && reorderBuffer.peek().isReadyForCommit) {
			Instruction retiring = reorderBuffer.remove();
			// setting renamed string back to null as the instruction has
			// completed. renaming from scratch if the same instruction is
			// executed again in future
			retiring.renamedString = null;
			if (retiring.instr_id == InstructionType.HALT) {
				System.out.println("==============HALT instruction encountered. (head of ROB)==============");
				this.displayAll();
				System.exit(0);
			}
			// write to ARF and update RAT if destination is present
			if (retiring.destination != -1) {
				register[retiring.destination] = -1;
				ar_register[retiring.destination] = retiring.destination_data;
				freelist.add(retiring.renamedDestination);
				RAT.put(retiring.destination, retiring.destination);
				RATdecision[retiring.destination] = 0;
				registerValid[retiring.renamedDestination] = false;
			}
			// write to memory if it's a store instruction
			if (retiring.instr_id == InstructionType.STORE) {
				if (retiring.literal == -1) {
					memory[retiring.src1_data + retiring.src2_data] = retiring.src1_data;
				} else {
					memory[retiring.src1_data + retiring.literal] = retiring.src1_data;
				}
			}
			// if branch instruction then remove it from the BIS
			if (retiring.instr_id == InstructionType.BZ || retiring.instr_id == InstructionType.BNZ) {
				Instruction removed = BIS.remove(0);
				if (removed.address != retiring.address) {
					System.err.println("removed from bis but not the same isntruction");
					System.exit(0);
				} else {
					System.err.println("removed from bis and same isntruction");
				}
			}
		}

		for (Instruction i : reorderBuffer) {
			// TODO hack, could have directly set the isReadyforCommit but then
			// instruction would've directly committed to and not spent min 1
			// cycle in ROB
			i.isReadyForCommit = i.writtenBack;
		}
	}

	public boolean checkForwardedPaths(int registerToLookup) {
		if (registerToLookup == forwardedFromIntEXtag) {
			forwardResult = forwardedFromIntEXValue;
			return true;
		} else if (registerToLookup == forwardedFromMulEXtag) {
			forwardResult = forwardedFromMulEXValue;
			return true;
		} else if (registerToLookup == forwardedfromLSFUtag) {
			forwardResult = forwardedFromLSFUValue;
			return true;
		} else if (registerToLookup == forwardedFromIntWBtag) {
			forwardResult = forwardedFromIntWBValue;
			return true;
		} else if (registerToLookup == forwardedFromMulWBtag) {
			forwardResult = forwardedFromMulWBValue;
			return true;
		} else if (registerToLookup == forwardedFromLSWBtag) {
			forwardResult = forwardedFromLSWBValue;
			return true;
		} else {
			return false;
		}
	}

	// display function and helpers
	public void displayAll() {
		System.out.println(
				"-----------------------------------------------------------------------------------------------------------------------------");
		System.out.println("Program Counter:" + GlobalPC);
		System.out.println("PSW-Zero(only 0 is 0):" + PSW_Z);
		printForwardingPaths();
		printRenameTable();
		printRegisters();
		printFreeList();
		printIssueQueue();
		this.printStages();
		printMemory();
		printreorderbuffer();
		printBIS();
		System.out.println(
				"\n-----------------------------------------------------------------------------------------------------------------------------");
	}

	private void printBIS() {
		// TODO Auto-generated method stub
		System.out.println("\nBIS:");
		System.out.println(BIS);
	}

	private void printFreeList() {
		System.out.println("\nFreelist:");
		System.out.println(freelist);
	}

	private void printForwardingPaths() {
		// might make this to Standard Err, hence String builder;
		StringBuilder sb = new StringBuilder();
		sb.append("From\tData\tValue\n");
		sb.append("IntFU\tP" + forwardedFromIntEXtag + "\t" + forwardedFromIntEXValue + "\n");
		sb.append("MulFU\tP" + forwardedFromMulEXtag + "\t" + forwardedFromMulEXValue + "\n");
		sb.append("LS FU\tP" + forwardedfromLSFUtag + "\t" + forwardedFromLSFUValue + "\n");
		sb.append("WB Int\tP" + forwardedFromIntWBtag + "\t" + forwardedFromIntWBValue + "\n");
		sb.append("WB Mul\tP" + forwardedFromMulWBtag + "\t" + forwardedFromMulWBValue + "\n");
		sb.append("WB LS\tP" + forwardedFromLSWBtag + "\t" + forwardedFromLSWBValue + "\n");
		System.out.println(sb.toString());
	}

	public void printMemory() {
		System.out.println("\n\nMemory(0 to 99):");
		// for (int i = 0; i < 20; i++) {
		// System.out.print(i + "\t");
		// }
		// System.out.println();
		for (int i = 0; i < 100; i++) {
			System.out.print(memory[i] + "\t");
			if ((i + 1) % 20 == 0)
				System.out.println();
		}
	}

	public void printreorderbuffer() {
		System.out.println("\nReorder Buffer:");
		// System.out.println(reorderBuffer);
		// printing ready for commit bool val
		System.out.print("[");
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
		System.out.print("\tX:" + ar_register[8]);
		System.out.format("\n\n%15s\t", "Phy Register|");
		System.out.print("P0:" + register[0]);
		System.out.print("\tP1:" + register[1]);
		System.out.print("\tP2:" + register[2]);
		System.out.print("\tP3:" + register[3]);
		System.out.print("\tP4:" + register[4]);
		System.out.print("\tP5:" + register[5]);
		System.out.print("\tP6:" + register[6]);
		System.out.print("\tP7:" + register[7]);
		System.out.print("\tP8:" + register[8]);
		System.out.print("\tP9:" + register[9]);
		System.out.print("\tP10:" + register[10]);
		System.out.print("\tP11:" + register[11]);
		System.out.print("\tP12:" + register[12]);
		System.out.print("\tP13:" + register[13]);
		System.out.print("\tP14:" + register[14]);
		System.out.print("\tP15:" + register[15]);
		System.out.format("\n%15s\t", "Allocated bit|");
		for (int i = 0; i < 16; i++) {
			System.out.print(!freelist.contains(i) + "\t");
		}
		System.out.format("\n%15s\t", "Status bit|");
		for (int i = 0; i < 16; i++) {
			System.out.print(registerValid[i] + "\t");
		}
		// System.out.format("\n%15s\t", "Renamed bit|");
		// for (int i = 0; i < 16; i++) {
		// System.out.print(renamed[i] + "\t");
		// }

	}

	// Various print functions
	public void printIssueQueue() {
		System.out.println("\nIssue Queue:");
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
		System.out.print(String.format("%10s", "In LS FU\n"));
		System.out.print(String.format("%10s", "In LS FU\t"));
		System.out.print(inEXLSFU + "(Stage1)\n");
		System.out.print(String.format("%10s", "In LS FU\t"));
		System.out.print(inEXLSFU1 + "(Stage2)\n");
		System.out.print(String.format("%10s", "In LS FU\t"));
		System.out.print(inEXLSFU2 + "(Stage3)");
		System.out.println();
	}

	public void printRenameTable() {
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
		System.out.print(String.format("%10s", "In WB\t"));
		System.out.print(inWB4Int + "\t" + inWB4Mul + "\t" + inWB4LS);
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
		System.out.println("Options:");
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
	public void compileInstruction(Instruction instruction) throws Exception {
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
		if (instruction.instr_id == InstructionType.LOAD || instruction.instr_id == InstructionType.STORE)
			instruction.isLoadStore = true;
		else {
			instruction.isLoadStore = false;
		}
		if (instruction.instr_id == InstructionType.HALT)
			return;

		if (instruction.instr_id == InstructionType.MUL)
			instruction.FUtype = 1;
		if (instruction.instr_id == InstructionType.LOAD || instruction.instr_id == InstructionType.STORE)
			instruction.FUtype = 2;
		if (tokenizer.hasMoreTokens()) {
			if (instruction.instr_id == InstructionType.JUMP) {
				d = tokenizer.nextToken();
				if (d.equals("X")) {
					// speical mapping to AR8.
					instruction.src1 = 8;
				} else if (d.charAt(0) != 'R') {
					instruction.literal = Integer.parseInt(d);
					System.out.println("JUMP/BZ/BNZ" + d);
					return;
				} else {
					instruction.src1 = Integer.parseInt(d.substring(1));
				}
			} else {

				d = tokenizer.nextToken();
				if (d.charAt(0) != 'R') {
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
		// hack for BZ/BNZ
		// save last destination register. make BZ/BNZ source dependent on that
		// register.we branch using the value of that register. Implicit
		// sourcing
		int lastDest = -1;
		String line;
		int i = 0;
		try {
			br = new BufferedReader(new FileReader("asm.txt"));
			while ((line = br.readLine()) != null) {
				System.out.println(i + " " + line);
				this.instructions[i].rawString = line;
				this.instructions[i].contains = true;
				this.instructions[i].address = i + 20000;
				this.compileInstruction(this.instructions[i]);
				// hack for setting up dependency for BZ/BNZ
				if (instructions[i].instr_id == InstructionType.BZ || instructions[i].instr_id == InstructionType.BNZ) {
					instructions[i].src1 = lastDest;
				}
				lastDest = instructions[i].destination;

				// following hack.
				// hack for store instruction
				if (instructions[i].instr_id == InstructionType.STORE) {
					instructions[i].src2 = instructions[i].src1;
					instructions[i].src1 = instructions[i].destination;
					instructions[i].destination = -1;
				}
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
			a.processCycles(a.userInputCycles);
		while (true) {
			a.PrintMenu();
			if (a.userInput(s) == 1)
				a.processCycles(a.userInputCycles);
			a.displayAll();
		}
	}
}