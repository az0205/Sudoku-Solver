package sudoku;

import java.io.IOException;

public class Run {
	
    public static void main(String[] args) throws IOException {

		SudokuGUI s = new SudokuGUI(); //instantiates the Sudoku GUI for the user
		s.setSize(800,850); //sets size of the window
		s.setTitle("Sudoku Solver"); //sets title of the window
		s.setVisible(true); //sets the window to be visible
    	
    }

}