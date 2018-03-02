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
		int maxOffset;

		Regs(int k){
			this.array = new int[k];
			this.maxOffset = 0;
		}
	}
	
	//takes register number and regs object and creates the spill Line to return, still must be added to code object
	public static Line spill(int reg, Regs regs){
		Line current = new Line();
		current.instruction = "storeAI";
		current.c = regs.maxOffset;
		current.reg1 = reg;
		current.reg3 = 0;
		regs.maxOffset = regs.maxOffset + 4;
		return current;
	}
	
	//takes int for register to load into, int for offset to load from, and creates a line to load the proper register's info from memory into the regs object
	//line still needs to be added to code object
	public static Line load(int reg, int offset) {
		Line current = new Line();
		current.instruction = "loadAI";
		current.reg1 = 0;
		current.c = offset;
		current.reg1 = reg;
		return current;
		
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
        	//inputs stored -> type, k, and file/filename
        	
            //start parsing file
        	ArrayList<Line> code = new ArrayList<Line>();
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
	            	
	            	input.close();
	            	
	            }
			} catch (FileNotFoundException e) {
				System.out.println("Can't find the file, please make sure it's in the current directory.");
        		return;
			}

            Regs registers = new Regs(k);
            //TODO handling for when max_live < k-2
            
            if(type == 's'){
            	//shrek
            	int max = 0;
            	int reg = -1;
            	for (x = 0; x<k-2; x++){
            		for (int q = 1; q<counts.size(); q++){
            			if (counts.get(q)>max){
            				max = counts.get(q);
            				reg = q;
            			}
            		}
            		counts.set(reg, 0);
            		registers.array[x] = reg;
            	}
            	boolean changed1, changed2, changed3;
            	boolean bool = false;
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
            			//3 register op (don't forget about r0
            			//shouldn't ever need this but can't hurt to ensure we leave weird instances of r0 alone
            			if(current.reg1 == 0)
            				changed1 = true;
            			if(current.reg2 == 0)
            				changed2 = true;
            			if(current.reg3 == 0)
            				changed3 = true;
            			//everything else needs feasible registers, spill and load instructions work
            			
            		}
            		else if(current.instruction.equals("load") || current.instruction.equals("store") || current.instruction.equals("loadAI") || current.instruction.equals("storeAI")){
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
            			//2 register op (don't forget about r0
            		}
            		else if(current.instruction.equals("loadI")){
            			changed1 = false;
            			for (int q = 0; q<k-2; q++){
	            			if (registers.array[q] == current.reg1 && changed1 == false){
	            				current.reg1 = q+1;
	            				changed1 = true;
	            			}
            			}
            			//1 register op (don't forget about r0(?)
            		}
            		//else nothing, nothing needed for output operation
            	}
            }
            else if(type == 'b'){
            	//bottom-up
            }
            else {
            	//top-down ugh
            }
			
            printCode(code);
        }
    }
}


