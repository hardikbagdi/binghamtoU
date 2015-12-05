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
	// minAgeinIQ- instructions spend atleaast one cycle in IQ
	private static final int minAgeInIQ = 2;
	// CPU representations
	private int PSW_Z = -1;
	// arch registers
	private int ar_register[];
	// physical registers
	private int register[];
	// status array for the ROB physical register
	private boolean registerValid[];
	// memory
	private int memory[];
	// PC counter - next fetch
	private int GlobalPC;
	// hold the instructions of the input file
	Instruction[] instructions;
	// load store queue. Size 4
	Instruction[] loadStoreQueue;
	private int currentSizeLSQ;
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
	int topBIS = -1;
	// hold pipeline details
	private Instruction inFetch = null, inDecode = null, toIntFu = null, toMulFU = null, inExIntFU = null,
			inExMulFU = null, inEXLSFU = null, inEXLSFU1 = null, inEXLSFU2 = null, inWB4Int = null, inWB4Mul = null,
			inWB4LS = null, toLSFU = null;
	// passing on. work as pipeline registers.
	private Instruction inFetchtoNext = null;
	// various flags to handle message passing
	private boolean stalled = false;
	// hack for BAL and jump
	private boolean masterStall;
	// take input for no of cycles to simulate.
	private int userInputCycles;
	// used to stop incrementing PC once end of instructions is reached
	boolean flagEND = false;
	// keeps track of Mul FU cycles - non-pipelined
	// also signals that MUL completed in the current cycle(and hence has to be
	// forwarding data
	private int multimer = 0;
	// tag is the register(physical) number being forwarded
	// value is the data being

	// forward path from EX stage
	private int forwardedFromIntEXtag = -1;
	private int forwardedFromIntEXValue = -1;
	private int forwardedFromMulEXtag = -1;
	private int forwardedFromMulEXValue = -1;
	private int forwardedfromLSFUtag = -1;
	private int forwardedFromLSFUValue = -1;
	private int forwardedFromLSQtag = -1;
	private int forwardedFromLSQValue = -1;

	// read value
	private int forwardResult = -1;

	// constructor
	public ApexOOO() {
		GlobalPC = 20000;
		instructions = new Instruction[1000];
		ar_register = new int[9];
		register = new int[16];
		registerValid = new boolean[16];
		// renamed = new boolean[16];
		loadStoreQueue = new Instruction[3];
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
		currentSizeLSQ = 0;
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

			// experimental
			removeLoadStoreFromIssueQueue();
			removeLoadOnly();
			// clock tick

		}
	}

	// fetch stage
	private void doFetch() {
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
		newinst.isLoadStore = instruction.isLoadStore;
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
	private void doDecode() {

		// these check SHOULD NOT BE PERFORMED FOR JUMP, BZ and may be a few
		// others
		// check if free slot in IQ and a free register is available,
		// other wise stall
		System.out.println("===Stalled from decode:" + stalled);
		//
		if (stalled)
			stalled = checkDecodeStall(inDecode);
		// above if checks only if last was stalled
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

	private void forwardCheckDuringDecodeStall(Instruction instruction) {
		if (instruction.src1 != -1 && checkForwardedPaths(instruction.renamedSrc1)) {
			instruction.src1_data = forwardResult;
			instruction.src1valid = true;
		}
		if (instruction.src2 != -1 && checkForwardedPaths(instruction.renamedSrc2)) {
			instruction.src2_data = forwardResult;
			instruction.src2valid = true;
		}
	}

	private boolean checkDecodeStall(Instruction instruction) {
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

			// Special check for Load Store instruction, need a free LSQ entry
			if (instruction.isLoadStore) {
				if (currentSizeLSQ == 3) {
					System.err.println("-->LSQ full and hence might stall if can't free up LSQ stalling");
					return tryFreeingUpLSQ();
				}
			}
		}
		return false;
	}

	// executes only if Decode not stalling. renames and attempts to read
	// sources. sets flags. renames destination and allocate a PR. update RAT.
	// allocate IQ & ROB (and LSQ for a LS instruction).

	private void renameReadPutintoIQnROB(Instruction instruction) {
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
				sb.append(" (" + instruction.src1_data + ")");
			}
		}
		if (instruction.src2 != -1) {
			if (RATdecision[instruction.src2] != 0)
				sb.append(" P" + instruction.renamedSrc2);
			else {
				sb.append(" (" + instruction.src2_data + ")");
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
		 * setting branch tags and updating counter if the instruction itself is
		 * a branch. Also, this is done only for a conditional branch as
		 * unconditional branch will make decode and fetch stall till target
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
				currentSizeIQ++;
				break;
			}

		}

		/*
		 * Put Load/Store instructions into LSQ too
		 */
		// TOOO verify this method call
		if (instruction.instr_id == InstructionType.LOAD || instruction.instr_id == InstructionType.STORE) {
			putIntoLSQ(instruction);
		}
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

	private void insertintoBIS(Instruction instruction) {
		BIS.push(instruction);
		System.err.println("branch instruction inserted into BIS");
		return;

	}

	private void updateIssueQueue() {
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
		// keep track of age
		for (int i = 0; i < 3; i++) {
			if (loadStoreQueue[i] != null)
				++loadStoreQueue[i].ageInIQ;
		}
	}

	// changed when forwarding logic is updated
	// probe IQ for a Int Issuable instruction
	private boolean checkIssueQueueForInt() {
		toIntFu = null;
		Instruction instruction;
		for (int i = 0; i < 8; i++) {
			instruction = issuequeue[i];
			if (instruction != null) {
				if (instruction.isReadyForIssue && instruction.FUtype == 0 && instruction.ageInIQ > minAgeInIQ) {
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
	private boolean checkIssueQueueForMul() {
		toMulFU = null;
		Instruction instruction;
		for (int i = 0; i < 8; i++) {
			instruction = issuequeue[i];
			if (instruction != null) {
				if (instruction.isReadyForIssue && instruction.FUtype == 1 && instruction.ageInIQ > minAgeInIQ) {
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

	// LSQ code
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// try freeing up LSQ if LSQ full

	// LSQ - 0 is head 3 is tail
	// TODO bug testing
	private boolean tryFreeingUpLSQ() {
		System.err.println(("trying to free up lsq"));
		System.err.println("before freeing up");
		printLSQ();
		if (currentSizeLSQ > 3 || currentSizeLSQ < 0) {
			System.err.println("un-possible. inconsistent state LSQ size" + currentSizeLSQ);
			System.exit(1);
		}
		if (loadStoreQueue[0] != null && loadStoreQueue[0].instr_id == InstructionType.LOAD) {
			if (loadStoreQueue[0].isLSissued) {
				loadStoreQueue[0] = null;
				--currentSizeLSQ;
				shiftLSQ();
				return false;

			}
		} else if (loadStoreQueue[0] != null && loadStoreQueue[0].instr_id == InstructionType.STORE) {
			if (loadStoreQueue[0].isReadyForCommit) {
				loadStoreQueue[0] = null;
				--currentSizeLSQ;
				shiftLSQ();
				return false;
			}
		}
		return true;
	}

	// TODO experimental
	private void removeLoadOnly() {
		System.err.println("before purging loads");
		printLSQ();
		while (loadStoreQueue[0] != null && loadStoreQueue[0].instr_id == InstructionType.LOAD
				&& loadStoreQueue[0].isLSissued) {
			shiftLSQ();
			--currentSizeLSQ;
		}
		while (loadStoreQueue[1] != null && loadStoreQueue[1].instr_id == InstructionType.LOAD
				&& loadStoreQueue[1].isLSissued) {
			loadStoreQueue[1] = loadStoreQueue[2];
			loadStoreQueue[2] = null;
			--currentSizeLSQ;
		}
		if (loadStoreQueue[2] != null && loadStoreQueue[2].instr_id == InstructionType.LOAD
				&& loadStoreQueue[2].isLSissued) {
			loadStoreQueue[2] = null;
			--currentSizeLSQ;
		}
		System.err.println("After purging loads");
		printLSQ();

	}

	private void putIntoLSQ(Instruction instruction) {
		// TODO Auto-generated method stub
		System.err.println("putting in LSQ" + instruction);
		for (int i = 0; i < 3; i++) {
			if (loadStoreQueue[i] == null) {
				loadStoreQueue[i] = instruction;
				++currentSizeLSQ;
				printLSQ();
				return;
			}
		}
		System.err.println("un-possible, lsq size:" + currentSizeLSQ);
		System.exit(1);
	}

	private void removeLoadStoreFromIssueQueue() {
		// System.err.println("hello from remove LS from IQ");
		Instruction instruction = null;
		for (int i = 0; i < 8; i++) {
			// System.err.println("t");
			instruction = issuequeue[i];
			if (instruction != null && instruction.isLoadStore && instruction.isReadyForIssue) {
				System.err.println("removing LS from issue queue: " + issuequeue[i]);
				issuequeue[i] = null;
				--currentSizeIQ;
			}
		}
	}

	// TODO verify, function which performs forwarding, (detection logic in
	// checkLoadStoreQueueForLoadStore())
	private void doforwarding(Instruction from, Instruction to) {
		System.err.println("forwarding data in LSQ");
		to.destination_data = from.src1_data;
		to.isLSissued = true;
		to.writtenBack = true;
		register[to.renamedDestination] = to.destination_data;
		forwardedFromLSQtag = to.renamedDestination;
		forwardedFromLSQValue = to.destination_data;
	}

	// TODO reno because of LSQ
	// probe IQ for a LOAD STORE Issuable instruction

	// delaying store instructions. delay are not executing out of order but
	// we're trying to do LOADS as early as possible
	private boolean checkLoadStoreQueueForIssue() {
		// various flags
		forwardedFromLSQtag = -1;
		forwardedFromLSQValue = -1;
		boolean yesFrom0to2 = false, yesFrom1to2 = false;
		toLSFU = null;

		// for the first(0) instruction in the queue, be it load or a store
		if (loadStoreQueue[0] != null && !loadStoreQueue[0].isLSissued && loadStoreQueue[0].isReadyForIssue
				&& loadStoreQueue[0].ageInIQ > minAgeInIQ) {
			loadStoreQueue[0].isLSissued = true;
			System.err.println("0th entry being sent out from LSQ" + loadStoreQueue[0] + "\t\tage:"
					+ loadStoreQueue[0].ageInIQ + "\t issued:" + loadStoreQueue[0].isLSissued);
			toLSFU = loadStoreQueue[0];
			return true;
		}

		// for second(1) instruction basic check
		if (loadStoreQueue[0] != null && loadStoreQueue[1] != null && !loadStoreQueue[1].isLSissued
				&& loadStoreQueue[1].isReadyForIssue && loadStoreQueue[1].ageInIQ > minAgeInIQ) {
			// if load
			if (loadStoreQueue[1].instr_id == InstructionType.LOAD) {
				if (loadStoreQueue[0].instr_id == InstructionType.STORE) {
					// if the first is a store, then check if the address is
					// resolved, if not resolved then you can't execute this
					// instruction, if resolved, check if the same address, if
					// same then forward, if not same then execute OOO

					// just check the src2 bit of 0 to check address equality
					if (loadStoreQueue[0].src2valid) {
						// check if the address is same. if not then execute OOO
						if ((loadStoreQueue[1].src2_data + loadStoreQueue[1].literal) != (loadStoreQueue[0].src2_data
								+ loadStoreQueue[0].literal)) {
							toLSFU = loadStoreQueue[1];
							loadStoreQueue[1].isLSissued = true;
							return true;
						} else {
							// we need to forward if the previous store has the
							// src1 valid
							if (loadStoreQueue[0].src1valid) {
								// write logic to forward
								doforwarding(loadStoreQueue[0], loadStoreQueue[1]);
							} else {
								// do nothing, we wait till the address is
								// resolved
							}
						}
					}
					// previous store instruction not resolved
					else {
						// cant do anything
					}
				}
				// if not preceding is not a store then you can execute the
				// instruction
				else {
					toLSFU = loadStoreQueue[1];
					loadStoreQueue[1].isLSissued = true;
					return true;

				}
			}
			// if an instruction is a STORE
			else {
				if (loadStoreQueue[0].instr_id == InstructionType.STORE) {
					if (loadStoreQueue[1].isReadyForIssue) {
						System.err.println("starting store on 1");
						toLSFU = loadStoreQueue[1];
						loadStoreQueue[1].isLSissued = true;
						return true;
					}

				}
			}
		}
		// for third(2) instruction basic check
		if (loadStoreQueue[0] != null && loadStoreQueue[1] != null && loadStoreQueue[2] != null
				&& !loadStoreQueue[2].isLSissued && loadStoreQueue[2].isReadyForIssue
				&& loadStoreQueue[2].ageInIQ > minAgeInIQ) {
			// if third instruction is a load
			if (loadStoreQueue[2].instr_id == InstructionType.LOAD) {
				System.err.println("checking if 3rd can be issued(LOAD)");
				if (loadStoreQueue[0].instr_id == InstructionType.LOAD
						&& loadStoreQueue[1].instr_id == InstructionType.LOAD) {
					// if previous are not store then you can execute

					toLSFU = loadStoreQueue[2];
					loadStoreQueue[2].isLSissued = true;
					return true;

				} else if (loadStoreQueue[0].instr_id == InstructionType.STORE) {
					if (loadStoreQueue[0].src2valid) {
						if ((loadStoreQueue[0].src2_data + loadStoreQueue[0].literal) != (loadStoreQueue[2].src1_data
								+ loadStoreQueue[2].literal)) {
							// can issue the instruction
							System.err.println("0 says yes to 2");
							yesFrom0to2 = true;
						}
						// address match and hence forward
						else {
							System.err.println("address match");
							// we need to forward if the previous store has the
							// src1 valid
							if (loadStoreQueue[0].src1valid) {
								// write logic to forward
								doforwarding(loadStoreQueue[0], loadStoreQueue[2]);

							} else {
								// do nothing, we wait till the data is
								// resolved
							}
						}
					} else {
						// can't do anything if a previous store address is not
						// known
					}

				}

				if (loadStoreQueue[1].instr_id == InstructionType.STORE) {
					System.err.println("checking for 2 , 1 is a STORE");
					if (loadStoreQueue[1].src2valid) {
						if ((loadStoreQueue[1].src2_data + loadStoreQueue[1].literal) != (loadStoreQueue[2].src1_data
								+ loadStoreQueue[2].literal)) {
							// can issue the instruction
							yesFrom1to2 = true;
							System.err.println("1 says yes to 2");
						}
						// address match and hence forward
						else {
							System.err.println("address match");
							// we need to forward if the previous store has the
							// src1 valid
							if (loadStoreQueue[1].src1valid) {
								// write logic to forward
								doforwarding(loadStoreQueue[1], loadStoreQueue[2]);

							} else {
								// do nothing, we wait till the data is
								// resolved
							}
						}
					} else {
						System.err.println("src2 of store of 1 not valid");
						// can't do anything if a previous store address is not
						// known
					}

				}
				if (loadStoreQueue[0].instr_id == InstructionType.STORE
						&& loadStoreQueue[1].instr_id == InstructionType.STORE) {
					if (yesFrom0to2 && yesFrom1to2) {
						toLSFU = loadStoreQueue[2];
						loadStoreQueue[2].isLSissued = true;
						return true;
					}
				}
			}

			// if third instruction is a store then-
			else {

			}

		}

		// if nothing can be found, then the bubble is LSFU
		toLSFU = null;
		return false;

	}

	private void shiftLSQ() {
		// TODO Auto-generated method stub
		loadStoreQueue[0] = loadStoreQueue[1];
		loadStoreQueue[1] = loadStoreQueue[2];
		loadStoreQueue[2] = null;

	}
	// LSQ code
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void doEX() {

		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// LSFU
		// new revamped implementation for LSFU TODO update requried if any for
		// LSQ
		// this is pipelined hence can be checked for every cycle
		inWB4LS = inEXLSFU2;
		inEXLSFU2 = inEXLSFU1;
		inEXLSFU1 = inEXLSFU;
		if (checkLoadStoreQueueForIssue()) {
			inEXLSFU = toLSFU;
			toLSFU.isLSissued = true;
			System.out.println("------>Starting load/store:" + inEXLSFU + "\t isLSissued:" + inEXLSFU.isLSissued);
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
		// TODO experimental, forwarding a cycle before

		if (multimer == 5) {
			multimer = 0;
			// pass to WB Stage
			inWB4Mul = inExMulFU;
			inExMulFU = null;
		} else {
			inWB4Mul = null;
			forwardedFromMulEXtag = -1;
			forwardedFromMulEXValue = -1;
		}
		if (multimer == 4) {
			forwardedFromMulEXtag = inExMulFU.renamedDestination;
			forwardedFromMulEXValue = inExMulFU.destination_data;
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

	private void processConditionalBranches(Instruction instruction) {
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
			// TODO verify PC update logic in case of branch mis predictions
			if (instruction.instr_id == InstructionType.BZ) {

				if (instruction.src1_data == 0) {
					GlobalPC = instruction.address + instruction.literal;
				} else {
					GlobalPC = instruction.address + 1;
				}
			}

			if (instruction.instr_id == InstructionType.BNZ) {

				if (instruction.src1_data != 0) {
					GlobalPC = instruction.address + instruction.literal;
				} else {
					GlobalPC = instruction.address + 1;
				}
			}

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

	// TODO for squashing instruction upon branch resolutions
	private void flushProcessorAfterMisprediction(Instruction conditionalInstruction) {
		inFetch = null;
		inFetchtoNext = null;
		inDecode = null;
		Instruction instruction;

		// TODO verify, flushing based on branchTag from IQ and LSQ
		while (conditionalInstruction.branchTag <= topBIS) {
			for (int i = 0; i < 8; i++) {
				// TODO code to flush LSQ also
				if (i < 3) {
					if (loadStoreQueue[i] != null && loadStoreQueue[i].branchTag == topBIS) {
						System.err.println(
								"!!!!!in LSQ, found: at tag" + topBIS + "ins:" + loadStoreQueue[i] + "now rollingback");
						loadStoreQueue[i] = null;
						currentSizeLSQ--;
					}
				}

				// code for flushing issue queue
				if (issuequeue[i] != null && issuequeue[i].branchTag == topBIS) {

					System.err.println("!!!!!found: at tag" + topBIS + "ins:" + issuequeue[i] + "now rollingback");
					issuequeue[i] = null;
					currentSizeIQ--;
				}
			}
			--topBIS;
		}
		topBIS = conditionalInstruction.branchTag;
		System.err.println("Issue queue now is-");
		printIssueQueue();
		// TODO(verify) flush ROB for instructions with branchtag greater than
		// the
		// branch
		// address

		// verify this todo
		// TODO remove instructions from LSU, and MUL if they have
		// instructions which have branchTags >= the mis-predicted branch.
		if (inExMulFU != null) {
			if (inExMulFU.branchTag >= topBIS)
				inExMulFU = null;
		}
		if (inEXLSFU != null) {
			if (inEXLSFU.branchTag >= topBIS)
				inEXLSFU = null;
		}
		if (inEXLSFU1 != null) {
			if (inEXLSFU1.branchTag >= topBIS)
				inEXLSFU1 = null;
		}
		if (inEXLSFU2 != null) {
			if (inEXLSFU2.branchTag >= topBIS)
				inEXLSFU2 = null;
		}
		// TODO verify, pop BIS till you find the mis-predicted branch
		while (!BIS.isEmpty() && BIS.peek().branchTag > conditionalInstruction.branchTag) {
			System.err.println(BIS.pop());
		}
		// copy committed ARF to RAT. now we will
		reInItRATandAllocatedList();
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
				// TODO bug testing
				RATdecision[instruction.destination] = 1;
				freelist.remove(instruction.renamedDestination);
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

	private void Mov(Instruction instruction) {
		if (instruction.instr_id == InstructionType.MOVC) {
			instruction.destination_data = instruction.literal;
		} else if (instruction.instr_id == InstructionType.MOV) {
			instruction.destination_data = instruction.src1_data;
		}
		return;
	}

	private void AddFU(Instruction instruction) {
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
	private void MulFU(Instruction instruction) {

		System.err.println("in Mul fu");
		int operand2 = 0;
		operand2 = instruction.src2 != -1 ? instruction.src2_data : instruction.literal;
		instruction.destination_data = instruction.src1_data * operand2;
	}

	private void SubFU(Instruction instruction) {
		System.err.println("in sub fu");
		int operand2 = 0;
		operand2 = instruction.src2 != -1 ? instruction.src2_data : instruction.literal;
		instruction.destination_data = instruction.src1_data - operand2;
		if (instruction.destination_data == 0)
			PSW_Z = 0;
		else
			PSW_Z = -1;
	}

	private void AndFU(Instruction instruction) {
		System.out.println("in OR fu");
		int operand2 = 0;
		operand2 = instruction.src2 != -1 ? instruction.src2_data : instruction.literal;
		instruction.destination_data = instruction.src1_data & operand2;
		if (instruction.destination_data == 0)
			PSW_Z = 0;
		else
			PSW_Z = -1;
	}

	private void OrFU(Instruction instruction) {
		System.out.println("in OR fu");
		int operand2 = 0;
		operand2 = instruction.src2 != -1 ? instruction.src2_data : instruction.literal;
		instruction.destination_data = instruction.src1_data | operand2;
		if (instruction.destination_data == 0)
			PSW_Z = 0;
		else
			PSW_Z = -1;
	}

	private void XorFU(Instruction instruction) {
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
	private void doWB() {
		// TODO verify
		// forwardedFromIntWBtag = -1;
		// forwardedFromIntWBValue = -1;
		// forwardedFromMulWBtag = -1;
		// forwardedFromMulWBValue = -1;
		// forwardedFromLSWBtag = -1;
		// forwardedFromLSWBValue = -1;

		// LS instructions
		if (inWB4LS != null && inWB4LS.instr_id != null) {
			// // forward result
			// forwardedFromLSWBtag = inWB4LS.renamedDestination;
			// forwardedFromLSWBValue = inWB4LS.destination_data;
			// // update WB done flag
			inWB4LS.writtenBack = true;
			if (inWB4LS.instr_id == InstructionType.LOAD) {
				registerValid[inWB4LS.renamedDestination] = true;
				register[inWB4LS.renamedDestination] = inWB4LS.destination_data;
			}
		}
		// Mul instructions
		if (inWB4Mul != null && inWB4Mul.instr_id != null) {
			// // forward result
			// forwardedFromMulWBtag = inWB4Mul.renamedDestination;
			// forwardedFromMulWBValue = inWB4Mul.destination_data;
			// // update WB done flag
			inWB4Mul.writtenBack = true;
			registerValid[inWB4Mul.renamedDestination] = true;
			register[inWB4Mul.renamedDestination] = inWB4Mul.destination_data;
		}
		// Int instructions
		if (inWB4Int != null && inWB4Int.instr_id != null) {
			// // set forwarding values
			// forwardedFromIntWBtag = inWB4Int.renamedDestination;
			// forwardedFromIntWBValue = inWB4Int.destination_data;
			// // set write back flag to true
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
		// System.err.print(" WB - LS" + inWB4LS + "\n");
		// System.err.print(" WB - MUL" + inWB4Mul + "\n");
		// System.err.print(" WB - INT" + inWB4Int + "\n");

	}

	// retirement logic for ROB, takes place during WB
	private void retirement() {
		// confirm if an instruction has to spend minimum one instruction in ROB
		// or if it is possible then it can be directly committed to the ARF,
		// right now it spends minimum 1 cycle in ROB
		while (reorderBuffer.size() > 0 && reorderBuffer.peek().isReadyForCommit) {
			Instruction retiring = reorderBuffer.remove();
			// setting renamed string back to null as the instruction has
			// completed. renaming from scratch if the same instruction is
			// executed again in future
			if (retiring.instr_id == InstructionType.HALT) {
				System.out.println("==============HALT instruction encountered. (head of ROB)==============");
				this.displayAll();
				System.exit(0);
			}
			// write to ARF and update RAT if destination is present
			if (retiring.destination != -1) {
				System.err.println("Commiting PR:" + retiring.renamedDestination + " to ARF:" + retiring.destination);
				register[retiring.destination] = -1;
				ar_register[retiring.destination] = retiring.destination_data;
				freelist.add(retiring.renamedDestination);
				RAT.put(retiring.destination, retiring.destination);
				RATdecision[retiring.destination] = 0;
				registerValid[retiring.renamedDestination] = false;
			}
			// write to memory if it's a store instruction
			if (retiring.instr_id == InstructionType.STORE) {
				memory[retiring.src2_data + retiring.literal] = retiring.src1_data;
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

	private boolean checkForwardedPaths(int registerToLookup) {
		if (registerToLookup == forwardedFromIntEXtag) {
			forwardResult = forwardedFromIntEXValue;
			return true;
		} else if (registerToLookup == forwardedFromMulEXtag) {
			forwardResult = forwardedFromMulEXValue;
			return true;
		} else if (registerToLookup == forwardedfromLSFUtag) {
			forwardResult = forwardedFromLSFUValue;
			return true;
		} else if (registerToLookup == forwardedFromLSQtag) {
			forwardResult = forwardedFromLSQValue;
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
		printLSQ();
		this.printStages();
		printMemory();
		printreorderbuffer();
		printBIS();
		System.out.println(
				"-----------------------------------------------------------------------------------------------------------------------------");
	}

	private void printLSQ() {
		// TODO Auto-generated method stub
		System.out.println("Load Store Queue:(Size:" + currentSizeLSQ + ")");
		boolean addResolved = false;
		for (int i = 0; i < 3; i++) {
			if (loadStoreQueue[i] != null) {
				addResolved = loadStoreQueue[i].instr_id == InstructionType.LOAD ? loadStoreQueue[i].src1valid
						: loadStoreQueue[i].src2valid;
				System.out.println(loadStoreQueue[i] + "\tAddress Resolved:" + addResolved + "\t,IsIssuable:"
						+ loadStoreQueue[i].isReadyForIssue + ", issued:" + loadStoreQueue[i].isLSissued
						+ "\t isReadyForCommited:" + loadStoreQueue[i].isReadyForCommit);
			} else {
				System.out.println("null");
			}
		}
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
		sb.append("LSQ forward\tP" + forwardedFromLSQtag + "\t" + forwardedFromLSQValue + "\n");
		System.out.println(sb.toString());
	}

	private void printMemory() {
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

	private void printreorderbuffer() {
		System.out.println("\nReorder Buffer:");
		// System.out.println(reorderBuffer);
		// printing ready for commit bool val
		System.out.print("[");
		for (Instruction i : reorderBuffer) {
			System.out.print(i + ":" + i.isReadyForCommit + ",");
		}
		System.out.print("]");

	}

	private void printRegisters() {
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
	private void printIssueQueue() {
		System.out.println("Issue Queue:(Size:" + currentSizeIQ + ")");
		for (int i = 0; i < 8; i++) {

			System.out.print(issuequeue[i]);
			if (issuequeue[i] != null) {
				System.out.print(
						"\tIssuable:" + issuequeue[i].isReadyForIssue + "\tAge in Queue:" + issuequeue[i].ageInIQ);
			}
			System.out.print("\n");
		}

	}

	private void printFUs() {
		System.out.print(String.format("%10s", "In Int FU\t"));
		System.out.println(inExIntFU);
		System.out.print(String.format("%10s", "In Mul FU\t"));
		System.out.print(inExMulFU);
		if (inExMulFU != null)
			System.out.print("\tCycle: " + multimer);
		System.out.println();

		System.out.print(String.format("%10s", "In LS FU\t"));
		System.out.print(inEXLSFU + "(Stage1)\n");
		System.out.print(String.format("%10s", "In LS FU\t"));
		System.out.print(inEXLSFU1 + "(Stage2)\n");
		System.out.print(String.format("%10s", "In LS FU\t"));
		System.out.print(inEXLSFU2 + "(Stage3)");

	}

	private void printRenameTable() {
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

	private void printStages() {
		System.out.print(String.format("%10s", "In fetch\t"));
		System.out.print(inFetch + "\n");
		System.out.print(String.format("%10s", "In decode\t"));
		System.out.print(inDecode + "\n");
		System.out.print(String.format("%10s", "In EX\n"));
		this.printFUs();
		// TODO verify timing consistencies
		// System.out.print(String.format("%10s", "In WB\t"));
		// System.out.print(inWB4Int + "\t" + inWB4Mul + "\t" + inWB4LS);
	}

	// initializing processor. TODO copy over from constructor
	private void processorInit() {
		System.out.println("===Processor initialized===");
		GlobalPC = 20000;
	}

	// command line user interfacing- 4 functions
	private void PrintMenuWithInit() {
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

	private int userInput(Scanner s) {
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
	private void compileInstruction(Instruction instruction) throws Exception {
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