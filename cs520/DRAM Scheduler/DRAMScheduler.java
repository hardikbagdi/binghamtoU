import java.util.*;
import java.io.*;
class Instruction{
		public char type;
		public String rawString;
		public String row;
		public String col;

	public Instruction(String raw){
		// System.out.println(raw);
		rawString=raw;
		if( raw.charAt(0)=='S'){
			type='W';
		}
		else {
			type='R';
		}
		// System.out.println(type);
		row= raw.substring(1,6).toUpperCase();
		// System.out.println(row);
		col= raw.substring(6).toUpperCase();
		// System.out.println(col);
	}
	public String toString(){
		return rawString;
	} 
}
public class DRAMScheduler{

	public void doTiming(Instruction i){
		System.out.println("A"+i.row);
		System.out.println("I11");
		System.out.println(i.type+i.col);
		//always happens till this point

		//multiple instruction here can come if same row

		//following always happens too
		System.out.println("P");
		System.out.println("I19");
	}
	
	public static void main(String[] args) {
		ArrayList<Instruction> instructions = new ArrayList<Instruction>();
		DRAMScheduler Dram = new DRAMScheduler();
		int counter=0;
		Scanner scanner;
		String line;
		try{ 
			scanner = new Scanner(System.in);
			while(scanner.hasNext()) {
				line = scanner.next();			
				Instruction i = new Instruction(line);
				instructions.add(i);
				// Dram.doTiming(i);
				// System.exit(0);
			}
		}
		catch (Exception e) {
			System.out.println("Problem with instructions in the  file. Please check file and retry.\n"+e);
			System.exit(0);
		}

		// System.out.println(instructions.size());
		
		ArrayList<Instruction> sameRow = new ArrayList<Instruction>();
		
	}
}