enum InstructionType {
	ADD, SUB, MUL, AND, OR, XOR, MOVC, MOV, LOAD, STORE, BZ, BNZ, JUMP, BAL, HALT // 15
																					// instruction
}

public class Instruction {
	// falg to detect if contains any data or not
	public boolean contains;
	// instruction type and corresponding pnumonic
	public InstructionType instr_id = null;
	// redundant. Intruction Type in string
	public String inst_name;
	// dest- architectural register. used for renaming register in decode and
	// also while commiting ot ARF
	public int destination = -1;
	// renamed destination register used from decoding to ROB
	public int renamedDestination = -1;
	// to hold destination data till in ROB
	public int destination_data = -1;
	// src Arch registers
	public int src1 = -1, src2 = -1;
	// renamed Source physical registers
	public int renamedSrc1 = -1, renamedSrc2 = -1;
	// data read out in Decode or while in IQ
	public int src1_data = -1, src2_data = -1;
	// literal value if any
	public int literal = -1;
	// for issue queue ready bit for whole instruction, checks sources and not
	// FU availability.
	public boolean isReadyForIssue;
	// for retirement logic from ROB
	public boolean isReadyForCommit;
	// ready bits for sources in forwarding.
	public boolean src1valid, src2valid;
	// ready bit if destination has been evaluated
	public boolean destvalid;
	// plain ASCII representation of the instructions
	public String rawString;
	// renamed instruction i.e. all arch registers replaced by physical
	// registers
	public String renamedString = null;
	// Address in Memory
	// address start from 20,000 onwards, incremented by 1 every time
	public int address;
	// type of FU required. Selection between Int FU(0) and Mul FU(1)
	public int FUtype;
	// age in IQ, to make sure instructions spends atleast one cycle in IQ
	public int ageInIQ;
	// no of operands of a given instruction, bug checking only
	int noOfOperands;
	// to check for WB complete in ROB
	public boolean writtenBack;

	public Instruction() {
		contains = false;
		writtenBack = false;
		ageInIQ = 0;
		instr_id = null;
	}

	public void print() {
		StringBuilder s = new StringBuilder();
		if (instr_id != null) {
			s.append("Instruction:" + instr_id.toString());
		} else {
			System.out.println("Empty");
			return;
		}
		s.append("Dest:R " + destination + "\tSrc1:R " + src1 + "\tSrc2:R " + src2 + "\tliteral:" + literal
				+ "\tAddress(PC):" + address);
		System.out.println(s);

	}

	public void printRaw() {

		System.out.println(rawString);
	}

	public String toString() {
		if (renamedString == null)
			return (rawString);
		else {
			return renamedString;
		}
	}
}