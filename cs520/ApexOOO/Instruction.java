enum InstructionType {
	ADD, SUB, MUL, AND, OR, XOR, MOVC, MOV, LOAD, STORE, BZ, BNZ, JUMP, BAL, HALT
}

public class Instruction {
	// enum type{MOV,JUMP};
	// falg to detect if contains any data or not
	public boolean contains;
	// instruction type and corresponding pnumonic

	public InstructionType instr_id = null;
	public String inst_name = "";
	// dest
	public int destination = -1;
	//renamed destination register
	public int renamedDestination = -1;
	//to hold destination data till WB
	public int destination_data = -1;
	// src
	public int src1 = -1, src2 = -1;
	//renamed Source physical registers
	public int renamedSrc1 = -1, renamedSrc2 = -1;
	//data read out in Decode or while in IQ
	public int src1_data = -1, src2_data = -1;
	// literal
	public int literal = -1;
	
	//for issue queue ready bit for whole instruction
	public boolean isReadyForIssue;
	//for retirement logic from ROB
	public boolean isReadyForCommit;
	//ready bits for sources in forwarding.
	public boolean src1valid,src2valid;

	// plain ascii representation of the instructions
	public String rawString;
	// renamed instruction
	public String renamedString;
	// Address in Memory
	public int address; // address start from 20,000 onwards, incremented by 1
						// everytime
	// no of operands of a given instruction
	int noOfOperands;

	public Instruction() {
		contains = false;
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

		return (rawString);
	}
}