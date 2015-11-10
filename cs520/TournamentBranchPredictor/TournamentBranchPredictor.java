import java.util.*;
import java.io.*;
public class TournamentBranchPredictor{
	//10 local counters
	public int[] local;
	//global history using an int
	//0 means not taken
	// 1 means taken 
	int globalHistory;
	//64 global counters
	public int[] global;
	//selector counter indexed by 
	public int[] selector;
	// total counters for correct prediction count. Only to benchmark the predictor.
	public int localOnly;
	public int globalOnly;
	public int tournament; 
	//constructor
	public TournamentBranchPredictor(){
		local = new int[10];
		global = new int[64];
		selector = new int[10];
		globalHistory=0;
	}
	//main prediction fn. uses  helper functions defined below this one
	public void predict(String line){
		int branch= Integer.parseInt(String.valueOf(line.charAt(0)));
		StringBuilder s = new StringBuilder();
		boolean actualpath = ('t'==line.charAt(1));
		boolean prediction=false;
		boolean localPredicts= localPredictor(local[branch]);
		boolean globalPredicts= globalPredictor();
		char select = select(selector[branch]);
		if(select=='l'){
			prediction= localPredicts;
		}
		else{
			prediction= globalPredicts;
		}
		//updating local nad global counters
		if(actualpath){
			local[branch]=increment(local[branch]);
			updateGlobal(actualpath);
		}
		else{
			local[branch]=decrement(local[branch]);
			updateGlobal(actualpath);
		}
		countingCorrect(actualpath,localPredicts,globalPredicts,prediction);
		//updating selector counters
		if(localPredicts!=globalPredicts){
			if(localPredicts==actualpath){
				selector[branch]= decrement(selector[branch]);
			}
			else{
				selector[branch]= increment(selector[branch]);
			}
		}
		s.append(branch);
		if(localPredicts)
			s.append("t");
		else
			s.append("n");
		
		if(globalPredicts)
			s.append("t");
		else
			s.append("n");
		s.append(select);
		
		if(prediction)
			s.append("t");
		else
			s.append("n");
		
		if(actualpath)
			s.append("t");
		else
			s.append("n");
		System.out.println(s);
	}
	//fn to increment a counter
	private int increment(int counter){
		if(counter!=3) 
			++counter;
		return counter;
	} 
	//fn to decrement a counter
	private int decrement(int counter){
		if(counter!=0) 
			--counter;
		return counter;
	} 
	//local predictor
	private boolean localPredictor(int counter){
		return counter>=2;
	}
	//global predictor
	private boolean globalPredictor(){
		return global[globalHistory]>=2;
	}
	//select between global and local
	private char select(int counter){
		if(counter<=1) return 'l';
		else return  'g';
	}
	//update global history based on branches taken
	private void updateGlobal(boolean actualpath){
		if(actualpath){
			global[globalHistory]=increment(global[globalHistory]);
		}
		else {
			global[globalHistory]=decrement(global[globalHistory]);
		}
		globalHistory=globalHistory<<1;
		globalHistory=globalHistory & 63;
		if(actualpath){
			globalHistory++;
		}
	}
	//incrementing benchmarking coutners
	private void countingCorrect(boolean actualpath,boolean localPredicts,boolean globalPredicts,boolean prediction){
		if(actualpath==localPredicts)
			++localOnly;
		if(actualpath==globalPredicts)
			++globalOnly;
		if(actualpath==prediction)
			++tournament;
	}
	public static void main(String[] args) {
		TournamentBranchPredictor predictor = new TournamentBranchPredictor();
		Scanner scanner;
		String line;
		try{ 
			scanner = new Scanner(System.in);
			while(scanner.hasNext()) {
				line = scanner.next();			
				predictor.predict(line);
			}
		}
		catch (Exception e) {
			System.out.println("Problem with instructions in the  file. Please check file and retry.\n"+e);
			System.exit(0);
		}
		
		// Uncomment following lines to get count of 
		// System.out.println("Correct Predictions using:");
		// System.out.println("only Local :"+predictor.localOnly);
		// System.out.println("only Global:"+predictor.globalOnly);
		// System.out.println("Local and Global combined :"+predictor.tournament);
	}
}