package sudoku;
import javax.swing.*;

public class Sudoku {
	
	// below are the metrics to be recorded throughout the solving process (number of backtracks, total time in ms)
	private int backtracks = 0;
	private long totalTime;
	
	// Returns total backtracks, used by the Sudoku GUI to display the backtracks
	public int getBacktracks() {
		return backtracks;
	}
	// Returns total time in milliseconds, used by the Sudoku GUI to display the total time
	public long getTotalTime() {
		return totalTime;
	}
	
	/* The main recursive backtrack function, takes the parameters sudoku 2D array to update and Sudoku CSP to
	 * access its functions, domains and values. It is recursive and recursion ends whenever it returns true or false.
	 * If, after going through all possible domains for a variable and none of them are consistent/valid, then it returns false,
	 * indicating it is an invalid sudoku. It returns true once selectUnassignedVar returns null which means all the variables
	 * are successfully assigned. Also returns true if a recursive backtrack call was successful meaning all variables were assigned
	 * in the recursion therefore goes up the chain with trues meaning the sudoku is solved. If a recursive call was unsuccessful, then we reassign
	 * the sudoku variable back to 0 and increment backtrack as we have gone back and will try again for a new value. */
	private boolean backtrack(int[][] sudoku, SudokuCSP csp, JLabel[][] cells){
		
		/* var array stores the row in position 0 and column in position 1. Calls the sudoku csp's function to select an unassigned
		 * variable using Minimum Remaining Values (MRV) Heuristic. This will return the variable and store it in var*/
		int[] var = csp.selectUnassignedVar(sudoku);
		/* If var is null this means there are no more unassigned variables so the sudoku is assumed solved therefore returns true */
		if (var == null)
			return true;
		
		int row = var[0];
		int col = var[1];
		
		/* for loop that loops through each value with the use of Least Constraining Value (LCV) Heuristic where the values
		 * start from the one which rules out the fewest future possibilities each time it loops until a valid value is found.
		 * This way, it reduces the number of backtracks. For LCV we have used a function in CSP called getLCV that takes the
		 * sudoku and the current variable and returns the best value that least impacts other variable's possible values. Inside
		 * the for loop we initially used the isConsistent method to continue with the process to see if the value stays within
		 * the constraints of the variable. However using this inside the getLCV method made the overall runtime drastically
		 * faster as getLCV works with less values and so will return not only an ordered set of domains but also constraint
		 * consistent ones. */
		for (int val : csp.getLCV(sudoku, var)) {
			/* Stores the value into the sudoku and also updates the GUI with setText and repaint.
			 * If no value was consistent, it will skip the for loop and return false returning to the previous recursion where
			 * it will set the sudoku variable back to empty and increment backtrack, to then possibly use the next LCV. */
			sudoku[row][col] = val;
			cells[row][col].setText(val== 0 ? "" : String.valueOf(val));
			cells[row][col].repaint();
			
			/* Thread sleep to add the delays in order to visualize the backtrack process on the GUI. In order to display accurate
			 * total time taken for the solving process keep as 0 */
			try {
				Thread.sleep(0);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			/* Uses forward check to see if there are still valid values for other variables once the current value
			 * has been assigned to the current variable. If it is not, then the current variable will reset to 0 and
			 * return false going to the previous recursion. This is efficient as it skips doing more unnecessary recursions
			 * wasting time as the code now knows assigning this current value to this current variable is entirely wrong. If
			 * forward check returns true, then we can proceed to performing the next recursion. If the recursion returns true
			 * that means a solution is found so this will also return true and go up the chain. If it is false, then assign current
			 * variable back to empty and return false to move back for either the next LCV or failure */
			if (forwardCheck(sudoku, row, col, csp)) {
				if(backtrack(sudoku, csp, cells))
					return true;
				// backtracks increment once there's a failure to allow a new value to be picked implying the function has backtracked
				backtracks++;
			}
			
			sudoku[row][col] = 0;
			cells[row][col].setText("");
			cells[row][col].repaint();
			
			try {
				Thread.sleep(0);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
		
		return false;
		
	}
	
	/* forwardCheck function takes the parameter: sudoku 2D array, the row index, column index and csp to access the number of legal vals function
	 * There's a loop for both the rows and columns and the 3 by 3 boxes. Checks if those cells are empty and that the number of legal values is 0
	 * so if that is the case the function needs to backtrack immediately so returns false. This is done for every row, every column and
	 * every 3x3 box the new variable is part of. It doesn't check all the cells because the new variable only affects these areas so in summary
	 * if this particular variable is given this particular value and it causes one or more variable to not have any more legal values, it must
	 * backtrack as this path will end in failure
	 */
	private boolean forwardCheck(int [][] sudoku, int row, int col, SudokuCSP csp) {
		// Checks number of legal values for empty cells part of same rows and columns of current variable
		for(int i = 0; i<9; i++) {
			if (sudoku[row][i] == 0 && csp.numOfLegalVals(sudoku, row, i) == 0)
				return false;
			if (sudoku[i][col] == 0 && csp.numOfLegalVals(sudoku, i, col) == 0)
				return false;

		}
		
		// Checks number of legal values for empty cells part of the same 3 by 3 box of current variable
		int boxRow = (row/3) * 3;
		int boxCol = (col/3) * 3;
		for (int i = 0; i<3; i++) {
			for (int j = 0; j<3; j++) {
				if (sudoku[boxRow+i][boxCol+j] == 0 && csp.numOfLegalVals(sudoku, boxRow+i, boxCol+j) == 0)
					return false;
			}
		}
		// Returns true if all these variables have at least one legal value
		return true;
	}
	
	/* The main solver function that calls the backtrack function. Takes the parameter sudoku 2D array read from a text file from the SudokuGUI 
	 * that stores it into the 2D array. Takes the parameter JLabel cells from SudokuGUI in order to send to backtrack so that backtrack can update
	 * the GUI whenever a cell is updated in the sudoku during backtrack so user can visualize the backtrack process. It returns the result which
	 * states if the sudoku solution was found or not so that GUI can display a relevant change to indicate the result to the user (successful or not) */
	public boolean solver(int[][] sudoku, JLabel[][] cells) {
		// Every time the solver is called, backtrack resets to 0 to not add on when user solves again for a new puzzle
		backtracks = 0;
		SudokuCSP csp = new SudokuCSP(); // Instantiates the CSP class
		
		long startTime = System.currentTimeMillis(); // To record time we log the start time
		
		/* the backtrack function will return false in failure or true if the sudoku was solvable storing it in result. Sends the sudoku, CSP and
		 * JLabel cells to be updated */
		boolean result = backtrack(sudoku, csp, cells);
		
		// End time recorded after the backtrack recursive process is finally over
		long endTime = System.currentTimeMillis();
		
		// If the result is true (meaning there is a solution), print out the solution in the terminal other wise print "Failed"
		if(result) {
			
			System.out.println("Solved \n");
			
			for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                	System.out.print(sudoku[i][j]+" ");
                }
                System.out.println(); 
			}
		}
		else
			System.out.print("Failed \n");
		
		// Prints out the total backtrack and finds the difference between start and end time to print out total time taken
		System.out.println("Total Backtracks: " + backtracks);
		totalTime = endTime - startTime;
		System.out.println("Total time taken: " + totalTime + " ms \n");
		
		// Returns the result for SudokuGUI
		return result;
	}


}