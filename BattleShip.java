import java.io.BufferedReader;
import java.io.FileReader;
import java.util.BitSet;
import java.util.Random;
import java.util.ArrayList;

/**
 *  Heuristic Algorithms
 *  BattleShip.java
 *  Implements a heuristic algorithm: Simulated Annealing to try and solve a Battleship Solitaire puzzle. 
 *	Accepts the name of a text file as a commandline argument. Within that file are three lines of integers, 
 * 	K integers specifying lengths of K ships, N integers specifying the number of ship-occupied cells in each 
 *	column,	and the number of ship-occupied cells in each row. 
 *	Also accepts an integer as a second commandline argument specifying the maximum number of potential solutions 
 *	to the puzzle that your program is allowed to evaluate before it must stop and produce its best result.
 *
 *  Authors: Sacha Raman and Elizabeth Macken
 *  
 **/
 

 public class BattleShip {
	
	//what ship is currently being placed
	private static int currShip = 0;
	//the size of the ship to be placed
	private static int shipSize;
	//how many ships have been placed
	private static int shipsPlacedCount = 0;
	//what is the current column or row position
	private static int colPos = 0;
	private static int rowPos = 0;
	//is the ship to be placed vertically or horizontally
	private static boolean vertical;
	//the start and end position of the ship to be placed
	private static int startPos = 0;
	private static int endPos = 0;
	//has the current ship been placed
	private static boolean shipPlaced = false;
	//does the perimiter of the ship to be placed, exceed any boundaries
	private static boolean above, below, rightBounds = false, leftBounds = false;
	//what is the start and end corners of the perimeter and the width of ship + perimeter area
	private static int loopStart, loopEnd, width;
	//flag to see if ship placement is invalid
	private static boolean posInvalid = false;
	private static boolean moveValid;
	//three int arrays to store the ships, xHits and yHits
	private static int[] ships;
	private static int[] xHits;
	private static int[] yHits;
	//the length of the board
	private static int N;
	//the best solution board
	private static BitSet bestBoard;
	//the current board
	//private static BitSet currBoard;
	//list of ships on the board
	private static ArrayList<Ship> shipsOnBoard = new ArrayList<Ship>();
	//flag for whether initial solution or not
	private static boolean initial = true;
	//flag if the solution is identical to our original solution
	private static boolean identicalSolution = false; 
	//ship counter for simulated annealing 
	private static int shipCounter, simACounter;
	//ignore the perimeters flag
	private static boolean ignorePerimeters = false;

	// -------------------------------------------------------------------------------------------------------------------
	
	// Prints the board pretty to the console												
	public static void printBoard() {
		System.out.println("Best Solution: ");
		System.out.print("   ");
		//print out the Xhits 
		for (int i = 0; i < xHits.length; i++) {
			System.out.print(xHits[i] + " ");
		}
		//print out the yHits and each sqaure of the game
		for(int i = 0; i < (N * N); i++) {
				if((i % N) == 0) {
					System.out.println();
					System.out.print(" ");
					System.out.print(yHits[i / N]);
					System.out.print(" ");
				}
				if(bestBoard.get(i) == true) {
					System.out.print("X ");
				}
				else {
					System.out.print("~ ");
				}
		}
		System.out.println();	
	}
	
	// -------------------------------------------------------------------------------------------------------------------
	
	// Reads in the data from the textfile to create the board, creates an initial solution 
	// and then does simulated annealing
	public static void main(String[] args) {
		
		//check we have been passed the correct number of args
		if(args.length != 2) {
			System.err.println("Usage: java BattleShip <filename> <max potential solutions>");
			return;
		}
		try {
			//declare variables
			String filename = args[0];
			int maxSolutions = Integer.parseInt(args[1]);
			BufferedReader br = new BufferedReader(new FileReader(filename));

			//read in the first line, the ships
			String s = br.readLine();
			if(s == null) {
				System.err.println("No ships in input file");
				System.exit(1);
			}
			String[] line1 = s.split(" ");
			ships = new int[line1.length];
			for(int i = 0; i < line1.length; i++) { 
				ships[i] = Integer.parseInt(line1[i]);
			}
			//read in the second line, the xHits
			s = br.readLine();
			if(s == null) {
				System.err.println("No xHits in input file");
				System.exit(1);
			}
			String[] line2 = s.split(" ");
			xHits = new int[line2.length];
			for(int i = 0; i < line2.length; i++) { 
				xHits[i] = Integer.parseInt(line2[i]);
			}
			//read in the third line, the yHits
			s = br.readLine();
			if(s == null) {
				System.err.println("No yHits in input file");
				System.exit(1);
			}
			String[] line3 = s.split(" ");
			yHits = new int[line3.length];
			for(int i = 0; i < line3.length; i++) { 
				yHits[i] = Integer.parseInt(line3[i]);
			}
			//check we have a NxN grid
			if(xHits.length != yHits.length) {
				System.err.println("Grid is not NxN");
				System.exit(1);
			}
			//close the BufferedReader
			br.close();
			
			//generate the first solution
			initialSolution();			
			//set initial flag to false as we have made the initial solution
			initial = false;
			
			/*
			//ONLY HILLCLIMBING
			int solutionCounter = 0;
			//call this method in a for loop from 0 to < maxSolutions
			while(solutionCounter < maxSolutions) {
				System.err.println("hill climbing");
				hillClimbing();
				if(identicalSolution == false) {
					solutionCounter++;
				}
			}
			*/
		
		
			//set the ship counter to the number of ships we have
			shipCounter = ships.length; 
			//if we have <= 20 solutions do Simulated Annealing the whole time
			if (maxSolutions <= 20) {
				simACounter = maxSolutions;
			}
			//otherwise do it three quarters of the time
			else {
				simACounter = maxSolutions * 3 / 4;
			} 
			//set the rate of cooling
			int interval = simACounter / shipCounter;
			int intervalCounter = 0;
			//Simulated Annealing
			int solutionCounter = 0;
			//call this method in a for loop from 0 to < maxSolutions
			while(solutionCounter < maxSolutions) {
				if (simACounter > 1) {
					simulatedAnnealing(); 
					intervalCounter++;
					if(intervalCounter == interval) {
						intervalCounter = 0;
						//lower the temperature
						shipCounter--;
					}
				}
				else {
					hillClimbing();
				}
				//if the board remains unchanged (i.e. no ships have moved) do not count as a solution
				if(identicalSolution == false) {
					solutionCounter++;
				}
			}
			
			//print the best solution followed by the score
			printBoard();
			System.out.println("SCORE: " +  calculateFitness(bestBoard));
			

		}
		catch (Exception ex) {
			System.err.println("Exception " + ex);
			ex.printStackTrace();
		}
	}
	
	// -------------------------------------------------------------------------------------------------------------------
	
	// Attempt to improve the score of the board through Simulated Annealing
	public static void simulatedAnnealing() {
		
		//get a copy of our bestBoard into currBoard
		BitSet currBoard = bestBoard.get(0, N*N);
		//a list for the ships we will be moving
		ArrayList<Integer> movedShips = new ArrayList<Integer>();
		//random number generator
		Random rand = new Random();
		//counter of how many ships added to the list
		int addedToList = 0;
		//while we haven't added the number of ships we are after to the list
		while(addedToList < shipCounter) {
			//generate a random number
			int randomShip = rand.nextInt(ships.length);
			//if its not in the list, add it
			if (!movedShips.contains(randomShip)) {
				movedShips.add(randomShip);
				addedToList++;
			}
		}
		//create a copy of shipsOnBoard
		ArrayList<Ship> currShips = shipsOnBoard;
		//create a new list to store ships to be moved in order
		ArrayList<Integer> orderedMovedShips = new ArrayList<Integer>();
		//order the ship pointers in movedShips from smallest to largest
		//this means the ships are ordered from largest to smallest - have to place largest first
		for(int i = 0; i < movedShips.size(); i++) {
			//if the list is empty just add the ship pointer to the list
			if(orderedMovedShips.isEmpty()) {
				orderedMovedShips.add(movedShips.get(i));
			}
			else {
				//otherwise go through the ships in our new list
				for(int j = 0; j < orderedMovedShips.size(); j++) {
					//if the ship pointer we are adding is smaller, then add it before
					if(movedShips.get(i) < orderedMovedShips.get(j)) {
						orderedMovedShips.add(j, movedShips.get(i));
						break;
					}
				}
			}
		}
		//loop through all ships in orderedMovedShips
		for(int j = 0; j < orderedMovedShips.size(); j++) {
			//get each ship
			Ship ship = currShips.get(orderedMovedShips.get(j));
			//get the start, end, and direction of this ship
			startPos = ship.startPos_;
			endPos = ship.endPos_;
			vertical = ship.vertical_;
			//erase this ship from the current board
			int bitCounter = 0;
			if(vertical == true) {
				for(int i = startPos; i <= endPos; i+=N) {
					currBoard.clear(i);
					bitCounter++;
				}
			}
			else {
				for(int i = startPos; i <= endPos; i++) {
					currBoard.clear(i);
					bitCounter++;
				}
			}			
		}
		//now that we have removed all those ships, we can add them back on
		for(int i = 0; i < orderedMovedShips.size(); i++) {
			//get each ship
			Ship ship = currShips.get(orderedMovedShips.get(i));
			//get the direction and size of this ship
			vertical = ship.vertical_;
			shipSize = ship.size_;
			int initialStart = ship.startPos_;
			int initialEnd = ship.endPos_;
			//find a position to place the ship
			initialShipPlacement();			
			//loop until ship is moved
			shipPlaced = false;
			while(shipPlaced != true) {				
				//check if this position is valid
				moveValid = isValid(currBoard);
				if(moveValid == true) {
					//place the ship down
					currBoard = placeShip(currBoard);
					shipPlaced = true;
					ignorePerimeters = false;					
					//update our ship positions and direction
					ship.startPos_ = startPos;
					ship.endPos_ = endPos;
					ship.vertical_ = vertical;
					//set the ship
					currShips.set(orderedMovedShips.get(i), ship);
				}
				else {
					//get the next possible place we can place the ship
					nextPossibleShipPos(vertical);
					if(initialStart == startPos && initialEnd == endPos) {
						ignorePerimeters = true;
					}
				}
			}
		}
		
		//check if the solution is identical - doesn't count as a solution
		identicalSolution = currBoard.equals(bestBoard);
		//calculate the fitness of the currBoard and the bestBoard
		int currBoardFitness = calculateFitness(currBoard);
		int bestBoardFitness = calculateFitness(bestBoard);
		//if the currBoard is better than or as good as bestBoard, then bestBoard = currBoard
		if(currBoardFitness <= bestBoardFitness) {			
			bestBoard = currBoard;					
			//update our ship list
			shipsOnBoard = currShips;
		}				
		simACounter--;
	}
	
	// -------------------------------------------------------------------------------------------------------------------
	
	// Attempt to improve the score of the board through Hill CLimbing
	public static void hillClimbing() {
		
		//get a copy of our bestBoard into currBoard
		BitSet currBoard = bestBoard.get(0, N*N);
		//pick a random ship to move
		Random rand = new Random();
		int randomShip = rand.nextInt(shipsOnBoard.size());
		Ship ship = shipsOnBoard.get(randomShip);
		//get the start, end, size, and direction of this ship
		startPos = ship.startPos_;
		endPos = ship.endPos_;
		vertical = ship.vertical_;
		shipSize = ship.size_;
		int initialStart = startPos;
		int initialEnd = endPos;
		//erase this ship from the current board
		if(vertical == true) {
			for(int i = startPos; i <= endPos; i+=N) {
				currBoard.clear(i);
			}
		}
		else {
			for(int i = startPos; i <= endPos; i++) {
				currBoard.clear(i);
			}
		}
		//loop until ship is moved
		shipPlaced = false;
		while(shipPlaced != true) {
			//get the next possible place we can place the ship
			nextPossibleShipPos(vertical);
			if(initialStart == startPos && initialEnd == endPos) {
				ignorePerimeters = true;
			}
			//check if this position is valid
			moveValid = isValid(currBoard);
			if(moveValid == true) {
				//place the ship down
				currBoard = placeShip(currBoard);
				shipPlaced = true;
				ignorePerimeters = false;
				//check if the solution is identical - doesn't count as a solution
				identicalSolution = currBoard.equals(bestBoard);
				//calculate the fitness of the currBoard and the bestBoard
				int currBoardFitness = calculateFitness(currBoard);
				int bestBoardFitness = calculateFitness(bestBoard);
				//if the currBoard is better than or as good as bestBoard, then bestBoard = currBoard
				if(currBoardFitness <= bestBoardFitness) {				
					bestBoard = currBoard;				
					//update our ship positions and direction
					ship.startPos_ = startPos;
					ship.endPos_ = endPos;
					ship.vertical_ = vertical;
				}				
			}
		}		
	}
	
	// -------------------------------------------------------------------------------------------------------------------
	
	//find a place to initially place a ship
	public static void initialShipPlacement() {	
	
		//decide where to attempt to stick the ship
		//if we are placing vertically
		if(vertical == true) {
			//go through all xHit values to find one that is equal to or greater than shipSize
			for(int i = 0; i < xHits.length; i++) {
				if(xHits[i] >= shipSize) {
					colPos = i;
					break;
				}
			}
		}
		//if we are placing the ship horizontally
		else {
			//go through all the yHit values to find one that is equal to or greater than shipSize
			for(int i = 0; i < yHits.length; i++) {
				if(yHits[i] >= shipSize) {
					rowPos = i;
					break;
				}
			}
		}
		//Specify the start and end position of ship to place
		//if ship is to be placed vertically
		if(vertical == true) {
			startPos = colPos;
			endPos = startPos + ((shipSize - 1) * N);
		}
		//otherwise if the ship is to be horizontal
		else {
			startPos = rowPos * N;
			endPos = startPos + shipSize - 1;
		}
	}
	
	// -------------------------------------------------------------------------------------------------------------------
	
	//Generates a initial solution
	public static void initialSolution() {
		
		//order the ships in the ships array by size using bubble sort
		int length = ships.length;
		int tempShip = 0;
		for(int i = 0; i < length; i++) {
			for(int j = 1; j < (length - i); j++) {
				if(ships[j-1] < ships[j]) {
					tempShip = ships[j - 1];
					ships[j-1] = ships[j];
					ships[j] = tempShip;
				}
			}
		}
				
		//generate the board
		N = xHits.length;
		int boardSize = N * N;
		bestBoard = new BitSet(boardSize);
		
		//set the direction to place the first ship (we will simply be alternating between the two)
		vertical = true;
		
		//while there are still ships to place
		while(shipsPlacedCount < ships.length) {
			//get the ship size
			shipSize = ships[currShip];
			//reset flags
			shipPlaced = false;			
			//find a place to initially attempt to place the ship
			initialShipPlacement();	
			int initialStart = startPos;
			int initialEnd = endPos;
			//while we haven't placed the ship down
			while(shipPlaced != true) {					
				//check if this move is valid
				moveValid = isValid(bestBoard);				
				//if this move is a valid move
				if(moveValid == true) {						
					//place the ship on the board
					bestBoard = placeShip(bestBoard);					
					//set the shipPlaced flag
					shipPlaced = true;
					//unset the ignorePerimeters flag
					ignorePerimeters = false;
					//increment the number of ships placed and our next ship counter
					shipsPlacedCount++;
					currShip++;
					//change the next ships placement direction
					if(vertical == true) {
						vertical = false;
					}
					else {
						vertical = true;
					}
				}				
				//if this is not a valid move we need to put the ship elsewhere
				else {
					nextPossibleShipPos(vertical);
					if(initialStart == startPos && initialEnd == endPos) {
						ignorePerimeters = true;
					}
				}	
				
			}
		}
	}
    
	// -------------------------------------------------------------------------------------------------------------------
	
	//find the next possible place to put the ship (DOES NOT CHECK VALIDITY)
	public static void nextPossibleShipPos(boolean vert) {
		
		//if we are currently placing ships vertically
		if(vert == true) {		
			//check we can move the ship down and remain within horizontal bounds
			if((endPos + N) < (N * N)) {
				startPos+=N;
				endPos+=N;
			}
			//if we cant move the ship down we must move it to the next col
			else {
				//check if we have reached the end of the cols
				if(((startPos + 1) % N) == 0) {
					//move the ship to the next row that Yhits permits						
					//go through all the yHit values to find one that is equal to or greater than shipSize
					for(int i = 0; i < yHits.length; i++) {
						if(yHits[i] >= shipSize) {							
							rowPos = i;
							break;
						}
					}
					//set the new start and end position of our ship
					startPos = rowPos * N;
					endPos = startPos + shipSize - 1;
					//we are now placing the ship horizontally
					vertical = false;
				}
				//otherwise move the ship to the start of the next col
				else {
					//move the ship to the next col given xHits permits
					//go through all the next xHit values to find one that is equal to or greater than shipSize
					int nextCol = (startPos % N) + 1;
					boolean noColRoom = true;
					for(int i = nextCol; i < xHits.length; i++) {
						if(xHits[i] >= shipSize) {
							noColRoom = false;
							colPos = i;
							break;
						}
					}
					//if there are no more valid columns for this ship, switch to rows
					if(noColRoom == true) {
						for(int i = 0; i < yHits.length; i++) {
							if(yHits[i] >= shipSize) {
								
								rowPos = i;
								break;
							}
						}
						//set the new start and end position of our ship
						startPos = rowPos * N;
						endPos = startPos + shipSize - 1;
						//we are now placing the ship horizontally
						vertical = false;
					}
					else {
						//set the new start and end position of the ship
						startPos = colPos;
						endPos = startPos + ((shipSize - 1) * N);
					}
				}
			}
		}
		//if we are currently placing ships horizontally
		else {
			//check we can move the ship across and remain within vertical bounds
			if( ((endPos + 1) % N) != 0) {
				startPos++;
				endPos++;
			}
			//if we cant move the ship across we must move it to the next row
			else{
				//check if we have reached the end of the rows
				if( (startPos + N) >= (N*N) ) {
					//move the ship to the next col given xHits permits
					//go through all xHit values to find one that is equal to or greater than shipSize
					for(int i = 0; i < xHits.length; i++) {
						if(xHits[i] >= shipSize) {
							colPos = i;
							break;
						}
					}
					startPos = colPos;
					endPos = startPos + ((shipSize - 1) * N);
					//we are now placing the ship vertically
					vertical = true;
				}
				//otherwise move the ship to the start of the next row
				else {
					//move the ship to the next row that yHits permits
					//go through all the yHit values to find one that is equal to or greater than shipSize
					int nextRow = (startPos / N) + 1;
					boolean noRowRoom = true;
					for(int i = nextRow; i < yHits.length; i++) {
						if(yHits[i] >= shipSize) {
							noRowRoom = false;
							rowPos = i;
							break;
						}
					}
					//if there is no more valid rows for the ship, then switch to cols
					if(noRowRoom == true) {
						for(int i = 0; i < xHits.length; i++) {
							if(xHits[i] >= shipSize) {
								colPos = i;
								break;
							}
						}
						startPos = colPos;
						endPos = startPos + ((shipSize - 1) * N);
						//we are now placing the ship vertically
						vertical = true;
					}
					else {
						startPos = rowPos * N;
						endPos = startPos + shipSize - 1;
					}
				}
			}
		}
	}
	
	// -------------------------------------------------------------------------------------------------------------------
		
	
	//places a ship on the board
	public static BitSet placeShip(BitSet board) {

		//to keep track of the length of this ship
		int sizeCounter = 0;		
		//place the ship down depending on which direction we are placing it
		if(vertical == true) {
			for(int i = startPos; i <= endPos; i+=N) {
				// set the bit at i to a 1	
				board.set(i);
				sizeCounter++;
			}
		}	
		else {
			for(int i = startPos; i <= endPos; i++) {
				//set the bit at i to a 1
				board.set(i);
				sizeCounter++;
			}
		}		
		//create a new ship if this is the initial solution
		if(initial == true) {
			Ship toPlace = new Ship(startPos, endPos, sizeCounter, vertical);
			shipsOnBoard.add(toPlace);
		}
		//return the updated board
		return board;
	}
	
	// -------------------------------------------------------------------------------------------------------------------
	
	//checks if a ship placing is a valid move or not
	public static boolean isValid(BitSet board) {
		
		//reset the position valid flag. Set to true as the position has not been proven invalid
		boolean valid = true;
		boolean leftBounds, rightBounds;
		int loopStart, loopEnd, width;
		
		if(!ignorePerimeters) {
			//check if we have sea above or below us or we are against the left or right
			//if start is not against top of board
			if(startPos - N >= 0) {
				//if the value directly above the start position is not empty 
				if(board.get((startPos - N)) == true){
					//this move is invalid
					valid = false;
				}
			}
			//if the end is not against the bottom of the board
			if((endPos + N) < (N * N)) {
				//if the value directly below the end position is not empty
				if(board.get((endPos + N)) == true){
					//this move is invalid
					valid = false;
				}
			}
		}
		//if we are against the left
		if((startPos - 1) % N  == N - 1) {
			leftBounds = true;
		}
		else {
			leftBounds = false;
		}
		//if we are against the right
		if(((startPos + 1) % N) == 0) {
			rightBounds = true;
		}
		else {
			rightBounds = false;
		}
		
		//use a nested for loop structure to check the ship area + perimeter 
		//get the start and end position of our loop
		
		if(!ignorePerimeters) {
			loopStart = startPos - N - 1;
			loopEnd = endPos + N;
			//specify the width of our check zone					
			if(vertical == true) {
				//if its vertical then the width can only be three
				width = 3;
			}
			else {
				//if horizontal the width is the ship + 1 perimeter on either side of ship
				width = shipSize + 2;
			}
		}
		else {
			loopStart = startPos;
			loopEnd = endPos + 1;
			if(vertical == true) {
				//if its vertical then the width can only be one
				width = 1;
			}
			else {
				//if horizontal the width is the ship 
				width = shipSize;
			}
		}
		
		
		
		//loops through each row of to be ship + perimeter 
		for(int i = loopStart; i < loopEnd; i+= N) {
			//loops through each element in row of specified width
			for(int j = i; j < i + width; j++) {
				//if this ship placing is invalid
				if (valid == false) {
					break;
				}
				//if we are within board upper and lower bounds
				if(j >= 0 && (j < (N * N))) {
					//if this ship placing is against the left bounds, check that we are within bounds
					if((leftBounds == true && ((j % N) != (N - 1))) || leftBounds == false) {
						//if this ship placing is against the right bounds, check we are within bounds
						if((rightBounds == true && ((j % N) != 0)) || rightBounds == false) {
							//check if the bit is aready a ship
							if(board.get(j) == true) {
								valid = false;
							}
						}
					}
				}
			}
			if (valid == false) 
				break;
		}
		//return the validity of this move
		return valid;
	}
	
	// -------------------------------------------------------------------------------------------------------------------

	//calculates the fitness of a board (the score)
	public static int calculateFitness(BitSet board) {
		
		int diffY = 0;
		int diffX = 0;
		//loop through each row
		for (int i = 0; i < N; i++) {
			int startRow = i*10;
			int rowTotal = 0;
			//iterate through each bit in the row
			for (int j = startRow; j < startRow + N; j++) {
				//if ship is encountered increment the counter
				if (board.get(j) == true) {
					rowTotal++;
				}
			}
			//get the difference between the yHits value and the number of ship segments in that row
			diffY+=Math.abs(yHits[i] - rowTotal); 
		}
		//loop through each column
		for (int i = 0; i < N; i++) {
			int colTotal = 0;
			//iterate through each bit in the column
			for (int j = i; j < N*N; j+=N) {
				if (board.get(j) == true) {
					colTotal++;
				}
			}
			//get the difference between the xHits value and the number of ship segments in that column
			diffX+=Math.abs(xHits[i] - colTotal);
		}
		//return the total
		return (diffY + diffX);
	}
 }
 
 // -------------------------------------------------------------------------------------------------------------------

 //This is the inner Ship class. It holds information on each ship placed on the board
 class Ship {
 	public int startPos_;
 	public int endPos_;
 	public int size_;
 	public boolean vertical_;
	
	//create a ship given its start and end position, size and its direction
 	public Ship(int startPos, int endPos, int size, boolean vertical) {
 		startPos_ = startPos;
 		endPos_ = endPos;
 		size_ = size;
 		vertical_ = vertical;
 	}
 }