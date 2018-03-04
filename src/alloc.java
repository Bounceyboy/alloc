import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

public class alloc {
	
	static class Line {
		int reg1, reg2, reg3, c;
		String instruction;

		Line() {
			this.reg1 = -1;
			this.reg2 = -1;
			this.reg3 = -1;
			this.c = Integer.MIN_VALUE;
			this.instruction = "void";
		}
	}

	/*
	total registers, array[k-2] and array[k-1] are feasible regs in top down
	*/
	static class Regs {
		int[] array;

		Regs(int k){
			this.array = new int[k];
		}
	}
	
	static class Reg {
		int liveStart, liveEnd;
		boolean spill;
		Reg(){
			this.liveStart = -1;
			this.liveEnd = -1;
			this.spill = false;
		}
	}
	
	static class Max {
		int numLive;
		ArrayList<Integer> liveRegisters;
		Max(){
			this.numLive = 0;
			this.liveRegisters = new ArrayList<Integer>();
		}
	}
	
	//takes register number and regs object and creates the spill Line to return, still must be added to code object
	public static Line spill(int reg, int offset){
		Line current = new Line();
		current.instruction = "storeAI";
		current.c = offset;
		current.reg1 = reg+1;
		current.reg3 = 0;
		return current;
	}
	
	//takes int for register to load into, int for offset to load from, and creates a line to load the proper register's info from memory into the regs object
	//line still needs to be added to code object
	public static Line load(int reg, int offset) {
		Line current = new Line();
		current.instruction = "loadAI";
		current.reg1 = 0;
		current.c = offset;
		current.reg3 = reg+1;
		return current;
		
	}
	
	//takes arraylist of ranges and maxlive arraylist, populates maxlive, returns it
	public static ArrayList<Max> CalcMaxLive(ArrayList<Reg> regs, ArrayList<Max> maxLive){
		int s = maxLive.size();
		maxLive.clear();
		for (int w = 0; w < s; w++) {
			maxLive.add(new Max());
		}
		Max current;
		for(int y = 1; y<regs.size(); y++) {
			if(!regs.get(y).spill) {
				for(int x = regs.get(y).liveStart; x< regs.get(y).liveEnd; x++) {
					current = maxLive.get(x);
					current.numLive++;
					current.liveRegisters.add(y);
				}
			}
		}
		
		return maxLive;
	}
	
	public static ArrayList<Max> BuildAppearances (ArrayList<Line> code){
		
		Line current;
		ArrayList<Max> appearances = new ArrayList<Max>();
		appearances.add(new Max());
		for(int x = 1; x < code.size(); x++) {
			//for each line, take each register, add it to appearances, and populate the "liveRegisters" with where each register appears
			current = code.get(x);
			if(current.reg1 != -1 && current.reg1 != 0) {
				while(appearances.size() < current.reg1+1) {
					appearances.add(new Max());
				}
				appearances.get(current.reg1).liveRegisters.add(x);
				appearances.get(current.reg1).numLive++;
			}
			if(current.reg2 != -1 && current.reg2 != 0) {
				while(appearances.size() < current.reg2+1) {
					appearances.add(new Max());
				}
				appearances.get(current.reg2).liveRegisters.add(x);
				appearances.get(current.reg2).numLive++;
			}
			if(current.reg3 != -1 && current.reg3 != 0) {
				while(appearances.size() < current.reg3+1) {
					appearances.add(new Max());
				}
				appearances.get(current.reg3).liveRegisters.add(x);
				appearances.get(current.reg3).numLive++;
			}
		}
		return appearances;
	}
	
	/*
	 * returns index + 1 of the thing to replace, negative if it doesn't need to be spilled, positive otherwise
	 */
	public static int findReplacement(int[] array, ArrayList<Max> appearances, int currentLine) {
		int maxDistance = -50;
		int index = 0;
		for (int y = 0; y < array.length; y++) {
			//for each currently existing register:
			//if its liverange is over, return that
			//else, return the index of the register that isn't used for the longest time
			
			if(appearances.get(array[y]).numLive == 0)
				return ((y+1)*-1);
			//calculate index that isn't used for the longest time
			
			if(appearances.get(array[y]).numLive > 1){
				if(appearances.get(array[y]).liveRegisters.get(1) > maxDistance){
					maxDistance = appearances.get(array[y]).liveRegisters.get(1);
					index = y+1;
				}
			}
			else if (appearances.get(array[y]).numLive == 1){
				if(appearances.get(array[y]).liveRegisters.get(0) > maxDistance && appearances.get(array[y]).liveRegisters.get(0) > currentLine){
					maxDistance = appearances.get(array[y]).liveRegisters.get(0);
					index = y+1;
				}
			}
		}
		return index;
	}
	
	
    public static void printCode(ArrayList<Line> code) {
    	Line current;
    	for(int x = 0; x<code.size(); x++) {
    		current = code.get(x);
    		System.out.print(current.instruction + "\t");
    		switch (current.instruction) {
    		case "loadI":
    			System.out.print(current.c + "\t=> r" + current.reg3 + "\n");
        		break;
        	case "add":
        		System.out.print("r" + current.reg1 + ", r" + current.reg2 + "\t=> r" + current.reg3 + "\n");
        		break;
        	case "sub":
        		System.out.print("r" + current.reg1 + ", r" + current.reg2 + "\t=> r" + current.reg3 + "\n");
        		break;
        	case "lshift":
        		System.out.print("r" + current.reg1 + ", r" + current.reg2 + "\t=> r" + current.reg3 + "\n");
        		break;
        	case "rshift":
        		System.out.print("r" + current.reg1 + ", r" + current.reg2 + "\t=> r" + current.reg3 + "\n");
        		break;
        	case "load":
        		System.out.print("r" + current.reg1 + "\t=> r" + current.reg3 + "\n");
        		break;
        	case "loadAI":
        		System.out.print("r" + current.reg1 + ", " + current.c + "\t=> r" + current.reg3 + "\n");
        		break;
        	case "store":
        		System.out.print("r" + current.reg1 + "\t=> r" + current.reg3 + "\n");
        		break;
        	case "storeAI":
        		System.out.print("r" + current.reg1 + "\t=> r" + current.reg3 + ", " + current.c + "\n");
        		break;
        	case "mult":
        		System.out.print("r" + current.reg1 + ", r" + current.reg2 + "\t=> r" + current.reg3 + "\n");
        		break;
        	case "div":
        		System.out.print("r" + current.reg1 + ", r" + current.reg2 + "\t=> r" + current.reg3 + "\n");
        		break;
        	case "output":
        		System.out.print(current.c + "\n");
        		break;
        	default:
        		System.out.println("Line didn't parse properly");
        		return;
    		}
    	}
    }
	
    public static void main (String[] args) {
    	//input checking
        if (args.length != 3) {
        	System.out.println("\nPlease use a valid input of the form:");
        	System.out.println("java alloc k tag filename\n");
        	System.out.println("where k is the number of available registers, tag is one of the set {b,s,t,o} and filename is a text file of ILOC code.\n");
        	return;
        }
        else if (args[1].length() > 1) {
        	System.out.println("\nPlease use a valid input of the form:");
        	System.out.println("java alloc k tag filename\n");
        	System.out.println("where k is the number of available registers, tag is one of the set {b,s,t,o} and filename is a text file of ILOC code.\n");
        	return;
        }
        else {
        	char type = args[1].charAt(0);
        	if (type != 'b' && type != 's' && type != 't' && type != 'o') {
        		System.out.println("\nPlease use a valid input of the form:");
            	System.out.println("java alloc k tag filename\n");
            	System.out.println("where k is the number of available registers, tag is one of the set {b,s,t,o} and filename is a text file of ILOC code.\n");
            	return;
        	}
        	
        	int k = Integer.parseInt(args[0]);
        	if (k < 3) {
        		System.out.println("Please ensure k>=3.");
        		return;
        	}
        	
        	String filename = args[2];
        	File file = new File(filename);        
        	ArrayList<Line> code = new ArrayList<Line>();
        	//inputs stored -> type, k, and file/filename
            if(type == 's'){	//TODO type s - complete (can optimize a lot of instructions though)
	            //start parsing file
	        	ArrayList<Integer> counts = new ArrayList<Integer>();
	        	int x = 0;
	        	counts.add(x);        	
	        	//at each index e, counts[e] = number of times re is seen in the iloc code
	        
	        	String line;
	        	String token;
	        	Line current;
	        	
	            try {
					Scanner input = new Scanner(file);
		            while(input.hasNext()) {
		            	//for each line
		            	line = input.nextLine();
		            	//current.instruction
		            		//System.out.println(line);
		            	if(line.equals(""))
		            		continue;
		            	
		            	StringTokenizer a = new StringTokenizer(line, "\t ,=>");
		            	token = a.nextToken();   	
		            	if(token.equals("//"))
		            		continue;
		            	
		            	//store instruction
		            	current = new Line();
		            	current.instruction = token;
		            	switch (current.instruction) {
		            	case "loadI":	//only reg3
		            		token=a.nextToken();
		            		current.c = Integer.parseInt(token);
		            		token = a.nextToken();
		            		token = token.substring(1);
		            		current.reg3 = Integer.parseInt(token);
		            		while(counts.size()<=current.reg3) {
		            			counts.add(x);
		            		}
		            		counts.set(current.reg3, counts.get(current.reg3)+1);
		            		code.add(current);
		            		break;
		            	case "add":	//all 3
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg1 = Integer.parseInt(token);
		            		while(counts.size()<=current.reg1) {
		            			counts.add(x);
		            		}
		            		counts.set(current.reg1, counts.get(current.reg1)+1);
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg2 = Integer.parseInt(token);
		            		while(counts.size()<=current.reg2) {
		            			counts.add(x);
		            		}
		            		counts.set(current.reg2, counts.get(current.reg2)+1);
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg3 = Integer.parseInt(token);
		            		while(counts.size()<=current.reg3) {
		            			counts.add(x);
		            		}
		            		counts.set(current.reg3, counts.get(current.reg3)+1);
		            		code.add(current);
		            		break;
		            	case "sub":	//all 3
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg1 = Integer.parseInt(token);
		            		while(counts.size()<=current.reg1) {
		            			counts.add(x);
		            		}
		            		counts.set(current.reg1, counts.get(current.reg1)+1);
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg2 = Integer.parseInt(token);
		            		while(counts.size()<=current.reg2) {
		            			counts.add(x);
		            		}
		            		counts.set(current.reg2, counts.get(current.reg2)+1);
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg3 = Integer.parseInt(token);
		            		while(counts.size()<=current.reg3) {
		            			counts.add(x);
		            		}
		            		counts.set(current.reg3, counts.get(current.reg3)+1);
		            		code.add(current);
		            		break;
		            	case "lshift":	//all 3
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg1 = Integer.parseInt(token);
		            		while(counts.size()<=current.reg1) {
		            			counts.add(x);
		            		}
		            		counts.set(current.reg1, counts.get(current.reg1)+1);
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg2 = Integer.parseInt(token);
		            		while(counts.size()<=current.reg2) {
		            			counts.add(x);
		            		}
		            		counts.set(current.reg2, counts.get(current.reg2)+1);
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg3 = Integer.parseInt(token);
		            		while(counts.size()<=current.reg3) {
		            			counts.add(x);
		            		}
		            		counts.set(current.reg3, counts.get(current.reg3)+1);
		            		code.add(current);
		            		break;
		            	case "rshift":	//all 3
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg1 = Integer.parseInt(token);
		            		while(counts.size()<=current.reg1) {
		            			counts.add(x);
		            		}
		            		counts.set(current.reg1, counts.get(current.reg1)+1);
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg2 = Integer.parseInt(token);
		            		while(counts.size()<=current.reg2) {
		            			counts.add(x);
		            		}
		            		counts.set(current.reg2, counts.get(current.reg2)+1);
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg3 = Integer.parseInt(token);
		            		while(counts.size()<=current.reg3) {
		            			counts.add(x);
		            		}
		            		counts.set(current.reg3, counts.get(current.reg3)+1);
		            		code.add(current);
		            		break;
		            	case "load":	//1 and 3
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg1 = Integer.parseInt(token);
		            		while(counts.size()<=current.reg1) {
		            			counts.add(x);
		            		}
		            		counts.set(current.reg1, counts.get(current.reg1)+1);
		            		token = a.nextToken();
		            		token = token.substring(1);
		            		current.reg3 = Integer.parseInt(token);
		            		while(counts.size()<=current.reg3) {
		            			counts.add(x);
		            		}
		            		counts.set(current.reg3, counts.get(current.reg3)+1);
		            		code.add(current);
		            		break;
		            	case "loadAI":		//1 and 3, 1 is always r0
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg1 = Integer.parseInt(token);
		            		while(counts.size()<=current.reg1) {
		            			counts.add(x);
		            		}
		            		counts.set(current.reg1, counts.get(current.reg1)+1);
		            		token = a.nextToken();
		            		current.c = Integer.parseInt(token);
		            		token = a.nextToken();
		            		token = token.substring(1);
		            		current.reg3 = Integer.parseInt(token);
		            		while(counts.size()<=current.reg3) {
		            			counts.add(x);
		            		}
		            		counts.set(current.reg3, counts.get(current.reg3)+1);
		            		code.add(current);
		            		break;
		            	case "store":	//1 and 3
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg1 = Integer.parseInt(token);
		            		while(counts.size()<=current.reg1) {
		            			counts.add(x);
		            		}
		            		counts.set(current.reg1, counts.get(current.reg1)+1);
		            		token = a.nextToken();
		            		token = token.substring(1);
		            		current.reg3 = Integer.parseInt(token);
		            		while(counts.size()<=current.reg3) {
		            			counts.add(x);
		            		}
		            		counts.set(current.reg3, counts.get(current.reg3)+1);
		            		code.add(current);
		            		break;
		            	case "storeAI":		//1 and 3, 3 is always r0
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg1 = Integer.parseInt(token);
		            		while(counts.size()<=current.reg1) {
		            			counts.add(x);
		            		}
		            		counts.set(current.reg1, counts.get(current.reg1)+1);
		            		token = a.nextToken();
		            		current.c = Integer.parseInt(token);
		            		token = a.nextToken();
		            		token = token.substring(1);
		            		current.reg3 = Integer.parseInt(token);
		            		while(counts.size()<=current.reg3) {
		            			counts.add(x);
		            		}
		            		counts.set(current.reg3, counts.get(current.reg3)+1);
		            		code.add(current);
		            		break;
		            	case "mult":	//all 3
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg1 = Integer.parseInt(token);
		            		while(counts.size()<=current.reg1) {
		            			counts.add(x);
		            		}
		            		counts.set(current.reg1, counts.get(current.reg1)+1);
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg2 = Integer.parseInt(token);
		            		while(counts.size()<=current.reg2) {
		            			counts.add(x);
		            		}
		            		counts.set(current.reg2, counts.get(current.reg2)+1);
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg3 = Integer.parseInt(token);
		            		while(counts.size()<=current.reg3) {
		            			counts.add(x);
		            		}
		            		counts.set(current.reg3, counts.get(current.reg3)+1);
		            		code.add(current);
		            		break;
		            	case "div":	//all 3
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg1 = Integer.parseInt(token);
		            		while(counts.size()<=current.reg1) {
		            			counts.add(x);
		            		}
		            		counts.set(current.reg1, counts.get(current.reg1)+1);
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg2 = Integer.parseInt(token);
		            		while(counts.size()<=current.reg2) {
		            			counts.add(x);
		            		}
		            		counts.set(current.reg2, counts.get(current.reg2)+1);
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg3 = Integer.parseInt(token);
		            		while(counts.size()<=current.reg3) {
		            			counts.add(x);
		            		}
		            		counts.set(current.reg3, counts.get(current.reg3)+1);
		            		code.add(current);
		            		break;
		            	case "output":
		            		token = a.nextToken();
		            		current.c = Integer.parseInt(token);
		            		code.add(current);
		            		break;
		            	default:
		            		System.out.println("Line didn't parse properly");
		            		return;
		            	} 	            	
		            }
		            input.close();
		            
				} catch (FileNotFoundException e) {
					System.out.println("Can't find the file, please make sure it's in the current directory.");
	        		return;
				}
	
	            
	            Regs registers = new Regs(k);
	            
	            	//simple
	            	int max = 0;
	            	int reg = -1;
	//																			                for (int r = 0; r < counts.size(); r++) {
	//																			                	System.out.println(r + "\t" + counts.get(r));
	//																			                }
	//																			                System.out.println("-");
            	for (x = 0; x<k-2; x++){
            		for (int q = 1; q<counts.size(); q++){
            			if (counts.get(q)>max){
            				max = counts.get(q);
            				reg = q;
            			}
            		}
            		max = 0;
            		counts.set(reg, 0);
            		registers.array[x] = reg;
            	}
//            	for (int i = 0; i<registers.array.length; i++) {
//            		System.out.println(registers.array[i]);
//            	}
            	boolean changed1, changed2, changed3;
            	boolean bool = true;
        		ArrayList<Integer> offsets = new ArrayList<Integer>();
        		for (int o = 0; o < counts.size(); o++) {
        			offsets.add(0);
        		}
        		int maxOffset = 0;
        		int currentOffset = 0;
        		Line ls;
            	for (x = 0; x < code.size(); x++){
            		/*
            		 * for each line do the following:
            		 * if any register is one of the ones in the non-feasible registers, change its value to the index of that register
            		 * for all other registers, do the following:
            		 * 		1. Check array[k-2] and array[k-1] for reg3
            		 * 		2. if it's there, just replace it with index. else, check for empty feasible reg. If there is one, set it to reg3
            		 * 		3. else, if bool=t replace k-2 else replace k-1, switch bool (replacing includes spilling)
            		 * 		4. if reg1 or reg2 != non-feasible reg ones and aren't stored in feasible regs, load them with the same alg as above
            		 * 		****this will have to be adjusted if all 3 are non-feasible registers, in that case we must load reg1 and reg2 first,
            		 * 			(spilling the existing feasible registers in use), then spill reg1 or reg2 based on bool, replace that one with reg3
            		 */
            		
            		current = code.get(x);
            		if (registers.array[k-1] != 0 && !current.instruction.equals("output")) {
            			//spill it
            			currentOffset = offsets.get(registers.array[k-1]);
    					if (currentOffset == 0) {
    						maxOffset-=4;
    						ls = spill(k-1,maxOffset);
    						offsets.set(registers.array[k-1], maxOffset);
    						code.add(x, ls);
    						x++;
    					}
    					else {
    						ls = spill(k-1,currentOffset);
    						code.add(x, ls);
    						x++;
    					}
            		}

            		if(current.instruction.equals("add") || current.instruction.equals("sub") || current.instruction.equals("mult") || current.instruction.equals("div") || current.instruction.equals("lshift") || current.instruction.equals("rshift")){
                		changed1 = false;
                		changed2 = false;
                		changed3 = false;
            			for (int q = 0; q<k-2; q++){
	            			if (registers.array[q] == current.reg1 && changed1 == false){
	            				current.reg1 = q+1;
	            				changed1 = true;
	            			}
	            			if (registers.array[q] == current.reg2 && changed2 == false){
	            				current.reg2 = q+1;
	            				changed2 = true;
	            			}
	            			if (registers.array[q] == current.reg3 && changed3 == false){
	            				current.reg3 = q+1;
	            				changed3 = true;
	            			}
	            		}
            			
            			//this will break code for now
            			
//            			if(registers.array[k-2] == current.reg1 && changed1 == false) {
//            				current.reg1 = k-1;
//            				changed1 = true;
//            			}
//            			else if (registers.array[k-1] == current.reg1 && changed1 == false) {
//            				current.reg1 = k;
//            				changed1 = true;
//            			}
//            			if(registers.array[k-2] == current.reg2 && changed1 == false) {
//            				current.reg2 = k-1;
//            				changed2 = true;
//            			}
//            			else if (registers.array[k-1] == current.reg2 && changed1 == false) {
//            				current.reg2 = k;
//            				changed2 = true;
//            			}
//            			if(registers.array[k-2] == current.reg3 && changed1 == false) {
//            				current.reg3 = k-1;
//            				changed3 = true;
//            			}
//            			else if (registers.array[k-1] == current.reg3 && changed1 == false) {
//            				current.reg3 = k;
//            				changed3 = true;
//            			}
            			
            			//3 register op
            			//shouldn't ever need this but can't hurt to ensure we leave weird instances of r0 alone
            			if(current.reg1 == 0)
            				changed1 = true;
            			if(current.reg2 == 0)
            				changed2 = true;
            			if(current.reg3 == 0)
            				changed3 = true;
            			//everything else needs feasible registers, spill and load instructions work
            			
            			if(!changed1 && !changed2 && !changed3) {
            				if(registers.array[k-2] != 0) {
            					currentOffset = offsets.get(registers.array[k-2]);
            					if (currentOffset == 0) {
            						maxOffset-=4;
            						ls = spill(k-2,maxOffset);
            						offsets.set(registers.array[k-2], maxOffset);
            						code.add(x, ls);
            						x++;
            					}
            					else {
            						ls = spill(k-2,currentOffset);
            						code.add(x, ls);
            						x++;
            					}
            				}
            				if(registers.array[k-1] != 0) {
            					currentOffset = offsets.get(registers.array[k-1]);
            					if (currentOffset == 0) {
            						maxOffset-=4;
            						ls = spill(k-1,maxOffset);
            						offsets.set(registers.array[k-1], maxOffset);
            						code.add(x, ls);
            						x++;
            					}
            					else {
            						ls = spill(k-1,currentOffset);
            						code.add(x, ls);
            						x++;
            					}
            				}
        					//load
//									            				System.out.println(offsets.get(current.reg1));
//									            				System.out.println(offsets.get(current.reg2));
//									            				System.out.println(current.reg1);
//									            				System.out.println(current.reg2);
            				if(offsets.get(current.reg1) != 0) {
            					ls = load(k-2, offsets.get(current.reg1));
            					code.add(x, ls);
            					x++;
            				}
        					registers.array[k-2] = current.reg1;
        					current.reg1 = k-1;
        					//load
            				if(offsets.get(current.reg2) != 0) {
            					ls = load(k-1, offsets.get(current.reg2));
            					code.add(x, ls);
            					x++;
            				}
        					current.reg2 = k;
        					
        					registers.array[k-1] = current.reg3;
        					current.reg3 = k;
            			}
            			else {
            				if (!changed1 && !changed2) {
                				if(registers.array[k-2] != 0) {
	            					currentOffset = offsets.get(registers.array[k-2]);
	            					if (currentOffset == 0) {
	            						maxOffset-=4;
	            						ls = spill(k-2,maxOffset);
	            						offsets.set(registers.array[k-2], maxOffset);
	            						code.add(x, ls);
	            						x++;
	            					}
	            					else {
	            						ls = spill(k-2,currentOffset);
	            						code.add(x, ls);
	            						x++;
	            					}
                				}
            					//load
                				if(offsets.get(current.reg1) != 0) {
                					ls = load(k-2, offsets.get(current.reg1));
                					code.add(x, ls);
                					x++;
                				}
            					registers.array[k-2] = current.reg1;
            					current.reg1 = k-1;
                				if(registers.array[k-1] != 0) {
	            					currentOffset = offsets.get(registers.array[k-1]);
	            					if (currentOffset == 0) {
	            						maxOffset-=4;
	            						ls = spill(k-1,maxOffset);
	            						offsets.set(registers.array[k-1], maxOffset);
	            						code.add(x, ls);
	            						x++;
	            					}
	            					else {
	            						ls = spill(k-1,currentOffset);
	            						code.add(x, ls);
	            						x++;
	            					}
                				}
            					//load
                				if(offsets.get(current.reg2) != 0) {
                					ls = load(k-1, offsets.get(current.reg2));
                					code.add(x, ls);
                					x++;
                				}
            					registers.array[k-1] = current.reg2;
            					current.reg2 = k;
            				}
            				else if(!changed2 && !changed3) {
                  				if(registers.array[k-2] != 0) {
    	            					currentOffset = offsets.get(registers.array[k-2]);
    	            					if (currentOffset == 0) {
    	            						maxOffset-=4;
    	            						ls = spill(k-2,maxOffset);
    	            						offsets.set(registers.array[k-2], maxOffset);
    	            						code.add(x, ls);
    	            						x++;
    	            					}
    	            					else {
    	            						ls = spill(k-2,currentOffset);
    	            						code.add(x, ls);
    	            						x++;
    	            					}
                    				}
                					//load
	                				if(offsets.get(current.reg2) != 0) {
	                					ls = load(k-2, offsets.get(current.reg2));
	                					code.add(x, ls);
	                					x++;
	                				}
                					registers.array[k-2] = current.reg2;
                					current.reg2 = k-1;
                    				if(registers.array[k-1] != 0) {
    	            					currentOffset = offsets.get(registers.array[k-1]);
    	            					if (currentOffset == 0) {
    	            						maxOffset-=4;
    	            						ls = spill(k-1,maxOffset);
    	            						offsets.set(registers.array[k-1], maxOffset);
    	            						code.add(x, ls);
    	            						x++;
    	            					}
    	            					else {
    	            						ls = spill(k-1,currentOffset);
    	            						code.add(x, ls);
    	            						x++;
    	            					}
                    				}
                					//load
                    				if(offsets.get(current.reg3) != 0) {
	                					ls = load(k-1, offsets.get(current.reg3));
	                					code.add(x, ls);
	                					x++;
                    				}
                    				registers.array[k-1] = current.reg3;
                					current.reg3 = k;
            				}
            				else if (!changed1 && !changed3){
                  				if(registers.array[k-2] != 0) {
    	            					currentOffset = offsets.get(registers.array[k-2]);
    	            					if (currentOffset == 0) {
    	            						maxOffset-=4;
    	            						ls = spill(k-2,maxOffset);
    	            						offsets.set(registers.array[k-2], maxOffset);
    	            						code.add(x, ls);
    	            						x++;
    	            					}
    	            					else {
    	            						ls = spill(k-2,currentOffset);
    	            						code.add(x, ls);
    	            						x++;
    	            					}
                    				}
                					//load
	                  				if(offsets.get(current.reg1) != 0) {
	                					ls = load(k-2, offsets.get(current.reg1));
	                					code.add(x, ls);
	                					x++;
	                				}
                					registers.array[k-2] = current.reg1;
                					current.reg1 = k-1;
                    				if(registers.array[k-1] != 0) {
    	            					currentOffset = offsets.get(registers.array[k-1]);
    	            					if (currentOffset == 0) {
    	            						maxOffset-=4;
    	            						ls = spill(k-1,maxOffset);
    	            						offsets.set(registers.array[k-1], maxOffset);
    	            						code.add(x, ls);
    	            						x++;
    	            					}
    	            					else {
    	            						ls = spill(k-1,currentOffset);
    	            						code.add(x, ls);
    	            						x++;
    	            					}
                    				}
                					//load
                    				if(offsets.get(current.reg3) != 0) {
	                					ls = load(k-1, offsets.get(current.reg3));
	                					code.add(x, ls);
	                					x++;
                    				}
                    				registers.array[k-1] = current.reg3;
                					current.reg3 = k;
            				}
            				else {
            					if(!changed1) {
            						if(registers.array[k-2] != 0) {
    	            					currentOffset = offsets.get(registers.array[k-2]);
    	            					if (currentOffset == 0) {
    	            						maxOffset-=4;
    	            						ls = spill(k-2,maxOffset);
    	            						offsets.set(registers.array[k-2], maxOffset);
    	            						code.add(x, ls);
    	            						x++;
    	            					}
    	            					else {
    	            						ls = spill(k-2,currentOffset);
    	            						code.add(x, ls);
    	            						x++;
    	            					}
                    				}
                					//load
                    				if(offsets.get(current.reg1) != 0) {
	                					ls = load(k-2, offsets.get(current.reg1));
	                					code.add(x, ls);
	                					x++;
                    				}
                					registers.array[k-2] = current.reg1;
                					current.reg1 = k-1;
            					}
            					else if(!changed2) {
            						if(registers.array[k-1] != 0) {
    	            					currentOffset = offsets.get(registers.array[k-1]);
    	            					if (currentOffset == 0) {
    	            						maxOffset-=4;
    	            						ls = spill(k-1,maxOffset);
    	            						offsets.set(registers.array[k-1], maxOffset);
    	            						code.add(x, ls);
    	            						x++;
    	            					}
    	            					else {
    	            						ls = spill(k-1,currentOffset);
    	            						code.add(x, ls);
    	            						x++;
    	            					}
                    				}
                					//load
                    				if(offsets.get(current.reg2) != 0) {
	                					ls = load(k-1, offsets.get(current.reg2));
	                					code.add(x, ls);
	                					x++;
                    				}
                					registers.array[k-1] = current.reg2;
                					current.reg2 = k;
            					}
            					else if(!changed3) {
                    				if(registers.array[k-1] != 0) {
    	            					currentOffset = offsets.get(registers.array[k-1]);
    	            					if (currentOffset == 0) {
    	            						maxOffset-=4;
    	            						ls = spill(k-1,maxOffset);
    	            						offsets.set(registers.array[k-1], maxOffset);
    	            						code.add(x, ls);
    	            						x++;
    	            					}
    	            					else {
    	            						ls = spill(k-1,currentOffset);
    	            						code.add(x, ls);
    	            						x++;
    	            					}
                    				}
                					//load
                    				if(offsets.get(current.reg3) != 0) {
	                					ls = load(k-1, offsets.get(current.reg3));
	                					code.add(x, ls);
	                					x++;
                    				}
                    				registers.array[k-1] = current.reg3;
                					current.reg3 = k;
            					}
            				}
            			}	
            		}
            		else if(current.instruction.equals("load") || current.instruction.equals("store") || current.instruction.equals("loadAI") || current.instruction.equals("storeAI")){
            			//2 register op
            			changed1 = false;
                		changed3 = false;
            			for (int q = 0; q<k-2; q++){
	            			if (registers.array[q] == current.reg1 && changed1 == false){
	            				current.reg1 = q+1;
	            				changed1 = true;
	            			}
	            			if (registers.array[q] == current.reg3 && changed3 == false){
	            				current.reg3 = q+1;
	            				changed3 = true;
	            			}
            			}
            			if(current.reg1 == 0)
            				changed1 = true;
            			if(current.reg3 == 0)
            				changed3 = true;
            			
            			if(!changed1 && !changed3) {
              				if(registers.array[k-2] != 0) {
	            					currentOffset = offsets.get(registers.array[k-2]);
	            					if (currentOffset == 0) {
	            						maxOffset-=4;
	            						ls = spill(k-2,maxOffset);
	            						offsets.set(registers.array[k-2], maxOffset);
	            						code.add(x, ls);
	            						x++;
	            					}
	            					else {
	            						ls = spill(k-2,currentOffset);
	            						code.add(x, ls);
	            						x++;
	            					}
                				}
            					//load
                  				if(offsets.get(current.reg1) != 0) {
                					ls = load(k-2, offsets.get(current.reg1));
                					code.add(x, ls);
                					x++;
                				}
            					registers.array[k-2] = current.reg1;
            					current.reg1 = k-1;
                				if(registers.array[k-1] != 0) {
	            					currentOffset = offsets.get(registers.array[k-1]);
	            					if (currentOffset == 0) {
	            						maxOffset-=4;
	            						ls = spill(k-1,maxOffset);
	            						offsets.set(registers.array[k-1], maxOffset);
	            						code.add(x, ls);
	            						x++;
	            					}
	            					else {
	            						ls = spill(k-1,currentOffset);
	            						code.add(x, ls);
	            						x++;
	            					}
                				}
            					//load
                				if(offsets.get(current.reg3) != 0) {
                					ls = load(k-1, offsets.get(current.reg3));
                					code.add(x, ls);
                					x++;
                				}
                				registers.array[k-1] = current.reg3;
            					current.reg3 = k;
            			}
            			else {
            				if(!changed1) {
        						if(registers.array[k-2] != 0) {
	            					currentOffset = offsets.get(registers.array[k-2]);
	            					if (currentOffset == 0) {
	            						maxOffset-=4;
	            						ls = spill(k-2,maxOffset);
	            						offsets.set(registers.array[k-2], maxOffset);
	            						code.add(x, ls);
	            						x++;
	            					}
	            					else {
	            						ls = spill(k-2,currentOffset);
	            						code.add(x, ls);
	            						x++;
	            					}
                				}
            					//load
                				if(offsets.get(current.reg1) != 0) {
                					ls = load(k-2, offsets.get(current.reg1));
                					code.add(x, ls);
                					x++;
                				}
            					registers.array[k-2] = current.reg1;
            					current.reg1 = k-1;
            				}
            				else if (!changed3) {
                				if(registers.array[k-1] != 0) {
	            					currentOffset = offsets.get(registers.array[k-1]);
	            					if (currentOffset == 0) {
	            						maxOffset-=4;
	            						ls = spill(k-1,maxOffset);
	            						offsets.set(registers.array[k-1], maxOffset);
	            						code.add(x, ls);
	            						x++;
	            					}
	            					else {
	            						ls = spill(k-1,currentOffset);
	            						code.add(x, ls);
	            						x++;
	            					}
                				}
            					//load
                				if(offsets.get(current.reg3) != 0) {
                					ls = load(k-1, offsets.get(current.reg3));
                					code.add(x, ls);
                					x++;
                				}
                				registers.array[k-1] = current.reg3;
            					current.reg3 = k;
            				}
            			}
            		}
            		else if(current.instruction.equals("loadI")){
            			//1 register op
            			changed1 = false;
            			for (int q = 0; q<k-2; q++){
	            			if (registers.array[q] == current.reg3 && changed1 == false){
	            				current.reg3 = q+1;
	            				changed1 = true;
	            			}
            			}
            			
            			if(current.reg3 == 0)
            				changed1 = true;
            			
            			if(!changed1) {
            				if(bool) {
            					bool = false;
	            				if(registers.array[k-2] != 0) {
	            					currentOffset = offsets.get(registers.array[k-2]);
	            					if (currentOffset == 0) {
	            						maxOffset-=4;
	            						ls = spill(k-2,maxOffset);
	            						offsets.set(registers.array[k-2], maxOffset);
	            						code.add(x, ls);
	            						x++;
	            					}
	            					else {
	            						ls = spill(k-2,currentOffset);
	            						code.add(x, ls);
	            						x++;
	            					}
	            				}
	        					//load
	            				if(offsets.get(current.reg3) != 0) {
	            					ls = load(k-2, offsets.get(current.reg3));
	            					code.add(x, ls);
	            					x++;
	            				}
	        					registers.array[k-2] = current.reg3;
	        					current.reg3 = k-1;
	            			}
            				else {
            					bool = true;
                				if(registers.array[k-1] != 0) {
	            					currentOffset = offsets.get(registers.array[k-1]);
	            					if (currentOffset == 0) {
	            						maxOffset-=4;
	            						ls = spill(k-1,maxOffset);
	            						offsets.set(registers.array[k-1], maxOffset);
	            						code.add(x, ls);
	            						x++;
	            					}
	            					else {
	            						ls = spill(k-1,currentOffset);
	            						code.add(x, ls);
	            						x++;
	            					}
                				}
            					//load
                				if(offsets.get(current.reg3) != 0) {
                					ls = load(k-1, offsets.get(current.reg3));
                					code.add(x, ls);
                					x++;
                				}
                				registers.array[k-1] = current.reg3;
            					current.reg3 = k;
        					}
            			}
            		}
            		//else nothing, nothing needed for output operation
        			code.set(x, current);
            	}
                for (int r = 0; r < offsets.size(); r++) {
                	//System.out.println(r + "\t" + offsets.get(r));
                }
            }
            
            else if(type == 'b'){	//TODO type b
            	//bottom-up
	            //start parsing file
	        
	        	String line;
	        	String token;
	        	Line current;
	        	
	            try {
					Scanner input = new Scanner(file);
		            while(input.hasNext()) {
		            	//for each line
		            	line = input.nextLine();
		            	//current.instruction
		            		//System.out.println(line);
		            	if(line.equals(""))
		            		continue;
		            	
		            	StringTokenizer a = new StringTokenizer(line, "\t ,=>");
		            	token = a.nextToken();   	
		            	if(token.equals("//"))
		            		continue;
		            	
		            	//store instruction
		            	current = new Line();
		            	current.instruction = token;
		            	switch (current.instruction) {
		            	case "loadI":	//only reg3
		            		token = a.nextToken();
		            		current.c = Integer.parseInt(token);
		            		token = a.nextToken();
		            		token = token.substring(1);
		            		current.reg3 = Integer.parseInt(token);
		            		code.add(current);
		            		break;
		            	case "add":	//all 3
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg1 = Integer.parseInt(token);
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg2 = Integer.parseInt(token);
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg3 = Integer.parseInt(token);
		            		code.add(current);
		            		break;
		            	case "sub":	//all 3
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg1 = Integer.parseInt(token);
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg2 = Integer.parseInt(token);
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg3 = Integer.parseInt(token);
		            		code.add(current);
		            		break;
		            	case "lshift":	//all 3
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg1 = Integer.parseInt(token);
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg2 = Integer.parseInt(token);
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg3 = Integer.parseInt(token);
		            		code.add(current);
		            		break;
		            	case "rshift":	//all 3
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg1 = Integer.parseInt(token);
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg2 = Integer.parseInt(token);
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg3 = Integer.parseInt(token);
		            		code.add(current);
		            		break;
		            	case "load":	//1 and 3
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg1 = Integer.parseInt(token);
		            		token = a.nextToken();
		            		token = token.substring(1);
		            		current.reg3 = Integer.parseInt(token);
		            		code.add(current);
		            		break;
		            	case "loadAI":		//1 and 3, 1 is always r0
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg1 = Integer.parseInt(token);
		            		token = a.nextToken();
		            		token = token.substring(1);
		            		current.reg3 = Integer.parseInt(token);
		            		code.add(current);
		            		break;
		            	case "store":	//1 and 3
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg1 = Integer.parseInt(token);
		            		token = a.nextToken();
		            		token = token.substring(1);
		            		current.reg3 = Integer.parseInt(token);
		            		code.add(current);
		            		break;
		            	case "storeAI":		//1 and 3, 3 is always r0
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg1 = Integer.parseInt(token);
		            		token = a.nextToken();
		            		token = token.substring(1);
		            		current.reg3 = Integer.parseInt(token);
		            		code.add(current);
		            		break;
		            	case "mult":	//all 3
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg1 = Integer.parseInt(token);
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg2 = Integer.parseInt(token);
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg3 = Integer.parseInt(token);
		            		code.add(current);
		            		break;
		            	case "div":	//all 3
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg1 = Integer.parseInt(token);
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg2 = Integer.parseInt(token);
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg3 = Integer.parseInt(token);
		            		code.add(current);
		            		break;
		            	case "output":
		            		token = a.nextToken();
		            		current.c = Integer.parseInt(token);
		            		code.add(current);
		            		break;
		            	default:
		            		System.out.println("Line didn't parse properly");
		            		return;
		            	} 	            	
		            }
		            input.close();
		            
		            ArrayList<Max> appearances = BuildAppearances(code);
		            
	            	Regs registers = new Regs(k);
	            	ArrayList<Integer> offsets = new ArrayList<Integer>();
	        		for (int o = 0; o < appearances.size(); o++) {
	        			offsets.add(0);
	        		}
	            	int maxOffset = 0;
	        		int currentOffset = 0;
	        		Line ls;
	        		int iterations = 0;
	        		boolean found = false;
	        		int toReplace;
	            	for (int x = 0; x < code.size(); x++){
	            		current = code.get(x);
	            		iterations++;
	            		System.out.println(iterations);
	            		/* For each line do the following:
	            		 * 
	            		 * for each register that isn't -1
	            		 * 		search array to see if it's there
	            		 * 			if it is, current.regx = array index
	            		 * 			else, find the one to replace, spill it (if necessary), load into that array index
	            		 */
	            		
	            		if(current.reg1 != -1 && current.reg1 != 0) {
	            			found = false;
	            			for (int i = 0; i < registers.array.length; i++) {
	            				if(registers.array[i] == current.reg1) {
	            					current.reg1 = i+1;
	            					found = true;
	            					break;
	            				}
	            			}
	            			if(!found) {
            					toReplace = findReplacement(registers.array, appearances, iterations);
            					//System.out.print(toReplace);
            					if(toReplace < 0) {
            						//no need to spill
            						toReplace = Math.abs(toReplace);
            						toReplace--;
            						
            						//load
            						if(offsets.get(current.reg1) != 0) {
	                					ls = load(toReplace, offsets.get(current.reg1));
	                					code.add(x, ls);
	                					x++;
	                				}
                					registers.array[toReplace] = current.reg1;
                					current.reg1 = toReplace+1;	
            					}
            					else if (toReplace > 0) {
            						toReplace--;
            						//spill
            						currentOffset = offsets.get(registers.array[toReplace]);
	            					if (currentOffset == 0) {
	            						maxOffset-=4;
	            						ls = spill(toReplace,maxOffset);
	            						offsets.set(registers.array[toReplace], maxOffset);
	            						code.add(x, ls);
	            						x++;
	            					}
	            					else {
	            						ls = spill(toReplace,currentOffset);
	            						code.add(x, ls);
	            						x++;
	            					}
	            					
	            					//load
            						if(offsets.get(current.reg1) != 0) {
	                					ls = load(toReplace, offsets.get(current.reg1));
	                					code.add(x, ls);
	                					x++;
	                				}
                					registers.array[toReplace] = current.reg1;
                					current.reg1 = toReplace+1;
            					}
	            			}
	            		}
	            		
	            		if(current.reg2 != -1 && current.reg2 != 0) {
	            			found = false;
	            			for (int i = 0; i < registers.array.length; i++) {
	            				if(registers.array[i] == current.reg2) {
	            					current.reg2 = i+1;
	            					found = true;
	            					break;
	            				}
	            			}
	            			if(!found) {
            					toReplace = findReplacement(registers.array, appearances, iterations);
            					//System.out.print(toReplace);
            					if(toReplace < 0) {
            						//no need to spill
            						toReplace = Math.abs(toReplace);
            						toReplace--;
            						
            						//load
            						if(offsets.get(current.reg2) != 0) {
	                					ls = load(toReplace, offsets.get(current.reg2));
	                					code.add(x, ls);
	                					x++;
	                				}
                					registers.array[toReplace] = current.reg2;
                					current.reg2 = toReplace+1;	
            					}
            					else if (toReplace > 0) {
            						toReplace--;
            						//spill
            						currentOffset = offsets.get(registers.array[toReplace]);
	            					if (currentOffset == 0) {
	            						maxOffset-=4;
	            						ls = spill(toReplace,maxOffset);
	            						offsets.set(registers.array[toReplace], maxOffset);
	            						code.add(x, ls);
	            						x++;
	            					}
	            					else {
	            						ls = spill(toReplace,currentOffset);
	            						code.add(x, ls);
	            						x++;
	            					}
	            					
	            					//load
            						if(offsets.get(current.reg2) != 0) {
	                					ls = load(toReplace, offsets.get(current.reg2));
	                					code.add(x, ls);
	                					x++;
	                				}
                					registers.array[toReplace] = current.reg2;
                					current.reg2 = toReplace+1;
            					}
	            			}
	            		}
	            		
	            		if(current.reg3 != -1 && current.reg3 != 0) {
	            			found = false;
	            			for (int i = 0; i < registers.array.length; i++) {
	            				if(registers.array[i] == current.reg3) {
	            					current.reg3 = i+1;
	            					found = true;
	            					break;
	            				}
	            			}
	            			if(!found) {
            					toReplace = findReplacement(registers.array, appearances, iterations);
            					//System.out.print(toReplace);
            					if(toReplace < 0) {
            						//no need to spill
            						toReplace = Math.abs(toReplace);
            						toReplace--;
            						
            						//load
            						if(offsets.get(current.reg3) != 0) {
	                					ls = load(toReplace, offsets.get(current.reg3));
	                					code.add(x, ls);
	                					x++;
	                				}
                					registers.array[toReplace] = current.reg3;
                					current.reg3 = toReplace+1;	
            					}
            					else if (toReplace > 0) {
            						toReplace--;
            						//spill
            						currentOffset = offsets.get(registers.array[toReplace]);
	            					if (currentOffset == 0) {
	            						maxOffset-=4;
	            						ls = spill(toReplace,maxOffset);
	            						offsets.set(registers.array[toReplace], maxOffset);
	            						code.add(x, ls);
	            						x++;
	            					}
	            					else {
	            						ls = spill(toReplace,currentOffset);
	            						code.add(x, ls);
	            						x++;
	            					}
	            					
	            					//load
            						if(offsets.get(current.reg3) != 0) {
	                					ls = load(toReplace, offsets.get(current.reg3));
	                					code.add(x, ls);
	                					x++;
	                				}
                					registers.array[toReplace] = current.reg3;
                					current.reg3 = toReplace+1;
            					}
            					else {
            						
            					}
	            			}
	            			if(current.reg1 != -1 && current.reg1 != 0) {
	        					appearances.get(registers.array[current.reg1-1]).numLive--;
	        					if(appearances.get(registers.array[current.reg1-1]).numLive>=0)
	        						appearances.get(registers.array[current.reg1-1]).liveRegisters.remove(0);
	            			}
	            			if(current.reg2 != -1 && current.reg2 != 0) {
	        					appearances.get(registers.array[current.reg2-1]).numLive--;
	        					if(appearances.get(registers.array[current.reg2-1]).numLive>=0)
	        						appearances.get(registers.array[current.reg2-1]).liveRegisters.remove(0);
	            			}
        					appearances.get(registers.array[current.reg3-1]).numLive--;
        					if(appearances.get(registers.array[current.reg3-1]).numLive>=0)
        						appearances.get(registers.array[current.reg3-1]).liveRegisters.remove(0);
	            		}
		            	code.set(x, current);
	            	}
		            
				} catch (FileNotFoundException e) {
					System.out.println("Can't find the file, please make sure it's in the current directory.");
	        		return;
				}
	
	            
            }
            else {	//TODO top-down
            	//top-down
	            //start parsing file
            	ArrayList<Reg> ranges = new ArrayList<Reg>();
	        	ArrayList<Integer> counts = new ArrayList<Integer>();
	        	ArrayList<Max> maxLive = new ArrayList<Max>();
	        	int x = 0;
	        	counts.add(x);        	
	        	ranges.add(new Reg());
	        	//at each index e, counts[e] = number of times re is seen in the iloc code
	        
	        	String line;
	        	String token;
	        	Line current;
	        	Reg reg;
	        	
	            try {
					Scanner input = new Scanner(file);
		            while(input.hasNext()) {
		            	//for each line
		            	line = input.nextLine();
		            	//current.instruction
		            		//System.out.println(line);
		            	if(line.equals(""))
		            		continue;
		            	
		            	StringTokenizer a = new StringTokenizer(line, "\t ,=>");
		            	token = a.nextToken();   	
		            	if(token.equals("//"))
		            		continue;
		            	
		            	//store instruction
		            	maxLive.add(new Max());
																				//System.out.println(maxLive.size());
		            	current = new Line();
		            	current.instruction = token;
		            	switch (current.instruction) {
		            	case "loadI":	//only reg3
		            		token = a.nextToken();
		            		current.c = Integer.parseInt(token);
		            		token = a.nextToken();
		            		token = token.substring(1);
		            		current.reg3 = Integer.parseInt(token);
		            		while(counts.size()<=current.reg3) {
		            			counts.add(x);
		            			ranges.add(new Reg());
		            		}
		            		reg = ranges.get(current.reg3);
		            		if(reg.liveStart == -1) {
		            			reg.liveStart = maxLive.size();
		            			reg.liveEnd = reg.liveStart;
		            		}
		            		else
		            			reg.liveEnd = maxLive.size();
		            		counts.set(current.reg3, counts.get(current.reg3)+1);
		            		code.add(current);
		            		break;
		            	case "add":	//all 3
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg1 = Integer.parseInt(token);
		            		while(counts.size()<=current.reg1) {
		            			counts.add(x);
		            			ranges.add(new Reg());
		            		}
		            		reg = ranges.get(current.reg1);
		            		if(reg.liveStart == -1) {
		            			reg.liveStart = maxLive.size();
		            			reg.liveEnd = reg.liveStart;
		            		}
		            		else
		            			reg.liveEnd = maxLive.size();
		            		counts.set(current.reg1, counts.get(current.reg1)+1);
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg2 = Integer.parseInt(token);
		            		while(counts.size()<=current.reg2) {
		            			counts.add(x);
		            			ranges.add(new Reg());
		            		}
		            		reg = ranges.get(current.reg2);
		            		if(reg.liveStart == -1) {
		            			reg.liveStart = maxLive.size();
		            			reg.liveEnd = reg.liveStart;
		            		}
		            		else
		            			reg.liveEnd = maxLive.size();
		            		counts.set(current.reg2, counts.get(current.reg2)+1);
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg3 = Integer.parseInt(token);
		            		while(counts.size()<=current.reg3) {
		            			counts.add(x);
		            			ranges.add(new Reg());
		            		}
		            		reg = ranges.get(current.reg3);
		            		if(reg.liveStart == -1) {
		            			reg.liveStart = maxLive.size();
		            			reg.liveEnd = reg.liveStart;
		            		}
		            		else
		            			reg.liveEnd = maxLive.size();
		            		counts.set(current.reg3, counts.get(current.reg3)+1);
		            		code.add(current);
		            		break;
		            	case "sub":	//all 3
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg1 = Integer.parseInt(token);
		            		while(counts.size()<=current.reg1) {
		            			counts.add(x);
		            			ranges.add(new Reg());
		            		}
		            		reg = ranges.get(current.reg2);
		            		if(reg.liveStart == -1) {
		            			reg.liveStart = maxLive.size();
		            			reg.liveEnd = reg.liveStart;
		            		}
		            		else
		            			reg.liveEnd = maxLive.size();
		            		counts.set(current.reg1, counts.get(current.reg1)+1);
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg2 = Integer.parseInt(token);
		            		while(counts.size()<=current.reg2) {
		            			counts.add(x);
		            			ranges.add(new Reg());
		            		}
		            		reg = ranges.get(current.reg2);
		            		if(reg.liveStart == -1) {
		            			reg.liveStart = maxLive.size();
		            			reg.liveEnd = reg.liveStart;
		            		}
		            		else
		            			reg.liveEnd = maxLive.size();
		            		counts.set(current.reg2, counts.get(current.reg2)+1);
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg3 = Integer.parseInt(token);
		            		while(counts.size()<=current.reg3) {
		            			counts.add(x);
		            			ranges.add(new Reg());
		            		}
		            		reg = ranges.get(current.reg3);
		            		if(reg.liveStart == -1) {
		            			reg.liveStart = maxLive.size();
		            			reg.liveEnd = reg.liveStart;
		            		}
		            		else
		            			reg.liveEnd = maxLive.size();
		            		counts.set(current.reg3, counts.get(current.reg3)+1);
		            		code.add(current);
		            		break;
		            	case "lshift":	//all 3
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg1 = Integer.parseInt(token);
		            		while(counts.size()<=current.reg1) {
		            			counts.add(x);
		            			ranges.add(new Reg());
		            		}
		            		reg = ranges.get(current.reg1);
		            		if(reg.liveStart == -1) {
		            			reg.liveStart = maxLive.size();
		            			reg.liveEnd = reg.liveStart;
		            		}
		            		else
		            			reg.liveEnd = maxLive.size();
		            		counts.set(current.reg1, counts.get(current.reg1)+1);
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg2 = Integer.parseInt(token);
		            		while(counts.size()<=current.reg2) {
		            			counts.add(x);
		            			ranges.add(new Reg());
		            		}
		            		reg = ranges.get(current.reg2);
		            		if(reg.liveStart == -1) {
		            			reg.liveStart = maxLive.size();
		            			reg.liveEnd = reg.liveStart;
		            		}
		            		else
		            			reg.liveEnd = maxLive.size();
		            		counts.set(current.reg2, counts.get(current.reg2)+1);
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg3 = Integer.parseInt(token);
		            		while(counts.size()<=current.reg3) {
		            			counts.add(x);
		            			ranges.add(new Reg());
		            		}
		            		reg = ranges.get(current.reg3);
		            		if(reg.liveStart == -1) {
		            			reg.liveStart = maxLive.size();
		            			reg.liveEnd = reg.liveStart;
		            		}
		            		else
		            			reg.liveEnd = maxLive.size();
		            		counts.set(current.reg3, counts.get(current.reg3)+1);
		            		code.add(current);
		            		break;
		            	case "rshift":	//all 3
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg1 = Integer.parseInt(token);
		            		while(counts.size()<=current.reg1) {
		            			counts.add(x);
		            			ranges.add(new Reg());
		            		}
		            		reg = ranges.get(current.reg2);
		            		if(reg.liveStart == -1) {
		            			reg.liveStart = maxLive.size();
		            			reg.liveEnd = reg.liveStart;
		            		}
		            		else
		            			reg.liveEnd = maxLive.size();
		            		counts.set(current.reg1, counts.get(current.reg1)+1);
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg2 = Integer.parseInt(token);
		            		while(counts.size()<=current.reg2) {
		            			counts.add(x);
		            			ranges.add(new Reg());
		            		}
		            		reg = ranges.get(current.reg2);
		            		if(reg.liveStart == -1) {
		            			reg.liveStart = maxLive.size();
		            			reg.liveEnd = reg.liveStart;
		            		}
		            		else
		            			reg.liveEnd = maxLive.size();
		            		counts.set(current.reg2, counts.get(current.reg2)+1);
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg3 = Integer.parseInt(token);
		            		while(counts.size()<=current.reg3) {
		            			counts.add(x);
		            			ranges.add(new Reg());
		            		}
		            		reg = ranges.get(current.reg3);
		            		if(reg.liveStart == -1) {
		            			reg.liveStart = maxLive.size();
		            			reg.liveEnd = reg.liveStart;
		            		}
		            		else
		            			reg.liveEnd = maxLive.size();
		            		counts.set(current.reg3, counts.get(current.reg3)+1);
		            		code.add(current);
		            		break;
		            	case "load":	//1 and 3
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg1 = Integer.parseInt(token);
		            		while(counts.size()<=current.reg1) {
		            			counts.add(x);
		            			ranges.add(new Reg());
		            		}
		            		reg = ranges.get(current.reg1);
		            		if(reg.liveStart == -1) {
		            			reg.liveStart = maxLive.size();
		            			reg.liveEnd = reg.liveStart;
		            		}
		            		else
		            			reg.liveEnd = maxLive.size();
		            		counts.set(current.reg1, counts.get(current.reg1)+1);
		            		token = a.nextToken();
		            		token = token.substring(1);
		            		current.reg3 = Integer.parseInt(token);
		            		while(counts.size()<=current.reg3) {
		            			counts.add(x);
		            			ranges.add(new Reg());
		            		}
		            		reg = ranges.get(current.reg3);
		            		if(reg.liveStart == -1) {
		            			reg.liveStart = maxLive.size();
		            			reg.liveEnd = reg.liveStart;
		            		}
		            		else
		            			reg.liveEnd = maxLive.size();
		            		counts.set(current.reg3, counts.get(current.reg3)+1);
		            		code.add(current);
		            		break;
		            	case "loadAI":		//1 and 3, 1 is always r0
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg1 = Integer.parseInt(token);
		            		while(counts.size()<=current.reg1) {
		            			counts.add(x);
		            			ranges.add(new Reg());
		            		}
		            		reg = ranges.get(current.reg1);
		            		if(reg.liveStart == -1) {
		            			reg.liveStart = maxLive.size();
		            			reg.liveEnd = reg.liveStart;
		            		}
		            		else
		            			reg.liveEnd = maxLive.size();
		            		counts.set(current.reg1, counts.get(current.reg1)+1);
		            		token = a.nextToken();
		            		token = token.substring(1);
		            		current.reg3 = Integer.parseInt(token);
		            		while(counts.size()<=current.reg3) {
		            			counts.add(x);
		            			ranges.add(new Reg());
		            		}
		            		reg = ranges.get(current.reg3);
		            		if(reg.liveStart == -1) {
		            			reg.liveStart = maxLive.size();
		            			reg.liveEnd = reg.liveStart;
		            		}
		            		else
		            			reg.liveEnd = maxLive.size();
		            		counts.set(current.reg3, counts.get(current.reg3)+1);
		            		code.add(current);
		            		break;
		            	case "store":	//1 and 3
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg1 = Integer.parseInt(token);
		            		while(counts.size()<=current.reg1) {
		            			counts.add(x);
		            			ranges.add(new Reg());
		            		}
		            		reg = ranges.get(current.reg1);
		            		if(reg.liveStart == -1) {
		            			reg.liveStart = maxLive.size();
		            			reg.liveEnd = reg.liveStart;
		            		}
		            		else
		            			reg.liveEnd = maxLive.size();
		            		counts.set(current.reg1, counts.get(current.reg1)+1);
		            		token = a.nextToken();
		            		token = token.substring(1);
		            		current.reg3 = Integer.parseInt(token);
		            		while(counts.size()<=current.reg3) {
		            			counts.add(x);
		            			ranges.add(new Reg());
		            		}
		            		reg = ranges.get(current.reg3);
		            		if(reg.liveStart == -1) {
		            			reg.liveStart = maxLive.size();
		            			reg.liveEnd = reg.liveStart;
		            		}
		            		else
		            			reg.liveEnd = maxLive.size();
		            		counts.set(current.reg3, counts.get(current.reg3)+1);
		            		code.add(current);
		            		break;
		            	case "storeAI":		//1 and 3, 3 is always r0
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg1 = Integer.parseInt(token);
		            		while(counts.size()<=current.reg1) {
		            			counts.add(x);
		            			ranges.add(new Reg());
		            		}
		            		reg = ranges.get(current.reg1);
		            		if(reg.liveStart == -1) {
		            			reg.liveStart = maxLive.size();
		            			reg.liveEnd = reg.liveStart;
		            		}
		            		else
		            			reg.liveEnd = maxLive.size();
		            		counts.set(current.reg1, counts.get(current.reg1)+1);
		            		token = a.nextToken();
		            		token = token.substring(1);
		            		current.reg3 = Integer.parseInt(token);
		            		while(counts.size()<=current.reg3) {
		            			counts.add(x);
		            			ranges.add(new Reg());
		            		}
		            		reg = ranges.get(current.reg3);
		            		if(reg.liveStart == -1) {
		            			reg.liveStart = maxLive.size();
		            			reg.liveEnd = reg.liveStart;
		            		}
		            		else
		            			reg.liveEnd = maxLive.size();
		            		counts.set(current.reg3, counts.get(current.reg3)+1);
		            		code.add(current);
		            		break;
		            	case "mult":	//all 3
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg1 = Integer.parseInt(token);
		            		while(counts.size()<=current.reg1) {
		            			counts.add(x);
		            			ranges.add(new Reg());
		            		}
		            		reg = ranges.get(current.reg1);
		            		if(reg.liveStart == -1) {
		            			reg.liveStart = maxLive.size();
		            			reg.liveEnd = reg.liveStart;
		            		}
		            		else
		            			reg.liveEnd = maxLive.size();
		            		counts.set(current.reg1, counts.get(current.reg1)+1);
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg2 = Integer.parseInt(token);
		            		while(counts.size()<=current.reg2) {
		            			counts.add(x);
		            			ranges.add(new Reg());
		            		}
		            		reg = ranges.get(current.reg2);
		            		if(reg.liveStart == -1) {
		            			reg.liveStart = maxLive.size();
		            			reg.liveEnd = reg.liveStart;
		            		}
		            		else
		            			reg.liveEnd = maxLive.size();
		            		counts.set(current.reg2, counts.get(current.reg2)+1);
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg3 = Integer.parseInt(token);
		            		while(counts.size()<=current.reg3) {
		            			counts.add(x);
		            			ranges.add(new Reg());
		            		}
		            		reg = ranges.get(current.reg3);
		            		if(reg.liveStart == -1) {
		            			reg.liveStart = maxLive.size();
		            			reg.liveEnd = reg.liveStart;
		            		}
		            		else
		            			reg.liveEnd = maxLive.size();
		            		counts.set(current.reg3, counts.get(current.reg3)+1);
		            		code.add(current);
		            		break;
		            	case "div":	//all 3
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg1 = Integer.parseInt(token);
		            		while(counts.size()<=current.reg1) {
		            			counts.add(x);
		            			ranges.add(new Reg());
		            		}
		            		reg = ranges.get(current.reg2);
		            		if(reg.liveStart == -1) {
		            			reg.liveStart = maxLive.size();
		            			reg.liveEnd = reg.liveStart;
		            		}
		            		else
		            			reg.liveEnd = maxLive.size();
		            		counts.set(current.reg1, counts.get(current.reg1)+1);
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg2 = Integer.parseInt(token);
		            		while(counts.size()<=current.reg2) {
		            			counts.add(x);
		            			ranges.add(new Reg());
		            		}
		            		reg = ranges.get(current.reg2);
		            		if(reg.liveStart == -1) {
		            			reg.liveStart = maxLive.size();
		            			reg.liveEnd = reg.liveStart;
		            		}
		            		else
		            			reg.liveEnd = maxLive.size();
		            		counts.set(current.reg2, counts.get(current.reg2)+1);
		            		token=a.nextToken();
		            		token = token.substring(1);
		            		current.reg3 = Integer.parseInt(token);
		            		while(counts.size()<=current.reg3) {
		            			counts.add(x);
		            			ranges.add(new Reg());
		            		}
		            		reg = ranges.get(current.reg3);
		            		if(reg.liveStart == -1) {
		            			reg.liveStart = maxLive.size();
		            			reg.liveEnd = reg.liveStart;
		            		}
		            		else
		            			reg.liveEnd = maxLive.size();
		            		counts.set(current.reg3, counts.get(current.reg3)+1);
		            		code.add(current);
		            		break;
		            	case "output":
		            		token = a.nextToken();
		            		current.c = Integer.parseInt(token);
		            		code.add(current);
		            		break;
		            	default:
		            		System.out.println("Line didn't parse properly");
		            		return;
		            	} 	            	
		            }
		            input.close();
		            
		            maxLive = CalcMaxLive(ranges, maxLive);
		            
		            int maximum = 0;
		            
		            for (Max theOne : maxLive) {
		            	if(theOne.numLive > maximum)
		            		maximum = theOne.numLive;
		            }
		            //maximum == real MAX_LIVE
		            
		            if(maximum > k-2) {
		            	/*	rest of code will go in here.
		            	 * 	for each line in maxLive
		            	 * if Max.numLive > k-2, spill the one with fewest uses, if tied, spill the one with the longest live range
		            	 * when spilling, set spill = true, line after assignment it must spill
		            	 * 
		            	 * then, once spilled variables are known, add those lines to the code (they get permanently moved to k-2 or k-1, get stored/loaded every use)
		            	 * then, assign registers to physical registers, each time a new one is introduced, overwrite the one that just left its live range
		            	 */
		            	int toSpill;
		            	
		            	while(maximum > k-2) {
		            		for(Max a : maxLive) {
		            			if(a.numLive > k-2) {
		            				toSpill = a.liveRegisters.get(0);
		            				for(int z = 1; z<a.numLive; z++) {
		            					if(counts.get(a.liveRegisters.get(z)) < counts.get(toSpill))
		            						toSpill = a.liveRegisters.get(z);
		            					else if(counts.get(a.liveRegisters.get(z)) == counts.get(toSpill)) {
		            						if((ranges.get(a.liveRegisters.get(z)).liveEnd-ranges.get(a.liveRegisters.get(z)).liveStart) > (ranges.get(toSpill).liveEnd-ranges.get(toSpill).liveStart))
		            							toSpill = a.liveRegisters.get(z);
		            					}
		            				}
		            				//toSpill = register number to spill
//																													System.out.println(toSpill + "\n-");
		            				ranges.get(toSpill).spill = true;
		            				break;
		            			}
		            		}
//																										            for(Max index : maxLive) {
//																										            	System.out.println(index.liveRegisters.toString());
//																										            }
//																										            System.out.println("-");
		            		maxLive = CalcMaxLive(ranges,maxLive);
		            		maximum = 0;
		            		for (Max theOne : maxLive) {
				            	if(theOne.numLive > maximum)
				            		maximum = theOne.numLive;
				            }
		            	}
//																										            for(Max index : maxLive) {
//																										            	System.out.println(index.liveRegisters.toString());
//																										            }
//																										            System.out.println("-");
		            	
		            	//now build code, if a spill, always save/load when it's used
		            	Regs registers = new Regs(k);
		            	ArrayList<Integer> offsets = new ArrayList<Integer>();
		        		for (int o = 0; o < counts.size(); o++) {
		        			offsets.add(0);
		        		}
		            	int maxOffset = 0;
		        		int currentOffset = 0;
		        		Line ls;
		        		int iterations = 0;
		            	for (x = 0; x < code.size(); x++){
		            		current = code.get(x);
		            		iterations++;
		            		/*
		            		 * for each line do the following:
		            		 * if reg3 is spilled, add a line to storeAI after it's assigned
		            		 * if reg1 AND reg2 are spilled, add a line to loadAI before it's used for each (reg1 -> k-2, reg2 -> k-1)
		            		 * if reg1 EXOR reg2 are spilled, add a line to loadAI before it's used (into k-2)
		            		 * for all non-spilled, find a physical register (0 through k-2). If already has one, leave it. Otherwise, overwrite the one outside of its live range (bc it's ded)
		            		 */
		            		
		            		

		            		if(current.reg1 != -1 && current.reg1 != 0) {
		            			if(ranges.get(current.reg1).spill) {
		            				//load
		            				if(offsets.get(current.reg1) != 0) {
	                					ls = load(k-2, offsets.get(current.reg1));
	                					code.add(x, ls);
	                					x++;
	                				}
	            					registers.array[k-2] = current.reg1;
	            					current.reg1 = k-1;
		            			}
		            			else {
		            				//find and set to the proper register
			            			for (int r = 0; r < k-2; r++) {
			            				if(current.reg1 == registers.array[r]) {
			            					current.reg1 = r+1;
			            					break;
			            				}
			            			}
		            			}
		            		}
		            		if(current.reg2 != -1 && current.reg2 != 0) {
	            				if(ranges.get(current.reg2).spill) {
	            					if(offsets.get(current.reg2) != 0) {
	                					ls = load(k-1, offsets.get(current.reg2));
	                					code.add(x, ls);
	                					x++;
	                				}
	            					registers.array[k-1] = current.reg2;
	            					current.reg2 = k;
		            			}
		            			else {
		            				//find and set to the proper register
			            			for (int r = 0; r < k-2; r++) {
			            				if(current.reg2 == registers.array[r]) {
			            					current.reg2 = r+1;
			            					break;
			            				}
			            			}
		            			}
		            		}
 		            		if(current.reg3!=-1 && current.reg3 != 0) {
			            		if(ranges.get(current.reg3).spill) {
		            				//load
		            				if(offsets.get(current.reg3) != 0) {
	                					ls = load(k-1, offsets.get(current.reg3));
	                					code.add(x, ls);
	                					x++;
	                				}	            					
			            			registers.array[k-1] = current.reg3;
			            			current.reg3 = k;
			            			currentOffset = offsets.get(registers.array[k-1]);
			    					if (currentOffset == 0) {
			    						maxOffset-=4;
			    						ls = spill(k-1,maxOffset);
			    						offsets.set(registers.array[k-1], maxOffset);
			    						code.add(x+1, ls);
			    						x++;
			    					}
			    					else {
			    						ls = spill(k-1,currentOffset);
			    						code.add(x+1, ls);
			    						x++;
			    					}
			            		}
			            		else {
			            			//search registers
			            			boolean found = false;
			            			for (int r = 0; r < k-2; r++) {
			            				if(current.reg3 == registers.array[r]) {
			            					found = true;
			            					current.reg3 = r+1;
			            					break;
			            				}
			            			}
			            			if(!found) {
//			            				if(current.instruction.equals("load")) {
//			            					for(int r = 0; r < k-2; r++) {
//				            					if(iterations >= ranges.get(registers.array[r]).liveEnd && current.reg1 != r+1) {
//				            						registers.array[r] = current.reg3;
//				            						current.reg3 = r+1;
//				            						break;
//				            					}
//			            				}
//			            				}
//			            				else {
			            					for(int r = 0; r < k-2; r++) {
			            					if(iterations >= ranges.get(registers.array[r]).liveEnd) {
			            						registers.array[r] = current.reg3;
			            						current.reg3 = r+1;
			            						break;
			            					}
			            				}
			            			}
			            		}
		            		}
		            	}
		            }
		            
				} catch (FileNotFoundException e) {
					System.out.println("Can't find the file, please make sure it's in the current directory.");
	        		return;
				}
	
	            
            }
			
            printCode(code);
        }
    }
}


