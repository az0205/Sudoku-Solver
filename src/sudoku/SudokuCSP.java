package sudoku;

import java.util.*;

/* SudokuCSP is the class holding all the CSP relevant variables and methods */
public class SudokuCSP {
	
	// the domain for each cell in the sudoku ranging from 1 to 9 stored in an int array
	public int domain [] = {1,2,3,4,5,6,7,8,9};
	
	/* selectUnassignedVar is the method to select the next unassigned variable in the board. Initially we have made it start from the
	 * top left to the bottom right of the board to find unassigned variables. However to make it more efficient and reduce the number
	 * of backtracks we incorporated the Minimum Remaining Values (MRV) Heuristic sending the variable that has the least set of
	 * legal values remaining therefore increasing efficiency. It takes the parameter of the 2D sudoku array and returns an array containing
	 * two values, the row location and column location. */
	public int[] selectUnassignedVar(int[][] sudoku) {
		int minVals = Integer.MAX_VALUE;
		int[] current = new int[2];
		int[] var = null;
		int currNum;
		/* Loops through all variables in the sudoku that are empty by checking if it's 0. Then calls the numOfLegalVals that returns how many
		 * legal values this variable has. If it is less than the minimum values, this becomes the new minimum and current variable is set as 'var'.
		 * By the end of the loop there should be the 'var' variable that holds the coordinates of the variable with the least legal values out
		 * of all empty variables */
		for(int i = 0; i < 9; i++) {
			
			for(int j = 0; j <9; j++) {
				
				if (sudoku[i][j] == 0) {
					current[0] = i;
					current[1] = j;
					currNum = numOfLegalVals(sudoku, i, j);
					
					if ( currNum < minVals) {
						minVals = currNum;
						var = new int[] { i, j };
					}
					
				}
			}
		}
		return var;
	}
	
	/* This method used by selectUnassignedVar for MRV and used for forwardChecking to collect an integer stating the number of legal values a variable has.
	 * Takes the parameter of the 2D sudoku in order to send it to isConsistent to check if the var and val are consistent with the constraints. Also takes
	 * row and column parameters as separate ints to also send to isConsistent. The function loops through all the values in the domains and checks if they are
	 * consistent with the current variable. If they are the count goes up meaning the number of legal values goes up by one. Then after going through all domain
	 * it returns that count which is the definition of the number of legal values for this variable. */
	public int numOfLegalVals(int[][] sudoku, int row, int col) {
		int count = 0;
		int[] var = new int[] {row, col};
		for (int val : domain) {
			
			if (isConsistent(sudoku, var, val))
				count++;
			
		}
		return count;
	}
	
	/* This function is used as a replacement for domain in the main function in order to incorporate LCV. Rather than looping through all the domain values in
	 * order (1-9), this function will instead send back an array similar to domain however with the values rearranged in an order starting from a value which
	 * once assigned to the current current variable, will rule out the fewest possibilities of the other variables in the puzzle. Takes parameter sudoku to use
	 * for trial assigning different values to the other parameter var (the current variable) to see the impact it makes to other variables in order to get an
	 * ordered LCV list of the domain values. It will return back an integer array of the list of values in LCV order. */
	public int[] getLCV(int[][] sudoku, int[] var){
		// Extract the row and column from var
		int row = var[0];
		int col = var[1];
		
		/* Array list values to store only the valid constraint consistent values by sending all the values (domain) to isConsistent for the current variable.
		 * This will allow the next parts of the LCV process to work with a smaller domain which drastically reduces the runtime. We used ArrayList as size could range */
		ArrayList<Integer> values = new ArrayList<Integer>();
		for(int val : domain) {
			
			if (isConsistent(sudoku, var, val))
				values.add(val);
			
		}

		/* ArrayList of scores which stores a list of integer arrays. Each array to hold two values, one being one of the legal values for the current variable and
		 * the other being the total legal possibilities this variable assigned to this value allows for its neighbors (i.e. cells on its same row, column, and
		 * 3x3 box) */
		ArrayList<int[]> scores = new ArrayList<int[]>();
		
		// possibilities to count total legal values allowed for all neighbors of current variable when this variable is assigned a particular value
		int possibilities;
		// count to count number of legal values for each single neighbor of current variable which is then added on to possibilities
		int count;
		
		/* Will loop through all the legal values for the variable to be trial assigned to however this assignment is not official nor part of the main backtrack process,
		 * it is only to check its affects on neighbor variables for all legal values. */
		for (int val : values) {
			
			// Trial assigns the value to the variable
			sudoku[row][col] = val;
			// Sets possibilities to 0 every time it loops for a new value assigned to the variable. This will later be added to the scores array list after one value is complete
			possibilities = 0;
			
			/* Calls getNeighbours method that takes the variable and will return all other variables it has an effect to (i.e variables in its same row, column and
			 * 3x3 box). An array list of variables is returned back where variables are stored in an integer array where first index is row, second is column. The for
			 * loop loops through each neighbor variable. Checks that it is empty first, initialises count as 0 to then use isConsistent method to count number of legal values
			 * for all values in the domain. After this is recorded, it is added on to possibilities. Then the loop repeats for the next neighbor. */
			for(int[] neighbour : getNeighbours(row, col)) {
				
				int nRow = neighbour[0], nCol = neighbour[1];
				
				if (sudoku[nRow][nCol] == 0) {
					
					count = numOfLegalVals(sudoku, nRow, nCol);
					
					possibilities += count;
				}
				
			}
			/* Once all neighbors are checked, possibilities will now store all legal values from all neighbors for the particular value that the current variable is
			 * assigned to. This is now added to scores array list where possibilities is paired with the value assigned to the variable that resulted in this data. The
			 * variable is then initialised to 0, and the loop repeats for the next legal value where a new possibilities number will be calculated and whole process repeats*/
			scores.add(new int[] {val, possibilities});
			sudoku[row][col] = 0;
			
		}
		
		/* scores now will store all the values of the variables paired with number of total possibilities allowed for its neighbours. Using sort, we sort possibilities (as it's
		 * stored in index 1) in descending order so the most legal values allowed for a particular value assignment is shown first. Then a for loop will collect only the values (using
		 * index 0) in that same order and store it into a new int array which will finally return the list of values ordered by least constraining value to the backtrack recursive
		 * method therefore incorporating the LCV Heuristic making it much more efficient with fewer backtracks.*/
		scores.sort((a,b) -> b[1] - a[1]);
		
		int[] orderedVals = new int[scores.size()];
		
		for(int i = 0; i<scores.size(); i++) {
			orderedVals[i] = scores.get(i)[0];
		}
		
		// The loop in backtrack method will now go through each value in the LCV order using orderedVals
		return orderedVals;
	}
	
	/* This is the helper array getNeighbours for getLCV that takes a variable's row and column and returns an array list of variables stored in int arrays. */
	public ArrayList<int[]> getNeighbours(int row, int col) {
		
		ArrayList<int[]>neighbours = new ArrayList<int[]>();
		
		/* Loops through all variables in the current variable's rows and columns (except itself) and adds them to the array list neighbours */
		for(int i = 0; i < 9; i++) {
			
			if (i != col) {
				neighbours.add(new int[] {row, i});
			}
			
			if (i != row) {
				neighbours.add(new int[] {i, col});
			}
			
		}
		
		/* Loops through all variables in the current variable's 3x3 box (except itself) and adds them to the array list neighbours */
		int boxRow = (row/3) * 3;
		int boxCol = (col/3) * 3;
		for (int i = 0; i < 3; i++) {
			
			for (int j = 0; j < 3; j++) {
				int r = boxRow + i, c = boxCol + j;
				
				if (!(r == row && c == col)) {
					neighbours.add(new int[] {r,c});
				}
				
			}
			
		}
		return neighbours;
	}
	
	/* To check if a particular value can be legally assigned to a particular variable whilst keeping consistent with the constraints. Takes parameters, sudoku
	 * 2D array, variable and the value in order to check the constraints by assigning the value to the variable and comparing this to the rest of the sudoku. Method
	 * returns boolean indicating if this assignment was consistent to constraints or not */
	public boolean isConsistent(int[][] sudoku, int[] var, int val) {
		// Separates var into int row and col to be used in sudoku array
		int row = var[0];
		int col = var[1];
		
		// Row constraint checks that all variables in the same row as var don't have the value that is going to be assigned to var. If it is, returns false.
		for(int j = 0; j<9; j++) {
			if (sudoku[row][j] == val)
				return false;
		}
	
		// Column constraint checks that all variables in the same column as var don't have the value that is going to be assigned to var. If it is, returns false.
		for(int i = 0; i<9; i++) {
			if (sudoku[i][col] == val)
				return false;
		}
		
		// 3x3 box constraint checks that all variables in the same 3x3 box as var don't have the value that is going to be assigned to var. If it is, returns false.
		int boxRow = (row/3) * 3; // this calculation gives the row index of the 3x3 box out of a 3x3 board of 3x3 boxes
		int boxCol = (col/3) * 3; // similarly this calculation does the same for the column index
		
		for (int i = 0; i<3; i++) {
			
			for (int j = 0; j<3; j++) {
				if (sudoku[boxRow+i][boxCol+j] == val) // adds i to boxRow and j to boxCol to get exact coordinates by first finding the correct box and every variable in it
					return false;
			}
			
		}
		// returns true if there was no conflicts in the rows, columns and boxes therefore stating this assignment is consistent
		return true;
	}
}