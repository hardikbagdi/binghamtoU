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
	static ArrayList<Instruction> instructions;
	int globalCounter=0;
	public void doTiming(ArrayList<Instruction> list){
		int i=0,flag=0;;
		int switchCount=0;
		char currentSequence;
		Instruction first = list.get(0);
		System.out.println("A"+first.row);
		int unkown_delay = 18;
		System.out.println("I11");
		//always happens till this point
		//multiple instruction here can come if same row
		//System.out.println(first.type+first.col);
		// for(i=1;i<list.size();i++){
			
		// 	System.out.println(list.get(i).type+list.get(i).col);
		// }
		globalCounter+=list.size();
		while(i<list.size()){
			currentSequence= list.get(i).type;
			System.out.println(list.get(i).type+list.get(i).col);
			if(list.get(i).type=='R') 
				flag=2;
			else
				flag=0;
			
			i++;

			while(i<list.size() && currentSequence == list.get(i).type){
				if(list.get(i).type=='R') 
					flag=2;
				else
					--flag;
				System.out.println(list.get(i).type+list.get(i).col);
					
				i++;
			}

			if(i<list.size() && currentSequence=='R'){
				if(list.get(i).type=='R') 
					flag=2;
				else
					--flag;
				switchCount++;
				System.out.println("I3");
			}


		}
		if(globalCounter>=(instructions.size())) return;
		if((unkown_delay-list.size()-(3*switchCount))>0){
			if((unkown_delay-list.size()-(3*switchCount))>flag){

				System.out.println("I"+(unkown_delay-3*switchCount-list.size()));
		//	System.out.println("SHit. this should never print");
			}
			else{
				System.out.println("I"+flag);
				
			}
		}
		else{
			//System.out.println("SHit. this should never print");
				if(flag>0)
				System.out.println("I"+flag);

		}
		//following always happens too
		System.out.println("P");
		System.out.println("I19");
	}
	
	public static void main(String[] args) {
		instructions = new ArrayList<Instruction>();
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