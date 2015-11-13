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

	public void doTiming(ArrayList<Instruction> list){
		Instruction first = list.get(0);
		System.out.println("A"+first.row);
		int unkown_delay = 18;
		System.out.println("I11");
		//always happens till this point
		System.out.println(first.type+first.col);
		for(int i=1;i<list.size();i++){
		System.out.println(list.get(i).type+list.get(i).col);
		}
		//multiple instruction here can come if same row
		if(unkown_delay-list.size()>0){
		System.out.println("I"+(unkown_delay-list.size()));
		//	System.out.println("SHit. this should never print");
		}
		else{
			//System.out.println("SHit. this should never print");

		}
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
		while(counter<instructions.size()){

			Instruction first = instructions.get(counter++);
			sameRow.add(first);
			if(counter==instructions.size()){
			Dram.doTiming(sameRow);
			break;
			}
			Instruction next= instructions.get(counter);
			while(counter<(instructions.size()-1) && first.row.equals(next.row) ){
				sameRow.add(next);
				next=instructions.get(++counter);
			}
			//System.out.println(sameRow.size());
			Dram.doTiming(sameRow);
			sameRow.clear();

			//if(counter>20){break;}

		}
	}
}