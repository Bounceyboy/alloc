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
	
    public static void printCode(ArrayList<Line> code) {
    	Line current;
    	for(int x = 0; x<code.size(); x++) {
    		current = code.get(x);
    		System.out.print(current.instruction + "\t");
    		switch (current.instruction) {
    		case "loadI":
    			System.out.print(current.c + "\t=> r" + current.reg1 + "\n");
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
        		System.out.print("r" + current.reg1 + "\t=> r" + current.reg2 + "\n");
        		break;
        	case "loadAI":
        		System.out.print("r" + current.reg1 + ", " + current.c + "\t=> r" + current.reg2 + "\n");
        		break;
        	case "store":
        		System.out.print("r" + current.reg1 + "\t=> r" + current.reg2 + "\n");
        		break;
        	case "storeAI":
        		System.out.print("r" + current.reg1 + ", " + current.c + "\t=> r" + current.reg2 + "\n");
        		break;
        	case "mult":
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
	            	case "loadI":
	            		token=a.nextToken();
	            		current.c = Integer.parseInt(token);
	            		token = a.nextToken();
	            		token = token.substring(1);
	            		current.reg1 = Integer.parseInt(token);
	            		while(counts.size()<=current.reg1) {
	            			counts.add(x);
	            		}
	            		counts.set(current.reg1, counts.get(current.reg1)+1);
	            		code.add(current);
	            		break;
	            	case "add":
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
	            	case "sub":
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
	            	case "lshift":
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
	            	case "rshift":
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
	            	case "load":
	            		token=a.nextToken();
	            		token = token.substring(1);
	            		current.reg1 = Integer.parseInt(token);
	            		while(counts.size()<=current.reg1) {
	            			counts.add(x);
	            		}
	            		counts.set(current.reg1, counts.get(current.reg1)+1);
	            		token = a.nextToken();
	            		token = token.substring(1);
	            		current.reg2 = Integer.parseInt(token);
	            		while(counts.size()<=current.reg2) {
	            			counts.add(x);
	            		}
	            		counts.set(current.reg2, counts.get(current.reg2)+1);
	            		code.add(current);
	            		break;
	            	case "loadAI":
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
	            		current.reg2 = Integer.parseInt(token);
	            		while(counts.size()<=current.reg2) {
	            			counts.add(x);
	            		}
	            		counts.set(current.reg2, counts.get(current.reg2)+1);
	            		code.add(current);
	            		break;
	            	case "store":
	            		token=a.nextToken();
	            		token = token.substring(1);
	            		current.reg1 = Integer.parseInt(token);
	            		while(counts.size()<=current.reg1) {
	            			counts.add(x);
	            		}
	            		counts.set(current.reg1, counts.get(current.reg1)+1);
	            		token = a.nextToken();
	            		token = token.substring(1);
	            		current.reg2 = Integer.parseInt(token);
	            		while(counts.size()<=current.reg2) {
	            			counts.add(x);
	            		}
	            		counts.set(current.reg2, counts.get(current.reg2)+1);
	            		code.add(current);
	            		break;
	            	case "storeAI":
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
	            		current.reg2 = Integer.parseInt(token);
	            		while(counts.size()<=current.reg2) {
	            			counts.add(x);
	            		}
	            		counts.set(current.reg2, counts.get(current.reg2)+1);
	            		code.add(current);
	            		break;
	            	case "mult":
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
			} catch (FileNotFoundException e) {
				System.out.println("Can't find the file, please make sure it's in the current directory.");
        		return;
			}
            printCode(code);
//             use this to test counts
//             for (int z=0; z<counts.size(); z++) {
//            	System.out.println(z + "\t" + counts.get(z));
//            }
        }
    }
}


